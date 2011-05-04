package com.novelBio.tools.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;
 





public class CompareListSimple 
{
	static String sep="\t";
	/**
	 * ���ļ��ĵڼ��п�ʼ��ȡ
	 */
	static int firstlinels1=1;
	static int firstlinels2=1;
	

	
	/**
	 * ��������lst���Ƚ�����lst֮������Щ��ͬ����Щls1���У���Щls2����
	 * lst�к����ظ����ֻ��¼���һ�����һ���Զ�ȥ���߿ո�
	 * 6: ls2mult
	 * @param ls1
	 * @param ls2
	 * @return
	 * ��󷵻�һ��arraylist--arraylist--string[] <br>
	 * ����5��arraylist <br>
	 * 1: ls1only<br>
	 * 2: ls2only<br>
	 * 3: intersection<br>
	 * 4: ls1mult
	 * 5: ls2mult
	 */
	private static ArrayList<ArrayList<String[]>> compareList(List<String[]> ls1,List<String[]> ls2,boolean considerCase) 
	{
		Hashtable<String, String[]> hs1=new Hashtable<String, String[]>();
		Hashtable<String, String[]> hs2=new Hashtable<String, String[]>();
		////////���ؽ��/////////////////////////////////
		ArrayList<String[]> ls1only=new ArrayList<String[]>();
		ArrayList<String[]> ls2only=new ArrayList<String[]>();
		ArrayList<String[]> lsBoth=new ArrayList<String[]>();
		ArrayList<String[]> ls1Mult=new ArrayList<String[]>();
		ArrayList<String[]> ls2Mult=new ArrayList<String[]>();
		
		/////////�м���///////////////////////////////////
		ArrayList<String> ls1NoDup=new ArrayList<String>();//ls1���ظ�key
		ArrayList<String> ls2NoDup=new ArrayList<String>();//ls2���ظ�key
		ArrayList<String> ls1MultKey=new ArrayList<String>();
		ArrayList<String> ls2MultKey=new ArrayList<String>();

		
		
		for (int i = 0; i < ls1.size(); i++) {
			String tmpKey="";
			if (!considerCase) {
				 tmpKey=ls1.get(i)[0].trim().toLowerCase();
			}
			else {
				tmpKey=ls1.get(i)[0].trim();
			}
			String[] tmpValue=ls1.get(i);
			if (hs1.containsKey(tmpKey))
			{
				if(!ls1MultKey.contains(tmpKey)) 
					ls1MultKey.add(tmpKey);
			}
			else {
				ls1NoDup.add(tmpKey);
			}
			hs1.put(tmpKey, tmpValue);
		}
		
		for (int i = 0; i < ls2.size(); i++) {
			String tmpKey="";
			if (!considerCase) {
				 tmpKey=ls2.get(i)[0].trim().toLowerCase();
			}
			else {
				tmpKey=ls2.get(i)[0].trim();
			}
			String[] tmpValue=ls2.get(i);
			if (hs2.containsKey(tmpKey))
			{
				if(!ls2MultKey.contains(tmpKey)) 
					ls2MultKey.add(tmpKey);
			}
			else {
				ls2NoDup.add(tmpKey);
			}
			hs2.put(tmpKey, tmpValue);
		}
		
		for (int i = 0; i < ls1NoDup.size(); i++)
		{ 
			String[] tmpValue2;
			String[] tmpValue1;
			//����
			String tmpls1key=ls1NoDup.get(i);
			if ((tmpValue2=hs2.get(tmpls1key))!=null) 
			{
				tmpValue1=hs1.get(tmpls1key);
				String[] tmpValue=new String[tmpValue1.length+tmpValue2.length]; 
				for (int j = 0; j < tmpValue.length-1; j++)
				{
					if (j<tmpValue1.length) 
						tmpValue[j]=tmpValue1[j];
					else 
						tmpValue[j]=tmpValue2[j+1-tmpValue1.length];
				}
				lsBoth.add(tmpValue);
			}
			else //��ls1����
			{
				ls1only.add(hs1.get(tmpls1key));
			}
		}
		
		for (int i = 0; i < ls2NoDup.size(); i++)
		{
			//����
			String tmpls2key=ls2NoDup.get(i);
			if (hs1.get(tmpls2key)==null) 
			{
				ls2only.add(hs2.get(tmpls2key));
			}
		}
		
		for (int i = 0; i < ls1MultKey.size(); i++) {
			ls1Mult.add(hs1.get(ls1MultKey.get(i)));
		}
		for (int i = 0; i < ls2MultKey.size(); i++) {
			ls2Mult.add(hs2.get(ls2MultKey.get(i)));
		}

		ArrayList<ArrayList<String[]>> result=new ArrayList<ArrayList<String[]>>();
		result.add(ls1only);
		result.add(ls2only);
		result.add(lsBoth);
		result.add(ls1Mult);
		result.add(ls2Mult);
		return result;
		
	}
	
	
	/**
	 * �������ı�ת��ΪArrayList����������
	 * @param filePath�ı�����·��
	 * @param resultFold ����ļ�
	 * @param FileA ��һ���ı���������tab��������һ���ж���
	 * @param FIleB �ڶ����ı���������tab��������һ���ж���
	 * @param caseSen �Ƿ��Сд����
	 * @throws Exception
	 */
	public static void getFileToList(String filePath,String resultFold,String FileA,String FileB,boolean caseSen,String FileIntersection,String FileAonly,String FileBonly) throws Exception 
	{
		 if (!filePath.endsWith(File.separator)) {  
			 filePath = filePath + File.separator;  
	         }  
		
		boolean considerCase=caseSen;
		
		
		ArrayList<String[]> ls1=null;ArrayList<String[]> ls2=null;
		TxtReadandWrite txt = new TxtReadandWrite();
		
		try {
			ExcelOperate excel = new ExcelOperate();
			excel.openExcel(filePath+FileA);
			ls1 = excel.ReadLsExcel(firstlinels1, 1, excel.getRowCount(), excel.getColCount(2));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if (ls1 == null || ls1.size()<1) {
			txt.setParameter(filePath+FileA, false,true);
			ls1=txt.ExcelRead(sep, firstlinels1, 1, txt.ExcelRows(), -1, 1);//��Ŀ���ж�ȡ
		}
			
		try {
			ExcelOperate excel = new ExcelOperate();
			excel.openExcel(filePath+FileB);
			ls2 = excel.ReadLsExcel(firstlinels2, 1, excel.getRowCount(), excel.getColCount(2));
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (ls2 == null || ls2.size()<1) {
			txt.setParameter(filePath+FileB, false,true);
			ls2=txt.ExcelRead(sep, firstlinels2, 1, txt.ExcelRows(), -1, 1);
		}
		
		
	

		
		

		ArrayList<ArrayList<String[]>> compareResult=compareList(ls1, ls2,considerCase);
		String intersectionResultFilePath=filePath+resultFold+"/";

		txt.setParameter(intersectionResultFilePath+FileAonly, true,false);
		txt.ExcelWrite(compareResult.get(0), sep, 1, 1);
		
		txt.setParameter(intersectionResultFilePath+FileBonly, true,false);
		txt.ExcelWrite(compareResult.get(1), sep, 1, 1);
	
		txt.setParameter(intersectionResultFilePath+FileIntersection, true,false);
		txt.ExcelWrite(compareResult.get(2), sep, 1, 1);
	 
	}
	
 
	
	
}
