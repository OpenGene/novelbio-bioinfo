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
 * �е�ʱ����Ƚϵ���Ŀ���������ص㣬ÿһ����Ŀ�á�: \ ���ȷ��Ÿ�����
 * Ʃ��һ�������ж��refID����������ֱ��û�����бȽϣ�
 * ��ô����һ���ı��Ķ��refID�ֳɶ���װ��hashTable���ڶ����ı����ֿ�װ��list��Ȼ�����list��ý���
 * @author zong0jie
 *
 */
public class ComTxt {
	public static void main(String[] args) {
		String aa = FileOperate.getProjectPath();
		System.out.println(aa);
	}
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

			boolean flagFind = false;//���Ƿ��ҵ�
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

			boolean flagFind = false;//���Ƿ��ҵ�
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
		
		
		String[] title1 = strFile1.get(0);String[] title2 = strFile2.get(0);
		String[] title = ArrayOperate.combArray(title1, title2, 0);
		lsInteract.add(0,title);
		lsonly1.add(0,title1);
		lsonly2.add(0,title2);
		

		txtOutFile.setParameter(outPutFile+"interaction.xls", true, false);
		txtOutFile.ExcelWrite(lsInteract);
		

		txtOutFile.setParameter(outPutFile+"Only"+file1Name, true, false);
		txtOutFile.ExcelWrite(lsonly1);
		txtOutFile.setParameter(outPutFile+"Only"+file2Name, true, false);
		txtOutFile.ExcelWrite(lsonly2);
		
	}
}
