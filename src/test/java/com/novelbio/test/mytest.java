package com.novelbio.test;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class mytest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String parentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/GO/zhanghuiGO/张辉GOmap 途径分析/优先做";
		ArrayList<String[]> lsExcelFileName = FileOperate.getFoldFileName( parentFile,"*", "xls");
		for (String[] strings : lsExcelFileName) {
			String excelName = parentFile + "/" +strings[0]+"."+strings[1];
			ExcelOperate excelOperate = new ExcelOperate();
			excelOperate.openExcel(excelName);
			int rowNum = excelOperate.getRowCount();
			String[][] str2 = new String[rowNum][1];
			for (int i = 0; i < str2.length; i++) {
				str2[i][0] = "2";
			}
			excelOperate.WriteExcel(1, 2, str2);
			excelOperate.Close();
		}
	}

}
