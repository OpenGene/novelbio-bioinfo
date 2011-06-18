package com.novelbio.analysis.seq.chipseq.cGIsland;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.TxtReadandWrite;


public class CGRun {
	static String CGFile="/media/winE/Bioinformatics/GenomeData/ucsc_mm9/D__mm9_cpg_all";
	
	//static String CGFile="/media/winG/bioinformation/GenomeData/HumanUCSChg19/rmsk.txt";
	//����peak�ļ�
	static String FlocationFile="/media/winE/NBC/Project/ChIPSeq_CDG101101/result/CpG/mT3RPeak Information.xls";
	//��ȡ������
	static int readRow=2;//�ӵڶ��п�ʼ��
	static int colChr=1;//��һ����chr
	static int colLOC1=2;//�ڶ����ǵ�һ������
	static int colLOC2=3;//�������ǵڶ�������
	static int colLOC=9;//���������м�����
	
	
	//����ļ�
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
	 * ��ȡCGFile�����ͳ�ƶ���peak region������CG��
	 */
	public static void PeakRegionStatstic() 
	{
		
		try { CpG.locatstatistic(FlocationFile, filePeakRegionStatstic, readRow, colChr, colLOC1,colLOC2); } catch (Exception e) { e.printStackTrace(); }
		System.out.println("ok");
	}

	/**
	 * ��ȡCGFile���������ļ���ÿ��peak region�����ǵ�CG����д��FresultFile
	 */
	public static void  PeakRegionDetail() 
	{
		try 
		{ 
			CpG.peakCGDetail(FlocationFile,filePeakRegionDetail, readRow, colChr, colLOC1,colLOC2); 	
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * ���CG������Ϣ
	 */
	public static void CGBackground()
	{
		
		try {
			ArrayList<String[]> result=CpG.getStaticInfo(CGFile);
			TxtReadandWrite CGbackGround=new TxtReadandWrite();
			CGbackGround.setParameter(CGBackGround, true,false);
			CGbackGround.ExcelWrite(result, "\t", 1, 1);
		} catch (Exception e) {	e.printStackTrace(); }
		System.out.println("ok");
	}
	
	
	
	
	
	
	
	
	
	
	
}
