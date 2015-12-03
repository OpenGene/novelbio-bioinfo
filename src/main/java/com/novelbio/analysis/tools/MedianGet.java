package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 获得一个表的中位数
 * @author zong0jie
 *
 */
public class MedianGet {
	public static void main(String[] args) {
		MedianGet medianGet = new MedianGet();
//		medianGet.getMedian("/media/winE/NBC/Project/miRNA_ZHENYANSUO_111018/20111113/miRNA治疗后VS治疗前.xls", 1, 
//				"/media/winE/NBC/Project/miRNA_ZHENYANSUO_111018/20111113/miRNA治疗后VS治疗前_cope.xls", 2,3,4,5,6,7,8,9,10,11,12,13,14,15);
		String file = "/home/zong0jie/桌面/共表达信号值 - 副本.xls";
		medianGet.getMedian(file, 1, 
		FileOperate.changeFileSuffix(file, "_median", null), 2,3,4,5,6);
	}

	/**
	 * @param filename 文件名
	 * @param colAccID accID所在的列，实际数字
	 * @param outFile 输出文件名
	 * @param colNum double数字所在的列，实际数字
	 */
	public void getMedian(String excelFile, int colAccID, String outFile,int... colNum) {
		ArrayList<Integer> lsCol = new ArrayList<Integer>();
		for (int i : colNum) {
			lsCol.add(i);
		}
		ArrayList<String[]> lsExcel = ExcelTxtRead.readLsExcelTxt(excelFile, 1);
		ArrayList<String[]> lsResult = MathComput.getMedian(lsExcel, colAccID, lsCol);
		ExcelOperate excelOperate = new ExcelOperate(outFile);
		excelOperate.writeExcel(1, 1, lsResult);
		excelOperate.close();
	}

	

}
