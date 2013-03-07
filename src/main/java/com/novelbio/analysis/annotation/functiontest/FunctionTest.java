package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public abstract class FunctionTest {
	private static final Logger logger = Logger.getLogger(FunctionTest.class);
	
	public static final String FUNCTION_GO_NOVELBIO = "gene ontology";
	public static final String FUNCTION_GO_ELIM = "gene ontology elim";
	public static final String FUNCTION_PATHWAY_KEGG = "pathway kegg";
	
	int taxID = 0;
	int[] blastTaxID = null;
	double blastEvalue = 1e-10;
	
	Set<GeneID> lsCopedIDsTest = null;
	Set<GeneID> lsCopedIDsBG = null;
	/** genUniID item,item格式  */
	List<GeneID2LsItem> lsTest = null;
	/** genUniID item,item格式
	 * key: geneUniID
	 * value:GeneID2LsItem
	 *  */
	Map<String, GeneID2LsItem> mapBGGeneID2Items = null;
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
		
	public void setStatisticsTest(StatisticsTest statisticsTest) {
		this.statisticsTest = statisticsTest;
	}
	public void setBlastInfo(double evalue, int... blastTaxID) {
		this.blastTaxID = blastTaxID;
		this.blastEvalue = evalue;
	}
	/** 比对到哪些物种上去了 */
	public int[] getBlastTaxID() {
		return blastTaxID;
	}
	public boolean isBlast() {
		if (blastTaxID != null) {
			for (int taxID : blastTaxID) {
				if (taxID > 0) {
					return true;
				}
			}
		}
		return false;
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
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		
		ArrayList<String[]> lsTmpGeneID2LsItem = ExcelTxtRead.readLsExcelTxt(fileName, new int[]{1,2}, 1, -1, true);
		mapBGGeneID2Items = readFromBGfile(lsTmpGeneID2LsItem);
		BGnum = mapBGGeneID2Items.size();
	}
	/**
	 * 将输入的geneID item,item list
	 * 导入
	 * @param lsTmpGeneID2LsItem
	 * @return
	 */
	protected abstract Map<String, GeneID2LsItem> readFromBGfile(Collection<String[]> lsTmpGeneID2LsItem);
	
	/**
	 * 读取AccID文件，然后将Item保存至相应的文件夹中
	 * @param fileName 文本名
	 * @param colNum 读取第几列，也就是accID所在的列
	 * @param outLsItemFile 输出的文本名
	 */
	public void setLsBGAccID(String fileName, int colNum, String outLsItemFile) {
		setLsBGAccID(fileName, colNum);
		Map<String, GeneID2LsItem> lsBG = getLsBG();
		TxtReadandWrite txtOut = new TxtReadandWrite(outLsItemFile, true);
		for (GeneID2LsItem geneID2LsGO : lsBG.values()) {
			txtOut.writefileln(geneID2LsGO.toString());
		}
		txtOut.close();
	}
	
	/**
	 * 第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum) {
		lsTestResult = new ArrayList<StatisticTestResult>();
		if (lsCopedIDsBG == null) {
			lsCopedIDsBG = new HashSet<GeneID>();
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
			if (isBlast()) {
				copedID.setBlastInfo(blastEvalue, blastTaxID);
			}
			lsCopedIDsBG.add(copedID);
		}
		mapBGGeneID2Items = convert2Item(lsCopedIDsBG);
		BGnum = mapBGGeneID2Items.size();
	}
	/**
	 * 第一时间设定，在此之前必须先设定{@link #setBlast(boolean, double, int...)} blast情况
	 * 读取背景文件，指定读取某一列
	 * @param showMessage
	 */
	public void setLsBGCopedID(Collection<GeneID> lsBGaccID) {
		lsTestResult = new ArrayList<StatisticTestResult>();
		for (GeneID copedID : lsBGaccID) {
			copedID.setBlastInfo(blastEvalue, blastTaxID);
		}
		lsCopedIDsBG = new HashSet<GeneID>(lsBGaccID);
		mapBGGeneID2Items = convert2Item(lsCopedIDsBG);
		BGnum = mapBGGeneID2Items.size();
	}
	/**
	 * 补充BG的基因，因为BG可能没有cover 输入的testGene
	 * 不过我觉得没必要这样做
	 * @param lsBGaccID
	 */
	public void addBGGeneID(Collection<GeneID> lsBGaccID) {
		for (GeneID geneID : lsBGaccID) {
			convert2ItemFromBG(geneID, true);
		}
		BGnum = mapBGGeneID2Items.size();
	}
	/**
	 * 补充BG的基因，因为BG可能没有cover 输入的testGene
	 * 不过我觉得没必要这样做
	 * @param lsBGaccID
	 */
	public void addBGGeneIDAccID(Collection<String> lsBGaccID) {
		for (String string : lsBGaccID) {
			GeneID geneID = new GeneID(string, taxID);
			convert2ItemFromBG(geneID, true);
		}
		BGnum = mapBGGeneID2Items.size();
	}
	private Map<String, GeneID2LsItem> convert2Item(Collection<GeneID> lsGeneID) {
		Map<String, GeneID2LsItem> mapGeneID2LsItem = new LinkedHashMap<String, GeneID2LsItem>();
		for (GeneID geneID : lsGeneID) {
			GeneID2LsItem geneID2LsItem = convert2Item(geneID);
			if (geneID2LsItem == null) {
				continue;
			}
			mapGeneID2LsItem.put(geneID.getGenUniID(), geneID2LsItem);
		}
		return mapGeneID2LsItem;
	}
	
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue() {
		return new ArrayList<StatisticTestItem2Gene>();
	}
	
	/**
	 * 要先读取AccID文件
	 * @return
	 */
	protected Map<String, GeneID2LsItem> getLsBG() {
		return mapBGGeneID2Items;
	}
	
	public void setLsTestAccID(Collection<String> lsCopedID) {
		lsCopedIDsTest = new HashSet<GeneID>();		
		for (String string : lsCopedID) {
			GeneID copedID = new GeneID(string, taxID, false);
			lsCopedIDsTest.add(copedID);
		}
		initial();
	}
	
	public void setLsTestGeneID(Collection<GeneID> lsCopedIDs) {
		this.lsCopedIDsTest = new HashSet<GeneID>(lsCopedIDs);
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
	private void fillCopedIDInfo(Collection<GeneID> lsCopedIDs) {
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
	 * 使用之前先要设定好LsBG
	 * 用copedID的geneUniID先查找lsBG，找不到再从头查找
	 * 目的是优化性能
	 * 如果lsTest中有一些新的gene，也添加入lsBGGeneID2Items中
	 * @param lsTest
	 * @return
	 */
	protected ArrayList<GeneID2LsItem> getLsTestFromLsBG(Collection<GeneID> lsTest) {
		//去冗余用的
		HashSet<GeneID> setGeneIDs = new HashSet<GeneID>();
		for (GeneID geneID : lsTest) {
			if (isBlast()) {
				geneID.setBlastInfo(blastEvalue, blastTaxID);
			}
			setGeneIDs.add(geneID);
		}
		
		//如果没有lsBG，就查找数据库，否则查找lsBG
		if (mapBGGeneID2Items == null || mapBGGeneID2Items.size() < 1) {
			logger.error("BG文件要先输入");
			return null;
		}
		ArrayList<GeneID2LsItem> lsout = new ArrayList<GeneID2LsItem>();
		
		for (GeneID geneID : setGeneIDs) {
			GeneID2LsItem tmpresult = convert2ItemFromBG(geneID, false);
			if (tmpresult == null || !tmpresult.isValidate()) {
				continue;
			}
			lsout.add(tmpresult);
		}
		return lsout;
	}
	
	/**
	 * 首先在BG中查找GeneID2LsItem
	 * 找不到再到数据库里面找，顺便将找到的结果添加入BG
	 * @param geneID
	 * @return addToBG true 将找到的结果添加入BG
	 * false 不添加入BG
	 */
	protected GeneID2LsItem convert2ItemFromBG(GeneID geneID, boolean addToBG) {
		GeneID2LsItem tmpresult = mapBGGeneID2Items.get(geneID.getGenUniID());
		if (tmpresult == null && addToBG) {
			tmpresult = convert2Item(geneID);
			if (tmpresult != null) {
				mapBGGeneID2Items.put(geneID.getGenUniID(), tmpresult);
			}
		}
		
		return tmpresult;
	}
	
	/**
	 * 将GeneID转化为
	 * geneID goID,goID,goID的样式
	 * key: geneUniID
	 * Value: GeneID2LsItem
	 */
	protected abstract GeneID2LsItem convert2Item(GeneID geneID);
	
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
			statisticTestGene2Item.setGeneID(geneID, isBlast());
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
		List<GeneID2LsItem> lstest = getFilteredLs(lsTest);
		if (lstest.size() == 0) {
			return null;
		}
		List<GeneID2LsItem> lsbg = getFilteredLs(mapBGGeneID2Items.values());
		lsTestResult = GeneID2LsItem.getFisherResult(statisticsTest, lstest, lsbg, BGnum);
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			statisticTestResult.setItemTerm(getItemTerm(statisticTestResult.getItemName()));
		}
		return lsTestResult;
	}
	
	private List<GeneID2LsItem> getFilteredLs(Collection<GeneID2LsItem> lsInput) {
		List<GeneID2LsItem> lsResult = new ArrayList<GeneID2LsItem>();
		for (GeneID2LsItem geneID2LsGO : lsInput) {
			if (!geneID2LsGO.isValidate()) {
				continue;
			}
			lsResult.add(geneID2LsGO);
		}
		return lsResult;
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
		for (GeneID2LsItem geneID2LsGO : mapBGGeneID2Items.values()) {
			txtOut.writefileln(geneID2LsGO.toString());
		}
		txtOut.close();
	}
	
	/**
	 * 选择一种检验方式FUNCTION_GO_NOVELBIO等
	 * 是否blast，如果blast那么blast到哪几个物种
	 * @param functionType
	 */
	public static FunctionTest getInstance(String functionType) {
		FunctionTest functionTest;
		if (functionType.equals(FUNCTION_GO_NOVELBIO)) {
			functionTest = new NovelGOFunTest();
		}
		else if (functionType.equals(FUNCTION_GO_ELIM)) {
			functionTest = new ElimGOFunTest();
		}
		else if (functionType.equals(FUNCTION_PATHWAY_KEGG)) {
			functionTest = new KEGGPathwayFunTest();
		}
		else {
			logger.error("unknown functiontest: "+ functionType);
			return null;
		}
		return functionTest;
	}
	
}
