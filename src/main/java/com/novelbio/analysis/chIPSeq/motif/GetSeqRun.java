package com.novelbio.analysis.chIPSeq.motif;

import com.novelbio.analysis.chIPSeq.prepare.GenomeBasePrepare;
import com.novelbio.analysis.generalConf.NovelBioConst;


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
			String txtFilepeakFile= "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/CSAnovelbio_annotationFiltered.txt";
			//����ı�
			String txtresultfilename="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/motif/CSAFilter80.txt";//
			int condition=1; //condition 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻��������� 1: ͨͨ��ȡ���� 2: ͨͨ��ȡ����
			int[] columnID=new int[2]; //��ȡ�ļ��У���int[]���棬�п����м�������ڶ�ȡ�����У�0��chrID��1:peakSummit
			columnID[0]=1;
			columnID[1]=6;
			int peaklength=80; //peak�������˳���
			GetSeq.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM,null, NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, "");
			GetSeq.getPeakSeq(
					peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd, txtresultfilename);
		} catch (Exception e) {			e.printStackTrace();	}
		
	 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
