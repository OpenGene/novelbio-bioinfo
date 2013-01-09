package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster.ExonSplicingType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamMapReads;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.TitleFormatNBC;
import com.sun.tools.javac.jvm.Code;

/** 可变剪接的检验 */
public class ExonSplicingTest implements Comparable<ExonSplicingTest> {
	private static Logger logger = Logger.getLogger(ExonSplicingTest.class);
	
	/** 实验组和对照组的junction reads数量加起来小于这个数，就返回1 */
	static int junctionReadsMinNum = 10;
	
	ExonCluster exonCluster;
	TophatJunction tophatJunction;
	
	/** 每个exonCluster组中condition以及其对应的信息 */
	HashMap<String, SpliceType2Value> mapCondition2SpliceInfo = new LinkedHashMap<String, SpliceType2Value>();
	
	/** 每个exonCluster组中condition以及其对应的排序并建索引的bam文件 */
	ArrayListMultimap<String, SamFile> mapCond2Samfile = ArrayListMultimap.create();
	Set<ExonSplicingType> setSplicingTypes;
	//差异最大的那个exonSplicingType
	ExonSplicingType splicingType;
	String condition1;
	String condition2;
	/** 设置一个负数的初始值 */
	Double pvalue= -1.0;
	double fdr = 1.0;
	int readsLength = 100;
	
	SeqHash seqHash;
	
	public ExonSplicingTest(ExonCluster exonCluster) {
		this.exonCluster = exonCluster;
		setSplicingTypes = exonCluster.getExonSplicingTypeSet();
	}
	
