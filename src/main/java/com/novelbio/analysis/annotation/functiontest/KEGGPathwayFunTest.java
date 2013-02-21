package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.plaf.BorderUIResource.TitledBorderUIResource;

import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.generalConf.TitleFormatNBC;

public class KEGGPathwayFunTest extends FunctionTest {

	@Override
	protected GeneID2LsItem convert2Item(GeneID geneID) {
		GeneID2LsItem geneID2LsItem = new GeneID2LsPath();
		geneID2LsItem.setGeneID(geneID, isBlast());
		if (!geneID2LsItem.isValidate()) {
			return null;
		}
		return geneID2LsItem;
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
	protected Map<String, GeneID2LsItem> readFromBGfile(Collection<String[]> lsTmpGeneID2LsItem) {
		Map<String, GeneID2LsItem> lsGeneID2LsItem = new LinkedHashMap<String, GeneID2LsItem>();
		for (String[] strings : lsTmpGeneID2LsItem) {
			GeneID2LsPath geneID2LsPath = new GeneID2LsPath();
			geneID2LsPath.setGeneUniID(strings[0]);
			String[] items = strings[1].split(",");
			for (String item : items) {
				geneID2LsPath.addItemID(item);
			}
			lsGeneID2LsItem.put(strings[0], geneID2LsPath);
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
