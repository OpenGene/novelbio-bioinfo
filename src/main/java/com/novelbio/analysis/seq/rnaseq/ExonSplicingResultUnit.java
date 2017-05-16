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
	public static final String PvalueArithmetic = "Pvalue-Arithmetic";
	public static final String PvalueGeometric = "Pvalue-Geometric";
	public static final String SpliceIndex = "SpliceIndex";
	public static final String ExonAround = "AroundExon";
	public static final String SplicingType = "SplicingType";
	public static final String ExonNum = "ExonNumber";

	boolean isArithmeticPvalue = true;
	
	String accId;
	Align alignLoc;
	/** 本剪接位点周边的坐标 */
	List<Align> lsBeforeThisAfterAlign;
	String exonNumStr;
	String cond1_SkipvsOther;
	String cond2_SkipvsOther;
	String cond1_Exp;
	String cond2_Exp;
	double pvalueArithmetic;
	double pvalueGeometric;
	double fdr;
	double spliceIndex;
	SplicingAlternativeType splicingType;
	
	public ExonSplicingResultUnit(ExonSplicingTest exonSplicingTest, Map<String, String> mapChrIdLowcase2ChrId, boolean isArithmeticPvalue) {
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
		pvalueArithmetic = pvalueCalculate.getPvalueAvg();
		pvalueGeometric = pvalueCalculate.getPvalueRootAvg();
		try {
			spliceIndex = pvalueCalculate.getSpliceIndex();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		splicingType = exonSplicingTest.getSplicingType();
		this.isArithmeticPvalue = isArithmeticPvalue;
	}
	
	public void setFdr(double fdr) {
		this.fdr = fdr;
	}
	public Double getPvalue() {
		return isArithmeticPvalue ? pvalueArithmetic : pvalueGeometric;
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
	
	//=============================================================
	private List<String> getLsArrayBasic() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add(accId);
		lsResult.add(alignLoc.toStringNoStrand());
		lsResult.add(exonNumStr);
		lsResult.add(cond1_SkipvsOther);
		lsResult.add(cond2_SkipvsOther);
		lsResult.add(cond1_Exp);
		lsResult.add(cond2_Exp);
		return lsResult;
	}
	private static List<String> getLsTitleBasic(String condition1, String condition2) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Location.toString());
		lsTitle.add(ExonNum);
		lsTitle.add(condition1 + "_Skip::Others");
		lsTitle.add(condition2 + "_Skip::Others");
		lsTitle.add(condition1 + "Exp");
		lsTitle.add(condition2 + "Exp");
		return lsTitle;
	}
	//==============================================================

	public String[] toStringArrayTmp() {
		List<String> lsResult = getLsArrayBasic();
		lsResult.add(pvalueArithmetic+"");
		lsResult.add(pvalueGeometric+"");
		lsResult.add(spliceIndex+"");
		lsResult.add(splicingType.toString());
		lsResult.add(getLocAround());
		return lsResult.toArray(new String[0]);
	}
	/** 获得标题,对外软件 */
	public static String[] getTitleTmp(String condition1, String condition2) {
		List<String> lsTitle = getLsTitleBasic(condition1, condition2);
		lsTitle.add(PvalueArithmetic);
		lsTitle.add(PvalueGeometric);
		lsTitle.add(SpliceIndex);
		lsTitle.add(SplicingType);
		lsTitle.add(ExonAround);
		return lsTitle.toArray(new String[0]);
	}
	
	public String[] toStringArray() {
		List<String> lsResult = getLsArrayBasic();
		lsResult.add(pvalueArithmetic+"");
		lsResult.add(pvalueGeometric+"");
		lsResult.add(fdr+"");
		lsResult.add(spliceIndex+"");
		lsResult.add(splicingType.toString());
		return lsResult.toArray(new String[0]);
	}
	/** 获得标题,我们自己用 */
	public static String[] getTitle(String condition1, String condition2) {
		List<String> lsTitle = getLsTitleBasic(condition1, condition2);
		lsTitle.add(PvalueArithmetic);
		lsTitle.add(PvalueGeometric);
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add(SpliceIndex);
		lsTitle.add(SplicingType);
		return lsTitle.toArray(new String[0]);
	}

	public String[] toStringArray_ASD() {
		List<String> lsResult = getLsArrayBasic();
		if (isArithmeticPvalue) {
			lsResult.add(pvalueArithmetic+"");
		} else {
			lsResult.add(pvalueGeometric+"");
		}
		lsResult.add(fdr+"");
		lsResult.add(splicingType.toString());
		return lsResult.toArray(new String[0]);
	}
	
	/** 获得标题,对外软件 */
	public static String[] getTitle_ASD(String condition1, String condition2) {
		List<String> lsTitle = getLsTitleBasic(condition1, condition2);
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
