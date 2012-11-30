package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * ElimGo ���е�GO2Gene
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
	 * ͬһ��geneUniID��Ӧ�Ķ����ͬaccID��geneID
	 * @param lsGeneIDs
	 */
	public void setLsGeneIDs(List<GeneID> lsGeneIDs) {
		this.lsGeneIDs = lsGeneIDs;
	}
	
	public void setBlast(boolean blast) {
		this.blast = blast;
	}
	
	public ArrayList<String[]> toStringsLs() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (GeneID geneID : lsGeneIDs) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(statisticTestResult.getItemName());
			lsTmpResult.add(statisticTestResult.getItemTerm());
			lsTmpResult.add(geneID.getAccID());
			lsTmpResult.add(geneID.getSymbol());
			lsTmpResult.add(geneID.getDescription());
			lsTmpResult.add(statisticTestResult.getPvalue() + "");
			lsTmpResult.add(statisticTestResult.getFdr() + "");
			lsTmpResult.add(statisticTestResult.getEnrichment() + "");
			lsTmpResult.add(statisticTestResult.getLog2Pnegative() + "");
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		
		return lsResult;
	}
	
	public static String[] getTitle() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.GOID.toString());
		lsTitle.add(TitleFormatNBC.GOTerm.toString());
		lsTitle.add(TitleFormatNBC.QueryID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add(TitleFormatNBC.Enrichment.toString());
		lsTitle.add(TitleFormatNBC.Log2Pnegative.toString());
		
		return lsTitle.toArray(new String[0]);
	}
}
