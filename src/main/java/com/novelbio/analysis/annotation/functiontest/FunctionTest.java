package com.novelbio.analysis.annotation.functiontest;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.StatisticsTest;
import com.novelbio.base.dataStructure.StatisticsTest.StatisticsPvalueType;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;

public abstract class FunctionTest implements Cloneable {
	private static final Logger logger = Logger.getLogger(FunctionTest.class);
	
	public static final String FUNCTION_GO_NOVELBIO = "gene ontology";
	public static final String FUNCTION_GO_ELIM = "gene ontology elim";
	public static final String FUNCTION_PATHWAY_KEGG = "pathway kegg";
	public static final String FUNCTION_COG = "cog";
	
	/** 默认pvalue的cutoff的值 */
	public static final double PvalueFdr_Cutoff = 0.05;
	
	int taxID = 0;
	List<Integer> lsBlastTaxId = null;
	double blastEvalue = 1e-10;
	
	Set<GeneID> setGeneIDsBG = null;
	/** genUniID item,item格式  */
	List<GeneID2LsItem> lsTest = null;
	/** genUniID item,item格式
	 * key: geneUniID
	 * value:GeneID2LsItem
	 *  */
	Map<String, GeneID2LsItem> mapBGGeneID2Items = null;
	String BGfile = "";
	protected int BGnum = 0;
	/**
	 * gene2CopedID的对照表，多个accID对应同一个geneID的时候就用这个hash来处理
	 * 用途，当做elimFisher的时候，最后会得到一系列的geneID，而每个geneID可能对应了多个accID
	 * 这时候就用geneID作为key，将accID放入value的list中。
	 * 但是很可能value里面的copedID有相同的accID，这时候为了避免这种情况，我新建了一个hashAcc2CopedID
	 * 专门用于去冗余
	 * <b>key为小写</b>
	 */
	ArrayListMultimap<String, GeneID> mapGeneUniID2LsGeneID = ArrayListMultimap.create();
	ArrayList<StatisticTestResult> lsTestResult = new ArrayList<StatisticTestResult>();
	
	StatisticsTest statisticsTest;
	
	protected abstract TestType getTestType();
	
	public void setStatisticsTest(StatisticsTest statisticsTest) {
		this.statisticsTest = statisticsTest;
	}
	
	public void setBlastInfo(double evalue, List<Integer> lsBlastTaxId) {
		this.blastEvalue = evalue;
		this.lsBlastTaxId = lsBlastTaxId;
	}
	/** 比对到哪些物种上去了 */
	public List<Integer> getBlastTaxID() {
		return lsBlastTaxId;
	}
	public boolean isBlast() {
		if (lsBlastTaxId != null) {
			for (int taxID : lsBlastTaxId) {
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
		
		List<String[]> lsTmpGeneID2LsItem = ExcelTxtRead.readLsExcelTxt(fileName, new int[]{1,2}, 1, -1, true);
		mapBGGeneID2Items = readFromBGfile(lsTmpGeneID2LsItem);
		BGnum = mapBGGeneID2Items.size();
	}
	
	/**
	 * 将输入的geneID item,item list
	 * 导入
	 * @param lsTmpGeneID2LsItem
	 * @return key为小写
	 */
	protected abstract Map<String, GeneID2LsItem> readFromBGfile(Collection<String[]> lsTmpGeneID2LsItem);
	
	/** 直接设定背景，效果类似 {@link #setLsBGItem(String)}
	 * @param mapBGGeneID2Items
	 * key务必为小写
	 *  */
	public void setMapBGGeneID2Items(Map<String, GeneID2LsItem> mapBGGeneID2Items) {
			//清空Test
		lsTestResult = new ArrayList<StatisticTestResult>();
		this.mapBGGeneID2Items = mapBGGeneID2Items;
		BGnum = mapBGGeneID2Items.size();
	}
	
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
		if (setGeneIDsBG == null) {
			setGeneIDsBG = new HashSet<GeneID>();
		}
		setGeneIDsBG.clear();
		
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no File exist: "+ fileName);
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
				copedID.setBlastInfo(blastEvalue, lsBlastTaxId);
			}
			setGeneIDsBG.add(copedID);
		}
		mapBGGeneID2Items = convert2Item(setGeneIDsBG);
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
	
	/**
	 * @param lsGeneID
	 * @return key 自动转换为小写
	 */
	private Map<String, GeneID2LsItem> convert2Item(Collection<GeneID> lsGeneID) {
		Map<String, GeneID2LsItem> mapGeneID2LsItem = new LinkedHashMap<String, GeneID2LsItem>();
		int num = 0;
		int numAll = lsGeneID.size();
		for (GeneID geneID : lsGeneID) {
			num++;
			GeneID2LsItem geneID2LsItem = convert2Item(geneID);
			if (geneID2LsItem == null) {
				continue;
			}
			mapGeneID2LsItem.put(geneID.getGeneUniID().toLowerCase(), geneID2LsItem);
			if (num % 200 == 0) {
				logger.info("总共 " + numAll + " 个基因， 已经找了" + num + "个基因" );
			}
		}
		return mapGeneID2LsItem;
	}
	
