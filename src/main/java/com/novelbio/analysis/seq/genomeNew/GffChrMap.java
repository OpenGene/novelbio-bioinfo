package com.novelbio.analysis.seq.genomeNew;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TestUtils;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;

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
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
import com.novelbio.base.plot.java.HeatChart;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.test.testextend.a;

/**
 * 给定基因的区域，画出各种统计图
 * 
 * @author zong0jie
 * 
 */
public class GffChrMap extends GffChrAbs {

	String fileName = "";
	/**
	 * 
	 */
	boolean HanYanFstrand = false;
	
	/**
	 * @param gffType
	 * @param gffFile
	 * @param readsBed
	 * @param binNum
	 *            每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 * @param HanYanFstrand
	 *            是否选择韩燕模式，根据reads是否与基因的方向相一致而进行过滤工作，这个是专门针对韩燕的项目做的分析。
	 */
	public GffChrMap(String gffType, String gffFile, String chrFile,
			String readsBed, int binNum, boolean HanYanFstrand) {
		super(gffType, gffFile, chrFile, readsBed, binNum);
		this.HanYanFstrand = HanYanFstrand;
	}
	
	/**
	 * 每隔多少位取样,如果设定为1，则算法会变化，然后会很精确
	 * @return
	 */
	public int getThisInv() {
		return mapReads.getBinNum();
	}
	/**
	 * 按照染色体数，统计每个染色体上总位点数，每个位点数， string[4] 0: chrID 1: readsNum 2: readsPipNum
	 * 3: readsPipMean
	 * 
	 * @return
	 */
	public ArrayList<String[]> getChrLenInfo() {
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
	 * @param binNum
	 *            每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 * @param HanYanFstrand
	 *            是否选择韩燕模式，根据reads是否与基因的方向相一致而进行过滤工作，这个是专门针对韩燕的项目做的分析。
	 */
	public GffChrMap(String gffType, String gffFile, String chrFile,
			String readsBed, int binNum) {
		super(gffType, gffFile, chrFile, readsBed, binNum);
		this.HanYanFstrand = false;
	}

 

	/**
	 * @param readsFile
	 *            mapping的结果文件，必须排过序，一般为bed格式
	 * @param binNum
	 *            每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			if (HanYanFstrand) {
				mapReads = new MapReadsHanyanChrom(binNum, readsFile);
				mapReads.setChrLenFile(getRefLenFile());
				mapReads.setNormalType(mapNormType);
			} else {
				mapReads = new MapReads(binNum, readsFile);
				mapReads.setChrLenFile(getRefLenFile());
				mapReads.setNormalType(mapNormType);
			}
		}
		setMapCorrect();
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
	 * @param uniqReads
	 *            当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod
	 *            从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique
	 *            Unique的reads在哪一列 novelbio的标记在第七列，从1开始计算
	 * @param booUniqueMapping
	 *            重复的reads是否只选择一条
	 * @param cis5to3
	 *            是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique,
			boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, colUnique, booUniqueMapping,
				cis5to3);
	}

	/**
	 * 返回某条染色体上的reads情况，不是密度图，只是简单的计算reads在一个染色体上的情况 主要用于RefSeq时，一个基因上的reads情况
	 * 
	 * @param chrID
	 * @param thisInvNum
	 *            每个区间几bp
	 * @parm type 取样方法 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 没有的话就返回null
	 */
	public double[] getChrInfo(String chrID, int thisInvNum, int type) {
		double[] tmpResult = mapReads.getRengeInfo(thisInvNum, chrID, 0, 0,
				type);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}

	int maxresolution = 10000;

	/**
	 * 画出所有染色体上密度图
	 * 用R画
	 * @param gffChrMap2
	 *            是否有第二条染色体，没有的话就是null
	 * @throws Exception
	 */
	public void getAllChrDistR(GffChrMap gffChrMap2) {
		ArrayList<String[]> chrlengthInfo = seqHash.getChrLengthInfo();
		for (int i = chrlengthInfo.size() - 1; i >= 0; i--) {
			try {
				getChrDist(chrlengthInfo.get(i)[0], maxresolution, gffChrMap2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 画出所有染色体上密度图
	 * 用java画
	 * @param gffChrMap2
	 *            是否有第二条染色体，没有的话就是null
	 * @throws Exception
	 */
	public void plotAllChrDist(String outPath) {
		ArrayList<String[]> chrlengthInfo = seqHash.getChrLengthInfo();
		//find the longest chromosome's density
		double[] chrReads = getChrDensity(seqHash.getChrLengthInfo().get(seqHash.getChrLengthInfo().size() - 1)[0], maxresolution);
		double axisY = MathComput.median(chrReads, 95)*4;
		for (int i = chrlengthInfo.size() - 1; i >= 0; i--) {
			try {
				plotChrDist(chrlengthInfo.get(i)[0], maxresolution, axisY, FileOperate.changeFileSuffix(outPath, "_"+chrlengthInfo.get(i)[0], "png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 给定染色体，返回该染色体上reads分布
	 * 
	 * @param chrID
	 *            第几个软色体
	 * @param maxresolution
	 *            最长分辨率
	 * @param gffChrMap2
	 *            如果需要画第二条染色体的图，也就是对称了画
	 * @param 输出文件名
	 *            ，带后缀"_chrID"
	 * @throws Exception
	 */
	private void plotChrDist(String chrID, int maxresolution, double axisY, String outFileName) throws Exception {
		int[] resolution = seqHash.getChrRes(chrID, maxresolution);
		long chrLengthMax = seqHash.getChrLenMax();
		double interval = ((int)(chrLengthMax/30)/1000)*1000;
		long chrLength = seqHash.getChrLength(chrID);
		
		/////////////////////   plotScatter can only accept double data   //////////////////////////////
		double[] resolutionDoub = new double[resolution.length];
		for (int i = 0; i < resolution.length; i++) {
			resolutionDoub[i] = i;
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] chrReads = getChrDensity(chrID.toLowerCase(), resolution.length);
		
		//////////////////////////////////////
//		double[] x = new double[1000];
//		double[] y = new double[1000];
//		for (int i = 0; i < x.length; i++) {
//			x[i] = i*2;
//			y[i] = 10;
//		}
		////////////////////////////////////////
		if (chrReads == null) {
			return;
		}
		PlotScatter plotScatter = new PlotScatter();
		plotScatter.setAxisX(0, maxresolution);
		plotScatter.setAxisY(0, axisY);
		plotScatter.mapNum2ChangeX(0, 0, resolution.length, chrLength, interval);
		DotStyle dotStyle = new DotStyle();
		dotStyle.setColor(new Color(0, 0, 255, 255));
		dotStyle.setStyle(DotStyle.STYLE_AREA);
		plotScatter.addXY(resolutionDoub, chrReads, dotStyle);
//		plotScatter.addXY(x, y, dotStyle);

		plotScatter.setBg(Color.WHITE);
		plotScatter.setAlpha(false);
		plotScatter.setTitle(chrID + " Reads Density", null);
		plotScatter.setTitleX("Chromosome Length", null, 0);
		plotScatter.setTitleY("Normalized Reads Counts", null, (int)axisY/5);
		
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_ML);
		
		plotScatter.saveToFile(outFileName, 10000, 1000);
	}
	/**
	 * 给定染色体，返回该染色体上reads分布
	 * 
	 * @param chrID
	 *            第几个软色体
	 * @param maxresolution
	 *            最长分辨率
	 * @param gffChrMap2
	 *            如果需要画第二条染色体的图，也就是对称了画
	 * @param 输出文件名
	 *            ，带后缀"_chrID"
	 * @throws Exception
	 */
	private void getChrDist(String chrID, int maxresolution,
			GffChrMap gffChrMap2) throws Exception {
		int[] resolution = seqHash.getChrRes(chrID, maxresolution);
		double[] chrReads = getChrDensity(chrID.toLowerCase(),
				resolution.length);
		long chrLength = seqHash.getChrLength(chrID);
		if (chrReads != null) {
			TxtReadandWrite txtRparamater = new TxtReadandWrite();
			// //////// 参 数 设 置 /////////////////////
			txtRparamater.setParameter(
					NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM, true, false);
			txtRparamater.writefile("Item" + "\t" + "Info" + "\r\n");// 必须要加上的，否则R读取会有问题
			txtRparamater.writefile("tihsresolution" + "\t" + chrLength
					+ "\r\n");
			txtRparamater.writefile("maxresolution" + "\t"
					+ seqHash.getChrLenMax() + "\r\n");
			txtRparamater.writefile("ChrID" + "\t" + chrID + "\r\n");

			// //////// 数 据 输 入 ///////////////////////
			txtRparamater.setParameter(
					NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X, true, false);
			txtRparamater.Rwritefile(resolution);
			txtRparamater.setParameter(
					NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y, true, false);
			txtRparamater.Rwritefile(chrReads);

			// /////////如果第二条染色体上有东西，那么也写入文本/////////////////////////////////////////
			if (gffChrMap2 != null) {
				double[] chrReads2 = gffChrMap2.getChrDensity(
						chrID.toLowerCase(), resolution.length);
				txtRparamater
						.setParameter(
								NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_2Y,
								true, false);
				txtRparamater.Rwritefile(chrReads2);
			}
			hist();
			FileOperate
					.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X,
							chrID + "readsx");
			FileOperate
					.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y,
							chrID + "readsy");
			FileOperate.changeFileName(
					NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_2Y, chrID
							+ "reads2y");
			FileOperate.changeFileName(
					NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM, chrID
							+ "parameter");
		}
	}

	/**
	 * 返回某条染色体上的reads情况，是密度图 主要用于基因组上，一条染色体上的reads情况
	 * 
	 * @param chrID
	 * @param binNum
	 *            分成几个区间
	 * @parm type 取样方法 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 没有的话就返回null
	 */
	private double[] getChrDensity(String chrID, int binNum) {
		double[] tmpResult = mapReads.getReadsDensity(chrID, 0, 0, binNum);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}
	/**
	 * 调用R画图
	 * 
	 * @throws Exception
	 */
	private void hist() throws Exception {
		// 这个就是相对路径，必须在当前文件夹下运行
		String command = "Rscript "
				+ NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_RSCRIPT;
		Runtime r = Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}

	/**
	 * 
	 * @param color
	 * @param SortS2M
	 *            是否从小到大排序
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore 如果小于0或等于colGeneID，那么就用指定区域的reads当作score
	 * @param rowStart
	 * @param heapMapSmall
	 * @param heapMapBig
	 * @param scale
	 * @param structure
	 *            基因结构，目前只有 GffDetailGene.TSS 和 GffDetailGene.TES
	 * @param binNum
	 *            最后分成几块
	 * @param outFile
	 */
	public void plotTssTesHeatMap(Color color, boolean SortS2M,
			String txtExcel, int colGeneID, int colScore, int rowStart,
			double heapMapSmall, double heapMapBig, double scale,
			String structure, int binNum, String outFile) {
		ArrayList<MapInfo> lsMapInfos = super.readFileGeneMapInfo(txtExcel,
				colGeneID, colScore, rowStart, structure, binNum);
		MapInfo.sortPath(SortS2M);
		Collections.sort(lsMapInfos);
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 100,
		// HeatChart.SCALE_LINEAR, FileOperate.changeFileSuffix(outFile,
		// "_100line", null));
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 100,
		// HeatChart.SCALE_EXPONENTIAL, FileOperate.changeFileSuffix(outFile,
		// "_100exp", null));
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 100,
		// HeatChart.SCALE_LOGARITHMIC, FileOperate.changeFileSuffix(outFile,
		// "_100log", null));

		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 70,
		// HeatChart.SCALE_LINEAR, FileOperate.changeFileSuffix(outFile,
		// "_70line", null));
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 70,
		// HeatChart.SCALE_EXPONENTIAL, FileOperate.changeFileSuffix(outFile,
		// "_70exp", null));
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 70,
		// HeatChart.SCALE_LOGARITHMIC, FileOperate.changeFileSuffix(outFile,
		// "_70log", null));

		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 200,
		// HeatChart.SCALE_LINEAR, FileOperate.changeFileSuffix(outFile,
		// "_200line", null));
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 200,
		// HeatChart.SCALE_EXPONENTIAL, FileOperate.changeFileSuffix(outFile,
		// "_200exp", null));
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 200,
		// HeatChart.SCALE_LOGARITHMIC, FileOperate.changeFileSuffix(outFile,
		// "_200log", null));
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 100, 0.5,
		// FileOperate.changeFileSuffix(outFile, "_100log", null));
		// plotHeatMap(lsMapInfos, structure, Color.BLUE, 0, 450, 1.5,
		// FileOperate.changeFileSuffix(outFile, null, null));
		plotHeatMap(lsMapInfos, structure, color, heapMapSmall, heapMapBig,
				scale, FileOperate.changeFileSuffix(outFile, null, null));
	}

	/**
	 * 
	 * 获得summit位点，画summit位点附近的reads图
	 * 
	 * @param SortS2M
	 *            是否从小到大排序
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
	public void plotSummitHeatMap(boolean SortS2M, String txtExcel,
			int colChrID, int colSummit, int colRegion, int colScore,
			int rowStart, double heapMapSmall, double heapMapBig, double scale,
			int binNum, String outFile) {
		ArrayList<MapInfo> lsMapInfos = super.readFileSiteMapInfo(txtExcel,
				colRegion, colChrID, colSummit, colScore, rowStart);
		MapInfo.sortPath(SortS2M);
		Collections.sort(lsMapInfos);
		mapReads.getRegionLs(binNum, lsMapInfos, 0);
		plotHeatMap(lsMapInfos, "", Color.BLUE, heapMapSmall, heapMapBig,
				scale, FileOperate.changeFileSuffix(outFile, null, null));
	}

	/**
	 * @param lsMapInfo
	 *            基因信息
	 * @param structure
	 *            基因结构，目前只有 GffDetailGene.TSS 和 GffDetailGene.TES
	 * @param color
	 * @param small
	 *            最小
	 * @param big
	 *            最大
	 * @param scale
	 *            scale次方，大于1则稀疏高表达，小于1则稀疏低表达
	 * @param outFile
	 */
	private static void plotHeatMap(ArrayList<MapInfo> lsMapInfo,
			String structure, Color color, double small, double big,
			double scale, String outFile) {
		HeatChart map = new HeatChart(lsMapInfo, small, big);
		if (structure.equals(GffDetailGene.TSS)) {
			map.setTitle("HeatMap Of TSS");
			map.setXAxisLabel("Distance To TSS");
			map.setYAxisLabel("");
		} else if (structure.equals(GffDetailGene.TES)) {
			map.setTitle("HeatMap Of TES");
			map.setXAxisLabel("Distance To TES");
			map.setYAxisLabel("");
		} else {
			map.setTitle("HeatMap Of Summit");
			map.setXAxisLabel("Distance To Summit");
			map.setYAxisLabel("");
		}

		String[] aa = new String[] { "a", "b", "c", "d", "e", "f" };
		map.setXValues(aa);
		String[] nn = new String[lsMapInfo.get(0).getDouble().length];
		for (int i = 0; i < nn.length; i++) {
			nn[i] = "";
		}
		map.setYValues(nn);
		Dimension bb = new Dimension();
		bb.setSize(1, 0.01);
		map.setCellSize(bb);
		// Output the chart to a file.
		Color colorblue = color;
		Color colorRed = Color.WHITE;
		// map.setBackgroundColour(color);
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
	 * @param lsMapInfo
	 *            基因信息
	 * @param structure
	 *            基因结构，目前只有 GffDetailGene.TSS 和 GffDetailGene.TES
	 * @param color
	 * @param small
	 *            最小
	 * @param big
	 *            最大
	 * @param scale
	 *            scale次方，大于1则稀疏高表达，小于1则稀疏低表达
	 * @param outFile
	 */
	public static void plotHeatMap2(ArrayList<MapInfo> lsMapInfo,
			ArrayList<MapInfo> lsMapInfo2, String outFile, double mindata1,
			double maxdata1, double mindata2, double maxdata2) {
		Color colorred = new Color(255, 0, 0, 255);
		Color colorwhite = new Color(0, 0, 0, 0);
		Color colorgreen = new Color(0, 255, 0, 255);

		Color[] gradientColors = new Color[] { colorwhite, colorred };
		Color[] customGradient = Gradient.createMultiGradient(gradientColors,
				250);

		Color[] gradientColors2 = new Color[] { colorwhite, colorgreen };
		Color[] customGradient2 = Gradient.createMultiGradient(gradientColors2,
				250);
		PlotHeatMap heatMap = new PlotHeatMap(lsMapInfo, lsMapInfo2, false,
				customGradient, customGradient2);
		heatMap.setRange(mindata1, maxdata1, mindata2, maxdata2);
		heatMap.saveToFile(outFile, 4000, 1000);

	}

	/**
	 * @param lsMapInfo1
	 * @param lsMapInfo2
	 * @param outFile
	 * @param mindata1 热图上的所能显示最深颜色的最小值
	 * @param maxdata1 热图上的所能显示最深颜色的最大值
	 */
	public static void plotHeatMapMinus(ArrayList<MapInfo> lsMapInfo1,
			ArrayList<MapInfo> lsMapInfo2, String outFile, double mindata1,
			double maxdata1) {
		ArrayList<MapInfo> lsMapInfoFinal = MapInfo.minusListMapInfo(
				lsMapInfo1, lsMapInfo2);
		Color colorgreen = new Color(0, 255, 0, 255);
		Color colorwhite = new Color(255, 255, 255, 255);
		Color colorred = new Color(255, 0, 0, 255);

		Color[] gradientColors = new Color[] { colorgreen, colorwhite, colorred };
		Color[] customGradient = Gradient.createMultiGradient(gradientColors,
				250);

		PlotHeatMap heatMap = new PlotHeatMap(lsMapInfoFinal, false, customGradient);
		heatMap.setRange(mindata1, maxdata1);
		heatMap.saveToFile(outFile, 6000, 1000);
	}

	/**
	 * 根据前面设定upBp和downBp 根据Peak所覆盖的基因做出TSS图
	 * @param fileName Peak文件
	 * @param rowStart 从第几行开始读
	 * @param binNum 分成几份
	 * @param resultFile 输出文件
	 * @param geneStructure GffDetailGene.TSS
	 */
	public void plotRegionDensity(String fileName, int rowStart, int binNum, String resultFile, String geneStructure) {
		ArrayList<MapInfo> lsMapInfo = super.readFileRegionMapInfo(fileName, 1, 2, 3, 0, rowStart);
		ArrayList<MapInfo> lsMapTssInfo = super.getPeakCoveredGeneMapInfo(lsMapInfo, geneStructure, binNum);
		double[] TssDensity = MapInfo.getCombLsMapInfo(lsMapTssInfo);
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		for (double d : TssDensity) {
			txtWrite.writefileln(d+"");
		}
		txtWrite.close();
	}
	/**
	 * 根据前面设定upBp和downBp 根据指定的基因做出TSS图
	 * @param fileName 基因文件，必须第一列为geneID，内部去重复
	 * @param rowStart 从第几行开始读
	 * @param binNum 分成几份
	 * @param resultFile 输出文件
	 * @param geneStructure GffDetailGene.TSS
	 */
	public void plotGeneDensity(String fileName, int rowStart, int binNum, String resultFile, String geneStructure) {
		ArrayList<MapInfo> lsMapInfo = super.readFileGeneMapInfo(fileName, 1, 0, rowStart, geneStructure, binNum);
		double[] TssDensity = MapInfo.getCombLsMapInfo(lsMapInfo);
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		for (double d : TssDensity) {
			txtWrite.writefileln(d+"");
		}
		txtWrite.close();
	}
	/**
	 * 给定基因的symbol，返回该基因在tss附近区域的mapreads的平均数
	 * @param geneID 基因名字
	 * @param tssUp tss上游多少bp 负数在上游正数在下游
	 * @param tssDown tss下游多少bp 负数在上游正数在下游
	 */
	private double getGeneTss(String geneID, int tssUp, int tssDown) {
		GffDetailGene gffDetailGene = gffHashGene.searchLOC(geneID);
		if (gffDetailGene == null) {
			return -1;
		}
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		int tssSite = gffGeneIsoInfo.getTSSsite();
		int tssStartR = 0; int tssEndR = 0;
		//方向不同，区域也不同
		if (gffGeneIsoInfo.isCis5to3()) {
			tssStartR = tssSite + tssUp;
			tssEndR = tssSite + tssDown;
		}
		else {
			tssStartR = tssSite - tssUp;
			tssEndR = tssSite - tssDown;
		}
		int tssStart = Math.min(tssStartR, tssEndR);
		int tssEnd = Math.max(tssStartR, tssEndR);
		double[] siteInfo = mapReads.getRengeInfo(mapReads.getBinNum(), gffGeneIsoInfo.getChrID(), tssStart, tssEnd, 0);
		return MathComput.mean(siteInfo);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 专为韩燕设计<br>
	 * 当为refseq时，获得的某个基因的分布情况，按照3个barcode划分
	 * 
	 * @return 没有该基因则返回null
	 */
	public double[] getGeneReadsHYRefseq(String geneID) {
		double[] tmpResult = getChrInfo(geneID, 1, 0);
		if (tmpResult == null) {
			return null;
		}
		// 获得具体转录本的信息
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return combineLoc(tmpResult, gffGeneIsoInfoOut.getLenUTR5() + 1);
	}

	/**
	 * 给定atg位点，获得该atg位点在合并后的序列中应该是第几个，从1开始
	 * 
	 * @param atgSite
	 * @return
	 */
	public int getCombAtgSite(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		int atgSite = gffGeneIsoInfoOut.getLenUTR5() + 1;
		// 除以3是指3个碱基
		return (int) Math.ceil((double) (atgSite - 1) / 3);
	}

	/**
	 * 给定atg位点，获得该atg位点在合并后的序列中应该是第几个，从1开始
	 * 
	 * @param atgSite
	 * @return
	 */
	public int getAtgSite(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return gffGeneIsoInfoOut.getLenUTR5() + 1;
		// 除以3是指3个碱基
	}
	/**
	 * 设定peak的bed文件，第一列为chrID，第二列为起点，第三列为终点， 返回去除peak后，每条染色体的bg情况
	 * @param peakFile
	 * @param firstlinls1
	 * @return
	 */
	public ArrayList<String[]> getBG(String peakFile, int firstlinls1)
	{
		return mapReads.getChIPBG(peakFile, firstlinls1);
	}
	/**
	 * 专为韩燕设计 将三个碱基合并为1个coding，取3个的最后一个碱基对应的reads数
	 * 
	 * @param geneReads
	 *            该基因的reads信息，必须是单碱基精度
	 * @param AtgSite
	 *            该基因的atg位点，从1开始计算
	 * @return 返回经过合并的结果，譬如 {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17}; atg位点为6
	 *         结果{ 2,5,8,11,14,17};
	 */
	private double[] combineLoc(double[] geneReads, int AtgSite) {
		// 此时的SeqInfo第一位就是实际的第一位，不是atgsite了
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
			if (!gffGeneIsoInfo.getGeneType().equals(
					GffGeneIsoInfo.TYPE_GENE_MRNA)
					&& !gffGeneIsoInfo.getGeneType().equals(
							GffGeneIsoInfo.TYPE_GENE_MRNA_TE)) {
				continue;
			}

			double[] tmp = mapReads.getRengeInfo(mapReads.getBinNum(), string,
					0, 0, 0);
			mapReads.setNormalType(super.mapNormType);
			double[] tmp2 = mapReads.getRengeInfo(mapReads.getBinNum(), string,
					0, 0, 0);
			// ///////////////// 异 常 处 理
			// /////////////////////////////////////////////////////////////////////
			if (tmp == null && tmp2 == null) {
				continue;
			} else if (tmp == null) {
				tmp = new double[tmp2.length];
			} else if (tmp2 == null) {
				tmp2 = new double[tmp.length];
			}
			// //////////////////////////////////////////////////////////////////////////////////////
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
	 * 经过标准化 将MapInfo中的double填充上相应的reads信息
	 * 
	 * @param binNum
	 *            待分割的区域数目
	 * @param lsmapInfo
	 * @param type
	 *            0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegionLs(int binNum, ArrayList<MapInfo> lsmapInfo, int type) {
		mapReads.getRegionLs(binNum, lsmapInfo, type);
	}
	/**
	 * 经过标准化 将MapInfo中的double填充上相应的reads信息
	 * 
	 * @param binNum
	 *            待分割的区域数目
	 * @param lsmapInfo
	 * @param type
	 *            0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegion(int binNum, MapInfo mapInfo, int type) {
		mapReads.getRegion(binNum, mapInfo, type);
	}
	
	/**
	 * 经过标准化，和equations修正
	 * @param lsmapInfo
	 * @param thisInvNum  每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegion(MapInfo mapInfo, int thisInvNum, int type) {
		mapReads.getRegion(mapInfo, thisInvNum, type);
	}
}
