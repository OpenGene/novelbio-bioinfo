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
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;

public abstract class GeneID2LsItem {
	private static final Logger logger = Logger.getLogger(GeneID2LsItem.class);
	
	String geneUniID;
	HashSet<String> setItemID = new HashSet<String>();
	GeneID geneID;
	boolean blast;
	
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
	
	public HashSet<String> getSetItemID() {
		return setItemID;
	}
	
	public boolean isValidate() {
		if (setItemID.size() == 0 || geneUniID == null || geneUniID.equals("")) {
			return false;
		}
		return true;
	}
	/**
	 * �������� LOCOs01g11110  GO:10001, GO:10002 ����
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
	 * ����������Ƶķ����������ķ����������ڲ���ģ��
	 * ��������Gene2Item��list������Fishertest���õ�������ء�
	 * ����Ź�����
	 * @param lsGene2Item �������� gene2item��list
	 * @param lsGene2ItemBG
	 * @param BGnum
	 * @return ���û�ӱ���<br>
	 * @throws Exception 
	 */
	public static ArrayList<StatisticTestResult> getFisherResult(StatisticsTest statisticsTest, List<GeneID2LsItem> lsGene2Item,List<GeneID2LsItem> lsGene2ItemBG, int bgNum) {
		HashMultimap<String, String> mapItemID2SetGeneID = getHashItem2Gen(lsGene2Item);
		HashMultimap<String, String> mapItemID2SetGeneIDBG = getHashItem2Gen(lsGene2ItemBG);
		int numDif = lsGene2Item.size();
		int numBG = lsGene2ItemBG.size();
		ArrayList<StatisticTestResult> lsFiserInput = cope2HashForPvalue(mapItemID2SetGeneID, numDif, mapItemID2SetGeneIDBG, numBG);
		doFisherTest(statisticsTest, lsFiserInput);
		return lsFiserInput;
		
	}
	
	/**
	 * ����gene2Item��list������ת��Ϊһ��hashMap����ʽΪ
	 * Item--list-GeneID<br>
	 * �����������ͱ���������hashmap��ʱ�򣬾Ϳ��Ե���cope2HashForPvalue������pvalue
	 * @param <T>
	 * @param lsGene2Item string2 0��gene  1��item,item,item����ʽ��ע�� 1. gene�������ظ� 2.ÿ��gene�ڵ�item����Ϊ�գ��Ҳ������ظ�
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
	 * ��������HashMap��
	 * Item--list-GeneID[]
	 * һ��Ϊ��Item
	 * Item---list-GeneID[]
	 * ע��������������ܻ�����
	 * @param hashDif һ��Ϊ����Item
	 * @param NumDif
	 * @param hashAll
	 * @param NumAll
	 * @return ��������õ�list
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
	 * ����fisher��Ҫ����Ϣ�� �����鲢���fdr
	 */
	private static void doFisherTest(StatisticsTest statisticsTest, List<StatisticTestResult> lsTestResult) {
		if (statisticsTest instanceof FisherTest) {
			setFisherTestMaxSize((FisherTest) statisticsTest, lsTestResult);
		}
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			statisticTestResult.setStatisticsTest(statisticsTest, StatisticsPvalueType.RightTail);
			statisticTestResult.calculatePvalue();
		}
		
		//����
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
		//Fisher������Ҫ�趨�ĳ�ʼֵ
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
	String GOtype;
	public void setGOtype(String gOtype) {
		GOtype = gOtype;
	}
	@Override
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGenUniID();
		ArrayList<AGene2Go> lsGo = null;
		if (blast) {
			lsGo = geneID.getGene2GOBlast(GOtype);
		} else {
			lsGo = geneID.getGene2GO(GOtype);
		}
		for (AGene2Go aGene2Go : lsGo) {
			setItemID.add(aGene2Go.getGOID());
		}
	}

}

class GeneID2LsPath extends GeneID2LsItem {
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGenUniID();
		ArrayList<KGpathway> lsPath = geneID.getKegPath(blast);
		for (KGpathway kGpathway : lsPath) {
			setItemID.add(kGpathway.getPathName());
		}
	}
}

