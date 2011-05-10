package com.novelbio.analysis.chIPSeq.motif;

import com.novelbio.analysis.chIPSeq.prepare.GenomeBasePrepare;
import com.novelbio.analysis.generalConf.NovelBioConst;


public class GetSeqRun {

	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static String gfffilename="/media/winG/bioinformation/Human/hg19_refSeqSortUsingNoChrM.txt";  //gff文件
	static String chrPah="/media/winG/bioinformation/Human/chromFa/";   //chr文件夹
	//peak信息，以文本形式保存
	static String txtFilepeakFile= "/media/winG/NBC/Project/ChIP-Seq-WJK100909/result/PeakCalling/PeakCallingNewannotation/NA_peaks_Macs.txt";
	//输出文本
	static String txtresultfilename="/media/winG/NBC/Project/ChIP-Seq-WJK100909/result/PeakCalling/PeakCallingNewannotation/NA_peaks_Macs.txtSeq100.txt";//
	static int condition=1; //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	**/
	
	


	static String sep="\t"; //文本里面的分割符，一般为"\t"
	
	static int rowStart=2; //从第几列读取
	static int rowEnd=-1;  //读到第几列 如果rowEnd=-1，则一直读到sheet1文件结尾

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
			//peak信息，以文本形式保存
			String txtFilepeakFile= "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/CSAnovelbio_annotationFiltered.txt";
			//输出文本
			String txtresultfilename="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/motif/CSAFilter80.txt";//
			int condition=1; //condition 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向 1: 通通提取正向 2: 通通提取反向
			int[] columnID=new int[2]; //读取哪几列，用int[]保存，列可以有间隔，现在读取这两列，0：chrID，1:peakSummit
			columnID[0]=1;
			columnID[1]=6;
			int peaklength=80; //peak左右两端长度
			GetSeq.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM,null, NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, "");
			GetSeq.getPeakSeq(
					peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd, txtresultfilename);
		} catch (Exception e) {			e.printStackTrace();	}
		
	 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
