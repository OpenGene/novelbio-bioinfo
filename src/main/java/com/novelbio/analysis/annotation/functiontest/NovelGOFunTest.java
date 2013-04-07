package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;

public class NovelGOFunTest extends FunctionTest {
	private static final Logger logger = Logger.getLogger(NovelGOFunTest.class); 
	GOtype GoType = GOtype.BP;
	ManageGo2Term servGo2Term = new ManageGo2Term();
	
	/**
	 * 如果大于0，则做层级GO
	 */
	int GOlevel = -1;
	
	public void setGoType(GOtype goType) {
		GoType = goType;
	}
	/**
	 * 如果大于0，则做层级GO
	 * 默认-1，不做层级GO
	 */
	public void setGOlevel(int gOlevel) {
		GOlevel = gOlevel;
	}
	@Override
	protected GeneID2LsItem convert2Item(GeneID geneID) {
		GeneID2LsGo geneID2LsItem = GeneID2LsGo.getInstance(GOlevel);
		geneID2LsItem.setGOtype(GoType);
		geneID2LsItem.setGeneID(geneID, isBlast());
		if (!geneID2LsItem.isValidate()) {
			return null;
		}
		return geneID2LsItem;
	}

	@Override
	public void setDetailType(GOtype gotype) {
		this.GoType = gotype;
	}
 
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue() {
		ArrayList<StatisticTestResult> lsTestResult = getTestResult();
		ArrayList<StatisticTestItem2Gene> lStatisticTestItem2Gene = new ArrayList<StatisticTestItem2Gene>();
		
		ArrayListMultimap<String, GeneID> hashGo2LsGene = getGo2GeneUniID();
		
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			List<GeneID> lsTmpGeneUniID = hashGo2LsGene.get(statisticTestResult.getItemName());
			ArrayList<GeneID> lsFinalGeneIDs = new ArrayList<GeneID>();
			for (GeneID geneID : lsTmpGeneUniID) {
				//同一个geneUniID对应的不同accID
				List<GeneID> lscopedIDs = mapGeneUniID2LsGeneID.get(geneID.getGeneUniID());
				lsFinalGeneIDs.addAll(lscopedIDs);
			}
			
			StatisticTestItem2Gene statisticTestItem2Gene = new StatisticTestItem2Gene();
			statisticTestItem2Gene.setStatisticTestResult(statisticTestResult);
			statisticTestItem2Gene.setLsGeneIDs(lsFinalGeneIDs);
			statisticTestItem2Gene.setBlast(isBlast());
			lStatisticTestItem2Gene.add(statisticTestItem2Gene);
		}
		return lStatisticTestItem2Gene;
	}
	
	private ArrayListMultimap<String, GeneID> getGo2GeneUniID() {
		ArrayListMultimap<String, GeneID> hashGo2LsGene = ArrayListMultimap.create();
		ArrayList<StatisticTestGene2Item> lsStatisticTestGene2Items = getGene2ItemPvalue();
		for (StatisticTestGene2Item statisticTestGene2Item : lsStatisticTestGene2Items) {
			GeneID2LsItem geneID2LsItem = convert2ItemFromBG(statisticTestGene2Item.geneID, false);
			if (geneID2LsItem == null) {
				continue;
			}
			for (String goid : geneID2LsItem.getSetItemID()) {
				hashGo2LsGene.put(goid, statisticTestGene2Item.geneID);
			}
		}
		return hashGo2LsGene;
	}
	
	@Override
	protected Map<String, GeneID2LsItem> readFromBGfile(Collection<String[]> lsTmpGeneID2LsItem) {
		Map<String, GeneID2LsItem> mapGeneID2LsItem = new LinkedHashMap<String, GeneID2LsItem>();
		for (String[] strings : lsTmpGeneID2LsItem) {
			GeneID2LsGo geneID2LsGo = GeneID2LsGo.getInstance(GOlevel);
			geneID2LsGo.setGeneUniID(strings[0]);
			String[] items = strings[1].split(",");
			for (String item : items) {
				geneID2LsGo.addItemID(item);
			}
			mapGeneID2LsItem.put(strings[0], geneID2LsGo);
		}
		return mapGeneID2LsItem;
	}
	
	@Override
	protected StatisticTestGene2Item creatStatisticTestGene2Item() {
		return new StatisticTestGene2GO();
	}
	@Override
	protected String getItemTerm(String item) {
		return servGo2Term.queryGo2Term(item).getGoTerm();
	}

}
