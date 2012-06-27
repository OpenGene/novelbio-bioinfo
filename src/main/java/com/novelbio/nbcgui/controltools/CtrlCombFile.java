package com.novelbio.nbcgui.controltools;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.novelbio.analysis.tools.compare.CombineTab;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class CtrlCombFile {
	CombineTab combineTab = new CombineTab();
	String oufFile = "";
	public void clean() {
		combineTab = new CombineTab();
	}
	public void setOufFile(String oufFile) {
		this.oufFile = oufFile;
	}
	/**
	 * �Ƚ��ļ���
	 * @param colNum
	 */
	public void setCompareCol(String colNum) {
		ArrayList<String[]> lsStrColID = PatternOperate.getPatLoc(colNum, "\\d+", false);
		if (lsStrColID.size() == 0) {
			return;
		}
		ArrayList<Integer> lsColID = new ArrayList<Integer>();
		for (String[] strings : lsStrColID) {
			lsColID.add(Integer.parseInt(strings[0]));
		}
		combineTab.setColCompareOverlapID(lsColID);
	}
	/**
	 *  ���ÿ���ļ���, ����ÿ���ļ����趨����ID��
	 *  �����������ϵ��趨
	 * @param condTxt �ļ�ȫ��
	 * @param codName �����ļ�������֣������������ʾ
	 * @param colDetai ѡ����ļ����ļ���
	 */
	public void setColDetail(String condTxt, String codName, String colStrDetail) {
		ArrayList<String[]> lsResult = PatternOperate.getPatLoc(colStrDetail, "\\d+", false);
		if (lsResult.size() == 0) {
			return;
		}
		int[] colDetail = new int[lsResult.size()];
		for (int i = 0; i < colDetail.length; i++) {
			colDetail[i] = Integer.parseInt(lsResult.get(i)[0]);
		}
		combineTab.setColExtractDetai(condTxt, codName, colDetail);
	}
	
	public void output() {
		ArrayList<String[]> lsOut = combineTab.getResultLsUnion();
//		if (lsOut.size() > 60000) {
//			JOptionPane.showMessageDialog(null, "Result num is bigger than 60000, so save to txt file", "alert", JOptionPane.INFORMATION_MESSAGE);
			TxtReadandWrite txtWrite = new TxtReadandWrite(oufFile, true);
			txtWrite.ExcelWrite(lsOut, "\t", 1, 1);
			return;
//		}
//		ExcelOperate excelOperate = new ExcelOperate();
//		excelOperate.openExcel(FileOperate.changeFileSuffix(oufFile, "", "xls"));
//		excelOperate.WriteExcel(1, 1, lsOut);
	}
}
