package com.novelbio.analysis.seq.mirna;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;
import com.novelbio.generalConf.TitleFormatNBC;

public class CtrlMiRNApipeline {
	private static final Logger logger = Logger.getLogger(CtrlMiRNApipeline.class);
	Species species;
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.miRNAName);
	SamMapRate samMapMiRNARate = new SamMapRate();
	
	CtrlMiRNAfastq ctrlMiRNAfastq;
	CtrlMiRNApredict ctrlMiRNApredict = new CtrlMiRNApredict();
	boolean mapMirna = true;
	boolean predictMirna = true;
	
	List<Species> lsSpeciesBlastTo;
	String outPath;
	boolean isPredictAlready = false;
	Map<String, AlignSeq> mapPrefix2AlignFile;
	Map<String, String> mapPrefix2Fastq;
	
	public CtrlMiRNApipeline(Species species) {
		this.species = species;
	}
	/** 输入数据 */
	public void setMapPrefix2Fastq(Map<String, String> mapPrefix2Fastq) {
		this.mapPrefix2Fastq = mapPrefix2Fastq;
	}
	/** 如果从头开始就不需要设定该值 */
	public void setMapPrefix2AlignFile(Map<String, AlignSeq> mapPrefix2AlignFile) {
		this.mapPrefix2AlignFile = mapPrefix2AlignFile;
	}
	/** 是否比对到本物种上 */
	public void setMapMirna(boolean mapMirna) {
		this.mapMirna = mapMirna;
	}
	public void setPredictMirna(boolean predictMirna) {
		this.predictMirna = predictMirna;
	}
	/** miRNA是否已经预测完毕 */
	public void setPredictAlready(boolean isPredictAlready) {
		this.isPredictAlready = isPredictAlready;
	}
	/** 设定比对到的物种，不设定就不做这步分析 */
	public void setLsSpeciesBlastTo(List<Species> lsSpeciesBlastTo) {
		this.lsSpeciesBlastTo = lsSpeciesBlastTo;
	}
	/** 设定输出路径 */
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	/** 读取已有的miRNA信息 */
	public void readExistMiRNA(String miRNApreFile, String miRNAmatureFile) {
		expMirPre.read(miRNApreFile, true);
		expMirMature.read(miRNAmatureFile, true);
	}

	public void run() {
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		ctrlMiRNAfastq = new CtrlMiRNAfastq();
		ctrlMiRNAfastq.setMiRNAexp(expMirPre, expMirMature);
		if (mapMirna) {
			runMapping(gffChrAbs, species, mapPrefix2Fastq);
			mapPrefix2AlignFile = ctrlMiRNAfastq.getMapPrefix2GenomeSam();
		}
		if (predictMirna) {
			if (isPredictAlready) {
				runPredictAlready(mapPrefix2AlignFile);
			} else {
				runPredict(mapPrefix2AlignFile, gffChrAbs, species);
			}
			mapPrefix2Fastq = ctrlMiRNApredict.getMapPrefix2UnmapFq();
		} else {
			mapPrefix2Fastq = convertAlign2Fq(mapPrefix2AlignFile);
		}
		if (lsSpeciesBlastTo.size() > 0) {
			blastToOtherSpecies();
		}
		CtrlMiRNAfastq.writeFile(true, outPath + "/miRNA_All_Counts", expMirMature, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(true, outPath + "/miRNA_All_UQ", expMirMature, EnumExpression.UQPM);

		CtrlMiRNAfastq.writeFile(true, outPath + "/miRNAPre_All_Counts", expMirPre, EnumExpression.Counts);
		CtrlMiRNAfastq.writeFile(true, outPath + "/miRNAPre_All_UQ", expMirPre, EnumExpression.UQPM);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPath + "/miRNAmappingStatistics", true);
		txtWrite.ExcelWrite(samMapMiRNARate.getLsResult());
		txtWrite.close();
	}
	
	/** 将sam文件转化为fastq文件 */
	private Map<String, String> convertAlign2Fq(Map<String, AlignSeq> mapAlign2Prefix) {
		Map<String, String> mapPrefix2unmapFastq = new HashMap<>();
		for (String prefix : mapAlign2Prefix.keySet()) {
			AlignSeq alignSeq = mapAlign2Prefix.get(prefix);
			mapPrefix2unmapFastq.put(prefix, alignSeq.getFastQ().getReadFileName());
		}
		return mapPrefix2unmapFastq;
	}
	
	private void runMapping(GffChrAbs gffChrAbs, Species species, Map<String, String> mapPrefix2Fastq) {
		ctrlMiRNAfastq.setMappingAll2Genome(false);
		ctrlMiRNAfastq.setRfamSpeciesSpecific(false);
		ctrlMiRNAfastq.setSpecies(species);
		ctrlMiRNAfastq.setOutPath(outPath);
		ctrlMiRNAfastq.setGffChrAbs(gffChrAbs);
		ctrlMiRNAfastq.setMapPrefix2Fastq(mapPrefix2Fastq);
		ctrlMiRNAfastq.setMiRNAinfo(PathDetailNBC.getMiRNADat());
		ctrlMiRNAfastq.setRfamFile(PathDetailNBC.getRfamTab());
		ctrlMiRNAfastq.setMapAll2Rfam(true);
		ctrlMiRNAfastq.mappingAndCounting(samMapMiRNARate);
//		ctrlMiRNAfastq.writeToFile();
		logger.info("finish mapping");
	}
	/** 从头预测 */
	private void runPredict(Map<String, AlignSeq> mapBedFile2Prefix, GffChrAbs gffChrAbs, Species species) {
		ctrlMiRNApredict.setGffChrAbs(gffChrAbs);
		ctrlMiRNApredict.setSpecies(species);
		ctrlMiRNApredict.setMapPrefix2GenomeSamFile(mapBedFile2Prefix);
		ctrlMiRNApredict.setOutPath(outPath);
		ctrlMiRNApredict.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		ctrlMiRNApredict.setExpMir(expMirPre, expMirMature);
		ctrlMiRNApredict.predictAndCalculate(samMapMiRNARate);
		ctrlMiRNApredict.writeToFile();
		logger.info("finish predict");
	}
	
	/** 已经预测好了算下表达就行了 */
	private void runPredictAlready(Map<String, AlignSeq> mapPrefix2GenomeSam) {
		ctrlMiRNApredict.setMapPrefix2GenomeSamFile(mapPrefix2GenomeSam);
		ctrlMiRNApredict.setOutPath(outPath);
		ctrlMiRNApredict.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		ctrlMiRNApredict.setExpMir(expMirPre, expMirMature);
		ctrlMiRNApredict.predictAndCalculate(samMapMiRNARate);
		ctrlMiRNApredict.writeToFile();
		logger.info("finish predict");
	}
	
	/** 比对到别的物种 */
	private void blastToOtherSpecies() {
		MirSpeciesPipline mirSpeciesPipline = new MirSpeciesPipline();
		mirSpeciesPipline.setMapPrefix2Fastq(mapPrefix2Fastq);
		mirSpeciesPipline.setExpMir(expMirPre, expMirMature);
		mirSpeciesPipline.setLsSpecies(lsSpeciesBlastTo); 
		mirSpeciesPipline.setOutPathTmp(outPath);
		mirSpeciesPipline.setThreadNum(4);
		mirSpeciesPipline.mappingPipeline(PathDetailNBC.getMiRNADat(), samMapMiRNARate);
//		mirSpeciesPipline.writeToFile();
	}

}
