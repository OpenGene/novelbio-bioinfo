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
	static int readRow=2;//�ӵڶ��п�ʼ��
	static int colChr=1;//��һ����chr
	static int colLOCstart=2;//�ڶ����ǵ�һ������
	static int colLOCend=3;//�������ǵڶ�������
	static int colLOCsummit=9;//���������м�����
	
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
	 * ���Ƚ���annotation�ļ��У����ļ����з���peak�ļ���Ȼ�����ò���
	 * @param args
	 * @throws Exception 
	 */
	public static void analysis() throws Exception 
	{
		/////////////  �����ļ�  //////////////////////////////////////////////
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
		
		/////////////////  �ļ������� //////////////////////////////////////////

		//Ŀ���ļ���
		String ResultFile="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/";
		//Ŀ��ʵ��ǰ׺
		String resultPrix ="CSAnovelbio";
		//peak�ļ���
		String annotation = ResultFile + "annotation/";
		String FpeaksFile=annotation+"CSA_peaks.xls";
		//mapping�ļ�
		///////////////// ���� ///////////////////////////////////
		//peak��summit��
		int[] colPeak=new int[2]; colPeak[0]=1; colPeak[1]=6;
		//peak�������˵�
		int[] colPeakChr=new int[3];colPeakChr[0]=1;colPeakChr[1]=2;colPeakChr[2]=3;
		//tss��geneEnd��range
		int range = 10000;
		//��ȡ���У�condition 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻��������� 1: ͨͨ��ȡ���� 2: ͨͨ��ȡ����
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
		//��Ҫ��excel�ļ�
		String ParentFile="/media/winE/NBC/Project/Project_ZDB_Lab/ZH/CSACHIP-SEQ/result/annotation/";

		//��λ����
		int[] region = new int[3];//0:UpstreamTSSbp 1:DownStreamTssbp 2:GeneEnd3UTR
		region[0] = 3000; region[1] = 3000; region[2] = 100;
		try {
			 PeakLOC.histTssGeneEnd(peaksFile, "\t", columnID, 2, -1, PeakHist, prix);
			 
			 PeakLOC.locatstatistic(peaksFile, "\t", columnID, 2, -1, statistics);
			 
			 PeakLOC.locatDetail(peaksFile, "\t", columnID,2, -1, annotationFile,region);
			 ////////////////////////////// ����ļ��м��� ////////////////////////////////////////
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
	 * ��Tss��GeneEnd��reads����ͼ
	 * @throws Exception 
	 */
	public static void readsInRegion(int[] colPeakChr,int binNum,int range, String txtPeakFile,String resultpath,String resultPrefix) throws Exception 
	{
		 int rowStart = 2;
		 int rowEnd = -1;
			RegDensity tssDistance=new RegDensity();
//			int[] colMap = new int[3];colMap[0] = 0; colMap[1] =1; colMap[2] =2;//����ï��bed
			tssDistance.setInvNum(binNum);	
			tssDistance.getPeakInfo(txtPeakFile, colPeakChr, rowStart, rowEnd);	
			tssDistance.getRegionDensity("GeneEnd",range, range*2/binNum,resultpath,resultPrefix);
			tssDistance.getRegionDensity("Tss",range,range*2/binNum,resultpath,resultPrefix);
	}
	/**
	 * ��ȡ����
	 * @param txtFilepeakFile
	 * @param colPeak
	 * @param resultPath
	 * @param prix
	 */
	public static void getseq(String txtFilepeakFile, int[] colPeak,String resultPath ,String prix) {
		
		try {
			//����ı�
			
			int condition=0; //condition 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻��������� 1: ͨͨ��ȡ���� 2: ͨͨ��ȡ����

			int peaklength=80; //peak�������˳���
			String txtresultfilename=resultPath +"/"+ prix + peaklength +".txt";
			GetSeq.getPeakSeq(peaklength, condition, txtFilepeakFile, sep, colPeak, 2, -1, txtresultfilename);
		} catch (Exception e) {			e.printStackTrace();	}
	}
	
	
	
	/**
	 * �ں�������������ͳ��
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
	 * ����R��ͼ
	 * @throws Exception
	 */
	private static void barPlot() throws Exception
	{
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+ NovelBioConst.R_WORKSPACE_CHIP_GENESTRUCTURE_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		System.out.println("ok");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
