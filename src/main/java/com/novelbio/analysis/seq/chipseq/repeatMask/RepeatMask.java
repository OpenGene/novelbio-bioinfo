package com.novelbio.analysis.seq.chipseq.repeatMask;


import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.novelbio.analysis.seq.genome.GffToRepeat;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashRepeat;
import com.novelbio.base.dataOperate.TxtReadandWrite;


public class RepeatMask {

	static GffToRepeat gffLocatCodRepeat=new GffToRepeat();
	static boolean flag=false;

	/**
	 * 读取repeat文件，
	 * @param repeatFile
	 * @throws Exception
	 */
	public static void loadRepeat(String repeatFile) throws Exception 
	{
		if (!flag) 
		{
			gffLocatCodRepeat.prepare(repeatFile);
			flag=true;
		}
		System.out.print("ok");
	}
	
	/**
	 * 读取repeatFile，最后统计多少peak summit落在了repeat中
	 * @param repeatFile repeat文件，为UCSCtxt文本
	 * @param locationFile peak范围文件，为txt文本
	 * @param resultFile 写入的文件，包含路径
	 * @param readRow 从第几行读取，实际行(从1开始计数)
	 * @param colChr ChrID在第几列，实际列
	 * @param colLOC 坐标在第几列，实际列，ChrID和坐标应该一一对应
	 * @throws Exception
	 */
	public static void locatstatistic(String locationFile,String resultFile,int readRow,int colChr, int colLOC) throws Exception 
	{
		//读repeat文件
		System.out.print("ok");
		//读peak坐标文件
		TxtReadandWrite txtlocate=new TxtReadandWrite();
		txtlocate.setParameter(locationFile,false,true);
		
		String[][] ChrID=txtlocate.ExcelRead("\t", readRow, colChr, txtlocate.ExcelRows(), colChr);
		String[][] LOCIDcod=txtlocate.ExcelRead("\t", readRow, colLOC, txtlocate.ExcelRows(), colLOC);
		//合并信息
		String [][] LOCIDinfo=new String[ChrID.length][2];
		for (int i = 0; i < ChrID.length; i++) {
			LOCIDinfo[i][0]=ChrID[i][0];
			LOCIDinfo[i][1]=LOCIDcod[i][0];
		}
		
		ArrayList<String[]> result=gffLocatCodRepeat.locateCod(LOCIDinfo);

		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(resultFile, true,false);
		txtresult.ExcelWrite(result, "\t", 1, 1);
	}

	/**
	 * 读取repeatFile，最后统计多少peak region落在了repeat中,
	 * @param Bp true:计算与Repea相交的具体Bp数<br>     
	 *  false:只有当peak和region的交集部分大于50%时，才算一个 输入的数据.
	 * @param locationFile peak范围文件，为txt文本
	 * @param repeatBackGround repeat文件，为UCSCtxt文本
	 * @param resultFile 写入的文件，包含路径
	 * @param readRow 从第几行读取，实际行(从1开始计数)
	 * @param colChr ChrID在第几列，实际列
	 * @param colLOC1 坐标1在第几列，实际列，ChrID和坐标应该一一对应
	 * @param colLOC2 坐标2在第几列，实际列，ChrID和坐标应该一一对应
	 * @throws Exception
	 */
	public static void locatstatistic(boolean Bp,String locationFile,String repeatBackGround,String resultFile,int readRow,int colChr, int colLOC1,int colLOC2) throws Exception 
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
		
		ArrayList<String[]> resultRepeatInfo=gffLocatCodRepeat.locCodReg(LOCIDinfo, Bp);
		TxtReadandWrite txtRepeatBackGround = new TxtReadandWrite();
		txtRepeatBackGround.setParameter(repeatBackGround, false, true);
		String[][] repeatBG = txtRepeatBackGround.ExcelRead("\t", 1, 1, txtRepeatBackGround.ExcelRows(), txtRepeatBackGround.ExcelColumns("\t"));
		//获得backGround的repeat的总长度
		long repeatAllBG = 0;
		for (int i = 0; i < repeatBG.length; i++) {
			repeatAllBG = repeatAllBG + Long.parseLong(repeatBG[i][1]);
		}
		////////////////////////////////////获得每个repeat的比例并装入hash表//////////////////////////////////
		Hashtable<String, String> hashRepeatBG = new Hashtable<String, String>();
		for (int i = 0; i < repeatBG.length; i++) {
			repeatBG[i][1] = Double.parseDouble(repeatBG[i][1])/repeatAllBG +"";
			hashRepeatBG.put(repeatBG[i][0], repeatBG[i][1]);
		}
		////////////////////////////////////获得样本的repeat的总数/总长度//////////////////////////////////
		long repeatAll = 0;
		for (String[] strings : resultRepeatInfo) {
			repeatAll = repeatAll + Long.parseLong(strings[1]);
		}
		for (String[] strings : resultRepeatInfo) {
			strings[1] =  Double.parseDouble(strings[1])/repeatAll +"";
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//用试验去查找背景,结果装入新的文本
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String[] strings : resultRepeatInfo) {
			String[] tmpResult = new String[3];
			tmpResult[0] = strings[0];
			tmpResult[1] = strings[1];
			tmpResult[2] = hashRepeatBG.get(strings[0]);
			lsResult.add(tmpResult);
		}
		String[] title = new String[3];
		title[0] = "RepeatClass"; title[1] = "Treatment"; title[2] = "BackGround";
		lsResult.add(0, title);
		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(resultFile, true,false);
		txtresult.ExcelWrite(lsResult, "\t", 1, 1);
		
		
		
		
	}

	
	/**
	 * 读取repeatFile，最后给出每个peak region所覆盖的repeat
	 * @param repeatFile repeat文件，为UCSCtxt文本
	 * @param locationFile peak范围文件，为txt文本
	 * @param resultFile 写入的文件，包含路径
	 * @param readRow 从第几行读取，实际行(从1开始计数)
	 * @param colChr ChrID在第几列，实际列
	 * @param colLOC1 坐标1在第几列，实际列，ChrID和坐标应该一一对应
	 * @param colLOC2 坐标2在第几列，实际列，ChrID和坐标应该一一对应
	 * @throws Exception
	 */
	public static void peakRepeatDetail(String locationFile,String resultFile,int readRow,int colChr, int colLOC1,int colLOC2) throws Exception 
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
		ArrayList<String[]> result=gffLocatCodRepeat.locateCodregionInfo(LOCIDinfo);
		
		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(resultFile, true,false);
		txtresult.ExcelWrite(result, "\t", 1, 1);
	}
	
	/**
	 * 获得repeat背景等信息
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String[]> getStaticInfo(String repeatFile) throws Exception 
	{
		
		GffHashRepeat gffHashRepeat=new GffHashRepeat();
		gffHashRepeat.ReadGffarray(repeatFile);
		Hashtable<String, Integer> gffStatisticHash=gffHashRepeat.getLength();
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
