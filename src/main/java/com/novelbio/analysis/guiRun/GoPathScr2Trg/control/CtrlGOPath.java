package com.novelbio.analysis.guiRun.GoPathScr2Trg.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modgo.GOInfoAbs;

public abstract class CtrlGOPath {
	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	
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
	
	int[] colID = new int[2];
	String resultExcel = "";
	double up = -1;
	double down = -1;

//	String[] prix = new String[2];
	boolean cluster = false;
	/**
	 * 结果,key： 时期等
	 * value：具体的结果
	 * key: gene2Go, resultTable等
	 * value：相应的结果
	 */
	LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>> hashResultGene = new LinkedHashMap<String, LinkedHashMap<String,ArrayList<String[]>>>();
	
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
	 * @param elimGo
	 * @param geneFileXls
	 * @param GOClass GOInfoAbs.GO_BP
	 * @param colAccID
	 * @param colFC
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 */
	protected CtrlGOPath( int QtaxID, boolean blast, double evalue) {
		this.QtaxID = QtaxID;
		this.blast = blast;
		this.evalue = evalue;
	}
	
	/**
	 * 最好第一时间输入
	 * 简单的判断下输入的是geneID还是geneID2Item表
	 * @param fileName
	 */
	public void setLsBG(String fileName)
	{
		boolean flagGeneID = true;
		ArrayList<String[]> lsArrayList = ExcelTxtRead.readLsExcelTxtFile(fileName, 1, 1, 100, -1);//readLsExcelTxt(fileName, 1, 100, 1, -1);
		for (String[] strings : lsArrayList) {
			if (strings.length > 1 && strings[1].contains(",")) {
				flagGeneID = false;
				break;
			}
		}
		if (flagGeneID) {
			setLsBGAccIDsave(fileName);
		}
		else {
			functionTest.setLsBGItem(fileName);
		}
	}
	
	abstract void setLsBGAccIDsave(String  fileName);
	/**
	 * 
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * @param lsAccID2Value  arraylist-string[] 如果 string[2],则第二个为上下调关系，判断上下调
	 * 如果string[1]则不判断上下调
	 * @param up
	 * @param down
	 */
	public void doInBackgroundNorm(ArrayList<String[]> lsAccID2Value, double up, double down) {
		hashResultGene.clear();
		HashMap<String, ArrayList<CopedID>> hashCluster = new LinkedHashMap<String, ArrayList<CopedID>>();
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
				try {
					if (Double.parseDouble(strings[1]) <= down) {
						lsDown.add(copedID);
					}
					else if (Double.parseDouble(strings[1]) >= up) {
						lsUp.add(copedID);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
			hashCluster.put("Up", lsUp);
			hashCluster.put("Down", lsDown);
		}
		
		for (Entry<String, ArrayList<CopedID>> entry : hashCluster.entrySet()) {
			getResult(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public void doInBackgroundCluster(ArrayList<String[]> lsAccID2Value) {
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
	private void getResult(String prix,ArrayList<CopedID>lsCopedIDs)
	{
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
	/**
	 * 是否需要额外的处理文件，不需要就留空
	 * 譬如elimGO需要移动GOMAP等
	 */
	protected abstract void copeFile(String prix, String excelPath);

}
