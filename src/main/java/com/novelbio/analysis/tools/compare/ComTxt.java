package com.novelbio.analysis.tools.compare;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

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
	public static void getCompFile(String file1,int file1FirstLine,String file2,int file2FirstLine,int file1ColNum,int file2ColNum,String sepReg,String outPutFile) throws Exception {
		file1ColNum--; file2ColNum--;
		
		ArrayList<String[]> strFile1 = ExcelTxtRead.readLsExcelTxt(file1, file1FirstLine);
		ArrayList<String[]> strFile2 = ExcelTxtRead.readLsExcelTxt(file2, file2FirstLine);
		
		Hashtable<String, String[]> hashCompFile1 = new Hashtable<String, String[]>();	
		Hashtable<String, String[]> hashCompFile2 = new Hashtable<String, String[]>();
		for (String[] strings : strFile1) {
			String[] file1Key = null;
			if (sepReg.equals("")) {
				file1Key = new String[1];
				file1Key[0] = strings[file1ColNum].trim();
			}
			else {
				file1Key= strings[file1ColNum].trim().split(sepReg);
			}
			
			for (int j = 0; j < file1Key.length; j++) {
				if (file1Key[j].trim().equals("")) 
				{
					continue;
				}
				hashCompFile1.put(file1Key[j].trim(), strings);
			}
		}
		for (String[] strings : strFile2) {
			String[] file2Key = null;
			if (sepReg.equals("")) {
				file2Key = new String[1];
				file2Key[0] = strings[file2ColNum].trim();
			}
			else {
				file2Key=strings[file2ColNum].trim().split(sepReg);
			}
 
			for (int j = 0; j < file2Key.length; j++) {
				if (file2Key[j].trim().equals("")) 
				{
					continue;
				}
				hashCompFile2.put(file2Key[j].trim(), strings);
			}
		}
		
		ArrayList<String[]> lsInteract = new ArrayList<String[]>();
		ArrayList<String[]> lsonly1 = new ArrayList<String[]>();
		ArrayList<String[]> lsonly2 = new ArrayList<String[]>();
		for (String[] strings : strFile1) {

			boolean flagFind = false;//看是否找到
			String[] file1Key = null;
			if (sepReg.equals("")) {
				file1Key = new String[1];
				file1Key[0] = strings[file1ColNum].trim();
			}
			else {
				file1Key= strings[file1ColNum].trim().split(sepReg);
			}
			for (int j = 0; j < file1Key.length; j++) 
			{
				String[] tmpFile2 = hashCompFile2.get(file1Key[j].trim());
				if (tmpFile2 != null) 
				{
					String[] tmpOut = ArrayOperate.combArray(strings, tmpFile2,0);
					lsInteract.add(tmpOut);
					flagFind = true;
					break;
				}
			}
			if (!flagFind) {
				lsonly1.add(strings);
			}
		}
		for (String[] strings : strFile2) {

			boolean flagFind = false;//看是否找到
			String[] file2Key = null;
			if (sepReg.equals("")) {
				file2Key = new String[1];
				file2Key[0] = strings[file2ColNum].trim();
			}
			else {
				file2Key=strings[file2ColNum].trim().split(sepReg);
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
				lsonly2.add(strings);
			}
		}
		///////////////////////写入文本//////////////////////////////////////////////////////
		String file1Name = new File(file1).getName();
		String file2Name = new File(file2).getName();
		
		int interactNum = lsInteract.size(); 
		int Aonly = lsonly1.size();
		int Bonly = lsonly2.size();
		TxtReadandWrite txtOutFile=new TxtReadandWrite(outPutFile+"Info.txt", true);
		txtOutFile.writefileln("interactNum\t"+interactNum);
		txtOutFile.writefileln(file1Name+"\t"+Aonly+"\t"+(double)interactNum/(Aonly+interactNum));
		txtOutFile.writefileln(file2Name+"\t"+Bonly+"\t"+(double)interactNum/(Bonly+interactNum));
		
		String[] title1 = strFile1.get(0);String[] title2 = strFile2.get(0);
		String[] title = ArrayOperate.combArray(title1, title2, 0);
		lsInteract.add(0,title);
		lsonly1.add(0,title1);
		lsonly2.add(0,title2);
		
		TxtReadandWrite txtOutFileInteract=new TxtReadandWrite(outPutFile+"interaction.xls", true);
		txtOutFileInteract.ExcelWrite(lsInteract);
		txtOutFileInteract.close();

		TxtReadandWrite txtOutFile1 = new TxtReadandWrite(outPutFile+"Only"+file1Name, true);
		txtOutFile1.ExcelWrite(lsonly1);
		txtOutFile1.close();
		
		TxtReadandWrite txtOutFile2 = new TxtReadandWrite(outPutFile+"Only"+file2Name, true);
		txtOutFile2.ExcelWrite(lsonly2);
		txtOutFile2.close();
	}
}
