package com.novelbio.analysis.project.zdb;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlGO;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgo.GOInfoAbs;

public class GOandAnno {

	
	public static void main(String[] args) {
		String BGfile = "/media/winE/Bioinformatics/GenomeData/Rice/RiceAffyBG2GOBlast.txt";
		String parentFile = ""; String file = "";
		
		
		parentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/HY2011xinpian/HY_filter/";
		
		file = parentFile + "HYM32 vs 11.xls";
		goanalysis(39947, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		file = parentFile + "HYM1 vs11.xls";
		goanalysis(39947, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		file = parentFile + "HYa1_6 vsM1.xls";
		goanalysis(39947, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		file = parentFile + "HYa1_6 vs11.xls";
		goanalysis(39947, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		file = parentFile + "HYa1_3 vs M1.xls";
		goanalysis(39947, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		file = parentFile + "HYa1_3vs1-6.xls";
		goanalysis(39947, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		file = parentFile + "HYa1_3vs11.xls";
		goanalysis(39947, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
//		anno(39947, 3702, file, FileOperate.changeFileSuffix(file, "_anno", null));
//		
//		parentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/THX/";
//		file = parentFile + "mtr45vswt_rma.xls";
//		
//		anno(39947, 3702, file, FileOperate.changeFileSuffix(file, "_anno", null));
//		
//		
//		parentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/THX/";
//		file = parentFile + "mtr35vswt_rma.xls";
//		
//		anno(39947, 3702, file, FileOperate.changeFileSuffix(file, "_anno", null));
		
	}
	

	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	public static void anno(int QtaxID, int StaxID, String geneFileXls, String outFile) {
		boolean blast = true;
		double evalue = 1e-10;
		int colAccID = 1;
		
		AnnoQuery.anno(geneFileXls, QtaxID, colAccID, blast, StaxID, evalue, "");
		
		
	}
	
	
	
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	public static void goanalysis(int QtaxID, int StaxID, String geneFileXls, String outFile, String backGroundFile) {
		
		
		
		String GOClass = GOInfoAbs.GO_BP;
		int colAccID = 1;
		int colFC = 2;
		boolean blast = true;
		double evalue = 1e-10;
		boolean elimGo = true;
		CtrlGO ctrlGO = null;
		
		ArrayList<String[]> lsAccIDCod = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID, colFC}, 1, 0);
		
		ArrayList<String[]> lsAccIDAll = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID}, 1, 0);
		
		ctrlGO = CtrlGO.getInstance(elimGo, GOClass, QtaxID, blast, evalue, StaxID);
		ctrlGO.setLsBG(backGroundFile);
		
		ctrlGO.doInBackgroundNorm(lsAccIDAll, 1, -1);
		ctrlGO.saveExcel(outFile);
		
		ctrlGO.doInBackgroundNorm(lsAccIDCod, 1, -1);
		ctrlGO.saveExcel(outFile);
		
		
//		AnnoQuery.anno(geneFileXls, QtaxID, colAccID, blast, StaxID, evalue, "");
		
		
	}
}
