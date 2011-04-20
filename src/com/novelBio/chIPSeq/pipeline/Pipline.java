package com.novelBio.chIPSeq.pipeline;

import java.io.File;
import java.util.ArrayList;

import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataStructure.ArrayOperate;
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.base.genome.GffLocatCod;
import com.novelBio.base.genome.GffToCG;
import com.novelBio.chIPSeq.cGIsland.CpG;
import com.novelBio.chIPSeq.prepare.GenomeBasePrepare;


public class Pipline extends GenomeBasePrepare{
	protected static GffLocatCod gffLocatCod=new GffLocatCod();
	static int invNum=10;
	static String sep="\t";
	static String CGFIle="";
	static String DataName1="";
	static String DataName2="";
	static String PeakFile1="";
	static String PeakFile2="";
	static String ResultFilePath="";
	static int readRow=2;//�ӵڶ��п�ʼ��
	static int colChr=1;//��һ����chr
	static int colLOCstart=2;//�ڶ����ǵ�һ������
	static int colLOCend=3;//�������ǵڶ�������
	static int colLOCsummit=9;//���������м�����
	
	Object[][] LOCInfo1=null;
	Object[][] LOCInfo2=null;
	//CpG
	String [][] LOCCpGinfo1=null;
	String [][] LOCCpGinfo2=null;
	
	
	/**
	 * ���ĳ��ֵ����Ϊ""�����ֵ������
	 * @param chrFilePath
	 * @param gffClass ��ʵ������Gffhash���ֻ࣬���� "TIGR","CG","UCSC","Peak","Repeat"�⼸��
	 * @param gffFilePath
	 * @param mapFilePath Ĭ��10bp�ļ������
	 * @param cgFile CpG�ļ�
	 * @param PeakFile peak�ļ�·��
	 *@param int[] ָ��peak��ȡ���к���
	 *0:�ڼ��п�ʼ��
	 *1: ChrID�ڼ���
	 *2:LOCstart�ڼ���
	 *3:LOCend�ڼ���
	 *4:LOCsummit�ڼ���
	 * @param resultFilePath ����ļ�����·��,�������ν�Ӳ���/
	 */
	public static void  prepare(String chrFilePath,String gffClass,String gffFilePath,String mapFilePath,String cgFile,String dataName1,String dataName2,String peakFile1,String peakFile2,int[] chrNum,String resultFilePath) {
		if (!chrFilePath.trim().equals("")) {
			gffLocatCod.loadChr(chrFilePath);
		}
		if (!gffFilePath.trim().equals("")) {
			gffLocatCod.loadGff(gffClass,gffFilePath);
		}
		if (!mapFilePath.trim().equals("")) {
			gffLocatCod.loadMap(mapFilePath, chrFilePath, " ", 0, 1, 2, invNum, 0);
		}
		if (!cgFile.trim().equals("")) {
			CGFIle=cgFile;
		}
		if (!dataName1.trim().equals("")) {
			PeakFile1=dataName1;
		}
		if (!dataName2.trim().equals("")) {
			PeakFile1=dataName2;
		}
		if (!peakFile1.trim().equals("")) {
			PeakFile1=peakFile1;
		}
		if (!peakFile2.trim().equals("")) {
			PeakFile2=peakFile2;
		}
		if (!resultFilePath.trim().equals("")) {
			ResultFilePath=resultFilePath;
	    	 if (!ResultFilePath.endsWith(File.separator)) {  
	    		 ResultFilePath = ResultFilePath + File.separator;  
		         }  
		}
		
		
		
		
		
		
		
		readRow=chrNum[0];//�ӵڶ��п�ʼ��
		colChr=chrNum[1];//��һ����chr
		colLOCstart=chrNum[2];//�ڶ����ǵ�һ������
		colLOCend=chrNum[3];//�������ǵڶ�������
		colLOCsummit=chrNum[4];//���������м�����
		
	}
	
	public void getLOCInfo() throws Exception 
	{
		TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
		txtReadandWrite.setParameter(PeakFile1, false,true);
		LOCInfo1=txtReadandWrite.ExcelRead(sep, 1, 1, txtReadandWrite.ExcelRows(), txtReadandWrite.ExcelColumns(1, sep));
		if (!PeakFile2.trim().equals("")) {
			txtReadandWrite.setParameter(PeakFile2, false,true);
			LOCInfo2=txtReadandWrite.ExcelRead(sep, 1, 1, txtReadandWrite.ExcelRows(), txtReadandWrite.ExcelColumns(1, sep));
		}
	}
	
	
	
	
	public void cpGRun() throws Exception 
	{
		CpG.loadCG(CGFIle);
		String CpGFIle=ResultFilePath+"CpG/";
		FileOperate.createFolder(CpGFIle);
	    CpG.locatstatistic(PeakFile1, CpGFIle+DataName1+"CpGStatistic", readRow, colChr, colLOCstart, colLOCend);
	    LOCCpGinfo1=CpG.peakCGDetailArray(PeakFile1, readRow, colChr, colLOCstart,colLOCend);
		Object[][] LOCIDCpG1=ArrayOperate.combArray(LOCInfo1, LOCCpGinfo1, 0);
		String[][] LOCIDCpGresult1=new String[LOCIDCpG1.length][LOCIDCpG1[0].length];
		for (int i = 0; i < LOCIDCpG1.length; i++)
		{
		   for (int j = 0; j < LOCIDCpG1[i].length; j++)
		   {
			   LOCIDCpGresult1[i][j]=(String)LOCIDCpG1[i][j];
		    }
		}
		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(CpGFIle+DataName1+"CpGDetail",true,false);
		txtresult.ExcelWrite(LOCIDCpGresult1, "\t");
		
	    if (!PeakFile2.trim().equals("")) {
	    	CpG.locatstatistic(PeakFile2, CpGFIle+DataName2+"CpG", readRow, colChr, colLOCstart, colLOCend);
	    	LOCCpGinfo2=CpG.peakCGDetailArray(PeakFile1, readRow, colChr, colLOCstart,colLOCend);
	    	String[][] LocCpGInfo=new String[][];
	    	
	    	
			Object[][] LOCIDCpG2=ArrayOperate.combArray(LOCInfo1, LOCCpGinfo2, 0);
			String[][] LOCIDCpGresult2=new String[LOCIDCpG2.length][LOCIDCpG2[0].length];
			for (int i = 0; i < LOCIDCpG2.length; i++)
			{
			   for (int j = 0; j < LOCIDCpG2[i].length; j++)
			   {
				   LOCIDCpGresult2[i][j]=(String)LOCIDCpG2[i][j];
			    }
			}
			txtresult.setParameter(CpGFIle+DataName2+"CpGDetail", true);
			txtresult.ExcelWrite(LOCIDCpGresult2, "\t");
		}
	}
	
	public void peakSummitSeq() {
		
	}
	
	
 
	
	
	
	
}
