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
	static int readRow=2;//从第二行开始读
	static int colChr=1;//第一列是chr
	static int colLOCstart=2;//第二列是第一个坐标
	static int colLOCend=3;//第三列是第二个坐标
	static int colLOCsummit=9;//第七列是中间坐标
	
	Object[][] LOCInfo1=null;
	Object[][] LOCInfo2=null;
	//CpG
	String [][] LOCCpGinfo1=null;
	String [][] LOCCpGinfo2=null;
	
	
	/**
	 * 如果某个值设置为""，则该值不设置
	 * @param chrFilePath
	 * @param gffClass 待实例化的Gffhash子类，只能有 "TIGR","CG","UCSC","Peak","Repeat"这几种
	 * @param gffFilePath
	 * @param mapFilePath 默认10bp的间隔计数
	 * @param cgFile CpG文件
	 * @param PeakFile peak文件路径
	 *@param int[] 指定peak读取的行和列
	 *0:第几行开始读
	 *1: ChrID第几列
	 *2:LOCstart第几列
	 *3:LOCend第几列
	 *4:LOCsummit第几列
	 * @param resultFilePath 结果文件保存路径,最后无所谓加不加/
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
		
		
		
		
		
		
		
		readRow=chrNum[0];//从第二行开始读
		colChr=chrNum[1];//第一列是chr
		colLOCstart=chrNum[2];//第二列是第一个坐标
		colLOCend=chrNum[3];//第三列是第二个坐标
		colLOCsummit=chrNum[4];//第七列是中间坐标
		
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
