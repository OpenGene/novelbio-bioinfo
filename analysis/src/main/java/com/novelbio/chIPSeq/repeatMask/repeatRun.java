package com.novelbio.chIPSeq.repeatMask;


import java.util.ArrayList;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;


public class repeatRun {

	
	
	
	static String repeakBackGround="/media/winE/Bioinformatics/GenomeData/ucsc_mm9/statistic/repeatregionBackGround.txt";
 


	
	
	//输出文件
	
	static String filePeakRegionStatsticBp="/media/winE/NBC/Project/ChIPSeq_CDG1011101/result/repeat/mT3RegionStasticBp";
	//static String filePeakSummitStatstic="/media/winG/NBC/Project/ChIP-SeqCDG20100911/result/repeat/FrepeatSummitStastic";

	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try {	RepeatMask.loadRepeat(NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REPEAT);	} catch (Exception e) {e.printStackTrace();	}
		//PeakRegionStatsticBp();
		PeakRegionStatstic50();
		//PeakSummitStatstic();
		PeakRegionDetail();
		//RepeatBackground();
	}
	
	/**
	 * 读取repeatFile，:最后统计多少peak region落在了repeat中
	 * @param Bp true:计算与Repea相交的具体Bp数
	 * false:只有当peak和region的交集部分大于50%时，才算一个 输入的数据.
	 */
	public static void PeakRegionStatsticBp() 
	{
		try { 
			 int readRow=2;//从第二行开始读
			 int colChr=1;//第一列是chr
			 int colLOC1=2;//第二列是第一个坐标
			 int colLOC2=3;//第三列是第二个坐标
			 int colLOC=9;//第七列是中间坐标
			String FlocationFile="/media/winE/NBC/Project/ChIPSeq_CDG1011101/result/annotation/mT3/RPeak Information.xls";
			RepeatMask.locatstatistic(true,FlocationFile,repeakBackGround, filePeakRegionStatsticBp, readRow, colChr, colLOC1,colLOC2);
			} catch (Exception e) { e.printStackTrace(); }
		System.out.println("ok");
	}
	
	/**
	 * 读取repeatFile，:最后统计多少peak region落在了repeat中
	 * @param Bp true:计算与Repea相交的具体Bp数
	 * false:只有当peak和region的交集部分大于50%时，才算一个 输入的数据.
	 */
	public static void PeakRegionStatstic50() 
	{
		try { 
			 int readRow=2;//从第二行开始读
			 int colChr=1;//第一列是chr
			 int colLOC1=2;//第二列是第一个坐标
			 int colLOC2=3;//第三列是第二个坐标
			String FlocationFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/annotation/FT5_macsPeak_peaks.xls";
			String filePeakRegionStatstic50="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/repeat/FT5_RegionStastic50";
			RepeatMask.locatstatistic(false,FlocationFile, NovelBioConst.GENOME_PATH_UCSC_MM19_STATISTIC_REPEAT,filePeakRegionStatstic50, readRow, colChr, colLOC1,colLOC2);
			
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("ok");
	}
	
	
	/**
	 * 读取repeatFile，最后统计多少peak summit落在了repeat中
	
	public static void PeakSummitStatstic() 
	{
		try { RepeatMask.locatstatistic(FlocationFile, filePeakSummitStatstic, readRow, colChr, colLOC); } catch (Exception e) { e.printStackTrace(); }
		System.out.println("ok");
	}
	 */
	
	/**
	 * 读取repeatFile，最后给出文件中每个peak region所覆盖的repeat区域并写入FresultFile
	 */
	public static void  PeakRegionDetail() 
	{
		try 
		{ 
			 int readRow=2;//从第二行开始读
			 int colChr=1;//第一列是chr
			 int colLOC1=2;//第二列是第一个坐标
			 int colLOC2=3;//第三列是第二个坐标
			 String FlocationFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/annotation/FT5_macsPeak_peaks.xls";
			 String filePeakRegionDetail="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/repeat/FT5_RrepeatPeakRegionDetail";
				
			RepeatMask.peakRepeatDetail(FlocationFile,filePeakRegionDetail, readRow, colChr, colLOC1,colLOC2); 	
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * 获得repeat背景信息
	 */
	public static void RepeatBackground()
	{
		
		try {
			ArrayList<String[]> result=RepeatMask.getStaticInfo(repeatFile);
			TxtReadandWrite repeatbackGround=new TxtReadandWrite();
			repeatbackGround.setParameter(repeakBackGround, true,false);
			repeatbackGround.ExcelWrite(result, "\t", 1, 1);
		} catch (Exception e) {	e.printStackTrace(); }
		System.out.println("ok");
	}
	
	
	
	
	
	
	
	
	
	
	
}
