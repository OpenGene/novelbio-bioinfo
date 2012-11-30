package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.service.servgeneanno.ServGo2Term;

public class NovelGOFunTest extends AbstFunTest{
	String GoType = Go2Term.GO_BP;
	ServGo2Term servGo2Term = new ServGo2Term();
	
	public NovelGOFunTest(ArrayList<GeneID> lsCopedIDsTest,
			ArrayList<GeneID> lsCopedIDsBG, boolean blast, String GoType) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast);
		this.GoType = GoType;
	}
	public NovelGOFunTest(boolean blast,String GoType, double evalue, int...blastTaxID) {
		this.GoType = GoType;
		setBlast(blast, evalue, blastTaxID);
	}
	
	public NovelGOFunTest() {}
	
	/**
	 * GOabs中的GOtype
	 * @param goType
	 */
	@Override
	public void setGoType(String goType) {
		this.GoType = goType;
	}
	
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
	public void setDetailType(String GOtype) {
		this.GoType = GOtype;
	}
	/**
	 * 不返回
	 */
	@Override
	public ArrayList<StatisticTestItem2GeneElimGo> getItem2GenePvalue() {
		return null;
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
