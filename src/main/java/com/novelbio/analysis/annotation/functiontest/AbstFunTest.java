package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.parser.node.PutExecutor;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.StatisticsTest;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;

public abstract class AbstFunTest implements FunTestInt{
	private static final Logger logger = Logger.getLogger(AbstFunTest.class);
	
	public static final String TEST_GO = "go";
	public static final String TEST_KEGGPATH = "KEGGpathway";
	
	int taxID = 0;
	boolean blast = false;
	int[] blastTaxID = null;
	double blastEvalue = 1e-10;
	
	ArrayList<GeneID> lsCopedIDsTest = null;
	ArrayList<GeneID> lsCopedIDsBG = null;
	/** genUniID item,item格式  */
	ArrayList<GeneID2LsItem> lsTest = null;
	/** genUniID item,item格式 */
	ArrayList<GeneID2LsItem> lsBGGeneID2Items = null;
	String BGfile = "";
	int BGnum = 0;
	/**
	 * gene2CopedID的对照表，多个accID对应同一个geneID的时候就用这个hash来处理
	 * 用途，当做elimFisher的时候，最后会得到一系列的geneID，而每个geneID可能对应了多个accID
	 * 这时候就用geneID作为key，将accID放入value的list中。
	 * 但是很可能value里面的copedID有相同的accID，这时候为了避免这种情况，我新建了一个hashAcc2CopedID
	 * 专门用于去冗余
	 */
	ArrayListMultimap<String, GeneID> mapGeneUniID2LsGeneID = ArrayListMultimap.create();
	ArrayList<StatisticTestResult> lsTestResult = new ArrayList<StatisticTestResult>();
	
	/**
	 * Gene2GO或者Gene2Path的信息
	 * 每次设置新的LsCopedTest后必须重置
	 */
	ArrayList<String[]> lsGene2GOPath = null;
	
	StatisticsTest statisticsTest;

	public AbstFunTest(ArrayList<GeneID> lsCopedIDsTest, ArrayList<GeneID> lsCopedIDsBG, boolean blast) {
		this.lsCopedIDsTest = lsCopedIDsTest;
		this.lsCopedIDsBG = lsCopedIDsBG;
		this.blast = blast;
	}
	
	public AbstFunTest() {}
	
	public void setStatisticsTest(StatisticsTest statisticsTest) {
		this.statisticsTest = statisticsTest;
	}
	
	public void setBlast(boolean blast, double evalue, int... blastTaxID) {
		this.blast = blast;
		this.blastTaxID = blastTaxID;
		this.blastEvalue = evalue;
	}
	
	public void setBlastTaxID(int... taxID) {
		this.blastTaxID = taxID;
	}

	/**
	 * 设定物种
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * 获得当前物种
	 * @return
	 */
	public int getTaxID() {
		return taxID;
	}
	
	/**
	 * 最好能第一时间设定
	 * 读取genUniID item,item格式的表
	 * @param fileName
	 */
	public void setLsBGItem(String fileName) {
		//清空Test
		lsTestResult = new ArrayList<StatisticTestResult>();
		lsBGGeneID2Items = new ArrayList<GeneID2LsItem>();
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		
		ArrayList<String[]> lsTmpGeneID2LsItem = ExcelTxtRead.readLsExcelTxt(fileName, new int[]{1,2}, 1, -1, true);
		lsBGGeneID2Items = readFromBGfile(lsTmpGeneID2LsItem);
		BGnum = lsBGGeneID2Items.size();
	}
	/**
	 * 将输入的geneID item,item list
	 * 导入
	 * @param lsTmpGeneID2LsItem
	 * @return
	 */
	protected abstract ArrayList<GeneID2LsItem> readFromBGfile(ArrayList<String[]> lsTmpGeneID2LsItem);
	
