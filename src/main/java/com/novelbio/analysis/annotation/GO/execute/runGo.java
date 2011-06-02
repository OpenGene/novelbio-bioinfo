package com.novelbio.analysis.annotation.GO.execute;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.GO.queryDB.QBlastGO;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class runGo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//goanalysis2();
		try
		{
		
			 //AgilentIDmodify.getInfo(9823, "/media/winE/Bioinformatics/Agilent/猪/0912_026440_1291690870959/AllAnnotations/026440_D_AA_20100525.txt", 2, "/media/winE/Bioinformatics/Agilent/猪/agilentPig.txt", "Agilent0912");
			// AgilentIDmodify.getInfo(9823, "/media/winE/Bioinformatics/Agilent/猪/0804_020109_1291691130807/AllAnnotations/020109_D_AA_20100525.txt", 2, "/media/winE/Bioinformatics/Agilent/猪/agilentPig2.txt", "Agilent0804");
			//UpDateFriceDB.upDateNCBIID("/media/winE/Bioinformatics/BLAST/result/susAgilent2RefSeqNCBIID.txt", "/media/winE/Bioinformatics/BLAST/result/out");
			//UpDateFriceDB.upDateNCBIID("/media/winE/Bioinformatics/Agilent/猪/agilentPig.txt", "/media/winE/Bioinformatics/Agilent/猪/out2");
			//blastgoanalysis();
//			goanalysisElim();
			goanalysisElimNew();
//			goanalysisNBCNew();
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
	
//	/**
//	 * elimFisher聚类分组GO分析，用R的topGO包
//	 */
//	public static void goanalysisCluster() {
//
//		try {
//			QGenID2GoInfoSepID qGenID2GoInfo=new QGenID2GoInfoSepID();
//			String file="/home/zong0jie/桌面/tmp/cdtmp/LCY/";
//			String geneFile=file+"受试药物趋势.xls";
//			String GOClass = "P";
//			int[] colID = new int[2];colID[0] = 1; colID[1] = 2;
//			String backGroundFile=file+"BG.txt";
//			int QtaxID = 0;
//			boolean blast = false;
//			int StaxID = 9606;
//			double evalue = 1e-10;
//			String resultExcel2003 = file +"受试药物趋势";
// 			GoFisher.getGoRunElim(geneFile, GOClass, colID, backGroundFile, QtaxID, blast, StaxID, evalue, resultExcel2003);
//			System.out.println("ok");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
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
		
		
		
		
		String file="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/GO/";
		String backGroundFile=file + "riceAffyBG.txt";
		String[] prix = new String[2];
		prix[0] = "";
//		prix[0] = "up";
		prix[1] = "down";
		
		try {
			String fileName = "1949个探针";			
			String geneFile=file+fileName+".xls";
			String GOClass = "P";
			int[] colID = new int[2];colID[0] = 1; colID[1] = 1;
			double up = 1;
			double down = -1;
			boolean sepID = false;
			int QtaxID = 39947;
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

		String file="/media/winE/NBC/Project/Microarray_WFL110423/王凤良 基因芯片/王凤良 基因芯片/chip result/GO/";
		String backGroundFile=file + "hg18refseqBGwithDuplication.txt";
		String[] prix = new String[2];
		prix[0] = "532";
		prix[1] = "635";
		try {
			String fileName = "WFL";			
			String geneFile=file+fileName+".xls";
			String GOClass = "P";
			int[] colID = new int[2];colID[0] = 1; colID[1] = 2;
			double up = 1.5;
			double down = 0.667;
			boolean sepID = false;
			int QtaxID = 9606;
			boolean blast = false;
			int StaxID = 9606;
			double evalue = 1e-10;
			String resultExcel2003 = file +fileName+"NBCNewGOCombID2.xls";
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
