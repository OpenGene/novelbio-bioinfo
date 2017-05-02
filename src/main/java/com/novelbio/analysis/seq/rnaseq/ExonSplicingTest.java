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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictRetainIntron;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.ISpliceTestModule.SpliceTestFactory;
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
	private static final Logger logger = LoggerFactory.getLogger(ExonSplicingTest.class);
	
	/** 实验组和对照组的junction reads数量加起来小于这个数，就返回1 */
	static int junctionReadsMinNum = 10;
		
	/** 没有重建转录本的老iso的名字 */
	Set<String> setIsoName_No_Reconstruct;
	
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
	double fdr = 1.0;
	Set<String> setCondition;
	/** readsLength越长，juncReadsPvalue所占的比例就越大 */
	int readsLength = 100;
	
	SeqHash seqHash;
	
	/** junction的pvalue所占的比重
	 * 小于0 或者大于1 表示动态比重
	 */
	double pvalueJunctionProp = -1;
	
	int juncAllReadsNum = 25;
	int juncSampleReadsNum = 10;
	
	Map<String, Map<String, double[]>> mapCond_Group2ReadsNum;
	Map<String, Map<String, double[]>> mapCond_Group2JunNum;
	
	/** 是否合并文件--也就是不考虑重复，默认为true，也就是合并文件 */
	boolean isCombine = true;
	
	/** 显示最后区域的，主要是给MXE使用 */
	Align alignDisplay;
	
	int minLen;
	
	private static final String debug = "HORVU1Hr1G000010";
	
	public ExonSplicingTest(ExonCluster exonCluster, int minLen) {
		this.exonCluster = exonCluster;
		this.minLen = minLen;
	}
	/** 显示最后区域的，如 chr1:23456-34567，主要是给MXE使用 */
	public void setAlignDisplay(Align alignDisplay) {
		this.alignDisplay = alignDisplay;
	}
	/** 设定junction数量，小于该数量的不会进行分析
	 * 
	 * @param juncAllReadsNum 所有样本的junction数量必须大于该值，否则不进行计算，默认25
	 * @param juncSampleReadsNum 单个样本的junction数量必须大于该值，否则不进行计算，默认10
	 */
	public void setJuncReadsNum(int juncAllReadsNum, int juncSampleReadsNum) {
	    this.juncAllReadsNum = juncAllReadsNum;
	    this.juncSampleReadsNum = juncSampleReadsNum;
    }
	/** 设定没有重建转录本的老iso的名字 */
	public void setSetIsoName_No_Reconstruct(Set<String> setIsoName_No_Reconstruct) {
		this.setIsoName_No_Reconstruct = setIsoName_No_Reconstruct;
	}
	/** 是否合并文件--也就是不考虑重复，默认为true，也就是合并文件 */
	public void setCombine(boolean isCombine) {
		this.isCombine = isCombine;
	}
	public void setGetSeq(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	/**
	 * pvalue的计算是合并exon表达pvalue和junction pvalue 
	 * junction的pvalue所占的比重
	 * 小于0 或者大于1 表示动态比重，也就是从exon的长度上推断pvalue
	 */
	public void setPvalueJunctionProp(double pvalueJunctionProp) {
		this.pvalueJunctionProp = pvalueJunctionProp;
	}
	/** 必须设定，总共的condition数 */
	public void setSetCondition(Set<String> setCondition) {
		this.setCondition = setCondition;
	}
	/** 必须设定 */
	public void setCompareCondition(String condition1, String condition2) {
		this.condition1 = condition1;
		this.condition2 = condition2;
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
		if (exonCluster.getParentGene().getName().contains(debug)) {
			logger.debug("stop");
		}

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
		if (exonCluster.getParentGene().getName().contains(debug)) {
			logger.debug("stop");
		}
		
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
		for (SpliceTypePredict spliceTypePredict : exonCluster.getSplicingTypeLs()) {
			List<? extends Alignment> lsSiteInfoBG = spliceTypePredict.getBGSite();
			List<Align> lsSiteInfo = spliceTypePredict.getDifSite();
			
			String refId = exonCluster.getRefID();
			double[] BGinfo = mapReads.getRangeInfo(refId, lsSiteInfoBG);
			double[] info = mapReads.getRangeInfo(refId, lsSiteInfo);

			spliceType2Value.addExp(group, exonCluster.getParentGene(), spliceTypePredict, BGinfo, info);
		}
		mapReads = null;
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
	
	/** 是否不需要做检验，主要是检验alt3和alt5是否特别短，只有几bp，这种在ion proton中常见 */
	public boolean isTestEmpty() {
		return exonCluster.getSplicingTypeSet(minLen).isEmpty();
	}
	
	/** 计算并获得pvalue */
	public Double getAndCalculatePvalue() {
		lsPvalueInfo = new ArrayList<>();
		if (exonCluster.getParentGene().getName().contains(debug)) {
			logger.debug("stop");
		}
		if (!lsPvalueInfo.isEmpty()) {
			return lsPvalueInfo.get(0).calculatePvalue();
		}
		
		for (SplicingAlternativeType splicingType : exonCluster.getSplicingTypeSet(minLen)) {
			PvalueCalculate pvaCalculate = new PvalueCalculate();
			pvaCalculate.setCombine(isCombine);
			pvaCalculate.setSpliceType2Value(splicingType, condition1, mapCondition2SpliceInfo.get(condition1), 
					condition2, mapCondition2SpliceInfo.get(condition2));
			
			pvaCalculate.calculatePvalue();
			lsPvalueInfo.add(pvaCalculate);
		}
		
		if (lsPvalueInfo.isEmpty()) {
			logger.debug("cannot find splicing site: {}", exonCluster.getParentGene().getNameSingle());
			PvalueCalculate pvaCalculate = new PvalueCalculate();
			pvaCalculate.pvalueAvg = 1;
			pvaCalculate.pvalueAvg = 1;
			pvaCalculate.pvalueAvg = 1;

			pvaCalculate.pvalueAvg = 1;

			lsPvalueInfo.add(pvaCalculate);
			return 1.0;
		}
		
		Collections.sort(lsPvalueInfo, new Comparator<PvalueCalculate>() {
			public int compare(PvalueCalculate o1, PvalueCalculate o2) {
				int[] readsInfo1 = o1.getReadsInfo();
				int[] readsInfo2 = o2.getReadsInfo();
				Integer o1min = Math.min(readsInfo1[0], readsInfo1[1]);
				Integer o2min = Math.min(readsInfo2[0], readsInfo2[1]);
				Double o1result = o1.calculatePvalue()/o1min;
				Double o2result = o2.calculatePvalue()/o2min;
				
				return o1result.compareTo(o2result);
			}
		});
		
		//优先选择 SE 的剪接形式
		PvalueCalculate pvalueFirst = lsPvalueInfo.get(0);
		if (pvalueFirst.splicingType != SplicingAlternativeType.cassette) {
			PvalueCalculate pvalueSe = null;
			int i = 0;
			for (PvalueCalculate pvalueCalculate : lsPvalueInfo) {
				if (pvalueCalculate.splicingType == SplicingAlternativeType.cassette) {
					pvalueSe = pvalueCalculate;
					break;
				}
				i++;
			}
			if (pvalueSe != null && pvalueSe.calculatePvalue()/pvalueFirst.calculatePvalue() < 1.1) {
				lsPvalueInfo.remove(i);
				lsPvalueInfo.add(0, pvalueSe);
			}
		}
		
		return lsPvalueInfo.get(0).calculatePvalue();
	}
	
	public double getfdr() {
		return fdr;
	}
	
	/**
	 * 计算完pvalue后{@link #getAndCalculatePvalue()}
	 * 获得该splicing type所对应的pvalue
	 * @return
	 */
	public PvalueCalculate getSpliceTypePvalue() {
		return lsPvalueInfo.get(0);
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
		return getAndCalculatePvalue().compareTo(o.getAndCalculatePvalue());
	}
	
	/** 获得具体的剪接位点区域 */
	public String getSpliceSite() {
		Align align = getSpliceSiteAlignDisplay();
		if (align == null) {
			return "";
		}
		return align.toStringNoStrand();
	}
	/** 获得具体的剪接位点区域 */
	public String getSpliceSite(Map<String, String> mapChrIdLowcase2ChrId) {
		Align align = getSpliceSiteAlignDisplay();
		if (align == null) {
			return "";
		}
		align.setChrID(mapChrIdLowcase2ChrId.get(align.getRefID().toLowerCase()));
		return align.toStringNoStrand();
	}
	public Align getSpliceSiteAlignDisplay() {
		if (alignDisplay != null) {
			return alignDisplay;
		}
		
		List<Align> lsAligns = mapCondition2SpliceInfo.get(condition1).getSpliceTypePredict(getSplicingType()).getDifSite();
		if (lsAligns.isEmpty()) {
			return null;
		}
		if (lsAligns.size() == 1) {
			Align align = new Align(lsAligns.get(0));
			align.setChrID(exonCluster.getRefID());
		}
		
		Align alignStart = lsAligns.get(0);
		Align alignEnd = lsAligns.get(lsAligns.size()-1);
		Align alignResult = new Align(exonCluster.getRefID(), Math.min(alignStart.getStartAbs(), alignEnd.getStartAbs()), 
				Math.max(alignStart.getEndAbs(), alignEnd.getEndAbs()));
		return alignResult;
	}
	
	public double getPvalue() {
		return lsPvalueInfo.get(0).pvalueAvg;
	}
	
	/** 
	 * 获得本exon坐标
	 * @return
	 */
	protected Align getAlignSite() {
		return new Align(exonCluster.getRefID(), exonCluster.getStartCis(), exonCluster.getEndCis());
	}
	/** 
	 * 获得前一个exon的坐标,本exon坐标,后一个exon的坐标
	 * 根据实际的方向而来的, 所以需要考虑方向
	 * 如果前后的exon不存在，则为null
	 * 譬如 ls-0 null ls-1 align ls-2 align 表示本exon前面没有exon了
	 * @return
	 */
	protected List<Align> getLsAlignBeforeThisAfter(Map<String, String> mapChrIdLowcase2ChrId) {
		//TODO 提取该exon左右两端的exon，写的很丑
		ExonCluster exonClusterBefore = exonCluster.getExonClusterBefore();
		boolean flag = true;
		Align beforeAlign = null, thisAlign = null, afterAlign = null;
		while (flag && exonClusterBefore != null) {
			for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
				if (exonClusterBefore.getMapIso2LsExon().containsKey(gffGeneIsoInfo) && exonClusterBefore.getMapIso2LsExon().get(gffGeneIsoInfo).size() > 0) {
					beforeAlign = new Align(exonCluster.getRefID(), exonClusterBefore.getStartCis(), exonClusterBefore.getEndCis());
					flag = false;
					break;
				}
			}
			exonClusterBefore = exonClusterBefore.getExonClusterBefore();
		}
		
		thisAlign = new Align(exonCluster.getRefID(), exonCluster.getStartCis(), exonCluster.getEndCis());
		
		ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
		flag = true;
		while (flag && exonClusterAfter != null) {
			for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2LsExon().keySet()) {
				if (exonClusterAfter.getMapIso2LsExon().containsKey(gffGeneIsoInfo) && exonClusterAfter.getMapIso2LsExon().get(gffGeneIsoInfo).size() > 0) {
					afterAlign = new Align( exonCluster.getRefID(), exonClusterAfter.getStartCis(), exonClusterAfter.getEndCis());
					flag = false;
					break;
				}
			}
			exonClusterAfter = exonClusterAfter.getExonClusterAfter();
		}
		
		List<Align> lsGetExon = new ArrayList<Align>();
		lsGetExon.add(beforeAlign);
		lsGetExon.add(thisAlign);
		lsGetExon.add(afterAlign);
		for (Align align : lsGetExon) {
			if (align != null) {
				align.setChrID(mapChrIdLowcase2ChrId.get(align.getRefID().toLowerCase()));
			}
		}
		return lsGetExon;
	}
	
	/** 输入的信息会自动排序 */
	public static void sortAndFdr(List<ExonSplicingTest> colExonSplicingTests, double fdrCutoff) {
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
			if (exonSplicingTest.getExonCluster().getStartAbs() == 136862119) {
				logger.debug("stop");
			}
			if (exonSplicingTest.getAndCalculatePvalue() > fdrCutoff) {
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
	
	/**
	 * 该剪接位置在具体哪个地方,譬如1-2表示第一个和第二个之间, 3表示第三个
	 * @return
	 */
	public String getExonNumStr() {
		return exonCluster.getExonNum(setIsoName_No_Reconstruct);
	}
	public String[] toStringArray(Map<String, String> mapChrIdLowcase2ChrId) {
		ArrayList<String> lsResult = new ArrayList<String>();
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		lsResult.add(gffDetailGene.getNameSingle());
		lsResult.add(getSpliceSite(mapChrIdLowcase2ChrId));
		PvalueCalculate pvalueCalculate = lsPvalueInfo.get(0);
		try {
			lsResult.add(exonCluster.getExonNum(setIsoName_No_Reconstruct));

		} catch (Exception e) {
			lsResult.add(exonCluster.getExonNum(setIsoName_No_Reconstruct));
		}
		lsResult.add(pvalueCalculate.getStrInfo(false, false));
		lsResult.add(pvalueCalculate.getStrInfo(false, true));
		lsResult.add(pvalueCalculate.getStrInfo(true, false));
		lsResult.add(pvalueCalculate.getStrInfo(true, true));
//		lsResult.add(pvalueCalculate.getPvalueJun() + "");
//		lsResult.add(pvalueCalculate.getPvalueExp() + "");
//		lsResult.add(pvalueCalculate.getPvalueAvg() + "");
//		
//		lsResult.add(pvalueCalculate.iSpliceTestExp.getSpliceIndex() + "");
		lsResult.add(pvalueCalculate.getPvalueAvg() + "");
		lsResult.add(pvalueCalculate.getPvalueRootAvg() + "");
		lsResult.add(fdr + "");
		//TODO
		lsResult.add(getSplicingType().toString());
		return lsResult.toArray(new String[0]);
	}

	public String[] toStringArray_ASD(Map<String, String> mapChrIdLowcase2ChrId) {
		ArrayList<String> lsResult = new ArrayList<String>();
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		lsResult.add(gffDetailGene.getNameSingle());
		lsResult.add(getSpliceSite(mapChrIdLowcase2ChrId));
		PvalueCalculate pvalueCalculate = lsPvalueInfo.get(0);
		lsResult.add(exonCluster.getExonNum(setIsoName_No_Reconstruct));

		lsResult.add(pvalueCalculate.getStrInfo(false, false));
		lsResult.add(pvalueCalculate.getStrInfo(false, true));
		lsResult.add(pvalueCalculate.getStrInfo(true, false));
		lsResult.add(pvalueCalculate.getStrInfo(true, true));
		
//		lsResult.add(pvalueCalculate.getStrNormInfo(false));
//		lsResult.add(pvalueCalculate.getStrNormInfo(true));
//		lsResult.add(pvalueCalculate.getPvalueAvg() + "");
		lsResult.add(pvalueCalculate.getPvalueRootAvg() + "");
		lsResult.add(fdr + "");
		//TODO
		lsResult.add(getSplicingType().toString());
		return lsResult.toArray(new String[0]);
	}
	
	/** 获得标题 */
	public static String[] getTitle_ASD(String condition1, String condition2) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add("Exon_Number");
		lsTitle.add(condition1 + "_Skip::Others");
		lsTitle.add(condition2 + "_Skip::Others");
		lsTitle.add(condition1 + "Exp");
		lsTitle.add(condition2 + "Exp");
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add("SplicingType");

		return lsTitle.toArray(new String[0]);
	}
	
	/** 获得标题 */
	public static String[] getTitle(String condition1, String condition2) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add("Exon_Number");
		lsTitle.add(condition1 + "_Skip::Others");
		lsTitle.add(condition2 + "_Skip::Others");
		lsTitle.add(condition1 + "Exp");
		lsTitle.add(condition2 + "Exp");
		
//		lsTitle.add("readsInfoDetailJun");
//		lsTitle.add("readsInfoDetailExp");
//		lsTitle.add("P-Value_Jun");
//		lsTitle.add("P-Value_Exp");
		lsTitle.add("P-Value_Average");
		
//		lsTitle.add("Splicing_Index");
		
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add("SplicingType");
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
	
	public class PvalueCalculate implements Comparable<PvalueCalculate> {
		boolean isCombine = true;
		//TODO
		int normExp = 300;
		int junction = 300;
		SplicingAlternativeType splicingType;
		ISpliceTestModule iSpliceTestExp;
		ISpliceTestModule iSpliceTestJun;
		double pvalueAvg = -1;
		double pvalueRootAvg = -1;
		double pvalueJun = 1;
		double pvalueExp = 1;
		
		/**
		 * 是否将n次重复的reads合并为一个bam文件，然后进行分析
		 * @param combine <br>true：合并，false：考虑重复<br>
		 * 默认为true
		 * @return
		 */
		public void setCombine(boolean isCombine) {
			this.isCombine = isCombine;
		}
		
		public void setNotSignificant() {
			pvalueAvg = 1;
			pvalueRootAvg = 1;
			pvalueJun = 1;
			pvalueExp = 1;
		}
		
		/** 返回junction的reads信息 */
		public int[] getReadsInfo() {
			return iSpliceTestJun.getReadsInfo();
		}
		
		public void setSpliceType2Value(SplicingAlternativeType splicingType, String condTreat,
				SpliceType2Value spliceType2ValueTreat, String condCtrl, SpliceType2Value spliceType2ValueCtrl) {
			pvalueAvg = -1;
			pvalueRootAvg = -1;
			pvalueJun = 1;
			pvalueExp = 1;
			
			this.splicingType = splicingType;
			
			iSpliceTestExp = SpliceTestFactory.createSpliceModule(isCombine);
			ArrayListMultimap<String, Double> lsExp1 = spliceType2ValueTreat.getLsExp(splicingType);
			ArrayListMultimap<String, Double> lsExp2= spliceType2ValueCtrl.getLsExp(splicingType);
			iSpliceTestExp.setMakeSmallValueBigger(true, 80, 2);
			iSpliceTestExp.setJuncReadsNum(juncAllReadsNum, juncSampleReadsNum);

			iSpliceTestExp.setLsRepeat2Value(mapCond_Group2ReadsNum, condTreat, lsExp1, condCtrl, lsExp2);
			
			iSpliceTestJun = SpliceTestFactory.createSpliceModule(isCombine);
			ArrayListMultimap<String, Double> lsJunc1 = spliceType2ValueTreat.getLsJun(splicingType);
			ArrayListMultimap<String, Double> lsJunc2 = spliceType2ValueCtrl.getLsJun(splicingType);
			iSpliceTestJun.setMakeSmallValueBigger(false, 0, 0);
			iSpliceTestJun.setJuncReadsNum(juncAllReadsNum, juncSampleReadsNum);
			iSpliceTestJun.setLsRepeat2Value(mapCond_Group2JunNum, condTreat, lsJunc1, condCtrl, lsJunc2);
			
			if (this.splicingType == SplicingAlternativeType.mutually_exclusive) {
				//如果是MXE，则将小与200的reads数都乘以1.5再标准化
				iSpliceTestJun.setMakeSmallValueBigger(true, 200, 1.5);
			}
			
			if (this.splicingType == SplicingAlternativeType.retain_intron) {
				iSpliceTestExp.setNormalizedNum(200000);
				iSpliceTestJun.setNormalizedNum(200000);
			} else {
				iSpliceTestExp.setNormalizedNum(normExp);
				iSpliceTestJun.setNormalizedNum(junction);
			}
		}
		
		public double calculatePvalue() {
			if (pvalueAvg < 0) {
				try {
					pvalueExp = iSpliceTestExp.calculatePvalue();

				} catch (Exception e) {
					pvalueExp = iSpliceTestExp.calculatePvalue();
				}
				pvalueJun = iSpliceTestJun.calculatePvalue();
			
				pvalueAvg = getPvalueCombine(pvalueExp, pvalueJun, false);
				pvalueRootAvg = getPvalueCombine(pvalueExp, pvalueJun, true);
			}
			return pvalueRootAvg;
		}

		/** 
		 *  公式：2^((log2(0.8)*0.5 + log2(0.1)*0.5))
		 *  */
		private double getPvalueCombine(double pvalueExp, double pvalueCounts, boolean isRootAvg) {
			double pvalue = 1.0;
			if (pvalueExp < 0) {
				pvalue = 1.0;
				return pvalue;
			}
			if (pvalueCounts == 1) {
				return 1;
			}
			double expPro = getPvaluePropExp();
						
			if (isRootAvg) {
//				if (splicingType == SplicingAlternativeType.retain_intron) {
//					return pvalueCounts;
//				}
				
				if (pvalueJunctionProp >= 0 && pvalueJunctionProp <= 1) {
					double pvalueLog = Math.log10(pvalueExp) * (1-pvalueJunctionProp) +  Math.log10(pvalueCounts) * pvalueJunctionProp;
					pvalue = Math.pow(10, pvalueLog);
				} else {
					double pvalueLog = Math.log10(pvalueExp) * expPro +  Math.log10(pvalueCounts) * (1 - expPro);
					pvalue = Math.pow(10, pvalueLog);
				}
			} else {
				if (pvalueJunctionProp >= 0 && pvalueJunctionProp <= 1) {
					pvalue = pvalueExp * (1-pvalueJunctionProp) + pvalueCounts * pvalueJunctionProp;
				} else {
					pvalue = pvalueExp * expPro +  pvalueCounts * (1 - expPro);
				}
			}
		
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
			double ratio = (double)exonCluster.getLength()/(readsLength * 2);
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
					info = iSpliceTestExp.getCondtionCtrl(true);
				} else {
					info = iSpliceTestExp.getCondtionTreat(true);
				}
			} else {
				if (isCtrl) {
					info = iSpliceTestJun.getCondtionCtrl(true);
				} else {
					info = iSpliceTestJun.getCondtionTreat(true);
				}
			}
			return info;
		}
		
		/** 目前只考虑覆盖度的index，因为比较好算 */
		public double getSpliceIndex() {
			return iSpliceTestExp.getSpliceIndex();
		}
		
		public String getStrNormInfo( boolean isExp) {
			String info = isExp? iSpliceTestExp.getSiteInfo() : iSpliceTestJun.getSiteInfo();
			return info;
		}
		
		public double getPvalueExp() {
			return pvalueExp;
		}
		public double getPvalueJun() {
			return pvalueJun;
		}
		
		public double getPvalueRootAvg() {
			return pvalueRootAvg;
		}
		public double getPvalueAvg() {
			return pvalueAvg;
		}
		@Override
		public int compareTo(PvalueCalculate o) {
			Double p1 = this.pvalueRootAvg;
			Double p2 = o.pvalueRootAvg;
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
	private static final Logger logger = LoggerFactory.getLogger(SpliceType2Value.class);

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
			SpliceTypePredict spliceTypePredict, double[] BGinfo, double[] info) {
		ArrayListMultimap<String, Double> mapGroup2LsExp = mapSplicingType2_MapGroup2LsExpValue.get(spliceTypePredict.getType());
		if (mapGroup2LsExp == null) {
			mapGroup2LsExp = ArrayListMultimap.create();
			mapSplicingType2_MapGroup2LsExpValue.put(spliceTypePredict.getType(), mapGroup2LsExp);
		}
		if (info == null || BGinfo == null) {
			mapGroup2LsExp.put(group, 0.0);
			mapGroup2LsExp.put(group, 0.0);
		} else {
			mapGroup2LsExp.put(group, (double) (getMean(info) + 1));
			mapGroup2LsExp.put(group, (double) (getMean(BGinfo) + 1));
		}
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