	public void setGetSeq(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	
	public void setConditionsetAndJunction(LinkedHashSet<String> setCondition, TophatJunction tophatJunction) {
		//初始化
		for (String string : setCondition) {
			mapCondition2SpliceInfo.put(string, null);
		}
		this.tophatJunction = tophatJunction;
	}
	
	/** 输入condition和bam文件的对照表 */
	public void setMapCond2Samfile(ArrayListMultimap<String, SamFile> mapCond2Samfile) {
		this.mapCond2Samfile = mapCond2Samfile;
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
	
	/** 
	 * 添加每个condition以及其对应的reads堆积
	 * 如果是相同的condition，则累加上去
	 */
	public void addMapCondition2MapReads(String condition, MapReadsAbs mapReads) {
		SpliceType2Value spliceType2Value = getAndCreatSpliceType2Value(condition);

		for (ExonSplicingType splicingType : setSplicingTypes) {
			ArrayList<Double> lsExp = new ArrayList<Double>();
			SiteInfo siteInfo = exonCluster.getDifSite(splicingType, tophatJunction);
			double[] info = mapReads.getRangeInfo(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs(), 0);
			double[] info2 = mapReads.getRangeInfo(siteInfo.getRefID(), exonCluster.getParentGene().getLongestSplitMrna());
			lsExp.add((double) (getMean(info) + 1));			
			lsExp.add((double) (getMean(info2) + 1));
			spliceType2Value.addExp(splicingType, lsExp);
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
	
	private static int getMean(double[] info) {
		if (info == null) {
			return -1;
		}
		return (int)new Mean().evaluate(info);
	}

	private void setFdr(double fdr) {
		this.fdr = fdr;
	}
	/** 计算并获得pvalue */
	public Double getAndCalculatePvalue() {
		if (pvalue > 0) {
			return pvalue;
		}
		fillJunctionReadsData();
		
		if (!mapCondition2SpliceInfo.containsKey(condition1) || !mapCondition2SpliceInfo.containsKey(condition2)) {
			pvalue = 1.0;
			return pvalue;
		}
		for (ExonSplicingType splicingType : setSplicingTypes) {
			double pvalueExp = getPvalueReads(splicingType);
			double pvalueCounts = getPvalueJunctionCounts(splicingType);
			double pvalue = getPvalueCombine(pvalueExp, pvalueCounts);
			if (this.pvalue < 0 || pvalue < this.pvalue) {
				this.splicingType = splicingType;
				this.pvalue = pvalue;
			}
		}
		return this.pvalue;
	}
	
	private void fillJunctionReadsData() {
		boolean junc = false;
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() > 0) {
			junc = true;
		}
		
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		String chrID = gffDetailGene.getRefID();
		
		//一般 setCondition 里面只有两项，也就是仅比较两个时期的可变剪接
		ArrayList<String> lsCondition = ArrayOperate.getArrayListKey(mapCondition2SpliceInfo);
		for (String condition : lsCondition) {
			SpliceType2Value spliceType2Value = getAndCreatSpliceType2Value(condition);
			for (ExonSplicingType splicingType : setSplicingTypes) {
				List<Double> lsJunctionCounts = null;
				if (splicingType == ExonSplicingType.alt5) {
					lsJunctionCounts = getAlt5Reads(gffDetailGene, chrID, condition);
				} else if (splicingType == ExonSplicingType.alt3) {
					lsJunctionCounts = getAlt3Reads(gffDetailGene, chrID, condition);
				} else if (splicingType == ExonSplicingType.retain_intron) {
					lsJunctionCounts = getRetainIntron(gffDetailGene, chrID, condition);
				} else if (splicingType == ExonSplicingType.cassette || splicingType == ExonSplicingType.cassette_multi) {
					lsJunctionCounts = getCasset(gffDetailGene, chrID, condition);
				} else {
					lsJunctionCounts = getNorm(junc, gffDetailGene, chrID, condition);
				}
				try {
					spliceType2Value.addJunction(splicingType, lsJunctionCounts);
				} catch (Exception e) {
					spliceType2Value = getAndCreatSpliceType2Value(condition);
					spliceType2Value.addJunction(splicingType, lsJunctionCounts);
				}
				
			}
		}
	}
	
	/**
	 * @param junc 跨过该exon的iso是否存在，0不存在，1存在
	 * @param gffDetailGene
	 * @param chrID
	 * @param condition
	 * @return
	 */
	private ArrayList<Double> getAlt5Reads(GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getExonInfoSingleLs();
		ArrayList<Double> lsCounts = new ArrayList<Double>();
	
		////合并终点loc
		Set<Integer> setExonStartLoc = new LinkedHashSet<Integer>();
		for (ExonInfo exonInfo : lsExon) {
			setExonStartLoc.add(exonInfo.getEndCis());
		}
		for (Integer exonstart : setExonStartLoc) {
			lsCounts.add( (double) tophatJunction.getJunctionSite(condition, chrID, exonstart));
		}
		return lsCounts;
	}
	
	/**
	 * @param junc 跨过该exon的iso是否存在，0不存在，1存在
	 * @param gffDetailGene
	 * @param chrID
	 * @param condition
	 * @return
	 */
	private ArrayList<Double> getAlt3Reads(GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getExonInfoSingleLs();
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		
		//合并起点loc
		Set<Integer> setExonStartLoc = new LinkedHashSet<Integer>();
		for (ExonInfo exonInfo : lsExon) {
			setExonStartLoc.add(exonInfo.getStartCis());
		}
		for (Integer exonstart : setExonStartLoc) {
			lsCounts.add( (double) tophatJunction.getJunctionSite(condition, chrID, exonstart));
		}
		
		return lsCounts;
	}

	private ArrayList<Double> getCasset(GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getExonInfoSingleLs();
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		lsCounts.add((double) getJunReadsNum(gffDetailGene, condition));
		
		//合并起点loc
		Set<Integer> setExonStartEndLoc = new LinkedHashSet<Integer>();
		for (ExonInfo exonInfo : lsExon) {
			setExonStartEndLoc.add(exonInfo.getStartCis());
			setExonStartEndLoc.add(exonInfo.getEndCis());
		}
		double casset = 0;
		for (Integer exonstart : setExonStartEndLoc) {
			casset = casset + tophatJunction.getJunctionSite(condition, chrID, exonstart);
		}
		lsCounts.add(casset);
		return lsCounts;
	}
	
	/**
	 * @param gffDetailGene
	 * @param chrID
	 * @param condition
	 * @return
	 */
	private ArrayList<Double> getRetainIntron(GffDetailGene gffDetailGene, String chrID, String condition) {
		SiteInfo siteInfo = exonCluster.getDifSite(ExonSplicingType.retain_intron, tophatJunction);
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		lsCounts.add((double) tophatJunction.getJunctionSite(chrID, siteInfo.getStartCis(), siteInfo.getEndCis()));
	
		
		List<SamFile> lsSamFile = mapCond2Samfile.get(condition);
		int throughStart = 0, throughEnd = 0;
		for (SamFile samFile : lsSamFile) {
			throughStart += getThroughSiteReadsNum(samFile, chrID, siteInfo.getStartCis());
			throughEnd += getThroughSiteReadsNum(samFile, chrID, siteInfo.getEndCis());
		}
		lsCounts.add( ((double)(throughStart + throughEnd)/2));
		
		
		return lsCounts;
	}
	
	/** 获得跨过该位点的readsNum */
	private int getThroughSiteReadsNum(SamFile samFile, String chrID, int site) {
		int throughSiteNum = 0;
		for (SamRecord samRecord : samFile.readLinesOverlap(chrID, site, site)) {
			List<Align> lsAligns = samRecord.getAlignmentBlocks();
			for (Align align : lsAligns) {
				if (align.getStartAbs() < site - 3 || align.getEndAbs() > site + 3) {
					throughSiteNum++;
					break;
				}
			}
		}
		return throughSiteNum;
	}
	/**
	 * @param junc 跨过该exon的iso是否存在，0不存在，1存在
	 * @param gffDetailGene
	 * @param chrID
	 * @param condition
	 * @return
	 */
	private ArrayList<Double> getNorm(boolean junc, GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getAllExons();
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		if (junc) {
			lsCounts.add((double) getJunReadsNum(gffDetailGene, condition));
		}
		
		//合并相同的边界
		HashSet<Integer> setLoc = new HashSet<Integer>();
		for (int i = 0; i < lsExon.size(); i++) {
			ExonInfo exon = lsExon.get(i);
			int thisCounts = tophatJunction.getJunctionSite(condition, chrID, exon.getStartCis()) 
					+ tophatJunction.getJunctionSite(condition, chrID, exon.getEndCis());
			lsCounts.add((double) thisCounts);
		}
		return lsCounts;
	}
	/**
	 * 获得跳过该exonCluster组的readsNum
	 * @param gffDetailGene
	 * @param exonCluster
	 * @param condition
	 * @return
	 */
	private int getJunReadsNum(GffDetailGene gffDetailGene,  String condition) {
		int result = 0;
		HashSet<String> setLocation = new HashSet<String>();
		setLocation.addAll(getSkipExonLoc_From_IsoHaveExon());
		setLocation.addAll(getSkipExonLoc_From_IsoWithoutExon(gffDetailGene));
		
		for (String string : setLocation) {
			String[] ss = string.split(SepSign.SEP_ID);
			result = result + tophatJunction.getJunctionSite(condition, gffDetailGene.getRefID(),
					Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
		}
		
		return result;
	}
	
	/** 查找含有该exon的转录本，
	 * 获得跨过该外显子的坐标 */
	private HashSet<String> getSkipExonLoc_From_IsoHaveExon() {
		HashSet<String> setLocation = new HashSet<String>();
		for (ArrayList<ExonInfo> lsExonInfos : exonCluster.getLsIsoExon()) {
			if (lsExonInfos.size() == 0) {
				continue;
			}
			GffGeneIsoInfo gffGeneIsoInfo = lsExonInfos.get(0).getParent();
			int exonNumBefore = lsExonInfos.get(0).getItemNum() - 1;
			int exonNumAfter = lsExonInfos.get(lsExonInfos.size() - 1).getItemNum() + 1;
			if (exonNumBefore < 0 || exonNumAfter >= gffGeneIsoInfo.size()) {
				continue;
			}
			int start = gffGeneIsoInfo.get(exonNumBefore).getEndCis();
			int end = gffGeneIsoInfo.get(exonNumAfter).getStartCis();
			setLocation.add(start + SepSign.SEP_ID + end);
		}
		return setLocation;
	}
	
	/**查找不含该exon的转录本， 
	 * 获得跨过该外显子的坐标 */
	private HashSet<String> getSkipExonLoc_From_IsoWithoutExon(GffDetailGene gffDetailGene) {
		HashSet<String> setLocation = new HashSet<String>();
		
		HashMap<GffGeneIsoInfo, Integer> hashTmp = exonCluster.getMapIso2ExonIndexSkipTheCluster();
		for (Entry<GffGeneIsoInfo, Integer> entry : hashTmp.entrySet()) {
			GffGeneIsoInfo gffGeneIsoInfo = entry.getKey();
			int exonNum = entry.getValue();
			if (exonNum >= gffGeneIsoInfo.size()-1) {
				continue;
			}
			String location = gffGeneIsoInfo.get(exonNum).getEndCis() + SepSign.SEP_ID + gffGeneIsoInfo.get(exonNum+1).getStartCis();
			setLocation.add(location);
		}
		return setLocation;
	}

	
	/** 出错就返回-1 */
	protected Double getPvalueReads(ExonSplicingType splicingType) {
		try {
			return getPvalueReadsExp(splicingType);
		} catch (Exception e) {
			return -1.0;
		}
	}

	/** 比较exon 表达量
	 * 在这之前务必设定condition
	 */
	private Double getPvalueReadsExp(ExonSplicingType splicingType) {
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
	protected Double getPvalueJunctionCounts(ExonSplicingType splicingType) {
		//如果count数超过该值，就标准化
		int normalizedNum = 200;
		
		List<Double> lsJunc1 = mapCondition2SpliceInfo.get(condition1).getLsJun(splicingType);
		List<Double> lsJunc2 = mapCondition2SpliceInfo.get(condition2).getLsJun(splicingType);
		int[] cond1 = new int[2];
		cond1[0] = lsJunc1.get(0).intValue(); cond1[1] = lsJunc1.get(1).intValue();
		int[] cond2 = new int[2];
		cond2[0] = lsJunc2.get(0).intValue(); cond2[1] = lsJunc2.get(1).intValue();
		
		if (MathComput.sum(cond1) + MathComput.sum(cond1) < junctionReadsMinNum) {
			return 1.0;
		}
		
//		cond1 = modifyInputValue(cond1);
//		cond2 = modifyInputValue(cond2);
		normalizeToLowValue(cond1, normalizedNum);
		normalizeToLowValue(cond2, normalizedNum);

		return chiSquareTestDataSetsComparison(cond1, cond2);
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


	
	/** Retain_Intron的pvalue比较奇怪，必须要exon才能计算的 */
	private void getPvalueRetain_Intron(double pvalueExp, double pvalueCounts) {
		if (pvalueExp > 0)
			pvalue = pvalueExp;
		else
			pvalue = pvalueCounts;
		
		pvalue = pvalue * 1.5;
		if (pvalue > 1) {
			pvalue = 1.0;
		}
		return;
	}
	
	/** 
	 * Retain_Intron的pvalue比较奇怪，必须要exon才能计算的
	 *  公式：2^((log2(0.8)*0.5 + log2(0.1)*0.5))
	 *  */
	private double getPvalueCombine(double pvalueExp, double pvalueCounts) {
		double pvalue = 1.0;
		if (pvalueExp < 0) {
			pvalue = pvalueCounts;
			return pvalue;
		}
		
		double expPro = getPvaluePropExp();
		double pvalueLog = Math.log10(pvalueExp) * expPro +  Math.log10(pvalueCounts) * (1 - expPro);
		pvalue = Math.pow(10, pvalueLog);
				
//		对于cassette的pvalue缩小
//		if (exonCluster.getExonSplicingType() == ExonSplicingType.cassette) {
//			pvalue = pvalue * pvalue * 2;
//		}
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
		double ratio = exonCluster.getLength()/(readsLength*3);
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
	 * 获得一系列序列：
	 * 1.前一个和后一个exon和intron的序列
	 * 2. 当前exon
	 * 3. 当前exon左右扩展300bp
	 */
	protected ArrayList<SeqFasta> getSeq(SeqHash seqHash) {
		ArrayList<SeqFasta> lsSeqFastas = new ArrayList<SeqFasta>();
		
//		ArrayList<ExonInfo> lsGetExon = new ArrayList<ExonInfo>();
//		if (exonCluster.getExonClusterBefore() != null) {
//			ExonCluster exonClusterBefore = exonCluster.getExonClusterBefore();
//			lsGetExon.add(new ExonInfo("exonCluster", exonCluster.isCis5To3(), exonClusterBefore.getStartCis(), exonClusterBefore.getEndCis()));
//		}
//		lsGetExon.add(new ExonInfo("exonCluster", exonCluster.isCis5To3(), exonCluster.getStartCis(), exonCluster.getEndCis()));
//		if (exonCluster.getExonClusterAfter() != null) {
//			ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
//			lsGetExon.add(new ExonInfo("exonCluster", exonCluster.isCis5To3(), exonClusterAfter.getStartCis(), exonClusterAfter.getEndCis()));
//		}
//		
//		SeqFasta seqFasta = seqHash.getSeq(exonCluster.getChrID(), lsGetExon, true);
//		if (seqFasta != null && !exonCluster.isCis5To3()) {
//			seqFasta = seqFasta.reservecom();
//		}
		
		SeqFasta seqFasta = seqHash.getSeq(exonCluster.isCis5To3(), exonCluster.getChrID(), 
				exonCluster.getStartLocAbs(), exonCluster.getEndLocAbs());
		lsSeqFastas.add(seqFasta);
		
		SeqFasta seqFasta2 = seqHash.getSeq(exonCluster.isCis5To3(), exonCluster.getChrID(),
				exonCluster.getStartLocAbs() - 300, exonCluster.getEndLocAbs() + 300);
		lsSeqFastas.add(seqFasta2);
		
		return lsSeqFastas;
	}
	@Override
	public int compareTo(ExonSplicingTest o) {
		return pvalue.compareTo(o.pvalue);
	}
	
	public static void setFdr(Collection<ExonSplicingTest> colExonSplicingTests) {
		ArrayList<Double> lsPvalue = new ArrayList<Double>();
		for (ExonSplicingTest exonSplicingTest : colExonSplicingTests) {
			lsPvalue.add(exonSplicingTest.getAndCalculatePvalue());
		}
		
		ArrayList<Double> lsFdr = MathComput.pvalue2Fdr(lsPvalue);
		int i = 0;
		for (ExonSplicingTest exonSplicingTest : colExonSplicingTests) {
			exonSplicingTest.setFdr(lsFdr.get(i));
			i++;
		}
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
		lsResult.add(exonCluster.getLocInfo());
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
			lsTitle.add("sequence");
		}
		return lsTitle.toArray(new String[0]);
	}
}

/**
 * 差异可变剪接的类型
 * reads数量以及表达值等
 * @author zong0jie
 */
class SpliceType2Value {
	private static Logger logger = Logger.getLogger(SpliceType2Value.class);
	
	Set<ExonSplicingType> setExonSplicingTypes = new HashSet<ExonCluster.ExonSplicingType>();
	HashMap<ExonSplicingType, List<Double>> mapSplicingType2LsExpValue = new HashMap<ExonCluster.ExonSplicingType, List<Double>>();
	HashMap<ExonSplicingType, List<Double>> mapSplicingType2LsJunctionReads = new HashMap<ExonCluster.ExonSplicingType, List<Double>>();
	
	/** 添加表达 */
	public void addExp(ExonSplicingType splicingType, List<Double> lsExp) {
		addLsDouble(mapSplicingType2LsExpValue, splicingType, lsExp);
		setExonSplicingTypes.add(splicingType);
	}
	/** 添加表达 */
	public void addJunction(ExonSplicingType splicingType, List<Double> lsJun) {
		addLsDouble(mapSplicingType2LsJunctionReads, splicingType, lsJun);
		setExonSplicingTypes.add(splicingType);
	}
	
	/** 把一个lsDouble和map里面已有的LsDouble加起来 */
	private static void addLsDouble(Map<ExonSplicingType, List<Double>> mapSplicingType2LsInfo, ExonSplicingType splicingType, List<Double> lsJun) {
		List<Double> lsNew = lsJun;
		if (mapSplicingType2LsInfo.containsKey(splicingType)) {
			lsNew = new ArrayList<Double>();
			List<Double> lsJunOld = mapSplicingType2LsInfo.get(splicingType);
			if (lsJunOld.size() != lsJun.size()) {
				logger.error("出错");
				return;
			}
			//新老加起来放入map
			for (int i = 0; i < lsJunOld.size(); i++) {
				lsNew.add(lsJunOld.get(i) + lsJun.get(i));
			}
		}
		
		mapSplicingType2LsInfo.put(splicingType, lsJun);
	}
	
	/** 获得reads，如果不存在这种类型的可变剪接，就返回null */
	public List<Double> getLsJun(ExonSplicingType exonSplicingType) {
		if (!setExonSplicingTypes.contains(exonSplicingType)) {
			return null;
		}
		return mapSplicingType2LsJunctionReads.get(exonSplicingType);
	}
	
	
	
	
	/** 获得表达，如果不存在这种类型的可变剪接，就返回null */
	public List<Double> getLsExp(ExonSplicingType exonSplicingType) {
		if (!setExonSplicingTypes.contains(exonSplicingType)) {
			return null;
		}
		return mapSplicingType2LsExpValue.get(exonSplicingType);
	}
}