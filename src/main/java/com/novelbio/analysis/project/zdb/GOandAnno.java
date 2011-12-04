package com.novelbio.analysis.project.zdb;

import java.util.ArrayList;

import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlGO;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;

public class GOandAnno {

	
	public static void main(String[] args) {
		String BGfile = "/media/winE/Bioinformatics/GenomeData/Rice/RiceAffyBG2GOBlast.txt";
		String parentFile = ""; String file = "";
		
		
		parentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/FZZ/compareFilter/";
		file = parentFile + "YLFonvsG368.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		file = parentFile + "YLFonvswt.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		file = parentFile + "YLG368vswt.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		file = parentFile + "YLGFvsFon.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		file = parentFile + "YLGFvsG368.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		file = parentFile + "YLGFvswt.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
//		parentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/FZZ/compareFilter/";
//		file = parentFile + "NFzz_GA2vsWT2.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		file = parentFile + "NFzz_GA3vsWT3.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		file = parentFile + "NFzz_TF2vsWT2.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		file = parentFile + "NFzz_TF3vsWT3.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		parentFile = "/home/zong0jie/桌面/";
		file = parentFile + "wwf_1201.xlsx";
		goanalysis(39947, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
	}
	
	
	
	
	
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	public static void goanalysis(int QtaxID, int StaxID, String geneFileXls, String outFile, String backGroundFile) {
		String GOClass = "P";
		int colAccID = 1;
		int colFC = 1;
		boolean blast = true;
		double evalue = 1e-10;
		boolean elimGo = true;
		CtrlGO ctrlGO = null;
		
		ArrayList<String[]> lsAccID = null;
		if (colAccID != colFC)
			 lsAccID = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID, colFC}, 1, 0);
		else
			lsAccID = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID}, 1, 0);
		
		ctrlGO = CtrlGO.getInstance(elimGo, GOClass, QtaxID, blast, evalue, StaxID);
		ctrlGO.setLsBG(backGroundFile);
		
		ctrlGO.doInBackgroundNorm(lsAccID, 1, -1);
		ctrlGO.saveExcel(outFile);
		
	}
}
