package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictAlt5Or3;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictRetainIntron;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 可变剪接的检验
 * 有一个需要修正的地方
 * 就是alt3和alt5
 * 有时候这个只相差3个bp，就是边界就相差1-3个氨基酸
 * 这种我觉得很扯淡
 * 我觉得这种要被过滤掉
 */
public class ExonSplicingTest implements Comparable<ExonSplicingTest> {
	private static Logger logger = Logger.getLogger(ExonSplicingTest.class);
	
	/** 实验组和对照组的junction reads数量加起来小于这个数，就返回1 */
	static int junctionReadsMinNum = 10;
	
	ExonCluster exonCluster;
	/** 每个exonCluster组中condition以及其对应的信息<br>
	 * key condition<br>
	 * value SpliceType2Value<br>
	 *  */
	HashMap<String, SpliceType2Value> mapCondition2SpliceInfo = new LinkedHashMap<String, SpliceType2Value>();
	
	/** 差异最大的那个exonSplicingType */
	SplicingAlternativeType splicingType;
	String condition1;
	String condition2;
	/** 设置一个负数的初始值 */
	Double pvalue= -1.0;
	double fdr = 1.0;
	
	/** readsLength越长，juncReadsPvalue所占的比例就越大 */
	int readsLength = 100;
	
	SeqHash seqHash;
	
	public ExonSplicingTest(ExonCluster exonCluster) {
		this.exonCluster = exonCluster;
	}
	
