package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.GuiAnnoInfo;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictRetainIntron;
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
import com.novelbio.database.domain.geneanno.TaxInfo;

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
//		lsAligns.add(new Align("1", 47916959,	47937428));
//		lsAligns.add(new Align("7",30050239,30137766));
//		lsAligns.add(new Align("7",30121258, 30123992));
//		lsAligns.add(new Align("4", 5683249, 6116963));
//		lsAligns.add(new Align("mt", 0, 5000));
//		chr1:33715442-33774115
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		System.out.println("start");
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setIsLessMemory(false);
//		exonJunction.setGffHashGene(new GffHashGene(GffType.GTF, "/home/zong0jie/Test/rnaseq/paper/chicken/raw_ensembl_genes/chicken_ensemble_KO-WT-merged.gtf"));
		exonJunction.setGffHashGene(new GffHashGene(GffType.GTF, "/home/zong0jie/Test/rnaseq/paper/chicken/raw_ensembl_genes/chicken_ensemble.gtf"));
		exonJunction.setgenerateNewIso();
		exonJunction.setLsReadRegion(lsAligns);
		exonJunction.setOneGeneOneSpliceEvent(false);
		exonJunction.addBamSorted("WT", "/home/zong0jie/Test/rnaseq/paper/chicken/DT40WT0h.bam");
		exonJunction.addBamSorted("KO", "/home/zong0jie/Test/rnaseq/paper/chicken/DT40KO0h.bam");
		exonJunction.setCompareGroups("KO", "WT");

		exonJunction.setResultFile("/home/zong0jie/Test/rnaseq/paper/chicken/ensemble_Iso2_merge_testl");
		exonJunction.setgenerateNewIso();

		exonJunction.run();
		exonJunction = null;
		Set<TaxInfo> setTax = new HashSet<>();
		for (int i = 0; i < 1000; i++) {
			TaxInfo taxInfo = new TaxInfo();
			taxInfo.setLatin("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
			setTax.add(taxInfo);
		}
		System.out.println(dateUtil.getEclipseTime());
	}
	
	private static Logger logger = Logger.getLogger(ExonJunction.class);
	private static String stopGeneName = "DIAPH1";
	
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
	boolean isReconstructIso = false;
		
	/**
	 * 读取区域，调试用。设定之后就只会读取这个区域的reads
	 */
	List<Align> lsReadReagion;

	public ExonJunction() {
	}
	/**
	 * 表示差异可变剪接的事件的pvalue阈值，仅用于统计差异可变剪接事件的数量，不用于可变剪接的筛选
	 * @param pvalue
	 */
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	/** 是否重建转录本，调用一下就重建<br>
	 * 在run之前最后一个设定
	 */
	public void setgenerateNewIso() {
		isReconstructIso = true;
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
		MapReads mapReads = null;
		if (isReconstructIso) {
			mapReads = getMapReads(mapCond2SamReader.values().iterator().next());
		}
		
		loadJunctionBam(mapReads);
		tophatJunction.conclusion();
		
		suspendCheck();
		logger.error("finish junction reads");
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setInfo2("Get Junction Event");
			List<Double> lsRegion = new ArrayList<>();
			lsRegion.add(1.0);
			lsRegion.add(0.0);
			lsRegion.add((double) gffHashGene.getGffDetailAll().size());
			guiAnnoInfo.setLsNumInfo(lsRegion);
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		GenerateNewIso generateNewIso = null;
		if (isReconstructIso) {
			generateNewIso = new GenerateNewIso(tophatJunction, mapReads, strandSpecific);
			generateNewIso.setGffHash(gffHashGene);
		}
		fillLsAll_Dif_Iso_Exon(generateNewIso);
		if (isReconstructIso) {
			generateNewIso.clear();
			generateNewIso = null;
			mapReads.clear();
			mapReads = null;
			System.gc();
		}
		
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			List<Double> lsRegion = new ArrayList<>();
			lsRegion.add(1.0);
			lsRegion.add(0.0);
			lsRegion.add((double) gffHashGene.getGffDetailAll().size());
			guiAnnoInfo.setLsNumInfo(lsRegion);
			guiAnnoInfo.setInfo2("Reading Exp");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
		loadExp();
		setSplicingType();
		if (lsCondCompare == null || lsCondCompare.size() == 0) {
			lsCondCompare = new ArrayList<String[]>();
			lsCondCompare.add(new String[]{condition1, condition2});
		}
		
		if (runGetInfo != null) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			List<Double> lsRegion = new ArrayList<>();
			lsRegion.add(2.0);
			lsRegion.add(0.0);
			lsRegion.add((double) (lsSplicingTests.size() * lsCondCompare.size()));
			guiAnnoInfo.setLsNumInfo(lsRegion);
			guiAnnoInfo.setInfo2("Doing Test");
			runGetInfo.setRunningInfo(guiAnnoInfo);
		}
		
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
	public long getFileLength() {
		long fileLength = 0;
		for (String condition : mapCond2SamReader.keySet()) {
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				fileLength = fileLength + FileOperate.getFileSizeLong(samFileReading.getSamFile().getFileName());
			}
		}
		return fileLength;
	}
	
	private void loadJunctionBam(MapReads mapReads) {
		AlignSeqReading samFileReadingLast = null;
		tophatJunction.setStrandSpecific(strandSpecific);
		for (String condition : mapCond2SamReader.keySet()) {
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2("Reading Junction " + condition);
				runGetInfo.setRunningInfo(guiAnnoInfo);
			}
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
				if (mapReads != null) {
					samFileReading.addAlignmentRecorder(mapReads);
				}
				samFileReading.run();
				samFileReading.clearRecorder();
				samFileReadingLast = samFileReading;
			}
		}
		samFileReadingLast = null;
	}
	
	/** 从全基因组中获取差异的可变剪接事件，放入lsSplicingTest中 */
	private void fillLsAll_Dif_Iso_Exon(GenerateNewIso generateNewIso) {
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		int i = 0;

		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			//TODO 设置断点
			if (gffDetailGene.getName().contains(stopGeneName)) {
				logger.debug("stop");
			}
//			logger.error(gffDetailGene.getNameSingle());
			reconstructIso(generateNewIso, gffDetailGene);
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
				guiAnnoInfo.setInfo("Get " + i + " Junction Gene");
				logger.error(i);
				setRunInfo(guiAnnoInfo);
			}
			i++;
		}
		logger.debug("finish");
	}
	
	private void reconstructIso(GenerateNewIso generateNewIso, GffDetailGene gffDetailGene) {
		if (gffDetailGene != null && generateNewIso != null) {
			generateNewIso.setGffDetailGene(gffDetailGene);
			generateNewIso.reconstructGffDetailGene();
		}
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
		List<ExonCluster> mapLoc2ExonCluster = new ArrayList<>(gffDetailGene.getDifExonMapLoc2Cluster());
		
		if (!mapLoc2ExonCluster.isEmpty()) {
			for (ExonCluster exonCluster : mapLoc2ExonCluster) {
				if (exonCluster.getLsIsoExon().size() == 1 || exonCluster.isAtEdge() || exonCluster.isNotSameTss_But_SameEnd()) {
					continue;
				}

				ExonSplicingTest exonSplicingTest = new ExonSplicingTest(exonCluster);
				//获得junction信息
				exonSplicingTest.setSetCondition(setCondition);
				exonSplicingTest.setJunctionInfo(tophatJunction);
				lsExonSplicingTestResult.add(exonSplicingTest);
			}
		}
		return lsExonSplicingTestResult;
	}
	
	private void loadExp() {
		AlignSamReading samFileReadingLast = null;
		for (String condition : mapCond2SamReader.keySet()) {
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2("Reading Exp " + condition);
				runGetInfo.setRunningInfo(guiAnnoInfo);
			}
			List<AlignSamReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (AlignSamReading samFileReading : lsSamFileReadings) {
				samFileReading.clear();
				if (samFileReadingLast != null) {
					samFileReading.setReadInfo(0L, samFileReadingLast.getReadByte());
				}
				samFileReadingLast = samFileReading;
				samFileReading.setLsAlignments(lsReadReagion);
				samFileReading.setRunGetInfo(runGetInfo);
				add_RetainIntron_Into_SamReading(condition, samFileReading);
				MapReadsAbs mapReadsAbs = null;
				if (isLessMemory) {
					mapReadsAbs = getSamMapReads(samFileReading);
				} else {
					mapReadsAbs = getMapReads(samFileReading);
					samFileReading.addAlignmentRecorder((MapReads)mapReadsAbs);
				}
				
				samFileReading.run();
				samFileReading.clearRecorder();
				addMapReadsInfo(condition, mapReadsAbs);
			}
		}
	}
	
	private void add_RetainIntron_Into_SamReading(String condition, AlignSamReading samFileReading) {
		List<PredictRetainIntron> lsRetainIntrons = new ArrayList<>();
		for (List<ExonSplicingTest> lsExonSplicingTests : lsSplicingTests) {
			for (ExonSplicingTest exonSplicingTest : lsExonSplicingTests) {
				lsRetainIntrons.addAll(exonSplicingTest.getLsRetainIntron());
			}
		}
		for (PredictRetainIntron predictRetainIntron : lsRetainIntrons) {
			predictRetainIntron.setCondition(condition);
		}
		samFileReading.addColAlignmentRecorder(lsRetainIntrons);
	}
	
	private MapReadsAbs getSamMapReads(AlignSamReading samFileReading) {
		SamMapReads samMapReads = new SamMapReads(samFileReading.getSamFile(), strandSpecific);
		samMapReads.setisUniqueMapping(true);
		samMapReads.setNormalType(MapReadsAbs.NORMALIZATION_NO);
		return samMapReads;
	}
	
	private MapReads getMapReads(AlignSeqReading samFileReading) {
		MapReads mapReads = new MapReads();
		mapReads.setInvNum(15);
		mapReads.setNormalType(MapReads.NORMALIZATION_NO);
		mapReads.setisUniqueMapping(true);
		mapReads.prepareAlignRecord(samFileReading.getSamFile().readFirstLine());
		//TODO 可以考虑从gtf文件中获取基因组长度然后给MapReads使用
		mapReads.setMapChrID2Len(((SamFile)samFileReading.getSamFile()).getMapChrID2Length());
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
				if (runGetInfo != null) {
					GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
					guiAnnoInfo.setInfo("reading " + condition + " exp gene num" + num);
					runGetInfo.setRunningInfo(guiAnnoInfo);
				}
				logger.error(dateTime.getEclipseTime());
			}
			num ++;
		}
	}
	
	private void setSplicingType() {
		for (List<ExonSplicingTest> lstest : lsSplicingTests) {
			for (ExonSplicingTest exonSplicingTest : lstest) {
				exonSplicingTest.setSpliceType2Value();
			}
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
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2("Reading Junction " + condition);
				runGetInfo.setRunningInfo(guiAnnoInfo);
			}
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
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2("Reading Junction " + condition);
				runGetInfo.setRunningInfo(guiAnnoInfo);
			}
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
			if (runGetInfo != null) {
				GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setInfo2("Reading Junction " + condition);
				runGetInfo.setRunningInfo(guiAnnoInfo);
			}
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
	}
}
