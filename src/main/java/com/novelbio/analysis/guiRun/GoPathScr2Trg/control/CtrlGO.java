package com.novelbio.analysis.guiRun.GoPathScr2Trg.control;


import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.annotation.GO.execute.GoFisherNew;
import com.novelbio.analysis.annotation.copeID.CopeID;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI.CopyOfGUIanalysisSimple;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;

public class CtrlGO {
	/**
	 * 用单例模式
	 */
	private static CtrlGO ctrlGO = null;
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
		double up= -1;
		double down = -1;
		boolean sepID = false;
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
		public static CtrlGO getInstance(String geneFileXls,String GOClass,int colAccID,int colFC,double up, double down,String backGroundFile,int QtaxID,
				boolean blast, int StaxID,double evalue,CopyOfGUIanalysisSimple guiBlast) {
			ctrlGO = new CtrlGO(geneFileXls, GOClass, colAccID, colFC, up, down, backGroundFile, QtaxID, blast, StaxID, evalue, guiBlast);
			return ctrlGO;
		}
		public static CtrlGO getCtrlGoUsed()
		{
			return ctrlGO;
		}
		/**
		 * 
		 * @param blast
		 * @param taxID
		 * @param StaxID
		 * @param evalue
		 * @param guiBlast
		 */
		private  CtrlGO(String geneFileXls,String GOClass,int colAccID,int colFC,double up, double down,String backGroundFile,int QtaxID,
				boolean blast, int StaxID,double evalue,CopyOfGUIanalysisSimple guiBlast) {
			this.blast = blast;
			this.QtaxID = QtaxID;
			this.StaxID = StaxID;
			this.evalue = evalue;
			this.guiBlast =guiBlast;
			this.geneFileXls = geneFileXls;
			this.GOClass = GOClass;
			this.colID[0] = colAccID;
			this.colID[1] = colFC;
			this.backGroundFile = backGroundFile;
			this.up = up;
			this.down = down;
		}
		
		public void saveExcel(String excelPath) {
			this.resultExcel2003 = excelPath;
			ExcelOperate excelResult = new ExcelOperate();
			excelResult.openExcel(resultExcel2003);
			if (lsResultUp.size() > 0) {
				excelResult.WriteExcel(prix[0]+"GoAnalysis", 1, 1, lsResultUp.get(0), true);
				excelResult.WriteExcel(prix[0]+"Gene2GO", 1, 1,lsResultUp.get(1) , true);
				if (blast) {
					excelResult.WriteExcel(prix[0]+"GO2Gene", 1, 1,lsResultUp.get(2) , true);
				}
			}
			if (lsResultDown.size() > 0) {
				excelResult.WriteExcel(prix[1]+"GoAnalysis", 1, 1, lsResultDown.get(0), true);
				excelResult.WriteExcel(prix[1]+"Gene2GO", 1, 1,lsResultDown.get(1) , true);
				if (blast) {
					excelResult.WriteExcel(prix[0]+"GO2Gene", 1, 1,lsResultDown.get(2) , true);
				}
			}
		}
		/**
		 * 给定文件，和文件分割符，以及第几列，获得该列的基因ID
		 * @param fileName
		 * @return
		 * @throws Exception 
		 */
		public void doInBackground() throws Exception
		{
			lsResultUp = null;
			lsResultDown = null;
			
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
			if (lsGeneUpCope.size()>0) {
				lsResultUp = GoFisherNew.getNBCFisher(prix[0],lsGeneUpCope, lsGeneBG, GOClass, sepID, QtaxID, blast, StaxID, evalue);
			}
			if (lsGeneDownCope.size()>0) {
				lsResultDown = GoFisherNew.getNBCFisher(prix[1],lsGeneDownCope, lsGeneBG, GOClass, sepID, QtaxID, blast, StaxID, evalue);
			}
		}	
}


class ProgressData
{
	public int rowNum;
	public String[] tmpInfo;
}