	public void setGetSeq(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	
	/** <b>在此之前必须设定{@link #setMapCond2Samfile(ArrayListMultimap)}</b><br> */
	public void setJunctionInfo(ArrayListMultimap<String, SamFile> mapCond2Samfile, TophatJunction tophatJunction) {
		List<SpliceTypePredict> lsSpliceTypePredicts = exonCluster.getSplicingTypeLs();
		if (lsSpliceTypePredicts.size() == 0) {
			return;
		}
		for (SpliceTypePredict spliceTypePredict : lsSpliceTypePredicts) {
			spliceTypePredict.setTophatJunction(tophatJunction);
			if (spliceTypePredict instanceof PredictRetainIntron) {
				((PredictRetainIntron)spliceTypePredict).setMapCond2Samfile(mapCond2Samfile);
			}
			for (String condition : tophatJunction.getConditionSet()) {
				SpliceType2Value spliceType2Value = getAndCreatSpliceType2Value(condition);
				spliceType2Value.addJunction(condition, spliceTypePredict);
			}
		}
	}

	/**
	 * <b>在此之前必须设定{@link #setJunctionInfo(TophatJunction)}</b><br>
	 * 添加每个condition以及其对应的reads堆积
	 * 如果是相同的condition，则累加上去
	 */
	public void addMapCondition2MapReads(String condition, MapReadsAbs mapReads) {
		SpliceType2Value spliceType2Value = getAndCreatSpliceType2Value(condition);
		
		for (SpliceTypePredict spliceTypePredict : exonCluster.getSplicingTypeLs()) {
			spliceType2Value.addExp(exonCluster.getParentGene(), spliceTypePredict, mapReads);
		}
	}
	
	private SpliceType2Value getAndCreatSpliceType2Value(String condition) {
		SpliceType2Value spliceType2Value = null;
		//不能用containsKey，因为一开始已经输入了信息
		if (mapCondition2SpliceInfo.get(condition) == null) {
			spliceType2Value = new SpliceType2Value();
			mapCondition2SpliceInfo.put(condition, spliceType2Value);
		} else {
			spliceType2Value = mapCondition2SpliceInfo.get(condition);
		}
		return spliceType2Value;
	}

	
	/** 必须设定 */
	public void setCompareCondition(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	
	/** 测序长度，根据这个长度来判定pvalue的比例 */
	public void setReadsLength(int readsLength) {
		this.readsLength = readsLength;
	}

	public ExonCluster getExonCluster() {
		return exonCluster;
	}

	private void setFdr(double fdr) {
		this.fdr = fdr;
	}
	/** 计算并获得pvalue */
	public Double getAndCalculatePvalue() {
		if (pvalue > 0) {
			return pvalue;
		}
		//TODO 可以设置断点
		if (exonCluster.getParentGene().getName().contains("Vdac3")) {
			logger.debug("stop");
		}
		
		if (!mapCondition2SpliceInfo.containsKey(condition1) || !mapCondition2SpliceInfo.containsKey(condition2)) {
			pvalue = 1.0;
			return pvalue;
		}
		for (SplicingAlternativeType splicingType : exonCluster.getSplicingTypeSet()) {
			double pvalueExp = getPvalueReads(splicingType);
			double pvalueCounts = getPvalueJunctionCounts(splicingType);
			double pvalue = getPvalueCombine(pvalueExp, pvalueCounts);
			
			if (mapCondition2SpliceInfo.get(condition1).isFiltered(splicingType) == false) {
				pvalue = 1.0;
			}
			
			if (this.pvalue < 0 || pvalue < this.pvalue) {
				this.splicingType = splicingType;
				this.pvalue = pvalue;
			}
		}
		return this.pvalue;
	}
	
	/**
	 * 计算完pvalue后{@link #getAndCalculatePvalue()}
	 * 获得该splicing type
	 * @return
	 */
	public SplicingAlternativeType getSplicingType() {
		return splicingType;
	}
	
	/** 出错就返回-1 */
	protected Double getPvalueReads(SplicingAlternativeType splicingType) {
		try {
			return getPvalueReadsExp(splicingType);
		} catch (Exception e) {
			return -1.0;
		}
	}

	/** 比较exon 表达量
	 * 在这之前务必设定condition
	 */
	private Double getPvalueReadsExp(SplicingAlternativeType splicingType) {
		//表达水平超过该值就标准化
		int normalizedValue = 50;
		
		List<Double> lsExp1 = mapCondition2SpliceInfo.get(condition1).getLsExp(splicingType);
		List<Double> lsExp2= mapCondition2SpliceInfo.get(condition2).getLsExp(splicingType);
		int[] tmpExpCond1 = new int[2];
		tmpExpCond1[0] = lsExp1.get(0).intValue(); tmpExpCond1[1] = lsExp1.get(1).intValue();
		int[] tmpExpCond2 = new int[2];
		tmpExpCond2[0] = lsExp2.get(0).intValue(); tmpExpCond2[1] = lsExp2.get(1).intValue();
		
//		long[] tmpExpCond1long = modifyInputValue(tmpExpCond1);
//		long[] tmpExpCond2long = modifyInputValue(tmpExpCond2);
		normalizeToLowValue(tmpExpCond1, normalizedValue);
		normalizeToLowValue(tmpExpCond2, normalizedValue);
		int sum = (int) (tmpExpCond1[0] + tmpExpCond1[1] + tmpExpCond2[0] + tmpExpCond2[1]);
		FisherTest fisherTest = new FisherTest(sum + 3);
		
		return fisherTest.getTwoTailedP(tmpExpCond1[0], tmpExpCond1[1], tmpExpCond2[0], tmpExpCond2[1]);
	}
	/** 比较junction reads
	 * 在这之前务必设定condition
	 */
	protected Double getPvalueJunctionCounts(SplicingAlternativeType splicingType) {
		//如果count数超过该值，就标准化
		int normalizedNum = 200;
		
		List<Double> lsJunc1 = mapCondition2SpliceInfo.get(condition1).getLsJun(splicingType);
		List<Double> lsJunc2 = mapCondition2SpliceInfo.get(condition2).getLsJun(splicingType);
		int[] cond1 = getReadsNum(lsJunc1);
		int[] cond2 = getReadsNum(lsJunc2);
		
		if (!filter(cond1, cond2)) {
			return 1.0;
		}
		
//		cond1 = modifyInputValue(cond1);
//		cond2 = modifyInputValue(cond2);
		normalizeToLowValue(cond1, normalizedNum);
		normalizeToLowValue(cond2, normalizedNum);

		return chiSquareTestDataSetsComparison(cond1, cond2);
	}
	
	private int[] getReadsNum(List<Double> lsJunc) {
		int[] result = new int[lsJunc.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = lsJunc.get(i).intValue();
		}
		return result;
	}
	/** 某些情况不适合做分析，就过滤掉<br>
	 * 譬如：遇到类似 0:5 0:50<br>
	 * 和<br>
	 * 2：3：50<br>  4：2：50<br>
	 * 以及总reads过少的情况，就要删除不进行分析<br>
	 */
	private boolean filter(int[] cond1, int[] cond2) {
		//遇到类似 0:5 0:50
		//2：3：50  4：2：50
		//等就要删除了
		int allReadsNum = MathComput.sum(cond1) + MathComput.sum(cond2);
		int readsNumLess = 0;
		for (int i = 0; i < cond1.length; i++) {
			if (cond1[i] <= allReadsNum/20 && cond2[i] <= allReadsNum/20) {
				readsNumLess++;
			}
		}
		if (cond1.length - readsNumLess <= 1) {
			return false;
		}
		
		//总reads数太少也过滤
		if (MathComput.sum(cond1) + MathComput.sum(cond1) < junctionReadsMinNum) {
			return false;
		}
		return true;
	}
	
	private double chiSquareTestDataSetsComparison(int[] cond1, int[] cond2) {
		long[] cond1Long = new long[cond1.length];
		long[] cond2Long = new long[cond2.length];
		for (int i = 0; i < cond1.length; i++) {
			cond1Long[i] = cond1[i] + 1;
		}
		for (int i = 0; i < cond2.length; i++) {
			cond2Long[i] = cond2[i] + 1;
		}
		try {
			return TestUtils.chiSquareTestDataSetsComparison(cond1Long, cond2Long);
		} catch (Exception e) {
			return 1.0;
		}
	}
	
	/** 
	 *  公式：2^((log2(0.8)*0.5 + log2(0.1)*0.5))
	 *  */
	private double getPvalueCombine(double pvalueExp, double pvalueCounts) {
		double pvalue = 1.0;
		if (pvalueExp < 0) {
			pvalue = pvalueCounts;
			return pvalue;
		}
		if (pvalueCounts == 1) {
			return 1;
		}
		double expPro = getPvaluePropExp();
//		double pvalueLog = Math.log10(pvalueExp) * expPro +  Math.log10(pvalueCounts) * (1 - expPro);
//		pvalue = Math.pow(10, pvalueLog);
		
		pvalue = pvalueExp * expPro +  pvalueCounts * (1 - expPro);
		
		if (pvalue > 1) {
			pvalue = 1.0;
		}
		return pvalue;
	}
	
	/** 获得表达所占有的pvalue的比例
	 * exon越长比例越高，越短比例越低
	 *  */
	private double getPvaluePropExp() {
		double prop = 0.5;
		double ratio = exonCluster.getLength()/(readsLength * 3);
		if (ratio > 1) {
			prop = Math.pow(0.5, 1/ratio);
		}
		else {
			prop = 1 - Math.pow(0.5, ratio);
		}
		if (prop > 0.85) {
			prop = 0.85;
		}
		else if (prop < 0.15) {
			prop = 0.15;
		}
		return prop;
	}
	
	/** 
	 * 当可变剪接的形式为cassette时，修正输入的值
	 * 就是将值加上他们的平均数
	 */
	private int[] modifyInputValue(int[] conditionInfo) {
		int mean = (int) MathComput.mean(conditionInfo);
		int[] modifiedCondition = new int[conditionInfo.length];
		for (int i = 0; i < conditionInfo.length; i++) {
			modifiedCondition[i] = conditionInfo[i] + mean;
		}
		return modifiedCondition;
	}
	
	/** 
	 * 如果count数量太大，就将其标准化至一个比较低的值
	 * @param normalizedValue 大于该值就开始修正
	 */
	private void normalizeToLowValue(int[] condition, int normalizedValue) {
		int meanValue = (int) MathComput.mean(condition);
		if (meanValue < normalizedValue) {
			return;
		}
		for (int i = 0; i < condition.length; i++) {
			condition[i] = (int) ((double)condition[i]/meanValue * normalizedValue);
		}
	}
	
	/** 
	 * 获得一系列序列：<br>
	 * 1. 当前exon<br>
	 * 2. 当前exon左右扩展300bp<br>
	 * 3.前一个和后一个exon和intron的序列
	 */
	protected ArrayList<SeqFasta> getSeq(SeqHash seqHash) {
		ArrayList<SeqFasta> lsSeqFastas = new ArrayList<SeqFasta>();
		
		SeqFasta seqFasta = seqHash.getSeq(exonCluster.isCis5to3(), exonCluster.getRefID(), 
				exonCluster.getStartAbs(), exonCluster.getEndAbs());
		lsSeqFastas.add(seqFasta);
		
		seqFasta = seqHash.getSeq(exonCluster.isCis5to3(), exonCluster.getRefID(),
				exonCluster.getStartAbs() - 300, exonCluster.getEndAbs() + 300);
		lsSeqFastas.add(seqFasta);
		
		ArrayList<ExonInfo> lsGetExon = new ArrayList<ExonInfo>();
		if (exonCluster.getExonClusterBefore() != null) {
			ExonCluster exonClusterBefore = exonCluster.getExonClusterBefore();
			lsGetExon.add(new ExonInfo("exonCluster", exonCluster.isCis5to3(), exonClusterBefore.getStartCis(), exonClusterBefore.getEndCis()));
		}
		lsGetExon.add(new ExonInfo("exonCluster", exonCluster.isCis5to3(), exonCluster.getStartCis(), exonCluster.getEndCis()));
		if (exonCluster.getExonClusterAfter() != null) {
			ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
			lsGetExon.add(new ExonInfo("exonCluster", exonCluster.isCis5to3(), exonClusterAfter.getStartCis(), exonClusterAfter.getEndCis()));
		}
		
		seqFasta = seqHash.getSeq(exonCluster.getRefID(), lsGetExon, true);
		if (seqFasta != null && !exonCluster.isCis5to3()) {
			seqFasta = seqFasta.reservecom();
		}
		lsSeqFastas.add(seqFasta);

		
		return lsSeqFastas;
	}
	@Override
	public int compareTo(ExonSplicingTest o) {
		return pvalue.compareTo(o.pvalue);
	}
	
	public String[] toStringArray() {
		getAndCalculatePvalue();
		ArrayList<String> lsResult = new ArrayList<String>();
		List<Double> lsJunc1 = mapCondition2SpliceInfo.get(condition1).getLsJun(splicingType);
		List<Double> lsJunc2 = mapCondition2SpliceInfo.get(condition2).getLsJun(splicingType);
		
		List<Double> lsExp1 = mapCondition2SpliceInfo.get(condition1).getLsExp(splicingType);
		List<Double> lsExp2 = mapCondition2SpliceInfo.get(condition2).getLsExp(splicingType);
 
		
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		lsResult.add(gffDetailGene.getName().get(0));
		lsResult.add(mapCondition2SpliceInfo.get(condition1).getSpliceTypePredict(splicingType).getDifSite().toStringNoCis());
		lsResult.add(getConditionInt(lsJunc1));
		lsResult.add(getConditionInt(lsJunc2));
		
		lsResult.add(getCondition(lsExp1));
		lsResult.add(getCondition(lsExp2));
		
		lsResult.add(getAndCalculatePvalue() + "");
		lsResult.add(fdr + "");
		//TODO
		lsResult.add(splicingType.toString());
//		GeneID geneID = gffDetailGene.getSetGeneID().iterator().next();
//		lsResult.add(geneID.getSymbol());
//		lsResult.add(geneID.getDescription());
		if (seqHash != null) {
			ArrayList<SeqFasta> lsSeqFasta = getSeq(seqHash);
			for (SeqFasta seqFasta : lsSeqFasta) {
				try {
					lsResult.add(seqFasta.toString());
				} catch (Exception e) {
					lsResult.add("");
				}
		
			}
		}
		return lsResult.toArray(new String[0]);
	}
	
	private String getCondition(List<Double> lsJunc) {
		if (lsJunc == null) {
			return "";
		}
		String condition = lsJunc.get(0)+ "";
		for (int i = 1; i < lsJunc.size(); i++) {
			condition = condition + "::" + lsJunc.get(i);
		}
		return condition;
	}
	private String getConditionInt(List<Double> lsJunc) {
		String condition = lsJunc.get(0).intValue() + "";
		for (int i = 1; i < lsJunc.size(); i++) {
			condition = condition + "::" + lsJunc.get(i).intValue();
		}
		return condition;
	}
	
	/** 输入的信息会自动排序 */
	public static void sortAndFdr(List<ExonSplicingTest> colExonSplicingTests) {
		//按照pvalue从小到大排序
		Collections.sort(colExonSplicingTests, new Comparator<ExonSplicingTest>() {
			public int compare(ExonSplicingTest o1, ExonSplicingTest o2) {
				return o1.getAndCalculatePvalue().compareTo(o2.getAndCalculatePvalue());
			}
		});
		
		ArrayList<Double> lsPvalue = new ArrayList<Double>();
		for (ExonSplicingTest exonSplicingTest : colExonSplicingTests) {
			if (exonSplicingTest.getAndCalculatePvalue() > 0.5) {
				break;
			}
			lsPvalue.add(exonSplicingTest.getAndCalculatePvalue());
		}
		
		ArrayList<Double> lsFdr = MathComput.pvalue2Fdr(lsPvalue);
		int i = 0;
		for (ExonSplicingTest exonSplicingTest : colExonSplicingTests) {
			if (i < lsFdr.size()) {
				exonSplicingTest.setFdr(lsFdr.get(i));
			} else {
				exonSplicingTest.setFdr(1);
			}

			i++;
		}
	}
	
	/** 获得标题 */
	public static String[] getTitle(String condition1, String condition2, boolean isGetSeq) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add(condition1 + "_Skip::Others");
		lsTitle.add(condition2 + "_Skip::Others");
		lsTitle.add(condition1 + "Exp");
		lsTitle.add(condition2 + "Exp");
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add("SplicingType");
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		if (isGetSeq) {
			lsTitle.add("Casual_Exon");
			lsTitle.add("Casual_Exon+-300bp");
			lsTitle.add("Casual_Exon+-1Exon");
		}
		return lsTitle.toArray(new String[0]);
	}

}
/**
 * 某个时期的某个位点的<br>
 * 
 * 某种可变剪接形式的<br>
 * 
 * 表达和Junction Reads数
 * 
 * @author zong0jie
 */
class SpliceType2Value {
	private static final Logger logger = Logger.getLogger(SpliceType2Value.class);
	
