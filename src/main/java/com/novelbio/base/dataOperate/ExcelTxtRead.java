package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	 * ָ��excel/txt�ļ����Լ���Ҫ��ȡ���к���
	 * @param excelFile ����ȡ��excel�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����
	 * @param rowStart
	 * @param rowEnd ���rowEnd<1����һֱ����sheet1�ļ���β
	 * @return
	 */
	@Deprecated
	public static String[][] readExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd)
	{
		if (ExcelOperate.isExcel(excelFile)) {
			return readExcel(excelFile, columnID, rowStart, rowEnd);
		}
		try {
			return readtxtExcel(excelFile, "\t", columnID, rowStart, rowEnd);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * ָ��excel/txt�ļ����Լ���Ҫ��ȡ���к���
	 *  ������һ�п�λ����null����ɾ��
	 * @param excelFile ����ȡ��excel�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����
	 * @param rowStart
	 * @param rowEnd ���rowEnd<1����һֱ����sheet1�ļ���β
	 * @return 
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd)
	{
		return readLsExcelTxt(excelFile, columnID, rowStart, rowEnd,false);
	}
	
	/**
	 * ָ��excel/txt�ļ����Լ���Ҫ��ȡ���к���
	 *  �Զ�����һ�п�λ����null����ɾ��
	 * @param excelFile ����ȡ��excel�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����
	 * @param rowStart
	 * @param rowEnd ���rowEnd<1����һֱ����sheet1�ļ���β
	 * @return 
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd, String sep)
	{
		return readLsExcelTxt(excelFile, columnID, rowStart, rowEnd,true, sep);
	}
	/**
	 * 
	 * ָ��excel/txt�ļ����Լ���Ҫ��ȡ���к���
	 * @param excelFile ����ȡ��excel�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����
	 * @param rowStart
	 * @param rowEnd ���rowEnd<1����һֱ����sheet1�ļ���β
	 * @param DelFirst �Ƿ񽫵�һ�п�λ����null����ɾ��
	 * @return
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd, boolean DelFirst, String sep)
	{
		String[][] tmpResult = null;
		if (ExcelOperate.isExcel(excelFile)) {
			tmpResult = readExcel(excelFile, columnID, rowStart, rowEnd);
		}
		else {
			try {
				tmpResult = readtxtExcel(excelFile, sep, columnID, rowStart, rowEnd);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		ArrayList<String[]> result = new ArrayList<String[]>();
		for (String[] strings : tmpResult) {
			if (DelFirst && (strings[0] == null || strings[0].trim().equals(""))) {
				continue;
			}
			result.add(strings);
		}
		return result;
	}
	/**
	 * 
	 * ָ��excel/txt�ļ����Լ���Ҫ��ȡ���к���
	 * @param excelFile ����ȡ��excel�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����
	 * @param rowStart
	 * @param rowEnd ���rowEnd<1����һֱ����sheet1�ļ���β
	 * @param DelFirst �Ƿ񽫵�һ�п�λ����null����ɾ��
	 * @return
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd, boolean DelFirst) {
		return readLsExcelTxt( excelFile, columnID, rowStart, rowEnd,  DelFirst,  "\t");
	}
	
	/**
	 * �ڲ�close
	 * �����ļ���xls2003/2007/txt��������ǵ���Ϣ����arraylist-string[]����
	 * @param File �ļ���
	 * @param firstlinels1 �ӵڼ��п�ʼ��ȥ
	 * @param sep �����txt�Ļ��������ʲô
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int firstlinels1) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(firstlinels1, 1, excel.getRowCount(), excel.getColCount());
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile, false);
		int txtRowNum = txt.ExcelRows();
		ls1=txt.ExcelRead("\t", firstlinels1, 1,txtRowNum , -1, 0);//��Ŀ���ж�ȡ
		return ls1;
	}
	
	/**
	 * ��readLsExcelTxtFile����
	 * �����ļ���xls2003/2007/txt��������ǵ���Ϣ����arraylist-string[]����
	 * @param excelFile
	 * @param rowStart 
	 * @param rowEnd ֵС�ڵ���0ʱ����ȡȫ����
	 * @param colStart 
	 * @param colEnd ֵС�ڵ���0ʱ����ȡȫ����
	 * @return
	 * @throws Exception
	 */
	@Deprecated 
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int rowStart, int rowEnd, int colStart, int colEnd)
	{
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(rowStart, colStart, rowEnd, colEnd);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile, false);
		ls1=txt.ExcelRead("\t", rowStart, colStart,rowEnd , colEnd, 0);//��Ŀ���ж�ȡ
		txt.close();
		return ls1;
	}
	/**
	 * �����ļ���xls2003/2007/txt��������ǵ���Ϣ����arraylist-string[]����
	 * @param excelFile
	 * @param rowStart
	 * @param colStart
	 * @param rowEnd ֵС�ڵ���0ʱ����ȡȫ����
	 * @param colEnd ֵС�ڵ���0ʱ����ȡȫ����
	 * @return
	 */
	public static ArrayList<String[]> readLsExcelTxtFile(String excelFile,int rowStart, int colStart, int rowEnd, int colEnd)
	{
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(rowStart, colStart, rowEnd, colEnd);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile, false);
		ls1=txt.ExcelRead("\t", rowStart, colStart,rowEnd , colEnd, 0);//��Ŀ���ж�ȡ
		txt.close();
		return ls1;
	}
	/**
	 * 
	 * �����ļ���xls2003/2007/txt��������ǵ���Ϣ����arraylist-string[]����
	 * @param excelFile д����֪�ĵ��������Ὣд���sheet���ǵ���txt�Ļ����½�һ���ĵ�
	 * @param rowStart 
	 * @param rowEnd ֵС�ڵ���0ʱ����ȡȫ����
	 * @param colStart 
	 * @param colEnd ֵС�ڵ���0ʱ����ȡȫ����
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String[]> writeLsExcelTxt(String excelTxtFile, List<String[]> lsContent, int rowStart, int colStart, int rowEnd, int colEnd)
	{
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelTxtFile)) {
			ExcelOperate excel = new ExcelOperate(excelTxtFile);
			excel.WriteExcel(1, 1, lsContent);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelTxtFile, true);
		txt.ExcelWrite(lsContent, "\t", 1, 1);
		txt.close();
		return ls1;
	}
	/**
	 * ָ��excel�ļ����Լ���Ҫ��ȡ���к���
	 * @param excelFile ����ȡ��excel�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����������м京��С��1���У�������
	 * @param rowStart
	 * @param rowEnd ���rowEnd<1����һֱ����sheet1�ļ���β
	 * @return
	 */
	@Deprecated
	public static String[][] readExcel(String excelFile,int[] columnID,int rowStart,int rowEnd) 
	{
		ExcelOperate excelOperate=new ExcelOperate();
		excelOperate.openExcel(excelFile);
		if (rowEnd<1) 
			rowEnd=excelOperate.getRowCount();
		
		ArrayList<String[][]> lstmpResult=new ArrayList<String[][]>();
		for (int i = 0; i < columnID.length; i++) {
			if (columnID[i] < 1) {
				continue;
			}
			String[][] tmpresult=excelOperate.ReadExcel(rowStart, columnID[i], rowEnd, columnID[i]);
			lstmpResult.add(tmpresult);
		}
		return ArrayOperate.combCol(lstmpResult);
	}
	
	
	
	/**
	 * ָ��txt�ļ����Լ���Ҫ��ȡ���к���
	 * @param txtFile ����ȡ��txt�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳��������������С��1��������
	 * @param rowStart
	 * @param rowEnd ���rowEnd=-1����һֱ�����ļ���β
	 * @return
	 * @throws Exception 
	 */
	@Deprecated
	public static String[][] readtxtExcel(String txtFile,String sep,int[] columnID,int rowStart,int rowEnd) throws Exception 
	{
		TxtReadandWrite txtOperate=new TxtReadandWrite();
		txtOperate.setParameter(txtFile, false,true);
		if (rowEnd==-1) 
			rowEnd=txtOperate.ExcelRows();

		ArrayList<String[][]> lstmpResult=new ArrayList<String[][]>();
		for (int i = 0; i < columnID.length; i++) {
			if (columnID[i] < 1) {
				continue;
			}
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
    @Deprecated
	public static ArrayList<String[]> getFileToList(String File,int firstlinels1, String sep) throws Exception 
	{
		return readLsExcelTxt(File, firstlinels1);
	}
	
	/**
	 * �����ļ���xls2003/txt��������ǵ���Ϣ����arraylist-string[]����
	 * @param File �ļ���
	 * @param firstlinels1 �ӵڼ��п�ʼ��ȥ
	 * @param sep �����txt�Ļ��������ʲô
	 * @return
	 * @throws Exception
	 */
	@Deprecated
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