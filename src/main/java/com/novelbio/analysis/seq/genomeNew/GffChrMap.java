package com.novelbio.analysis.seq.genomeNew;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.addRsvRequest;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.HeatMap;
import com.novelbio.base.plot.java.HeatChart;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.test.testextend.a;

/**
 * 给定基因的区域，画出各种统计图
 * @author zong0jie
 *
 */
public class GffChrMap extends GffChrAbs{
	
	String fileName = "";
	/**
	 * 
	 */
	boolean HanYanFstrand =false;
	/**
	 * @param gffType
	 * @param gffFile
	 * @param readsBed
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 * @param HanYanFstrand 是否选择韩燕模式，根据reads是否与基因的方向相一致而进行过滤工作，这个是专门针对韩燕的项目做的分析。
	 */
	public GffChrMap(String gffType, String gffFile, String chrFile,String readsBed, int binNum, boolean HanYanFstrand) {
		super(gffType, gffFile, chrFile, readsBed, binNum);
		this.HanYanFstrand = HanYanFstrand;
	}
	/**
	 * 按照染色体数，统计每个染色体上总位点数，每个位点数，
	 * string[4]
	 * 0: chrID
	 * 1: readsNum
	 * 2: readsPipNum
	 * 3: readsPipMean
	 * @return
	 */
	public ArrayList<String[]> getChrLenInfo()
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String> lsChrID = mapReads.getChrIDLs();
		for (String string : lsChrID) {
			String[] chrInfoTmp = new String[4];
			chrInfoTmp[0] = string;
			chrInfoTmp[1] = mapReads.getChrReadsNum(string) + "";
			chrInfoTmp[2] = mapReads.getChrReadsPipNum(string) + "";
			chrInfoTmp[3] = mapReads.getChrReadsPipMean(string) + "";
			lsResult.add(chrInfoTmp);
		}
		return lsResult;
	}
	
	
	/**
	 * @param gffType
	 * @param gffFile
	 * @param readsBed
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 * @param HanYanFstrand 是否选择韩燕模式，根据reads是否与基因的方向相一致而进行过滤工作，这个是专门针对韩燕的项目做的分析。
	 */
	public GffChrMap(String gffType, String gffFile, String chrFile,String readsBed, int binNum) {
		super(gffType, gffFile, chrFile, readsBed, binNum);
		this.HanYanFstrand = false;
	}
	
	public static void main(String[] args) {
		GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE,
				NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM, 
				"/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/N/result/Nextend_sort.bed", 10);
		gffChrMap.loadChrFile();
		gffChrMap.loadMapReads();
		gffChrMap.setPlotRegion(4000, 4000);
		gffChrMap.setMapNormType(MapReads.NORMALIZATION_NO);
		gffChrMap.plotTssTesHeatMap(Color.blue,false,"/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP_mRNA/sigle/dgeexpress2.xls",
				1, 2, 2, 0,100,1,GffDetailGene.TSS, 1000, "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/Result/TssTesHeatMap/check2.png");
		
	}
	/**
	 * @param readsFile mapping的结果文件，必须排过序，一般为bed格式
	 * @param binNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			if (HanYanFstrand) {
				mapReads = new MapReadsHanyanChrom(binNum, readsFile);
				mapReads.setChrLenFile(getRefLenFile());
				mapReads.setNormalType(mapNormType);
			}
			else {
				mapReads = new MapReads(binNum, readsFile);
				mapReads.setChrLenFile(getRefLenFile());
				mapReads.setNormalType(mapNormType);
			}
		}
	}
	
	/**
	 * 读取bed文件
	 */
	public void readMapBed() {
		try {
			mapReads.ReadMapFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique Unique的reads在哪一列 novelbio的标记在第七列，从1开始计算
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param cis5to3 是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique, boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, colUnique, booUniqueMapping, cis5to3);
	}
	
	/**
	 * 返回某条染色体上的reads情况，不是密度图，只是简单的计算reads在一个染色体上的情况
	 * 主要用于RefSeq时，一个基因上的reads情况
	 * @param chrID
	 * @param thisInvNum 每个区间几bp
	 * @parm type 取样方法 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 没有的话就返回null
	 */
	public double[] getChrInfo(String chrID, int thisInvNum, int type)
	{
		double[] tmpResult = mapReads.getRengeInfo(thisInvNum, chrID, 0, 0, type);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}
	

	
	int maxresolution =10000;
	
	/**
	 * 画出所有染色体上密度图
	 * @param gffChrMap2 是否有第二条染色体，没有的话就是null
	 * @throws Exception
	 */
	public void getAllChrDist(GffChrMap gffChrMap2) 
	{
		ArrayList<String[]> chrlengthInfo=seqHash.getChrLengthInfo();
		for (int i = chrlengthInfo.size()-1; i>=0; i--) {
			try {
				getChrDist(chrlengthInfo.get(i)[0], maxresolution, gffChrMap2);
			} catch (Exception e) { 	e.printStackTrace();			}
		}
	}
	
	/**
	 * 给定染色体，返回该染色体上reads分布
	 * @param chrID 第几个软色体
	 * @param maxresolution 最长分辨率
	 * @param gffChrMap2 如果需要画第二条染色体的图，也就是对称了画
	 * @param 输出文件名，带后缀"_chrID"
	 * @throws Exception
	 */
	private void getChrDist(String chrID,int maxresolution, GffChrMap gffChrMap2) throws Exception
	{
		int[] resolution=seqHash.getChrRes(chrID, maxresolution);
		double[] chrReads=getChrDensity(chrID.toLowerCase(),resolution.length);
		long chrLength =seqHash.getChrLength(chrID);
		if (chrReads!=null)
		{
			TxtReadandWrite txtRparamater=new TxtReadandWrite();
			////////// 参 数 设 置 /////////////////////
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM,true, false);
			txtRparamater.writefile("Item"+"\t"+"Info"+"\r\n");//必须要加上的，否则R读取会有问题
			txtRparamater.writefile("tihsresolution"+"\t"+chrLength+"\r\n");
			txtRparamater.writefile("maxresolution"+"\t"+seqHash.getChrLenMax()+"\r\n");
			txtRparamater.writefile("ChrID"+"\t"+chrID+"\r\n");
			
			////////// 数 据 输 入 ///////////////////////
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X, true,false);
			txtRparamater.Rwritefile(resolution);
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y, true,false);
			txtRparamater.Rwritefile(chrReads);
			
			///////////如果第二条染色体上有东西，那么也写入文本/////////////////////////////////////////
			if (gffChrMap2!=null) 
			{
				double[] chrReads2=gffChrMap2.getChrDensity(chrID.toLowerCase(), resolution.length);
				txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_2Y, true,false);
				txtRparamater.Rwritefile(chrReads2);
			}
			hist();
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X,chrID+"readsx");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y,chrID+"readsy");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_2Y,chrID+"reads2y");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM,chrID+"parameter");
		}
	}
	
	/**
	 * 返回某条染色体上的reads情况，是密度图
	 * 主要用于基因组上，一条染色体上的reads情况
	 * @param chrID
	 * @param binNum 分成几个区间
	 * @parm type 取样方法 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 没有的话就返回null
	 */
	private double[] getChrDensity(String chrID, int binNum)
	{
		double[] tmpResult = mapReads.getReadsDensity(chrID, 0, 0, binNum);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}
	
	/**
	 * 调用R画图
	 * @throws Exception
	 */
	private void hist() throws Exception
	{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+ NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
	/**
	 * 
	 * @param color
	 * @param SortS2M 是否从小到大排序
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param heapMapSmall
	 * @param heapMapBig
	 * @param scale
	 * @param structure 基因结构，目前只有 GffDetailGene.TSS 和 GffDetailGene.TES
	 * @param binNum 最后分成几块
	 * @param outFile
	 */
	public void plotTssTesHeatMap(Color color,boolean SortS2M, String txtExcel, int colGeneID, int colScore, int rowStart, 
			double heapMapSmall, double heapMapBig, double scale, String structure, int binNum, String outFile) {
		ArrayList<MapInfo> lsMapInfos = super.readFileGeneMapInfo(txtExcel, colGeneID, colScore, rowStart, structure, binNum);
		MapInfo.sortPath(SortS2M);
		Collections.sort(lsMapInfos);
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 100, HeatChart.SCALE_LINEAR, FileOperate.changeFileSuffix(outFile, "_100line", null));
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 100, HeatChart.SCALE_EXPONENTIAL, FileOperate.changeFileSuffix(outFile, "_100exp", null));
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 100, HeatChart.SCALE_LOGARITHMIC, FileOperate.changeFileSuffix(outFile, "_100log", null));
		
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 70, HeatChart.SCALE_LINEAR, FileOperate.changeFileSuffix(outFile, "_70line", null));
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 70, HeatChart.SCALE_EXPONENTIAL, FileOperate.changeFileSuffix(outFile, "_70exp", null));
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 70, HeatChart.SCALE_LOGARITHMIC, FileOperate.changeFileSuffix(outFile, "_70log", null));
		
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 200, HeatChart.SCALE_LINEAR, FileOperate.changeFileSuffix(outFile, "_200line", null));
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 200, HeatChart.SCALE_EXPONENTIAL, FileOperate.changeFileSuffix(outFile, "_200exp", null));
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 200, HeatChart.SCALE_LOGARITHMIC, FileOperate.changeFileSuffix(outFile, "_200log", null));
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 100, 0.5, FileOperate.changeFileSuffix(outFile, "_100log", null));
//		plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 450, 1.5, FileOperate.changeFileSuffix(outFile, null, null));
		plotHeatMap(lsMapInfos, structure, color, heapMapSmall, heapMapBig, scale, FileOperate.changeFileSuffix(outFile, null, null));
	}
	
	/**
	 * 
	 * 获得summit位点，画summit位点附近的reads图
	 * @param SortS2M 是否从小到大排序
	 * @param txtExcel
	 * @param colChrID
	 * @param colSummit
	 * @param colRegion
	 * @param colScore
	 * @param rowStart
	 * @param heapMapSmall
	 * @param heapMapBig
	 * @param scale
	 * @param binNum
	 * @param outFile
	 */
	public void plotSummitHeatMap(boolean SortS2M, String txtExcel, int colChrID, int colSummit, int colRegion, int colScore, int rowStart, 
			double heapMapSmall, double heapMapBig, double scale, int binNum, String outFile) {
		ArrayList<MapInfo> lsMapInfos = super.readFileSiteMapInfo(txtExcel, colRegion, colChrID, colSummit, colScore, rowStart);
		MapInfo.sortPath(SortS2M);
		Collections.sort(lsMapInfos);
		mapReads.getRegionLs(binNum, lsMapInfos, 0);
		plotHeatMap(lsMapInfos, "", Color.BLUE, heapMapSmall, heapMapBig, scale, FileOperate.changeFileSuffix(outFile, null, null));
	}	
	/**
	 * @param lsMapInfo 基因信息
	 * @param structure 基因结构，目前只有 GffDetailGene.TSS 和 GffDetailGene.TES
	 * @param color
	 * @param small 最小
	 * @param big 最大
	 * @param scale scale次方，大于1则稀疏高表达，小于1则稀疏低表达
	 * @param outFile
	 */
	private static void plotHeatMap(ArrayList<MapInfo> lsMapInfo, String structure, Color color, double small, double big, double scale ,String outFile)
	{
		HeatChart map = new HeatChart(lsMapInfo, small, big);
		if (structure.equals(GffDetailGene.TSS)) {
			map.setTitle("HeatMap Of TSS");
			map.setXAxisLabel("Distance To TSS");
			map.setYAxisLabel("");
		}
		else if (structure.equals(GffDetailGene.TES)) {
			map.setTitle("HeatMap Of TES");
			map.setXAxisLabel("Distance To TES");
			map.setYAxisLabel("");
		}
		else {
			map.setTitle("HeatMap Of Summit");
			map.setXAxisLabel("Distance To Summit");
			map.setYAxisLabel("");
		}
		
		String[] aa = new String[]{"a","b","c","d","e","f"};
		map.setXValues(aa);
		String[] nn = new String[lsMapInfo.get(0).getDouble().length];
		for (int i = 0; i < nn.length; i++) {
			nn[i] = "";
		}
		map.setYValues(nn);
		Dimension bb = new Dimension();
		bb.setSize(1, 0.01);
		map.setCellSize(bb );
		//Output the chart to a file.
		Color colorblue = color;
		Color colorRed = Color.WHITE;
		//map.setBackgroundColour(color);
		map.setHighValueColour(colorblue);
		map.setLowValueColour(colorRed);
		map.setColourScale(scale);
		try {
			map.saveToFile(new File(outFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	/**
	 * @param lsMapInfo 基因信息
	 * @param structure 基因结构，目前只有 GffDetailGene.TSS 和 GffDetailGene.TES
	 * @param color
	 * @param small 最小
	 * @param big 最大
	 * @param scale scale次方，大于1则稀疏高表达，小于1则稀疏低表达
	 * @param outFile
	 */
	public static void plotHeatMap2(ArrayList<MapInfo> lsMapInfo,ArrayList<MapInfo> lsMapInfo2 ,
			String outFile,double mindata1, double maxdata1, double mindata2, double maxdata2)
	{
		Color colorred = new Color(255, 0, 0, 255);
		Color colorwhite = new Color(0, 0, 0, 0);
		Color colorgreen = new Color(0, 255, 0, 255);
		
		Color[] gradientColors = new Color[] { colorwhite, colorred };
		Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);
		
		Color[] gradientColors2 = new Color[] { colorwhite, colorgreen };
		Color[] customGradient2 = Gradient.createMultiGradient(gradientColors2, 250);
		HeatMap heatMap = new HeatMap(lsMapInfo, lsMapInfo2, false, customGradient, customGradient2);
		heatMap.setRange(mindata1, maxdata1, mindata2, maxdata2);
		try {
			heatMap.saveToFile(outFile, 4000, 1000, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param lsMapInfo 基因信息
	 * @param structure 基因结构，目前只有 GffDetailGene.TSS 和 GffDetailGene.TES
	 * @param color
	 * @param small 最小
	 * @param big 最大
	 * @param scale scale次方，大于1则稀疏高表达，小于1则稀疏低表达
	 * @param outFile
	 */
	public static void plotHeatMapMinus(ArrayList<MapInfo> lsMapInfo1,ArrayList<MapInfo> lsMapInfo2 ,
			String outFile,double mindata1, double maxdata1, double mindata2, double maxdata2)
	{
		ArrayList<MapInfo> lsMapInfoFinal = MapInfo.minusListMapInfo(lsMapInfo1, lsMapInfo2);
		Color colorgreen = new Color(0, 255, 0, 255);
		Color colorwhite = new Color(0, 0, 0, 0);
		Color colorred = new Color(255, 0, 0, 255);
		
		
		
		Color[] gradientColors = new Color[] { colorgreen, colorwhite, colorred };
		Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);

		HeatMap heatMap = new HeatMap(lsMapInfoFinal, false, customGradient);
		heatMap.setRange(mindata1, maxdata1);
		try {
			heatMap.saveToFile(outFile, 6000, 1000, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * 根据前面设定upBp和downBp
	 * 根据Peak文件做出TSS图
	 * @param range Tss两端区域
	 * @param binNum 分割分数
	 * @param figure 图片路径
	 * @param RworkSpace 
	 * @param resultFilePath 保存至哪个文件夹
	 * @param prefix 文件名前缀
	 */
	public void getTssDensity(String fileName, int colChrID, int colStartLoc, int colEndLoc, int colScore, int rowStart,int binNum, String resultFilePath, String prefix) {
		ArrayList<MapInfo> lsMapInfo = super.readFileRegionMapInfo(fileName, colChrID, colStartLoc, colEndLoc, colScore, rowStart);
		ArrayList<MapInfo> lsMapTssInfo = super.getPeakCoveredGeneMapInfo(lsMapInfo, GffDetailGene.TSS, binNum);//(binNum,lsMapInfo, GffDetailGene.TSS);
		
		double[] TssDensity = MapInfo.getCombLsMapInfo(lsMapTssInfo);
//		double[] TssDensity=gffLocatCod.getUCSCTssRange(LocInfo, range, binNum);
		plotRTss(TssDensity);
	}
	
	/**
	 * 根据前面设定upBp和downBp
	 * 根据Peak文件做出TSS图
	 * @param range Tss两端区域
	 * @param binNum 分割分数
	 * @param figure 图片路径
	 * @param RworkSpace 
	 * @param resultFilePath 保存至哪个文件夹
	 * @param prefix 文件名前缀
	 */
	public void getTesDensity(String fileName, int colChrID, int colStartLoc, int colEndLoc, int colScore, int rowStart,int binNum, String resultFilePath, String prefix) {
		ArrayList<MapInfo> lsMapInfo = super.readFileRegionMapInfo(fileName, colChrID, colStartLoc, colEndLoc, colScore, rowStart);
		ArrayList<MapInfo> lsMapTssInfo = super.getPeakCoveredGeneMapInfo(lsMapInfo, GffDetailGene.TES, binNum);//(binNum,lsMapInfo, GffDetailGene.TSS);
		
		double[] TssDensity = MapInfo.getCombLsMapInfo(lsMapTssInfo);
//		double[] TssDensity=gffLocatCod.getUCSCTssRange(LocInfo, range, binNum);
		plotRTss(TssDensity);
	}
	private void plotRTss(double[] TssDensity)
	{
		TxtReadandWrite tssReadandWrite=new TxtReadandWrite();
		tssReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_R, true,false);
		try { tssReadandWrite.Rwritefile(TssDensity); 	} catch (Exception e) { 	e.printStackTrace(); }
		tssReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_PARAM, true,false);
		try{
			tssReadandWrite.writefile(super.upBp+""); 
		} catch (Exception e) { 	e.printStackTrace(); }
		try {density("Tss");	} catch (Exception e) {	e.printStackTrace();}
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_EXCEL, resultFilePath,prefix+"tss.txt",true);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_PIC, resultFilePath,prefix+"TSSReads.jpg",true);
		
		TxtReadandWrite txtTmpGenNum = new TxtReadandWrite();
		//写入该区域进行统计的基因数目
		 if (!resultFilePath.endsWith(File.separator)) {  
			 resultFilePath = resultFilePath + File.separator;  
	         }
		 txtTmpGenNum.setParameter(resultFilePath+prefix+"tssGenNum.txt", true, false);
		 try {
			txtTmpGenNum.writefile(gffLocatCod.getRegGenNum()+"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 专为韩燕设计<br>
	 * 当为refseq时，获得的某个基因的分布情况，按照3个barcode划分
	 * @return
	 * 没有该基因则返回null
	 */
	public double[] getGeneReadsHYRefseq(String geneID) {
		double[] tmpResult = getChrInfo(geneID, 1, 0);
		if (tmpResult == null) {
			return null;
		}
		//获得具体转录本的信息
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return combineLoc(tmpResult, gffGeneIsoInfoOut.getLenUTR5()+1);
	}
	/**
	 * 给定atg位点，获得该atg位点在合并后的序列中应该是第几个，从1开始
	 * @param atgSite
	 * @return
	 */
	public int getCombAtgSite(String geneID)
	{
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		int atgSite = gffGeneIsoInfoOut.getLenUTR5()+1;
		//除以3是指3个碱基
		return (int)Math.ceil((double)(atgSite -  1)/3);
	}
	
	/**
	 * 给定atg位点，获得该atg位点在合并后的序列中应该是第几个，从1开始
	 * @param atgSite
	 * @return
	 */
	public int getAtgSite(String geneID)
	{
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return gffGeneIsoInfoOut.getLenUTR5()+1;
		//除以3是指3个碱基
	}
	/**
	 * 专为韩燕设计
	 * 将三个碱基合并为1个coding，取3个的最后一个碱基对应的reads数
	 * @param geneReads 该基因的reads信息，必须是单碱基精度
	 * @param AtgSite 该基因的atg位点，从1开始计算
	 * @return
	 * 返回经过合并的结果，譬如
	 * {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
	 * atg位点为6
	 * 结果{ 2,5,8,11,14,17};
	 */
	private double[] combineLoc(double[] geneReads, int AtgSite)
	{
		//此时的SeqInfo第一位就是实际的第一位，不是atgsite了		
		return MathComput.mySplineHY(geneReads, 3, AtgSite, 3);
	}

	/**
	 * 仅给<b>韩燕</b>使用<br>
	 * 获得基因的信息，然后排序，可以从里面挑选出含reads最多的几个然后画图
	 * 返回经过排序的mapinfo的list，每一个mapInfo包含了该基因的核糖体信息
	 */
	public ArrayList<MapInfo> getChrInfo() {
		ArrayList<String> lsChrID = mapReads.getChrIDLs();
		ArrayList<MapInfo> lsMapInfo = new ArrayList<MapInfo>();
		for (String string : lsChrID) {
			mapReads.setNormalType(MapReads.NORMALIZATION_NO);
			GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(string);
			if (!gffGeneIsoInfo.getGeneType().equals( GffGeneIsoInfo.TYPE_GENE_MRNA)
					&& !gffGeneIsoInfo.getGeneType().equals( GffGeneIsoInfo.TYPE_GENE_MRNA_TE)
			) {
				continue;
			}
			
			double[] tmp = mapReads.getRengeInfo(mapReads.getBinNum(), string, 0, 0,0);
			mapReads.setNormalType(super.mapNormType);
			double[] tmp2 = mapReads.getRengeInfo(mapReads.getBinNum(), string, 0, 0,0);
			///////////////////  异 常 处 理 /////////////////////////////////////////////////////////////////////
			if (tmp == null && tmp2 == null) {
				continue;
			}
			else if (tmp == null) {
				tmp = new double[tmp2.length];
			}
			else if (tmp2 == null) {
				tmp2 = new double[tmp.length];
			}
			////////////////////////////////////////////////////////////////////////////////////////
			int combatgSite = getCombAtgSite(string);
			tmp2 = combineLoc(tmp2, getAtgSite(string));

			
			
			double weight = MathComput.sum(tmp);
			MapInfo mapInfo = new MapInfo(string);
			mapInfo.setWeight(weight);
			mapInfo.setDouble(tmp2);
			mapInfo.setFlagLoc(combatgSite);
			CopedID copedID = new CopedID(string, 0, false);
			mapInfo.setTitle(copedID.getSymbol());
			lsMapInfo.add(mapInfo);
		}
		Collections.sort(lsMapInfo);
		return lsMapInfo;
	}
	
	/**
	 * 经过标准化
	 * 将MapInfo中的double填充上相应的reads信息
	 * @param binNum 待分割的区域数目
	 * @param lsmapInfo
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegionLs(int binNum, ArrayList<MapInfo> lsmapInfo, int type)
	{
		mapReads.getRegionLs(binNum, lsmapInfo, type);
	}
	
	
	
	
	
	
	
	
	
}
