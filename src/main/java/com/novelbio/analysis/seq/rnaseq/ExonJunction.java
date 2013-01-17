package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileReading;
import com.novelbio.analysis.seq.sam.SamMapReads;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.nbcgui.GUI.GuiAnnoInfo;

/**
 * 得到每个gene的Junction后，开始计算其可变剪接的差异
 * 每次跑之前要清空
 * @author zong0jie
 */
public class ExonJunction extends RunProcess<GuiAnnoInfo> {
	private static Logger logger = Logger.getLogger(ExonJunction.class);

	GffHashGene gffHashGene = null;
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
	boolean readJunctionFromBedFile = false;
	TophatJunction tophatJunction = new TophatJunction();
	LinkedHashSet<String> setCondition = new LinkedHashSet<String>();
	
	String condition1, condition2;
	/** condition到排序的bam文件 */
	ArrayListMultimap<String, SamFileReading> mapCond2SamReader = ArrayListMultimap.create();
	ArrayListMultimap<String, SamFile> mapCond2SamFile = ArrayListMultimap.create();
	/** 统计可变剪接事件的map
	 * key：可变剪接类型
	 * value：int[2]
	 * 0： 差异可变剪接数量
	 * 1： 全体可变剪接数量
	 *  */
	HashMap<SplicingAlternativeType, int[]> mapSplicingType2Num = new LinkedHashMap<SplicingAlternativeType, int[]>();
	double pvalue = 0.05;//表示差异可变剪接的事件的pvalue阈值
	

	
	/** 是否提取序列 */
	SeqHash seqHash;
	
	boolean isLessMemory = true;
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
	 * @param isLessMemory
	 */
	public void setIsLessMemory(boolean isLessMemory) {
		this.isLessMemory = isLessMemory;
	}

