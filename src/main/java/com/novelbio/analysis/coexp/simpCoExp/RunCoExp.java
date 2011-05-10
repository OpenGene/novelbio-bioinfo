package com.novelbio.analysis.coexp.simpCoExp;

import com.novelbio.analysis.upDateDB.dataBase.UpDateFriceDB;
import com.novelbio.base.dataOperate.ExcelOperate;


public class RunCoExp {
	
	public static void main(String[] args) {
		
		String parentFIle = "/media/winE/NBC/Project/Microarray_WFL110423/王凤良 基因芯片/王凤良 基因芯片/chip result/src2trg/";

//	try {
//			String excelFile = parentFIle+"共表达数据V.xls";
//			String outFile = parentFIle+ "共表达数据VCoexp.xls";
//			
//			
//			ExcelOperate excelOperate = new ExcelOperate();
//			excelOperate.openExcel(excelFile);
//			int ColNum = excelOperate.getColCount(1);
//			String[][] aaa = excelOperate.ReadExcel(1, 1, 1, ColNum);
//			ColNum = 0;int m = aaa[0].length;
//			for (int i = 0; i < aaa[0].length; i++) {
//				if (aaa[0][i]!=null && !aaa[0][i].trim().equals("")) {
//					ColNum++;
//				}
//			}
//			int[] columnID = new int[ColNum];
//			for (int i = 0; i < ColNum ; i++) {
//				columnID[i] = i+1;
//			}
//			SimpCoExp.getCoExpInfo(excelFile, columnID, 9606, 0.05, 0.995, 2, outFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		//   /** 计算degree
	try {
			String excelFile = parentFIle+"WFTrgno.xls";
			String outFile = parentFIle+"CoexpSDegree.xls";
			SimpCoExp.getCoExpDegree(excelFile,9606,outFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
