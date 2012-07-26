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
 * 本类为实现一个小功能<br>
 * 安列读取exce或txt文件，如指定需要的某不连续几列<br>
 * 然后将这几列合并后以String[][]的形式返回
 * @author zong0jie
 *
 */
public class ExcelTxtRead {
	private static final Logger logger = Logger.getLogger(ExcelTxtRead.class);
	
	/**
	 * 指定excel/txt文件，以及需要读取的列和行
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
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
	 * 指定excel/txt文件，以及需要读取的列和行
	 *  不将第一列空位或者null的行删除
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
	 * @return 
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd)
	{
		return readLsExcelTxt(excelFile, columnID, rowStart, rowEnd,false);
	}
	
	/**
	 * 指定excel/txt文件，以及需要读取的列和行
	 *  自动将第一列空位或者null的行删除
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
	 * @return 
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd, String sep)
	{
		return readLsExcelTxt(excelFile, columnID, rowStart, rowEnd,true, sep);
	}
	/**
	 * 
	 * 指定excel/txt文件，以及需要读取的列和行
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
	 * @param DelFirst 是否将第一列空位或者null的行删除
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
	 * 指定excel/txt文件，以及需要读取的列和行
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
	 * @param DelFirst 是否将第一列空位或者null的行删除
	 * @return
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd, boolean DelFirst) {
		return readLsExcelTxt( excelFile, columnID, rowStart, rowEnd,  DelFirst,  "\t");
	}
	
	/**
	 * 内部close
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param File 文件名
	 * @param firstlinels1 从第几行开始读去
	 * @param sep 如果是txt的话，间隔是什么
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
		ls1=txt.ExcelRead("\t", firstlinels1, 1,txtRowNum , -1, 0);//从目标行读取
		return ls1;
	}
	
	/**
	 * 用readLsExcelTxtFile代替
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param excelFile
	 * @param rowStart 
	 * @param rowEnd 值小于等于0时，读取全部行
	 * @param colStart 
	 * @param colEnd 值小于等于0时，读取全部列
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
		ls1=txt.ExcelRead("\t", rowStart, colStart,rowEnd , colEnd, 0);//从目标行读取
		txt.close();
		return ls1;
	}
	/**
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param excelFile
	 * @param rowStart
	 * @param colStart
	 * @param rowEnd 值小于等于0时，读取全部行
	 * @param colEnd 值小于等于0时，读取全部列
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
		ls1=txt.ExcelRead("\t", rowStart, colStart,rowEnd , colEnd, 0);//从目标行读取
		txt.close();
		return ls1;
	}
	/**
	 * 
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param excelFile 写入已知文档，不过会将写入的sheet覆盖掉，txt的话会新建一个文档
	 * @param rowStart 
	 * @param rowEnd 值小于等于0时，读取全部行
	 * @param colStart 
	 * @param colEnd 值小于等于0时，读取全部列
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
	 * 指定excel文件，以及需要读取的列和行
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出，如果中间含有小于1的列，则跳过
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
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
	 * 指定txt文件，以及需要读取的列和行
	 * @param txtFile 待读取的txt文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出，如果列数小于1，则跳过
	 * @param rowStart
	 * @param rowEnd 如果rowEnd=-1，则一直读到文件结尾
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
    @Deprecated
	public static ArrayList<String[]> getFileToList(String File,int firstlinels1, String sep) throws Exception 
	{
		return readLsExcelTxt(File, firstlinels1);
	}
	
	/**
	 * 给定文件，xls2003/txt，获得它们的信息，用arraylist-string[]保存
	 * @param File 文件名
	 * @param firstlinels1 从第几行开始读去
	 * @param sep 如果是txt的话，间隔是什么
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
				ls1=txt.ExcelRead(sep, firstlinels1, 1,txtRowNum , txt.ExcelColumns(2, sep));//从目标行读取
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		txt.close();
		return ls1;
	}
	
	
	
}