	/** 
	 * 一个基因可能有多个可变剪接事件，但是我们可以只挑选其中最显著的那个可变剪接事件
	 * 也可以输出全部的可变剪接事件
	 * @param oneGeneOneSpliceEvent true:  每个基因只有一个可变剪接事件, 默认为true
	 * false: 每个基因输出全部可变剪接事件
	 */
	public void setOneGeneOneSpliceEvent(boolean oneGeneOneSpliceEvent) {
		this.oneGeneOneSpliceEvent = oneGeneOneSpliceEvent;
	}
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		lsSplicingTests = new ArrayList<ArrayList<ExonSplicingTest>>();
	}
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
	/**
	 * 设定junction文件以及所对应的时期
	 * 一旦设定该项目，则不会从bam文件中获取junction信息
	 * @param condition
	 * @param junctionFile
	 */
	public void setIsoJunFile(String condition,String junctionFile) {
		tophatJunction.setJunFile(condition,junctionFile);
		setCondition.add(condition);
		readJunctionFromBedFile = true;
	}
	
	public void addBamSorted(String condition, String sortedBamFile) {
		setCondition.add(condition);
		SamFileReading samFileReading = new SamFileReading(new SamFile(sortedBamFile));
		mapCond2SamReader.put(condition, samFileReading);
		mapCond2SamFile.put(condition, new SamFile(sortedBamFile));
	}
	
	public void running() {
		if (!readJunctionFromBedFile) {
			loadJunction();
		}
		logger.error("finish junction reads");
		fillLsAll_Dif_Iso_Exon();
		
		loadExp();
		
		lsResult = getTestResult_FromIso();
	}
	
	private void loadJunction() {
		ArrayList<Align> lsDifIsoGene = null;//getLsDifIsoGene();
		for (String condition : mapCond2SamReader.keySet()) {
			tophatJunction.setCondition(condition);
			List<SamFileReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (SamFileReading samFileReading : lsSamFileReadings) {
				samFileReading.clear();
				samFileReading.setLsAlignments(lsDifIsoGene);
				samFileReading.setRunGetInfo(runGetInfo);
				samFileReading.addAlignmentRecorder(tophatJunction);
				samFileReading.run();
				
				GuiAnnoInfo exonJunctionGuiInfo = new GuiAnnoInfo();
				exonJunctionGuiInfo.setInfo("Finished Reading Junction");
				setRunInfo(exonJunctionGuiInfo);
			}
		}
	}
	
	/** 从全基因组中获取差异的可变剪接事件，放入lsSplicingTest中 */
	private void fillLsAll_Dif_Iso_Exon() {
		ArrayList<GffDetailGene> lsGffDetailGenes = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1 || isOnlyOneIso(gffDetailGene)) {
				continue;
			}
			ArrayList<ExonSplicingTest> lsExonSplicingTest = getGeneDifExon(gffDetailGene);
			if (lsExonSplicingTest.size() == 0) {
				continue;
			}
			lsSplicingTests.add(lsExonSplicingTest);
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
		if (gffDetailGene.getName().contains("Foxp1")) {
			logger.debug("stop");
		}
		
		ArrayList<ExonSplicingTest> lsExonSplicingTestResult = new ArrayList<ExonSplicingTest>();
		HashMap<String, ExonCluster> mapLoc2ExonCluster = gffDetailGene.getDifExonMapLoc2Cluster();

		if (mapLoc2ExonCluster.size() > 0) {
			for (ExonCluster exonCluster : mapLoc2ExonCluster.values()) {
				if (exonCluster.getLsIsoExon().size() == 1 || exonCluster.isAtEdge() || exonCluster.isNotSameTss_But_SameEnd()) {
					continue;
				}

				ExonSplicingTest exonSplicingTest = new ExonSplicingTest(exonCluster);
				//获得junction信息
				exonSplicingTest.setJunctionInfo(mapCond2SamFile, tophatJunction);
				lsExonSplicingTestResult.add(exonSplicingTest);
			}
		}
		return lsExonSplicingTestResult;
	}
	
	private void loadExp() {
		for (String condition : mapCond2SamReader.keySet()) {
			List<SamFileReading> lsSamFileReadings = mapCond2SamReader.get(condition);
			for (SamFileReading samFileReading : lsSamFileReadings) {
				samFileReading.clear();
				samFileReading.setRunGetInfo(runGetInfo);
				MapReadsAbs mapReadsAbs = null;
				if (isLessMemory) {
					mapReadsAbs = getSamMapReads(samFileReading);
				} else {
					mapReadsAbs = getMapReads(samFileReading);
					samFileReading.run();
				}
				addMapReadsInfo(condition, mapReadsAbs);

				GuiAnnoInfo exonJunctionGuiInfo = new GuiAnnoInfo();
				//TODO
				setRunInfo(exonJunctionGuiInfo);
			}
		}
	}
	private MapReadsAbs getSamMapReads(SamFileReading samFileReading) {
		SamMapReads samMapReads = new SamMapReads(samFileReading.getSamFile());
		samMapReads.setisUniqueMapping(true);
		samMapReads.setNormalType(MapReadsAbs.NORMALIZATION_NO);
		return samMapReads;
	}
	private MapReadsAbs getMapReads(SamFileReading samFileReading) {
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
		int num = 0;
		for (ArrayList<ExonSplicingTest> lsExonTest : lsSplicingTests) {
			for (ExonSplicingTest exonSplicingTest : lsExonTest) {
				exonSplicingTest.addMapCondition2MapReads(condition, mapReads);
			}
			
			if (num % 100 == 0) {
				logger.error(num);
			}
			num ++;
		}
	}

	/** 获得检验完毕的test */
	private ArrayList<ExonSplicingTest> getTestResult_FromIso() {
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
		txtOut.writefileln(ExonSplicingTest.getTitle(condition1, condition2,true));
		for (ExonSplicingTest chisqTest : lsResult) {
			chisqTest.setGetSeq(seqHash);
			txtOut.writefileln(chisqTest.toStringArray());
		}
		txtOut.close();
		
		TxtReadandWrite txtStatistics = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_statistics", "txt"), true);
		txtStatistics.writefileln("SplicingEvent\tSignificantNum\tAllNum");
		for (Entry<SplicingAlternativeType, int[]> exonSplicingInfo : mapSplicingType2Num.entrySet()) {
			String tmpResult = exonSplicingInfo.getKey() + "\t" + exonSplicingInfo.getValue()[0] + "\t" +  exonSplicingInfo.getValue()[1];
			txtStatistics.writefileln(tmpResult);
		}
		txtStatistics.close();
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
		readJunctionFromBedFile = false;
	}
}
