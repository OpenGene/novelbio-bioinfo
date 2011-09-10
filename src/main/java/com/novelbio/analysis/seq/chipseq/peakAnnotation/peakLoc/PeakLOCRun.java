package com.novelbio.analysis.seq.chipseq.peakAnnotation.peakLoc;


import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.chipseq.peakAnnotation.symbolAnnotation.SymbolDesp;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;


public class PeakLOCRun {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		 int[] columnID=new int[3];
		columnID[0]=1;
		columnID[1]=2;
		columnID[2]=3;
		PeakLOC.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM,null,NovelBioConst.GENOME_GFF_TYPE_UCSC,
				NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, "");
		
//		PeakLOC.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM,null,NovelBioConst.GENOME_GFF_TYPE_UCSC,
//				NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, "");
		
		System.out.println("prepare ok");
		filterPeak();
//		annotation();
		//histData();
		System.out.println(" ok");
		try {
//			statisticNum();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();																																																																																																																																																																																																																																																														
		}
	}
	
	
	
	
	
	
	/**Human
	static String chrFilePath="/media/winG/bioinformation/Human/chromFa";
	static String refSortUsingFile="/media/winG/bioinformation/Human/hg19_refSeqSortUsingNoChrM.txt";
	**/
	//**Mouse
	static String chrFilePath="/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/chromFa";
	static String refSortUsingFile="/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/refseqSortUsing.txt";

	
	
	//static String Rworkspace="/media/winE/bioinformation/R/practice_script/platform/";


 
 
	static String RwritehistFile="/media/winE/NBC/Project/ChIPSeq_CDG1011101/result/annotation/mCE/histDataRef.txt";
	
	/**
	 * 获得peak与基因组的关系，用于画peak和TSS分布图之类
	 */
	public static void  histData() 
	{
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=6;
		try {
			PeakLOC.locatstatistic("/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/CSA_peaks.xls", "\t", 
					columnID, 2, -1, "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/statistic/CSAStatistic");
			//PeakLOC.locatstatistic(refSortUsingFile, FpeaksFile, "\t", columnID,2, -1, FwritehistFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok");
	}
	
	
 

	/**
	 * peak Annotation
	 */
	public static void  annotation() {
		//需要是excel文件
		String ParentFile="/media/winE/NBC/Project/MethyArray_MXY110901/rawdata/";
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=12;
		int[] region = new int[3];
		region[0] = 5000; region[1] = 3000; region[2] = 200;
		try {
			 String FpeaksFile=ParentFile+"AMS_All.xls";
			 String FannotationFile=ParentFile+"AMS_All_Annotation.xls";
			 PeakLOC.locatDetail(FpeaksFile, "\t", columnID,2, -1, FannotationFile,region);
			// String RpeaksFile=ParentFile+"RPeak Information.xls";
			// String RannotationFile=ParentFile+"RPeak_annotation5k.xls";
			//PeakLOC.locatDetail(RpeaksFile, "\t", columnID,2, -1, RannotationFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok");
	}
	
	
	/**
	 * peak Annotation
	 */
	public static void  filterPeak() {
		//需要是excel文件
		String ParentFile="/media/winE/NBC/Project/MethyArray_QZL110907/";
		int taxID = 10090;
		int colChrID = 1; int colSummit = 5;
		int rowStart = 1; 
		int[] filterTss = new int[2]; filterTss[0] = 5000; filterTss[1] = 5000;
		int[] filterGenEnd = new int[2]; filterGenEnd[0] = 0; filterGenEnd[1] = 0;
		filterGenEnd = null;
		boolean filterGeneBody = false;
		boolean filter5UTR = false;
		boolean filter3UTR = false;
		boolean filterExon = false;
		boolean filterIntron = false;
		try {
			String txtFile=ParentFile+"KC190 VS UN12D_Cope.txt";
			String excelResultFile=ParentFile+"KC190 VS UN12D_Cope_filter.txt";
			PeakLOC.filterPeak(txtFile, "\t", colChrID, colSummit, rowStart, filterTss, filterGenEnd, filterGeneBody, filter5UTR, filter3UTR, filterExon, filterIntron, excelResultFile);
			int columnNum=0;
			 TxtReadandWrite txtReadandWrite=new TxtReadandWrite(excelResultFile, false);
			try {
				columnNum = txtReadandWrite.ExcelColumns(2,"\t");
				System.out.println(columnNum);
			} catch (Exception e2) {
			}
			int columnRead=columnNum-1;
			SymbolDesp.getRefSymbDesp(taxID,excelResultFile, columnRead, rowStart, columnRead);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok");
	}
	
	
	
	
	
	
	/**
	 * 内含子外显子数量统计
	 * @throws Exception
	 */
	public static void statisticNum() 
	{
		String ParentFile="/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110608Paper/annotation/";
		String resultParentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110608Paper/GeneStructure/";
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=6;
		String[][] intronExonStatistic;
		try {
			String FpeaksFile=ParentFile+"PHF8_peaks.txt";
			String prix = "PHF8";
			
			
			String genestructureBar = prix + "bar.jpg";
			String genestructureStatistic = prix + "geneStructure";
			intronExonStatistic = PeakLOC.getPeakStaticInfo(FpeaksFile, "\t", columnID, 2, -1);
			TxtReadandWrite txtstatistic=new TxtReadandWrite();
			txtstatistic.setParameter(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_FILE, true,false);
			txtstatistic.ExcelWrite(intronExonStatistic, "\t");
			barPlot();
			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_RESULT_PIC, resultParentFile, genestructureBar,true);
			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_FILE, resultParentFile,genestructureStatistic,true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 调用R画图
	 * @throws Exception
	 */
	private static void barPlot() throws Exception
	{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+ NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		System.out.println("ok");
	}
	
	

}
