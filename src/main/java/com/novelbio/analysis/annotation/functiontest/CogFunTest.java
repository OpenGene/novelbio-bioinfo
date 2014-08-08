package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.annotation.cog.COGanno;
import com.novelbio.analysis.annotation.cog.CogInfo;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modkegg.KeggInfo;

public class CogFunTest extends FunctionTest {
	COGanno cogAnno = new COGanno();
	
	/** Cog不需要blast到其他物种，本身他就是blast到Cog数据库的 */
	@Deprecated
	public void setBlastInfo(double evalue, List<Integer> lsBlastTaxId) {}
	/** 返回一个空的list */
	public List<Integer> getBlastTaxID() {
		return new ArrayList<>();
	}
	public boolean isBlast() {
		return false;
	}
	
	@Override
	protected Map<String, GeneID2LsItem> readFromBGfile(
			Collection<String[]> lsTmpGeneID2LsItem) {
		Map<String, GeneID2LsItem> lsGeneID2LsItem = new LinkedHashMap<String, GeneID2LsItem>();
		for (String[] strings : lsTmpGeneID2LsItem) {
			GeneID2LsCog geneID2LsCog = new GeneID2LsCog();
			geneID2LsCog.setGeneUniID(strings[0]);
			String[] items = strings[1].split(",");
			for (String item : items) {
				geneID2LsCog.addItemID(item);
			}
			lsGeneID2LsItem.put(strings[0].toLowerCase(), geneID2LsCog);
		}
		return lsGeneID2LsItem;
	}

	@Override
	protected GeneID2LsItem convert2Item(GeneID geneID) {
		GeneID2LsCog geneId2LsCog = new GeneID2LsCog();
		geneId2LsCog.setGeneID(geneID, isBlast());
		CogInfo cogInfo = cogAnno.getCogInfoFromGeneUniId(geneID.getGeneUniID());
		geneId2LsCog.setCogInfo(cogInfo);
		if (!geneId2LsCog.isValidate()) {
			return null;
		}
		return geneId2LsCog;
	}

	@Override
	protected StatisticTestGene2Item creatStatisticTestGene2Item() {
		return new StatisticTestGene2Cog();
	}

	@Override
	protected String getItemTerm(String item) {
		if (item.contains(":")) {
			item = item.split(":")[1];
		}
		cogAnno.getCogInfoFromCogId(item);
		return KeggInfo.getKGpathway(item).getTitle();
	}

	@Override
	public void setDetailType(GOtype gotype) { }

	@Override
	public Map<String, List<String[]>> getMapWriteToExcel() {
		Map<String, List<String[]>> mapResult = new LinkedHashMap<String, List<String[]>>();
		List<String[]> lsStatisticTestResults = StatisticTestResult.getLsInfo(false, getTestResult());
		if (lsStatisticTestResults.size() == 0) {
			return new HashMap<>();
		}
		mapResult.put(StatisticTestResult.titleCOG, lsStatisticTestResults);
		List<String[]> lsGene2PathPvalue = StatisticTestGene2Item.getLsInfo(getGene2ItemPvalue());
		mapResult.put(StatisticTestGene2Item.titleCOG, lsGene2PathPvalue);
		return mapResult;
	}

	@Override
	protected String getTitle() {
		return "COG-Analysis";
	}

}
