package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.service.servgeneanno.ServGo2Term;

public class NovelGOFunTest extends AbstFunTest{
	GOtype GoType = GOtype.BP;
	ServGo2Term servGo2Term = new ServGo2Term();
	
	public NovelGOFunTest(ArrayList<GeneID> lsCopedIDsTest,
			ArrayList<GeneID> lsCopedIDsBG, boolean blast, GOtype GoType) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast);
		this.GoType = GoType;
	}
	public NovelGOFunTest(boolean blast,GOtype GoType, double evalue, int...blastTaxID) {
		this.GoType = GoType;
		setBlast(blast, evalue, blastTaxID);
	}
	
	public NovelGOFunTest() {}

	
	@Override
	protected ArrayList<GeneID2LsItem> convert2Item(Collection<GeneID> lsGeneIDs) {
		HashSet<String> hashGenUniID = new HashSet<String>();
		ArrayList<GeneID2LsItem> lsResult = new ArrayList<GeneID2LsItem>();
		for (GeneID geneID : lsGeneIDs) {
			if (hashGenUniID.contains(geneID.getGenUniID())) {
				continue;
			}
			hashGenUniID.add(geneID.getGenUniID());
			GeneID2LsGo geneID2LsItem = new GeneID2LsGo();
			geneID2LsItem.setGOtype(GoType);
			geneID2LsItem.setGeneID(geneID, blast);
			if (!geneID2LsItem.isValidate()) {
				continue;
			}
			lsResult.add(geneID2LsItem);
		}
		return lsResult;
	}

	@Override
	public void setDetailType(GOtype gotype) {
		this.GoType = gotype;
	}
	/**
	 * 不返回
	 */
	@Override
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue() {
		ArrayList<StatisticTestResult> lsTestResult = getTestResult();
		ArrayList<StatisticTestItem2Gene> lStatisticTestItem2Gene = new ArrayList<StatisticTestItem2Gene>();
		
		ArrayListMultimap<String, GeneID> hashGo2LsGene = getGo2GeneUniID();
		
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			List<GeneID> lsTmpGeneUniID = hashGo2LsGene.get(statisticTestResult.getItemName());
			ArrayList<GeneID> lsFinalGeneIDs = new ArrayList<GeneID>();
			for (GeneID geneID : lsTmpGeneUniID) {
				//同一个geneUniID对应的不同accID
				List<GeneID> lscopedIDs = mapGeneUniID2LsGeneID.get(geneID.getGenUniID());
				lsFinalGeneIDs.addAll(lscopedIDs);
			}
			
			StatisticTestItem2Gene statisticTestItem2Gene = new StatisticTestItem2Gene();
			statisticTestItem2Gene.setStatisticTestResult(statisticTestResult);
			statisticTestItem2Gene.setLsGeneIDs(lsFinalGeneIDs);
			statisticTestItem2Gene.setBlast(blast);
			lStatisticTestItem2Gene.add(statisticTestItem2Gene);
		}
		return lStatisticTestItem2Gene;
	}
	private ArrayListMultimap<String, GeneID> getGo2GeneUniID() {
		ArrayListMultimap<String, GeneID> hashGo2LsGene = ArrayListMultimap.create();
		ArrayList<StatisticTestGene2Item> lsStatisticTestGene2Items = getGene2ItemPvalue();
		for (StatisticTestGene2Item statisticTestGene2Item : lsStatisticTestGene2Items) {
			GeneID2LsGo geneID2LsItem = new GeneID2LsGo();
			geneID2LsItem.setGOtype(GoType);
			geneID2LsItem.setGeneID(statisticTestGene2Item.geneID, blast);
			
			for (String goid : geneID2LsItem.getSetItemID()) {
				hashGo2LsGene.put(goid, statisticTestGene2Item.geneID);
			}
		}
		return hashGo2LsGene;
	}
	@Override
	protected ArrayList<GeneID2LsItem> readFromBGfile(ArrayList<String[]> lsTmpGeneID2LsItem) {
		ArrayList<GeneID2LsItem> lsGeneID2LsItem = new ArrayList<GeneID2LsItem>();
		for (String[] strings : lsTmpGeneID2LsItem) {
			GeneID2LsGo geneID2LsGo = new GeneID2LsGo();
			geneID2LsGo.setGeneUniID(strings[0]);
			String[] items = strings[1].split(",");
			for (String item : items) {
				geneID2LsGo.addItemID(item);
			}
			lsGeneID2LsItem.add(geneID2LsGo);
		}
		return lsGeneID2LsItem;
	
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
