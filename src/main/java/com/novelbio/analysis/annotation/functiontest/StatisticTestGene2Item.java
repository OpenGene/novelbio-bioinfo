package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Map;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;

public abstract class StatisticTestGene2Item {
	
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
	
	public ArrayList<String[]> toStringLs() {
		ArrayList<ArrayList<String>> lsInfo = getInfo();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (ArrayList<String> lsTmpInfo : lsInfo) {
			String[] tmp = lsTmpInfo.toArray(new String[1]);
			lsResult.add(tmp);
		}
		return lsResult;
	}
	
	public abstract String[] getTitle();
}

class StatisticTestGene2GO extends StatisticTestGene2Item {
	String GOtype;
	public void setGOtype(String gOtype) {
		this.GOtype = gOtype;
	}
	
	@Override
	protected ArrayList<ArrayList<String>> getInfo() {
		ArrayList<ArrayList<String>> lsFinal = new ArrayList<ArrayList<String>>();
		ArrayList<String> lsTmpFinal = new ArrayList<String>();

		// GO前面的常规信息的填充,Symbol和description等
		lsTmpFinal.add(geneID.getAccID());
		lsTmpFinal.add(geneID.getSymbol());
		lsTmpFinal.add(geneID.getDescription());
		if (blast) {
			lsTmpFinal.add(geneID.getLsBlastInfos().get(0).getEvalue() + "");
			lsTmpFinal.add(geneID.getLsBlastGeneID().get(0).getSymbol());
			lsTmpFinal.add(geneID.getLsBlastGeneID().get(0).getDescription());
		}
		ArrayList<AGene2Go> lsGO = null;
		if (blast) {
			lsGO = geneID.getGene2GOBlast(GOtype);				
		} else {
			lsGO = geneID.getGene2GO(GOtype);
		}
		if (lsGO == null || lsGO.size() == 0) {
			return lsFinal;
		}
		for (AGene2Go aGene2Go : lsGO) {
			ArrayList<String> lsTmpFinalNew = (ArrayList<String>) lsTmpFinal.clone();
			if (!mapItem2StatisticTestResult.containsKey(aGene2Go.getGOID().toLowerCase())) {
				continue;
			}
			StatisticTestResult statisticTestResult = mapItem2StatisticTestResult.get(aGene2Go.getGOID().toLowerCase());
			
			lsTmpFinalNew.add(aGene2Go.getGOID());
			lsTmpFinalNew.add(aGene2Go.getGOTerm());
			
			lsTmpFinalNew.add(statisticTestResult.difGeneInItemNum + "");
			lsTmpFinalNew.add(statisticTestResult.allDifGeneNum + "");
			lsTmpFinalNew.add(statisticTestResult.GeneInItemIDNum + "");
			lsTmpFinalNew.add(statisticTestResult.AllGeneNum + "");
			lsTmpFinalNew.add(statisticTestResult.getPvalue() + "");
			lsTmpFinalNew.add(statisticTestResult.getEnrichment() + "");
			lsTmpFinalNew.add(statisticTestResult.getLog2Pnegative() + "");
			lsFinal.add(lsTmpFinalNew);
		}

		return lsFinal;
	}
	
	public String[] getTitle() {
		//TODO 改成list的形式更好
		String[] title;
		if (blast) {
			title = new String[9];
			title[0]="QueryID";title[1]="QuerySymbol";title[2]="Description";
			title[3]="Evalue"; title[4]="subjectSymbol"; title[5]="Description";
			title[6]="GOID"; title[7]="GOTerm"; title[8]="Evidence";
		}
		else {
			title = new String[6];
			title[0]="QueryID";title[1]="QuerySymbol";title[2]="Description";
			title[3]="GOID"; title[4]="GOTerm"; title[5]="Evidence";
		}
		return title;
	}
}


class StatisticTestGene2Path extends StatisticTestGene2Item {

	
	protected ArrayList<ArrayList<String>> getInfo() {
		ArrayList<ArrayList<String>> lsFinal = new ArrayList<ArrayList<String>>();
		ArrayList<String> lsTmpFinal = new ArrayList<String>();

		// GO前面的常规信息的填充,Symbol和description等
		lsTmpFinal.add(geneID.getAccID());
		lsTmpFinal.add(geneID.getSymbol());
		lsTmpFinal.add(geneID.getDescription());
		if (blast) {
			lsTmpFinal.add(geneID.getLsBlastInfos().get(0).getEvalue() + "");
			lsTmpFinal.add(geneID.getLsBlastGeneID().get(0).getSymbol());
			lsTmpFinal.add(geneID.getLsBlastGeneID().get(0).getDescription());
		}

		ArrayList<KGpathway> lsPath = geneID.getKegPath(blast);
		if (lsPath == null || lsPath.size() == 0) {
			return lsFinal;
		}
		for (KGpathway kGpathway : lsPath) {
			ArrayList<String> lsTmpFinalNew = (ArrayList<String>) lsTmpFinal.clone();
			if (!mapItem2StatisticTestResult.containsKey(kGpathway.getPathName().toLowerCase())) {
				continue;
			}
			StatisticTestResult statisticTestResult = mapItem2StatisticTestResult.get(kGpathway.getPathName().toLowerCase());
			
			lsTmpFinalNew.add(kGpathway.getTitle());
			lsTmpFinalNew.add(kGpathway.getPathName());
			
			lsTmpFinalNew.add(statisticTestResult.difGeneInItemNum + "");
			lsTmpFinalNew.add(statisticTestResult.allDifGeneNum + "");
			lsTmpFinalNew.add(statisticTestResult.GeneInItemIDNum + "");
			lsTmpFinalNew.add(statisticTestResult.AllGeneNum + "");
			lsTmpFinalNew.add(statisticTestResult.getPvalue() + "");
			lsTmpFinalNew.add(statisticTestResult.getEnrichment() + "");
			lsTmpFinalNew.add(statisticTestResult.getLog2Pnegative() + "");
			lsFinal.add(lsTmpFinalNew);
		}

		return lsFinal;
	}
	
	public String[] getTitle() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.QueryID.toString());
		lsTitle.add("QuerySymbol");
		lsTitle.add(TitleFormatNBC.Description.toString());
		
		if (blast) {
			lsTitle.add(TitleFormatNBC.Evalue.toString());
			lsTitle.add("SubjectSymbol");
			lsTitle.add(TitleFormatNBC.Description.toString());
		}
		
		lsTitle.add(TitleFormatNBC.PathwayID.toString());
		lsTitle.add(TitleFormatNBC.PathwayTerm.toString());
		
		return lsTitle.toArray(new String[0]);
	}
}