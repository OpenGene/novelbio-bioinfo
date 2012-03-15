package com.novelbio.analysis.project.zdb;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.nbcgui.controltest.CtrlGO;

public class GOandAnno {

	
	public static void main(String[] args) {
		String BGfile = "/media/winE/Bioinformatics/GenomeData/checken/rsem/all_Gene2Iso.txt";
		String parentFile = ""; String file = "";
		
		
		parentFile = "/media/winF/NBC/Project/Project_FY/chicken/Result/rsem/DEGseq/";
		
		file = parentFile + "KO0vsKO5_anno_ensembl_Filtered.xls";
		goanalysis(9031, 9606, file, 
				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
//		file = parentFile + "Fon4vsQ34.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		
//		file = parentFile + "Q34vsFonM.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		
//		file = parentFile + "QctrvsFon4.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		
//		file = parentFile + "QctrvsFonM.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
//		
//		file = parentFile + "WTvsTF.xls";
//		goanalysis(39947, 3702, file, 
//				FileOperate.changeFileSuffix(file, "_elimGO", "xlsx"), BGfile);
		
		
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
		
		
		
		String GOClass = Go2Term.GO_BP;
		int colAccID = 1;
		int colFC = 4;
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
