package com.novelbio.nbcgui.controltools;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.MathComput;

public class CtrlMedian {
	/**
	 * �ļ���
	 */
	String file = "";
	/**
	 * ���յڼ�����Ϊȡ��λ����ID
	 */
	int accID = 0;
	/**
	 * ���ļ�����ȡ����Ϣ
	 */
	ArrayList<String[]> lsFileInfo = new ArrayList<String[]>();
	ArrayList<String[]> lsResult = new ArrayList<String[]>();
	ArrayList<Integer> lsCol = null;
	public void setFile(String file) {
		this.file = file;
	}
	/**
	 * ���ض�ȡ�Ĵ�ȡ��λ�����ļ�
	 * @return
	 */
	public ArrayList<String[]> readFile()
	{
		lsFileInfo = ExcelTxtRead.readLsExcelTxt(file, 1);
		return lsFileInfo;
	}
	/**
	 * �趨ΨһID
	 * @param accID
	 */
	public void setAccID(int accID)
	{
		this.accID = accID;
	}
	/**
	 * �趨Ҫ���ļ���ȡ��λ������ȡ���ж����õ�һ�γ��ֵ���Ϣ���
	 * Ʃ���ұȽ�accID��ȡ��3-6�е���λ�������ǻ������symbol��description�еȣ���ô���õ�һ�����ֵ�symbol��description���
	 */
	public void setMedianID(ArrayList<Integer> lsCol)
	{
		this.lsCol = lsCol;
	}
	/**
	 * �ڴ�֮ǰ��ҪreadFile()
	 * @return
	 */
	public ArrayList<String[]> getResult() {
		lsResult = MathComput.getMedian(lsFileInfo, accID, lsCol);
		return lsResult;
	}
	
	public void saveFile(String excelFile)
	{
		ExcelOperate excel = new ExcelOperate();
		excel.openExcel(excelFile);
		excel.WriteExcel(1, 1, lsResult);
		excel.Close();
	}
}
