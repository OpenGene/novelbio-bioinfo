package com.novelBio.chIPSeq.cGIsland;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.genome.GffToCG;
import com.novelBio.base.genome.gffOperate.GffHashCG;
import com.novelBio.base.genome.gffOperate.GffHashRepeat;


public class CpG 
{

	static GffToCG gffToCG=new GffToCG();
	static boolean flag=false;

	/**
	 * 读取repeat文件，
	 * @param repeatFile
	 * @throws Exception
	 */
	public static void loadCG(String CGFile) throws Exception 
	{
		if (!flag) 
		{
			gffToCG.prepare(CGFile);
			flag=true;
		}
		System.out.print("ok");
	}
	
	/**
	 * 读取repeatFile，最后统计多少bp的CG被peak覆盖.
	 * @param locationFile peak范围文件，为txt文本
	 * @param resultFile 写入的文件，包含路径
	 * @param readRow 从第几行读取，实际行(从1开始计数)
	 * @param colChr ChrID在第几列，实际列
	 * @param colLOC1 坐标1在第几列，实际列，ChrID和坐标应该一一对应
	 * @param colLOC2 坐标2在第几列，实际列，ChrID和坐标应该一一对应
	 * @throws Exception
	 */
	public static void locatstatistic(String locationFile,String resultFile,int readRow,int colChr, int colLOC1,int colLOC2) throws Exception 
	{
		//读peak坐标文件
		TxtReadandWrite txtlocate=new TxtReadandWrite();
		txtlocate.setParameter(locationFile,false, true);
		String[][] ChrID=txtlocate.ExcelRead("\t", readRow, colChr, txtlocate.ExcelRows(), colChr);
		String[][] LOCIDcod1=txtlocate.ExcelRead("\t", readRow, colLOC1, txtlocate.ExcelRows(), colLOC1);
		String[][] LOCIDcod2=txtlocate.ExcelRead("\t", readRow, colLOC2, txtlocate.ExcelRows(), colLOC2);
		//合并信息
		String [][] LOCIDinfo=new String[ChrID.length][3];
		for (int i = 0; i < ChrID.length; i++) {
			LOCIDinfo[i][0]=ChrID[i][0];
			LOCIDinfo[i][1]=LOCIDcod1[i][0];
			LOCIDinfo[i][2]=LOCIDcod2[i][0];
		}
		
		ArrayList<String[]> result=gffToCG.locCodRegBp(LOCIDinfo);

		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(resultFile, true,false);
		txtresult.ExcelWrite(result, "\t", 1, 1);
	}

	
	/**
	 * 读取CGFile，最后给出每个peak region所覆盖的CG
	 * @param locationFile peak范围文件，为txt文本
	 * @param resultFile 写入的文件，包含路径
	 * @param readRow 从第几行读取，实际行(从1开始计数)
	 * @param colChr ChrID在第几列，实际列
	 * @param colLOC1 坐标1在第几列，实际列，ChrID和坐标应该一一对应
	 * @param colLOC2 坐标2在第几列，实际列，ChrID和坐标应该一一对应
	 * @throws Exception
	 */
	public static void peakCGDetail(String locationFile,String resultFile,int readRow,int colChr, int colLOC1,int colLOC2) throws Exception 
	{
		//读peak坐标文件
		TxtReadandWrite txtlocate=new TxtReadandWrite();
		txtlocate.setParameter(locationFile,false, true);
		
		String[][] ChrID=txtlocate.ExcelRead("\t", readRow, colChr, txtlocate.ExcelRows(), colChr);
		String[][] LOCIDcod1=txtlocate.ExcelRead("\t", readRow, colLOC1, txtlocate.ExcelRows(), colLOC1);
		String[][] LOCIDcod2=txtlocate.ExcelRead("\t", readRow, colLOC2, txtlocate.ExcelRows(), colLOC2);
		//合并信息
		String [][] LOCIDinfo=new String[ChrID.length][3];
		for (int i = 0; i < ChrID.length; i++) {
			LOCIDinfo[i][0]=ChrID[i][0];
			LOCIDinfo[i][1]=LOCIDcod1[i][0];
			LOCIDinfo[i][2]=LOCIDcod2[i][0];
		}
		ArrayList<String[]> result=gffToCG.locateCodregionInfo(LOCIDinfo);
		
		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(resultFile,true,false);
		txtresult.ExcelWrite(result, "\t", 1, 1);
	}
	
	/**
	 * 读取CGFile，最后给出每个peak region所覆盖的CG
	 * @param locationFile peak范围文件，为txt文本
	 * @param resultFile 写入的文件，包含路径
	 * @param readRow 从第几行读取，实际行(从1开始计数)
	 * @param colChr ChrID在第几列，实际列
	 * @param colLOC1 坐标1在第几列，实际列，ChrID和坐标应该一一对应
	 * @param colLOC2 坐标2在第几列，实际列，ChrID和坐标应该一一对应
	 * @throws Exception
	 */
	public static String[][] peakCGDetailArray(String locationFile,int readRow,int colChr, int colLOC1,int colLOC2) throws Exception 
	{
		//读peak坐标文件
		TxtReadandWrite txtlocate=new TxtReadandWrite();
		txtlocate.setParameter(locationFile, false,true);
		
		String[][] ChrID=txtlocate.ExcelRead("\t", readRow, colChr, txtlocate.ExcelRows(), colChr);
		String[][] LOCIDcod1=txtlocate.ExcelRead("\t", readRow, colLOC1, txtlocate.ExcelRows(), colLOC1);
		String[][] LOCIDcod2=txtlocate.ExcelRead("\t", readRow, colLOC2, txtlocate.ExcelRows(), colLOC2);
		//合并信息
		String [][] LOCIDinfo=new String[ChrID.length][3];
		for (int i = 0; i < ChrID.length; i++) {
			LOCIDinfo[i][0]=ChrID[i][0];
			LOCIDinfo[i][1]=LOCIDcod1[i][0];
			LOCIDinfo[i][2]=LOCIDcod2[i][0];
		}
		ArrayList<String[]> lsresult=gffToCG.locateCodregionInfo(LOCIDinfo);
		String[][] result=new String[lsresult.size()	][lsresult.get(0).length];
		for (int i = 0; i < result.length; i++) {
			result[i]=lsresult.get(i);
		}
		return result;
	}
	
	
	
	
	/**
	 * 获得CG背景等信息
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String[]> getStaticInfo(String CGFile) throws Exception 
	{
		
		GffHashCG gffHashCG=new GffHashCG();
		gffHashCG.ReadGffarray(CGFile);
		Hashtable<String, Integer> gffStatisticHash=gffHashCG.getLength();
		ArrayList<String[]> lsresult=new ArrayList<String[]>();
		Iterator iter = gffStatisticHash.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String[] tmpresult=new String[2];
		    tmpresult[0]= (String) entry.getKey();
		    tmpresult[1]= (Integer) entry.getValue()+"";
		    lsresult.add(tmpresult);
		}
			return lsresult;
	}
}
