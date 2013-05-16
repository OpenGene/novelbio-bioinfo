package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.PathNBCDetail;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

/** 
 * 给定一系列fastq文件，获得miRNA的bed文件
 * @author zong0jie
 */
public class CtrlMiRNAfastq {
	public static void main(String[] args) {
		Species species = new Species(7955);
		MiRNACount miRNACount = new MiRNACount();
		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_RNA_DATA, species, PathNBCDetail.getMiRNADat());

		miRNACount.setMiRNAfile(species.getMiRNAhairpinFile(), species.getMiRNAmatureFile());
		miRNACount.setAlignFile(new SamFile("/media/winE/NBC/Project/Test/miRNA/jws/tmpMapping/G56_miRNA.bam"));
		miRNACount.run();
		miRNACount.writeResultToOut("/media/winE/NBC/Project/Test/miRNA/jws/tmpMapping/G56_miRNA_result.txt");
	
	}
	private static final Logger logger = Logger.getLogger(CtrlMiRNAfastq.class);
	
	Species species;
	/** 设定gff和chrome */
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	/** mapping 序列 */
	MiRNAmapPipline miRNAmappingPipline = new MiRNAmapPipline();
	
	MiRNACount miRNACount = new MiRNACount();
	RfamStatistic rfamStatistic = new RfamStatistic();
	ReadsOnRepeatGene readsOnRepeatGene = new ReadsOnRepeatGene();
	ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();

	/** fastqFile--prefix */
	ArrayList<String[]> lsFastqFile2Prefix;
	
	///////输出文件夹 //////////
	String outPath;
	String outPathTmpMapping;
	///////输出数量 ///////////
	HashMap<String, HashMap<String, Double>> mapPrefix2MiRNAPre = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2MiRNAmature = new HashMap<String, HashMap<String,Double>>();
	
	HashMap<String, HashMap<String, Double>> mapPrefix2MiRNArfam = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2MiRNAncrna = new HashMap<String, HashMap<String,Double>>();
	
	HashMap<String, HashMap<String, Double>> mapPrefix2RepeatName = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2RepeatFamily = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2GeneInfo = new HashMap<String, HashMap<String,Double>>();
	
	////// 没有mapping到的bed文件，用于预测新miRNA的 */
	Map<AlignSeq, String> mapNovelMiRNASamFile2Prefix = new LinkedHashMap<AlignSeq, String>();
	boolean rfamSpeciesSpecific = false;
	
	/** 务必首先设定 */
	public void setSpecies(Species species) {
		this.species = species;
	}
	/** 首先会判断两个gffChrAbs的species是否一致 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies())) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	
	public void setLsFastqFile(ArrayList<String[]> lsFastqFile2Prefix) {
		this.lsFastqFile2Prefix = lsFastqFile2Prefix;
	}
	
	/** 设定输出文件夹 */
	public void setOutPath(String outPath) {
		this.outPath = FileOperate.addSep(outPath);
		FileOperate.createFolders(this.outPath);
		this.outPathTmpMapping = this.outPath + "tmpMapping";
	}
	
	/**
	 * 是每个物种仅与本物种相关的rfam进行比较，还是与全体rfam文件进行比较
	 * 默认序列与全体rfam数据库进行比较
	 */
	public void setRfamSpeciesSpecific(boolean rfamSpeciesSpecific) {
		this.rfamSpeciesSpecific = rfamSpeciesSpecific;
	}
	
	/** rfam的信息比较文件，类似一个键值表 */
	public void setRfamFile(String rfamFile) {
		rfamStatistic.readRfamTab(rfamFile);
	}
	/** 是否全部mapping至genome上，默认为true */
	public void setMappingAll2Genome(boolean mappingAll2Genome) {
		miRNAmappingPipline.setMappingAll2Genome(mappingAll2Genome);
	}
	public void setMapAll2Rfam(boolean mappingAll2Rfam) {
		miRNAmappingPipline.setMappingAll2Seq(mappingAll2Rfam);
	}
	/**
	 * miRNA计算表达使用
	 * @param rnadatFile miRNA.dat文件
	 */
	public void setMiRNAinfo(String rnadatFile) {
		logger.error("读取的文件为：" + rnadatFile);
		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_RNA_DATA, species, rnadatFile);
	}
	
	/** 比对和计数，每比对一次就计数。主要是为了防止出错 */
	public void mappingAndCounting() {
		mapPrefix2MiRNAPre.clear();
		mapPrefix2MiRNAmature.clear();
		mapPrefix2MiRNArfam.clear();
		mapPrefix2MiRNAncrna.clear();
		
		FileOperate.createFolders(outPathTmpMapping);
		setConfigFile();
		for (String[] fastq2Prefix : lsFastqFile2Prefix) {
			//文件名为输出文件夹+文件前缀
			miRNAmappingPipline.setSample(fastq2Prefix[1], fastq2Prefix[0]);
			miRNAmappingPipline.setOutPathTmp(outPathTmpMapping);
			miRNAmappingPipline.mappingPipeline();
			AlignSeq alignSeq = miRNAmappingPipline.getOutGenomeAlignSeq();
			if (alignSeq != null) {
				mapNovelMiRNASamFile2Prefix.put(alignSeq, fastq2Prefix[1]);
			}
			countSmallRNA(outPath, fastq2Prefix[1], miRNAmappingPipline);
		}
	}
	
	/** 设定待比对的序列 */
	private void setConfigFile() {
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bwa.toString());
		
		miRNAmappingPipline.setExePath(softWareInfo.getExePath());
		miRNAmappingPipline.setMiRNApreSeq(species.getMiRNAhairpinFile());
		miRNAmappingPipline.setNcRNAseq(species.getRefseqNCfile());
		miRNAmappingPipline.setRfamSeq(species.getRfamFile(rfamSpeciesSpecific));
		miRNAmappingPipline.setGenome(species.getIndexChr(SoftWare.bwa));//默认bwa做mapping
	}
	
	
	/** 计算miRNA表达
	 * @param solo 前面是否有mapping
	 */
	private void countSmallRNA(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		outPath = outPath + prefix + FileOperate.getSepPath();
		FileOperate.createFolders(outPath);
		countMiRNA(outPath, prefix, miRNAmappingPipline);
		countRfam(outPath, prefix, miRNAmappingPipline);
		countNCrna(outPath, prefix, miRNAmappingPipline);
		countRepeatGene(outPath, prefix, miRNAmappingPipline);
	}

	/**
	* 计算miRNA表达
	* @param outPath
	* @param prefix
	* @param miRNAmappingPipline
	*/
	private void countMiRNA(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		miRNACount.setMiRNAfile(species.getMiRNAhairpinFile(), species.getMiRNAmatureFile());
		AlignSeq alignSeq = miRNAmappingPipline.getOutMiRNAAlignSeq();
		if (alignSeq != null) {
			miRNACount.setAlignFile(alignSeq);
			miRNACount.run();
			miRNACount.writeResultToOut(outPath);
			mapPrefix2MiRNAmature.put(prefix, miRNACount.getMapMirMature2Value());
			mapPrefix2MiRNAPre.put(prefix, miRNACount.getMapMiRNApre2Value());
		}
	}
	
	private void readRepeatGff() {
		if (gffChrAbs == null) {
			gffChrAbs = new GffChrAbs(species);
		}
		readsOnRepeatGene.setGffGene(gffChrAbs);
		readsOnRepeatGene.readGffRepeat(species.getGffRepeat());
	}

	/** 读取rfam信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRfam(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		rfamStatistic.setOutputFile(outPath + "RfamStatistics.txt");
		AlignSeq alignSeq = miRNAmappingPipline.getOutRfamAlignSeq();
		if (alignSeq != null) {
			rfamStatistic.countRfamInfo(alignSeq);
			mapPrefix2MiRNArfam.put(prefix, rfamStatistic.getMapRfam2Counts());
		}
	}
	/** 读取ncRNA的信息并计数
	 * @param solo 单独计数
	 *  */
	private void countNCrna(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		AlignSeq alignSeq = miRNAmappingPipline.getOutNCRNAAlignSeq();
		if (alignSeq != null) {
			readsOnNCrna.setAlignSeq(alignSeq);
			readsOnNCrna.searchNCrna();
			readsOnNCrna.writeToFile(outPath + "NCrnaStatistics.txt");
			mapPrefix2MiRNAncrna.put(prefix, readsOnNCrna.getMapNCrnaID_2_nameDescripValue());
		}
	}
	
	/** 读取repeat和gene信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRepeatGene(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		AlignSeq alignSeq = miRNAmappingPipline.getOutGenomeAlignSeq();
		if (alignSeq != null) {
			readRepeatGff();
			readsOnRepeatGene.countReadsInfo(alignSeq);
			readsOnRepeatGene.writeToFileGeneProp(outPath + "geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outPath + "RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outPath + "RepeatName.txt");
			
			mapPrefix2RepeatFamily.put(prefix, readsOnRepeatGene.getMapRepeatFamily2Value());
			mapPrefix2RepeatName.put(prefix, readsOnRepeatGene.getMapRepeatName2Value());
			mapPrefix2GeneInfo.put(prefix, readsOnRepeatGene.getMapGeneStructure2Value());
		}
	}
	/** 将汇总结果写入文本 */
	public void writeToFile() {
		ArrayList<String[]> lsMirPre = miRNACount.combMapMir2Value(mapPrefix2MiRNAPre);
		writeFile(outPath + "mirPreAll.txt", lsMirPre);
		
		ArrayList<String[]> lsMirMature = miRNACount.combMapMir2MatureValue(mapPrefix2MiRNAmature);
		writeFile(outPath + "mirMatureAll.txt", lsMirMature);
		
		ArrayList<String[]> lsGeneInfo = readsOnRepeatGene.combMapGeneStructure2Value(mapPrefix2GeneInfo);
		writeFile(outPath + "GeneStructureAll.txt", lsGeneInfo);
		
		ArrayList<String[]> lsRepeatFamily = readsOnRepeatGene.combMapRepatFamily(mapPrefix2RepeatFamily);
		writeFile(outPath + "RepeatFamilyAll.txt", lsRepeatFamily);
		
		ArrayList<String[]> lsRepeatName = readsOnRepeatGene.combMapRepatName(mapPrefix2RepeatName);
		writeFile(outPath + "RepeatNameAll.txt", lsRepeatName);
		
		ArrayList<String[]> lsNcRNA = readsOnNCrna.combValue(mapPrefix2MiRNAncrna);
		writeFile(outPath + "NCRNAAll.txt", lsNcRNA);
		
		ArrayList<String[]> lsRfamRNA = rfamStatistic.combValue(mapPrefix2MiRNArfam);
		writeFile(outPath + "RfamAll.txt", lsRfamRNA);
	}
	
	private void writeFile(String fileName, ArrayList<String[]> lsInfo) {
		if (lsInfo == null || lsInfo.size() == 0) {
			return;
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileName, true);
		txtWrite.ExcelWrite(lsInfo);
		txtWrite.close();
	}
	
	/**
	 * 返回mapping至基因组上的bed文件
	 * 必须要等mapping完后才能获取
	 */
	public Map<AlignSeq, String> getMapGenomeBed2Prefix() {
		return mapNovelMiRNASamFile2Prefix;
	}
	
	/** 清空存储的信息 */
	public void clear() {
		 mapPrefix2MiRNAPre.clear();
		 mapPrefix2MiRNAmature.clear();
		
		 mapPrefix2MiRNArfam.clear();
		 mapPrefix2MiRNAncrna.clear();
		
		 mapPrefix2RepeatName.clear();
		 mapPrefix2RepeatFamily.clear();
		 mapPrefix2GeneInfo.clear();
	}
}
