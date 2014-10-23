package com.novelbio.analysis.tools.Mas3;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.ExcelOperate;





/**
 * MAS3的pathway结果，将其中的probeID提取出来，保存为excel格式
 * @author zong0jie
 *
 */
public class getProbID {
	
	
	public static void main(String[] args) 
	{
		String file = "/media/winE/NBC/Project/Microarray_QY110318/";
		String fileU = file + "MAS3pathwayUp.xls";String fileUS = file + "MAS3pathwayUpResult.xls";
		String fileD = file + "MAS3pathwayDown.xls";String fileDS = file + "MAS3pathwayDownResult.xls";
		getProbID(fileU, 2, 6, fileUS);
		getProbID(fileD, 2, 6, fileDS);
	}

	
	/**
	 * 
	 * @param excelFile
	 * @param rowNum 从第几行开始读取
	 * @param colNum 读取第几列的信息
	 * @param resultExcel
	 */
	public static void getProbID(String excelFile, int rowNum,int colNum,String resultExcel) {
		ExcelOperate excelMas = new ExcelOperate();
		excelMas.openExcel(excelFile);
		ArrayList<String[]> lsProbID = excelMas.ReadLsExcel(rowNum, colNum, excelMas.getRowCount(), colNum);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String[] strings : lsProbID) {
			String[] ss = strings[0].split(";");
			for (String string : ss) {
				String[] tmpResult = new String[1];
				tmpResult[0] = string;
				lsResult.add(tmpResult);
			}
		}
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(resultExcel);
		excelResult.WriteExcel( 1, 1, lsResult);
		excelResult.close();
	}
}
