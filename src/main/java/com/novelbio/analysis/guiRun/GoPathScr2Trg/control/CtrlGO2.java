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
	 * �õ���ģʽ
	 */
	private static CtrlGO2 ctrlGO = null;
	
	FunctionTest functionTest = null;
	/**
	 * �Ƿ���Ҫblast
	 */
	boolean blast = false;
	/**
	 * ��������
	 */
	int QtaxID = 0;
	/**
	 * blast����
	 */
	int[] StaxID = null;
	/**
	 * blast��evalue
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
	 * ���,key�� ʱ�ڵ�
	 * value������Ľ��
	 * key: gene2Go, resultTable��
	 * value����Ӧ�Ľ��
	 */
	LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>> hashResultGene = new LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>>();
	/**
	 * �������
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
	 * ����ܵ�һʱ���趨 ��ȡgenUniID item,item��ʽ�ı�
	 * 
	 * @param fileName
	 */
	public void setLsBGItem(String fileName) {
		functionTest.setLsBGItem(fileName);
	}

	/**
	 * ����ܵ�һʱ���趨 ��ȡ�����ļ���ָ����ȡĳһ��
	 * 
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum) {
		functionTest.setLsBGAccID(fileName, colNum);
	}
	
	/**
	 * �����ļ������ļ��ָ�����Լ��ڼ��У���ø��еĻ���ID
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public void doInBackgroundNorm(ArrayList<String[]> lsAccID2Value, int up, int down) {
		hashResultGene.clear();
		HashMap<String, ArrayList<CopedID>> hashCluster = new HashMap<String, ArrayList<CopedID>>();
		//�����µ�
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
	 * �����ļ������ļ��ָ�����Լ��ڼ��У���ø��еĻ���ID
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
	 * ��������㣬��������save��
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
