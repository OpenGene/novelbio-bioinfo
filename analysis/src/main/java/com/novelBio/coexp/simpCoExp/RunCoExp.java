package com.novelBio.coexp.simpCoExp;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.upDateDB.dataBase.UpDateFriceDB;


public class RunCoExp {
	
	public static void main(String[] args) {
		
		String parentFIle = "/media/winE/NBC/Project/Microarray_QY110318/coexpression/";

//	try {
//			String excelFile = parentFIle+"����������V.xls";
//			String outFile = parentFIle+ "����������VCoexp.xls";
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

		//   /** ����degree
	try {
			String excelFile = parentFIle+"����������SCoexpFilter.xls";
			String outFile = parentFIle+"CoexpSDegree.xls";
			SimpCoExp.getCoExpDegree(excelFile,9606,outFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String excelFile = parentFIle+"����������VCoexpFilter.xls";
			String outFile = parentFIle+"CoexpVDegree.xls";
			SimpCoExp.getCoExpDegree(excelFile,9606,outFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
}