package com.novelbio.analysis.coexp.simpCoExp;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.database.updatedb.database.UpDateFriceDB;


public class RunCoExp {
	
	public static void main(String[] args) {
		
		String parentFIle = "/home/zong0jie/桌面/";
		long start=System.currentTimeMillis(); //获取最初时间

	try {
			String excelFile = parentFIle+"靶基因交集.xls";
			String outFile = parentFIle+ "靶基因交集VCoexpNoFilter.xls";
			
			
			ExcelOperate excelOperate = new ExcelOperate();
			excelOperate.openExcel(excelFile);
			int ColNum = excelOperate.getColCount(1);
			String[][] aaa = excelOperate.ReadExcel(1, 1, 1, ColNum);
			ColNum = 0;
			for (int i = 0; i < aaa[0].length; i++) {
				if (aaa[0][i]!=null && !aaa[0][i].trim().equals("")) {
					ColNum++;
				}
			}
			int[] columnID = new int[ColNum];
			for (int i = 0; i < ColNum ; i++) {
				columnID[i] = i+1;
			}
			SimpCoExp.getCoExpInfo(excelFile, columnID, 9606, 0.05, outFile, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end=System.currentTimeMillis(); //获取运行结束时间

		System.out.println("程序运行时间： "+(end-start)+"ms"); 
		//   /** 计算degree
//	try {
//			String excelFile = parentFIle+"WFTrgno.xls";
//			String outFile = parentFIle+"CoexpSDegree.xls";
//			SimpCoExp.getCoExpDegree(excelFile,9606,outFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	
}
