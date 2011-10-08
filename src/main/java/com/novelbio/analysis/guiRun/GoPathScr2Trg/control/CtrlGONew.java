package com.novelbio.analysis.guiRun.GoPathScr2Trg.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.HTMLReader.HiddenAction;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.GO.execute.GoFisherNew;
import com.novelbio.analysis.annotation.GO.queryDB.QgeneID2Go;
import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI.CopyOfGUIanalysisSimple;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;

public class CtrlGONew {
	private static final Logger logger = Logger.getLogger(CtrlGONew.class);
	/**
	 * 用单例模式
	 */
	private static CtrlGONew ctrlGO = null;
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
	int StaxID = 0;
	/**
	 * blast的evalue
	 */
	double evalue = 100;

	String geneFileXls = "";
	String GOClass = "";
	int[] colID = new int[2];
	String backGroundFile = "";
	String resultExcel2003 = "";
	double up = -1;
	double down = -1;
	boolean sepID = false;
	boolean elimGo = false;
	String[] prix = new String[2];
	boolean cluster = false;
	/**
	 * 结果
	 */
	ArrayList<ArrayList<String[]>> lsResultUp = null;
	ArrayList<ArrayList<String[]>> lsResultDown = null;
	/**
	 * 界面对象
	 */
	CopyOfGUIanalysisSimple guiBlast;

	public ArrayList<ArrayList<String[]>> getLsResultUp() {
		return lsResultUp;
	}

	public ArrayList<ArrayList<String[]>> getLsResultDown() {
		return lsResultDown;
	}

	public HashMap<String, ArrayList<ArrayList<String[]>>> getHashResult() {
		return hashResultGene;
	}

	/**
	 * 
	 * @param elimGo
	 * @param geneFileXls
	 * @param GOClass
	 * @param colAccID
	 * @param colFC
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public static CtrlGONew getInstance(boolean elimGo, String geneFileXls,
			String GOClass, int colAccID, int colFC, double up, double down,
			String backGroundFile, int QtaxID, boolean blast, int StaxID,
			double evalue) {
		ctrlGO = new CtrlGONew(elimGo, geneFileXls, GOClass, colAccID, colFC,
				up, down, backGroundFile, QtaxID, blast, StaxID, evalue);
		return ctrlGO;
	}

	/**
	 * 
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
	 * @return
	 */
	public static CtrlGONew getInstance(boolean elimGo, String geneFileXls,
			String GOClass, int colAccID, int colFC, String backGroundFile,
			int QtaxID, boolean blast, int StaxID, double evalue) {
		ctrlGO = new CtrlGONew(elimGo, geneFileXls, GOClass, colAccID, colFC,
				backGroundFile, QtaxID, blast, StaxID, evalue);
		return ctrlGO;
	}

	/**
	 * 
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
	private CtrlGONew(boolean elimGo, String geneFileXls, String GOClass,
			int colAccID, int colFC, String backGroundFile, int QtaxID,
			boolean blast, int StaxID, double evalue) {
		this.elimGo = elimGo;
		this.blast = blast;
		this.QtaxID = QtaxID;
		this.StaxID = StaxID;
		this.evalue = evalue;
		this.geneFileXls = geneFileXls;
		this.GOClass = GOClass;
		this.colID[0] = colAccID;
		this.colID[1] = colFC;
		this.backGroundFile = backGroundFile;
		this.up = 0;
		this.down = 0;
		this.cluster = true;
	}

	public static CtrlGONew getCtrlGoUsed() {
		return ctrlGO;
	}

	/**
	 * 
	 * @param elimGo
	 * @param geneFileXls
	 * @param GOClass
	 * @param colAccID
	 * @param colFC
	 * @param up
	 * @param down
	 * @param backGroundFile
	 * @param QtaxID
	 * @param blast
	 * @param StaxID
	 * @param evalue
	 */
	private CtrlGONew(boolean elimGo, String geneFileXls, String GOClass,
			int colAccID, int colFC, double up, double down,
			String backGroundFile, int QtaxID, boolean blast, int StaxID,
			double evalue) {
		this.elimGo = elimGo;
		this.blast = blast;
		this.QtaxID = QtaxID;
		this.StaxID = StaxID;
		this.evalue = evalue;
		this.geneFileXls = geneFileXls;
		this.GOClass = GOClass;
		this.colID[0] = colAccID;
		this.colID[1] = colFC;
		this.backGroundFile = backGroundFile;
		this.up = up;
		this.down = down;
		this.cluster = false;
	}

