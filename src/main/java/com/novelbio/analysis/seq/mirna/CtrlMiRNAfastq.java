package com.novelbio.analysis.seq.mirna;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;

/** 
 * 给定一系列fastq文件，获得miRNA的bed文件
 * @author zong0jie
 */
public class CtrlMiRNAfastq {
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
	Map<String, String> mapPrefix2Fastq;
		
	///////输出文件夹 //////////
	String outPath;
	String outPathTmpMapping;
	///////输出数量 ///////////
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.miRNAName);
	
	GeneExpTable expRfamID = new GeneExpTable(TitleFormatNBC.RfamID);
	GeneExpTable expRfamClass = new GeneExpTable(TitleFormatNBC.RfamClass);
	
	GeneExpTable expNcRNA = new GeneExpTable(TitleFormatNBC.GeneID);
	GeneExpTable expRepeatName = new GeneExpTable(TitleFormatNBC.RepeatName);
	GeneExpTable expRepeatFamily = new GeneExpTable(TitleFormatNBC.RepeatFamily);
	GeneExpTable expGeneStructure = new GeneExpTable(TitleFormatNBC.GeneStructure);
	
	////// 没有mapping到的bed文件，用于预测新miRNA的 */
	Map<String, AlignSeq> mapNovelMiRNAPrefix2SamFile = new LinkedHashMap<>();
	boolean rfamSpeciesSpecific = false;
	
	boolean countRepeat = false;
	
	public CtrlMiRNAfastq() {}
	
	/** 务必首先设定 */
	public void setSpecies(Species species) {
		this.species = species;
	}
	/** 遇到已经存在的文件，是重做该步骤，还是跳过该步骤 */
	public void setIsUseOldResult(boolean isUseOldResult) {
		miRNAmappingPipline.setIsUseOldResult(isUseOldResult);
	}
	public void setThreadNumMiRNAmap(int mapThreadNum) {
		miRNAmappingPipline.setThreadNum(mapThreadNum);
	}
	/** 首先会判断两个gffChrAbs的species是否一致 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies())) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	public void setMapPrefix2Fastq(Map<String, String> mapPrefix2Fastq) {
		this.mapPrefix2Fastq = mapPrefix2Fastq;
	}
	/** 设定输出文件夹 */
	public void setOutPath(String outPath) {
		this.outPath = FileOperate.addSep(outPath);
		FileOperate.createFolders(this.outPath);
		this.outPathTmpMapping = this.outPath + "tmpMapping";
	}
	
	public void setMiRNAexp(GeneExpTable expMirPre, GeneExpTable expMirMature) {
		this.expMirPre = expMirPre;
		this.expMirMature = expMirMature;
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
		miRNACount.setSpecies(species, rnadatFile);
	}
	
	/** 比对和计数，每比对一次就计数。主要是为了防止出错 */
	public void mappingAndCounting(SamMapRate samMapMiRNARate) {
		FileOperate.createFolders(outPathTmpMapping);
		setConfigFile();
		initial();
		for (String prefix : mapPrefix2Fastq.keySet()) {
			String fastq = mapPrefix2Fastq.get(prefix);
			//文件名为输出文件夹+文件前缀
			miRNAmappingPipline.setSample(prefix, fastq);
			miRNAmappingPipline.setOutPathTmp(outPathTmpMapping);
			miRNAmappingPipline.mappingPipeline();
			SamFile alignSeq = miRNAmappingPipline.getOutGenomeAlignSeq();
			if (alignSeq != null) {
				mapNovelMiRNAPrefix2SamFile.put(prefix, alignSeq);
			}
			if (samMapMiRNARate != null) {
				samMapMiRNARate.addMapInfo("MiRNA", miRNAmappingPipline.getSamFileStatisticsMiRNA());
			}
			setCurrentCondition(prefix);
			countSmallRNA(outPath, prefix, miRNAmappingPipline);
		}
	}
	
	/** 设定待比对的序列 */
	private void setConfigFile() {
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bowtie2.toString());
		
		miRNAmappingPipline.setExePath(softWareInfo.getExePath());
		miRNAmappingPipline.setMiRNApreSeq(species.getMiRNAhairpinFile());
		miRNAmappingPipline.setNcRNAseq(species.getRefseqNCfile());
		miRNAmappingPipline.setRfamSeq(species.getRfamFile(rfamSpeciesSpecific));
		miRNAmappingPipline.setGenome(species.getIndexChr(SoftWare.bowtie2));//默认bwa做mapping
	}
	
	/** 没有初始化repeat */
	private void initial() {
		miRNACount.setExpTable(expMirPre, expMirMature);
		
		 List<String> lsRfamNameRaw = SeqHash.getLsSeqName(species.getRfamFile(rfamSpeciesSpecific));
		 expRfamID.addLsGeneName(rfamStatistic.getLsRfamID(lsRfamNameRaw));
		 expRfamID.addAnnotationArray(rfamStatistic.getMapRfamID2Info());
		 expRfamID.addLsTitle(RfamStatistic.getLsTitleRfamIDAnno());
		 expRfamClass.addLsGeneName(rfamStatistic.getLsRfamClass(lsRfamNameRaw));
		 
		 List<String> lsNCrnaName = SeqHash.getLsSeqName(species.getRefseqNCfile());
		 expNcRNA.addLsGeneName(lsNCrnaName);
		 expNcRNA.addAnnotationArray(readsOnNCrna.getLsMapGene2Anno(lsNCrnaName));
		 expNcRNA.addLsTitle(ReadsOnNCrna.getLsTitleAnno());

		 expGeneStructure.addLsGeneName(readsOnRepeatGene.getLsGeneStructure());
	}

	private void setCurrentCondition(String currentCondition) {
		expGeneStructure.setCurrentCondition(currentCondition);
		expMirMature.setCurrentCondition(currentCondition);
		expMirPre.setCurrentCondition(currentCondition);
		expNcRNA.setCurrentCondition(currentCondition);
		expRepeatFamily.setCurrentCondition(currentCondition);
		expRepeatName.setCurrentCondition(currentCondition);
		expRfamClass.setCurrentCondition(currentCondition);
		expRfamID.setCurrentCondition(currentCondition);
	}
	
	/** 计算miRNA表达
	 * @param solo 前面是否有mapping
	 */
	private void countSmallRNA(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		outPath = outPath + prefix + FileOperate.getSepPath();
		countMiRNA(outPath, prefix, miRNAmappingPipline);
		countRfam(outPath, prefix, miRNAmappingPipline);
		countNCrna(outPath, prefix, miRNAmappingPipline);
		countRepeatGene(outPath, prefix, miRNAmappingPipline);
		writeToFileCurrent(outPath, prefix);
	}

	/**
	* 计算miRNA表达
	* @param outPath
	* @param prefix
	* @param miRNAmappingPipline
	*/
	private void countMiRNA(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		AlignSeq alignSeq = miRNAmappingPipline.getOutMiRNAAlignSeq();
		if (alignSeq != null) {
			miRNACount.setAlignFile(alignSeq);
			miRNACount.run();
			
			expMirMature.addAllReads(miRNACount.getCountMatureAll());
			expMirMature.addGeneExp(miRNACount.getMapMirMature2Value());

			expMirPre.addAllReads(miRNACount.getCountPreAll());
			expMirPre.addGeneExp(miRNACount.getMapMiRNApre2Value());
		}
	}
	
	private void readRepeatGff() {
		if (countRepeat) {
			return;
		}
		
		if (gffChrAbs == null) {
			gffChrAbs = new GffChrAbs(species);
		}
		readsOnRepeatGene.setGffGene(gffChrAbs);
		if (!FileOperate.isFileExistAndBigThanSize(species.getGffRepeat(), 0)) {
			return;
		}
		readsOnRepeatGene.readGffRepeat(species.getGffRepeat());
		 
		expRepeatName.addLsGeneName(readsOnRepeatGene.getLsRepeatName());
		expRepeatFamily.addLsGeneName(readsOnRepeatGene.getLsRepeatFamily());
		countRepeat = true;
	}

	/** 读取rfam信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRfam(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		SamFile alignSeq = miRNAmappingPipline.getOutRfamAlignSeq();
		rfamStatistic.setSamFile(alignSeq);
		if (alignSeq != null) {
			rfamStatistic.countRfamBam();
			expRfamClass.addGeneExp(rfamStatistic.getMapRfamClass2Counts());
			expRfamID.addGeneExp(rfamStatistic.getMapRfamID2Counts());
		}
	}
	/** 读取ncRNA的信息并计数
	 * @param solo 单独计数
	 *  */
	private void countNCrna(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		SamFile alignSeq = miRNAmappingPipline.getOutNCRNAAlignSeq();
		if (alignSeq != null) {
			readsOnNCrna.setSamFile(alignSeq);
			readsOnNCrna.searchNCrna();
			readsOnNCrna.writeToFile(outPath + "NCrnaStatistics.txt");
			expNcRNA.addGeneExp(readsOnNCrna.getMapNCrnaID2Value());
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
			expRepeatFamily.addGeneExp(readsOnRepeatGene.getMapRepeatFamily2Value());
			expRepeatName.addGeneExp(readsOnRepeatGene.getMapRepeatName2Value());
			expGeneStructure.addGeneExp(readsOnRepeatGene.getMapGeneStructure2Value());
		}
	}
	
	private void writeToFileCurrent(String outPath, String prefix) {
		writeFile(false, outPath + prefix + "_mirPre_Counts.txt", expMirPre, EnumExpression.Counts);
		writeFile(false, outPath + prefix + "_mirMature_Counts.txt", expMirMature, EnumExpression.Counts);
		writeFile(false, outPath + prefix + "_GeneStructure.txt", expGeneStructure, EnumExpression.Counts);
		writeFile(false, outPath + prefix + "_RepeatFamily.txt", expRepeatFamily, EnumExpression.Counts);
		writeFile(false, outPath + prefix + "_NCRNA.txt", expNcRNA, EnumExpression.Counts);
		writeFile(false, outPath + prefix + "_RfamClass.txt", expRfamClass, EnumExpression.Counts);
		writeFile(false, outPath + prefix + "_RfamID.txt", expRfamID, EnumExpression.Counts);
	}

	
	/** 将汇总结果写入文本 */
	public void writeToFile() {
		writeFile(true, outPathTmpMapping + "mirPreAll_Counts.txt", expMirPre, EnumExpression.Counts);
		writeFile(true, outPathTmpMapping + "mirMatureAll_Counts.txt", expMirMature, EnumExpression.Counts);
		writeFile(true, outPath + "GeneStructureAll.txt", expGeneStructure, EnumExpression.Counts);
		writeFile(true, outPath + "RepeatFamilyAll.txt", expRepeatFamily, EnumExpression.Counts);
		writeFile(true, outPath + "NCRNAAll.txt", expNcRNA, EnumExpression.Counts);
		writeFile(true, outPath + "RfamClassAll.txt", expRfamClass, EnumExpression.Counts);
		writeFile(true, outPath + "RfamIDAll.txt", expRfamID, EnumExpression.Counts);
	}
	
	/**
	 * 如果文件夹不存在，会新建文件夹
	 * @param writeAllCondition
	 * @param fileName
	 * @param expTable
	 * @param enumExpression
	 */
	public static void writeFile(boolean writeAllCondition, String fileName, GeneExpTable expTable, EnumExpression enumExpression) {
		List<String[]> lsValues = null;
		if (writeAllCondition) {
			lsValues = expTable.getLsAllCountsNum(enumExpression);
		} else {
			lsValues = expTable.getLsCountsNum(enumExpression);
		}
		
		if (lsValues == null || lsValues.size() <= 1) {
			return;
		}
		FileOperate.createFolders(FileOperate.getPathName(fileName));
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileName, true);
		txtWrite.ExcelWrite(lsValues);
		txtWrite.close();
	}
	public GeneExpTable getExpMirPre() {
		return expMirPre;
	}
	public GeneExpTable getExpMirMature() {
		return expMirMature;
	}
	/**
	 * 返回mapping至基因组上的bed文件
	 * 必须要等mapping完后才能获取
	 */
	public Map<String, AlignSeq> getMapPrefix2GenomeSam() {
		return mapNovelMiRNAPrefix2SamFile;
	}
	
}
