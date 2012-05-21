package com.novelbio.analysis.seq.chipseq.peakOverlap;

import java.util.ArrayList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.chipseq.repeatMask.RepeatMask;
import com.novelbio.analysis.seq.genome.GffPeakOverlap;
import com.novelbio.base.dataOperate.TxtReadandWrite;



public class PeakOverlap 
{
	public static void main(String[] args) {
		BedSeq bedSeq = new BedSeq("/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/FHE.all_SE.bed");
		bedSeq = bedSeq.extend(240);
		bedSeq.sortBedFile();
	}
	public static void main2(String[] args) {

		try {
			String parentFile1 = "/home/zong0jie/����/�½��ļ���/";
			 String file2N=parentFile1+"FHE_Peak_FilteredP30FE3_add.xls";
			 
			 String parentFile2 = "/media/winE/NBC/Project/ChIPSeq_CDG101101CT/result/annotation/";
			 String  fileN=parentFile1+"FHE_peaks.xls";
			/**
			 * ÿ��peakOverlap��ϸ��
			 */
			 String parentFile3 = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/";
			 String txtPeakOverlapFileN2N=parentFile1+"FHEvsFHEadd";
			 String txtPeakOverlapFileN2NStic=parentFile1+"FHEvsFHEaddstatistic";

			PeakOverLap(fileN,file2N,txtPeakOverlapFileN2N);
		    PeakStatistic("FHE", "FHEadd",fileN,file2N,txtPeakOverlapFileN2NStic);
		    
//			PeakOverLap(fileN,file3N,txtPeakOverlapFileN3N);
//		    PeakStatistic("N", "3N",fileN,file3N,txtPeakOverlapFileN3NStic);
//		    
//			PeakOverLap(file2N,file3N,txtPeakOverlapFile2N3N);
//		    PeakStatistic("2N", "3N",file2N,file3N,txtPeakOverlapFile2N3NStic);
		    
	
		} catch (Exception e) {}
		System.gc();
		
	}
	
	/**
	 * ������������peak��overlap�����ע��û�н��ص�peak�ϲ�
	 * @param fileMinus ����
	 * @param filePlus ����
	 * @param txtPeakOverlapFile �����ÿ��peakOverlap��ϸ��
	 */
	public static void PeakOverLap(String fileMinus, String filePlus, String txtPeakOverlapFile) {
		int colChr=1;
		int peakStart=2;
		int peakEnd=3;
		int rowNum=2;
		
		GffPeakOverlap cdgPeak=new GffPeakOverlap();
		try {
			cdgPeak.readPeakFile(filePlus, fileMinus, colChr, peakStart, peakEnd, rowNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<String[]> resultexcel=cdgPeak.compareMinus2Plus(false);
		TxtReadandWrite txtPeakOverlap=new TxtReadandWrite();
		txtPeakOverlap.setParameter(txtPeakOverlapFile, true,false);
		try {
			txtPeakOverlap.ExcelWrite(resultexcel, "\t", 1, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok");
	}
	
	/**
	 * ����PeakOverlap��ͳ�ƽ��,������peak�ĺϲ�����
	 */
	public static void PeakStatistic(String MinNam, String PlusName,String fileMinus, String filePlus,String txtPeakOverlapStatisticFile) {
	
		int colChr=1;
		int peakStart=2;
		int peakEnd=3;
		int rowNum=2;
		
		GffPeakOverlap cdgPeak=new GffPeakOverlap();
		try {
			cdgPeak.readPeakFile(filePlus, fileMinus, colChr, peakStart, peakEnd, rowNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int minusLength=cdgPeak.getMinusallLength();
		int plusLength=cdgPeak.getPlusallLength();
		int[] peakOverlapLength=cdgPeak.getOverlapInfo();
		TxtReadandWrite txtPeakOverlap=new TxtReadandWrite();
		ArrayList<String[]> resultexcel=new ArrayList<String[]>();

		String[] result0=new String[2];
		result0[0]=PlusName+" Length";
		result0[1]=plusLength+"";
		resultexcel.add(result0);
		String[] result1=new String[2];
		result1[0]=MinNam+" Length";
		result1[1]=minusLength+"";
		resultexcel.add(result1);

		String[] result2=new String[2];
		result2[0]="peakOverlapLength";
		result2[1]=peakOverlapLength[0]+"";
		resultexcel.add(result2);
		String[] result3=new String[2];
		result3[0]="Peak Overlap Proportion To "+PlusName+" Length";
		result3[1]=(double)peakOverlapLength[0]/plusLength+"";
		resultexcel.add(result3);
		String[] result4=new String[2];
		result4[0]="Peak Overlap Proportion To "+MinNam+" Length";
		result4[1]=(double)peakOverlapLength[0]/minusLength+"";
		resultexcel.add(result4);
		
		
		String[] result5=new String[2];
		result5[0]=PlusName+" Peak Num";
		result5[1]=cdgPeak.getPlusNum()+"";
		resultexcel.add(result5);
		
		String[] result6=new String[2];
		result6[0]=MinNam+" Peak Num";
		result6[1]=cdgPeak.getMinusNum()+"";
		resultexcel.add(result6);
		
		String[] result7=new String[2];
		result7[0]=MinNam+" To "+PlusName+" OverlapNum";
		result7[1]=peakOverlapLength[1]+"";
		resultexcel.add(result7);
		
		String[] result8=new String[2];
		result8[0]=PlusName+"To "+MinNam+" OverlapNum";
		result8[1]=peakOverlapLength[3]+"";
		resultexcel.add(result8);

		String[] result9=new String[2];
		result9[0]="peakOverlapProportion To "+PlusName+" Num";
		result9[1]=(double)peakOverlapLength[3]/cdgPeak.getPlusNum()+"";
		resultexcel.add(result9);
		
		String[] result10=new String[2];
		result10[0]="peakOverlapProportion To "+MinNam+" Num";
		result10[1]=(double)peakOverlapLength[1]/cdgPeak.getMinusNum()+"";
		resultexcel.add(result10);
		
		txtPeakOverlap.setParameter(txtPeakOverlapStatisticFile, true,false);
		try {
			txtPeakOverlap.ExcelWrite(resultexcel, "\t", 1, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("ok");
	}
}
