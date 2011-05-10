package com.novelbio.analysis.tools.compare;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * �е�ʱ����Ƚϵ���Ŀ���������ص㣬ÿһ����Ŀ�á�: \ ���ȷ��Ÿ�����
 * Ʃ��һ�������ж��refID����������ֱ��û�����бȽϣ�
 * ��ô����һ���ı��Ķ��refID�ֳɶ���װ��hashTable���ڶ����ı����ֿ�װ��list��Ȼ�����list��ý���
 * @author zong0jie
 *
 */
public class ComTxt {

	/**
	 * 
	 * �е�ʱ����Ƚϵ���Ŀ���������ص㣬ÿһ����Ŀ�á�: \ ���ȷ��Ÿ�����
	 * Ʃ��һ�������ж��refID����������ֱ��û�����бȽϣ�
	 * ��ô����һ���ı��Ķ��refID�ֳɶ���װ��hashTable���ڶ����ı����ֿ�װ��list��Ȼ�����list��ý���
	 * ��һ���������������ȣ���һ�� A/B �ڶ���A/C ������B/C �����ӱ�����������⣬���Ա�֮ǰҪ��ȥ�ظ�
	 * �Ƚ�ǰ�����ļ�����Ҫ���ظ���
	 * @author zong0jie
	 * @param file1 ��һ���ļ�����Ϊ�ڶ����ļ�,װ��hash�����Ƚϵģ����Ե�һ���ļ�����ò�Ҫ���ظ���
	 * @param file1FirstLine ��һ���ļ��ӵڼ��п�ʼ�������������� Ϊʵ����
	 * @param file2 �ڶ����ļ�,װ��hash�����Ƚϵģ����Եڶ����ļ��п������ظ���,������ظ���ָǱ�ڵ�������ͬ��refseq��Ӧ��һ����geneID
	 * @param file2FirstLine �ڶ����ļ��ӵڼ��п�ʼ��������������  Ϊʵ����
	 * @param file1ColNum ��һ���ļ��Ƚϵڼ��� ʵ����
	 * @param file2ColNum �ڶ����ļ��Ƚϵڼ��� ʵ����
	 * @param sepReg �Ƚ�����ʲô������ʽ���зָ�,<b>���sepReg Ϊ���� ��ô�Ͳ��и�</b>
	 * @param outPutFile ����ļ�
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
			strFile1 = txt.ExcelRead("\t", file1FirstLine, 1, txt.ExcelRows(), txt.ExcelColumns("\t"));//��Ŀ���ж�ȡ
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
			strFile2 = txt.ExcelRead("\t", file2FirstLine, 1, txt.ExcelRows(), txt.ExcelColumns("\t"));//��Ŀ���ж�ȡ
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
			boolean flagFind = false;//���Ƿ��ҵ�
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
			boolean flagFind = false;//���Ƿ��ҵ�
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
		
		///////////////////////д���ı�//////////////////////////////////////////////////////
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
