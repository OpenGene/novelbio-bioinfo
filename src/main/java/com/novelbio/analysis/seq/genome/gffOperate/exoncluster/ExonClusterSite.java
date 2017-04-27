package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.rnaseq.ExonSplicingTest;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;

/** 每一个大的exon内部可能会有有多个 exoncluster，那么把这多个exonCluster放到一起，一起来做差异可变剪接预测 */
public class ExonClusterSite {
	private static final Logger logger = Logger.getLogger(ExonClusterSite.class);
	
	List<ExonCluster> lsExonCluster = new ArrayList<>();
	List<ExonSplicingTest> lsExonSplicingTests = new ArrayList<>();
	
	ExonCluster exonClusterCurrent;
	
	public void add(ExonCluster exonCluster) {
		lsExonCluster.add(exonCluster);
		this.exonClusterCurrent = exonCluster;
	}
	
	public void addAll(List<ExonCluster> lsExonClusters) {
		this.lsExonCluster.addAll(lsExonClusters);
	}
	
	/** 获得当前的exonCluster */
	public ExonCluster getCurrentExonCluster() {
		return exonClusterCurrent;
	}
	
	public List<ExonCluster> getLsExonCluster() {
		return lsExonCluster;
	}
	public List<ExonSplicingTest> getLsExonSplicingTests() {
		return lsExonSplicingTests;
	}
	
	public void generateExonTestUnit(int juncAllReadsNum, int juncSampleReadsNum, Set<String> setIsoName_No_Reconstruct,
			double pvalueJunctionProp, boolean isCombine, Set<String> setCondition, int minDifLen, TophatJunction tophatJunction) {
		for (ExonCluster exonCluster : lsExonCluster) {
			if (exonCluster.getLsIsoExon().size() == 1 || exonCluster.isAtEdge() || exonCluster.isNotSameTss_But_SameEnd()) {
				continue;
			}

			ExonSplicingTest exonSplicingTest = new ExonSplicingTest(exonCluster, minDifLen);
			exonSplicingTest.setJuncReadsNum(juncAllReadsNum, juncSampleReadsNum);
			exonSplicingTest.setSetIsoName_No_Reconstruct(setIsoName_No_Reconstruct);
			exonSplicingTest.setPvalueJunctionProp(pvalueJunctionProp);
			exonSplicingTest.setCombine(isCombine);
			//在这里设置主要是为了后面的Retain-Intron
			exonSplicingTest.setJunctionInfo(tophatJunction);
			//获得junction信息
			exonSplicingTest.setSetCondition(setCondition);
			lsExonSplicingTests.add(exonSplicingTest);
		}
	}
	
	public void addMapCondition2MapReads(String condition, String group, MapReadsAbs mapReads) {
		for (ExonSplicingTest exonSplicingTest : getLsExonSplicingTests()) {
			exonSplicingTest.addMapCondition2MapReads(condition, group, mapReads);
		}
		mapReads = null;
	}
	
	public void setSpliceType2Value(TophatJunction tophatJunction, Map<String, Map<String, double[]>> mapCond_group2ReadsNum) {
		for (ExonSplicingTest exonSplicingTest : lsExonSplicingTests) {
			exonSplicingTest.setMapCond_Group2ReadsNum(mapCond_group2ReadsNum);
			exonSplicingTest.setJunctionInfo(tophatJunction);
			exonSplicingTest.setSpliceType2Value();
		}
	}
	
	public void setCompareConditionAndCalculate(String condition1, String condition2) {
		if (exonClusterCurrent.getStartAbs() == 70329988) {
			logger.debug("");
		}
		for (ExonSplicingTest test : lsExonSplicingTests) {
			test.setCompareCondition(condition1, condition2);
			test.getAndCalculatePvalue();
		}
	}
	
	public List<ExonSplicingTest> getLsTestResult() {
		if (exonClusterCurrent.getStartAbs() == 70329988) {
			logger.debug("");
		}

		List<ExonSplicingTest> lsRI = new ArrayList<>();
		List<ExonSplicingTest> lsOther = new ArrayList<>();
		for (ExonSplicingTest exonSplicingTest : lsExonSplicingTests) {
			if (exonSplicingTest.isTestEmpty()) {
				continue;
			}
			
			exonSplicingTest.getAndCalculatePvalue();
			if (exonSplicingTest.getSplicingType() == SplicingAlternativeType.retain_intron) {
				lsRI.add(exonSplicingTest);
			} else if(exonSplicingTest.getSplicingType() != SplicingAlternativeType.unknown) {
				lsOther.add(exonSplicingTest);
			}
		}
		Collections.sort(lsRI);
		//选择多个剪接形式中，reads比较多的那个，而不仅仅是看pvalue
		Collections.sort(lsOther, new Comparator<ExonSplicingTest>() {
			public int compare(ExonSplicingTest o1, ExonSplicingTest o2) {
				int[] readsInfo1 = o1.getSpliceTypePvalue().getReadsInfo();
				int[] readsInfo2 = o2.getSpliceTypePvalue().getReadsInfo();
				Integer o1min = Math.min(readsInfo1[0], readsInfo1[1]);
				Integer o2min = Math.min(readsInfo2[0], readsInfo2[1]);
				Double o1result = o1.getPvalue()/o1min;
				Double o2result = o2.getPvalue()/o2min;
				return o1result.compareTo(o2result);
			}
		});

		List<ExonSplicingTest> lsResult = new ArrayList<>();
		if (!lsRI.isEmpty()) {
			lsResult.add(lsRI.get(0));
		}
		
		//优先选择 SE 类型的剪接形式
		if (!lsOther.isEmpty()) {
			ExonSplicingTest exonSplicingTestFirst = lsOther.get(0);
			if (exonSplicingTestFirst.getSplicingType() != SplicingAlternativeType.cassette) {
				ExonSplicingTest firstSeTest = null;
				for (ExonSplicingTest seTest : lsOther) {
					if (seTest.getSplicingType() == SplicingAlternativeType.cassette) {
						firstSeTest = seTest;
						break;
					}
				}
				if (firstSeTest != null && firstSeTest.getAndCalculatePvalue()/exonSplicingTestFirst.getAndCalculatePvalue() < 1.2) {
					exonSplicingTestFirst = firstSeTest;
				}
			}
			lsResult.add(exonSplicingTestFirst);
			
			for (ExonSplicingTest spliceTest : lsOther) {
				if (spliceTest == exonSplicingTestFirst) {
					continue;
				}
				if (!isOverLap(lsResult, spliceTest)) {
					lsResult.add(spliceTest);
				}
			}
		}
		return lsResult;
	}
	
	public static List<ExonClusterSite> generateLsExonCluster(List<ExonCluster> lsClusters) {
		List<ExonClusterSite> lsExonClusters = new ArrayList<>();
		for (ExonCluster exonCluster : lsClusters) {
			ExonClusterSite exonClusterSite = new ExonClusterSite();
			exonClusterSite.add(exonCluster);
			exonCluster.setExonClusterSite(exonClusterSite);
			lsExonClusters.add(exonClusterSite);
		}
		return lsExonClusters;
	}
	
	public boolean isOverLap(List<ExonSplicingTest> lsExons, ExonSplicingTest thisExon) {
		for (ExonSplicingTest exonSplicingTest : lsExons) {
			if (exonSplicingTest.getExonCluster().getSplicingTypeSet().contains(SplicingAlternativeType.retain_intron)) {
				continue;
			}
			if (PredictAlt5Or3.isOverlap(exonSplicingTest.getExonCluster(), thisExon.getExonCluster())) {
				return true;
			}
		}
		return false;
	}
	
}
