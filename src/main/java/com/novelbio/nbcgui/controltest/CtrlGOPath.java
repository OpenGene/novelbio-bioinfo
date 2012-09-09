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
 * 考虑添加进度条
 * @author zong0jie
 *
 */
public abstract class CtrlGOPath extends RunProcess<GoPathInfo>{
	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	FunctionTest functionTest = null;
	/**  是否需要blast */
	boolean blast = false;
	/** 查找物种 */
	int QtaxID = 0;
	/**  blast物种 */
	int[] StaxID = null;
	/** blast的evalue */
	double evalue = 1e-10;
	int[] colID = new int[2];
	String resultExcel = "";
	double up = -1;
	double down = -1;
	boolean cluster = false;
	String bgFile = "";
	/**
	 * 结果,key： 时期等
	 * value：具体的结果
	 * key: gene2Go, resultTable等
	 * value：相应的结果
	 */
	LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>> hashResultGene = new LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>>();
	
	boolean isCluster = false;
	
	ArrayList<String[]> lsAccID2Value;
	
	/**
	 * 结果,key： 时期等<br>
	 * value：具体的结果<br>
	 * key: gene2Go, resultTable等<br>
	 * value：相应的结果
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
	/** lsAccID2Value  arraylist-string[] 如果 string[2],则第二个为上下调关系，判断上下调
	 * 否则就做单个
	 *  */
	public void setLsAccID2Value(ArrayList<String[]> lsAccID2Value) {
		this.lsAccID2Value = lsAccID2Value;
	}
	public void setUpDown(double up, double down) {
		this.up = up;
		this.down = down;
	}
	/**
	 * 最好第一时间输入
	 * 简单的判断下输入的是geneID还是geneID2Item表
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
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * @param lsAccID2Value  arraylist-string[] 如果 string[2],则第二个为上下调关系，判断上下调
	 * 如果string[1]则不判断上下调
	 * @param up
	 * @param down
	 */
	public void doInBackgroundNorm() {
		isCluster = false;
		hashResultGene.clear();
		HashMap<String, ArrayList<GeneID>> hashCluster = new LinkedHashMap<String, ArrayList<GeneID>>();
		//分上下调
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
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
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
	 * 用这个计算，算完后才能save等
	 * @param functionTest
	 * @param prix
	 * @param lsCopedIDs
	 * @return
	 * 没有就返回null
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
	 * 返回该检验所对应返回的几个时期的信息，也就是几个sheet
	 * @param lsResultTest 将检验结果装入hash表
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
	 * 是否需要额外的处理文件，不需要就留空
	 * 譬如elimGO需要移动GOMAP等
	 */
	protected abstract void copeFile(String prix, String excelPath);

}

class GoPathInfo {
	int num = 0;
	public GoPathInfo(int num) {
		this.num = num;
	}
}
