package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.ExonSplicingTest.PvalueCalculate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 分析结果的简单版模型
 * @author zong0jie
 * @data 2017年4月28日
 */
public class ExonSplicingResultUnit {
	String accId;
	Align alignLoc;
	/** 本剪接位点周边的坐标 */
	List<Align> lsBeforeThisAfterAlign;
	String exonNumStr;
	String cond1_SkipvsOther;
	String cond2_SkipvsOther;
	String cond1_Exp;
	String cond2_Exp;
	double pvalueAvg;
	double pvalue;
	double fdr;
	double spliceIndex;
	SplicingAlternativeType splicingType;
	
	public ExonSplicingResultUnit(ExonSplicingTest exonSplicingTest, Map<String, String> mapChrIdLowcase2ChrId) {
		GffDetailGene gffDetailGene = exonSplicingTest.exonCluster.getParentGene();
		PvalueCalculate pvalueCalculate = exonSplicingTest.getSpliceTypePvalue();
		accId = gffDetailGene.getNameSingle();
		alignLoc = exonSplicingTest.getSpliceSiteAlignDisplay();
		alignLoc.setChrID(mapChrIdLowcase2ChrId.get(alignLoc.getRefID().toLowerCase()));
		try {
			lsBeforeThisAfterAlign = exonSplicingTest.getLsAlignBeforeThisAfter(mapChrIdLowcase2ChrId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		exonNumStr = exonSplicingTest.getExonNumStr();
		cond1_SkipvsOther = pvalueCalculate.getStrInfo(false, false);
		cond2_SkipvsOther = pvalueCalculate.getStrInfo(false, true);
		cond1_Exp = pvalueCalculate.getStrInfo(true, false);
		cond2_Exp = pvalueCalculate.getStrInfo(true, true);
		pvalueAvg = pvalueCalculate.getPvalueAvg();
		pvalue = pvalueCalculate.getPvalueRootAvg();
		try {
			spliceIndex = pvalueCalculate.getSpliceIndex();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		splicingType = exonSplicingTest.getSplicingType();
	}
	
	public void setFdr(double fdr) {
		this.fdr = fdr;
	}
	public Double getPvalue() {
		return pvalue;
	}
	public double getFdr() {
		return fdr;
	}
	public SplicingAlternativeType getSplicingType() {
		return splicingType;
	}
	
	private String getLocAround() {
		StringBuilder sBuilder = new StringBuilder();
		if (lsBeforeThisAfterAlign.get(0) == null) {
			sBuilder.append("BeforeExon:null|");
		} else {
			sBuilder.append("BeforeExon:" + lsBeforeThisAfterAlign.get(0).toString() + "|");
		}
		sBuilder.append("ThisExon: " + lsBeforeThisAfterAlign.get(1).toString() + "|");
		
		if (lsBeforeThisAfterAlign.get(2) == null) {
			sBuilder.append("AfterExon:null");
		} else {
			sBuilder.append("AfterExon:" + lsBeforeThisAfterAlign.get(2).toString());
		}	
		return sBuilder.toString();
	}
	
	public String[] toStringArray() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add(accId);
		lsResult.add(alignLoc.toStringNoStrand());
		lsResult.add(getLocAround());
		lsResult.add(exonNumStr);
		lsResult.add(cond1_SkipvsOther);
		lsResult.add(cond2_SkipvsOther);
		lsResult.add(cond1_Exp);
		lsResult.add(cond2_Exp);
		lsResult.add(pvalueAvg+"");
		lsResult.add(pvalue+"");
		lsResult.add(fdr+"");
		lsResult.add(spliceIndex+"");
		lsResult.add(splicingType.toString());
		return lsResult.toArray(new String[0]);
	}
	
	public String[] toStringArray_ASD() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add(accId);
		lsResult.add(alignLoc.toStringNoStrand());
		lsResult.add(exonNumStr);
		lsResult.add(cond1_SkipvsOther);
		lsResult.add(cond2_SkipvsOther);
		lsResult.add(cond1_Exp);
		lsResult.add(cond2_Exp);
		lsResult.add(pvalueAvg+"");
		lsResult.add(pvalue+"");
		lsResult.add(fdr+"");
		lsResult.add(splicingType.toString());
		return lsResult.toArray(new String[0]);
	}
	
	/** 获得标题,我们自己用 */
	public static String[] getTitle(String condition1, String condition2) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add("ExonLocAroundSite");
		lsTitle.add("Exon_Number");
		lsTitle.add(condition1 + "_Skip::Others");
		lsTitle.add(condition2 + "_Skip::Others");
		lsTitle.add(condition1 + "Exp");
		lsTitle.add(condition2 + "Exp");
		lsTitle.add("P-Value_Average");
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add("SplicingIndex");
		lsTitle.add("SplicingType");
		return lsTitle.toArray(new String[0]);
	}
	
	/** 获得标题,对外软件 */
	public static String[] getTitle_ASD(String condition1, String condition2) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add("Exon_Number");
		lsTitle.add(condition1 + "_Skip::Others");
		lsTitle.add(condition2 + "_Skip::Others");
		lsTitle.add(condition1 + "Exp");
		lsTitle.add(condition2 + "Exp");
		lsTitle.add("P-Value_Average");
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add("SplicingType");

		return lsTitle.toArray(new String[0]);
	}
	
	/** 输入的信息会自动排序 */
	public static void sortAndFdr(List<ExonSplicingResultUnit> colExonSplicingTests, double fdrCutoff) {
		//按照pvalue从小到大排序
		Collections.sort(colExonSplicingTests, new Comparator<ExonSplicingResultUnit>() {
			public int compare(ExonSplicingResultUnit o1, ExonSplicingResultUnit o2) {
				return o1.getPvalue().compareTo(o2.getPvalue());
			}
		});
//		
		ArrayList<Double> lsPvalue = new ArrayList<Double>();
		for (ExonSplicingResultUnit exonSplicingTest : colExonSplicingTests) {
			if (exonSplicingTest.getPvalue() > fdrCutoff) {
				break;
			}
			lsPvalue.add(exonSplicingTest.getPvalue());
		}
		
		ArrayList<Double> lsFdr = MathComput.pvalue2Fdr(lsPvalue);
		int i = 0;
		for (ExonSplicingResultUnit exonSplicingTest : colExonSplicingTests) {
			if (i < lsFdr.size()) {
				exonSplicingTest.setFdr(lsFdr.get(i));
			} else {
				exonSplicingTest.setFdr(1);
			}
			i++;
		}
	}

}
