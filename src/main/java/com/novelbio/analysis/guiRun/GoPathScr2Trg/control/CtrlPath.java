package com.novelbio.analysis.guiRun.GoPathScr2Trg.control;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.GO.execute.GoFisherNew;
import com.novelbio.analysis.annotation.pathway.kegg.kGpath.PathEnrichNew;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI.CopyOfGUIanalysisSimple;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopeID;

public class CtrlPath {
	private static final Logger logger = Logger.getLogger(CtrlPath.class);
	/**
	 * 用单例模式
	 */
	private static CtrlPath ctrlPath = null;
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
		int[] colID = new int[2];
		String backGroundFile = "";
		String resultExcel2003 = "";
		double up= -1;
		double down = -1;
		boolean sepID = false;
		boolean cluster = false;
		
		String[] prix = new String[2];
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
		/**
		 * @param blast
		 * @param taxID
		 * @param StaxID
		 * @param evalue
		 * @param guiBlast
		 */
		public static CtrlPath getInstance(String geneFileXls,int colAccID,int colFC,double up, double down,String backGroundFile,int QtaxID,
				boolean blast, int StaxID,double evalue) {
			ctrlPath = new CtrlPath(geneFileXls, colAccID, colFC, up, down, backGroundFile, QtaxID, blast, StaxID, evalue);
			return ctrlPath;
		}
		
		/**
		 * 
		 * @param geneFileXls
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
		public static CtrlPath getInstance(String geneFileXls,int colAccID,int colFC,String backGroundFile,int QtaxID,
				boolean blast, int StaxID,double evalue) {
			ctrlPath = new CtrlPath(geneFileXls, colAccID, colFC, backGroundFile, QtaxID, blast, StaxID, evalue);
			return ctrlPath;
		}
		
		
		
		
		
		public static CtrlPath getCtrlPathUsed()
		{
			return ctrlPath;
		}
		/**
		 * 
		 * @param geneFileXls
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
		private  CtrlPath(String geneFileXls,int colAccID,int colFC,double up, double down,String backGroundFile,int QtaxID,
				boolean blast, int StaxID,double evalue) {
			this.blast = blast;
			this.QtaxID = QtaxID;
			this.StaxID = StaxID;
			this.evalue = evalue;
			this.geneFileXls = geneFileXls;
			this.colID[0] = colAccID;
			this.colID[1] = colFC;
			this.backGroundFile = backGroundFile;
			this.up = up;
			this.down = down;
			this.cluster = false;
		}
		/**
		 * 
		 * @param geneFileXls
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
		private  CtrlPath(String geneFileXls,int colAccID,int colFC,String backGroundFile,int QtaxID,
				boolean blast, int StaxID,double evalue) {
			this.blast = blast;
			this.QtaxID = QtaxID;
			this.StaxID = StaxID;
			this.evalue = evalue;
			this.geneFileXls = geneFileXls;
			this.colID[0] = colAccID;
			this.colID[1] = colFC;
			this.backGroundFile = backGroundFile;
			this.cluster = true;
		}
		HashMap<String,ArrayList<ArrayList<String[]>>> hashResultGene = null;
		public HashMap<String,ArrayList<ArrayList<String[]>>> getHashResult() {
			return hashResultGene;
		}
		public void doInBackground()
		{
			try {
				if (cluster) {
					doInBackgroundCluster();
				}
				else {
					doInBackgroundNorm();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void save(String excelPath)
		{
			if (cluster) {
				saveExcelCluster(excelPath);
			}
			else {
				saveExcelNorm(excelPath);
			}
		}
		
		
		/**
		 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
		 * @param fileName
		 * @return
		 * @throws Exception 
		 */
		public void doInBackgroundCluster() throws Exception
		{
			lsResultUp = null;
			lsResultDown = null;
			hashResultGene = new HashMap<String, ArrayList<ArrayList<String[]>>>();
			HashMap<String, ArrayList<String>> hashGene = new HashMap<String, ArrayList<String>>();			
			prix[0] = "up";
			prix[1] = "down";
			FileOperate.delAllFile(NovelBioConst.R_WORKSPACE_TOPGO);
			colID[0]--;colID[1]--;
			ExcelOperate excelGeneID = new ExcelOperate();
			excelGeneID.openExcel(geneFileXls);
			int rowCount = excelGeneID.getRowCount();
			int colCount = excelGeneID.getColCount(2);
			String[][] geneID = excelGeneID.ReadExcel(2, 1,rowCount, colCount);
			
			for (int i = 0; i < geneID.length; i++) {
				if (hashGene.containsKey(geneID[i][colID[1]].trim())) {
					ArrayList<String> lsGeneID = hashGene.get(geneID[i][colID[1]].trim());
					lsGeneID.add(geneID[i][colID[0]]);
				}
				else {
					ArrayList<String> lsGeneID = new ArrayList<String>();
					lsGeneID.add(geneID[i][colID[0]]);
					hashGene.put(geneID[i][colID[1]].trim(), lsGeneID);
				}
			}
			ArrayList<String[]> lsBGIDAll = ExcelTxtRead.getFileToList(backGroundFile, 1, "\t");
			ArrayList<String> lsBGID = new ArrayList<String>();
			for (String[] strings : lsBGIDAll) {
				lsBGID.add(strings[0]);
			}
			ArrayList<String[]> lsGeneBG = CopeID.getGenID("BG",lsBGID, QtaxID,sepID);
			HashMap<String, ArrayList<String[]>> hashBGgene = PathEnrichNew.getPath2Keg(lsGeneBG,QtaxID,blast,StaxID,evalue);
			int GeneBGNum = PathEnrichNew.gethashGeneNum().size();
			for(Entry<String, ArrayList<String>> entry:hashGene.entrySet())
			{
				String key = entry.getKey();
				ArrayList<String> value = entry.getValue();
				ArrayList<String[]> lsGeneTmpCope = CopeID.getGenID(key,value, QtaxID,sepID);
				ArrayList<ArrayList<String[]>> lstmp = null;
				lstmp = PathEnrichNew.getPathEnrich(key, lsGeneTmpCope, hashBGgene, sepID, QtaxID, blast, StaxID, evalue,GeneBGNum);
				if (lstmp != null) {
					hashResultGene.put(key, lstmp);
				}
				else {
					logger.error("本class没有Path：文件名 "+geneFileXls+"cluster"+ key );
				}
			}
		}
		public void saveExcelCluster(String excelPath) {
			this.resultExcel2003 = excelPath;
			ExcelOperate excelResult = new ExcelOperate();
			String filePath = FileOperate.getParentPathName(excelPath) + "/"
					+ FileOperate.getFileNameSep(excelPath)[0];

			if (hashResultGene.size() > 0) {
				for (Entry<String, ArrayList<ArrayList<String[]>>> entry : hashResultGene.entrySet()) {
					String key = entry.getKey();
					ArrayList<ArrayList<String[]>> value = entry.getValue();
					excelResult.openExcel(filePath + key + ".xls");
					excelResult.WriteExcel(key+"PathAnalysis", 1, 1, value.get(0));
					excelResult.WriteExcel(key+"Gene2Path", 1, 1,value.get(1));
				}
			}
		}
		
