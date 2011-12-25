package com.novelbio.analysis.project.qmaize;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlGO;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgo.GOInfoAbs;

public class GOandAnno {

	
	public static void main(String[] args) {
		String BGfile = "/media/winE/NBC/Project/Project_Q_Lab/tophat/maizeBG_Item.txt";
		String parentFile = ""; String file = "";
		
		
		parentFile = "/media/winE/NBC/Project/Project_Q_Lab/tophat/GO/";
		
		file = parentFile + "1vs0gene_exp.diff.xls";
		goanalysis(4577, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		file = parentFile + "2vs0gene_exp.diff.xls";
		goanalysis(4577, 3702, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		file = parentFile + "3vs0gene_exp.diff.xls";
		goanalysis(4577, 3702, file, 
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
	@SuppressWarnings("deprecation")
	public static void goanalysis(int QtaxID, int StaxID, String geneFileXls, String outFile, String backGroundFile) {
		
		
		
		String GOClass = GOInfoAbs.GO_BP;
		int colAccID = 1;
		int colFC = 5;
		boolean blast = true;
		double evalue = 1e-10;
		boolean elimGo = true;
		CtrlGO ctrlGO = null;
		
		ArrayList<String[]> lsAccIDCod = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID, colFC}, 1, 0);
		
		ArrayList<String[]> lsAccIDAll = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID}, 1, 0);
		
		ctrlGO = CtrlGO.getInstance(elimGo, GOClass, QtaxID, blast, evalue, StaxID);
		ctrlGO.setLsBG(backGroundFile);
		
		ctrlGO.doInBackgroundNorm(lsAccIDAll, 0.9, -0.9);
		ctrlGO.saveExcel(outFile);
		
		ctrlGO.doInBackgroundNorm(lsAccIDCod, 0.9, -0.9);
		ctrlGO.saveExcel(outFile);
		
		
//		AnnoQuery.anno(geneFileXls, QtaxID, colAccID, blast, StaxID, evalue, "");
		
		
	}
}
