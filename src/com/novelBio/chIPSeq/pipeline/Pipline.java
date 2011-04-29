package com.novelBio.chIPSeq.pipeline;

import java.io.File;
import java.util.ArrayList;

import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataStructure.ArrayOperate;
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.base.genome.GffLocatCod;
import com.novelBio.base.genome.GffToCG;
import com.novelBio.chIPSeq.cGIsland.CpG;
import com.novelBio.chIPSeq.motif.GetSeq;
import com.novelBio.chIPSeq.peakAnnotation.peakLoc.PeakLOC;
import com.novelBio.chIPSeq.peakAnnotation.symbolAnnotation.SymbolDesp;
import com.novelBio.chIPSeq.prepare.GenomeBasePrepare;
import com.novelBio.chIPSeq.regDensity.RegDensity;
import com.novelBio.generalConf.NovelBioConst;


public class Pipline extends GenomeBasePrepare{
	protected static GffLocatCod gffLocatCod=new GffLocatCod();
	static int invNum=10;
	static String sep="\t";
	static String CGFIle="";
	static String DataName1="";
	static String DataName2="";
	static String PeakFile1="";
	static String PeakFile2="";
	static String ResultFilePath="";
	static int readRow=2;//从第二行开始读
	static int colChr=1;//第一列是chr
	static int colLOCstart=2;//第二列是第一个坐标
	static int colLOCend=3;//第三列是第二个坐标
	static int colLOCsummit=9;//第七列是中间坐标
	