	/**
	 * 第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum) {
		lsTestResult = new ArrayList<StatisticTestResult>();
		if (lsCopedIDsBG == null) {
			lsCopedIDsBG = new ArrayList<GeneID>();
		}
		lsCopedIDsBG.clear();
		
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		ArrayList<String[]> accID = null;
		try {
			accID =  ExcelTxtRead.readLsExcelTxt(fileName, new int[]{colNum}, 1, -1);
		} catch (Exception e) {
			logger.error("BG accID file is not correct: "+ fileName);
		}
		for (String[] strings : accID) {
			GeneID copedID = new GeneID(strings[0], taxID, false);
			if (blast) {
				copedID.setBlastInfo(blastEvalue, blastTaxID);
			}
			lsCopedIDsBG.add(copedID);
		}
		lsBGGeneID2Items = convert2Item(lsCopedIDsBG);
		BGnum = lsBGGeneID2Items.size();
	}
	/**
	 * 第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param showMessage
	 */
	public void setLsBGCopedID(ArrayList<GeneID> lsBGaccID) {
		lsTestResult = new ArrayList<StatisticTestResult>();
		for (GeneID copedID : lsBGaccID) {
			copedID.setBlastInfo(blastEvalue, blastTaxID);
		}
		this.lsCopedIDsBG = lsBGaccID;
		lsBGGeneID2Items = convert2Item(lsCopedIDsBG);
		BGnum = lsBGGeneID2Items.size();
	}
	/**
	 * 要先读取AccID文件
	 * @return
	 */
	protected ArrayList<GeneID2LsItem> getLsBG() {
		return lsBGGeneID2Items;
	}
	
	public void setLsTestAccID(ArrayList<String> lsCopedID) {
		lsCopedIDsTest = new ArrayList<GeneID>();		
		for (String string : lsCopedID) {
			GeneID copedID = new GeneID(string, taxID, false);
			lsCopedIDsTest.add(copedID);
		}
		initial();
	}
	
	public void setLsTestGeneID(ArrayList<GeneID> lsCopedIDs) {
		this.lsCopedIDsTest = lsCopedIDs;
		initial();
	}
	
	private void initial() {
		lsGene2GOPath = null;
		fillCopedIDInfo(lsCopedIDsTest);
		lsTest = getLsTestFromLsBG(lsCopedIDsTest);
		lsTestResult = new ArrayList<StatisticTestResult>();
	}
	/**
	 * 设定hashgene2CopedID，就是一个geneID会对应多个accID的这种
	 * @param lsCopedIDs
	 */
	private void fillCopedIDInfo(ArrayList<GeneID> lsCopedIDs) {
		//////////////  先 清 空  ////////////////////////
		HashSet<String> setAccID = new HashSet<String>();
		mapGeneUniID2LsGeneID.clear();
		////////////////////////////////////////////
		for (GeneID geneID : lsCopedIDs) {
			//去冗余，accID相同去掉
			if (setAccID.contains(geneID.getAccID())) {
				continue;
			}
			setAccID.add(geneID.getAccID());
			mapGeneUniID2LsGeneID.put(geneID.getGenUniID(), geneID);
		}
	}
	/**
	 * 用copedID的geneUniID先查找lsBG，找不到再从头查找
	 * 目的是优化性能
	 * 如果lsTest中有一些新的gene，也添加入lsBGGeneID2Items中
	 * @param lsTest
	 * @return
	 */
	private ArrayList<GeneID2LsItem> getLsTestFromLsBG(ArrayList<GeneID> lsTest) {
		//去冗余用的
		HashSet<GeneID> setGeneIDs = new HashSet<GeneID>();
		for (GeneID geneID : lsTest) {
			if (blast) {
				geneID.setBlastInfo(blastEvalue, blastTaxID);
			}
			setGeneIDs.add(geneID);
		}
		
		//如果没有lsBG，就查找数据库，否则查找lsBG
		if (lsBGGeneID2Items == null || lsBGGeneID2Items.size() < 1) {
			return convert2Item(setGeneIDs);
		}
		
		HashMap<String, GeneID2LsItem>  mapBGGeneID2Items = new HashMap<String, GeneID2LsItem>();
		for (GeneID2LsItem geneID2LsGO : lsBGGeneID2Items) {
			mapBGGeneID2Items.put(geneID2LsGO.getGeneUniID(), geneID2LsGO);
		}
		ArrayList<GeneID2LsItem> lsout = new ArrayList<GeneID2LsItem>();
		
		//输入的lsTest基因，如果在背景中找不到对应的信息，则保存进入该list
		ArrayList<GeneID> lsInputNotFindGene = new ArrayList<GeneID>();
		for (GeneID copedID : setGeneIDs) {
			GeneID2LsItem tmpresult = mapBGGeneID2Items.get(copedID.getGenUniID());
			if (tmpresult == null) {
				lsInputNotFindGene.add(copedID);
				continue;
			}
			lsout.add(tmpresult);
		}
		if (lsInputNotFindGene.size() > 0) {
			ArrayList<GeneID2LsItem> lsnew = convert2Item(lsInputNotFindGene);
			lsout.addAll(lsnew);
			lsBGGeneID2Items.addAll(lsnew);
		}
		return lsout;
	}
	
