package com.novelbio.analysis.annotation.functiontest;

import java.util.List;

import com.novelbio.database.model.modgeneid.GeneID;

/**
 * ElimGo 特有的GO2Gene
 * @author zong0jie
 *
 */
public class StatisticTestItem2GeneElimGo {
	StatisticTestResult statisticTestResult;
	List<GeneID> lsGeneIDs;
	
	boolean blast;
	
	public void setStatisticTestResult(StatisticTestResult statisticTestResult) {
		this.statisticTestResult = statisticTestResult;
	}
	
	/**
	 * 同一个geneUniID对应的多个不同accID的geneID
	 * @param lsGeneIDs
	 */
	public void setLsGeneIDs(List<GeneID> lsGeneIDs) {
		this.lsGeneIDs = lsGeneIDs;
	}
	
	public void setBlast(boolean blast) {
		this.blast = blast;
	}
	
	public static String[] getTitle() {
		String[] title = new String[9];
		title[0]="GOID";title[1]="GOTerm";title[2]="QueryID";title[3]="GeneSymbol";title[4]="Description";
		title[5]="P-Value";title[6]="FDR";title[7]="Enrichment";title[8]="(-log2P)";
		return title;
	}
}
