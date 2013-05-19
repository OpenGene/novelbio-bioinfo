package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.StatisticTestResult;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * 考虑添加进度条
 * @author zong0jie
 *
 */
public abstract class CtrlGOPath extends RunProcess<GoPathInfo> {
	private static final Logger logger = Logger.getLogger(CtrlGOPath.class);

	FunctionTest functionTest = null;
	
	double up = -1;
	double down = -1;
	
	/** 是否为clusterGO */
	boolean isCluster = false;
	
	/** 
	 * 读入的gene2Value表
	 * lsAccID2Value  arraylist-string[] 若为string[2],则第二个为上下调关系，判断上下调
	 * 若为string[1] 则跑全部基因作分析
	 */
	ArrayList<String[]> lsAccID2Value;
	
	/**
	 * 结果,key： 时期等
	 * value：相应的结果
	 */	
	Map<String, FunctionTest> mapPrefix2FunTest = new LinkedHashMap<String, FunctionTest>();
	
	
	public void setTaxID(int taxID) {
		functionTest.setTaxID(taxID);
	}
	
	public int getTaxID() {
		return functionTest.getTaxID();
	}
	public List<Integer> getBlastTaxID() {
		return functionTest.getBlastTaxID();
	}
	
	/** lsAccID2Value  arraylist-string[] 若为string[2],则第二个为上下调关系，判断上下调
	 * 若为string[1] 则跑全部基因作分析
	 */
	public void setLsAccID2Value(ArrayList<String[]> lsAccID2Value) {
		this.lsAccID2Value = lsAccID2Value;
	}
	
	public void setUpDown(double up, double down) {
		this.up = up;
		this.down = down;
	}
	
	public void setBlastInfo(double blastevalue, List<Integer> lsBlastTaxID) {
		functionTest.setBlastInfo(blastevalue, lsBlastTaxID);
	}
	
