package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Map;

import com.novelbio.database.model.modgeneid.GeneID;

public abstract class StatisticTestGene2Item {
	/** Ĭ����2Ϊ���� */
	static int logBaseNum = 2;
	
	boolean blast;
	/**
	 * key Сд
	 */
	Map<String, StatisticTestResult> mapItem2StatisticTestResult;
	GeneID geneID;
	
	/**
	 * blastʲô���Զ�Ҫ�趨���ٴ��ݽ���
	 * @param geneID
	 */
	public void setGeneID(GeneID geneID) {
		this.geneID = geneID;
	}
	/**
	 * ����ȫ����pvalue��item��Ϣ
	 * keyΪСд
	 * @param mapItem2StatisticTestResult
	 */
	public void setStatisticTestResult(Map<String, StatisticTestResult> mapItem2StatisticTestResult) {
		this.mapItem2StatisticTestResult = mapItem2StatisticTestResult;
	}
	
	protected abstract ArrayList<ArrayList<String>> getInfo();
}
