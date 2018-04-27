package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.geneanno.GOtype;
import com.novelbio.database.model.geneanno.Go2Term;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;

public class NovelGOFunTest extends FunctionTest {
	private static final Logger logger = LoggerFactory.getLogger(NovelGOFunTest.class); 
	GOtype GoType = GOtype.BP;
	ManageGo2Term servGo2Term = ManageGo2Term.getInstance();
	
	boolean isCombineDB;
	HashMultimap<String, String> mapGene2LsItem;
	/**
	 * 如果大于0，则做层级GO
	 */
	int GOlevel = -1;
	
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

	/** 读取gene2Item的文件，用来增加注释
	 * 第一列是geneName，第二列是goId */
	public void readGene2ItemAnnoFile(String goAnnoFile) {
		if (mapBGGeneID2Items == null) mapBGGeneID2Items = new HashMap<>();
		
		TxtReadandWrite txtRead = new TxtReadandWrite(goAnnoFile);
		ArrayListMultimap<String, String> mapGeneName2LsGO = ArrayListMultimap.create();
		int i = 0;
		for (String content : txtRead.readlines()) {
			i++;
			
			if (content.startsWith("#")) continue;
			String[] ss = content.split("\t");
			//判断第一行是否为标题
			if (i == 1 && !ss[1].contains(":")) {
				continue;
			}
			mapGeneName2LsGO.put(ss[0], ss[1]);
		}
		txtRead.close();
		
		int allBGgeneNum = mapGeneName2LsGO.keySet().size();
		int m = 0;
		for (String geneId : mapGeneName2LsGO.keySet()) {
			List<String> lsItemId = mapGeneName2LsGO.get(geneId);
			List<String> lsItemIdReal = new ArrayList<>();
			for (String goId : lsItemId) {
				Go2Term go2Term = servGo2Term.queryGo2Term(goId);
				if (go2Term == null || go2Term.getGOtype() != GoType) {
					continue;
				}
				lsItemIdReal.add(goId);
			}
			
			if (lsItemIdReal.isEmpty()) continue;
			
			String geneUniId = geneId;
			GeneID2LsItem geneID2LsItem = mapBGGeneID2Items.get(geneUniId.toLowerCase());
			if (geneID2LsItem == null) {
				GeneID geneID = new GeneID(geneId, taxID);
				geneUniId = geneID.getGeneUniID();
				geneID2LsItem = mapBGGeneID2Items.get(geneUniId.toLowerCase());
			}
	
			
			if (geneID2LsItem == null) {
				geneID2LsItem = generateGeneID2LsItem();
				geneID2LsItem.setGeneUniID(geneUniId);
				mapBGGeneID2Items.put(geneUniId.toLowerCase(), geneID2LsItem);
			}
			for (String itemId : lsItemIdReal) {
				geneID2LsItem.addItemID(itemId);
			}
			
			if (m++%1000 == 0) {
				logger.info("All Gene Num is {}, run {}", allBGgeneNum, m );
			}
		}
		
		BGnum = mapBGGeneID2Items.size();
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
	
	protected GeneID2LsItem generateGeneID2LsItem() {
		return GeneID2LsGo.getInstance(GOlevel);
	}
	
	@Override
	protected StatisticTestGene2Item creatStatisticTestGene2Item() {
		return new StatisticTestGene2GO(GOlevel);
	}
	
	@Override
	protected String getItemTermDB(String item) {
		Go2Term go2Term = servGo2Term.queryGo2Term(item);
		if (go2Term == null) {
			return null;
		}
		return go2Term.getGoTerm();	
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
