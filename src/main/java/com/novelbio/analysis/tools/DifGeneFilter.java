package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 差异基因过滤
 * 就是给定一组差异基因的txt或者excel，将符合条件的挑选出来
 */
public class DifGeneFilter {
	String excelTxtFile;
	String outTxtFile;
	/** 从第几行开始筛选，因为前面可能是title */
	int readFromLines = 1;
	/**
	 *  key: 列数，从0开始
	 * value：过滤器
	 */
	HashMap<Integer, FilterValue> mapColNum2Filter = new HashMap<Integer, FilterValue>();
	
	/**
	 * 设定过滤器
	 * @param 第几列，从1开始计算，实际列
	 * @param small 最小值
	 * @param big 最大值
	 * @param isBetweenSmall2Big 是否为介于两者之间
	 */
	public void addFilterInfo(Integer columnNum, double small, double big, boolean isBetweenSmall2Big) {
		FilterValue filterValue = new FilterValue();
		filterValue.setSmall(small);
		filterValue.setBig(big);
		filterValue.setIsBetweenSmall2Big(isBetweenSmall2Big);
		mapColNum2Filter.put(columnNum, filterValue);
	}
	public void clearFilter() {
		mapColNum2Filter.clear();
	}
	public void setInputFile(String inputFile) {
		this.excelTxtFile = inputFile;
	}
	public void setOutTxtFile(String outTxtFile) {
		this.outTxtFile = outTxtFile;
	}
	public void setReadFromLines(int readFromLines) {
		if (readFromLines < 1) {
			readFromLines = 1;
		}
		this.readFromLines = readFromLines;
	}
	/** 开始过滤，采用直接读入内存的模式 */
	public void filtering() {
		TxtReadandWrite txtFiltered = new TxtReadandWrite(outTxtFile, true);
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(excelTxtFile, 1);
		for (int i = 0; i < lsInfo.size(); i++) {
			String[] ss = lsInfo.get(i);

			if (i < readFromLines - 1) {
				txtFiltered.writefileln(ss);
			}
			
			//是否通过过滤
			boolean filtered = true;
			for (Integer colNum : mapColNum2Filter.keySet()) {
				FilterValue filterValue = mapColNum2Filter.get(colNum);
				//如果没通过过滤
				if (!filterValue.isFiltered(ss[colNum - 1])) {
					filtered = false;
					break;
				}
			}
			if (filtered) {
				txtFiltered.writefileln(ss);
			}
		}
		txtFiltered.close();
	}
	
}

class FilterValue {
	double big= 0;
	double small = 0;
	/** 是否待筛选的值为介于small和big之间
	 * true：表示大于等于 big， 小于等于 small
	 * false：表示大于big，小于small
	 */
	boolean isBetweenSmall2Big = false;
	
	public void setBig(double big) {
		this.big = big;
	}
	public void setSmall(double small) {
		this.small = small;
	}
	
	/**  是否待筛选的值为介于small和big之间 */
	public void setIsBetweenSmall2Big(boolean isBetweenSmall2Big) {
		this.isBetweenSmall2Big = isBetweenSmall2Big;
	}
	
	/** 输入的信息，虽然是字符串，但是要能改成double
	 * 如果输入不能转化为double的string，也返回false
	 *  */
	public boolean isFiltered(String valueStr) {
		double value = 0;
		try {
			value = Double.parseDouble(valueStr);
		} catch (Exception e) {
			return false;
		}
		boolean result = false;
		if (isBetweenSmall2Big && (value >= small && value <= big)) {
			result = true;
		} else if (!isBetweenSmall2Big && (value >= big || value <= small)) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}
}