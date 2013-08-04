package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamMapReads;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.nbcgui.GuiAnnoInfo;

/**
 * 得到每个gene的Junction后，开始计算其可变剪接的差异
 * 每次跑之前要清空
 * @author zong0jie
 */
public class ExonJunction extends RunProcess<GuiAnnoInfo> {
	public static void main(String[] args) {
		//TODO
		List<Align> lsAligns = new ArrayList<>();
//		lsAligns.add(new Align("chr13", 113834688, 113853827));
//		lsAligns.add(new Align("chr12", 4647587, 4669830));
//		lsAligns.add(new Align("chrX", 48779231, 48817543));
//		lsAligns.add(new Align("chrX", 148573976, 148586877));
//		lsAligns.add(new Align("chr15", 42468140, 42502218));
//		lsAligns.add(new Align("chr2", 191834371, 191854578));
//		lsAligns.add(new Align("chr1", 65549980, 65715670));
//		lsAligns.add(new Align("chr9", 128118727, 128494218));
//		lsAligns.add(new Align("chr17", 41149494, 41155620));
//		lsAligns.add(new Align("chr8", 4713317, 4782464));
		
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setIsLessMemory(false);
		GffChrAbs gffHashGene = new GffChrAbs(9031);
		exonJunction.setGffHashGene(gffHashGene.getGffHashGene());
		exonJunction.setLsReadRegion(lsAligns);
		exonJunction.setOneGeneOneSpliceEvent(false);
		exonJunction.addBamSorted("WT", "/home/zong0jie/Test/rnaseq/paper/chicken/DT40WT.bam");
		exonJunction.addBamSorted("KO", "/home/zong0jie/Test/rnaseq/paper/chicken/DT40KO.bam");
		exonJunction.setCompareGroups("KO", "WT");

		exonJunction.setResultFile("/home/zong0jie/Test/rnaseq/paper/chicken/testNewIso2");
		exonJunction.run();
	}
	
	private static Logger logger = Logger.getLogger(ExonJunction.class);
	private static String stopGeneName = "PRRC2C";
	
	GffHashGene gffHashGene = null;
	StrandSpecific strandSpecific = StrandSpecific.NONE;
	/** 全体差异基因的外显子
	 * ls--
	 * ls：gene
	 * ExonSplicingTest：difexon
	 *  */
	ArrayList<ArrayList<ExonSplicingTest>> lsSplicingTests;
	ArrayList<ExonSplicingTest> lsResult;
	/** 
	 * 一个基因可能有多个可变剪接事件，但是我们可以只挑选其中最显著的那个可变剪接事件
	 * 也可以输出全部的可变剪接事件
	 * 每个基因只有一个可变剪接事件
	 */
	boolean oneGeneOneSpliceEvent = true;
	
	/** 是否从junction文件中读取信息 */
	boolean readFromJunctionFile = false;
	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> setCondition = new LinkedHashSet<String>();
	
	/** 如果有一系列需要比较的conditions，就写在这个里面 */
	List<String[]> lsCondCompare;
	
	/** 本次比较的condition */
	String condition1, condition2;
	/** condition到排序的bam文件 */
	ArrayListMultimap<String, AlignSamReading> mapCond2SamReader = ArrayListMultimap.create();
	ArrayListMultimap<String, SamFile> mapCond2SamFile = ArrayListMultimap.create();
	/** 统计可变剪接事件的map
	 * key：可变剪接类型
	 * value：int[2]
	 * 0： 差异可变剪接数量
	 * 1： 全体可变剪接数量
	 *  */
	HashMap<SplicingAlternativeType, int[]> mapSplicingType2Num = new LinkedHashMap<SplicingAlternativeType, int[]>();
	double pvalue = 0.05;//表示差异可变剪接的事件的pvalue阈值
	
	String resultFile;
	
	/** 是否提取序列 */
	SeqHash seqHash;
	//TODO 默认设置为false
	boolean isLessMemory = false;
	/** 是否读取表达
	 * 默认true
	*/
	boolean readExp = true;
	CtrlSplicing ctrlSplicing;
	
	/**
	 * 读取区域，调试用。设定之后就只会读取这个区域的reads
	 */
	List<Align> lsReadReagion;
	
	public ExonJunction() {

		/////
	}
	
	/**
	 * 表示差异可变剪接的事件的pvalue阈值，仅用于统计差异可变剪接事件的数量，不用于可变剪接的筛选
	 * @param pvalue
	 */
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	/**
	 * 是否采用节省内存模式
	 * 如果节省内存就用SamMapReads 速度慢
	 * 如果耗内存就用MapReads 速度快
	 * 默认false
	 * @param isLessMemory
	 */
	public void setIsLessMemory(boolean isLessMemory) {
		this.isLessMemory = isLessMemory;
	}
	
