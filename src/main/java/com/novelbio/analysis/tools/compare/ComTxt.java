package com.novelbio.analysis.tools.compare;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * 有的时候待比较的项目有这样的特点，每一个项目用“: \ ”等符号隔开，
 * 譬如一个基因有多个refID，这样两列直接没法进行比较，
 * 那么将第一个文本的多个refID分成多行装入hashTable，第二个文本不分开装入list，然后遍历list获得交集
 * @author zong0jie
 *
 */
public class ComTxt {

	/**
	 * 
	 * 有的时候待比较的项目有这样的特点，每一个项目用“: \ ”等符号隔开，
	 * 譬如一个基因有多个refID，这样两列直接没法进行比较，
	 * 那么将第一个文本的多个refID分成多行装入hashTable，第二个文本不分开装入list，然后遍历list获得交集
	 * 有一种情况不能用这个比，第一个 A/B 第二个A/C 第三个B/C 这样子比起来会出问题，所以比之前要先去重复
	 * 比较前两个文件都不要有重复项
	 * @author zong0jie
	 * @param file1 第一个文件，因为第二个文件,装入hash表来比较的，所以第一个文件中最好不要有重复项
	 * @param file1FirstLine 第一个文件从第几行开始读，包含标题行 为实际行
	 * @param file2 第二个文件,装入hash表来比较的，所以第二个文件中可以有重复项,这里的重复是指潜在的两个不同的refseq对应了一样的geneID
	 * @param file2FirstLine 第二个文件从第几行开始读，包含标题行  为实际行
	 * @param file1ColNum 第一个文件比较第几列 实际列
	 * @param file2ColNum 第二个文件比较第几列 实际列
	 * @param sepReg 比较行用什么正则表达式进行分割,<b>如果sepReg 为“” 那么就不切割</b>
	 * @param outPutFile 输出文件
	 * @param outInfo
	 * @throws Exception
	 */
	public static void getCompFile(String file1,int file1FirstLine,String file2,int file2FirstLine,int file1ColNum,int file2ColNum,String sepReg,String outPutFile) throws Exception 
	{
		file1ColNum--; file2ColNum--;
		
		String[][] strFile1 = null;
		String[][] strFile2 = null;
		try {
			ExcelOperate excel = new ExcelOperate();
			excel.openExcel(file1);
			strFile1 = excel.ReadExcel(file1FirstLine, 1, excel.getRowCount(), excel.getColCount(2));
		} catch (Exception e) {
			// TODO: handle exception
		}
		TxtReadandWrite txt = new TxtReadandWrite();
		if (strFile1 == null || strFile1.length<1) {
			txt.setParameter(file1, false,true);
			strFile1 = txt.ExcelRead("\t", file1FirstLine, 1, txt.ExcelRows(), txt.ExcelColumns("\t"));//从目标行读取
		}
		
		try {
			ExcelOperate excel = new ExcelOperate();
			excel.openExcel(file2);
			strFile2 = excel.ReadExcel(file2FirstLine, 1, excel.getRowCount(), excel.getColCount(2));
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (strFile2 == null || strFile2.length<1) {
			txt.setParameter(file2, false,true);
			strFile2 = txt.ExcelRead("\t", file2FirstLine, 1, txt.ExcelRows(), txt.ExcelColumns("\t"));//从目标行读取
		}
		Hashtable<String, String[]> hashCompFile1 = new Hashtable<String, String[]>();				
		Hashtable<String, String[]> hashCompFile2 = new Hashtable<String, String[]>();		
		for (int i = 1; i < strFile1.length; i++)
		{
			String[] file1Key = null;
			if (sepReg.equals("")) {
				file1Key = new String[1];
				file1Key[0] = strFile1[i][file1ColNum].trim();
			}
			else {
				file1Key= strFile1[i][file1ColNum].trim().split(sepReg);
			}
			
			for (int j = 0; j < file1Key.length; j++) 
			{
				if (file1Key[j].trim().equals("")) 
				{
					continue;
				}
				hashCompFile1.put(file1Key[j].trim(), strFile1[i]);
			}
		}
		
		for (int i = 1; i < strFile2.length; i++)
		{
			String[] file2Key = null;
			if (sepReg.equals("")) {
				file2Key = new String[1];
				file2Key[0] = strFile2[i][file2ColNum].trim();
			}
			else {
				file2Key=strFile2[i][file2ColNum].trim().split(sepReg);
			}
 
			for (int j = 0; j < file2Key.length; j++) 
			{
				if (file2Key[j].trim().equals("")) 
				{
					continue;
				}
				hashCompFile2.put(file2Key[j].trim(), strFile2[i]);
			}
		}
		
		ArrayList<String[]> lsInteract = new ArrayList<String[]>();
		ArrayList<String[]> lsonly1 = new ArrayList<String[]>();
		ArrayList<String[]> lsonly2 = new ArrayList<String[]>();
		for (int i = 1; i < strFile1.length; i++)
		{
			boolean flagFind = false;//看是否找到
			String[] file1Key = null;
			if (sepReg.equals("")) {
				file1Key = new String[1];
				file1Key[0] = strFile1[i][file1ColNum].trim();
			}
			else {
				file1Key= strFile1[i][file1ColNum].trim().split(sepReg);
			}
			for (int j = 0; j < file1Key.length; j++) 
			{
				String[] tmpFile2 = hashCompFile2.get(file1Key[j].trim());
				if (tmpFile2 != null) 
				{
					String[] tmpOut = ArrayOperate.combArray( strFile1[i], tmpFile2,0);
					lsInteract.add(tmpOut);
					flagFind = true;
					break;
				}
			}
			if (!flagFind) {
				lsonly1.add(strFile1[i]);
			}
		}
		
		for (int i = 1; i < strFile2.length; i++)
		{
			boolean flagFind = false;//看是否找到
			String[] file2Key = null;
			if (sepReg.equals("")) {
				file2Key = new String[1];
				file2Key[0] = strFile2[i][file2ColNum].trim();
			}
			else {
				file2Key=strFile2[i][file2ColNum].trim().split(sepReg);
			}
			for (int j = 0; j < file2Key.length; j++) 
			{
				String[] tmpFile1 = hashCompFile1.get(file2Key[j].trim());
				if (tmpFile1 != null) 
				{
					flagFind = true;
					break;
				}
			}
			if (!flagFind) {
				lsonly2.add(strFile2[i]);
			}
		}
		
		///////////////////////写入文本//////////////////////////////////////////////////////
		TxtReadandWrite txtOutFile=new TxtReadandWrite();
		String file1Name = new File(file1).getName();
		String file2Name = new File(file2).getName();
		
		txtOutFile.setParameter(outPutFile+"Info.txt", true, false);
		int interactNum = lsInteract.size(); 
		int Aonly = lsonly1.size();
		int Bonly = lsonly2.size();
		txtOutFile.writefile("interactNum\t"+interactNum+"\n");
		txtOutFile.writefile(file1Name+"\t"+Aonly+"\t"+(double)interactNum/(Aonly+interactNum)+"\n");
		txtOutFile.writefile(file2Name+"\t"+Bonly+"\t"+(double)interactNum/(Bonly+interactNum)+"\n");
		
		
		String[] title1 = strFile1[0];String[] title2 = strFile2[0];
		String[] title = ArrayOperate.combArray(title1, title2, 0);
		lsInteract.add(0,title);
		lsonly1.add(0,title1);
		lsonly2.add(0,title2);
		

		txtOutFile.setParameter(outPutFile+"interaction.xls", true, false);
		txtOutFile.ExcelWrite(lsInteract, "\t", 1, 1);
		

		txtOutFile.setParameter(outPutFile+"Only"+file1Name, true, false);
		txtOutFile.ExcelWrite(lsonly1, "\t", 1, 1);
		txtOutFile.setParameter(outPutFile+"Only"+file2Name, true, false);
		txtOutFile.ExcelWrite(lsonly2, "\t", 1, 1);
		
	}
}
