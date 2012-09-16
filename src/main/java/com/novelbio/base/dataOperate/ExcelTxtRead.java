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
	 *  ������һ�п�λ����null����ɾ��
	 * @param excelFile ����ȡ��excel�ļ�
	 * @param columnID ����ȡ���У�int[]�м��Ƕ�ȡ�ĵڼ��У���ȡ����ᰴ��ָ�����е�˳�����
	 * @param rowStart
	 * @param rowEnd ���rowEnd<1����һֱ����sheet1�ļ���β
	 * @return 
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd) {
		return readLsExcelTxt(excelFile, columnID, rowStart, rowEnd,false);
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
		ArrayList<String[]> lsResultTmp = new ArrayList<String[]>();

		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excelOperate = new ExcelOperate(excelFile);
			lsResultTmp = excelOperate.ReadLsExcel(rowStart, rowEnd, columnID);//(rowStartNum, columnStartNum, rowEndNum, columnEndNum);//readExcel(excelFile, columnID, rowStart, rowEnd);
		}
		else {
			TxtReadandWrite txtRead = new TxtReadandWrite(excelFile, false);
			lsResultTmp = txtRead.ExcelRead(rowStart, rowEnd, columnID, -1);
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String[] strings : lsResultTmp) {
			if (DelFirst && (strings[0] == null || strings[0].trim().equals(""))) {
				continue;
			}
			lsResult.add(strings);
		}
		return lsResult;
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
		ls1 = txt.ExcelRead(firstlinels1, 1, txtRowNum , -1, 0);//��Ŀ���ж�ȡ
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
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int rowStart, int rowEnd, int colStart, int colEnd) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(rowStart, colStart, rowEnd, colEnd);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile, false);
		ls1=txt.ExcelRead(rowStart, colStart,rowEnd , colEnd, 0);//��Ŀ���ж�ȡ
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
	public static ArrayList<String[]> readLsExcelTxtFile(String excelFile,int rowStart, int colStart, int rowEnd, int colEnd) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(rowStart, colStart, rowEnd, colEnd);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile, false);
		ls1=txt.ExcelRead(rowStart, colStart,rowEnd , colEnd, 0);//��Ŀ���ж�ȡ
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
	public static ArrayList<String[]> writeLsExcelTxt(String excelTxtFile, List<String[]> lsContent, int rowStart, int colStart, int rowEnd, int colEnd) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelTxtFile)) {
			ExcelOperate excel = new ExcelOperate(excelTxtFile);
			excel.WriteExcel(1, 1, lsContent);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelTxtFile, true);
		txt.ExcelWrite(lsContent);
		txt.close();
		return ls1;
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
    
}