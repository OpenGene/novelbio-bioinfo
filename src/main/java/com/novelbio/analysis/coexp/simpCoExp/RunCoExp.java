package com.novelbio.analysis.coexp.simpCoExp;

import com.novelbio.base.dataOperate.ExcelOperate;


public class RunCoExp {
	
	public static void main(String[] args) {
		
		String parentFIle = "/home/zong0jie/����/";
		
		
		
//		long start=System.currentTimeMillis(); //��ȡ���ʱ��
//
//	try {
//			String excelFile = parentFIle+"T&DC Coexpression - ����.xls";
//			String outFile = parentFIle+ "T&DC CoexpressionNoFilter.xls";
//			
//			
//			ExcelOperate excelOperate = new ExcelOperate();
//			excelOperate.openExcel(excelFile);
//			int ColNum = excelOperate.getColCount(1);
//			String[][] aaa = excelOperate.ReadExcel(1, 1, 1, ColNum);
//			ColNum = 0;
//			for (int i = 0; i < aaa[0].length; i++) {
//				if (aaa[0][i]!=null && !aaa[0][i].trim().equals("")) {
//					ColNum++;
//				}
//			}
//			int[] columnID = new int[ColNum];
//			for (int i = 0; i < ColNum ; i++) {
//				columnID[i] = i+1;
//			}
//			SimpCoExp.getCoExpInfo(excelFile, columnID, 9606, 0.05, outFile, false);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		long end=System.currentTimeMillis(); //��ȡ���н���ʱ��
//
//		System.out.println("��������ʱ�䣺 "+(end-start)+"ms");
		
		
//		   ����degree
	try {
			String excelFile = parentFIle+"NB.xls";
			String outFile = parentFIle+"NBDegree.xls";
			SimpCoExp.getCoExpDegree(excelFile,39947,outFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}
