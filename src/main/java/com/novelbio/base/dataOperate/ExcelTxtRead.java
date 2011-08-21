package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * 本类为实现一个小功能<br>
 * 安列读取exce或txt文件，如指定需要的某不连续几列<br>
 * 然后将这几列合并后以String[][]的形式返回
 * @author zong0jie
 *
 */
public class ExcelTxtRead {
	private static final Logger logger = Logger.getLogger(ExcelTxtRead.class);
	/**
	 * 指定excel文件，以及需要读取的列和行
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
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
	 * 指定txt文件，以及需要读取的列和行
	 * @param txtFile 待读取的txt文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd=-1，则一直读到文件结尾
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
	 * 给定一个文本，指定某几列，然后将这几列所有相邻且重复的行全部删除，只保留重复的第一行
	 * 这个其实是shell命令uniq的一个补充
	 * @param inputFIle 输入文件
	 * @param sep 分隔符一般为\t
	 * @param column 第几列，实际列
	 * @param outPut 输出文件
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
	 * 给定文件，xls2003/txt，获得它们的信息，用arraylist-string[]保存
	 * @param File 文件名
	 * @param firstlinels1 从第几行开始读去
	 * @param sep 如果是txt的话，间隔是什么
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
			ls1=txt.ExcelRead(sep, firstlinels1, 1,txtRowNum , -1, 1);//从目标行读取
		}
		txt.close();
		return ls1;
	}
	
	/**
	 * 给定文件，xls2003/txt，获得它们的信息，用arraylist-string[]保存
	 * @param File 文件名
	 * @param firstlinels1 从第几行开始读去
	 * @param sep 如果是txt的话，间隔是什么
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
				ls1=txt.ExcelRead(sep, firstlinels1, 1,txtRowNum , txt.ExcelColumns(2, sep));//从目标行读取
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		txt.close();
		return ls1;
	}
	
	
	
}