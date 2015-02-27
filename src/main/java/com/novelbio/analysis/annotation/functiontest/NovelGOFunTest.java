package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;

public class NovelGOFunTest extends FunctionTest {
	private static final Logger logger = Logger.getLogger(NovelGOFunTest.class); 
	GOtype GoType = GOtype.BP;
	ManageGo2Term servGo2Term = ManageGo2Term.getInstance();
	
	boolean isCombineDB;
	HashMultimap<String, String> mapGene2LsItem;
	/**
	 * 如果大于0，则做层级GO
	 */
	int GOlevel = -1;
	
	public void setGoType(GOtype goType) {
		GoType = goType;
	}
	
	/** 设定自己的GO注释文件
	 * @param goAnnoFile GO注释文件，第一列为GeneName，第二列为GOIterm
	 * @param isCombineDB 是否与数据库已有的数据进行合并
	 */
	public void setGoAnnoFile(String goAnnoFile, boolean isCombineDB) {
		mapGene2LsItem = GeneID2LsGo.readGoTxtFile(goAnnoFile);
		this.isCombineDB = isCombineDB;
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
		geneID2LsItem.setMapGene2LsItem(mapGene2LsItem, isCombineDB);
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
			mapGeneID2LsItem.put(strings[0].toLowerCase(), geneID2LsGo);
		}
		return mapGeneID2LsItem;
	}
	
	@Override
	protected StatisticTestGene2Item creatStatisticTestGene2Item() {
		return new StatisticTestGene2GO(GOlevel);
	}
	
	@Override
	protected String getItemTerm(String item) {
		return servGo2Term.queryGo2Term(item).getGoTerm();	
	}
	
	@Override
	protected String getTitle() {
		return "GO-Analysis_" + GoType.getTwoWord();
	}
	
	@Override
	protected TestType getTestType() {
		return TestType.GO;
	}

}
