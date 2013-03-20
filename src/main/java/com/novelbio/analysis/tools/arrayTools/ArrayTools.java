package com.novelbio.analysis.tools.arrayTools;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;


public class ArrayTools {
	public static void seperateFile(String filePath,String excelfileName,String outFileName) throws Exception {
		ExcelOperate excelArray=new ExcelOperate();
		excelArray.openExcel(filePath+"/"+excelfileName);
		ArrayList<String[]> lsarrayInfo=excelArray.ReadLsExcel(1, 1, excelArray.getRowCount(), excelArray.getColCount());
		
		ArrayList<Integer> lsSepNum=new ArrayList<Integer>();
		lsSepNum.add(0);
		//获得分割的列，也就是说以该列为分割列，将前后的分开,本列第一个为0，最后一个为列数
		for (int i = 0; i < lsarrayInfo.get(0).length; i++) {
			if (lsarrayInfo.get(0)[i]==null||lsarrayInfo.get(0)[i].trim().equals("")) {
				lsSepNum.add(i);
			}
		}
		lsSepNum.add(excelArray.getColCount());
		//最后产生的文件名，为A1.txt等以此类似
		ArrayList<String> lsName=new ArrayList<String>();
		lsName.add("A");lsName.add("B");lsName.add("C");lsName.add("D");lsName.add("E");lsName.add("F");lsName.add("G");lsName.add("H");
		
	
		String resultFilePath=filePath+"/"+outFileName;
		FileOperate.createFolders(resultFilePath);
		
		int k=0;//计数分割的列数
		//正式分割
		for (int ss = 0; ss <lsSepNum.size()-1; ss++) {
			int kk=1;
			for (int i =lsSepNum.get(ss)+1; i < lsSepNum.get(ss+1); i=i+2)
			{
				ArrayList<String[]> sepResult=new ArrayList<String[]>();
				for (int j = 0; j < lsarrayInfo.size(); j++) 
				{
					String[] tmp=new String[3];
					tmp[0] = lsarrayInfo.get(j)[0];
					tmp[1] = lsarrayInfo.get(j)[i];
					tmp[2] = lsarrayInfo.get(j)[i+1];
					sepResult.add(tmp);
				}
				
				//thisFilePath=thisFilePath.substring(1);
				String path=resultFilePath+"/"+lsName.get(ss)+kk+".txt";kk++;
				TxtReadandWrite txtReadandWrite=new TxtReadandWrite(path, true);
				txtReadandWrite.ExcelWrite(sepResult);
				txtReadandWrite.close();
			}
				
		}


	}
	
}
