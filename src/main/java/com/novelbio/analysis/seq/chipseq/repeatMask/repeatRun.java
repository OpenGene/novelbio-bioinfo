package com.novelbio.analysis.seq.chipseq.repeatMask;


import java.util.ArrayList;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;


public class repeatRun {

	
	
	
	static String repeakBackGround="/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/statisticInfo/repeatregionBackGround.txt";
 


	
	
	//����ļ�
	
	static String filePeakRegionStatsticBp="/media/winE/NBC/Project/ChIPSeq_WJK100909/result/Repeat/WJKRegionStasticBp";
	//static String filePeakSummitStatstic="/media/winG/NBC/Project/ChIP-SeqCDG20100911/result/repeat/FrepeatSummitStastic";

	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try {	RepeatMask.loadRepeat(NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REPEAT);	} catch (Exception e) {e.printStackTrace();	}
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
			String FlocationFile="/media/winE/NBC/Project/ChIPSeq_WJK100909/result/PeakCalling/Peak Information.xls";
			String filePeakRegionStatstic50="/media/winE/NBC/Project/ChIPSeq_WJK100909/result/Repeat/WJK_RegionStastic50";
			RepeatMask.locatstatistic(false,FlocationFile, repeakBackGround,filePeakRegionStatstic50, readRow, colChr, colLOC1,colLOC2);
			
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
			 String FlocationFile="/media/winE/NBC/Project/ChIPSeq_WJK100909/result/PeakCalling/Peak Information20100926_all.xls";
			 String filePeakRegionDetail="/media/winE/NBC/Project/ChIPSeq_WJK100909/result/Repeat/WJK_RegionDetail";
				
			RepeatMask.peakRepeatDetail(FlocationFile,filePeakRegionDetail, readRow, colChr, colLOC1,colLOC2); 	
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * ���repeat������Ϣ
	 */
	public static void RepeatBackground()
	{
		String repeatFile = "";
		try {
			ArrayList<String[]> result=RepeatMask.getStaticInfo(repeatFile);
			TxtReadandWrite repeatbackGround=new TxtReadandWrite();
			repeatbackGround.setParameter(repeakBackGround, true,false);
			repeatbackGround.ExcelWrite(result, "\t", 1, 1);
		} catch (Exception e) {	e.printStackTrace(); }
		System.out.println("ok");
	}
	
	
	
	
	
	
	
	
	
	
	
}
