package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Go2Term;
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
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.blast = blast;
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
		if (lsInfo.size() == 0) {
			getInfo();
		}
		
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
	int goLevel = -1;
	public StatisticTestGene2GO(int goLevel) {
		this.goLevel = goLevel;
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
			if (geneID.getLsBlastInfos().size() > 0) {
				lsTmpFinal.add(geneID.getLsBlastInfos().get(0).getEvalue() + "");
				lsTmpFinal.add(geneID.getLsBlastGeneID().get(0).getSymbol());
				lsTmpFinal.add(geneID.getLsBlastGeneID().get(0).getDescription());
			} else {
				lsTmpFinal.add("");
				lsTmpFinal.add("");
				lsTmpFinal.add("");
			}
		}
		List<AGene2Go> lsGO = null;
		if (blast) {
			lsGO = geneID.getGene2GOBlast(GOtype.ALL);				
		} else {
			lsGO = geneID.getGene2GO(GOtype.ALL);
		}
		if (lsGO == null || lsGO.size() == 0) {
			return lsFinal;
		}
		for (AGene2Go aGene2Go : lsGO) {
			ArrayList<String> lsTmpFinalNew = (ArrayList<String>) lsTmpFinal.clone();
			Go2Term go2Term = aGene2Go.getGO2Term().getGOlevel(goLevel);
			String goID = "";
			if (go2Term != null) {
				goID = go2Term.getGoID();
			} else {
				continue;
			}
			if (!mapItem2StatisticTestResult.containsKey(goID.toLowerCase()) ) {
				continue;
			}
			StatisticTestResult statisticTestResult = mapItem2StatisticTestResult.get(goID.toLowerCase());
			
			lsTmpFinalNew.add(aGene2Go.getGOID());
			lsTmpFinalNew.add(aGene2Go.getGO2Term().getGoTerm());
			
//			lsTmpFinalNew.add(statisticTestResult.difGeneInItemNum + "");
//			lsTmpFinalNew.add(statisticTestResult.allDifGeneNum + "");
//			lsTmpFinalNew.add(statisticTestResult.GeneInItemIDNum + "");
//			lsTmpFinalNew.add(statisticTestResult.AllGeneNum + "");
			
			lsTmpFinalNew.add(statisticTestResult.getPvalue() + "");
			lsTmpFinalNew.add(statisticTestResult.getEnrichment() + "");
//			lsTmpFinalNew.add(statisticTestResult.getLog2Pnegative() + "");
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
		
		lsTitle.add(TitleFormatNBC.GOID.toString());
		lsTitle.add(TitleFormatNBC.GOTerm.toString());
		
//		lsTitle.add("DifGene");
//		lsTitle.add("AllDifGene");
//		lsTitle.add("GeneInGOID");
//		lsTitle.add("AllGene");
		
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.Enrichment.toString());
//		lsTitle.add(TitleFormatNBC.Log2Pnegative.toString());
		
		return lsTitle.toArray(new String[0]);
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
		if (blast && geneID.getLsBlastInfos().size() > 0) {
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
			if (!mapItem2StatisticTestResult.containsKey(("PATH:" + kGpathway.getMapNum()).toLowerCase())) {
				continue;
			}
			StatisticTestResult statisticTestResult = mapItem2StatisticTestResult.get(("PATH:" + kGpathway.getMapNum()).toLowerCase());
			
			lsTmpFinalNew.add("PATHID:" + kGpathway.getMapNum());
			lsTmpFinalNew.add(kGpathway.getTitle());
			
//			lsTmpFinalNew.add(statisticTestResult.difGeneInItemNum + "");
//			lsTmpFinalNew.add(statisticTestResult.allDifGeneNum + "");
//			lsTmpFinalNew.add(statisticTestResult.GeneInItemIDNum + "");
//			lsTmpFinalNew.add(statisticTestResult.AllGeneNum + "");
			
			lsTmpFinalNew.add(statisticTestResult.getPvalue() + "");
			lsTmpFinalNew.add(statisticTestResult.getEnrichment() + "");
//			lsTmpFinalNew.add(statisticTestResult.getLog2Pnegative() + "");
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
		
//		lsTitle.add("DifGene");
//		lsTitle.add("AllDifGene");
//		lsTitle.add("GeneInGOID");
//		lsTitle.add("AllGene");
		
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.Enrichment.toString());
//		lsTitle.add(TitleFormatNBC.Log2Pnegative.toString());
		
		return lsTitle.toArray(new String[0]);
	}
}