	public ArrayList<StatisticTestItem2Gene> getItem2GenePvalue() {
		ArrayList<StatisticTestResult> lsTestResult = getTestResult();
		ArrayList<StatisticTestItem2Gene> lStatisticTestItem2Gene = new ArrayList<StatisticTestItem2Gene>();
		
		ArrayListMultimap<String, GeneID> hashGo2LsGene = getGo2GeneUniID();
		
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			List<GeneID> lsTmpGeneUniID = hashGo2LsGene.get(statisticTestResult.getItemID());
			ArrayList<GeneID> lsFinalGeneIDs = new ArrayList<GeneID>();
			for (GeneID geneID : lsTmpGeneUniID) {
				//同一个geneUniID对应的不同accID
				List<GeneID> lscopedIDs = mapGeneUniID2LsGeneID.get(geneID.getGeneUniID().toLowerCase());
				lsFinalGeneIDs.addAll(lscopedIDs);
			}
			
			StatisticTestItem2Gene statisticTestItem2Gene = new StatisticTestItem2Gene();
			statisticTestItem2Gene.setStatisticTestResult(statisticTestResult);
			statisticTestItem2Gene.setLsGeneIDs(lsFinalGeneIDs);
			statisticTestItem2Gene.setBlast(isBlast());
			lStatisticTestItem2Gene.add(statisticTestItem2Gene);
		}
		Collections.sort(lStatisticTestItem2Gene, new Comparator<StatisticTestItem2Gene>() {
			public int compare(StatisticTestItem2Gene o1, StatisticTestItem2Gene o2) {
				Double pvalue1 = o1.statisticTestResult.getPvalue();
				Double pvalue2 = o2.statisticTestResult.getPvalue();
				return pvalue1.compareTo(pvalue2);
			}
		});
		return lStatisticTestItem2Gene;
	}
	
	private ArrayListMultimap<String, GeneID> getGo2GeneUniID() {
		ArrayListMultimap<String, GeneID> hashGo2LsGene = ArrayListMultimap.create();
		ArrayList<StatisticTestGene2Item> lsStatisticTestGene2Items = getGene2ItemPvalue();
		for (StatisticTestGene2Item statisticTestGene2Item : lsStatisticTestGene2Items) {
			GeneID2LsItem geneID2LsItem = convert2ItemFromBG(statisticTestGene2Item.getGeneID(), false);
			if (geneID2LsItem == null) {
				continue;
			}
			for (String goid : geneID2LsItem.getSetItemID()) {
				hashGo2LsGene.put(goid, statisticTestGene2Item.getGeneID());
			}
		}
		return hashGo2LsGene;
	}
	
	
	/**
	 * 要先读取AccID文件
	 * @return
	 */
	protected Map<String, GeneID2LsItem> getLsBG() {
		return mapBGGeneID2Items;
	}
	
	public void setLsTestGeneID(Collection<GeneID> lsCopedIDs) {
		Set<GeneID> setGeneIDsTest = new HashSet<>(lsCopedIDs);
		initial(setGeneIDsTest);
	}
	
	private void initial(Set<GeneID> setGeneIDsTest) {
		fillCopedIDInfo(setGeneIDsTest);
		lsTest = getLsTestFromLsBG(setGeneIDsTest);
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
		for (GeneID geneID : lsCopedIDs) {
			//去冗余，accID相同去掉
			if (setAccID.contains(geneID.getAccID())) {
				continue;
			}
			setAccID.add(geneID.getAccID());
			mapGeneUniID2LsGeneID.put(geneID.getGeneUniID().toLowerCase(), geneID);
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
	protected List<GeneID2LsItem> getLsTestFromLsBG(Collection<GeneID> lsTest) {
		//去冗余用的
		HashSet<GeneID> setGeneIDs = new HashSet<GeneID>();
		for (GeneID geneID : lsTest) {
			if (isBlast()) {
				geneID.setBlastInfo(blastEvalue, lsBlastTaxId);
			}
			setGeneIDs.add(geneID);
		}
		
		//如果没有lsBG，就查找数据库，否则查找lsBG
		if (mapBGGeneID2Items == null || mapBGGeneID2Items.size() < 1) {
			throw new RuntimeException(BGfile + " no background item find, please check");
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
		GeneID2LsItem tmpresult = mapBGGeneID2Items.get(geneID.getGeneUniID().toLowerCase());
		if (tmpresult == null && addToBG) {
			tmpresult = convert2Item(geneID);
			if (tmpresult != null) {
				mapBGGeneID2Items.put(geneID.getGeneUniID().toLowerCase(), tmpresult);
			}
		}
		if (tmpresult != null && tmpresult.geneID == null) {
			tmpresult.geneID = geneID;
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
		Set<String> setAccID = new HashSet<String>();//用来去重复的
		for (GeneID2LsItem geneID2LsItem : lsTest) {
			for (GeneID geneID : mapGeneUniID2LsGeneID.get(geneID2LsItem.getGeneUniID().toLowerCase())) {
				if (setAccID.contains(geneID.getAccID())) continue;
				
				setAccID.add(geneID.getAccID());
				StatisticTestGene2Item statisticTestGene2Item = creatStatisticTestGene2Item();
				statisticTestGene2Item.setGeneID(geneID2LsItem, geneID, isBlast());
				statisticTestGene2Item.setStatisticTestResult(mapItem2StatictResult);
				lsTestResult.add(statisticTestGene2Item);
			}
		}
		return lsTestResult;
	}
	
	protected abstract StatisticTestGene2Item creatStatisticTestGene2Item();

	/**
	 * 把 getTestResult() 的结果装入hash表
	 * @return
	 */
	protected HashMap<String, StatisticTestResult> getMapItemID2StatisticsResult() {
		ArrayList<StatisticTestResult> lStatisticTestResults = getTestResult();
		//key为小写，item和检验结果的map
		HashMap<String, StatisticTestResult> mapItem2StatisticsResult = new HashMap<String, StatisticTestResult>();
		for (StatisticTestResult statisticTestResult : lStatisticTestResults) {
			mapItem2StatisticsResult.put(statisticTestResult.getItemID().toLowerCase(), statisticTestResult);
		}
		return mapItem2StatisticsResult;
	}
	
	public int getAllDifGeneNum() {
		return getTestResult().get(0).getAllDifGeneNum();
	}
	public int getAllGeneNum() {
		return getTestResult().get(0).getAllGeneNum();
	}
	/**
	 * booRun 新跑一次 返回最后的结果，ElimGO需要覆盖该方法 对结果排个序
	 * 返回最后的结果，ElimGO需要覆盖该方法
	 * @throws Exception 
	 * 没有就返回null<br>
	 * <b>结果已经排过序了</b>
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
		lsTestResult = new ArrayList<>();
		
		ArrayList<StatisticTestResult> lsTestResultTmp = getFisherResult(statisticsTest, lstest, lsbg, BGnum);
		for (StatisticTestResult statisticTestResult : lsTestResultTmp) {
			try {
				statisticTestResult.setItemTerm(getItemTerm(statisticTestResult.getItemID()));
				lsTestResult.add(statisticTestResult);
			} catch (Exception e) {
				logger.error("Iterm: " + statisticTestResult.getItemID() + " may have error", e);
			}
		}
		return lsTestResult;
	}
	
	protected List<GeneID2LsItem> getFilteredLs(Collection<GeneID2LsItem> lsInput) {
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
	 * 空的map表示没有返回结果<br>
	 * 获得可以写入excel的map<br>
	 * key为sheet名，是{@link StatisticTestResult#titleGO}, {@link StatisticTestGene2Item#titleGO}, {@link StatisticTestItem2Gene#titleGO} 等，<br>
	 * 注意pathway没有{@link StatisticTestItem2Gene#titlePath}这一个sheet
	 * <br><br>
	 * value为list
	 * @return
	 */
	public Map<String, List<String[]>> getMapWriteToExcel() {
		Map<String, List<String[]>> mapResult = new LinkedHashMap<String, List<String[]>>();
		List<String[]> lsStatisticTestResults = StatisticTestResult.getLsInfo(getTestType(), getTestResult());
		if (lsStatisticTestResults.size() == 0) {
			return new HashMap<>();
		}
		mapResult.put(StatisticTestResult.getTitle(getTestType()), lsStatisticTestResults);
		
		List<String[]> lsPath2GenePvalue = StatisticTestItem2Gene.getLsInfo(getTestType(), getItem2GenePvalue());
		mapResult.put(StatisticTestItem2Gene.getTitle(getTestType()), lsPath2GenePvalue);
		
		List<String[]> lsGene2PathPvalue = StatisticTestGene2Item.getLsInfo(getGene2ItemPvalue());
		mapResult.put(StatisticTestGene2Item.getTitle(getTestType()), lsGene2PathPvalue);
	
		return mapResult;
	}

	
	/**
	 * 对于输出的excel，每一个表需要套三线表到第几行，从1开始计算，不包括title<br>
	 *  key: sheetName<br>
	 *  value: 第几行，从1开始计算，如果小于0，则表示本表格全都套上格式
	 */
	public Map<String, Integer> getMapSheetName2EndLine() {
		Map<String, Integer> mapResult = new LinkedHashMap<>();
		List<String[]> lsStatisticTestResults = StatisticTestResult.getLsInfo(TestType.GO, getTestResult());
		if (lsStatisticTestResults.size() == 0) {
			return new HashMap<>();
		}
		mapResult.put(StatisticTestResult.getTitle(getTestType()), StatisticTestResult.getSigItemNum(getTestResult(), 0.05));
		mapResult.put(StatisticTestItem2Gene.getTitle(getTestType()), StatisticTestItem2Gene.getiSigNum(getItem2GenePvalue(), 0.05));
		mapResult.put(StatisticTestGene2Item.getTitle(getTestType()), -5);
		return mapResult;
	}
	
	/** 浅层克隆
	 * 其中与背景相关的mapBGGeneID2Items和setGeneIDsBG仅引用传递
	 * */
	public FunctionTest clone() {
		FunctionTest functionTest = null;
		try {
			functionTest = (FunctionTest)super.clone();
			functionTest.setGeneIDsBG = setGeneIDsBG;
			functionTest.BGfile = BGfile;
			functionTest.BGnum = BGnum;
			functionTest.blastEvalue = blastEvalue;
			if (lsBlastTaxId != null) {
				functionTest.lsBlastTaxId = new ArrayList<Integer>(lsBlastTaxId);
			}
			if (lsTest != null) {
				functionTest.lsTest = new ArrayList<GeneID2LsItem>(lsTest);
			}
			if (lsTestResult != null) {
				functionTest.lsTestResult = new ArrayList<StatisticTestResult>(lsTestResult);
			}
			if (mapGeneUniID2LsGeneID != null) {
				functionTest.mapGeneUniID2LsGeneID = ArrayListMultimap.create(mapGeneUniID2LsGeneID);
			}
			functionTest.mapBGGeneID2Items = mapBGGeneID2Items;
			functionTest.taxID = taxID;
		} catch (CloneNotSupportedException e) {
			return null;
		}
		
		return functionTest;
	}
	
	public BufferedImage getImagePvalue() {
		List<StatisticTestResult> lsTestResults = getTestResult();
		if (lsTestResults == null || lsTestResults.size() == 0) {
			return null;
		}
		try {
			BufferedImage bfImageLog2Pic = GoPathBarPlot.drawLog2PvaluePicture(lsTestResults, getTitle());
			return bfImageLog2Pic;
		} catch (Exception e) { e.printStackTrace(); 
			logger.error("draw pvalue pic error " + getTitle(), e);
			throw new ExceptionFunctionTest("draw pvalue pic error " + getTitle(), e);
		}
	}
	
	public BufferedImage getImageEnrichment() {
		List<StatisticTestResult> lsTestResults = getTestResult();
		if (lsTestResults == null || lsTestResults.size() == 0) {
			return null;
		}
		try {
			BufferedImage bfImageLog2Pic = GoPathBarPlot.drawEnrichmentPicture(lsTestResults, getTitle());
			return bfImageLog2Pic;
		} catch (Exception e) { 
			logger.error("draw enrich pic error " + getTitle(), e);
			throw new ExceptionFunctionTest("draw enrich pic error " + getTitle(), e);
		}
	}
	
	protected abstract String getTitle();
	
	/**
	 * 选择一种检验方式FUNCTION_GO_NOVELBIO等
	 * 是否blast，如果blast那么blast到哪几个物种
	 * @param functionType
	 */
	public static FunctionTest getInstance(String functionType) {
		FunctionTest functionTest;
		if (functionType.equals(FUNCTION_GO_NOVELBIO)) {
			functionTest = new NovelGOFunTest();
		} else if (functionType.equals(FUNCTION_GO_ELIM)) {
			functionTest = new ElimGOFunTest();
		} else if (functionType.equals(FUNCTION_PATHWAY_KEGG)) {
			functionTest = new KEGGPathwayFunTest();
		} else if (functionType.equals(FUNCTION_COG)) {
			functionTest = new CogFunTest();
		} else {
			logger.error("unknown functiontest: "+ functionType);
			return null;
		}
		return functionTest;
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
	protected static ArrayList<StatisticTestResult> getFisherResult(StatisticsTest statisticsTest, List<GeneID2LsItem> lsGene2Item,List<GeneID2LsItem> lsGene2ItemBG, int bgNum) {
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
		for (GeneID2LsItem geneID2LsItem : lsGene2Item) {
			for (String itemID : geneID2LsItem.getSetItemID()) {
				mapItem2SetGeneID.put(itemID, geneID2LsItem.getGeneUniID());
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
	 * 对结果进行排序
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
