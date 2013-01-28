package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.plaf.BorderUIResource.TitledBorderUIResource;

import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.generalConf.TitleFormatNBC;

public class KEGGPathwayFunTest extends AbstFunTest{

	public KEGGPathwayFunTest(ArrayList<GeneID> lsCopedIDsTest,
			ArrayList<GeneID> lsCopedIDsBG, boolean blast) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast);
	}
	
	public KEGGPathwayFunTest(boolean blast, double evalue, int... blastTaxID) {
		setBlast(blast, evalue, blastTaxID);
	}
	
	public KEGGPathwayFunTest() {}

	@Override
	protected ArrayList<GeneID2LsItem> convert2Item(Collection<GeneID> lsGeneIDs) {
		HashSet<String> hashGenUniID = new HashSet<String>();
		ArrayList<GeneID2LsItem> lsResult = new ArrayList<GeneID2LsItem>();
		for (GeneID geneID : lsGeneIDs) {
			if (hashGenUniID.contains(geneID.getGenUniID())) {
				continue;
			}
			hashGenUniID.add(geneID.getGenUniID());
			GeneID2LsItem geneID2LsItem = new GeneID2LsPath();
			geneID2LsItem.setGeneID(geneID, blast);
			if (!geneID2LsItem.isValidate()) {
				continue;
			}
			lsResult.add(geneID2LsItem);
		}
		return lsResult;
	}
	
	/**
	 * 暂时没用
	 */
	@Override
	public void setDetailType(GOtype GOtype) {}
	/**
	 * 不返回
	 */
	@Override
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue() {
		return null;
	}

	@Override
	protected ArrayList<GeneID2LsItem> readFromBGfile(ArrayList<String[]> lsTmpGeneID2LsItem) {
		ArrayList<GeneID2LsItem> lsGeneID2LsItem = new ArrayList<GeneID2LsItem>();
		for (String[] strings : lsTmpGeneID2LsItem) {
			GeneID2LsPath geneID2LsPath = new GeneID2LsPath();
			geneID2LsPath.setGeneUniID(strings[0]);
			String[] items = strings[1].split(",");
			for (String item : items) {
				geneID2LsPath.addItemID(item);
			}
			lsGeneID2LsItem.add(geneID2LsPath);
		}
		return lsGeneID2LsItem;
	}

	@Override
	protected StatisticTestGene2Item creatStatisticTestGene2Item() {
		return new StatisticTestGene2Path();
	}

	@Override
	protected String getItemTerm(String item) {
		return KeggInfo.getKGpathway(item).getTitle();
	}

}
