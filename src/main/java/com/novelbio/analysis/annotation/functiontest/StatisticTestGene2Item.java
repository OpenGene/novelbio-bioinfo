package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Map;

import com.novelbio.database.model.modgeneid.GeneID;

public abstract class StatisticTestGene2Item {
	/** 默认以2为底数 */
	static int logBaseNum = 2;
	
	boolean blast;
	/**
	 * key 小写
	 */
	Map<String, StatisticTestResult> mapItem2StatisticTestResult;
	GeneID geneID;
	
	/**
	 * blast什么属性都要设定好再传递进来
	 * @param geneID
	 */
	public void setGeneID(GeneID geneID) {
		this.geneID = geneID;
	}
	/**
	 * 输入全体有pvalue的item信息
	 * key为小写
	 * @param mapItem2StatisticTestResult
	 */
	public void setStatisticTestResult(Map<String, StatisticTestResult> mapItem2StatisticTestResult) {
		this.mapItem2StatisticTestResult = mapItem2StatisticTestResult;
	}
	
	protected abstract ArrayList<ArrayList<String>> getInfo();
}
