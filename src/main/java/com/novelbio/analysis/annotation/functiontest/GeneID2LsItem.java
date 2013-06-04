package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.StatisticsTest;
import com.novelbio.base.dataStructure.StatisticsTest.StatisticsPvalueType;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;

public abstract class GeneID2LsItem {
	private static final Logger logger = Logger.getLogger(GeneID2LsItem.class);
	
	String geneUniID;
	/** 内部为GO:00001 或 PATHID:00001
	 * 均为大写
	 * */
	Set<String> setItemID = new HashSet<String>();
	GeneID geneID;
	
	public abstract void setGeneID(GeneID geneID, boolean blast);
	
	public void setGeneUniID(String geneUniID) {
		if (geneUniID == null) {
			return;
		}
		this.geneUniID = geneUniID.trim();
	}
	
	public String getGeneUniID() {
		return geneUniID;
	}
	
	public void addItemID(String itemID) {
		if (itemID == null) {
			return;
		}
		setItemID.add(itemID.toUpperCase());
	}
	
	public Set<String> getSetItemID() {
		return setItemID;
	}
	/**
	 * 是否有效
	 * @return
	 */
	public boolean isValidate() {
		if (setItemID.size() == 0 || geneUniID == null || geneUniID.equals("")) {
			return false;
		}
		return true;
	}
	/**
	 * 返回类似 LOCOs01g11110  GO:10001, GO:10002 这种
	 */
	public String toString() {
		if (setItemID.size() == 0 || geneUniID == null || geneUniID.equals("")) {
			return "";
		}
		String result = geneUniID + "\t";
		int i = 0;
		for (String itemID : setItemID) {
			if (i == 0) {
				result = result + itemID;
			} else {
				result = result + "," + itemID;
			}
			i++;
		}
		return result;
	}
	
	/**
	 * 这个是最完善的方法，其他的方法都是它内部的模块
	 * 给定两个Gene2Item的list，计算Fishertest并得到结果返回。
	 * 结果排过序了
	 * @param lsGene2Item 差异基因的 gene2item的list
	 * @param lsGene2ItemBG
	 * @param BGnum
	 * @return 结果没加标题<br>
	 * @throws Exception 
	 */
	public static ArrayList<StatisticTestResult> getFisherResult(StatisticsTest statisticsTest, List<GeneID2LsItem> lsGene2Item,List<GeneID2LsItem> lsGene2ItemBG, int bgNum) {
		HashMultimap<String, String> mapItemID2SetGeneID = getHashItem2Gen(lsGene2Item);
		HashMultimap<String, String> mapItemID2SetGeneIDBG = getHashItem2Gen(lsGene2ItemBG);
		int numDif = lsGene2Item.size();
		int numBG = lsGene2ItemBG.size();
		ArrayList<StatisticTestResult> lsFiserInput = cope2HashForPvalue(mapItemID2SetGeneID, numDif, mapItemID2SetGeneIDBG, bgNum);		
		doFisherTest(statisticsTest, lsFiserInput);
		return lsFiserInput;
	}
	
	/**
	 * 给定gene2Item的list，将其转化为一个hashMap。格式为
	 * Item--list-GeneID<br>
	 * 当获得了试验和背景的两个hashmap的时候，就可以调用cope2HashForPvalue来计算pvalue
	 * @param <T>
	 * @param lsGene2Item string2 0：gene  1：item,item,item的形式，注意 1. gene不能有重复 2.每个gene内的item不能为空，且不能有重复
	 * @return
	 */
	private static HashMultimap<String, String> getHashItem2Gen(List<GeneID2LsItem> lsGene2Item) {
		HashMultimap<String, String> mapItem2SetGeneID = HashMultimap.create();
		for (GeneID2LsItem geneID2LsGO : lsGene2Item) {
			for (String itemID : geneID2LsGO.getSetItemID()) {
				mapItem2SetGeneID.put(itemID, geneID2LsGO.getGeneUniID());
			}
		}
		return mapItem2SetGeneID;
	}
	