	/**
	 * <b>在这之前要先设定GOlevel</b>
	 * 简单的判断下输入的是geneID还是geneID2Item表
	 * @param fileName
	 */
	public void setLsBG(String fileName) {
		boolean flagGeneID = testBGfile(fileName);
		if (flagGeneID) {
			functionTest.setLsBGItem(fileName);
		} else {
			if (FileOperate.isFileExist( getGene2ItemFileName(fileName))) {
				functionTest.setLsBGItem(getGene2ItemFileName(fileName));
			} else {
				functionTest.setLsBGAccID(fileName, 1, getGene2ItemFileName(fileName));
			}
		}
	}
	/**
	 * 文件名后加上go_item或者path_item等
	 * @param fileName
	 * @return
	 */
	abstract String getGene2ItemFileName(String  fileName);
	/**
	 * 测试文件是否为gene item,item的格式
	 * @param fileName
	 * @return
	 */
	private boolean testBGfile(String fileName) {
		boolean result = false;
		TxtReadandWrite txtRead = new TxtReadandWrite(fileName);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			//TODO 判定是否为gene item,item的格式
			if (ss.length == 2 && ss[1].contains(",") && ss[1].split(",")[0].contains(":")) {
				result = true;
				break;
			}
		}
		txtRead.close();
		return result;
	}
	
	public void setIsCluster(boolean isCluster) {
		this.isCluster = isCluster;
	}
	
	/**
	 * 运行完后获得结果<br>
	 * 结果,key： 时期等<br>
	 * value：具体的结果<br>
	 */
	public Map<String, FunctionTest> getMapResult_Prefix2FunTest() {
		return mapPrefix2FunTest;
	}
	
	public void running() {
		if (isCluster) {
			runCluster();
		} else {
			runNorm();
		}
	}
	
	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * @param lsAccID2Value  arraylist-string[] 如果 string[2],则第二个为上下调关系，判断上下调
	 * 如果string[1]则不判断上下调
	 * @param up
	 * @param down
	 */
	private void runNorm() {
		isCluster = false;
		mapPrefix2FunTest.clear();
		HashMultimap<String, String> mapPrefix2AccID = HashMultimap.create();
		for (String[] strings : lsAccID2Value) {
			if (strings[0] == null || strings[0].trim().equals("")) {
				continue;
			}
			try {
				if (strings.length == 1) {
					mapPrefix2AccID.put("All", strings[0]);
				} else if (strings.length > 1 && Double.parseDouble(strings[1]) <= down ) {
					mapPrefix2AccID.put("Down", strings[0]);
				} else if (strings.length > 1 && Double.parseDouble(strings[1]) >= up) {
					mapPrefix2AccID.put("Up", strings[0]);
				}
			} catch (Exception e) { }
		}
		HashMultimap<String, GeneID> mapPrefix2SetAccID = addBG_And_Convert2GeneID(mapPrefix2AccID);
		for (String prefix : mapPrefix2SetAccID.keySet()) {
			getResult(prefix, mapPrefix2SetAccID.get(prefix));
		}
	}
	
	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * 
	 * @param showMessage
	 * @return
	 * @throws Exception
	 */
	private void runCluster() {
		isCluster = true;
		mapPrefix2FunTest.clear();
		HashMultimap<String, String> mapCluster2SetAccID = HashMultimap.create();
		for (String[] accID2prefix : lsAccID2Value) {
			mapCluster2SetAccID.put(accID2prefix[1], accID2prefix[0]);
		}
		HashMultimap<String, GeneID> mapCluster2SetGeneID = addBG_And_Convert2GeneID(mapCluster2SetAccID);
		for (String prefix : mapCluster2SetGeneID.keySet()) {
			getResult(prefix, mapCluster2SetGeneID.get(prefix));
		}
	}
	
	/** 将输入转化为geneID */
	private HashMultimap<String, GeneID> addBG_And_Convert2GeneID(HashMultimap<String, String> mapPrefix2SetAccID) {
		HashMultimap<String, GeneID> mapPrefix2SetGeneID = HashMultimap.create();
		for (String prefix : mapPrefix2SetAccID.keySet()) {
			Set<String> setAccID = mapPrefix2SetAccID.get(prefix);
			for (String accID : setAccID) {
				GeneID geneID = new GeneID(accID, functionTest.getTaxID());
				if (geneID.getIDtype() != GeneID.IDTYPE_ACCID || geneID.getLsBlastGeneID().size() > 0) {
					mapPrefix2SetGeneID.put(prefix, geneID);
				}
			}
		}//*1
		//以下是打算将输入的testID补充进入BG，不过我觉得没必要了
		//我sfesa们只要将BG尽可能做到全面即可，不用想太多
//		for (String prefix : mapPrefix2SetGeneID.keySet()) {
//			Set<GeneID> setGeneIDs = mapPrefix2SetGeneID.get(prefix);
//			functionTest.addBGGeneID(setGeneIDs);
//		}
		return mapPrefix2SetGeneID;
	}
	/**
	 * 用这个计算，算完后才能save等
	 * @param functionTest
	 * @param prixz1
	 * @param lsCopedIDs
	 * @return
	 * 没有就返回null   
	 */
	private void getResult(String prix, Collection<GeneID>lsCopedIDs) {
		functionTest.setLsTestGeneID(lsCopedIDs);
		ArrayList<StatisticTestResult> lsResultTest = functionTest.getTestResult();
		if (lsResultTest == null || lsResultTest.size() == 0) {
			return;
		}
		mapPrefix2FunTest.put(prix, functionTest.clone());
	}

	public void saveExcel(String excelPath) {
		if (isCluster) {
			saveExcelCluster(excelPath);
		} else {
			saveExcelNorm(excelPath);
		}
	}
	
	private void saveExcelNorm(String excelPath) {
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(excelPath);
		for (String prefix : mapPrefix2FunTest.keySet()) {
			FunctionTest functionTest = mapPrefix2FunTest.get(prefix);
			Map<String,   List<String[]>> mapSheetName2LsInfo = functionTest.getMapWriteToExcel();
			
			for (String sheetName : mapSheetName2LsInfo.keySet()) {
				excelResult.WriteExcel(prefix + sheetName, 1, 1, mapSheetName2LsInfo.get(sheetName));
			}
			copeFile(prefix, excelPath);
		}
	}
	
	private void saveExcelCluster(String excelPath) {
		for (String prefix : mapPrefix2FunTest.keySet()) {
			ExcelOperate excelResult = new ExcelOperate();
			String excelPathOut = FileOperate.changeFileSuffix(excelPath, "_" + prefix, null);
			excelResult.openExcel(excelPathOut);
			Map<String, List<String[]>> mapSheetName2LsInfo = mapPrefix2FunTest.get(prefix).getMapWriteToExcel();
			for (String sheetName : mapSheetName2LsInfo.keySet()) {
				excelResult.WriteExcel(sheetName, 1, 1, mapSheetName2LsInfo.get(sheetName));
			}
			copeFile(prefix, excelPath);
		}
	}
	/**
	 * 保存文件时，是否需要额外的处理文件，不需要就留空
	 * 譬如elimGO需要移动GOMAP等
	 */
	protected abstract void copeFile(String prix, String excelPath);
	
	/**
	 * 清空参数，每次调用之前先清空参数
	 */
	public void clearParam() {
		up = -1;
		down = -1;
		isCluster = false;
		lsAccID2Value = null;
		mapPrefix2FunTest = new LinkedHashMap<String, FunctionTest>();
		clear();
	}
	
	protected abstract void clear();
}

class GoPathInfo {
	int num = 0;
	public GoPathInfo(int num) {
		this.num = num;
	}
}
