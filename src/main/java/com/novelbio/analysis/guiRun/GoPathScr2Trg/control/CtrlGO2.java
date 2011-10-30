package com.novelbio.analysis.guiRun.GoPathScr2Trg.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.GO.execute.GoFisherNew;
import com.novelbio.analysis.annotation.GO.goEntity.GOInfoAbs;
import com.novelbio.analysis.annotation.GO.queryDB.QgeneID2Go;
import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI.CopyOfGUIanalysisSimple;
import com.novelbio.analysis.seq.chipseq.cGIsland.CpG;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;

public class CtrlGO2 {

	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	/**
	 * 用单例模式
	 */
	private static CtrlGO2 ctrlGO = null;
	
	FunctionTest functionTest = null;
	/**
	 * 是否需要blast
	 */
	boolean blast = false;
	/**
	 * 查找物种
	 */
	int QtaxID = 0;
	/**
	 * blast物种
	 */
	int[] StaxID = null;
	/**
	 * blast的evalue
	 */
	double evalue = 1e-10;

	String GOClass = GOInfoAbs.GO_BP;
	int[] colID = new int[2];
	String resultExcel = "";
	double up = -1;
	double down = -1;
	boolean elimGo = true;
	String[] prix = new String[2];
	boolean cluster = false;
	/**
	 * 结果,key： 时期等
	 * value：具体的结果
	 * key: gene2Go, resultTable等
	 * value：相应的结果
	 */
	LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>> hashResultGene = new LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>>();
	/**
	 * 界面对象
	 */
	CopyOfGUIanalysisSimple guiBlast;

	public HashMap<String, LinkedHashMap<String,ArrayList<String[]>>> getHashResult() {
		return hashResultGene;
	}

	/**
	 * @param elimGo
	 * @param GOClass
	 * @param QtaxID
	 * @param blast
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public static CtrlGO2 getInstance(boolean elimGo, String GOClass, int QtaxID, boolean blast, double evalue, int... StaxID) {
		ctrlGO = new CtrlGO2(elimGo, GOClass, QtaxID, blast, evalue, StaxID);
		return ctrlGO;
	}

	/**
	 * @param elimGo
	 * @param geneFileXls
	 * @param GOClass
	 * @param colAccID
	 * @param colFC
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 */
	private CtrlGO2(boolean elimGo, String GOClass, int QtaxID, boolean blast,
			double evalue, int... StaxID) {
		if (elimGo) {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_ELIM,
					QtaxID, blast, evalue, StaxID);
		} else {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_NOVELBIO,
					QtaxID, blast, evalue, StaxID);
		}
	}

	public void setLsTestID(ArrayList<String> lsAccID) {
		functionTest.setLsTestAccID(lsAccID);
	}

	/**
	 * 最好能第一时间设定 读取genUniID item,item格式的表
	 * 
	 * @param fileName
	 */
	public void setLsBGItem(String fileName) {
		functionTest.setLsBGItem(fileName);
	}

	/**
	 * 最好能第一时间设定 读取背景文件，指定读取某一列
	 * 
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum) {
		functionTest.setLsBGAccID(fileName, colNum);
	}
	
	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public void doInBackgroundNorm(ArrayList<String[]> lsAccID2Value, int up, int down) {
		hashResultGene.clear();
		HashMap<String, ArrayList<CopedID>> hashCluster = new HashMap<String, ArrayList<CopedID>>();
		//分上下调
		if (lsAccID2Value.get(0).length == 1) {
			ArrayList<CopedID> lsAll = new ArrayList<CopedID>();
			for (String[] strings : lsAccID2Value) {
				CopedID copedID = new CopedID(strings[0], QtaxID, false);
				lsAll.add(copedID);
			}
			hashCluster.put("All", lsAll);
		}
		else {
			ArrayList<CopedID> lsUp = new ArrayList<CopedID>();
			ArrayList<CopedID> lsDown = new ArrayList<CopedID>();
			for (String[] strings : lsAccID2Value) {
				CopedID copedID = new CopedID(strings[0], QtaxID, false);
				if (Double.parseDouble(strings[1]) <= down) {
					lsDown.add(copedID);
				}
				else if (Double.parseDouble(strings[1]) >= up) {
					lsUp.add(copedID);
				}
			}
			hashCluster.put("Up", lsUp);
			hashCluster.put("Down", lsDown);
		}
		
		for (Entry<String, ArrayList<CopedID>> entry : hashCluster.entrySet()) {
			getResult( functionTest, entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public void doInBackgroundCluster(ArrayList<String[]> lsAccID2Value, int up, int down) {
		hashResultGene.clear();
		HashMap<String, ArrayList<CopedID>> hashCluster = new HashMap<String, ArrayList<CopedID>>();
		
		
		for (String[] strings : lsAccID2Value) {
			CopedID copedID = new CopedID(strings[0], QtaxID, false);
			if (hashCluster.containsKey(strings[1].trim())) {
				ArrayList<CopedID> lsTmp = hashCluster.get(strings[1].trim());
				lsTmp.add(copedID);
			}
			else {
				ArrayList<CopedID> lsTmp = new ArrayList<CopedID>();
				lsTmp.add(copedID);
				hashCluster.put(strings[1].trim(), lsTmp);
			}
		}
		
		for (Entry<String, ArrayList<CopedID>> entry : hashCluster.entrySet()) {
			getResult( functionTest, entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * 用这个计算，算完后才能save等
	 * @param functionTest
	 * @param prix
	 * @param lsCopedIDs
	 * @return
	 */
	private HashMap<String, LinkedHashMap<String, ArrayList<String[]>>> getResult(FunctionTest functionTest, String prix,ArrayList<CopedID>lsCopedIDs)
	{
		functionTest.setLsTest(lsCopedIDs);
		ArrayList<String[]> lsResultTest = functionTest.getTestResult();
		ArrayList<String[]> lsGene2GO = functionTest.getGene2Item();
		
		LinkedHashMap<String, ArrayList<String[]>> hashResult = new LinkedHashMap<String, ArrayList<String[]>>();
		hashResult.put("GO_Result", lsResultTest);
		hashResult.put("Gene2GO", lsGene2GO);
		if (elimGo) {
			ArrayList<String[]> lsGO2Gene = functionTest.getItem2GenePvalue();
			hashResult.put("GO2Gene", lsGO2Gene);
		}
		hashResultGene.put(prix, hashResult);
		return hashResultGene;
	}

	public void saveExcel(String excelPath) {
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(excelPath);
		for (Entry<String, LinkedHashMap<String, ArrayList<String[]>>> entry : hashResultGene.entrySet()) {
			String prix = entry.getKey();
			HashMap<String, ArrayList<String[]>> hashValue = entry.getValue();
			for (Entry<String,ArrayList<String[]>> entry2 : hashValue.entrySet()) {
				excelResult.WriteExcel(prix + entry2.getKey(), 1, 1, entry2.getValue());
			}
			if (elimGo) {
				FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP + prix,
						FileOperate.getParentPathName(excelPath), FileOperate.getFileNameSep(excelPath)[0] + prix + "GoMap.pdf", true);
			}
		}
	}

}
