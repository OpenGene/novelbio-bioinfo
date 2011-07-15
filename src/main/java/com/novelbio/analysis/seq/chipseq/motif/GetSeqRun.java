package com.novelbio.analysis.seq.chipseq.motif;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.chipseq.prepare.GenomeBasePrepare;


public class GetSeqRun {

	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static String gfffilename="/media/winG/bioinformation/Human/hg19_refSeqSortUsingNoChrM.txt";  //gff�ļ�
	static String chrPah="/media/winG/bioinformation/Human/chromFa/";   //chr�ļ���
	//peak��Ϣ�����ı���ʽ����
	static String txtFilepeakFile= "/media/winG/NBC/Project/ChIP-Seq-WJK100909/result/PeakCalling/PeakCallingNewannotation/NA_peaks_Macs.txt";
	//����ı�
	static String txtresultfilename="/media/winG/NBC/Project/ChIP-Seq-WJK100909/result/PeakCalling/PeakCallingNewannotation/NA_peaks_Macs.txtSeq100.txt";//
	static int condition=1; //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	**/
	
	


	static String sep="\t"; //�ı�����ķָ����һ��Ϊ"\t"
	
	static int rowStart=2; //�ӵڼ��ж�ȡ
	static int rowEnd=-1;  //�����ڼ��� ���rowEnd=-1����һֱ����sheet1�ļ���β

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		getseq();
		//GenomeBasePrepare.prepare( NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, null, "", "", "");
		//GenomeBasePrepare.getChrStatistic("/media/winE/Bioinformatics/GenomeData/ucsc_mm9/statistic/chrLengthInfo.txt");
		System.out.println("ok");
	}

	public static void getseq() {
		
		try {
			//peak��Ϣ�����ı���ʽ����
			String txtFilepeakFile= "/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/result/getSummitSeq/IntersectionResults/InterSection.xls";
			//����ı�
			String txtresultfilename="/media/winE/NBC/Project/Project_CDG_Lab/ChIP-Seq_XLY_Paper/result/getSummitSeq/IntersectionResults/outseq.xls";//
			int condition=1; //condition 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻��������� 1: ͨͨ��ȡ���� 2: ͨͨ��ȡ����
			int[] columnID=new int[2]; //��ȡ�ļ��У���int[]���棬�п����м�������ڶ�ȡ�����У�0��chrID��1:peakSummit
			columnID[0]=2;
			columnID[1]=7;
			int peaklength=250; //peak�������˳���
			GetSeq.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM,null, NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, "");
			GetSeq.getPeakSeq(
					peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd, txtresultfilename);
		} catch (Exception e) {			e.printStackTrace();	}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