	/** 设定建库的方式 */
	public void setStrandSpecific(StrandSpecific strandSpecific) {
		this.strandSpecific = strandSpecific;
	}
	
	/** 是否读取表达
	 * 默认true
	*/
	public void setReadExp(boolean readExp) {
		this.readExp = readExp;
	}
	/**
	 * 设定输出
	 * @param resultFile
	 */
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	
	public void setLsReadRegion(List<Align> lsAligns) {
		if (lsAligns != null && lsAligns.size() > 0) {
			lsReadReagion = lsAligns;
		}
	}
	/** 
	 * 一个基因可能有多个可变剪接事件，但是我们可以只挑选其中最显著的那个可变剪接事件
	 * 也可以输出全部的可变剪接事件
	 * @param oneGeneOneSpliceEvent true:  每个基因只有一个可变剪接事件, <b>默认为true</b>
	 * false: 每个基因输出全部可变剪接事件
	 */
	public void setOneGeneOneSpliceEvent(boolean oneGeneOneSpliceEvent) {
		this.oneGeneOneSpliceEvent = oneGeneOneSpliceEvent;
	}
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		lsSplicingTests = new ArrayList<ArrayList<ExonSplicingTest>>();
	}
	/**
	 * 如果seqhash为true，则提取序列
	 * @param seqHash
	 */
	public void setSeqHash(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	
	protected ArrayList<Align> getLsDifIsoGene() {
		ArrayList<Align> lsAlignments = new ArrayList<Align>();
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1 || isOnlyOneIso(gffDetailGene)) {
				continue;
			}
			Align align = new Align(gffDetailGene.getRefID(), gffDetailGene.getStartCis(), gffDetailGene.getEndCis());
			lsAlignments.add(align);
		}
		return lsAlignments;
	}

	public void setCompareGroups(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	
	public void setCompareGroupsLs(List<String[]> lsConditions) {
		this.lsCondCompare = lsConditions;
	}
	
	public void addBamSorted(String condition, String sortedBamFile) {
		setCondition.add(condition);
		SamFile samFile = new SamFile(sortedBamFile); 
		AlignSamReading samFileReading = new AlignSamReading(samFile);
		mapCond2SamReader.put(condition, samFileReading);
		mapCond2SamFile.put(condition, samFile);
	}
	
	public void running() {
		ctrlSplicing = new CtrlSplicing();
		if (runGetInfo != null && runGetInfo instanceof CtrlSplicing) {
			ctrlSplicing = (CtrlSplicing) runGetInfo;
		}

		ArrayList<Double> lsLevels = new ArrayList<Double>();
		lsLevels.add(0.3);
		lsLevels.add(0.4);
		lsLevels.add(0.7);
		lsLevels.add(1.0);
		long fileLength = getFileLength();
		ctrlSplicing.setProcessBarStartEndBarNum("Reading Junction", 0, 0, fileLength);

		ctrlSplicing.setProgressBarLevelLs(lsLevels);
		if (!readFromJunctionFile) {
			loadJunctionBam();
			tophatJunction.conclusion();
		} else {
			readJuncFile();
			logger.error("Not Support Now");
		}
		
		suspendCheck();
		logger.error("finish junction reads");
		ctrlSplicing.setProcessBarStartEndBarNum("Get Junction Event", 1, 0, gffHashGene.getGffDetailAll().size());
		fillLsAll_Dif_Iso_Exon();
		
		ctrlSplicing.setProcessBarStartEndBarNum("Reading Exp", 2, 0, fileLength);
		
		if (readExp) {
			loadExp();
		}
		
		if (lsCondCompare == null || lsCondCompare.size() == 0) {
			lsCondCompare = new ArrayList<String[]>();
			lsCondCompare.add(new String[]{condition1, condition2});
		}
		
		ctrlSplicing.setProcessBarStartEndBarNum("Doing Test", 2, 0, lsSplicingTests.size() * lsCondCompare.size());
		int num = 0;
		for (String[] condCompare : lsCondCompare) {
			setCompareGroups(condCompare[0], condCompare[1]);
			lsResult = getTestResult_FromIso(num);
			if (resultFile != null) {
				String outFile = "";
				if (FileOperate.isFileDirectory(resultFile)) {
					outFile = resultFile + condition1 +"vs" + condition2 + ".txt";
				} else {
					outFile = FileOperate.changeFileSuffix(resultFile, "_"+condition1 +"vs" + condition2, "txt");
				}
				writeToFile(outFile );
			}
		}
	}
	
	/**
	 * 获得文件长度
	 * @return
	 */
	private long getFileLength() {
		long fileLength = 0;
		for (String condition : mapCond2SamReader.keySet()) {
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				fileLength = fileLength + FileOperate.getFileSizeLong(samFileReading.getSamFile().getFileName());
			}
		}
		return fileLength;
	}
	
	private void loadJunctionBam() {
		AlignSeqReading samFileReadingLast = null;
		tophatJunction.setStrandSpecific(strandSpecific);
//		List<Align> lsDifIsoGene = new ArrayList<Align>();//getLsDifIsoGene();
		for (String condition : mapCond2SamReader.keySet()) {
			ctrlSplicing.setInfo("Reading Junction " + condition);
			tophatJunction.setCondition(condition);
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.clear();
				samFileReading.getSamFile().indexMake();
				if (samFileReadingLast != null) {
					samFileReading.setReadInfo(0L, samFileReadingLast.getReadByte());
				}
				
				samFileReading.setLsAlignments(lsReadReagion);
//				samFileReading.setLsAlignments(lsDifIsoGene);
				samFileReading.setRunGetInfo(runGetInfo);
				samFileReading.addAlignmentRecorder(tophatJunction);
				samFileReading.run();
				samFileReadingLast = samFileReading;
			}
		}
		samFileReadingLast = null;
	}
	
	private void readJuncFile() {
//		Thread thread = new Thread(tophatJunction);
//		thread.start();
//		while (tophatJunction.isRunning()) {
//			try { Thread.sleep(200); } catch (InterruptedException e) { }
//		}
	}
	
	/** 从全基因组中获取差异的可变剪接事件，放入lsSplicingTest中 */
	private void fillLsAll_Dif_Iso_Exon() {
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		int i = 0;
		GenerateNewIso generateNewIso = new GenerateNewIso(tophatJunction, mapCond2SamFile.values(), strandSpecific != StrandSpecific.NONE);
		generateNewIso.setGffHash(gffHashGene);
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			//TODO 设置断点
			if (gffDetailGene.getName().contains(stopGeneName)) {
				logger.debug("stop");
			}
			generateNewIso.setGffDetailGene(gffDetailGene);
			generateNewIso.reconstructGffDetailGene();
			
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1 || isOnlyOneIso(gffDetailGene)) {
				continue;
			}
			ArrayList<ExonSplicingTest> lsExonSplicingTest = getGeneDifExon(gffDetailGene);
			if (lsExonSplicingTest.size() == 0) {
				continue;
			}
			lsSplicingTests.add(lsExonSplicingTest);
			if (i%500 == 0) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setNum(i);
				guiAnnoInfo.setDouble(i);
				guiAnnoInfo.setInfo("Get " + i + " Junction Event");
				setRunInfo(guiAnnoInfo);
				logger.error(i);
			}
			i++;
		}
		logger.debug("finish");
	}
	/**
	 * 计数，看有多少iso与gffDetailGene同方向，如果只有一个，则跳过该基因
	 * @param gffDetailGene
	 * @return
	 */
	private boolean isOnlyOneIso(GffDetailGene gffDetailGene) {
		int tmpIso = 0;
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.isCis5to3() == gffDetailGene.isCis5to3()) {
				tmpIso++;
			}
		}
		return tmpIso <=1 ? true : false;
	}
	/**
	 * 获得每个 gffDetailGene中的所有差异exon，包装成 LsExonSplicingTest 并返回
	 * @param gffDetailGene
	 * @return
	 */
	private ArrayList<ExonSplicingTest> getGeneDifExon(GffDetailGene gffDetailGene) {
		//TODO 设置断点
		if (gffDetailGene.getName().contains(stopGeneName)) {
			logger.debug("stop");
		}
		
		ArrayList<ExonSplicingTest> lsExonSplicingTestResult = new ArrayList<ExonSplicingTest>();
		Collection<ExonCluster> mapLoc2ExonCluster = gffDetailGene.getDifExonMapLoc2Cluster();

		if (!mapLoc2ExonCluster.isEmpty()) {
			for (ExonCluster exonCluster : mapLoc2ExonCluster) {
				if (exonCluster.getLsIsoExon().size() == 1 || exonCluster.isAtEdge() || exonCluster.isNotSameTss_But_SameEnd()) {
					continue;
				}

				ExonSplicingTest exonSplicingTest = new ExonSplicingTest(exonCluster);
				//获得junction信息
				exonSplicingTest.setSetCondition(setCondition);
				exonSplicingTest.setJunctionInfo(mapCond2SamFile, tophatJunction);
				lsExonSplicingTestResult.add(exonSplicingTest);
			}
		}
		return lsExonSplicingTestResult;
	}
	
	private void loadExp() {
		AlignSamReading samFileReadingLast = null;

		for (String condition : mapCond2SamReader.keySet()) {
			ctrlSplicing.setInfo("Reading Exp " + condition);
			
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.clear();
				if (samFileReadingLast != null) {
					samFileReading.setReadInfo(0L, samFileReadingLast.getReadByte());
				}
			
				samFileReadingLast = samFileReading;
				samFileReading.setLsAlignments(lsReadReagion);
				samFileReading.setRunGetInfo(runGetInfo);
				MapReadsAbs mapReadsAbs = null;
				if (isLessMemory) {
					mapReadsAbs = getSamMapReads(samFileReading);
				} else {
					mapReadsAbs = getMapReads(samFileReading);
					samFileReading.run();
				}
				addMapReadsInfo(condition, mapReadsAbs);
			}
		}
	}
	
	private MapReadsAbs getSamMapReads(AlignSamReading samFileReading) {
		SamMapReads samMapReads = new SamMapReads(samFileReading.getSamFile());
		samMapReads.setisUniqueMapping(true);
		samMapReads.setNormalType(MapReadsAbs.NORMALIZATION_NO);
		return samMapReads;
	}
	
	private MapReadsAbs getMapReads(AlignSeqReading samFileReading) {
		MapReads mapReads = new MapReads();
		mapReads.setInvNum(15);
		mapReads.setNormalType(MapReads.NORMALIZATION_NO);
		mapReads.setisUniqueMapping(true);
		mapReads.prepareAlignRecord(samFileReading.getSamFile().readFirstLine());
		//TODO 可以考虑从gtf文件中获取基因组长度然后给MapReads使用
		mapReads.setMapChrID2Len(gffHashGene.getChrID2LengthForRNAseq());
		samFileReading.addAlignmentRecorder(mapReads);
		return mapReads;
	}
	
	/** 将表达信息加入统计 */
	private void addMapReadsInfo(String condition, MapReadsAbs mapReads) {
		DateUtil dateTime = new DateUtil();
		dateTime.setStartTime();
		int num = 0;
		for (ArrayList<ExonSplicingTest> lsExonTest : lsSplicingTests) {
			for (ExonSplicingTest exonSplicingTest : lsExonTest) {
				//TODO
				if (exonSplicingTest.getExonCluster().getParentGene().getName().contains(stopGeneName)) {
					logger.error("");
				}
				exonSplicingTest.addMapCondition2MapReads(condition, mapReads);
			}
			if (num % 100 == 0) {
				logger.error(num);
				ctrlSplicing.setDetailInfo("reading " + condition + " exp gene num" + num);
				logger.error(dateTime.getEclipseTime());
			}
			num ++;
		}
	}

	/** 获得检验完毕的test
	 * @param num 已经跑掉几个测试了，这个仅仅用在gui上
	 * @return
	 */
	private ArrayList<ExonSplicingTest> getTestResult_FromIso(int num) {
		initialMapSplicingType2Num();
		setConditionWhileConditionIsNull();

		ArrayList<ExonSplicingTest> lsResult = new ArrayList<ExonSplicingTest>();

		for (ArrayList<ExonSplicingTest> lsIsoExonSplicingTests : lsSplicingTests) {
			doTest_And_StatisticSplicingEvent(lsIsoExonSplicingTests);
			if (oneGeneOneSpliceEvent) {
				lsResult.add(lsIsoExonSplicingTests.get(0));
			} else {
				lsResult.addAll(lsIsoExonSplicingTests);
			}
			num++;
			if (flagStop) {
				break;
			}
			suspendCheck();
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setNum(num);
			guiAnnoInfo.setDouble(num);
			setRunInfo(guiAnnoInfo);
		}
		sortLsExonTest_Use_Pvalue(lsResult);
		return lsResult;
	}
	
	private void initialMapSplicingType2Num() {
		mapSplicingType2Num.clear();
		for (SplicingAlternativeType exonSplicingType : SplicingAlternativeType.getMapName2SplicingEvents().values()) {
			mapSplicingType2Num.put(exonSplicingType, new int[2]);
		}
	}
	/**
	 * 如果没有设定condition
	 * 则跟据setCondition的信息自动设定condition
	 */
	private void setConditionWhileConditionIsNull() {
		if (condition1 == null && condition2 == null) {
			ArrayList<String> lsCondition = ArrayOperate.getArrayListValue(setCondition);
			condition1 = lsCondition.get(0);
			condition2 = lsCondition.get(1);
		}
	}
	/**
	 * 计算每个时期的exon的差值
	 * @param lsExonClusters
	 * @return ls ExonSplicingTest -- ls 每个时期 -- 所涉及到的exon的检验结果，按照pvalue从小到大排序
	 */
	private void doTest_And_StatisticSplicingEvent(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		for (ExonSplicingTest exonSplicingTest : lsExonSplicingTest) {
			if (exonSplicingTest.getExonCluster().getParentGene().getName().contains(stopGeneName)) {
				logger.error("");
			}
			exonSplicingTest.setCompareCondition(condition1, condition2);
		}
		//按照pvalue从小到大排序
		sortLsExonTest_Use_Pvalue(lsExonSplicingTest);
		statisticsSplicingEvent(lsExonSplicingTest);
	}
	
	private void sortLsExonTest_Use_Pvalue(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		ExonSplicingTest.sortAndFdr(lsExonSplicingTest);
	}
	
	/**
	 * 统计可变剪接事件
	 * @param lsExonSplicingTest
	 */
	private void statisticsSplicingEvent(ArrayList<ExonSplicingTest> lsExonSplicingTest) {
		SplicingAlternativeType exonSplicingType = null;
		for (ExonSplicingTest exonSplicingTest : lsExonSplicingTest) {
			exonSplicingType = exonSplicingTest.getSplicingType();
			int[] tmpInfo = new int[]{0, 0};
			if (mapSplicingType2Num.containsKey(exonSplicingType)) {
				tmpInfo = mapSplicingType2Num.get(exonSplicingType);
			} else {
				mapSplicingType2Num.put(exonSplicingType, tmpInfo);
			}
			tmpInfo[1] ++;
			if (exonSplicingTest.getAndCalculatePvalue() <= pvalue) {
				tmpInfo[0] ++;
			}
		}
	}
	
	/** 写入文本 */
	public void writeToFile(String fileName) {
		TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
		
		boolean isGetSeq = seqHash == null ? false : true;
		TxtReadandWrite txtOutSeq = null;
		if (isGetSeq) {
			txtOutSeq = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_Seq", "txt"), true);
		}
		
		txtOut.writefileln(ExonSplicingTest.getTitle(condition1, condition2));
		for (ExonSplicingTest chisqTest : lsResult) {
			//TODO 设定断点
//			if (chisqTest.getExonCluster().getParentGene().getName().contains(stopGeneName)) {
//				logger.error("stop");
//			}
			chisqTest.setGetSeq(seqHash);
			try {
				txtOut.writefileln(chisqTest.toStringArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			if (isGetSeq) {
				txtOutSeq.writefileln(chisqTest.toStringSeq());
			}
		}
		
		txtOut.close();
		if (isGetSeq) {
			txtOutSeq.close();
		}
		
		TxtReadandWrite txtStatistics = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_statistics", "txt"), true);
		txtStatistics.writefileln("SplicingEvent\tSignificantNum\tAllNum");
		for (Entry<SplicingAlternativeType, int[]> exonSplicingInfo : mapSplicingType2Num.entrySet()) {
			String tmpResult = exonSplicingInfo.getKey() + "\t" + exonSplicingInfo.getValue()[0] + "\t" +  exonSplicingInfo.getValue()[1];
			txtStatistics.writefileln(tmpResult);
		}
		txtStatistics.close();
	}
	/** 终止线程，需要在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadStop() {
		super.threadStop();
		for (String condition : mapCond2SamReader.keySet()) {
			ctrlSplicing.setInfo("Reading Junction " + condition);
			tophatJunction.setCondition(condition);
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.threadStop();
			}
		}
	}
	/** 终止线程，需要在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadSuspend() {
		super.threadSuspend();
		for (String condition : mapCond2SamReader.keySet()) {
			ctrlSplicing.setInfo("Reading Junction " + condition);
			tophatJunction.setCondition(condition);
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.threadSuspend();
			}
		}
	}
	/** 终止线程，需要在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadResume() {
		super.threadResume();
		for (String condition : mapCond2SamReader.keySet()) {
			ctrlSplicing.setInfo("Reading Junction " + condition);
			tophatJunction.setCondition(condition);
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.threadResume();
			}
		}
	}
	public void clear() {
		condition1 = null; condition2 = null;
		lsResult = null;
		lsSplicingTests= null;
		mapCond2SamFile.clear();
		mapCond2SamReader.clear();
		mapSplicingType2Num.clear();
		setCondition.clear();
		tophatJunction = new TophatJunction();
		readFromJunctionFile = false;
	}
}