		public void saveExcelNorm(String excelPath) {
			this.resultExcel2003 = excelPath;
			ExcelOperate excelResult = new ExcelOperate();
			excelResult.openExcel(resultExcel2003);
			if (lsResultUp.size() > 0) {
				excelResult.WriteExcel(prix[0]+"PathAnalysis", 1, 1, lsResultUp.get(0));
				excelResult.WriteExcel(prix[0]+"Gene2Path", 1, 1,lsResultUp.get(1) );
			}
			if (lsResultDown.size() > 0) {
				excelResult.WriteExcel(prix[1]+"PathAnalysis", 1, 1, lsResultDown.get(0));
				excelResult.WriteExcel(prix[1]+"Gene2Path", 1, 1,lsResultDown.get(1) );
			}
		}
		/**
		 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
		 * @param fileName
		 * @return
		 * @throws Exception 
		 */
		public void doInBackgroundNorm() throws Exception
		{
			lsResultUp = null;
			lsResultDown = null;
			hashResultGene = null;
			prix[0] = "up";
			prix[1] = "down";

			
			colID[0]--;colID[1]--;
			ExcelOperate excelGeneID = new ExcelOperate();
			excelGeneID.openExcel(geneFileXls);
			int rowCount = excelGeneID.getRowCount();
			int colCount = excelGeneID.getColCount(2);
			String[][] geneID = excelGeneID.ReadExcel(2, 1,rowCount, colCount);
			
			ArrayList<String> lsGeneUp = new ArrayList<String>();
			ArrayList<String> lsGeneDown = new ArrayList<String>();
			for (int i = 0; i < geneID.length; i++) {
				if (geneID[i][colID[0]] == null || geneID[i][colID[0]].trim().equals("")) {
					continue;
				}
				if (colID[0] == colID[1]) {
					lsGeneUp.add(geneID[i][colID[0]]);
					continue;
				}
				if (Double.parseDouble(geneID[i][colID[1]])<=down) {
					lsGeneDown.add(geneID[i][colID[0]]);
				}
				else if (Double.parseDouble(geneID[i][colID[1]])>=up) {
					lsGeneUp.add(geneID[i][colID[0]]);
				}
			}
			
			ArrayList<String[]> lsBGIDAll = ExcelTxtRead.getFileToList(backGroundFile, 1, "\t");
			ArrayList<String> lsBGID = new ArrayList<String>();
			for (String[] strings : lsBGIDAll) {
				lsBGID.add(strings[0]);
			}
			ArrayList<String[]> lsGeneUpCope = CopeID.getGenID(prix[0],lsGeneUp, QtaxID,sepID);
			ArrayList<String[]> lsGeneDownCope = CopeID.getGenID(prix[1],lsGeneDown, QtaxID,sepID);
			ArrayList<String[]> lsGeneBG = CopeID.getGenID("BG",lsBGID, QtaxID,sepID);
			HashMap<String, ArrayList<String[]>> hashBGgene = PathEnrichNew.getPath2Keg(lsGeneBG,QtaxID,blast,StaxID,evalue);
			int GeneBGNum = PathEnrichNew.gethashGeneNum().size();
			if (lsGeneUpCope.size()>0) {
				lsResultUp = PathEnrichNew.getPathEnrich(prix[0],lsGeneUpCope, hashBGgene, sepID, QtaxID, blast, StaxID, evalue,GeneBGNum);
			}
			if (lsGeneDownCope.size()>0) {
				lsResultDown = PathEnrichNew.getPathEnrich(prix[1],lsGeneDownCope, hashBGgene, sepID, QtaxID, blast, StaxID, evalue,GeneBGNum);
			}
		}
}
