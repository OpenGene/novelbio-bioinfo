package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

/**
 * 小RNA比对流水线
 * @author zong0jie
 *
 */
public class MirSpeciesPipline {
	private static final Logger logger = Logger.getLogger(MirSpeciesPipline.class);
	/** 序列文件 */
	Map<String, String> mapPrefix2Fastq = new LinkedHashMap<>();
	/** 输出的临时文件夹，主要保存mapping的中间文件 */
	String outPath;
	String outPathTmpMapping;
	List<Species> lsSpecies;
	/** bwa所在的路径 */
	GeneExpTable expMirMature;
	GeneExpTable expMirPre;
	////////////////////// 输出文件名 /////////////////////////////
	String samFileOut = null;
	
	int threadNum = 3;
	
	boolean overlap = false;
	
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	public void setOverlap(boolean overlap) {
		this.overlap = overlap;
	}
	public void setLsSpecies(List<Species> lsSpecies) {
		this.lsSpecies = lsSpecies;
	}
 
	/**
	 * @param prefix 前缀
	 * @param seqFile 输入的fastq文件
	 */
	public void addSample(String prefix, String fastqFile) {
		this.mapPrefix2Fastq.put(prefix, fastqFile);
	}
	/** 设定待比对到其他物种的序列 */
	public void setMapPrefix2Fastq(Map<String, String> mapPrefix2Fastq) {
		this.mapPrefix2Fastq = mapPrefix2Fastq;
	}
	private String getOutputPrefix(String prefix) {
		if (prefix != null && !prefix.trim().equals("")) {
			if (!prefix.endsWith("_")) {
				prefix = prefix.trim() + "_";
			}
		}
		return prefix;
	}
	/** 设定输出文件夹，必须是文件夹 */
	public void setOutPathTmp(String outPath) {
		this.outPath = FileOperate.addSep(outPath);
		this.outPathTmpMapping = this.outPath + "tmpMapping/";
	}
	public void setExpMir(GeneExpTable expMirPre, GeneExpTable expMirMature) {
		this.expMirPre = expMirPre;
		this.expMirMature = expMirMature;
	}
	/** 最后比对获得的Sam文件，可用于统计没有比对上的reads数 */
	public SamFile getOutSeq() {
		if (FileOperate.isFileExistAndBigThanSize(samFileOut, 0)) {
			return new SamFile(samFileOut);
		}
		return null;
	}

	/** mapping的流水线 */
	public void mappingPipeline(String rnadatFile, SamMapRate samMapRate) {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.bowtie2);
		FileOperate.createFolders(outPathTmpMapping);
		for (int i = 0; i < lsSpecies.size(); i++) {
			Species species = lsSpecies.get(i);

			MiRNACount miRNACount = new MiRNACount();
			miRNACount.setSpecies(species, rnadatFile);
			miRNACount.setExpTableWithoutLsGeneName(expMirPre, expMirMature);
			for (String prefix : mapPrefix2Fastq.keySet()) {
				String outputPrefix = getOutputPrefix(prefix);
				String fastqFile = mapPrefix2Fastq.get(prefix);
				String outFastq = outPathTmpMapping + outputPrefix + species.getCommonName() + "_unmapped.fq.gz";
				samFileOut = outPathTmpMapping + outputPrefix + species.getCommonName() + ".bam";
				SamFileStatistics samFileStatistics = new SamFileStatistics(prefix);
				samFileOut = MiRNAmapPipline.mappingBowtie2(overlap, samFileStatistics, softWareInfo.getExePath(), threadNum, fastqFile, species.getMiRNAhairpinFile(), samFileOut, outFastq);
				
				if (samMapRate != null) {
					samMapRate.addMapInfo(species.getCommonName() + "_miRNA", samFileStatistics);
					if (i == lsSpecies.size() - 1) {
						samMapRate.addUnmapInfo(samFileStatistics);
					}
				}
				
				miRNACount.setAlignFile(new SamFile(samFileOut));
				miRNACount.run();
				expMirMature.setCurrentCondition(prefix);
				expMirMature.addAllReads(miRNACount.getCountMatureAll());
				expMirMature.addLsGeneName(getLsGeneNot0(miRNACount.getMapMirMature2Value()));
				expMirMature.addGeneExp(miRNACount.getMapMirMature2Value());
				
				expMirPre.setCurrentCondition(prefix);
				expMirPre.addAllReads(miRNACount.getCountPreAll());
				expMirPre.addLsGeneName(getLsGeneNot0(miRNACount.getMapMiRNApre2Value()));
				expMirPre.addGeneExp(miRNACount.getMapMiRNApre2Value());
				mapPrefix2Fastq.put(prefix, outFastq);
				
				CtrlMiRNAfastq.writeFile(false, outPath + prefix + FileOperate.getSepPath() + prefix + "_BlastTo" 
				+ species.getCommonName() + "_Pre_Counts.txt", expMirPre, EnumExpression.Counts);
				CtrlMiRNAfastq.writeFile(false, outPath + prefix + FileOperate.getSepPath() + prefix + "_BlastTo" 
						+ species.getCommonName() + "_Mature_Counts.txt", expMirMature, EnumExpression.Counts);
			}
		}
	}
	
	/** 获得所有不为0的geneName */
	private List<String> getLsGeneNot0(Map<String, Double> mapGeneName2Value) {
		List<String> lsName = new ArrayList<>();
		for (String geneName : mapGeneName2Value.keySet()) {
			Double value = mapGeneName2Value.get(geneName);
			if (value == null || value == 0) {
				continue;
			}
			lsName.add(geneName);
		}
		return lsName;
	}
	
	/** 当运行完pipeline后，返回的就是prefix和最后UnMappedFastq */
	public Map<String, String> getMapPrefix2Fastq() {
		return mapPrefix2Fastq;
	}
	
	public void writeToFile() {
		CtrlMiRNAfastq.writeFile(true, outPathTmpMapping + "blastMirPreAll_Counts.txt", expMirPre, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(true, outPathTmpMapping + "blastMirMatureAll_Counts.txt", expMirMature, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(true, outPathTmpMapping + "blastMirPreAll_UQTPM.txt", expMirPre, EnumExpression.UQPM);
		CtrlMiRNAfastq.writeFile(true, outPathTmpMapping + "blastMirMatureAll_UQTPM.txt", expMirMature, EnumExpression.UQPM);
	}
}
