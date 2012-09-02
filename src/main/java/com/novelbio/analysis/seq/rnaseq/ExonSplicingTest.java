package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TestUtils;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonCluster;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonCluster.ExonSplicingType;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.TitleFormatNBC;

/** 可变剪接的检验 */
public class ExonSplicingTest implements Comparable<ExonSplicingTest> {
	ExonCluster exonCluster;
	TophatJunction tophatJunction;
	
	/** 每个exonCluster组中condition以及其对应的exon的junction reads counts */
	LinkedHashMap<String, int[]> mapCondition2Counts = new LinkedHashMap<String, int[]>();
	/** 每个condition以及其对应的reads堆积 */
	HashMap<String, MapReads> mapCondition2MapReads;
	
	String condition1;
	String condition2;
	/** 设置一个大于1的初始值 */
	Double pvalue= -1.0;
	
	public ExonSplicingTest(ExonCluster exonCluster, LinkedHashSet<String> setCondition, TophatJunction tophatJunction) {
		this.exonCluster = exonCluster;
		for (String string : setCondition) {
			mapCondition2Counts.put(string, new int[0]);
		}
		this.tophatJunction = tophatJunction;
		prepare();
	}
	/** 必须设定
	 * 设定好了就自动计算pvalues
	 */
	public void setCondition(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
		getPvalue();
	}
	/** 设定每个condition以及其对应的reads堆积 */
	public void setMapCondition2MapReads(HashMap<String, MapReads> mapCondition2MapReads) {
		this.mapCondition2MapReads = mapCondition2MapReads;
	}
	
