package com.novelbio.analysis.seq.chipseq.cGIsland;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.TxtReadandWrite;


public class CGRun {
	static String CGFile="/media/winE/Bioinformatics/GenomeData/ucsc_mm9/D__mm9_cpg_all";
	
	//static String CGFile="/media/winG/bioinformation/GenomeData/HumanUCSChg19/rmsk.txt";
	//输入peak文件
	static String FlocationFile="/media/winE/NBC/Project/ChIPSeq_CDG101101/result/CpG/mT3RPeak Information.xls";
	//读取的内容
	static int readRow=2;//从第二行开始读
	static int colChr=1;//第一列是chr
	static int colLOC1=2;//第二列是第一个坐标
	static int colLOC2=3;//第三列是第二个坐标
	static int colLOC=9;//第七列是中间坐标
	
	
	//输出文件
	static String CGBackGround="/media/winE/Bioinformatics/GenomeData/ucsc_mm9/statistic/CGBackGround.txt";
	static String filePeakRegionStatstic="/media/winE/NBC/Project/ChIPSeq_CDG101101/result/CpG/mT3RCpGInfo.txt";
	static String filePeakRegionDetail="/media/winE/NBC/Project/ChIPSeq_CDG101101/result/CpG/mT3RCpG.xls";
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
	//	CGBackground();
		try {	CpG.loadCG(CGFile);	} catch (Exception e) {e.printStackTrace();	}
		PeakRegionStatstic();
		//PeakSummitStatstic();
		PeakRegionDetail();
		//CGBackground();
	}
	
	/**
	 * 读取CGFile，最后统计多少peak region落在了CG中
	 */
	public static void PeakRegionStatstic() 
	{
		
		try { CpG.locatstatistic(FlocationFile, filePeakRegionStatstic, readRow, colChr, colLOC1,colLOC2); } catch (Exception e) { e.printStackTrace(); }
		System.out.println("ok");
	}

	/**
	 * 读取CGFile，最后给出文件中每个peak region所覆盖的CG区域并写入FresultFile
	 */
	public static void  PeakRegionDetail() 
	{
		try 
		{ 
			CpG.peakCGDetail(FlocationFile,filePeakRegionDetail, readRow, colChr, colLOC1,colLOC2); 	
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * 获得CG背景信息
	 */
	public static void CGBackground()
	{
		
		try {
			ArrayList<String[]> result=CpG.getStaticInfo(CGFile);
			TxtReadandWrite CGbackGround=new TxtReadandWrite();
			CGbackGround.setParameter(CGBackGround, true,false);
			CGbackGround.ExcelWrite(result);
		} catch (Exception e) {	e.printStackTrace(); }
		System.out.println("ok");
	}
	
	
	
	
	
	
	
	
	
	
	
}
