package com.novelbio.analysis.seq.chipseq.regDensity;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.fileOperate.FileOperate;

public class runRegDistance {

	
	//////////////////////////////////////////////
	

	///////////////////////////////////////////////
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		 int[] columnID=new int[3]; columnID[0] = 1; columnID[1] = 2; columnID[2] = 3;//读取peak文件，读取哪几列，依次为 chrID， 起点， 终点
		 int[] colMap = new int[3]; colMap[0] = 1; colMap[1] =2; colMap[2] =3; //mapping 文件中 chr 起点 终点的位置 常规bed文件 1，2，3 王从茂的文件，0，1，2
		 int rowStart = 2;
		 int rowEnd = -1;
		 int binNum = 5; //精度
		 int range = 5000;//上下游多少距离
		 String mapparentFIle = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/all/";
//		 String PeakparentFile = "/media/winE/NBC/Project/ChIPSeq_WJK100909/result/PeakCalling/";//+ "peakCalling/";
		try {
			String mapFilePath=mapparentFIle+"K4all_Extend.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/high.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "Nhigh";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			String mapFilePath=mapparentFIle+"Nextend_sort.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/mid.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "Nmid";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"Nextend_sort.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/low.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "Nlow";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
		try {
			String mapFilePath=mapparentFIle+"2Nextend_sort.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/high.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "2Nhigh";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			String mapFilePath=mapparentFIle+"2Nextend_sort.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/mid.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "2Nmid";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"2Nextend_sort.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/low.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "2Nlow";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
		try {
			String mapFilePath=mapparentFIle+"3Nextend_sort.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/high.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "3Nhigh";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			String mapFilePath=mapparentFIle+"3Nextend_sort.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/mid.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "3Nmid";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"3Nextend_sort.bed";
			String geneFile = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/low.txt";
//			String txtPeakFile= PeakparentFile + "Peak Information.xls";
			String resultPrefix = "3Nlow";
			String resultpath = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTes/expressTssTes/";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//					NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, mapFilePath);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, 
					NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
 
			tssDistance.getGeneNameTssDensity(geneFile, range, 1000, resultpath, resultPrefix);
			tssDistance.getGeneNameGeneEndDensity(geneFile, range, 1000, resultpath, resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
