package com.novelbio.analysis.tools;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 饶军的标准化
 * 每一行获得中位数，然后标准化为1，其他跟着变，也就是除以中位数。缺失值用最小值代替
 * @author zong0jie
 *
 */
public class MediaNormalization {
	public static void main(String[] args) {
		String excelFile = "/home/zong0jie/桌面/宗杰归一化.xlsx";
		String out = FileOperate.changeFileSuffix(excelFile, "_normalization", null);
		MediaNormalization mediaNormalization = new MediaNormalization();
		mediaNormalization.readFile(excelFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(out, true);
		txtWrite.ExcelWrite(mediaNormalization.copeInfoAndGetResult());
	}
	/**
	 * 第一行是title
	 * 第一列是item
	 */
	ArrayList<String[]> lsInfo;
	
	public void readFile(String excelFile) {
		lsInfo = ExcelTxtRead.readLsExcelTxt(excelFile, 1);
	}
	
	public ArrayList<String[]> copeInfoAndGetResult() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] title = lsInfo.get(0);
		lsResult.add(title);
		
		for (int i = 1; i < lsInfo.size(); i++) {
			String[] tmp = lsInfo.get(i);
			String item = tmp[0];
			double[] tmpValue = getDouble(2, tmp);
			ArrayList<Integer> lsNull = getLsNullPosition(tmpValue);
			
			//如果30%的空缺，则跳过
			if (lsNull.size() > tmpValue.length * 0.4) {
				lsResult.add(tmp);
				continue;
			}
			
			double[] normValue = normalizeMedian(tmpValue);
			String[] tmpResult = combResult(item, normValue);
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	/**
	 * 从第几列开始，将tmp转换为double
	 * @param from 譬如第一列是item那么就从第二列开始，实际列
	 * @param tmp
	 * @return
	 */
	private double[] getDouble(int from, String[] tmp) {
		from--;
		if (from < 0) {
			from = 0;
		}
		
		double[] result = new double[tmp.length - from];
		int m = 0;
		for (int i = from; i < tmp.length; i++) {
			try {
				result[m] = Double.parseDouble(tmp[i]);
			} catch (Exception e) {
				result[m] = -Double.MAX_VALUE;
			}
			m++;
		}
		return result;
	}
	/** 饶军的需求 */
	private double[] normalizeMedian(double[] input) {
		double[] out;
		ArrayList<Integer> lsNullCol = getLsNullPosition(input);
		double median = MathComput.median(input);
		out = normalization(input, median);
		double min = getMinValue(out);
		fillNullWithMin(lsNullCol, out, min);
		return out;
	}
	/** 返回没有值，或者说最小值的单元 */
	private ArrayList<Integer> getLsNullPosition(double[] input) {
		ArrayList<Integer> lsNullInfo = new ArrayList<Integer>();
		for (int i = 0; i < input.length; i++) {
			if (input[i] == -Double.MAX_VALUE) {
				lsNullInfo.add(i);
			}
		}
		return lsNullInfo;
	}
	
	/** 用中位数进行标准化，也就是每个项都除以中位数 */
	private double[] normalization(double[] input, double median) {
		double[] result = new double[input.length];
		for (int i = 0; i < input.length; i++) {
			double tmpResult = input[i];
			if (tmpResult > -Double.MAX_VALUE) {
				tmpResult = tmpResult/median;
			}
			result[i] = tmpResult;
		}
		return result;
	}
	
	/** 获得除null之外的最小值 */
	private double getMinValue(double[] input) {
		double min = Double.MAX_VALUE;
		for (double d : input) {
			if (d > -Double.MAX_VALUE && d < min) {
				min = d;
			}
		}
		return min;
	}
	/** 用最小值填充null */
	private void fillNullWithMin(ArrayList<Integer> lsNullCol, double[] input, double min) {
		for (Integer d : lsNullCol) {
			input[d] = min;
		}
	}
	/** 将item和value合并起来 */
	private String[] combResult(String item, double[] value) {
		String[] result = new String[value.length + 1];
		result[0] = item;
		for (int i = 1; i < result.length; i++) {
			result[i] = value[i - 1] + "";
		}
		return result;
	}
}
