package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.StatisticsTest;
import com.novelbio.base.dataStructure.StatisticsTest.StatisticsPvalueType;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 检验的结果
 */
public class StatisticTestResult {
	public static final String titlePath = "Pathway_Result";
	public static final String titleGO = "GO_Result";
	public static final String titleCOG = "COG_Result";
	
	static int logBaseNum = 2;
	
	String itemID;
	String itemTerm;
	
	int difGeneInItemNum;
	int allDifGeneNum;

	int GeneInItemIDNum;
	int AllGeneNum;

	double pvalue;
	double fdr;
	
	StatisticsPvalueType statisticsPvalueType = StatisticsPvalueType.TwoTail;
	StatisticsTest statisticsTest;
	
	
	public StatisticTestResult(String itermID) {
		this.itemID = itermID;
	}
	public void setItemTerm(String itemTerm) {
		this.itemTerm = itemTerm;
	}
	public void setStatisticsTest(StatisticsTest statisticsTest, StatisticsPvalueType statisticsPvalueType) {
		this.statisticsTest = statisticsTest;
		this.statisticsPvalueType = statisticsPvalueType;
	}
	
	public void setDifGeneNum(int difGeneInItemNum, int allDifGeneNum) {
		this.difGeneInItemNum = difGeneInItemNum;
		this.allDifGeneNum = allDifGeneNum;
	}
	
	public void setGeneNum(int geneInItemIDNum, int AllGeneNum) {
		this.GeneInItemIDNum = geneInItemIDNum;
		this.AllGeneNum = AllGeneNum;
	}
	public int getAllDifGeneNum() {
		return allDifGeneNum;
	}
	public int getAllGeneNum() {
		return AllGeneNum;
	}
	public int getDifGeneInItemNum() {
		return difGeneInItemNum;
	}
	/** 只有elimGo才用 */
	protected void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	
	/** 只有elimGo才用 */
	protected void setFdr(double fdr) {
		this.fdr = fdr;
	}
	
	public String getItemID() {
		return itemID;
	}
	public String getItemTerm() {
		return itemTerm;
	}
	public double getPvalue() {
		return pvalue;
	}
	public double getFdr() {
		return fdr;
	}
	public double getEnrichment() {
		return ((double)difGeneInItemNum/allDifGeneNum)
				/
				((double)GeneInItemIDNum/AllGeneNum);
	}
	/**
	 * 返回负logp
	 * @param num 底数譬如-log2P，或者-log10P
	 * @return
	 */
	public double getLog2Pnegative() {
		return -Math.log(pvalue)/Math.log(logBaseNum);
	}
	
	private void setFDR(double fdr) {
		this.fdr = fdr;
	}
	/**
	 * 返回四个数字的加和
	 * @return
	 */
	protected int getAllCountNum() {
		return difGeneInItemNum + allDifGeneNum + GeneInItemIDNum + AllGeneNum;
	}
	
	protected void calculatePvalue() {
		if (statisticsPvalueType == StatisticsPvalueType.TwoTail) {
			pvalue = statisticsTest.getTwoTailedP(difGeneInItemNum, allDifGeneNum, GeneInItemIDNum, AllGeneNum);
		} else if (statisticsPvalueType == StatisticsPvalueType.RightTail) {
			pvalue = statisticsTest.getRightTailedP(difGeneInItemNum, allDifGeneNum, GeneInItemIDNum, AllGeneNum);
		} else if (statisticsPvalueType == StatisticsPvalueType.LeftTail) {
			pvalue = statisticsTest.getLeftTailedP(difGeneInItemNum, allDifGeneNum, GeneInItemIDNum, AllGeneNum);
		}
	}
	
	public String[] toStringArray() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(itemID);
		lsTitle.add(itemTerm);
		lsTitle.add(difGeneInItemNum + "");
		lsTitle.add(allDifGeneNum + "");
		lsTitle.add(GeneInItemIDNum + "");
		lsTitle.add(AllGeneNum + "");
		lsTitle.add(getPvalue() + "");
		lsTitle.add(fdr + "");
		lsTitle.add(getEnrichment() + "");
		lsTitle.add(getLog2Pnegative() + "");
		return lsTitle.toArray(new String[0]);
	}
	
	/**
	 * 根据pvalue，将输入的colTestResult添加上fdr
	 * @param colTestResult
	 */
	public static void setFDR(Collection<StatisticTestResult> colTestResult) {
		ArrayList<Double> lsPvalue = new ArrayList<Double>();
		for (StatisticTestResult statisticTestResult : colTestResult) {
			lsPvalue.add(statisticTestResult.getPvalue());
		}
		int i = 0;
		//TODO fdr的计算需要重写，主要是考虑能否降低fdr的值
		ArrayList<Double> lsFDR = MathComput.pvalue2Fdr(lsPvalue);
		for (StatisticTestResult statisticTestResult : colTestResult) {
			statisticTestResult.setFDR(lsFDR.get(i));
			i++;
		}
	}
	
	public static String[] getTitleGo() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.GOID.toString());
		lsTitle.add(TitleFormatNBC.GOTerm.toString());
		lsTitle.add("DifGene");
		lsTitle.add("AllDifGene");
		lsTitle.add("GeneInGOID");
		lsTitle.add("AllGene");
		
		addTitle(lsTitle);
		return lsTitle.toArray(new String[0]);
	}
	public static String[] getTitlePath() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.PathwayID.toString());
		lsTitle.add(TitleFormatNBC.PathwayTerm.toString());
		lsTitle.add("DifGene");
		lsTitle.add("AllDifGene");
		lsTitle.add("GeneInPathwayID");
		lsTitle.add("AllGene");
		
		addTitle(lsTitle);
		return lsTitle.toArray(new String[0]);
	}
	
	public static String[] getTitleCOG() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.COGID.toString());
		lsTitle.add(TitleFormatNBC.COGTerm.toString());
		lsTitle.add("DifGene");
		lsTitle.add("AllDifGene");
		lsTitle.add("GeneInCOGID");
		lsTitle.add("AllGene");
		
		addTitle(lsTitle);
		return lsTitle.toArray(new String[0]);
	}
	
	private static void addTitle(ArrayList<String> lsTitle) {
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add(TitleFormatNBC.Enrichment.toString());
		lsTitle.add(TitleFormatNBC.Log2Pnegative.toString());		
	}
	
	/**
	 * 将lsStatisticTestResults转化为可以写入excel的形式
	 * @param go true：用go的title false：用pathway的title
	 * @param lsStatisticTestResults
	 */
	public static List<String[]> getLsInfo(TestType testType, List<StatisticTestResult> lsStatisticTestResults) {
		if (lsStatisticTestResults == null || lsStatisticTestResults.size() == 0) {
			return new ArrayList<String[]>();
		}
		List<String[]> lsResult = new ArrayList<String[]>();
		if (testType == TestType.GO) {
			lsResult.add(StatisticTestResult.getTitleGo());
		} else if(testType == TestType.Pathway) {
			lsResult.add(StatisticTestResult.getTitlePath());
		} else if (testType == testType.COG) {
			lsResult.add(StatisticTestResult.getTitleCOG());
		}
		
		for (StatisticTestResult statisticTestResult : lsStatisticTestResults) {
			lsResult.add(statisticTestResult.toStringArray());
		}
		return lsResult;
	}

}

