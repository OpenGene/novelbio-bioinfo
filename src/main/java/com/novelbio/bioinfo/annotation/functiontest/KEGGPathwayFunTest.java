package com.novelbio.bioinfo.annotation.functiontest;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.modkegg.KeggInfo;
import com.novelbio.database.model.geneanno.GOtype;
import com.novelbio.database.model.kegg.KGpathway;

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
	
	/** 暂时没用 */
	@Override
	public void setDetailType(GOtype GOtype) {}

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
			lsGeneID2LsItem.put(strings[0].toLowerCase(), geneID2LsPath);
		}
		return lsGeneID2LsItem;
	}
	
	protected GeneID2LsItem generateGeneID2LsItem() {
		return new GeneID2LsPath();
	}
	
	@Override
	protected StatisticTestGene2Item creatStatisticTestGene2Item() {
		return new StatisticTestGene2Path();
	}

	@Override
	protected String getItemTermDB(String item) {
		if (item.contains(":")) {
			item = item.split(":")[1];
		}
		String term = null;
		KGpathway kGpathway = KeggInfo.getKGpathway(item);
		if (kGpathway != null) {
			term = kGpathway.getTitle();
		}
		return term;
	}

	@Override
	protected String getTitle() {
		return "Pathway-Analysis";
	}

	@Override
	protected TestType getTestType() {
		return TestType.Pathway;
	}



}