	private void prepare() {
		ArrayList<ExonInfo> lsExon = exonCluster.getAllExons();
		
		//跨过该exon的iso是否存在，0不存在，1存在
		int junc = 0;
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() > 0)
			junc = 1;

		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		String chrID = gffDetailGene.getParentName();
		//一般 setCondition 里面只有两项，也就是仅比较两个时期的可变剪接
		ArrayList<String> lsCondition = ArrayOperate.getArrayListKey(mapCondition2Counts);
		Set<ExonSplicingType> setexExonSplicingTypes = exonCluster.getExonSplicingTypeSet();
		for (String condition : lsCondition) {
			int[] counts = null;
			if (setexExonSplicingTypes.contains(ExonSplicingType.alt5)) {
				counts = getAlt5Reads(junc, gffDetailGene, chrID, condition);
			}
			else if (setexExonSplicingTypes.contains(ExonSplicingType.alt3)) {
				counts = getAlt3Reads(junc, gffDetailGene, chrID, condition);
			}
			else {
				counts = getNorm(junc, gffDetailGene, chrID, condition);
			}
			
			mapCondition2Counts.put(condition, counts);
		}
	}
	
	private int[] getAlt5Reads(int junc, GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getExonInfoSingleLs();
		int[] counts = new int[lsExon.size()];
		for (int i = 0; i < lsExon.size(); i++) {
			ExonInfo exon = lsExon.get(i);
			counts[i] = tophatJunction.getJunctionSite(chrID, exon.getEndCis(), condition);
		}
		if (junc == 1) {
			counts[counts.length - 1] = getJunReadsNum(gffDetailGene, exonCluster, condition);
		}
		return counts;
	}
	private int[] getAlt3Reads(int junc, GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getExonInfoSingleLs();
		int[] counts = new int[lsExon.size()];
		for (int i = 0; i < lsExon.size(); i++) {
			ExonInfo exon = lsExon.get(i);
			counts[i] = tophatJunction.getJunctionSite(chrID, exon.getStartCis(), condition);
		}
		if (junc == 1) {
			counts[counts.length - 1] = getJunReadsNum(gffDetailGene, exonCluster, condition);
		}
		return counts;
	}
	private int[] getNorm(int junc, GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getAllExons();
		int[] counts = new int[lsExon.size() + junc];
		for (int i = 0; i < lsExon.size(); i++) {
			ExonInfo exon = lsExon.get(i);
			counts[i] = tophatJunction.getJunctionSite(chrID, exon.getStartCis(), condition) + tophatJunction.getJunctionSite(chrID, exon.getEndCis(), 	condition);
		}
		if (junc == 1) {
			counts[counts.length - 1] = getJunReadsNum(gffDetailGene, exonCluster, condition);
		}
		return counts;
	}
	/**
	 * 获得跳过该exonCluster组的readsNum
	 * @param gffDetailGene
	 * @param exonCluster
	 * @param condition
	 * @return
	 */
	private int getJunReadsNum(GffDetailGene gffDetailGene, ExonCluster exonCluster, String condition) {
		int result = 0;
		HashMap<String, Integer> hashTmp = exonCluster.getMapIso2ExonIndexSkipTheCluster();
		for (Entry<String, Integer> entry : hashTmp.entrySet()) {
			String isoName = entry.getKey();
			int exonNum = entry.getValue();
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getIsolist(isoName);
			if (exonNum >= gffGeneIsoInfo.size()-1) {
				continue;
			}
			//TODO 检查本步是否正确
			result = result + tophatJunction.getJunctionSite(gffDetailGene.getParentName(), gffGeneIsoInfo.get(exonNum).getEndCis(), gffGeneIsoInfo.get(exonNum+1).getStartCis(), condition);
		}
		return result;
	}
	
	protected Double getPvalue() {
		if (pvalue > 0) {
			return pvalue;
		}
		if (exonCluster.getParentGene().getName().contains("ENSGALT00000022585") || exonCluster.getParentGene().getName().contains("ENSGALT00000011172")) {
			System.out.println("stop");
		}
		double pvalueExp = getPvalueReads();
		double pvalueCounts = getPvalueJunctionCounts();
		if (exonCluster.getExonSplicingType() == ExonSplicingType.retain_intron) {
			getPvalueRetain_Intron(pvalueExp, pvalueCounts);
		}
		else {
			getPvalueOther(pvalueExp, pvalueCounts);
		}
		return pvalue;
	}
	/** Retain_Intron的pvalue比较奇怪，必须要exon才能计算的 */
	private void getPvalueRetain_Intron(double pvalueExp, double pvalueCounts) {
		if (pvalueExp > 0)
			pvalue = pvalueExp;
		else
			pvalue = pvalueCounts;
		return;
	}
	/** Retain_Intron的pvalue比较奇怪，必须要exon才能计算的 */
	private void getPvalueOther(double pvalueExp, double pvalueCounts) {
		if (pvalueExp < 0) {
			pvalue = pvalueCounts;
			return;
		}
		
		pvalue = pvalueExp * 0.6 + pvalueCounts *0.4;
		if (pvalue > 1) {
			pvalue = 1.0;
		}
		return;
	}
	/** 比较junction reads
	 * 在这之前务必设定condition
	 */
	protected Double getPvalueJunctionCounts() {
		//如果count数超过该值，就标准化
		int normalizedNum = 200;
		if (pvalue > 0) {
			return pvalue;
		}
		int[] cond1 = mapCondition2Counts.get(condition1);
		int[] cond2 = mapCondition2Counts.get(condition2);
		
		cond1 = modifyInputValue(cond1);
		cond2 = modifyInputValue(cond2);
		
		normalizeToLowValue(cond1, normalizedNum);
		normalizeToLowValue(cond2, normalizedNum);
		pvalue = chiSquareTestDataSetsComparison(cond1, cond2);
		return pvalue;
	}
	/** 出错就返回-1 */
	protected Double getPvalueReads() {
		try {
			return getPvalueReadsExp();
		} catch (Exception e) {
			return -1.0;
		}
	}
	/** 比较exon 表达量
	 * 在这之前务必设定condition
	 */
	private Double getPvalueReadsExp() {

		//表达水平超过该值就标准化
		int normalizedValue = 50;
		
		SiteInfo siteInfo = exonCluster.getDifSite();
		MapReads mapReadsCond1 = mapCondition2MapReads.get(condition1);
		MapReads mapReadsCond2 = mapCondition2MapReads.get(condition2);

		int[] tmpExpCond1 = new int[2];
		int[] tmpExpCond2 = new int[2];
		tmpExpCond1[0] = (int) mapReadsCond1.getRegionMean(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs()) + 1;
		tmpExpCond1[1]= (int) mapReadsCond1.regionMean(siteInfo.getRefID(), exonCluster.getParentGene().getLongestSplit()) + 1;
		
		tmpExpCond2[0] = (int) mapReadsCond2.getRegionMean(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs()) + 1;
		tmpExpCond2[1] = (int) mapReadsCond2.regionMean(siteInfo.getRefID(), exonCluster.getParentGene().getLongestSplit()) + 1;
		
//		long[] tmpExpCond1long = modifyInputValue(tmpExpCond1);
//		long[] tmpExpCond2long = modifyInputValue(tmpExpCond2);
		normalizeToLowValue(tmpExpCond1, normalizedValue);
		normalizeToLowValue(tmpExpCond2, normalizedValue);
		int sum = (int) (tmpExpCond1[0] + tmpExpCond1[1] + tmpExpCond2[0] + tmpExpCond2[1]);
		FisherTest fisherTest = new FisherTest(sum + 3);
		
		double pvalue = fisherTest.getTwoTailedP(tmpExpCond1[0], tmpExpCond1[1], tmpExpCond2[0], tmpExpCond2[1]);
		if (pvalue < 0.2) {
			System.out.println("stop");
		}
		return pvalue;
	}
	
	/** 当可变剪接的形式为cassette时，修正输入的值
	 * 就是将值加上他们的平均数
	 *  */
	private int[] modifyInputValue(int[] conditionInfo) {
		int mean = (int) MathComput.mean(conditionInfo);
		int[] modifiedCondition = new int[conditionInfo.length];
		for (int i = 0; i < conditionInfo.length; i++) {
			modifiedCondition[i] = conditionInfo[i] + mean;
		}
		return modifiedCondition;
	}
	
	/** 如果count数量太大，就将其标准化至一个比较低的值
	 * @param normalizedValue 大于该值就开始修正
	 *  */
	private void normalizeToLowValue(int[] condition, int normalizedValue) {
		int meanValue = (int) MathComput.mean(condition);
		if (meanValue < normalizedValue) {
			return;
		}
		else {
			for (int i = 0; i < condition.length; i++) {
				condition[i] = (int) ((double)condition[i]/meanValue * normalizedValue);
			}
		}
	}
	private double chiSquareTestDataSetsComparison(int[] cond1, int[] cond2) {
		long[] cond1Long = new long[cond1.length];
		long[] cond2Long = new long[cond2.length];
		for (int i = 0; i < cond1.length; i++) {
			cond1Long[i] = cond1[i];
		}
		for (int i = 0; i < cond2.length; i++) {
			cond2Long[i] = cond2[i];
		}
		try {
			return TestUtils.chiSquareTestDataSetsComparison(cond1Long, cond2Long);
		} catch (Exception e) {
			return 1.0;
		}
	}
	@Override
	public int compareTo(ExonSplicingTest o) {
		return pvalue.compareTo(o.pvalue);
	}
	
	public String toString() {
		int[] cond1 = mapCondition2Counts.get(condition1);
		int[] cond2 = mapCondition2Counts.get(condition2);
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		String result = gffDetailGene.getName() + "\t" + exonCluster.getLocInfo() +"\t"+ cond1[0]+ "";
		for (int i = 1; i < cond1.length; i++) {
			result = result + "::" + cond1[i];
		}
		result = result + "\t" + cond2[0];
		for (int i = 1; i < cond2.length; i++) {
			result = result + "::" + cond2[i];
		}
		result = result + "\t" + getPvalue() + "\t" + exonCluster.getExonSplicingType().toString();
		return result;
	}
	/** 获得标题 */
	public static String getTitle(String condition1, String condition2) {
		String title = TitleFormatNBC.AccID + "\t" + TitleFormatNBC.Location + "\t" + condition1 + "\t" + condition2 + "\t" + TitleFormatNBC.Pvalue + "\tSplicingType";
		return title;
	}
}