	Set<SplicingAlternativeType> setExonSplicingTypes = new HashSet<SplicingAlternativeType>();
	ArrayListMultimap<SplicingAlternativeType, Double> mapSplicingType2LsExpValue = ArrayListMultimap.create();
	ArrayListMultimap<SplicingAlternativeType, Double> mapSplicingType2LsJunctionReads = ArrayListMultimap.create();
	Map<SplicingAlternativeType, SpliceTypePredict> mapSplicingType2Detail = new HashMap<SplicingAlternativeType, SpliceTypePredict>();
	
	/**
	 * 是否通过过滤
	 * 有些譬如类似alt5和alt3，如果差距太小，就不进行考虑
	 */
	boolean isFiltered = true;
	
	/** 添加表达 */
	public void addExp(GffDetailGene gffDetailGene, SpliceTypePredict spliceTypePredict, MapReadsAbs mapReads) {
		ArrayList<Double> lsExp = new ArrayList<Double>();
		Align siteInfo = spliceTypePredict.getDifSite();
		double[] info = mapReads.getRangeInfo(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs(), 0);
		double[] info2 = mapReads.getRangeInfo(siteInfo.getRefID(), gffDetailGene.getLongestSplitMrna());
		lsExp.add((double) (getMean(info) + 1));			
		lsExp.add((double) (getMean(info2) + 1));

		addLsDouble(mapSplicingType2LsExpValue, spliceTypePredict.getType(), lsExp);
		setExonSplicingTypes.add(spliceTypePredict.getType());
	}
	private static int getMean(double[] info) {
		if (info == null) {
			return -1;
		}
		return (int)new Mean().evaluate(info);
	}
	/** 添加指定时期的JunctionReads
	 * condition是用来设定spliceTypePredict的时期的
	 */
	public void addJunction(String condition, SpliceTypePredict spliceTypePredict) {
		SplicingAlternativeType splicingAlternativeType = spliceTypePredict.getType();
		ArrayList<Double> lsCounts = spliceTypePredict.getJuncCounts(condition);
		
		addLsDouble(mapSplicingType2LsJunctionReads, splicingAlternativeType, lsCounts);
		setExonSplicingTypes.add(spliceTypePredict.getType());
		mapSplicingType2Detail.put(splicingAlternativeType, spliceTypePredict);
	}
	
