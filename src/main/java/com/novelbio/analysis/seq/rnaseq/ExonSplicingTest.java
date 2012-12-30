package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster.ExonSplicingType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;

/** �ɱ���ӵļ��� */
public class ExonSplicingTest implements Comparable<ExonSplicingTest> {
	private static Logger logger = Logger.getLogger(ExonSplicingTest.class);
	
	ExonCluster exonCluster;
	TophatJunction tophatJunction;
	
	/** ÿ��exonCluster����condition�Լ����Ӧ��exon��junction reads counts */
	LinkedHashMap<String, int[]> mapCondition2Counts = new LinkedHashMap<String, int[]>();
	
	//TODO �����Է������hash����
	/** ÿ��exonCluster����condition�Լ����Ӧ��exon��expression reads counts
	 * TODO ��������LinkedHashMap<String, ArrayList<int[]>> 
	 * list�������ÿ��chrID
	 *  */
	LinkedHashMap<String, int[]> mapCondition2Exp = new LinkedHashMap<String, int[]>();
	
	String condition1;
	String condition2;
	/** ����һ�������ĳ�ʼֵ */
	Double pvalue= -1.0;
	double fdr = 1.0;
	int readsLength = 100;
	
	SeqHash seqHash;
	
	public ExonSplicingTest(ExonCluster exonCluster) {
		this.exonCluster = exonCluster;
	}
	
