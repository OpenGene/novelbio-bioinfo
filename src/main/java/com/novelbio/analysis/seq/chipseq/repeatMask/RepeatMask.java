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
	 * ��ȡrepeat�ļ���
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
	 * ��ȡrepeatFile�����ͳ�ƶ���peak summit������repeat��
	 * @param repeatFile repeat�ļ���ΪUCSCtxt�ı�
	 * @param locationFile peak��Χ�ļ���Ϊtxt�ı�
	 * @param resultFile д����ļ�������·��
	 * @param readRow �ӵڼ��ж�ȡ��ʵ����(��1��ʼ����)
	 * @param colChr ChrID�ڵڼ��У�ʵ����
	 * @param colLOC �����ڵڼ��У�ʵ���У�ChrID������Ӧ��һһ��Ӧ
	 * @throws Exception
	 */
	public static void locatstatistic(String locationFile,String resultFile,int readRow,int colChr, int colLOC) throws Exception 
	{
		//��repeat�ļ�
		System.out.print("ok");
		//��peak�����ļ�
		TxtReadandWrite txtlocate=new TxtReadandWrite();
		txtlocate.setParameter(locationFile,false,true);
		
		String[][] ChrID=txtlocate.ExcelRead("\t", readRow, colChr, txtlocate.ExcelRows(), colChr);
		String[][] LOCIDcod=txtlocate.ExcelRead("\t", readRow, colLOC, txtlocate.ExcelRows(), colLOC);
		//�ϲ���Ϣ
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
	 * ��ȡrepeatFile�����ͳ�ƶ���peak region������repeat��,
	 * @param Bp true:������Repea�ཻ�ľ���Bp��<br>     
	 *  false:ֻ�е�peak��region�Ľ������ִ���50%ʱ������һ�� ���������.
	 * @param locationFile peak��Χ�ļ���Ϊtxt�ı�
	 * @param repeatBackGround repeat�ļ���ΪUCSCtxt�ı�
	 * @param resultFile д����ļ�������·��
	 * @param readRow �ӵڼ��ж�ȡ��ʵ����(��1��ʼ����)
	 * @param colChr ChrID�ڵڼ��У�ʵ����
	 * @param colLOC1 ����1�ڵڼ��У�ʵ���У�ChrID������Ӧ��һһ��Ӧ
	 * @param colLOC2 ����2�ڵڼ��У�ʵ���У�ChrID������Ӧ��һһ��Ӧ
	 * @throws Exception
	 */
	public static void locatstatistic(boolean Bp,String locationFile,String repeatBackGround,String resultFile,int readRow,int colChr, int colLOC1,int colLOC2) throws Exception 
	{
		//��peak�����ļ�
		TxtReadandWrite txtlocate=new TxtReadandWrite();
		txtlocate.setParameter(locationFile, false,true);
		String[][] ChrID=txtlocate.ExcelRead("\t", readRow, colChr, txtlocate.ExcelRows(), colChr);
		String[][] LOCIDcod1=txtlocate.ExcelRead("\t", readRow, colLOC1, txtlocate.ExcelRows(), colLOC1);
		String[][] LOCIDcod2=txtlocate.ExcelRead("\t", readRow, colLOC2, txtlocate.ExcelRows(), colLOC2);
		//�ϲ���Ϣ
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
		//���backGround��repeat���ܳ���
		long repeatAllBG = 0;
		for (int i = 0; i < repeatBG.length; i++) {
			repeatAllBG = repeatAllBG + Long.parseLong(repeatBG[i][1]);
		}
		////////////////////////////////////���ÿ��repeat�ı�����װ��hash��//////////////////////////////////
		Hashtable<String, String> hashRepeatBG = new Hashtable<String, String>();
		for (int i = 0; i < repeatBG.length; i++) {
			repeatBG[i][1] = Double.parseDouble(repeatBG[i][1])/repeatAllBG +"";
			hashRepeatBG.put(repeatBG[i][0], repeatBG[i][1]);
		}
		////////////////////////////////////���������repeat������/�ܳ���//////////////////////////////////
		long repeatAll = 0;
		for (String[] strings : resultRepeatInfo) {
			repeatAll = repeatAll + Long.parseLong(strings[1]);
		}
		for (String[] strings : resultRepeatInfo) {
			strings[1] =  Double.parseDouble(strings[1])/repeatAll +"";
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//������ȥ���ұ���,���װ���µ��ı�
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
	 * ��ȡrepeatFile��������ÿ��peak region�����ǵ�repeat
	 * @param repeatFile repeat�ļ���ΪUCSCtxt�ı�
	 * @param locationFile peak��Χ�ļ���Ϊtxt�ı�
	 * @param resultFile д����ļ�������·��
	 * @param readRow �ӵڼ��ж�ȡ��ʵ����(��1��ʼ����)
	 * @param colChr ChrID�ڵڼ��У�ʵ����
	 * @param colLOC1 ����1�ڵڼ��У�ʵ���У�ChrID������Ӧ��һһ��Ӧ
	 * @param colLOC2 ����2�ڵڼ��У�ʵ���У�ChrID������Ӧ��һһ��Ӧ
	 * @throws Exception
	 */
	public static void peakRepeatDetail(String locationFile,String resultFile,int readRow,int colChr, int colLOC1,int colLOC2) throws Exception 
	{	
		//��peak�����ļ�
		TxtReadandWrite txtlocate=new TxtReadandWrite();
		txtlocate.setParameter(locationFile, false,true);
		
		String[][] ChrID=txtlocate.ExcelRead("\t", readRow, colChr, txtlocate.ExcelRows(), colChr);
		String[][] LOCIDcod1=txtlocate.ExcelRead("\t", readRow, colLOC1, txtlocate.ExcelRows(), colLOC1);
		String[][] LOCIDcod2=txtlocate.ExcelRead("\t", readRow, colLOC2, txtlocate.ExcelRows(), colLOC2);
		//�ϲ���Ϣ
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
	 * ���repeat��������Ϣ
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
