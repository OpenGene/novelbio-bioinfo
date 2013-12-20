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

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictRetainIntron;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 一个ExonSplicingTest专门检测一个ExonCluster
 * 而一个ExonCluster会存在超过一种的剪接形式，所以这里会选择其中最显著的一个结果
 * 
 * 可变剪接的检验
 * 有一个需要修正的地方
 * 就是alt3和alt5
 * 有时候这个只相差3个bp，就是边界就相差1-3个氨基酸
 * 这种我觉得很扯淡--不过后来发现是有文献依据的
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
	Map<String, SpliceType2Value> mapCondition2SpliceInfo = new LinkedHashMap<String, SpliceType2Value>();
	List<PvalueCalculate> lsPvalueInfo = new ArrayList<>();

	String condition1;
	String condition2;
	/** 设置一个负数的初始值 */
	Double pvalue= -1.0;
	double fdr = 1.0;
	Set<String> setCondition;
	/** readsLength越长，juncReadsPvalue所占的比例就越大 */
	int readsLength = 100;
	
	SeqHash seqHash;
	
	Map<String, Map<String, double[]>> mapCond_Group2ReadsNum;
	Map<String, Map<String, double[]>> mapCond_Group2JunNum;
	
	private static final String debug = "CAMKK2";
	
	public ExonSplicingTest(ExonCluster exonCluster) {
		this.exonCluster = exonCluster;
	}
	
	public void setGetSeq(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	/** 必须设定，总共的condition数 */
	public void setSetCondition(Set<String> setCondition) {
		this.setCondition = setCondition;
	}
	/** 必须设定 */
	public void setCompareCondition(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
		pvalue = -1.0;
	}
	public void setMapCond_Group2ReadsNum(
			Map<String, Map<String, double[]>> mapCond_Group2ReadsNum) {
		this.mapCond_Group2ReadsNum = mapCond_Group2ReadsNum;
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
	
	/**
	 * 设定每个时期对应的Sam文件，以及junction信息
	 * @param mapCond2Samfile 在校正retainIntron时使用
	 * @param tophatJunction
	 */
	public void setJunctionInfo(TophatJunction tophatJunction) {
		mapCond_Group2JunNum = tophatJunction.mapCondition_Group2JunNum;
		for (SpliceTypePredict spliceTypePredict : exonCluster.getSplicingTypeLs()) {
			spliceTypePredict.setTophatJunction(tophatJunction);
		}
	}
	
	/** 拿出来专门设定readsCounts的 */
	public List<PredictRetainIntron> getLsRetainIntron() {
		List<PredictRetainIntron> lsRetainIntrons = new ArrayList<>();
		for (SpliceTypePredict spliceTypePredict : exonCluster.getSplicingTypeLs()) {
			if (spliceTypePredict instanceof PredictRetainIntron) {
				lsRetainIntrons.add((PredictRetainIntron) spliceTypePredict);
			}
		}
		return lsRetainIntrons;
	}
	
	/**
	 * <b>因为涉及到junction reads的信息</b><br>
	 * 设定具体剪接位点的readsCount数，必须在读取完sam文件之后再设定
	 */
	public void setSpliceType2Value() {
		for (SpliceTypePredict spliceTypePredict : exonCluster.getSplicingTypeLs()) {
			for (String condition : setCondition) {
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
	public void addMapCondition2MapReads(String condition, String group, MapReadsAbs mapReads) {
		SpliceType2Value spliceType2Value = getAndCreatSpliceType2Value(condition);
		double[] BG = null;
		List<? extends Alignment> lsSiteInfoBG = null;
		for (SpliceTypePredict spliceTypePredict : exonCluster.getSplicingTypeLs()) {
			if (!spliceTypePredict.getBGSite().equals(lsSiteInfoBG)) {				
				lsSiteInfoBG = spliceTypePredict.getBGSite();
				BG = mapReads.getRangeInfo(spliceTypePredict.getDifSite().getRefID(), lsSiteInfoBG);
			}
			spliceType2Value.addExp(group, exonCluster.getParentGene(), spliceTypePredict, mapReads, BG);
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

	/** 计算并获得pvalue */
	public Double getAndCalculatePvalue() {
		if (exonCluster.getParentGene().getName().contains(debug)) {
			logger.debug("stop");
		}
		for (SplicingAlternativeType splicingType : exonCluster.getSplicingTypeSet()) {
			PvalueCalculate pvaCalculate = new PvalueCalculate();
			pvaCalculate.setSpliceType2Value(splicingType, condition1, mapCondition2SpliceInfo.get(condition1), 
					condition2, mapCondition2SpliceInfo.get(condition2));
			pvaCalculate.calculatePvalue();
			lsPvalueInfo.add(pvaCalculate);
		}
		Collections.sort(lsPvalueInfo);
		return lsPvalueInfo.get(0).calculatePvalue();
	}
	
	/**
	 * 计算完pvalue后{@link #getAndCalculatePvalue()}
	 * 获得该splicing type
	 * @return
	 */
	public SplicingAlternativeType getSplicingType() {
		return lsPvalueInfo.get(0).splicingType;
	}

	
	@Override
	public int compareTo(ExonSplicingTest o) {
		return pvalue.compareTo(o.pvalue);
	}
	
	public String[] toStringArray() {
		getAndCalculatePvalue();
		ArrayList<String> lsResult = new ArrayList<String>();
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		lsResult.add(gffDetailGene.getName().get(0));
		lsResult.add(mapCondition2SpliceInfo.get(condition1).getSpliceTypePredict(getSplicingType()).getDifSite().toStringNoCis());
		lsResult.add(lsPvalueInfo.get(0).getStrInfo(false, false));
		lsResult.add(lsPvalueInfo.get(0).getStrInfo(false, true));
		lsResult.add(lsPvalueInfo.get(0).getStrInfo(true, false));
		lsResult.add(lsPvalueInfo.get(0).getStrInfo(true, true));

//		double logfcJun = getLogFC(mapGroup2LsValue_Junc1, mapGroup2LsValue_Junc2);
//		double logfcJunNew = getLogFCnew(mapGroup2LsValue_Junc1, mapGroup2LsValue_Junc2);

//		double logfcExp = getLogFC(mapGroup2LsValue_Exp1, mapGroup2LsValue_Exp2);
//		double logfcExpNew = getLogFCnew(mapGroup2LsValue_Exp1, mapGroup2LsValue_Exp2);
//		
//		lsResult.add(logfcExp + "");
//		lsResult.add(logfcExpNew + "");
//		
//		lsResult.add((logfcJun + logfcExp)/2 + "");
//		lsResult.add((logfcJunNew + logfcExpNew)/2 + "");
		
		
		
		lsResult.add(getAndCalculatePvalue() + "");
		lsResult.add(fdr + "");
		//TODO
		lsResult.add(getSplicingType().toString());
//		GeneID geneID = gffDetailGene.getSetGeneID().iterator().next();
//		lsResult.add(geneID.getSymbol());
//		lsResult.add(geneID.getDescription());
		
//		if (seqHash != null) {
//			try {
//				ArrayList<SeqFasta> lsSeqFasta = getSeq(seqHash);
//				for (SeqFasta seqFasta : lsSeqFasta) {
//					try {
//						lsResult.add(seqFasta.toString());
//					} catch (Exception e) {
//						lsResult.add("");
//					}
//				}
//			} catch (Exception e) {
//			}
//		}
		return lsResult.toArray(new String[0]);
	}
		
//	private double getLogFC(List<List<Double>> lsInfo1, List<List<Double>> lsInfo2) {
//		if (lsInfo1.size() < 2 || lsInfo2.size() < 2) return 0;
//		
//		double a1 = MathComput.sum(lsInfo1.get(0)), b1 = MathComput.sum(lsInfo1.get(1));
//		double a2 = MathComput.sum(lsInfo2.get(0)), b2 = MathComput.sum(lsInfo2.get(1));
//		double result = Math.log((a1*b2 + 1)/(a2*b1+1))/Math.log(2);
//		return Math.abs(result);
//	}
//	
//	private double getLogFCnew(List<List<Double>> lsInfo1, List<List<Double>> lsInfo2) {
//		if (lsInfo1.size() < 2 || lsInfo2.size() < 2) return 0;
//		
//		double a1 = MathComput.sum(lsInfo1.get(0)), b1 = MathComput.sum(lsInfo1.get(1));
//		double a2 = MathComput.sum(lsInfo2.get(0)), b2 = MathComput.sum(lsInfo2.get(1));
//		double result = Math.log(a2-a1*b2/b1);
//		return Math.abs(result);
//	}
	
	
	public String[] toStringSeq() {
		if (seqHash == null) {
			return null;
		}
		getAndCalculatePvalue();
		ArrayList<String> lsResult = new ArrayList<String>();
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		lsResult.add(gffDetailGene.getName().get(0));
		lsResult.add(mapCondition2SpliceInfo.get(condition1).getSpliceTypePredict(getSplicingType()).getDifSite().toStringNoCis());

		try {
			ArrayList<SeqFasta> lsSeqFasta = getSeq(seqHash);
			for (SeqFasta seqFasta : lsSeqFasta) {
				try {
					lsResult.add(seqFasta.toString());
				} catch (Exception e) {
					lsResult.add("");
				}
			}
		} catch (Exception e) {
			String[] ss = new String[5];
			for (int i = 0; i < ss.length; i++) {
				ss[i] = "";
			}
			return ss;
		}
		
		return lsResult.toArray(new String[0]);
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
		//TODO 提取该exon左右两端的exon，写的很丑
		ExonCluster exonClusterBefore = exonCluster.getExonClusterBefore();
		boolean flag = true;
		while (flag && exonClusterBefore != null) {
			for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
				if (exonClusterBefore.getMapIso2LsExon().containsKey(gffGeneIsoInfo) && exonClusterBefore.getMapIso2LsExon().get(gffGeneIsoInfo).size() > 0) {
					lsGetExon.add(new ExonInfo(exonCluster.isCis5to3(), exonClusterBefore.getStartCis(), exonClusterBefore.getEndCis()));
					flag = false;
					break;
				}
				exonClusterBefore = exonClusterBefore.getExonClusterBefore();
			}	
		}
		
		lsGetExon.add(new ExonInfo(exonCluster.isCis5to3(), exonCluster.getStartCis(), exonCluster.getEndCis()));
		
		ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
		flag = true;
		while (flag && exonClusterAfter != null) {
			for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
				if (exonClusterAfter.getMapIso2LsExon().containsKey(gffGeneIsoInfo) && exonClusterAfter.getMapIso2LsExon().get(gffGeneIsoInfo).size() > 0) {
					lsGetExon.add(new ExonInfo( exonCluster.isCis5to3(), exonClusterAfter.getStartCis(), exonClusterAfter.getEndCis()));
					flag = false;
					break;
				}
				exonClusterAfter = exonClusterAfter.getExonClusterAfter();
			}	
		}
		
		seqFasta = seqHash.getSeq(exonCluster.isCis5to3(), exonCluster.getRefID(), lsGetExon, true);
		lsSeqFastas.add(seqFasta);
		
		return lsSeqFastas;
	}
	
	/** 输入的信息会自动排序 */
	public static void sortAndFdr(List<ExonSplicingTest> colExonSplicingTests) {
		//按照pvalue从小到大排序
		Collections.sort(colExonSplicingTests, new Comparator<ExonSplicingTest>() {
			public int compare(ExonSplicingTest o1, ExonSplicingTest o2) {
				return o1.getAndCalculatePvalue().compareTo(o2.getAndCalculatePvalue());
			}
		});
//		
		ArrayList<Double> lsPvalue = new ArrayList<Double>();
		for (ExonSplicingTest exonSplicingTest : colExonSplicingTests) {
			//TODO
			if (exonSplicingTest.getExonCluster().getParentGene().getName().contains(debug)) {
				logger.error("stop");
			}
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
	public static String[] getTitle(String condition1, String condition2) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add(condition1 + "_Skip::Others");
		lsTitle.add(condition2 + "_Skip::Others");
		lsTitle.add(condition1 + "Exp");
		lsTitle.add(condition2 + "Exp");
//		lsTitle.add("LogFoldChange_Exp_Type1");
//		lsTitle.add("LogFoldChange_Exp_Type2");
//		
//		lsTitle.add("LogFoldChange_Type1");
//		lsTitle.add("LogFoldChange_Type2");

		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add("SplicingType");
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		return lsTitle.toArray(new String[0]);
	}
	/** 获得标题 */
	public static String[] getSeqTitle() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add("Casual_Exon");
		lsTitle.add("Casual_Exon+-300bp");
		lsTitle.add("Casual_Exon+-1Exon");
		return lsTitle.toArray(new String[0]);
	}	
	
	class PvalueCalculate implements Comparable<PvalueCalculate> {
		int normExp = 50;
		int junction = 200;
		SplicingAlternativeType splicingType;
		ISpliceTestModule iSpliceTestExp;
		ISpliceTestModule iSpliceTestJun;
		double pvalue = -1;
		public void setSpliceType2Value(SplicingAlternativeType splicingType, String condTreat,
				SpliceType2Value spliceType2ValueTreat, String condCtrl, SpliceType2Value spliceType2ValueCtrl) {
			pvalue = -1;
			this.splicingType = splicingType;
			
			iSpliceTestExp = new SpliceTestCombine();
			ArrayListMultimap<String, Double> lsExp1 = spliceType2ValueTreat.getLsExp(splicingType);
			ArrayListMultimap<String, Double> lsExp2= spliceType2ValueCtrl.getLsExp(splicingType);
			iSpliceTestExp.setNormalizedNum(normExp);
			iSpliceTestExp.setLsRepeat2Value(mapCond_Group2JunNum, condTreat, lsExp1, condCtrl, lsExp2);
			
			iSpliceTestJun = new SpliceTestCombine();
			ArrayListMultimap<String, Double> lsJunc1 = spliceType2ValueTreat.getLsJun(splicingType);
			ArrayListMultimap<String, Double> lsJunc2 = spliceType2ValueCtrl.getLsJun(splicingType);
			iSpliceTestJun.setNormalizedNum(junction);
			iSpliceTestJun.setLsRepeat2Value(mapCond_Group2JunNum, condTreat, lsJunc1, condCtrl, lsJunc2);
		}
		
		public double calculatePvalue() {
			if (pvalue < 0) {
				double pvalueExp = iSpliceTestExp.calculatePvalue();
				double pvalueJun = iSpliceTestJun.calculatePvalue();
				pvalue = getPvalueCombine(pvalueExp, pvalueJun);
			}
			return pvalue;
		}

		/** 
		 *  公式：2^((log2(0.8)*0.5 + log2(0.1)*0.5))
		 *  */
		private double getPvalueCombine(double pvalueExp, double pvalueCounts) {
			double pvalue = 1.0;
			if (pvalueExp < 0) {
				pvalue = 1.0;
				return pvalue;
			}
			if (pvalueCounts == 1) {
				return 1;
			}
			double expPro = getPvaluePropExp();
//			double pvalueLog = Math.log10(pvalueExp) * expPro +  Math.log10(pvalueCounts) * (1 - expPro);
//			pvalue = Math.pow(10, pvalueLog);
			
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
			//TODO 将ratio换掉，把exonCluster.getLength()换成difSite.Length();
			double ratio = (double)exonCluster.getLength()/(readsLength * 1.5);
			if (ratio > 1) {
				prop = Math.pow(0.5, 1/ratio);
			} else {
				prop = 1 - Math.pow(0.5, ratio);
			}
			
			if (prop > 0.85) {
				prop = 0.85;
			} else if (prop < 0.15) {
				prop = 0.15;
			}
			return prop;
		}
		
		public String getStrInfo( boolean isExp, boolean isCtrl) {
			String info = null;
			if (isExp) {
				if (isCtrl) {
					info = iSpliceTestExp.getCondtionCtrl(false);
				} else {
					info = iSpliceTestExp.getCondtionTreat(true);
				}
			} else {
				if (isCtrl) {
					info = iSpliceTestJun.getCondtionCtrl(false);
				} else {
					info = iSpliceTestJun.getCondtionTreat(true);
				}
			}
			return info;
		}

		@Override
		public int compareTo(PvalueCalculate o) {
			Double p1 = this.pvalue;
			Double p2 = o.pvalue;
			return p1.compareTo(p2);
		}
	}
}

//TODO 把SplicingAlternativeType换成SpliceTypePredict
/**
 * 某个时期的某个位点的<br>
 * 
 * 多种可变剪接形式的<br>
 * 
 * 表达和Junction Reads数
 * 
 * @author zong0jie
 */
class SpliceType2Value {
	private static final Logger logger = Logger.getLogger(SpliceType2Value.class);

	Set<SplicingAlternativeType> setExonSplicingTypes = new HashSet<SplicingAlternativeType>();
	Map<SplicingAlternativeType, ArrayListMultimap<String, Double>> mapSplicingType2_MapGroup2LsExpValue = new HashMap<>();
	Map<SplicingAlternativeType, ArrayListMultimap<String, Double>> mapSplicingType2_MapGroup2LsJunctionReads = new HashMap<>();
	Map<SplicingAlternativeType, SpliceTypePredict> mapSplicingType2Detail = new HashMap<SplicingAlternativeType, SpliceTypePredict>();
	
	/**
	 * 是否通过过滤
	 * 有些譬如类似alt5和alt3，如果差距太小，就不进行考虑
	 */
	boolean isFiltered = true;
	
	/** 添加表达 */
	public void addExp(String group, GffDetailGene gffDetailGene, 
			SpliceTypePredict spliceTypePredict, MapReadsAbs mapReads, double[] BGinfo) {
		//TODO
//		if (gffDetailGene.getName().contains("Foxp1") && spliceTypePredict instanceof PredictAltStart) {
//			logger.error("stop");
//		}
		
		List<Double> lsExp = new ArrayList<>();
		Align siteInfo = spliceTypePredict.getDifSite();
		ArrayListMultimap<String, Double> mapGroup2LsExp = ArrayListMultimap.create();
		double[] info = mapReads.getRangeInfo(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs(), 0);
		if (info == null || BGinfo == null) {
			mapGroup2LsExp.put(group, 0.0);
			mapGroup2LsExp.put(group, 0.0);

		} else {
			lsExp.add((double) (getMean(info) + 1));
			lsExp.add((double) (getMean(BGinfo) + 1));
			
			mapGroup2LsExp.put(group, (double) (getMean(info) + 1));
			mapGroup2LsExp.put(group, (double) (getMean(BGinfo) + 1));
		}
		mapSplicingType2_MapGroup2LsExpValue.put(spliceTypePredict.getType(), mapGroup2LsExp);
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
		ArrayListMultimap<String, Double> mapGroup2LsValue = spliceTypePredict.getJunGroup2lsValue(condition);
		mapSplicingType2_MapGroup2LsJunctionReads.put(splicingAlternativeType, mapGroup2LsValue);
		setExonSplicingTypes.add(splicingAlternativeType);
		mapSplicingType2Detail.put(splicingAlternativeType, spliceTypePredict);
	}
	
	/** 获得reads，如果不存在这种类型的可变剪接，就返回null
	 * 
	 * @param splicingAlternativeType
	 * @return
	 * list--每个group，各个位点的junction数量
	 */
	public ArrayListMultimap<String, Double> getLsJun(SplicingAlternativeType splicingAlternativeType) {
		return mapSplicingType2_MapGroup2LsJunctionReads.get(splicingAlternativeType);
	}
		
	/** 获得表达，如果不存在这种类型的可变剪接，就返回null
	 * 
	 * @param splicingAlternativeType
	 * @return
	 * list--每个group，各个位点的reads number
	 */
	public ArrayListMultimap<String, Double> getLsExp(SplicingAlternativeType splicingAlternativeType) {
		return mapSplicingType2_MapGroup2LsExpValue.get(splicingAlternativeType);
	}
	
	/**
	 * 给定指定的剪接类型，返回该剪接类型的各种指标
	 * @param splicingAlternativeType
	 * @return
	 * list--每个位点，各个group的junction reads number
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

