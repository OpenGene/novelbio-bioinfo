package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

public class XiaoBai {
	public static void main(String[] args) {
		overlap();
	}
	public static void overlap() {
		String queryFile = "/home/zong0jie/桌面/7月汇总表生产厂商.xlsx";
		String excelFile = "/home/zong0jie/桌面/原料及编码表.xlsx";
		String outFile = "/home/zong0jie/桌面/7yue.xls";
		
		HashMap<String, String[]> mapSub_ID2Line = new HashMap<String, String[]>();
		ArrayList<String[]> lsExcel = ExcelTxtRead.readLsExcelTxt(excelFile, 1);
		ArrayList<String[]> lsQuery = ExcelTxtRead.readLsExcelTxt(queryFile, 1);
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		
		for (String[] strings : lsExcel) {
			mapSub_ID2Line.put(strings[0].trim().toLowerCase(), strings);
		}
		for (String[] strings : lsQuery) {
			String ID = strings[0].trim();
			String[] resultQuery = mapSub_ID2Line.get(ID.toLowerCase());
			if (resultQuery == null) {
				txtOut.writefileln(strings);
				continue;
			}
			String[] result = ArrayOperate.combArray(strings, resultQuery, 0);
			txtOut.writefileln(result);
		}
		txtOut.close();
	}


}