	/** 把一个lsDouble和map里面已有的LsDouble加起来 */
	private static void addLsDouble(ArrayListMultimap<SplicingAlternativeType, Double> mapSplicingType2LsInfo, 
			SplicingAlternativeType splicingType, List<Double> lsJunNew) {
		if (mapSplicingType2LsInfo.containsKey(splicingType)) {
			List<Double> lsJun = mapSplicingType2LsInfo.get(splicingType);
			if (lsJunNew.size() != lsJun.size()) {
				logger.error("出错");
				return;
			}
			//新老加起来放入map
			for (int i = 0; i < lsJun.size(); i++) {
				lsJun.set(i, lsJun.get(i) + lsJunNew.get(i));
			}
		} else {
			mapSplicingType2LsInfo.putAll(splicingType, lsJunNew);
		}
	}
	
	/** 获得reads，如果不存在这种类型的可变剪接，就返回null */
	public List<Double> getLsJun(SplicingAlternativeType splicingAlternativeType) {
		return mapSplicingType2LsJunctionReads.get(splicingAlternativeType);
	}
		
	/** 获得表达，如果不存在这种类型的可变剪接，就返回null */
	public List<Double> getLsExp(SplicingAlternativeType splicingAlternativeType) {
		return mapSplicingType2LsExpValue.get(splicingAlternativeType);
	}
	
	/**
	 * 给定指定的剪接类型，返回该剪接类型的各种指标
	 * @param splicingAlternativeType
	 * @return
	 */
	public SpliceTypePredict getSpliceTypePredict(SplicingAlternativeType splicingAlternativeType) {
		return mapSplicingType2Detail.get(splicingAlternativeType);
	}
	
	/**
	 * 输入的可变剪接类型是否通过过滤
	 * 例外：alt5和alt3，如果差异的那一小段的太短，譬如长度小于10bp，就会返回false
	 * @return
	 */
	public boolean isFiltered(SplicingAlternativeType splicingAlternativeType) {
		if (!setExonSplicingTypes.contains(splicingAlternativeType)) {
			return false;
		}
		return mapSplicingType2Detail.get(splicingAlternativeType).isFiltered();
	}
}