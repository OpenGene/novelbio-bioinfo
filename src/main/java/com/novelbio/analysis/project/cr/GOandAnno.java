package com.novelbio.analysis.project.cr;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.nbcgui.controltest.CtrlGO;

public class GOandAnno {

	
	public static void main(String[] args) {
		String BGfile = "/media/winE/Bioinformatics/GenomeData/human/UCSChg19RefseqDuplicateID_Item.txt";
		String parentFile = ""; String file = "";
		
		
		parentFile = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/GO/";
		
		file = parentFile + "E6dvsE6dn.xls";
		goanalysis(9606, 9606, file, 
				FileOperate.changeFileSuffix(file, "_elimGO2", "xlsx"), BGfile);
		
		file = parentFile + "E6dvsNS6d.xls";
		goanalysis(9606, 9606, file, 
				FileOperate.changeFileSuffix(file, "_elimGO2", "xlsx"), BGfile);
		
 
		
	}
	

	
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	public static void goanalysis(int QtaxID, int StaxID, String geneFileXls, String outFile, String backGroundFile) {
		
		
		
		String GOClass = GOInfoAbs.GO_BP;
		int colAccID = 1;
		int colFC = 2;
		boolean blast = false;
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
