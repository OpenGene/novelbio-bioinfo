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
 * ���һ�������λ��
 * @author zong0jie
 *
 */
public class MedianGet {
	public static void main(String[] args) {
		MedianGet medianGet = new MedianGet();
//		medianGet.getMedian("/media/winE/NBC/Project/miRNA_ZHENYANSUO_111018/20111113/miRNA���ƺ�VS����ǰ.xls", 1, 
//				"/media/winE/NBC/Project/miRNA_ZHENYANSUO_111018/20111113/miRNA���ƺ�VS����ǰ_cope.xls", 2,3,4,5,6,7,8,9,10,11,12,13,14,15);
		String file = "/home/zong0jie/����/������ź�ֵ - ����.xls";
		medianGet.getMedian(file, 1, 
		FileOperate.changeFileSuffix(file, "_median", null), 2,3,4,5,6);
	}

	/**
	 * @param filename �ļ���
	 * @param colAccID accID���ڵ��У�ʵ������
	 * @param outFile ����ļ���
	 * @param colNum double�������ڵ��У�ʵ������
	 */
	public void getMedian(String excelFile, int colAccID, String outFile,int... colNum) {
		ArrayList<Integer> lsCol = new ArrayList<Integer>();
		for (int i : colNum) {
			lsCol.add(i);
		}
		ArrayList<String[]> lsExcel = ExcelTxtRead.readLsExcelTxt(excelFile, 1);
		ArrayList<String[]> lsResult = MathComput.getMedian(lsExcel, colAccID, lsCol);
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(outFile);
		excelOperate.WriteExcel(1, 1, lsResult);
	}

	

}