	public void doInBackGround() {
		try {
			if (cluster) {
				doInBackgroundCluster();
			} else {
				doInBackgroundNormGO();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveFile(String excelPath) {
		if (cluster) {
			saveExcelCluster(excelPath);
		} else {
			saveExcelNorm(excelPath);
		}
	}

	public void saveExcelCluster(String excelPath) {
		this.resultExcel2003 = excelPath;
		ExcelOperate excelResult = new ExcelOperate();
		String filePath = FileOperate.getParentPathName(excelPath) + "/"
				+ FileOperate.getFileNameSep(excelPath)[0];

		if (hashResultGene.size() > 0) {
			for (Entry<String, ArrayList<ArrayList<String[]>>> entry : hashResultGene
					.entrySet()) {
				String key = entry.getKey();
				ArrayList<ArrayList<String[]>> value = entry.getValue();
				excelResult.openExcel(filePath + key + ".xls");
				if (!elimGo) {
					excelResult.WriteExcel(key + "GoAnalysis", 1, 1,
							value.get(0), true);
					excelResult.WriteExcel(key + "Gene2GO", 1, 1, value.get(1),
							true);
					if (blast) {
						excelResult.WriteExcel(key + "GO2Gene", 1, 1,
								value.get(2), true);
					}
				} else {
					excelResult.WriteExcel(prix[0] + "GoAnalysis", 1, 1,
							value.get(0), true);
					excelResult.WriteExcel(prix[0] + "GO2Gene", 1, 1,
							value.get(1), true);
					excelResult.WriteExcel(prix[0] + "Gene2GO", 1, 1,
							value.get(2), true);
					FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP
							+ key, FileOperate.getParentPathName(excelPath),
							FileOperate.getFileNameSep(excelPath)[0] + key
									+ "GoMap.pdf", true);
				}
			}
		}
	}

	HashMap<String, ArrayList<ArrayList<String[]>>> hashResultGene = null;

	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public void doInBackgroundCluster() throws Exception {
		lsResultUp = null;
		lsResultDown = null;
		hashResultGene = new HashMap<String, ArrayList<ArrayList<String[]>>>();
		HashMap<String, ArrayList<String>> hashGene = new HashMap<String, ArrayList<String>>();
		prix[0] = "up";
		prix[1] = "down";
		FileOperate.delAllFile(NovelBioConst.R_WORKSPACE_TOPGO);
		colID[0]--;
		colID[1]--;
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		int rowCount = excelGeneID.getRowCount();
		int colCount = excelGeneID.getColCount(2);
		String[][] geneID = excelGeneID.ReadExcel(2, 1, rowCount, colCount);

		for (int i = 0; i < geneID.length; i++) {
			if (hashGene.containsKey(geneID[i][colID[1]].trim())) {
				ArrayList<String> lsGeneID = hashGene.get(geneID[i][colID[1]]
						.trim());
				lsGeneID.add(geneID[i][colID[0]]);
			} else {
				ArrayList<String> lsGeneID = new ArrayList<String>();
				lsGeneID.add(geneID[i][colID[0]]);
				hashGene.put(geneID[i][colID[1]].trim(), lsGeneID);
			}
		}
		ArrayList<String[]> lsBGIDAll = ExcelTxtRead.getFileToList(
				backGroundFile, 1, "\t");
		ArrayList<String> lsBGID = new ArrayList<String>();
		for (String[] strings : lsBGIDAll) {
			lsBGID.add(strings[0]);
		}
		ArrayList<String[]> lsGeneBG = CopeID.getGenID("BG", lsBGID, QtaxID,
				sepID);
		ArrayList<ArrayList<String[]>> lsBGGenGoInfo = QgeneID2Go.getGenGoInfo(
				lsGeneBG, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		for (Entry<String, ArrayList<String>> entry : hashGene.entrySet()) {
			String key = entry.getKey();
			ArrayList<String> value = entry.getValue();
			ArrayList<String[]> lsGeneTmpCope = CopeID.getGenID(key, value,
					QtaxID, sepID);
			ArrayList<ArrayList<String[]>> lstmp = null;
			try {
				if (elimGo) {
					lstmp = GoFisherNew.getElimFisher(key, lsGeneTmpCope,
							lsBGGenGoInfo, GOClass, sepID, QtaxID, blast,
							StaxID, evalue, 300);
				} else {
					lstmp = GoFisherNew.getNBCFisher(key, lsGeneTmpCope,
							lsBGGenGoInfo, GOClass, sepID, QtaxID, blast,
							StaxID, evalue);
				}
				hashResultGene.put(key, lstmp);
			} catch (Exception e) {
				logger.error("本class没有GO：文件名 " + geneFileXls + "cluster" + key);
			}

		}
	}

	public void saveExcelNorm(String excelPath) {
		this.resultExcel2003 = excelPath;
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(resultExcel2003);
		if (!elimGo) {
			if (lsResultUp.size() > 0) {
				excelResult.WriteExcel(prix[0] + "GoAnalysis", 1, 1,
						lsResultUp.get(0), true);
				excelResult.WriteExcel(prix[0] + "Gene2GO", 1, 1,
						lsResultUp.get(1), true);
				if (blast) {
					excelResult.WriteExcel(prix[0] + "GO2Gene", 1, 1,
							lsResultUp.get(2), true);
				}
			}
			if (lsResultDown.size() > 0) {
				excelResult.WriteExcel(prix[1] + "GoAnalysis", 1, 1,
						lsResultDown.get(0), true);
				excelResult.WriteExcel(prix[1] + "Gene2GO", 1, 1,
						lsResultDown.get(1), true);
				if (blast) {
					excelResult.WriteExcel(prix[0] + "GO2Gene", 1, 1,
							lsResultDown.get(2), true);
				}
			}
		} else {
			if (lsResultUp.size() > 0) {
				excelResult.WriteExcel(prix[0] + "GoAnalysis", 1, 1,
						lsResultUp.get(0), true);
				excelResult.WriteExcel(prix[0] + "GO2Gene", 1, 1,
						lsResultUp.get(1), true);
				excelResult.WriteExcel(prix[0] + "Gene2GO", 1, 1,
						lsResultUp.get(2), true);
				FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP
						+ prix[0], FileOperate.getParentPathName(excelPath),
						FileOperate.getFileNameSep(excelPath)[0] + prix[0]
								+ "GoMap.pdf", true);
			}
			if (lsResultDown.size() > 0) {
				excelResult.WriteExcel(prix[1] + "GoAnalysis", 1, 1,
						lsResultDown.get(0), true);
				excelResult.WriteExcel(prix[1] + "GO2Gene", 1, 1,
						lsResultDown.get(1), true);
				excelResult.WriteExcel(prix[1] + "Gene2GO", 1, 1,
						lsResultDown.get(2), true);
				FileOperate.moveFile(NovelBioConst.R_WORKSPACE_TOPGO_GOMAP
						+ prix[1], FileOperate.getParentPathName(excelPath),
						FileOperate.getFileNameSep(excelPath)[0] + prix[1]
								+ "GoMap.pdf", true);
			}
		}
	}

	/**
	 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public void doInBackgroundNormGO() throws Exception {
		lsResultUp = null;
		lsResultDown = null;
		hashResultGene = null;
		prix[0] = "up";
		prix[1] = "down";
		FileOperate.delAllFile(NovelBioConst.R_WORKSPACE_TOPGO);
		colID[0]--;
		colID[1]--;
		ExcelOperate excelGeneID = new ExcelOperate();
		excelGeneID.openExcel(geneFileXls);
		int rowCount = excelGeneID.getRowCount();
		int colCount = excelGeneID.getColCount(2);
		String[][] geneID = excelGeneID.ReadExcel(2, 1, rowCount, colCount);

		ArrayList<String> lsGeneUp = new ArrayList<String>();
		ArrayList<String> lsGeneDown = new ArrayList<String>();
		for (int i = 0; i < geneID.length; i++) {
			if (geneID[i] == null || geneID[i][colID[0]] == null
					|| geneID[i][colID[0]].trim().equals("")) {
				continue;
			}
			if (colID[0] == colID[1]) {
				lsGeneUp.add(geneID[i][colID[0]]);
				continue;
			}
			if (Double.parseDouble(geneID[i][colID[1]]) <= down) {
				lsGeneDown.add(geneID[i][colID[0]]);
			} else if (Double.parseDouble(geneID[i][colID[1]]) >= up) {
				lsGeneUp.add(geneID[i][colID[0]]);
			}
		}

		ArrayList<String[]> lsBGIDAll = ExcelTxtRead.getFileToList(
				backGroundFile, 1, "\t");
		ArrayList<String> lsBGID = new ArrayList<String>();
		for (String[] strings : lsBGIDAll) {
			lsBGID.add(strings[0]);
		}
		ArrayList<String[]> lsGeneUpCope = CopeID.getGenID(prix[0], lsGeneUp,
				QtaxID, sepID);
		ArrayList<String[]> lsGeneDownCope = CopeID.getGenID(prix[1],
				lsGeneDown, QtaxID, sepID);
		ArrayList<String[]> lsGeneBG = CopeID.getGenID("BG", lsBGID, QtaxID,
				sepID);
		ArrayList<ArrayList<String[]>> lsBGGenGoInfo = QgeneID2Go.getGenGoInfo(
				lsGeneBG, QtaxID, GOClass, sepID, blast, evalue, StaxID);
		if (lsGeneUpCope.size() > 0) {
			if (elimGo)
				lsResultUp = GoFisherNew.getElimFisher(prix[0], lsGeneUpCope,
						lsBGGenGoInfo, GOClass, sepID, QtaxID, blast, StaxID,
						evalue, 300);
			else
				lsResultUp = GoFisherNew.getNBCFisher(prix[0], lsGeneUpCope,
						lsBGGenGoInfo, GOClass, sepID, QtaxID, blast, StaxID,
						evalue);
		}
		if (lsGeneDownCope.size() > 0) {
			if (elimGo)
				lsResultDown = GoFisherNew.getElimFisher(prix[1],
						lsGeneDownCope, lsBGGenGoInfo, GOClass, sepID, QtaxID,
						blast, StaxID, evalue, 300);
			else
				lsResultDown = GoFisherNew.getNBCFisher(prix[1],
						lsGeneDownCope, lsBGGenGoInfo, GOClass, sepID, QtaxID,
						blast, StaxID, evalue);
		}
	}
}
