package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Arrays;

import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;

public class StatisticTestGene2Path extends StatisticTestGene2Item {

	
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
			lsTmpFinalNew.add(statisticTestResult.getLog2Pnegative(logBaseNum) + "");
			lsFinal.add(lsTmpFinalNew);
		}

		return lsFinal;
	}
	
	public static String[] getTitle(boolean blast) {
		//TODO 改成list的形式更好
		String[] title;
		if (blast) {
			title = new String[8];
			title[0]=TitleFormatNBC.QueryID.toString(); title[1]="QuerySymbol";title[2]="Description";
			title[3]="Evalue"; title[4]="subjectSymbol"; title[5]="Description";
			title[6]="PathID"; title[7]="PathTerm";
		}
		else {
			title = new String[5];
			title[0]="QueryID";title[1]="QuerySymbol";title[2]="Description";
			title[3]="PathID"; title[4]="PathTerm";
		}
		return title;
	}
}
