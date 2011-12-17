package com.novelbio.analysis.project.cdg;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.statusAckLog;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.chipseq.regDensity.RegDensity;

public class TssPlot {
	public static void main(String[] args) {
		TssK4();
		TssK27();
		TssFHX();
	}
	public static void TssK4() {
		 int[] columnID=new int[3]; columnID[0] = 1; columnID[1] = 2; columnID[2] = 3;//读取peak文件，读取哪几列，依次为 chrID， 起点， 终点
		 int[] colMap = new int[3]; colMap[0] = 1; colMap[1] =2; colMap[2] =3; //mapping 文件中 chr 起点 终点的位置 常规bed文件 1，2，3 王从茂的文件，0，1，2
		 int rowStart = 2;
		 int rowEnd = -1;
		 int binNum = 5; //精度
		 int range = 5000;//上下游多少距离
		 String mapparentFIle = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		 String PeakparentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/result/peakCalling/SICER/";//+ "peakCalling/";
		 String resultpath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/readsTss/";
		try {
			String mapFilePath=mapparentFIle+"k0_extend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "k0sort-W200-G200-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K4_K0_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"k4_extend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "k4sort-W200-G200-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K4_K4_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"W0_extend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "W0sort-W200-G200-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K4_W0_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"W4_extend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "W4sort-W200-G200-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K4_W4_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void TssK27() {
		 int[] columnID=new int[3]; columnID[0] = 1; columnID[1] = 2; columnID[2] = 3;//读取peak文件，读取哪几列，依次为 chrID， 起点， 终点
		 int[] colMap = new int[3]; colMap[0] = 1; colMap[1] =2; colMap[2] =3; //mapping 文件中 chr 起点 终点的位置 常规bed文件 1，2，3 王从茂的文件，0，1，2
		 int rowStart = 2;
		 int rowEnd = -1;
		 int binNum = 5; //精度
		 int range = 5000;//上下游多少距离
		 String mapparentFIle = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		 String PeakparentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/PeakCalling/";//+ "peakCalling/";
		 String resultpath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/readsTss/";
		try {
			String mapFilePath=mapparentFIle+"WEextend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "WEseSort-W200-G600-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K27_WE_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"KEextend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "KEseSort-W200-G600-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K27_KE_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"2Wextend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "2WseSort-W200-G600-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K27_2W_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"2Kextend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "2KseSort-W200-G600-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K27_2K_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			String mapFilePath=mapparentFIle+"4Kextend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "HSZ_K-4.clean.fq_SE-W200-G600-E100.scoreisland_score_35.xls";
			String resultPrefix = "H3K27_4K_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"4Wextend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "HSZ_W-4.clean.fq_SE-W200-G600-E100.scoreisland_score35.xls";
			String resultPrefix = "H3K27_4W_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	public static void TssFHX() {
		 int[] columnID=new int[3]; columnID[0] = 1; columnID[1] = 2; columnID[2] = 3;//读取peak文件，读取哪几列，依次为 chrID， 起点， 终点
		 int[] colMap = new int[3]; colMap[0] = 1; colMap[1] =2; colMap[2] =3; //mapping 文件中 chr 起点 终点的位置 常规bed文件 1，2，3 王从茂的文件，0，1，2
		 int rowStart = 2;
		 int rowEnd = -1;
		 int binNum = 5; //精度
		 int range = 5000;//上下游多少距离
		 String mapparentFIle = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		 String PeakparentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/FHE/";//+ "peakCalling/";
		 String resultpath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/readsTss/";
		try {
			String mapFilePath=mapparentFIle+"FHE.clean.fq_ExtendSort.bed";
			
			String txtPeakFile= PeakparentFile + "FHE_peaks.xls";
			String resultPrefix = "FHE_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String mapFilePath=mapparentFIle+"FX2extend_sort.bed";
			
			String txtPeakFile= PeakparentFile + "FX2_peaks.xls";
			String resultPrefix = "FH2_sicer_35_Peak";
			RegDensity tssDistance=new RegDensity();
			tssDistance.setInvNum(binNum);
			tssDistance.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, colMap,NovelBioConst.GENOME_GFF_TYPE_UCSC, 
					NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, mapFilePath);
			tssDistance.getPeakInfo(txtPeakFile, columnID, rowStart, rowEnd);	
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
