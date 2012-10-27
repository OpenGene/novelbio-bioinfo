package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/** ����������
 * ���Ǹ���һ���������txt����excel����������������ѡ����
 */
public class DifGeneFilter {
	String excelTxtFile;
	String outTxtFile;
	/** �ӵڼ��п�ʼɸѡ����Ϊǰ�������title */
	int readFromLines = 1;
	/**
	 *  key: ��������0��ʼ
	 * value��������
	 */
	HashMap<Integer, FilterValue> mapColNum2Filter = new HashMap<Integer, FilterValue>();
	
	/**
	 * �趨������
	 * @param �ڼ��У���1��ʼ���㣬ʵ����
	 * @param small ��Сֵ
	 * @param big ���ֵ
	 * @param isBetweenSmall2Big �Ƿ�Ϊ��������֮��
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
	/** ��ʼ���ˣ�����ֱ�Ӷ����ڴ��ģʽ */
	public void filtering() {
		TxtReadandWrite txtFiltered = new TxtReadandWrite(outTxtFile, true);
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(excelTxtFile, 1);
		for (int i = 0; i < lsInfo.size(); i++) {
			String[] ss = lsInfo.get(i);

			if (i < readFromLines - 1) {
				txtFiltered.writefileln(ss);
			}
			
			//�Ƿ�ͨ������
			boolean filtered = true;
			for (Integer colNum : mapColNum2Filter.keySet()) {
				FilterValue filterValue = mapColNum2Filter.get(colNum);
				//���ûͨ������
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
	/** �Ƿ��ɸѡ��ֵΪ����small��big֮��
	 * true����ʾ���ڵ��� big�� С�ڵ��� small
	 * false����ʾ����big��С��small
	 */
	boolean isBetweenSmall2Big = false;
	
	public void setBig(double big) {
		this.big = big;
	}
	public void setSmall(double small) {
		this.small = small;
	}
	
	/**  �Ƿ��ɸѡ��ֵΪ����small��big֮�� */
	public void setIsBetweenSmall2Big(boolean isBetweenSmall2Big) {
		this.isBetweenSmall2Big = isBetweenSmall2Big;
	}
	
	/** �������Ϣ����Ȼ���ַ���������Ҫ�ܸĳ�double
	 * ������벻��ת��Ϊdouble��string��Ҳ����false
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