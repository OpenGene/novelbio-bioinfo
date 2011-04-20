package com.novelBio.tools.arrayTools;

import java.util.ArrayList;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;


public class ArrayTools
{
	public static void seperateFile(String filePath,String excelfileName,String outFileName) throws Exception {
		ExcelOperate excelArray=new ExcelOperate();
		excelArray.openExcel(filePath+"/"+excelfileName);
		String[][] arrayInfo=excelArray.ReadExcel(1, 1, excelArray.getRowCount(), excelArray.getColCount());
		
		ArrayList<Integer> lsSepNum=new ArrayList<Integer>();
		lsSepNum.add(0);
		//��÷ָ���У�Ҳ����˵�Ը���Ϊ�ָ��У���ǰ��ķֿ�,���е�һ��Ϊ0�����һ��Ϊ����
		for (int i = 0; i < arrayInfo[0].length; i++) {
			if (arrayInfo[0][i]==null||arrayInfo[0][i].trim().equals("")) 
			{
				lsSepNum.add(i);
			}
		}
		lsSepNum.add(excelArray.getColCount());
		//���������ļ�����ΪA1.txt���Դ�����
		ArrayList<String> lsName=new ArrayList<String>();
		lsName.add("A");lsName.add("B");lsName.add("C");lsName.add("D");lsName.add("E");lsName.add("F");lsName.add("G");lsName.add("H");
		
		TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
		String resultFilePath=filePath+"/"+outFileName;
		FileOperate.createFolder(resultFilePath);
		
		int k=0;//�����ָ������
		//��ʽ�ָ�
		for (int ss = 0; ss <lsSepNum.size()-1; ss++) 
		{
			int kk=1;
			for (int i =lsSepNum.get(ss)+1; i < lsSepNum.get(ss+1); i=i+2)
			{
				ArrayList<String[]> sepResult=new ArrayList<String[]>();
				for (int j = 0; j < arrayInfo.length; j++) 
				{
					String[] tmp=new String[3];
					tmp[0]=arrayInfo[j][0];
					tmp[1]=arrayInfo[j][i];
					tmp[2]=arrayInfo[j][i+1];
					sepResult.add(tmp);
				}
				
				//thisFilePath=thisFilePath.substring(1);
				String path=resultFilePath+"/"+lsName.get(ss)+kk+".txt";kk++;
				txtReadandWrite.setParameter(path, true,false);
				txtReadandWrite.ExcelWrite(sepResult, "\t", 1, 1);
			}
				
		}

			
			
	
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
