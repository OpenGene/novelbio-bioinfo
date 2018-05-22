package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.GeneExpTable.EnumAddAnnoType;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;
import com.novelbio.generalconf.TitleFormatNBC;

/** 
 * 给定一系列fastq文件，获得miRNA的bed文件
 * @author zong0jie
 */
public class CtrlMiRNAfastq implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(CtrlMiRNAfastq.class);
	
	Species species;
	/** 设定gff和chrome */
	GffChrAbs gffChrAbs;
	boolean isUseOldResult = true;
	/** mapping 序列 */
	MiRNAmapPipline miRNAmappingPipline = new MiRNAmapPipline();
	
	MiRNACount miRNACount = new MiRNACount();
	MirnaIso mirnaIso;
//	MirnaIso mirnaIso = new MirnaIso();
	RfamStatistic rfamStatistic = new RfamStatistic();
	ReadsOnRepeatGene readsOnRepeatGene = new ReadsOnRepeatGene();
	ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();

	/** fastqFile--prefix */
	Map<String, String> mapPrefix2Fastq;
		
	///////输出文件夹 //////////
	String outPath;
	String outPathSample;
	String outPathTmpMapping;
	String outPathStatistics;
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
	
	List<String> lsCmd = new ArrayList<>();
	
	int lenMin = 17, lenMax = 32;
	
	public CtrlMiRNAfastq() {}
	
	/** 务必首先设定 */
	public void setSpecies(Species species) {
		this.species = species;
	}
	/** 设定isoMiRNA的 */
	public void setMirnaIso(MirnaIso mirnaIso) {
		this.mirnaIso = mirnaIso;
	}
	/** 遇到已经存在的文件，是重做该步骤，还是跳过该步骤
	 * @param isUseOldResult true表示跳过该步骤
	 */
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
		miRNAmappingPipline.setIsUseOldResult(isUseOldResult);
	}
	public void setThreadNumMiRNAmap(int mapThreadNum) {
		miRNAmappingPipline.setThreadNum(mapThreadNum);
	}
	/** 首先会判断两个gffChrAbs的species是否一致，此外如果不设置gffChrAbs，那么就不会进行repeat分析 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (gffChrAbs == null 
				|| (this.gffChrAbs != null && this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies()))
		) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	public void setMapPrefix2Fastq(final Map<String, String> mapPrefix2Fastq) {
		this.mapPrefix2Fastq = mapPrefix2Fastq;
	}
	/** 设定输出文件夹 */
	public void setOutPath(String outPath, String outPathSample, String outPathTmpMapping, String outPathStatistics) {
		this.outPath = outPath;
		this.outPathSample = outPathSample;
		this.outPathTmpMapping = outPathTmpMapping;
		this.outPathStatistics = outPathStatistics;
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
	/** 设定线程数 */
	public void setThreadNum(int threadNum) {
		miRNAmappingPipline.setThreadNum(threadNum);
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
		lsCmd.clear();
		FileOperate.createFolders(outPathTmpMapping);
		setConfigFile();
		initial();
		for (String prefix : mapPrefix2Fastq.keySet()) {
			String fastq = mapPrefix2Fastq.get(prefix);
			//文件名为输出文件夹+文件前缀
			miRNAmappingPipline.setSample(prefix, fastq);
			miRNAmappingPipline.setOutPathTmp(outPathTmpMapping, outPathStatistics);
			miRNAmappingPipline.mappingPipeline();
			lsCmd.addAll(miRNAmappingPipline.getCmdExeStr());
			SamFile alignSeq = miRNAmappingPipline.getOutGenomeAlignSeq();
			if (alignSeq != null) {
				mapNovelMiRNAPrefix2SamFile.put(prefix, alignSeq);
			}
			if (samMapMiRNARate != null && miRNAmappingPipline.getSamFileStatisticsMiRNA() != null) {
				samMapMiRNARate.addMapInfo("MiRNA", miRNAmappingPipline.getSamFileStatisticsMiRNA());
			}
			setCurrentCondition(prefix);
			countSmallRNA(outPathSample, prefix, miRNAmappingPipline);
		}
	}
	
	/** 设定待比对的序列 */
	private void setConfigFile() {
		miRNAmappingPipline.setMiRNApreSeq(species.getMiRNAhairpinFile());
		miRNAmappingPipline.setNcRNAseq(species.getRefseqNCfile());
		if (species.getTaxID() != 0) {
			miRNAmappingPipline.setRfamSeq(species.getRfamFile(rfamSpeciesSpecific));
		} else {
			miRNAmappingPipline.setRfamSeq(species.getRfamFile(rfamSpeciesSpecific));
		}
	
		miRNAmappingPipline.setGenome(species.getIndexChr(SoftWare.bwa_aln));//默认bwa做mapping
	}
	
	/** 没有初始化repeat */
	private void initial() {
		if (!StringOperate.isRealNull(species.getMiRNAmatureFile())) {
			miRNACount.setExpTable(expMirPre, expMirMature);	
		}
		
		List<String> lsRfamNameRaw = SeqHash.getLsSeqNameFromFasta(species.getRfamFile(rfamSpeciesSpecific));
		expRfamID.addLsGeneName(rfamStatistic.getLsRfamID(lsRfamNameRaw));
		expRfamID.addAnnotationArray(rfamStatistic.getMapRfamID2Info());
		expRfamID.addLsTitle(RfamStatistic.getLsTitleRfamIDAnno());
		expRfamClass.addLsGeneName(rfamStatistic.getLsRfamClass(lsRfamNameRaw));
		
		List<String> lsNCrnaName = SeqHash.getLsSeqNameFromFasta(species.getRefseqNCfile());
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
		mirnaIso.setCurrentCondition(currentCondition);
	}
	
	private AlignSeqReading getAlignSeqReading(AlignSeq alignSeq, AlignmentRecorder alignmentRecorder) {
		AlignSeqReading alignSeqReading = new AlignSeqReading();
		alignSeqReading.addSeq(alignSeq);
		alignSeqReading.setLenMin(lenMin);
		alignSeqReading.setLenMax(lenMax);
		alignSeqReading.addAlignmentRecorder(alignmentRecorder);
		return alignSeqReading;
	}
	
	/** 计算miRNA表达
	 * @param solo 前面是否有mapping
	 */
	private void countSmallRNA(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		outPath = outPath + prefix + FileOperate.getSepPath();
		countMiRNA(outPath, prefix, miRNAmappingPipline);
		countRfam(outPath, prefix, miRNAmappingPipline);
		if (gffChrAbs != null) {
			countNCrna(outPath, prefix, miRNAmappingPipline);
			countRepeatGene(outPath, prefix, miRNAmappingPipline);
		}

		writeToFileCurrent(outPath, prefix);
	}

	/**
	* 计算miRNA表达
	* @param outPath
	* @param prefix
	* @param miRNAmappingPipline
	*/
	private void countMiRNA(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		if (isUseOldResult && FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".mirPre.Counts.txt", 0) 
				&& FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".mirMature.Counts.txt", 0)
				&& FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".mirna.Iso.txt", 0)
				) {
			expMirPre.read(outPath + prefix + ".mirPre.Counts.txt", EnumAddAnnoType.notAdd);
			expMirMature.read(outPath + prefix + ".mirMature.Counts.txt", EnumAddAnnoType.notAdd);
			mirnaIso.read(outPath + prefix + ".mirna.Iso.txt");
			return;
		}
		
		AlignSeq alignSeq = miRNAmappingPipline.getOutMiRNAAlignSeq();
		if (alignSeq != null) {
			miRNACount.initial();
			//TODO
			AlignSeqReading alignSeqReading = getAlignSeqReading(alignSeq, miRNACount);
			alignSeqReading.addAlignmentRecorder(mirnaIso);
			alignSeqReading.running();
			
			expMirMature.addAllReads(miRNACount.getCountMatureAll());
			expMirMature.addGeneExp(miRNACount.getMapMirMature2Value());

			expMirPre.addAllReads(miRNACount.getCountPreAll());
			expMirPre.addGeneExp(miRNACount.getMapMiRNApre2Value());
		}
	}

	/** 读取rfam信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRfam(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		if (isUseOldResult && FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".RfamClass.txt", 0) 
				&& FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".RfamID.txt", 0)) {
			expRfamClass.read(outPath + prefix + ".RfamClass.txt", EnumAddAnnoType.notAdd);
			expRfamID.read(outPath + prefix + ".RfamID.txt", EnumAddAnnoType.notAdd);
			return;
		}
		
		SamFile alignSeq = miRNAmappingPipline.getOutRfamAlignSeq();
		if (alignSeq != null) {
			rfamStatistic.initial();
			AlignSeqReading alignSeqReading = getAlignSeqReading(alignSeq, rfamStatistic);
			alignSeqReading.running();
			expRfamClass.addGeneExp(rfamStatistic.getMapRfamClass2Counts());
			expRfamID.addGeneExp(rfamStatistic.getMapRfamID2Counts());
		}
	}
	/** 读取ncRNA的信息并计数
	 * @param solo 单独计数
	 *  */
	private void countNCrna(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		if (isUseOldResult && FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".NCRNA.txt", 0)) {
			expNcRNA.read(outPath + prefix + ".NCRNA.txt", EnumAddAnnoType.notAdd);
			return;
		}
		
		SamFile alignSeq = miRNAmappingPipline.getOutNCRNAAlignSeq();
		if (alignSeq != null) {
			readsOnNCrna.initial();
			AlignSeqReading alignSeqReading = getAlignSeqReading(alignSeq, readsOnNCrna);
			alignSeqReading.running();
			readsOnNCrna.writeToFile(outPath + "NCrnaStatistics.txt");
			expNcRNA.addGeneExp(readsOnNCrna.getMapNCrnaID2Value());
		}
	}
	
	/** 读取repeat和gene信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRepeatGene(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		if (isUseOldResult && FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".GeneStructure.txt", 0)
				&& !FileOperate.isFileExistAndBigThanSize(species.getGffRepeat(), 0)) {
			expGeneStructure.read(outPath + prefix + ".GeneStructure.txt", EnumAddAnnoType.notAdd);
			return;
		} else if (isUseOldResult && FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".GeneStructure.txt", 0) 
				&& FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".RepeatFamily.txt", 0)
				&& FileOperate.isFileExistAndBigThanSize(outPath + prefix + ".RepeatName.txt", 0)
				) {
			expGeneStructure.read(outPath + prefix + ".GeneStructure.txt", EnumAddAnnoType.notAdd);
			expRepeatName.read(outPath + prefix + ".RepeatName.txt", EnumAddAnnoType.notAdd);
			expRepeatFamily.read(outPath + prefix + ".RepeatFamily.txt", EnumAddAnnoType.notAdd);
			return;
		}
		
		AlignSeq alignSeq = miRNAmappingPipline.getOutGenomeAlignSeq();
		if (alignSeq != null) {
			readRepeatGff();
			readsOnRepeatGene.initial();
			AlignSeqReading alignSeqReading = getAlignSeqReading(alignSeq, readsOnRepeatGene);
			alignSeqReading.running();
			
			expRepeatFamily.addGeneExp(readsOnRepeatGene.getMapRepeatFamily2Value());
			expRepeatName.addGeneExp(readsOnRepeatGene.getMapRepeatName2Value());
			expGeneStructure.addGeneExp(readsOnRepeatGene.getMapGeneStructure2Value());
		}
	}
	
	private void readRepeatGff() {
		if (countRepeat || gffChrAbs == null) {
			return;
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
	
	private void writeToFileCurrent(String outPath, String prefix) {
		expMirPre.writeFile(false, outPath + prefix + ".mirPre.Counts.txt", EnumExpression.Counts);
		expMirMature.writeFile(false, outPath + prefix + ".mirMature.Counts.txt", EnumExpression.Counts);
		expGeneStructure.writeFile(false, outPath + prefix + ".GeneStructure.txt", EnumExpression.Counts);
		expRepeatFamily.writeFile(false, outPath + prefix + ".RepeatFamily.txt", EnumExpression.Counts);
		expRepeatName.writeFile(false, outPath + prefix + ".RepeatName.txt", EnumExpression.Counts);
		expNcRNA.writeFile(false, outPath + prefix + ".NCRNA.txt", EnumExpression.Counts);
		expRfamClass.writeFile(false, outPath + prefix + ".RfamClass.txt", EnumExpression.Counts);
		expRfamID.writeFile(false, outPath + prefix + ".RfamID.txt", EnumExpression.Counts);
		mirnaIso.writeFile(false, outPath + prefix + ".mirna.Iso.txt");
	}

	
	/** 将汇总结果写入文本 */
	public void writeToFile() {
		expMirPre.writeFile(true, outPathTmpMapping + "mirPreAll.Counts.txt", EnumExpression.Counts);
		expMirMature.writeFile(true, outPathTmpMapping + "mirMatureAll.Counts.txt", EnumExpression.Counts);
		expGeneStructure.writeFile(true, outPath + "GeneStructureAll.txt", EnumExpression.Counts);
		expRepeatFamily.writeFile(true, outPath + "RepeatFamilyAll.txt", EnumExpression.Counts);
		expNcRNA.writeFile(true, outPath + "NCRNAAll.txt", EnumExpression.Counts);
		expRfamClass.writeFile(true, outPath + "RfamClassAll.txt", EnumExpression.Counts);
		expRfamID.writeFile(true, outPath + "RfamIDAll.txt", EnumExpression.Counts);
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

	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
	
}
