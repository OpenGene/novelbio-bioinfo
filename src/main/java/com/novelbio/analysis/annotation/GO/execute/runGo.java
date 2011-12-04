package com.novelbio.analysis.annotation.GO.execute;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.GO.queryDB.QBlastGO;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlGO;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.database.model.modgo.GOInfoAbs;

public class runGo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//goanalysis2();
		try
		{
			goanalysis(39947, 3702, "/media/winE/NBC/Project/Project_ZDB_Lab/THX/mtr45vswt_rma_filter.xls", 
					"/media/winE/NBC/Project/Project_ZDB_Lab/THX/GO//mtr45vswt.xlsx");
			goanalysis(39947, 3702, "/media/winE/NBC/Project/Project_ZDB_Lab/THX/mtr55vswt_rma_filter.xls", 
					"/media/winE/NBC/Project/Project_ZDB_Lab/THX/GO//mtr55vswt.xlsx");
			
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
	
	/**
	 * elimFisher聚类分组GO分析，用R的topGO包
	 */
	public static void goanalysis(int QtaxID, int StaxID, String geneFileXls, String outFile) {
		String GOClass = "P";
		int colAccID = 1;
		int colFC = 2;
		String backGroundFile = "/media/winE/Bioinformatics/GenomeData/Rice/RiceAffyBG2GOBlast.txt";
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
//	
//	/**
//	 * 其明聚类分组GO分析，用其明算法
//	 */
//	public static void goanalysisClusterQM() {
//
//		try {
//			QGenID2GoInfoSepID qGenID2GoInfo=new QGenID2GoInfoSepID();
//			String file="/media/winE/NBC/Project/ChIPSeq_CDG101101/result/GO/";
//			String geneFile=file+"WYRmR1Allpvalue0.001Maxpilup20FR.xls";
//			String GOClass = "P";
//			String taxIDfile = file+"taxID.txt";
//			int[] colID = new int[2];colID[0] = 1; colID[1] = 2;
//			String backGroundFile=file+"BG.txt";
//			int QtaxID = 0;
//			boolean blast = false;
//			int StaxID = 9606;
//			double evalue = 1e-10;
//			String resultExcel2003 = file +"阳性药物趋势";
////			GoFisher.getGoRunQM(geneFile, GOClass, colID, backGroundFile, taxIDfile, false, resultExcel2003);
//			System.out.println("ok");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
	/**
	 * 常规GO分析，用R的topGO包
	 */
	public static void goanalysisElim() {

		String file="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/GO/";
		String backGroundFile=file + "AffyPigBG.txt";
		try {
			String fileName = "CSAnovelbio_annotationFiltered";			
			String geneFile=file+fileName+".xls";
			String GOClass = "P";
			int[] colID = new int[2];colID[0] = 1; colID[1] = 6;
			
			int QtaxID = 0;
			boolean blast = false;
			int StaxID = 9606;
			double evalue = 1e-10;
			String resultExcel2003 = file +fileName+"GOanalysis.xls";
			GoFisher.getGoRunElim(geneFile, GOClass, colID, 1.5, 0.667, backGroundFile, QtaxID, blast, StaxID, evalue, resultExcel2003);
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 常规GO分析，用R的topGO包
	 */
	public static void goanalysisElimNew() {

//try {
//	String file="/media/winE/NBC/Project/Project_ZDB_Lab/XCX/GO";
//	String backGroundFile=file + "/riceAffyBG.txt";
//	ArrayList<String[]> lsExcelFileName = FileOperate.getFoldFileName( file,"*", "xls");
//	String[] prix = new String[2];
////	prix[0] = "Cotex KO VS WT";
//	prix[0] = "";
//	prix[1] = "down";
//	for (String[] strings : lsExcelFileName) {
//		try {
//		String excelName = file + "/" +strings[0]+"."+strings[1];
//		String GOClass = "P";
//		int[] colID = new int[2];colID[0] = 1; colID[1] = 1;
//		double up = 2;
//		double down = -2;
//		boolean sepID = false;
//		int QtaxID = 39947;
//		boolean blast = false;
//		int StaxID = 9606;
//		double evalue = 1e-10;
//		String resultExcel2003 = file +"/"+strings[0]+"elimGOCombID.xls";
//		String resultGoMap = file +"/"+ strings[0]+"GOmap";
//		GoFisherNew.getGoRunElim(excelName, sepID, GOClass, colID, up, down, backGroundFile, QtaxID, blast, StaxID, evalue, resultExcel2003,resultGoMap, prix,300);
//		System.out.println("ok");
//	} catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	
//	
//	}
//	
//} catch (Exception e) {
//	e.printStackTrace();
//}
//
//		
		
		
		
		
		String file="/media/winE/NBC/Project/Project_CDG_Lab/Microarray_XLY110623/";
		String backGroundFile=file + "mouseAffyBG.txt";
		String[] prix = new String[2];
		prix[0] = "up";
//		prix[0] = "up";
		prix[1] = "down";
		
		try {
			String fileName = "Leg fc2";			
			String geneFile=file+fileName+".xls";
			String GOClass = "P";
			int[] colID = new int[2];colID[0] = 1; colID[1] = 6;
			double up = 1.9;
			double down = 0.6;
			boolean sepID = false;
			int QtaxID = 10090;
			boolean blast = false;
			int StaxID = 9606;
			double evalue = 1e-10;
			String resultExcel2003 = file +fileName+"elimGOCombID.xls";
			String resultGoMap = file + fileName+"GOmap";
			GoFisherNew.getGoRunElim(geneFile, sepID, GOClass, colID, up, down, backGroundFile, QtaxID, blast, StaxID, evalue, resultExcel2003,resultGoMap, prix,300);
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * 常规GO分析，用R的topGO包
	 */
	public static void goanalysisNBCNew() {

		String file = "/media/winE/NBC/Project/MiRNA_DrZ110701/";
		String backGroundFile=file + "UCSChg19RefseqDuplicateID.txt";
		String[] prix = new String[2];
		prix[0] = "";
		prix[1] = "635";
		try {
			String fileName = "Targetscan";
			String geneFile=file+fileName+".xls";
			String GOClass = "P";
			int[] colID = new int[2];colID[0] = 1; colID[1] = 2;
			double up = 1;
			double down = -1;
			boolean sepID = false;
			int QtaxID = 9606;
			boolean blast = false;
			int StaxID = 9606;
			double evalue = 1e-10;
			String resultExcel2003 = file +fileName+"NBCNewGOCombID.xls";
			GoFisherNew.getGoRunNBC(geneFile, sepID, GOClass, colID, up, down, backGroundFile, QtaxID, blast, StaxID, evalue, resultExcel2003, prix);
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
//	/**
//	 * 常规Blast2GO分析，用R的topGO包
//	 */
//	public static void Blast2Goanalysis() {
//
//		try {
//			QBlastGO qBlastGO=new QBlastGO();
//			String file="/media/winE/NBC/Project/";
//			String geneFile=file+"targetGene.xls";
//			String backGroundFile=file+"down.txt";
//			String taxIDfile=file+"taxID.txt";
//			String resultGeneGofile = file +"down2Go.txt";
//			String resultBGGofile = file + "BG2Go.txt";
//			String resultGeneIDfile = file + "downRgeneID.txt";
//			qBlastGO.goAnalysis(geneFile, backGroundFile, taxIDfile, resultGeneGofile, resultBGGofile, resultGeneIDfile);
//			System.out.println("ok");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	
	
//	/**
//	 * 陈岱的建议GO方法
//	 */
//	public static void goanalysisQM() {
// 
//		try {
//			String filePath="/media/winE/NBC/Project/Microarray_QY110318/";
//			String fileName = "QY-DIFGENE";
//			String geneFileXls=filePath+fileName+".xls";
//			String resultfile = filePath +fileName+"QMGO.xls";
//			String backGroundFile=filePath+"Human U133 Plus 2.0 BG3.txt";
//			int QtaxID = 9606;
//			String GOClass = "P";
//			int[] colID = new int[2];colID[0] = 1; colID[1] = 8;
//			double up = 2; double down = 0.5;
//			boolean blast = false;
//			int StaxID = 9606;
//			double evalue = 1e-10;
//			GoFisherNew.getGoRunNBC(QtaxID, geneFileXls, GOClass, colID, up, down, backGroundFile, resultfile, blast, StaxID, evalue, true);
//			System.out.println("ok");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}

}