	/**
	 * 将List-CopedID转化为
	 * geneID goID,goID,goID的样式
	 * 并按照genUniID去冗余
	 */
	protected abstract ArrayList<GeneID2LsItem> convert2Item(Collection<GeneID> lsCopedIDs);
	
	/**
	 * 待修正
	 * 返回Gene2ItemPvalue
	 * @param Type
	 * @return
	 * 根据不同的StatisticTestGene2Item子类有不同的情况
	 */
	public ArrayList<StatisticTestGene2Item> getGene2ItemPvalue() {
		ArrayList<StatisticTestGene2Item> lsTestResult = new ArrayList<StatisticTestGene2Item>();
		Map<String, StatisticTestResult> mapItem2StatictResult = getMapItemID2StatisticsResult();
		for (GeneID geneID : lsCopedIDsTest) {
			StatisticTestGene2Item statisticTestGene2Item = creatStatisticTestGene2Item();
			statisticTestGene2Item.setGeneID(geneID, blast);
			statisticTestGene2Item.setStatisticTestResult(mapItem2StatictResult);
			lsTestResult.add(statisticTestGene2Item);
		}
		return lsTestResult;
	}
	
	protected abstract StatisticTestGene2Item creatStatisticTestGene2Item();

	/**
	 * 把 getTestResult() 的结果装入hash表
	 * @return
	 */
	private HashMap<String, StatisticTestResult> getMapItemID2StatisticsResult() {
		ArrayList<StatisticTestResult> lStatisticTestResults = getTestResult();
		//key为小写，item和检验结果的map
		HashMap<String, StatisticTestResult> mapItem2StatisticsResult = new HashMap<String, StatisticTestResult>();
		for (StatisticTestResult statisticTestResult : lStatisticTestResults) {
			mapItem2StatisticsResult.put(statisticTestResult.getItemName().toLowerCase(), statisticTestResult);
		}
		return mapItem2StatisticsResult;
	}
	/**
	 * booRun 新跑一次 返回最后的结果，ElimGO需要覆盖该方法 对结果排个序
	 * 返回最后的结果，ElimGO需要覆盖该方法
	 * @throws Exception 
	 * 没有就返回null
	 */
	public ArrayList<StatisticTestResult> getTestResult() {
		if (statisticsTest == null) {
			statisticsTest = new FisherTest();
		}
		if (lsTestResult != null && lsTestResult.size() > 10) {
			return lsTestResult;
		}
		ArrayList<GeneID2LsItem> lstest = new ArrayList<GeneID2LsItem>();
		for (GeneID2LsItem geneID2LsGO : lsTest) {
			if (!geneID2LsGO.isValidate()) {
				continue;
			}
			lstest.add(geneID2LsGO);
		}
		if (lstest.size() == 0) {
			return null;
		}
		ArrayList<GeneID2LsItem> lsbg = new ArrayList<GeneID2LsItem>();
		for (GeneID2LsItem geneID2LsGO : lsBGGeneID2Items) {
			if (!geneID2LsGO.isValidate()) {
				continue;
			}
			lsbg.add(geneID2LsGO);
		}
		lsTestResult = GeneID2LsItem.getFisherResult(statisticsTest, lstest, lsbg,BGnum);
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			statisticTestResult.setItemTerm(getItemTerm(statisticTestResult.getItemName()));
		}
		return lsTestResult;
	}
	/**
	 * 返回指定的Item的注释
	 * 譬如GOterm。kegg term等
	 * @param item
	 * @return
	 */
	protected abstract String getItemTerm(String item);
	/**
	 * 目前只能设定GO的type
	 */
	public abstract void setDetailType(GOtype gotype);
	
	/**
	 * 保存本LsBG的信息
	 * @param txtBGItem
	 */
	public void saveLsBGItem(String txtBGItem) {
		TxtReadandWrite txtOut = new TxtReadandWrite(txtBGItem, true);
		for (GeneID2LsItem geneID2LsGO : lsBGGeneID2Items) {
			txtOut.writefileln(geneID2LsGO.toString());
		}
		txtOut.close();
	}
	
	/**
	 * 只能用于GO分析
	 * @param goType
	 */
	public void setGoType(GOtype goType) { }
	
}