	Object[][] LOCInfo1=null;
	Object[][] LOCInfo2=null;
	//CpG
	String [][] LOCCpGinfo1=null;
	String [][] LOCCpGinfo2=null;
	
	
	public static void main(String[] args) 
	{

		try {
			analysis();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 首先建立annotation文件夹，在文件夹中放入peak文件，然后设置参数
	 * @param args
	 * @throws Exception 
	 */
	public static void analysis() throws Exception 
	{
		/////////////  配置文件  //////////////////////////////////////////////
		 int[] colMap=new int[3];
		 colMap[0]=1;
		 colMap[1]=2;
		 colMap[2]=3;
		String mappingBedFile = "/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/mapping/CSA_Treat_Cal_Sort.bed";
		PeakLOC.prepare(NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM,
				colMap,
				NovelBioConst.GENOME_GFF_TYPE_TIGR, 
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, 
				mappingBedFile);
		System.out.println("prepare ok");
		///////////////////////////////////////////////////////////
		
		/////////////////  文件夹配置 //////////////////////////////////////////

		//目标文件夹
		String ResultFile="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/";
		//目标实验前缀
		String resultPrix ="CSAnovelbio";
		//peak文件名
		String annotation = ResultFile + "annotation/";
		String FpeaksFile=annotation+"CSA_peaks.xls";
		//mapping文件
		///////////////// 参数 ///////////////////////////////////
		//peak的summit点
		int[] colPeak=new int[2]; colPeak[0]=1; colPeak[1]=6;
		//peak的两个端点
		int[] colPeakChr=new int[3];colPeakChr[0]=1;colPeakChr[1]=2;colPeakChr[2]=3;
		//tss和geneEnd的range
		int range = 10000;
		//提取序列，condition 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向 1: 通通提取正向 2: 通通提取反向
		int condition=0;
		
		
		////////////////////////   annotation   ////////////////////////
//		FileOperate.createFolder(annotation);
		
		String FannotationFile=annotation+resultPrix+"_annotation.xls";
		String statistics = annotation+ resultPrix+"statistics.txt";
		////////////////////////   Peak Gene Structure   ////////////////////////
		String peakGeneStructure = ResultFile + "GeneStructure/";
		FileOperate.createFolder(peakGeneStructure);
		////////////////////////   Peak Destribution   ////////////////////////
		String peakHist = ResultFile + "Peak_destribution/";
		FileOperate.createFolder(peakHist);
		//////////////////// Reads in Tss and GeneEnd ////////////////////////////////
		String readsInRegion = ResultFile + "readsInRegion/";
		FileOperate.createFolder(readsInRegion);
		//////////////////// motif seq ////////////////////////////////
		String motif = ResultFile + "motif/";
		FileOperate.createFolder(motif);
		////////////////////////////////////////////////////////////////////////////////////////////////
		annotation(FpeaksFile, colPeak, FannotationFile, statistics, peakHist, resultPrix);
		statisticNum(FpeaksFile, colPeak, resultPrix, peakGeneStructure);
		readsInRegion(colPeakChr, 10, range, FpeaksFile, readsInRegion, resultPrix);
		getseq(FpeaksFile, colPeakChr, motif, resultPrix);
	}
	
	
	/**
	 * peak Annotation
	 */
	public static void  annotation(String peaksFile, int[] columnID, String annotationFile, String statistics, String PeakHist,String prix) {
		//需要是excel文件
		String ParentFile="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/";

		//定位区域
		int[] region = new int[3];//0:UpstreamTSSbp 1:DownStreamTssbp 2:GeneEnd3UTR
		region[0] = 3000; region[1] = 3000; region[2] = 100;
		try {
			 PeakLOC.histTssGeneEnd(peaksFile, "\t", columnID, 2, -1, PeakHist, prix);
			 
			 PeakLOC.locatstatistic(peaksFile, "\t", columnID, 2, -1, statistics);
			 
			 PeakLOC.locatDetail(peaksFile, "\t", columnID,2, -1, annotationFile,region);
			 ////////////////////////////// 获得文件有几列 ////////////////////////////////////////
			 TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
			 txtReadandWrite.setParameter(annotationFile, false, true);
			 int columnNum=0;
			 try {
				 columnNum = txtReadandWrite.ExcelColumns("\t");
			 } catch (Exception e2) {
			 }
			 int columnRead=columnNum-1;
			 ///////////////////////////////////////////////////////////////////////////////////////////////
			 int rowStart=2;
			 SymbolDesp.getRefSymbDesp(39947,annotationFile, columnRead, rowStart, columnRead);
			 SymbolDesp.getRefSymbDesp(39947,annotationFile, columnRead-2, rowStart, columnRead-2);
			 SymbolDesp.getRefSymbDesp(39947,annotationFile, columnRead-4, rowStart, columnRead-4);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok");
	}
	
	/**
	 * 画Tss和GeneEnd的reads精度图
	 * @throws Exception 
	 */
	public static void readsInRegion(int[] colPeakChr,int binNum,int range, String txtPeakFile,String resultpath,String resultPrefix) throws Exception 
	{
		 int rowStart = 2;
		 int rowEnd = -1;
			RegDensity tssDistance=new RegDensity();
//			int[] colMap = new int[3];colMap[0] = 0; colMap[1] =1; colMap[2] =2;//王从茂的bed
			tssDistance.setInvNum(binNum);	
			tssDistance.getPeakInfo(txtPeakFile, colPeakChr, rowStart, rowEnd);	
			tssDistance.getRegionDensity("GeneEnd",range, range*2/binNum,resultpath,resultPrefix);
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
	}
	/**
	 * 提取序列
	 * @param txtFilepeakFile
	 * @param colPeak
	 * @param resultPath
	 * @param prix
	 */
	public static void getseq(String txtFilepeakFile, int[] colPeak,String resultPath ,String prix) {
		
		try {
			//输出文本
			
			int condition=0; //condition 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向 1: 通通提取正向 2: 通通提取反向

			int peaklength=80; //peak左右两端长度
			String txtresultfilename=resultPath +"/"+ prix + peaklength +".txt";
			GetSeq.getPeakSeq(peaklength, condition, txtFilepeakFile, sep, colPeak, 2, -1, txtresultfilename);
		} catch (Exception e) {			e.printStackTrace();	}
	}
	
	
	
	/**
	 * 内含子外显子数量统计
	 * @throws Exception
	 */
	public static void statisticNum(String peakFile, int[] colPeak ,String prix,String resultPath) 
	{
		String[][] intronExonStatistic;
		try {
			String genestructureBar = prix + "bar.jpg";
			String genestructureStatistic = prix + "geneStructure";
			intronExonStatistic = PeakLOC.getPeakStaticInfo(peakFile, "\t", colPeak, 2, -1);
			TxtReadandWrite txtstatistic=new TxtReadandWrite();
			txtstatistic.setParameter(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_FILE, true,false);
			txtstatistic.ExcelWrite(intronExonStatistic, "\t");
			barPlot();
			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_RESULT_PIC, resultPath, genestructureBar,true);
			FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_FILE, resultPath,genestructureStatistic,true);
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
