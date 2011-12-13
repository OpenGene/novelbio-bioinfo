package com.novelbio.analysis.guiRun.GoPathScr2Trg.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI.CopyOfGUIanalysisSimple;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modgo.GOInfoAbs;

public class CtrlGO {

	private static final Logger logger = Logger.getLogger(CtrlGO.class);
	/**
	 * 用单例模式
	 */
	private static CtrlGO ctrlGO = null;
	
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
	 * @param GOClass GOInfoAbs.GO_BP
	 * @param QtaxID
	 * @param blast
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public static CtrlGO getInstance(boolean elimGo, String GOClass, int QtaxID, boolean blast, double evalue, int... StaxID) {
		ctrlGO = new CtrlGO(elimGo, GOClass, QtaxID, blast, evalue, StaxID);
		return ctrlGO;
	}
	/**
	 * 返回已有的GtrlGO
	 * @return
	 */
	public static CtrlGO getInstance() {
		return ctrlGO;
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
	private CtrlGO(boolean elimGo, String GOClass, int QtaxID, boolean blast,
			double evalue, int... StaxID) {
		this.elimGo = elimGo;
		this.QtaxID = QtaxID;
		this.GOClass = GOClass;
		this.blast = blast;
		this.evalue = evalue;
		if (elimGo) {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_ELIM,
					QtaxID, blast, evalue, StaxID);
		} else {
			functionTest = new FunctionTest(FunctionTest.FUNCTION_GO_NOVELBIO,
					QtaxID, blast, evalue, StaxID);
		}
		functionTest.setGOtype(GOClass);
	}
	
	public void setLsTestID(ArrayList<String> lsAccID) {
		functionTest.setLsTestAccID(lsAccID);
	}

	/**
	 * 最好第一时间输入
	 * 简单的判断下输入的是geneID还是geneID2Item表
	 * @param fileName
	 */
	public void setLsBG(String fileName)
	{
		boolean flagGeneID = true;
		ArrayList<String[]> lsArrayList = ExcelTxtRead.readLsExcelTxt(fileName, 1, 100, 1, -1);
		for (String[] strings : lsArrayList) {
			if (strings.length > 1 && strings[1].contains(",")) {
				flagGeneID = false;
				break;
			}
		}
		if (flagGeneID) {
			functionTest.setLsBGAccID(fileName, 1,FileOperate.changeFileSuffix(fileName, "_Item", "txt"));
		}
		else {
			functionTest.setLsBGItem(fileName);
		}
	}
	
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
			getResult( functionTest, entry.getKey(), entry.getValue());
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
	private void getResult(FunctionTest functionTest, String prix,ArrayList<CopedID>lsCopedIDs)
	{
		functionTest.setLsTest(lsCopedIDs);
		ArrayList<String[]> lsResultTest = functionTest.getTestResult();
		if (lsResultTest == null) {
			return;
		}
		
		String[] title = new String[10];
		title[0] = "GOID"; title[1] = "GOTerm";
		title[2] = "DifGene"; title[3] = "AllDifGene"; title[4] = "GeneInGOID"; title[5] = "AllGene";
		title[6] = "P-Value"; title[7] = "FDR"; title[8] = "Enrichment"; title[9] = "(-log2P)";
		lsResultTest.add(0,title);
		
		LinkedHashMap<String, ArrayList<String[]>> hashResult = new LinkedHashMap<String, ArrayList<String[]>>();
		hashResult.put("GO_Result", lsResultTest);
		FileOperate.changeFileSuffixReal(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, "_"+prix, null);
		if (elimGo) {
			ArrayList<String[]> lsGene2GO = functionTest.getGene2Item();
			hashResult.put("Gene2GO", lsGene2GO);
			
			ArrayList<String[]> lsGO2Gene = functionTest.getItem2GenePvalue();
			hashResult.put("GO2Gene", lsGO2Gene);
			
		}
		else {
			ArrayList<String[]> lsGene2GOPvalue = functionTest.getGene2ItemPvalue();
			hashResult.put("Gene2GO", lsGene2GOPvalue);
		}
		hashResultGene.put(prix, hashResult);
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
				FileOperate.moveFile(FileOperate.changeFileSuffix(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP, "_"+prix, null),
						FileOperate.getParentPathName(excelPath), FileOperate.getFileNameSep(excelPath)[0] + prix + "GoMap.pdf", true);
			}
		}
	}

}
