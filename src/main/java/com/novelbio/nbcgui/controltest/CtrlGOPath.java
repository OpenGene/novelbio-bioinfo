package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.nbcgui.GUI.GuiGoJPanel;
/**
 * ������ӽ�����
 * @author zong0jie
 *
 */
public abstract class CtrlGOPath extends RunProcess<GoPathInfo>{
	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	FunctionTest functionTest = null;
	/**  �Ƿ���Ҫblast */
	boolean blast = false;
	/** �������� */
	int QtaxID = 0;
	/**  blast���� */
	int[] StaxID = null;
	/** blast��evalue */
	double evalue = 1e-10;
	int[] colID = new int[2];
	String resultExcel = "";
	double up = -1;
	double down = -1;
	boolean cluster = false;
	String bgFile = "";
	/**
	 * ���,key�� ʱ�ڵ�
	 * value������Ľ��
	 * key: gene2Go, resultTable��
	 * value����Ӧ�Ľ��
	 */
	LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>> hashResultGene = new LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>>();
	
	boolean isCluster = false;
	
	ArrayList<String[]> lsAccID2Value;
	
	/**
	 * ���,key�� ʱ�ڵ�<br>
	 * value������Ľ��<br>
	 * key: gene2Go, resultTable��<br>
	 * value����Ӧ�Ľ��
	 */
	public HashMap<String, LinkedHashMap<String,ArrayList<String[]>>> getHashResult() {
		return hashResultGene;
	}
	/**
	 * @param QtaxID
	 * @param blast
	 * @param evalue
	 */
	protected CtrlGOPath( int QtaxID, boolean blast, double evalue) {
		this.QtaxID = QtaxID;
		this.blast = blast;
		this.evalue = evalue;
	}
	public void running() {
		if (isCluster) {
			doInBackgroundCluster();
		}
		else {
			doInBackgroundNorm();
		}
	}
	/** lsAccID2Value  arraylist-string[] ��� string[2],��ڶ���Ϊ���µ���ϵ���ж����µ�
	 * �����������
	 *  */
	public void setLsAccID2Value(ArrayList<String[]> lsAccID2Value) {
		this.lsAccID2Value = lsAccID2Value;
	}
	public void setUpDown(double up, double down) {
		this.up = up;
		this.down = down;
	}
	/**
	 * ��õ�һʱ������
	 * �򵥵��ж����������geneID����geneID2Item��
	 * @param fileName
	 */
	public void setLsBG(String fileName) {
		bgFile = fileName;
		boolean flagGeneID = testBGfile(fileName);
		if (flagGeneID) {
			functionTest.setLsBGItem(fileName);
		}
		else {
			if (FileOperate.isFileExist( getGene2ItemFileName(fileName))) {
				functionTest.setLsBGItem(getGene2ItemFileName(fileName));
			}
			else {
				functionTest.setLsBGAccID(fileName, 1, getGene2ItemFileName(fileName));
			}
		}
	}
	/**
	 * �ļ��������go_item����path_item��
	 * @param fileName
	 * @return
	 */
	abstract String getGene2ItemFileName(String  fileName);
	/**
	 * �����ļ��Ƿ�Ϊgene item,item�ĸ�ʽ
	 * @param fileName
	 * @return
	 */
	private boolean testBGfile(String fileName) {
		boolean result = false;
		ArrayList<String[]> lsArrayList = ExcelTxtRead.readLsExcelTxtFile(fileName, 1, 1, 100, -1);//readLsExcelTxt(fileName, 1, 100, 1, -1);
		for (String[] strings : lsArrayList) {
			if (strings.length > 1 && strings[1].contains(",")) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public void setIsCluster(boolean isCluster) {
		this.isCluster = isCluster;
	}
	/**
	 * �����ļ������ļ��ָ�����Լ��ڼ��У���ø��еĻ���ID
	 * @param lsAccID2Value  arraylist-string[] ��� string[2],��ڶ���Ϊ���µ���ϵ���ж����µ�
	 * ���string[1]���ж����µ�
	 * @param up
	 * @param down
	 */
	public void doInBackgroundNorm() {
		isCluster = false;
		hashResultGene.clear();
		HashMap<String, ArrayList<GeneID>> hashCluster = new LinkedHashMap<String, ArrayList<GeneID>>();
		//�����µ�
		if (lsAccID2Value.get(0).length == 1) {
			ArrayList<GeneID> lsAll = new ArrayList<GeneID>();
			for (String[] strings : lsAccID2Value) {
				if (strings[0] == null || strings[0].trim().equals("")) {
					continue;
				}
				GeneID copedID = new GeneID(strings[0], QtaxID, false);
				lsAll.add(copedID);
			}
			hashCluster.put("All", lsAll);
		}
		else {
			ArrayList<GeneID> lsUp = new ArrayList<GeneID>();
			ArrayList<GeneID> lsDown = new ArrayList<GeneID>();
			for (String[] strings : lsAccID2Value) {
				if (strings[0] == null || strings[0].trim().equals("")) {
					continue;
				}
				GeneID copedID = new GeneID(strings[0], QtaxID, false);
				try {
					if (Double.parseDouble(strings[1]) <= down) {
						lsDown.add(copedID);
					}
					else if (Double.parseDouble(strings[1]) >= up) {
						lsUp.add(copedID);
					}
				} catch (Exception e) { }
			}
			hashCluster.put("Up", lsUp);
			hashCluster.put("Down", lsDown);
		}
		
		for (Entry<String, ArrayList<GeneID>> entry : hashCluster.entrySet()) {
			getResult(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * �����ļ������ļ��ָ�����Լ��ڼ��У���ø��еĻ���ID
	 * 
	 * @param showMessage
	 * @return
	 * @throws Exception
	 */
	public void doInBackgroundCluster() {
		isCluster = true;
		hashResultGene.clear();
		HashMap<String, ArrayList<GeneID>> hashCluster = new HashMap<String, ArrayList<GeneID>>();
		for (String[] strings : lsAccID2Value) {
			if (strings[0] == null || strings[0].trim().equals("")) {
				continue;
			}
			GeneID copedID = new GeneID(strings[0], QtaxID, false);
			if (hashCluster.containsKey(strings[1].trim())) {
				ArrayList<GeneID> lsTmp = hashCluster.get(strings[1].trim());
				lsTmp.add(copedID);
			}
			else {
				ArrayList<GeneID> lsTmp = new ArrayList<GeneID>();
				lsTmp.add(copedID);
				hashCluster.put(strings[1].trim(), lsTmp);
			}
		}
		
		for (Entry<String, ArrayList<GeneID>> entry : hashCluster.entrySet()) {
			getResult(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * ��������㣬��������save��
	 * @param functionTest
	 * @param prix
	 * @param lsCopedIDs
	 * @return
	 * û�оͷ���null
	 */
	private void getResult(String prix,ArrayList<GeneID>lsCopedIDs) {
		functionTest.setLsTest(lsCopedIDs);
		ArrayList<String[]> lsResultTest = functionTest.getTestResult();
		if (lsResultTest == null) {
			return;
		}
		lsResultTest.add(0,getResultTitle());
		LinkedHashMap<String, ArrayList<String[]>> hashResult = calItem2GenePvalue(prix, lsResultTest);
		hashResultGene.put(prix, hashResult);
	}
	abstract String[] getResultTitle();
	/**
	 * ���ظü�������Ӧ���صļ���ʱ�ڵ���Ϣ��Ҳ���Ǽ���sheet
	 * @param lsResultTest ��������װ��hash��
	 * @return
	 */
	protected abstract LinkedHashMap<String, ArrayList<String[]>> calItem2GenePvalue(String prix, ArrayList<String[]> lsResultTest);

	public void saveExcel(String excelPath) {
		if (cluster)
			saveExcelCluster(excelPath);
		else
			saveExcelNorm(excelPath);
	}
	
	private void saveExcelNorm(String excelPath) {
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(excelPath);
		for (Entry<String, LinkedHashMap<String, ArrayList<String[]>>> entry : hashResultGene.entrySet()) {
			String prix = entry.getKey();
			HashMap<String, ArrayList<String[]>> hashValue = entry.getValue();
			for (Entry<String,ArrayList<String[]>> entry2 : hashValue.entrySet()) {
				excelResult.WriteExcel(prix + entry2.getKey(), 1, 1, entry2.getValue());
			}
			copeFile(prix, excelPath);
		}
	}
	
	private void saveExcelCluster(String excelPath) {
		for (Entry<String, LinkedHashMap<String, ArrayList<String[]>>> entry : hashResultGene.entrySet()) {
			ExcelOperate excelResult = new ExcelOperate();
			String prix = entry.getKey();

			String excelPathOut = FileOperate.changeFileSuffix(excelPath, "_" + prix, null);
			excelResult.openExcel(excelPathOut);
			
			HashMap<String, ArrayList<String[]>> hashValue = entry.getValue();
			for (Entry<String,ArrayList<String[]>> entry2 : hashValue.entrySet()) {
				excelResult.WriteExcel(entry2.getKey(), 1, 1, entry2.getValue());
			}
			copeFile(prix, excelPath);
		}
	}
	/**
	 * �Ƿ���Ҫ����Ĵ����ļ�������Ҫ������
	 * Ʃ��elimGO��Ҫ�ƶ�GOMAP��
	 */
	protected abstract void copeFile(String prix, String excelPath);

}

class GoPathInfo {
	int num = 0;
	public GoPathInfo(int num) {
		this.num = num;
	}
}
