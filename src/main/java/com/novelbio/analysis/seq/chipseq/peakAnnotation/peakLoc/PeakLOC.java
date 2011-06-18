package com.novelbio.analysis.seq.chipseq.peakAnnotation.peakLoc;


import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.chipseq.prepare.GenomeBasePrepare;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.genome.GffLocatCod;
import com.novelbio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelbio.base.plot.Rplot;


 
/**
 * 将peak定位到基因上，并确定是外显子还内含子
 * @author zong0jie
 *
 */
public class PeakLOC extends GenomeBasePrepare{
	
	/**
	 * 
	 * peak定位，并写入文本文件，只获得2k上游以及50bp内的位点
	 * @param gfffilename gff文件
	 * @param txtFilepeakFile
	 * @param sep 分隔符
	 * @param columnID 读取peak文件的哪几列，需要指定ChrID和summit位点
	 * @param rowStart 从第几行读起
	 * @param rowEnd 读到第几行为止
	 * @param txtresultfilename 写入文件的路径与文件名
	 * @throws Exception
	 * @param txtFilepeakFile  peak文件,注意第一行是title，必须要有的
	 * @param sep 分隔符
	 * @param columnID 读取peak文件的哪几列，需要指定ChrID和summit位点
	 * @param rowStartt 从第几行读起
	 * @param rowEnd 读到第几行为止
	 * @param excelFile 写入文件的路径与文件名
	 * @param Region 定位区域int 3 0:UpstreamTSSbp 1:DownStreamTssbp 2:GeneEnd3UTR
	 * null 为默认，up3k down
	 * @throws Exception
	 */
	public static void locatDetail(String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd,String excelFile,int[] Region) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		if (Region != null) {
			gffLocatCod.setUpstreamTSSbp(Region[0]);
			gffLocatCod.setDownStreamTssbp(Region[1]);
			gffLocatCod.setGeneEnd3UTR(Region[2]);
		}
		
		ArrayList<String[]>LOCDetail=gffLocatCod.peakAnnotationEN(LOCIDInfo);		
		/**
		 * 给定二维数组,计算出每个peakLOC所在的基因，针对UCSCknown gene以及refseq 输入的数据，
			第一维是ChrID
			第二维是坐标
			输出ArrayList-String[8]
		0: ChrID
		1: 坐标
		2: 在基因内，本基因名
		3: 在基因内的具体信息
		4: 在基因间并且距离上个基因很近，上个基因名
		5: 上个基因方向，到上个基因起点/终点的距离
		6: 在基因间并且距离下个基因很近，下个基因名
		7: 下个基因方向，到下个基因起点/终点的距离 
		 */
		
		TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
		txtReadandWrite.setParameter(txtFilepeakFile, false, true);
		String[][] all = txtReadandWrite.ExcelRead("\t", 1, 1, txtReadandWrite.ExcelRows(), txtReadandWrite.ExcelColumns(2, "\t"));
		
		
		String[] content=new String[8];
		content[0]="";content[1]="";
		content[2]="insideGene_GeneName";
		content[3]="DetailInfo";content[4]="UpStream_GeneName";
		content[5]="Distance to UpStream Gene";content[6]="DownStream_GeneName";
		content[7]="Distance to DownSteam Gene";
		LOCDetail.add(0, content);
		String[][] locDetailStr=new String[LOCDetail.size()][LOCDetail.get(1).length-2];
		for (int i = 0; i < locDetailStr.length; i++) {
			for (int j = 2; j <LOCDetail.get(1).length; j++) {
				locDetailStr[i][j-2]=LOCDetail.get(i)[j];
			}
		}
		String[][] result=ArrayOperate.combStrArray(all, locDetailStr, -1);
		txtReadandWrite.setParameter(excelFile, true, false);
		txtReadandWrite.ExcelWrite(result, "\t");
	}
	
	
	/**
	 * 
	 * 筛选出合适的peak
	 * 
	 * @param txtFile peak文件,注意第一行是title，必须要有的
	 * @param sep 分隔符
	 * @param colChrID chrID在第几列，实际列
	 * @param colSummit summit位点在第几列，实际列
	 * @param rowStart 从第几行读起，注意本列要包含title
	 * @param filterTss 是否进行tss筛选，null不进行，如果进行，那么必须是int[2],0：tss上游多少bp  1：tss下游多少bp，都为正数 <b>只有当filterGeneBody为false时，tss下游才会发会作用</b>
	 * @param filterGenEnd 是否进行geneEnd筛选，null不进行，如果进行，那么必须是int[2],0：geneEnd上游多少bp  1：geneEnd下游多少bp，都为正数<b>只有当filterGeneBody为false时，geneEnd上游才会发会作用</b>
	 * @param filterGeneBody 是否处于geneBody，true，将处于geneBody的基因全部筛选出来，false，不进行geneBody的筛选<br>
	 * <b>以下条件只有当filterGeneBody为false时才能发挥作用</b>
	 * @param filter5UTR 是否处于5UTR中
	 * @param filter3UTR 是否处于3UTR中
	 * @param filterExon 是否处于外显子中
	 * @param filterIntron 是否处于内含子中
	 * @param excelResultFile 保存文件
	 * 0-n:输入的loc信息<br>
	 * n+1: 基因名<br>
	 * n+2: 基因信息<br>
	 */
	public static void filterPeak(String txtFile,String sep,int colChrID,int colSummit,int rowStart,
			int[] filterTss, int[] filterGenEnd, boolean filterGeneBody, boolean filter5UTR, boolean filter3UTR, boolean filterExon,boolean filterIntron,
			String excelResultFile) throws Exception 
	{
		TxtReadandWrite txtPeakFile = new TxtReadandWrite(); txtPeakFile.setParameter(txtFile, false, true);
		ArrayList<String[]> LOCIDInfo = txtPeakFile.ExcelRead(sep, rowStart, 1, txtPeakFile.ExcelRows(), txtPeakFile.ExcelColumns(rowStart, sep), -1);
		String[] title = LOCIDInfo.get(0);
		String[] titleNew = new String[title.length + 2];
		for (int i = 0; i < title.length; i++) {
			titleNew[i] = title[i];
		}
		titleNew[title.length] = "AccID";
		titleNew[title.length+1] = "Location";
		List<String[]> lsQuery = LOCIDInfo.subList(1, LOCIDInfo.size()-1);
		ArrayList<String[]> LOCDetail=gffLocatCod.peakAnnoFilter(lsQuery, colChrID, colSummit, filterTss, filterGenEnd, filterGeneBody, filter5UTR, filter3UTR, filterExon, filterIntron);
		LOCDetail.add(0, titleNew);
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(excelResultFile);
		excelResult.WriteExcel(true, 1, 1, LOCDetail);
	}
	
	
	
	
	/**
	 * too detail so not useful, using locatDetail instead<br>
	 * peak定位，并写入文本文件，得到的数据可以用来画直方图
	 * @param gfffilename gff文件
	 * @param txtFilepeakFile peak文件
	 * @param sep 分隔符
	 * @param columnID 读取peak文件的哪几列，需要指定ChrID和summit位点
	 * @param rowStart 从第几行读起
	 * @param rowEnd 读到第几行为止
	 * @param txtresultfilename 写入文件的路径与文件名
	 * @throws Exception
	 */
	@Deprecated
	public static void locatstatistic(String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd,String txtresultfilename) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		
		ArrayList<String[]> LOCDetail=gffLocatCod.peakAnnotationDetail(LOCIDInfo);
		
		/**
		 * 给定二维数组,计算出每个peakLOC所在的基因，针对UCSCknown gene以及refseq 输入的数据，
			第一维是ChrID
			第二维是坐标
			输出ArrayList-String[10]
			1: 坐标
			2: 基因内还是基因间
			3: 在基因内: 内含子还是外显子
			4: 内含子与内含子起点距离比例
			5: 内含子与内含子终点距离比例
			6: 外显子与外显子起点距离比例
			7: 外显子与外显子终点距离比例
			8: 5UTR与TSS距离比例，不准，这里所有都需要和陈德桂讨论
			9: 5UTR与ATG距离比例
			10: 3UTR与编码区距离比例
			11: 3UTR与基因结尾距离比例
			12: peak与TSS距离
			13: peak与ATG距离
			14: 如果peak在基因间，peak与基因终点的距离
		 */
		
		TxtReadandWrite result=new TxtReadandWrite();
		result.setParameter(txtresultfilename, true,false);
		String content="chrID"+"\t"+"坐标"+"\t"+"基因内还是基因间"+"\t"+"内含子还是外显子"+"\t"+"内含子与内含子起点距离比例"+"\t"+"内含子与内含子终点距离比例"+"\t"+
		"外显子与外显子起点距离比例"+"\t"+"外显子与外显子终点距离比例"+"\t"+"是否5UTR"+"\t"+"5UTR与TSS距离"+"\t"+
		"5UTR与ATG距离比例"+"\t"+"是否3UTR"+"\t"+"3UTR与编码区距离比例"+"\t"+"3UTR与基因结尾距离比例"+"\t"+"peak与TSS距离"+"\t"+"peak与ATG距离"+"\t"
		+"如果peak在基因间，peak与基因终点的距离"+"\r\n";
		result.writefile(content);
		result.ExcelWrite(LOCDetail, sep, 1, 1);
	}
	
	/**
	 * 画Tss和GeneEnd的Peak级别图
	 * @param gfffilename gff文件
	 * @param txtFilepeakFile peak文件
	 * @param sep 分隔符
	 * @param columnID 读取peak文件的哪几列，需要指定ChrID和summit位点
	 * @param rowStart 从第几行读起
	 * @param rowEnd 读到第几行为止 如果rowEnd=-1，则一直读到文件结尾
	 * @param txtresultfilename 写入文件的路径与文件名
	 * @throws Exception
	 */
	public static void histTssGeneEnd (String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd,String resultPath, String resultPrix) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		
		ArrayList<String[]> LOCDetail=gffLocatCod.peakAnnotationDetail(LOCIDInfo);
		ArrayList<Double> lsTss = new ArrayList<Double>();ArrayList<Double> lsGeneEnd= new ArrayList<Double>();
		for (String[] strings : LOCDetail) {
			try {
				lsTss.add(Double.parseDouble(strings[14]));
			} catch (Exception e) {}
			try {
				lsGeneEnd.add(Double.parseDouble(strings[16]));
			} catch (Exception e) {}
		}
		double[] tss = new double[lsTss.size()];	double[] geneEnd = new double[lsGeneEnd.size()];
		for (int i = 0; i < lsTss.size(); i++) {
			tss[i] = lsTss.get(i);
		}
		for (int i = 0; i < lsGeneEnd.size(); i++) {
			geneEnd[i] = lsGeneEnd.get(i);
		}
		Rplot.plotHist(tss, -10000, 10000, "Peak Near Tss", "Tss Region", "Peak Density", resultPath, resultPrix+"_Tss");
		Rplot.plotHist(geneEnd, -10000, 10000, "Peak Near GeneEnd", "GeneEnd Region", "Peak Density", resultPath, resultPrix+"_GeneEnd");
	}
	
	
	
	
	/**
	 * 获得Intron/Exon等统计信息，用来画柱状图的
	 * 直接将返回的结果写入文本文件
	 * @return
	 * @throws Exception 
	 */
	public static String[][] getPeakStaticInfo(String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		String[][]  gffPeakstaticInfo=gffLocatCod.peakStatistic(LOCIDInfo);
		ArrayList<Long>  gffstaticInfo=gffLocatCod.getGeneStructureLength();
		String[] item=new String[gffPeakstaticInfo.length];
		long[] peakInfo=new long[gffPeakstaticInfo.length];
		long[] background=new long[gffPeakstaticInfo.length];
		long chrLength=gffLocatCod.getChrLength("");
		for (int i = 0; i < gffPeakstaticInfo.length; i++) {
			item[i]=gffPeakstaticInfo[i][0];
			peakInfo[i]=Integer.parseInt(gffPeakstaticInfo[i][1]);
			background[i]=gffstaticInfo.get(i);
		}
		background[5]=chrLength-background[0]-background[1]-background[2]-background[3]-background[4];
		return MathComput.batStatistic(peakInfo, background, item, "PeakInfo", "GenomeBackGround");
	}
	
	
	
	
	
	
	/**
	 * 获得全基因组Intron/Exon等信息
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String[]> getStaticInfo() throws Exception 
	{
		ArrayList<Long>  gffstaticInfo=gffLocatCod.getGeneStructureLength();
		ArrayList<String[]> lsresult=new ArrayList<String[]>();
			String[] tmpresult0=new String[2];
			tmpresult0[0]="allGeneLength"; tmpresult0[1]=gffstaticInfo.get(0)+"";
			lsresult.add(tmpresult0);
			String[] tmpresult1=new String[2];
			tmpresult1[0]="allIntronLength"; tmpresult1[1]=gffstaticInfo.get(1)+"";
			lsresult.add(tmpresult1);
			String[] tmpresult2=new String[2];
			tmpresult2[0]="allExonLength"; tmpresult2[1]=gffstaticInfo.get(2)+"";
			lsresult.add(tmpresult2);
			String[] tmpresult3=new String[2];
			tmpresult3[0]="all5UTRLength"; tmpresult3[1]=gffstaticInfo.get(3)+"";
			lsresult.add(tmpresult3);
			String[] tmpresult4=new String[2];
			tmpresult4[0]="all3UTRLength"; tmpresult4[1]=gffstaticInfo.get(4)+"";
			lsresult.add(tmpresult4);
			String[] tmpresult5=new String[2];
			tmpresult5[0]="allup2kLength"; tmpresult5[1]=gffstaticInfo.get(5)+"";
			lsresult.add(tmpresult5);
			return lsresult;
	}

}
