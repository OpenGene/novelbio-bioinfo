package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.omg.CosNaming._BindingIteratorImplBase;

import com.google.common.collect.HashMultimap;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.StatisticsTest;
import com.novelbio.base.dataStructure.StatisticsTest.StatisticsPvalueType;

/**
 * 检验的结果
 * 
 * @author zong0jie 0:itemID <br>
 *         1到n:item信息 <br>
 *         n+1:difGene <br>
 *         n+2:AllDifGene<br>
 *         n+3:GeneInGoID <br>
 *         n+4:AllGene <br>
 *         n+5:Pvalue<br>
 *         n+6:FDR <br>
 *         n+7:enrichment n+8:(-log2P) <br>
 */
public class StatisticTestResult {
	String itemName;
	String itemTerm;
	
	int difGeneInItemNum;
	int allDifGeneNum;

	int GeneInItemIDNum;
	int AllGeneNum;

	double pvalue;
	double fdr;
	
	StatisticsPvalueType statisticsPvalueType = StatisticsPvalueType.TwoTail;
	StatisticsTest statisticsTest;
	
	public StatisticTestResult(String itemName) {
		this.itemName = itemName;
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
		this.AllGeneNum = allDifGeneNum;
	}
	
	/** 只有elimGo才用 */
	protected void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	
	/** 只有elimGo才用 */
	protected void setFdr(double fdr) {
		this.fdr = fdr;
	}
	
	public String getItemName() {
		return itemName;
	}
	
	public double getPvalue() {
		return pvalue;
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
	public double getLog2Pnegative(int num) {
		return -Math.log(pvalue)/Math.log(num);
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
		ArrayList<Double> lsFDR = MathComput.pvalue2Fdr(lsPvalue);
		for (StatisticTestResult statisticTestResult : colTestResult) {
			statisticTestResult.setFDR(lsFDR.get(i));
			i++;
		}
	}
}