	/**
	 * 给定两个HashMap，
	 * Item--list-GeneID[]
	 * 一个为总Item
	 * Item---list-GeneID[]
	 * 注意差异基因必须在总基因中
	 * @param hashDif 一个为差异Item
	 * @param NumDif
	 * @param hashAll
	 * @param NumAll
	 * @return 返回整理好的list
	 */
	private static ArrayList<StatisticTestResult> cope2HashForPvalue(HashMultimap<String, String> mapItemID2SetGeneID, int NumDif, HashMultimap<String, String> mapItemID2SetGeneIDBG ,int NumAll) {
		ArrayList<StatisticTestResult> lsResult=new ArrayList<StatisticTestResult>();
		
		for (String itemID : mapItemID2SetGeneID.keySet()) {
			if (itemID == null || itemID.trim().equals("")) {
				continue;
			}
			Set<String> setGeneID = mapItemID2SetGeneID.get(itemID);
			Set<String> setGeneIDBG = mapItemID2SetGeneIDBG.get(itemID);
			StatisticTestResult statisticTestResult = new StatisticTestResult(itemID);
			statisticTestResult.setDifGeneNum(setGeneID.size(), NumDif);
			statisticTestResult.setGeneNum(setGeneIDBG.size(), NumAll);
			lsResult.add(statisticTestResult);
		}
		return lsResult;
	}
	
	/**
	 * 给定fisher需要的信息， 做检验并获得fdr
	 */
	private static void doFisherTest(StatisticsTest statisticsTest, List<StatisticTestResult> lsTestResult) {
		if (statisticsTest instanceof FisherTest) {
			setFisherTestMaxSize((FisherTest) statisticsTest, lsTestResult);
		}
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			statisticTestResult.setStatisticsTest(statisticsTest, StatisticsPvalueType.RightTail);
			statisticTestResult.calculatePvalue();
		}
		
		//排序
        Collections.sort(lsTestResult,new Comparator<StatisticTestResult>(){
            public int compare(StatisticTestResult arg0, StatisticTestResult arg1) {
            	Double a = arg0.getPvalue();
            	Double b = arg1.getPvalue();
                return a.compareTo(b);
            }
        });
        StatisticTestResult.setFDR(lsTestResult);
	}
	
	private static void setFisherTestMaxSize(FisherTest statisticsTest, List<StatisticTestResult> lsGOinfo) {
		//Fisher检验需要设定的初始值
		int max = 0;
		for (StatisticTestResult statisticTestResult : lsGOinfo) {
			int tmp = statisticTestResult.getAllCountNum();
			if (tmp > max) {
				max = tmp; 
			}
		}
		statisticsTest.setMaxSize(max);
	}
}

class GeneID2LsGo extends GeneID2LsItem {
	GOtype goType;
	
	protected GeneID2LsGo() {}
	
	public void setGOtype(GOtype goType) {
		this.goType  = goType;
	}
	@Override
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGeneUniID();
		List<AGene2Go> lsGo = null;
		if (blast) {
			lsGo = geneID.getGene2GOBlast(goType );
		} else {
			lsGo = geneID.getGene2GO(goType );
		}
		for (AGene2Go aGene2Go : lsGo) {
			addItemID(aGene2Go.getGOID());
		}
	}
	
	public static GeneID2LsGo getInstance(int goLevel) {
		if (goLevel < 0) {
			return new GeneID2LsGo();
		} else {
			GeneID2LsGoLevel geneID2LsGo = new GeneID2LsGoLevel();
			geneID2LsGo.setGoLevel(goLevel);
			return geneID2LsGo;
		}
	}
}

class GeneID2LsPath extends GeneID2LsItem {
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGeneUniID();
		ArrayList<KGpathway> lsPath = geneID.getKegPath(blast);
		for (KGpathway kGpathway : lsPath) {
			addItemID("PATH:" + kGpathway.getMapNum());
		}
	}
}

