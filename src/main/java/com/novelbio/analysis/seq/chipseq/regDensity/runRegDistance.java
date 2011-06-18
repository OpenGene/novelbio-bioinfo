package com.novelbio.analysis.seq.chipseq.regDensity;

import com.novelbio.analysis.generalConf.NovelBioConst;

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
		 int range = 10000;//上下游多少距离
		 String mapparentFIle="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/mapping/";
		 String PeakparentFile = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/";
		try {
			String mapFilePath=mapparentFIle+"CSA_Treat_Cal_Sort.bed";
			
			String txtPeakFile= PeakparentFile + "CSA sepis peak Filter.xls";
			
			String resultpath = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/regionReads/";
			String resultPrefix = "CSA sepis peak Filter";
			
			RegDensity tssDistance=new RegDensity();
			
//			int[] colMap = new int[3];colMap[0] = 0; colMap[1] =1; colMap[2] =2;//王从茂的bed
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, mapFilePath);
//			String geneFIle = "/home/zong0jie/桌面/CDG/CDG20110201/CTvsmT3/IntersectionResults/InterSectionGeneName.xls";
//			tssDistance.getGeneNameTssDensity(geneFIle, 10000, 10000, "/media/winE/Bioinformatics/R/practice_script/platform/", resultpath, resultPrefix);
//			tssDistance.getGeneNameGeneEndDensity(geneFIle, 10000, 10000, "/media/winE/Bioinformatics/R/practice_script/platform/", resultpath, resultPrefix);
			
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("GeneEnd",range, range*2/binNum,resultpath,resultPrefix);
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
//		try {
//			String mapFilePath=mapparentFIle+"sortD4K.filt.bed";
//			
//			String txtPeakFile= PeakparentFile + "D4K.filt_peaks.xls";
//			
//			String resultpath = "/media/winE/NBC/Project/ChIPSeq_CDG101011/result/readsInRegion/";
//			String resultPrefix = "D4K";
//			
//			RegDensity tssDistance=new RegDensity();
//			
////			int[] colMap = new int[3];colMap[0] = 0; colMap[1] =1; colMap[2] =2;//王从茂的bed
//			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
////			String geneFIle = "/home/zong0jie/桌面/CDG/CDG20110201/CTvsmT3/IntersectionResults/InterSectionGeneName.xls";
////			tssDistance.getGeneNameTssDensity(geneFIle, 10000, 10000, "/media/winE/Bioinformatics/R/practice_script/platform/", resultpath, resultPrefix);
////			tssDistance.getGeneNameGeneEndDensity(geneFIle, 10000, 10000, "/media/winE/Bioinformatics/R/practice_script/platform/", resultpath, resultPrefix);
//			
//			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
//			tssDistance.getRegionDensity("GeneEnd",range, range*2/binNum,resultpath,resultPrefix);
//			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			String mapFilePath=mapparentFIle+"sortD4W.filt.bed";
//			
//			String txtPeakFile= PeakparentFile + "D4W.filt_peaks.xls";
//			
//			String resultpath = "/media/winE/NBC/Project/ChIPSeq_CDG101011/result/readsInRegion/";
//			String resultPrefix = "D4W";
//			
//			RegDensity tssDistance=new RegDensity();
//			
////			int[] colMap = new int[3];colMap[0] = 0; colMap[1] =1; colMap[2] =2;//王从茂的bed
//			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
////			String geneFIle = "/home/zong0jie/桌面/CDG/CDG20110201/CTvsmT3/IntersectionResults/InterSectionGeneName.xls";
////			tssDistance.getGeneNameTssDensity(geneFIle, 10000, 10000, "/media/winE/Bioinformatics/R/practice_script/platform/", resultpath, resultPrefix);
////			tssDistance.getGeneNameGeneEndDensity(geneFIle, 10000, 10000, "/media/winE/Bioinformatics/R/practice_script/platform/", resultpath, resultPrefix);
//			
//			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
//			tssDistance.getRegionDensity("GeneEnd",range, range*2/binNum,resultpath,resultPrefix);
//			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			String mapFilePath=mapparentFIle+"sortEK.filt.bed";
//			
//			String txtPeakFile= PeakparentFile + "EK.filt_peaks.xls";
//			
//			String resultpath = "/media/winE/NBC/Project/ChIPSeq_CDG101011/result/readsInRegion/";
//			String resultPrefix = "EK";
//			
//			RegDensity tssDistance=new RegDensity();
//			
////			int[] colMap = new int[3];colMap[0] = 0; colMap[1] =1; colMap[2] =2;//王从茂的bed
//			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
////			String geneFIle = "/home/zong0jie/桌面/CDG/CDG20110201/CTvsmT3/IntersectionResults/InterSectionGeneName.xls";
////			tssDistance.getGeneNameTssDensity(geneFIle, 10000, 10000, "/media/winE/Bioinformatics/R/practice_script/platform/", resultpath, resultPrefix);
////			tssDistance.getGeneNameGeneEndDensity(geneFIle, 10000, 10000, "/media/winE/Bioinformatics/R/practice_script/platform/", resultpath, resultPrefix);
//			
//			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
//			tssDistance.getRegionDensity("GeneEnd",range, range*2/binNum,resultpath,resultPrefix);
//			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			String mapFilePath=mapparentFIle+"Rfragment.fasta";
//			
//			String txtPeakFile= PeakparentFile + "k0_macsPeak_peaks.txt";
//			
//			String resultpath = "/media/winE/NBC/Project/ChIPSeq_CDG101101/result/regionReads/selectGene/";
//			String resultPrefix = "mCEF";
//			
//			RegDensity tssDistance = new RegDensity();
////			int[] colMap = new int[3];colMap[0] = 0; colMap[1] =1; colMap[2] =2;//王从茂的bed
//			tssDistance.setInvNum(binNum);
//			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
//			String geneFile1 = "/home/zong0jie/桌面/gene.txt";
////			String geneFile2 = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/statisticInfo/randomRefseqGene/mouseRefseqRandom2.txt";
//			tssDistance.getGeneNameTssDensity("NM_009233", 10000, "Sox1R", 10000, resultpath);
//			tssDistance.getGeneNameTssDensity("NM_010277", 10000, "GfapR", 10000, resultpath);
//			tssDistance.getGeneNameTssDensity("NM_013627", 10000, "Pax6R", 10000, resultpath);
//			tssDistance.getGeneNameTssDensity("NM_009234", 10000, "Sox11R", 10000, resultpath);
//			tssDistance.getGeneNameTssDensity("NM_016968", 10000, "Olig1R", 10000, resultpath);
//			tssDistance.getGeneNameTssDensity("NM_016967", 10000, "Olig2R", 10000, resultpath);
//			tssDistance.getGeneNameTssDensity("NM_008629", 10000, "Msi1R", 10000, resultpath);
//			tssDistance.getGeneNameTssDensity("NM_023279", 10000, "Tubb3R", 10000, resultpath);
//			tssDistance.getGeneNameTssDensity("NM_008632", 10000, "Mtap2R", 10000, resultpath);
////			tssDistance.getGeneNameTssDensity(geneFile2, 10000, 10000, resultpath, resultPrefix+"random2");
////			tssDistance.getGeneNameGeneEndDensity(geneFile2, 10000, 10000, resultpath, resultPrefix+"random2");
//			
//			
//			
////			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
////			tssDistance.getRegionDensity("GeneEnd",range, range*2/binNum,resultpath,resultPrefix);
////			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
//		
		
		
		
		
	}
}
