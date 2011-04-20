package com.novelBio.chIPSeq.peakAnnotation.peakLoc;


import java.util.ArrayList;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.generalConf.NovelBioConst;


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
		PeakLOC.prepare(NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM,columnID, "UCSC", NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ, "");
		System.out.println("prepare ok");
//		filterPeak();
		//annotation();
		histData();
		
		try {
//			statisticNum();
	//	statisticNum();
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
			PeakLOC.locatstatistic("/media/winE/NBC/Project/ChIPSeq_CDG110330/result/compare/FT5_macsPeak_peaks.xls", "\t", columnID, 2, -1, "/media/winE/NBC/Project/ChIPSeq_CDG110330/result/compare/FT5Statistic");
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
		String ParentFile="/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/";
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=2;
		try {
			 String FpeaksFile=ParentFile+"Tigrtest2.txt";
			 String FannotationFile=ParentFile+"testLOCAnno2.txt";
			 PeakLOC.locatDetail(FpeaksFile, "\t", columnID,2, -1, FannotationFile);
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
		String ParentFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/compare/FT52Tet1gene/";
		int colChrID = 1; int colSummit = 6;
		int rowStart = 1; 
		int[] filterTss = new int[2]; filterTss[0] = 3000; filterTss[1] = 3000;
		int[] filterGenEnd = null;//new int[2]; filterTss[0] = 2000; filterTss[1] = 2500;
		boolean filterGeneBody = false;
		boolean filter5UTR = false;
		boolean filter3UTR = false;
		boolean filterExon = false;
		boolean filterIntron = false;
		try {
			String txtFile=ParentFile+"FT5_macsPeak_peaks.xls";
			String excelResultFile=ParentFile+"FT5_macs_peaksGene.xls";
			PeakLOC.filterPeak(txtFile, "\t", colChrID, colSummit, rowStart, filterTss, filterGenEnd, filterGeneBody, filter5UTR, filter3UTR, filterExon, filterIntron, excelResultFile);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String txtFile=ParentFile+"Tet1 binding sites in wild-type mouse ES cells.txt";
			String excelResultFile=ParentFile+"Tet1_ES_Gene.xls";
			PeakLOC.filterPeak(txtFile, "\t", colChrID, colSummit, rowStart, filterTss, filterGenEnd, filterGeneBody, filter5UTR, filter3UTR, filterExon, filterIntron, excelResultFile);
		} catch (Exception e) {
			// TODO: handle exception
		}

		System.out.println("ok");
	}
	
	
	
	
	
	
	/**
	 * 内含子外显子数量统计
	 * @throws Exception
	 */
	public static void statisticNum() 
	{
		String ParentFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/result/annotation/";
		String resultParentFile = "/media/winE/NBC/Project/ChIPSeq_CDG110330/result/GeneStructure/";
		int[] columnID=new int[2];
		columnID[0]=1;
		columnID[1]=6;
		String[][] intronExonStatistic;
		try {
			String FpeaksFile=ParentFile+"FT5_macsPeak_peaks.xls";
			String prix = "FT5";
			
			
			String genestructureBar = prix + "bar.jpg";
			String genestructureStatistic = prix + "geneStructure";
			intronExonStatistic = PeakLOC.getPeakStaticInfo(FpeaksFile, "\t", columnID, 2, -1);
			TxtReadandWrite txtstatistic=new TxtReadandWrite();
			txtstatistic.setParameter(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_FILE, true,false);
			txtstatistic.ExcelWrite(intronExonStatistic, "\t");
			barPlot();
			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_RESULT_PIC, resultParentFile, genestructureBar);
			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_FILE, resultParentFile,genestructureStatistic);
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
