package com.novelbio.analysis.tools.repeatdel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataStructure.MathComput;

public class Repeatdel {
	
	/**
	 * 每个ID一个基因
	 */
	HashMap<String, ArrayList<String[]>> hashGeneInfo = new HashMap<String, ArrayList<String[]>>();

	/**
	 * @param filename 文件名
	 * @param colAccID accID所在的列，实际数字
	 * @param outFile 输出文件名
	 * @param colNum double数字所在的列，实际数字
	 */
	public void getMedian(String filename, int colAccID, String outFile,int... colNum) {
		hashGeneInfo = new HashMap<String, ArrayList<String[]>>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		colAccID--;
		for (int i = 0; i < colNum.length; i++) {
			colNum[i] --;
		}
		ExcelOperate excelComb = new ExcelOperate(filename);
		ArrayList<String[]> lsExcel = excelComb.readLsExcel(1, 1, -1, excelComb.getColCount());
		excelComb.close();
		for (String[] strings : lsExcel) {	
			if (hashGeneInfo.containsKey(strings[colAccID].trim()) ) {
				ArrayList<String[]> lsInfo = hashGeneInfo.get(strings[colAccID].trim());
				lsInfo.add(strings);
			}
			else {
				ArrayList<String[]> lsInfo = new ArrayList<String[]>();
				lsInfo.add(strings);
				hashGeneInfo.put(strings[colAccID].trim(), lsInfo);
			}
		}
		
		Collection<ArrayList<String[]>> values = hashGeneInfo.values();
		lsResult.add(lsExcel.get(0));
		for(ArrayList<String[]> value:values)
		{
			try {
				lsResult.add(getMediaInfo(value, colNum));
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		ExcelOperate excelOperate = new ExcelOperate(outFile);
		excelOperate.writeExcel(1, 1, lsResult);
		excelOperate.close();
	}
	
	
	
	
	
	
	/**
	 * @param lsInfo 指定几行信息
	 * @param col 指定所在的列
	 * @return 返回所在列中位数的列
	 */
	private String[] getMediaInfo(List<String[]> lsInfo, int[] col) {
		if (lsInfo.size() == 1) {
			return lsInfo.get(0);
		}
		String[] result = lsInfo.get(0);
		
		for (int i = 0; i < col.length; i++) {
			double[] info = new double[lsInfo.size()];
			for (int m = 0; m < lsInfo.size(); m++) {
				info[m] = Double.parseDouble(lsInfo.get(m)[col[i]]);
			}
			double infoNew = MathComput.median(info);
			result[col[i]] = infoNew + "";
		}
		return result;
	}
	
	/**
	 * @param lsInfo 指定几行信息
	 * @param col 指定所在的列
	 * @param info 要查找的数字
	 * @return 返回找到的那一行
	 */
	private String[] getDetailInfo(ArrayList<String[]> lsInfo, int col, double info) {
		for (String[] strings : lsInfo) {
			if (Double.parseDouble(strings[col]) == info) {
				return strings;
			}
		}
		return null;
	}
}
