package com.novelbio.analysis.project.fy;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.blast.blastRun;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlGO;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlPath;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgo.GOInfoAbs;

public class FunctionTest {
	
	public static void main(String[] args) {
		path();
		
		
	}
	

	private static void annotation()
	{
		int QtaxID = 10090;
		int colAccID = 1;
		String geneFileXls = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/heartK0vsWT/gene_exp.diff";
		anno(QtaxID, 0, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEF2dK0vsWT/gene_exp.diff";
//		anno(QtaxID, 0, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEFK00dvs2d/gene_exp.diff";
//		anno(QtaxID, 0, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEFWT0dvs2d/gene_exp.diff";
//		anno(QtaxID, 0, colAccID, "", geneFileXls);
		
		QtaxID = 9031;
		colAccID = 1;
		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/cuffdiff/gene_exp.diff";
		
//		anno(QtaxID, 0, colAccID, "", geneFileXls);
		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/cuffdiff/cuffDifK0vsWT0/gene_exp.diff";
//		anno(QtaxID, 9606, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/cuffdiff/cuffDifK5vsWT5/gene_exp.diff";
//		anno(QtaxID, 9606, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/cuffdiff/cuffDifK0vsK5/gene_exp.diff";
//		anno(QtaxID, 9606, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/cuffdiff/cuffDifWT0vsWT5/gene_exp.diff";
//		anno(QtaxID, 9606, colAccID, "", geneFileXls);
		
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/chickenK0vsWT0outDifResult.xls";
//		anno(QtaxID, 9606, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsWT5outDifResult.xls";
//		anno(QtaxID, 9606, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/chickenK5vsK0outDifResult.xls";
//		anno(QtaxID, 9606, colAccID, "", geneFileXls);
//		geneFileXls = "/media/winF/NBC/Project/Project_FY/chicken/chickenWT5vsWT0outDifResult.xls";
//		anno(QtaxID, 9606, colAccID, "", geneFileXls);
		
	}
	
	private static void go() {
		int QtaxID = 10090;
		int StaxID = 0;
		String geneFileXls = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEF2dK0vsWT/gene_exp2.csv";
		String outFile = FileOperate.changeFileSuffix(geneFileXls, "_ElimGO", "xlsx");
		String bg = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/mouse_mm9_UCSC_ensembl_BG_Item.txt";
		goanalysis(QtaxID, StaxID, geneFileXls, outFile, bg);

	}
	private static void path() {
		int QtaxID = 10090;
		int StaxID = 0;
		String geneFileXls = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/cufDif/MEF2dK0vsWT/gene_exp2.csv";
		String outFile = FileOperate.changeFileSuffix(geneFileXls, "_Path", "xlsx");
		String bg = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/mouse_mm9_UCSC_ensembl_BG.txt";
		pathwayAnalysis(QtaxID, StaxID, geneFileXls, outFile, bg);

	}
	
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	private static void anno(int QtaxID, int StaxID, int colAccID, String regex, String geneFileXls) {
		boolean blast = true;
		if (StaxID <= 0 ) {
			blast = false;
		}
		String txtOutFile = FileOperate.changeFileSuffix(geneFileXls, "_anno", null);
		AnnoQuery.annoGeneIDXls(geneFileXls, txtOutFile, QtaxID, 1, colAccID, regex, blast, StaxID);
	}
	
	
	
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	public static void goanalysis(int QtaxID, int StaxID, String geneFileXls, String outFile, String backGroundFile) {
		String GOClass = Go2Term.GO_BP;
		int colAccID = 1;
		int colFC = 10;
		boolean blast = true;
		if (StaxID <= 0) {
			blast = false;
		}
		
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
	
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	public static void pathwayAnalysis(int QtaxID, int StaxID, String geneFileXls, String outFile, String backGroundFile) {
		int colAccID = 1;
		int colFC = 10;
		boolean blast = true;
		if (StaxID <= 0) {
			blast = false;
		}
		
		double evalue = 1e-10;
		boolean elimGo = true;
		CtrlPath ctrlGO = null;
		
		ArrayList<String[]> lsAccIDCod = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID, colFC}, 1, 0);
		
		ArrayList<String[]> lsAccIDAll = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID}, 1, 0);
		
		ctrlGO = CtrlPath.getInstance(QtaxID, blast, evalue, StaxID);
		ctrlGO.setLsBG(backGroundFile);
		
		ctrlGO.doInBackgroundNorm(lsAccIDAll, 1, -1);
		ctrlGO.saveExcel(outFile);
		
		ctrlGO.doInBackgroundNorm(lsAccIDCod, 1, -1);
		ctrlGO.saveExcel(outFile);
		
		
//		AnnoQuery.anno(geneFileXls, QtaxID, colAccID, blast, StaxID, evalue, "");
		
		
	}
	
	
	
}
