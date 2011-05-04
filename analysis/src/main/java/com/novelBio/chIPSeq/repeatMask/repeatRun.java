package com.novelBio.chIPSeq.repeatMask;


import java.util.ArrayList;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.generalConf.NovelBioConst;


public class repeatRun {

	
	
	
	static String repeakBackGround="/media/winE/Bioinformatics/GenomeData/ucsc_mm9/statistic/repeatregionBackGround.txt";
 


	
	
	//����ļ�
	
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
	 * ��ȡrepeatFile��:���ͳ�ƶ���peak region������repeat��
	 * @param Bp true:������Repea�ཻ�ľ���Bp��
	 * false:ֻ�е�peak��region�Ľ������ִ���50%ʱ������һ�� ���������.
	 */
	public static void PeakRegionStatsticBp() 
	{
		try { 
			 int readRow=2;//�ӵڶ��п�ʼ��
			 int colChr=1;//��һ����chr
			 int colLOC1=2;//�ڶ����ǵ�һ������
			 int colLOC2=3;//�������ǵڶ�������
			 int colLOC=9;//���������м�����
			String FlocationFile="/media/winE/NBC/Project/ChIPSeq_CDG1011101/result/annotation/mT3/RPeak Information.xls";
			RepeatMask.locatstatistic(true,FlocationFile,repeakBackGround, filePeakRegionStatsticBp, readRow, colChr, colLOC1,colLOC2);
			} catch (Exception e) { e.printStackTrace(); }
		System.out.println("ok");
	}
	
	/**
	 * ��ȡrepeatFile��:���ͳ�ƶ���peak region������repeat��
	 * @param Bp true:������Repea�ཻ�ľ���Bp��
	 * false:ֻ�е�peak��region�Ľ������ִ���50%ʱ������һ�� ���������.
	 */
	public static void PeakRegionStatstic50() 
	{
		try { 
			 int readRow=2;//�ӵڶ��п�ʼ��
			 int colChr=1;//��һ����chr
			 int colLOC1=2;//�ڶ����ǵ�һ������
			 int colLOC2=3;//�������ǵڶ�������
			String FlocationFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/annotation/FT5_macsPeak_peaks.xls";
			String filePeakRegionStatstic50="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/repeat/FT5_RegionStastic50";
			RepeatMask.locatstatistic(false,FlocationFile, NovelBioConst.GENOME_PATH_UCSC_MM19_STATISTIC_REPEAT,filePeakRegionStatstic50, readRow, colChr, colLOC1,colLOC2);
			
		} catch (Exception e) { e.printStackTrace(); }
		System.out.println("ok");
	}
	
	
	/**
	 * ��ȡrepeatFile�����ͳ�ƶ���peak summit������repeat��
	
	public static void PeakSummitStatstic() 
	{
		try { RepeatMask.locatstatistic(FlocationFile, filePeakSummitStatstic, readRow, colChr, colLOC); } catch (Exception e) { e.printStackTrace(); }
		System.out.println("ok");
	}
	 */
	
	/**
	 * ��ȡrepeatFile���������ļ���ÿ��peak region�����ǵ�repeat����д��FresultFile
	 */
	public static void  PeakRegionDetail() 
	{
		try 
		{ 
			 int readRow=2;//�ӵڶ��п�ʼ��
			 int colChr=1;//��һ����chr
			 int colLOC1=2;//�ڶ����ǵ�һ������
			 int colLOC2=3;//�������ǵڶ�������
			 String FlocationFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/annotation/FT5_macsPeak_peaks.xls";
			 String filePeakRegionDetail="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/repeat/FT5_RrepeatPeakRegionDetail";
				
			RepeatMask.peakRepeatDetail(FlocationFile,filePeakRegionDetail, readRow, colChr, colLOC1,colLOC2); 	
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * ���repeat������Ϣ
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