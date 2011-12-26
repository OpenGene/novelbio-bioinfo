package com.novelbio.analysis.project.cr;

import java.util.ArrayList;

import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlGO;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlPath2;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.database.model.modgo.GOInfoAbs;

public class Pathway {
	
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	public static void pathAnalysis(int QtaxID, int StaxID, String geneFileXls, String outFile, String backGroundFile) {
		int colAccID = 1;
		int colFC = 2;
		boolean blast = false;
		double evalue = 1e-10;
		boolean elimGo = true;
		CtrlPath2 ctrlPath = null;
		
		ArrayList<String[]> lsAccIDCod = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID, colFC}, 1, 0);
		
		ArrayList<String[]> lsAccIDAll = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID}, 1, 0);
		
		ctrlPath = CtrlPath2.getInstance(geneFileXls, colAccID, colFC, backGroundFile, QtaxID, blast, StaxID, evalue);
//		ctr
		ctrlPath.
		
		ctrlGO.doInBackgroundNorm(lsAccIDAll, 1, -1);
		ctrlGO.saveExcel(outFile);
		
		ctrlGO.doInBackgroundNorm(lsAccIDCod, 1, -1);
		ctrlGO.saveExcel(outFile);
//		AnnoQuery.anno(geneFileXls, QtaxID, colAccID, blast, StaxID, evalue, "");
	}
}
