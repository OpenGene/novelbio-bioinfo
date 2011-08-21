package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * ����Ϊʵ��һ��С����<br>
 * ���ж�ȡexce��txt�ļ�����ָ����Ҫ��ĳ����������<br>
 * Ȼ���⼸�кϲ�����String[][]����ʽ����
 * @author zong0jie
 *
 */
public class ExcelTxtRead {
	private static final Logger logger = Logger.getLogger(ExcelTxtRead.class);
	/**
	 * ָ��excel�ļ����Լ���Ҫ��ȡ���к���
	 * @param excelFile ����ȡ��excel�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����
	 * @param rowStart
	 * @param rowEnd ���rowEnd<1����һֱ����sheet1�ļ���β
	 * @return
	 */
	public static String[][] readExcel(String excelFile,int[] columnID,int rowStart,int rowEnd) 
	{
		ExcelOperate excelOperate=new ExcelOperate();
		excelOperate.openExcel(excelFile);
		if (rowEnd<1) 
			rowEnd=excelOperate.getRowCount();
		
		ArrayList<String[][]> lstmpResult=new ArrayList<String[][]>();
		for (int i = 0; i < columnID.length; i++) {
			String[][] tmpresult=excelOperate.ReadExcel(rowStart, columnID[i], rowEnd, columnID[i]);
			lstmpResult.add(tmpresult);
		}
		return ArrayOperate.combCol(lstmpResult);
	}
	
	
	
	/**
	 * ָ��txt�ļ����Լ���Ҫ��ȡ���к���
	 * @param txtFile ����ȡ��txt�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����
	 * @param rowStart
	 * @param rowEnd ���rowEnd=-1����һֱ�����ļ���β
	 * @return
	 * @throws Exception 
	 */
	public static String[][] readtxtExcel(String txtFile,String sep,int[] columnID,int rowStart,int rowEnd) throws Exception 
	{
		TxtReadandWrite txtOperate=new TxtReadandWrite();
		txtOperate.setParameter(txtFile, false,true);
		if (rowEnd==-1) 
			rowEnd=txtOperate.ExcelRows();

		ArrayList<String[][]> lstmpResult=new ArrayList<String[][]>();
		for (int i = 0; i < columnID.length; i++) {
			String[][] tmpresult=txtOperate.ExcelRead(sep,rowStart, columnID[i], rowEnd, columnID[i]);
			lstmpResult.add(tmpresult);
		}
		txtOperate.close();
		return ArrayOperate.combCol(lstmpResult);
	}
	
	/**
	 * ����һ���ı���ָ��ĳ���У�Ȼ���⼸�������������ظ�����ȫ��ɾ����ֻ�����ظ��ĵ�һ��
	 * �����ʵ��shell����uniq��һ������
	 * @param inputFIle �����ļ�
	 * @param sep �ָ���һ��Ϊ\t
	 * @param column �ڼ��У�ʵ����
	 * @param outPut ����ļ�
	 * @throws Exception 
	 */
    public static void uniq(String inputFIle,String sep, int column, String outPut) throws Exception {
    	TxtReadandWrite txtInputFile=new TxtReadandWrite();
    	txtInputFile.setParameter(inputFIle, false, true);
    	TxtReadandWrite txtOutput = new TxtReadandWrite();
    	txtOutput.setParameter(outPut, true, false);
    	
    	BufferedReader inputReader=txtInputFile.readfile();
    	String content="";
    	String tmp="";
    	while ((content=inputReader.readLine())!=null) 
    	{
    		String tmp2=content.split(sep)[column-1].trim();
			if (tmp.equals(tmp2)) {
				continue;
			}
			tmp=tmp2;
			txtOutput.writefile(content+"\n",false);
		}
    	txtOutput.writefile("",true);
    	txtInputFile.close();
    	txtOutput.close();
	}
	
    
	/**
	 * �����ļ���xls2003/txt��������ǵ���Ϣ����arraylist-string[]����
	 * @param File �ļ���
	 * @param firstlinels1 �ӵڼ��п�ʼ��ȥ
	 * @param sep �����txt�Ļ��������ʲô
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String[]> getFileToList(String File,int firstlinels1, String sep) throws Exception 
	{
		ArrayList<String[]> ls1=null;ArrayList<String[]> ls2=null;
		TxtReadandWrite txt = new TxtReadandWrite(File,false);
		
		try {
			ExcelOperate excel = new ExcelOperate();
			excel.openExcel(File);
			ls1 = excel.ReadLsExcel(firstlinels1, 1, excel.getRowCount(), excel.getColCount(2));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if (ls1 == null || ls1.size()<1) {
			int txtRowNum = txt.ExcelRows();
			ls1=txt.ExcelRead(sep, firstlinels1, 1,txtRowNum , -1, 1);//��Ŀ���ж�ȡ
		}
		txt.close();
		return ls1;
	}
	
	/**
	 * �����ļ���xls2003/txt��������ǵ���Ϣ����arraylist-string[]����
	 * @param File �ļ���
	 * @param firstlinels1 �ӵڼ��п�ʼ��ȥ
	 * @param sep �����txt�Ļ��������ʲô
	 * @return
	 * @throws Exception
	 */
	public static String[][] getFileToArray(String File,int firstlinels1, String sep)
	{
		String[][] ls1=null;
		TxtReadandWrite txt = new TxtReadandWrite(File,false);
		
		try {
			ExcelOperate excel = new ExcelOperate();
			excel.openExcel(File);
			ls1 = excel.ReadExcel(firstlinels1, 1, excel.getRowCount(), excel.getColCount(2));
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (ls1 == null || ls1.length<1) {
			try {
				int txtRowNum = txt.ExcelRows();
				ls1=txt.ExcelRead(sep, firstlinels1, 1,txtRowNum , txt.ExcelColumns(2, sep));//��Ŀ���ж�ȡ
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		txt.close();
		return ls1;
	}
	
	
	
}