	public void setGetSeq(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	
	public void setConditionsetAndJunction(LinkedHashSet<String> setCondition, TophatJunction tophatJunction) {
		//��ʼ��
		for (String string : setCondition) {
			mapCondition2Counts.put(string, new int[0]);
		}
		this.tophatJunction = tophatJunction;
		fillJunctionReadsData();
	}
	
	/** �����趨 */
	public void setCompareCondition(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	
	/** �趨ÿ��condition�Լ����Ӧ��reads�ѻ� */
	public void setMapCondition2MapReads(String condition, MapReads mapReads) {
		SiteInfo siteInfo = exonCluster.getDifSite();
		int[] tmpExpCond = new int[2];
		
		double[] info = mapReads.getRangeInfo(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs(), 0);
		tmpExpCond[0] = getMean(info) + 1;
		
		double[] info2 = mapReads.getRangeInfo(siteInfo.getRefID(), exonCluster.getParentGene().getLongestSplitMrna());
		tmpExpCond[1] = getMean(info2) + 1;
		
		if (tmpExpCond[0] <= 0 || tmpExpCond[1] <=0) {
			return;
		}
		if (mapCondition2Exp.containsKey(condition)) {
			int[] expCond = mapCondition2Exp.get(condition);
			expCond[0] = expCond[0] + tmpExpCond[0];
			expCond[1] = expCond[1] + tmpExpCond[1];
		}
		else {
			mapCondition2Exp.put(condition, tmpExpCond);
		}
	}
	
	private static int getMean(double[] info) {
		if (info == null) {
			return -1;
		}
		return (int)new Mean().evaluate(info);
	}
	
	/** ���򳤶ȣ���������������ж�pvalue�ı��� */
	public void setReadsLength(int readsLength) {
		this.readsLength = readsLength;
	}
	
	public ExonCluster getExonCluster() {
		return exonCluster;
	}
	
	private void fillJunctionReadsData() {
		//�����exon��iso�Ƿ���ڣ�0�����ڣ�1����
		int junc = 0;
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() > 0)
			junc = 1;

		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		String chrID = gffDetailGene.getRefID();
		//һ�� setCondition ����ֻ�����Ҳ���ǽ��Ƚ�����ʱ�ڵĿɱ����
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
	/**
	 * @param junc �����exon��iso�Ƿ���ڣ�0�����ڣ�1����
	 * @param gffDetailGene
	 * @param chrID
	 * @param condition
	 * @return
	 */
	private int[] getAlt5Reads(int junc, GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getExonInfoSingleLs();
		int[] counts = new int[lsExon.size() + junc];
		//��һλ��������exon��reads
		if (junc == 1)
			counts[0] = getJunReadsNum(gffDetailGene, condition);
		
		for (int i = 0; i < lsExon.size(); i++) {
			ExonInfo exon = lsExon.get(i);
			counts[i+junc] = tophatJunction.getJunctionSite(chrID, exon.getEndCis(), condition);
		}

		return counts;
	}
	
	/**
	 * @param junc �����exon��iso�Ƿ���ڣ�0�����ڣ�1����
	 * @param gffDetailGene
	 * @param chrID
	 * @param condition
	 * @return
	 */
	private int[] getAlt3Reads(int junc, GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getExonInfoSingleLs();
		int[] counts = new int[lsExon.size() + junc];
		if (junc == 1)
			counts[0] = getJunReadsNum(gffDetailGene, condition);
		
		for (int i = 0; i < lsExon.size(); i++) {
			ExonInfo exon = lsExon.get(i);
			counts[i+junc] = tophatJunction.getJunctionSite(chrID, exon.getStartCis(), condition);
		}

		return counts;
	}
	
	/**
	 * @param junc �����exon��iso�Ƿ���ڣ�0�����ڣ�1����
	 * @param gffDetailGene
	 * @param chrID
	 * @param condition
	 * @return
	 */
	private int[] getNorm(int junc, GffDetailGene gffDetailGene, String chrID, String condition) {
		ArrayList<ExonInfo> lsExon = exonCluster.getAllExons();
		int[] counts = new int[lsExon.size() + junc];
		
		if (junc == 1)
			counts[0] = getJunReadsNum(gffDetailGene, condition);
		
		for (int i = 0; i < lsExon.size(); i++) {
			ExonInfo exon = lsExon.get(i);
			counts[i+junc] = tophatJunction.getJunctionSite(chrID, exon.getStartCis(), condition) + tophatJunction.getJunctionSite(chrID, exon.getEndCis(), 	condition);
		}

		return counts;
	}
	/**
	 * ���������exonCluster���readsNum
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
			result = result + tophatJunction.getJunctionSite(gffDetailGene.getRefID(), Integer.parseInt(ss[0]),
					Integer.parseInt(ss[1]), condition);
		}
		
		return result;
	}
	/** ���Һ��и�exon��ת¼����
	 * ��ÿ���������ӵ����� */
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
	
	/**���Ҳ�����exon��ת¼���� 
	 * ��ÿ���������ӵ����� */
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
	private void setFdr(double fdr) {
		this.fdr = fdr;
	}
	/** ���㲢���pvalue */
	public Double getAndCalculatePvalue() {
		if (pvalue > 0) {
			return pvalue;
		}
		if (isZeroCounts()) {
			pvalue = 1.0;
			return pvalue;
		}
		
		double pvalueExp = getPvalueReads();
		double pvalueCounts = getPvalueJunctionCounts();
//		if (exonCluster.getExonSplicingType() == ExonSplicingType.retain_intron) {
//			getPvalueRetain_Intron(pvalueExp, pvalueCounts);
//		}
//		else {
			getPvalueOther(pvalueExp, pvalueCounts);
//		}
		return pvalue;
	}
	
	/** reads����Ŀ�Ƿ�Ϊ 0
	 * Ϊ0���޷�����pvalue ����ô����Ҫֱ���趨Ϊ1
	 */
	private boolean isZeroCounts() {
		int[] cond1 = mapCondition2Counts.get(condition1);
		int[] cond2 = mapCondition2Counts.get(condition2);
		boolean isZero = true;
		for (int i : cond1) {
			if (i > 3) {
				isZero = false;
			}
		}
		for (int i : cond2) {
			if (i > 3) {
				isZero = false;
			}
		}
		return isZero;
	}
	
	/** �Ƚ�junction reads
	 * ����֮ǰ����趨condition
	 */
	protected Double getPvalueJunctionCounts() {
		//���count��������ֵ���ͱ�׼��
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
	
	/** ����ͷ���-1 */
	protected Double getPvalueReads() {
		try {
			return getPvalueReadsExp();
		} catch (Exception e) {
			return -1.0;
		}
	}

	/** �Ƚ�exon �����
	 * ����֮ǰ����趨condition
	 */
	private Double getPvalueReadsExp() {
		//���ˮƽ������ֵ�ͱ�׼��
		int normalizedValue = 50;
		
		if (!mapCondition2Exp.containsKey(condition1) || !mapCondition2Exp.containsKey(condition2)) {
			return 1.0;
		}
		int[] tmpExpCond1 = mapCondition2Exp.get(condition1);
		int[] tmpExpCond2 = mapCondition2Exp.get(condition2);
		
//		long[] tmpExpCond1long = modifyInputValue(tmpExpCond1);
//		long[] tmpExpCond2long = modifyInputValue(tmpExpCond2);
		normalizeToLowValue(tmpExpCond1, normalizedValue);
		normalizeToLowValue(tmpExpCond2, normalizedValue);
		int sum = (int) (tmpExpCond1[0] + tmpExpCond1[1] + tmpExpCond2[0] + tmpExpCond2[1]);
		FisherTest fisherTest = new FisherTest(sum + 3);
		
		double pvalue = fisherTest.getTwoTailedP(tmpExpCond1[0], tmpExpCond1[1], tmpExpCond2[0], tmpExpCond2[1]);
		return pvalue;
	}
	
	/** Retain_Intron��pvalue�Ƚ���֣�����Ҫexon���ܼ���� */
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
	 * Retain_Intron��pvalue�Ƚ���֣�����Ҫexon���ܼ����
	 *  ��ʽ��2^((log2(0.8)*0.5 + log2(0.1)*0.5))
	 *  */
	private void getPvalueOther(double pvalueExp, double pvalueCounts) {
		if (pvalueExp < 0) {
			pvalue = pvalueCounts;
			return;
		}
		
		double expPro = getPvaluePropExp();
		double pvalueLog = Math.log10(pvalueExp) * expPro +  Math.log10(pvalueCounts) * (1 - expPro);
		pvalue = Math.pow(10, pvalueLog);
				
//		����cassette��pvalue��С
		if (exonCluster.getExonSplicingType() == ExonSplicingType.cassette) {
			pvalue = pvalue * pvalue * 2;
		}
		if (pvalue > 1) {
			pvalue = 1.0;
		}
		return;
	}
	
	/** ��ñ����ռ�е�pvalue�ı���
	 * exonԽ������Խ�ߣ�Խ�̱���Խ��
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
	 * ���ɱ���ӵ���ʽΪcassetteʱ�����������ֵ
	 * ���ǽ�ֵ�������ǵ�ƽ����
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
	 * ���count����̫�󣬾ͽ����׼����һ���Ƚϵ͵�ֵ
	 * @param normalizedValue ���ڸ�ֵ�Ϳ�ʼ����
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
	
	/** ���ǰһ���ͺ�һ��exon��intron������ */
	protected SeqFasta getSeq(SeqHash seqHash) {
		int start = exonCluster.getStartCis();
		int end = exonCluster.getEndCis();
		if (exonCluster.getExonClusterBefore() != null) {
			start = exonCluster.getExonClusterBefore().getStartCis();
		}
		if (exonCluster.getExonClusterAfter() != null) {
			end = exonCluster.getExonClusterAfter().getEndCis();
		}
		SeqFasta seqFasta = seqHash.getSeq(exonCluster.getChrID(), Math.min(start, end), Math.max(start, end));
		if (seqFasta != null && !exonCluster.isCis5To3()) {
			seqFasta = seqFasta.reservecom();
		}
		return seqFasta;
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
		ArrayList<String> lsResult = new ArrayList<String>();
		int[] cond1 = mapCondition2Counts.get(condition1);
		int[] cond2 = mapCondition2Counts.get(condition2);
		String condition1 = cond1[0]+ "";
		for (int i = 1; i < cond1.length; i++) {
			condition1 = condition1 + "::" + cond1[i];
		}
		
		
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		if (gffDetailGene.getName().contains("NM_001160292")) {
			logger.error("stop");
		}
		lsResult.add(gffDetailGene.getName().get(0));
		lsResult.add(exonCluster.getLocInfo());
		lsResult.add(getCondition(cond1));
		lsResult.add(getCondition(cond2));
		lsResult.add(getAndCalculatePvalue() + "");
		lsResult.add(fdr + "");
		lsResult.add(exonCluster.getExonSplicingType().toString());
//		GeneID geneID = gffDetailGene.getSetGeneID().iterator().next();
//		lsResult.add(geneID.getSymbol());
//		lsResult.add(geneID.getDescription());
		if (seqHash != null) {
			SeqFasta seqFasta = getSeq(seqHash);
			if (seqFasta != null) {
				lsResult.add(getSeq(seqHash).toString());	
			}
		}
		return lsResult.toArray(new String[0]);
	}
	
	private String getCondition(int[] cond) {
		String condition = cond[0]+ "";
		for (int i = 1; i < cond.length; i++) {
			condition = condition + "::" + cond[i];
		}
		return condition;
	}
	/** ��ñ��� */
	public static String[] getTitle(String condition1, String condition2, boolean isGetSeq) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add(condition1 + "_Skip::Others");
		lsTitle.add(condition2 + "_Skip::Others");
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

