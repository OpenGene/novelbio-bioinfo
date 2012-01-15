package com.novelbio.analysis.seq.chipseq.readsChrDensity;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.fileOperate.FileOperate;


public class runReadsDensity 
{

	public static void main(String[] args) {
		plotBGK4();
	}
	/**
	 * 对于macs的结果bed文件，首先需要进行排序
	 * 排序方法
	 * sort -k1,1 -k2,2n test #第一列起第一列终止排序，第二列起第二列终止按数字排序
	 * @param args
	 */
	public static void plot() {
		///**mouse
		String parentFile="/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		String resultFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/readsChr/";
		int invNum=5;
		int tagLength= 250;//考虑自动获取
		try {
			
			String mapFFile=parentFile+"2Kextend_sort.bed";
			String mapRFile="";
			String prix = "2K";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			
			String mapFFile=parentFile+"2Wextend_sort.bed";
			String mapRFile="";
			String prix = "2W";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			
			String mapFFile=parentFile+"KEextend_sort.bed";
			String mapRFile="";
			String prix = "KE";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			
			String mapFFile=parentFile+"WEextend_sort.bed";
			String mapRFile="";
			String prix = "WE";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			
			String mapFFile=parentFile+"4Kextend_sort.bed";
			String mapRFile="";
			String prix = "4K";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			
			String mapFFile=parentFile+"4Wextend_sort.bed";
			String mapRFile="";
			String prix = "4W";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void plotBGK27() {
		///**mouse
		String parentFile="/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		String resultFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/readsChrBG/";
		int invNum=5;
		int tagLength= 250;//考虑自动获取
		
		String peakParentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/compareSICER/compareWT0WT4/";
		
		
		int colChrID = 1;
		int colStartLoc = 2;
		int colEndLoc = 3;
	try {
			String PeakFile = peakParentPath + "HSZ_W-4.clean.fq_SE-W200-G600-E100.scoreisland";
			String mapFFile=parentFile+"4Wextend_sort.bed";
			String mapRFile="";
			String prix = "4W";
			String sep="\t"; 
			System.out.println("4W");
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDistBG(PeakFile, 2, colChrID, colStartLoc, colEndLoc);
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String PeakFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/compareSICER/compareK0K4/" + "HSZ_K-4.clean.fq_SE-W200-G600-E100.scoreisland";
			String mapFFile=parentFile+"4Kextend_sort.bed";
			String mapRFile="";
			String prix = "4K";
			String sep="\t"; 
			System.out.println("4K");
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDistBG(PeakFile, 2, colChrID, colStartLoc, colEndLoc);
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String PeakFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/compareSICER/compareK0K4/" + "KEse-W200-G600-E100.scoreisland";
			String mapFFile=parentFile+"KEextend_sort.bed";
			String mapRFile="";
			String prix = "KE";
			String sep="\t"; 
			System.out.println("KE");
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDistBG(PeakFile, 2, colChrID, colStartLoc, colEndLoc);
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String PeakFile = peakParentPath + "WEse-W200-G600-E100.scoreisland";
			String mapFFile=parentFile+"WEextend_sort.bed";
			String mapRFile="";
			String prix = "WE";
			String sep="\t"; 
			System.out.println("WE");
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDistBG(PeakFile, 2, colChrID, colStartLoc, colEndLoc);
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void plotBGK4() {
		///**mouse
		String parentFile="/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		String resultFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/readsChrBGK4/";
		int invNum=5;
		int tagLength= 250;//考虑自动获取
		
		String peakParentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/result/peakCalling/SICER/";
		
		
		int colChrID = 1;
		int colStartLoc = 2;
		int colEndLoc = 3;
	try {
			String PeakFile = peakParentPath + "k0sort-W200-G200-E100.scoreisland";
			String mapFFile=parentFile+"k0_extend_sort.bed";
			String mapRFile="";
			String prix = "K0";
			String sep="\t"; 
			System.out.println("K0");
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDistBG(PeakFile, 1, colChrID, colStartLoc, colEndLoc);
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String PeakFile = peakParentPath + "k4sort-W200-G200-E100.scoreisland";
			String mapFFile=parentFile+"k4_extend_sort.bed";
			String mapRFile="";
			String prix = "K4";
			String sep="\t"; 
			System.out.println("K4");
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDistBG(PeakFile, 1, colChrID, colStartLoc, colEndLoc);
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String PeakFile = peakParentPath + "W0sort-W200-G200-E100.scoreisland";
			String mapFFile=parentFile+"W0_extend_sort.bed";
			String mapRFile="";
			String prix = "W0";
			String sep="\t"; 
			System.out.println("W0");
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDistBG(PeakFile, 1, colChrID, colStartLoc, colEndLoc);
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String PeakFile = peakParentPath + "W4sort-W200-G200-E100.scoreisland";
			String mapFFile=parentFile+"W4_extend_sort.bed";
			String mapRFile="";
			String prix = "W4";
			String sep="\t"; 
			System.out.println("W4");
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDistBG(PeakFile, 1, colChrID, colStartLoc, colEndLoc);
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
