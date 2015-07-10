package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * ElimGo 特有的GO2Gene
 * @author zong0jie
 *
 */
public class StatisticTestItem2Gene {
	
	public static String getTitle(TestType testType) {
		return testType + "2Gene";
	}
	
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
	
	public ArrayList<String[]> toStringsLs() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		HashSet<String> setGeneAccID = new HashSet<String>();
		for (GeneID geneID : lsGeneIDs) {
			if (setGeneAccID.contains(geneID.getAccID())) {
				continue;
			}
			setGeneAccID.add(geneID.getAccID());
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(statisticTestResult.getItemID());
			lsTmpResult.add(statisticTestResult.getItemTerm());
			lsTmpResult.add(geneID.getAccID());
			lsTmpResult.add(geneID.getSymbol());
			lsTmpResult.add(geneID.getDescription());
			lsTmpResult.add(statisticTestResult.getPvalue() + "");
			lsTmpResult.add(statisticTestResult.getFdr() + "");
			lsTmpResult.add(statisticTestResult.getEnrichment() + "");
//			lsTmpResult.add(statisticTestResult.getLog2Pnegative() + "");
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		
		return lsResult;
	}
	
	public static String[] getTitleGO() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.GOID.toString());
		lsTitle.add(TitleFormatNBC.GOTerm.toString());
		lsTitle.add(TitleFormatNBC.QueryID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add(TitleFormatNBC.Enrichment.toString());
//		lsTitle.add(TitleFormatNBC.Log2Pnegative.toString());
		
		return lsTitle.toArray(new String[0]);
	}
	
	public static String[] getTitlePath() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.PathwayID.toString());
		lsTitle.add(TitleFormatNBC.PathwayTerm.toString());
		lsTitle.add(TitleFormatNBC.QueryID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add(TitleFormatNBC.Enrichment.toString());
//		lsTitle.add(TitleFormatNBC.Log2Pnegative.toString());
		
		return lsTitle.toArray(new String[0]);
	}
	
	public static String[] getTitleCOG() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.COGID.toString());
		lsTitle.add(TitleFormatNBC.COGTerm.toString());
		lsTitle.add(TitleFormatNBC.QueryID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add(TitleFormatNBC.Enrichment.toString());
//		lsTitle.add(TitleFormatNBC.Log2Pnegative.toString());
		
		return lsTitle.toArray(new String[0]);
	}
	
	/**
	 * @param go 是否为GO，true： 采用GO的title， false：采用pathway的title
	 * @param lsItem2Gene
	 * @return
	 */
	public static List<String[]> getLsInfo(TestType testType, List<StatisticTestItem2Gene> lsItem2Gene) {
		if (lsItem2Gene == null || lsItem2Gene.size() == 0) {
			return new ArrayList<String[]>();
		}
		List<String[]> lsGo2GeneResult = new ArrayList<String[]>();
		if (testType == TestType.GO) {
			lsGo2GeneResult.add(StatisticTestItem2Gene.getTitleGO());
		} else if(testType == TestType.Pathway) {
			lsGo2GeneResult.add(StatisticTestItem2Gene.getTitlePath());
		} else if (testType == TestType.COG) {
			lsGo2GeneResult.add(StatisticTestItem2Gene.getTitleCOG());
		}
	
		for (StatisticTestItem2Gene statisticTestItem2GeneElimGo : lsItem2Gene) {
			lsGo2GeneResult.addAll(statisticTestItem2GeneElimGo.toStringsLs());
		}
		return lsGo2GeneResult;
	}
	
	/**
	 * @param go 是否为GO，true： 采用GO的title， false：采用pathway的title
	 * @param lsItem2Gene
	 * @return
	 */
	public static int getiSigNum(List<StatisticTestItem2Gene> lsItem2Gene, double pvalue) {
		if (lsItem2Gene == null || lsItem2Gene.size() == 0) {
			return 0;
		}
		int num = 0;
		for (StatisticTestItem2Gene statisticTestItem2GeneElimGo : lsItem2Gene) {
			if (statisticTestItem2GeneElimGo.statisticTestResult.getPvalue() <= pvalue) {
				num += statisticTestItem2GeneElimGo.toStringsLs().size();
			}
		}
		return num;
	}
}
