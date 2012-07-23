package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class XiaoBai {
	public static void main(String[] args) {
		overlap();
	}
	public static void overlap() {
		String excelFile = "/home/zong0jie/桌面/新建文件夹 (3)/company.xls";
		String queryFile = "/home/zong0jie/桌面/新建文件夹 (3)/Sample.xls";
		String outFile = "/home/zong0jie/桌面/新建文件夹 (3)/out.xls";
		
		HashMap<String, String> mapID2Factory = new HashMap<String, String>();
		ArrayList<String[]> lsExcel = ExcelTxtRead.readLsExcelTxt(excelFile, 1);
		ArrayList<String[]> lsQuery = ExcelTxtRead.readLsExcelTxt(queryFile, 1);
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		
		for (String[] strings : lsExcel) {
			mapID2Factory.put(strings[0].trim().toLowerCase(), strings[1]);
		}
		for (String[] strings : lsQuery) {
			String[] result = new String[2];
			result[0] = strings[0].trim();
			result[1] = mapID2Factory.get(result[0].toLowerCase());
			txtOut.writefileln(result);
		}
		txtOut.close();
	}


}
