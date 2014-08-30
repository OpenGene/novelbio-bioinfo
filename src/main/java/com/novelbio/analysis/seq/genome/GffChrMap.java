package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.analysis.seq.genome.mappingOperate.EnumMapNormalizeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteSeqInfo;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotBar;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;

import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.Location;

/**
 * 给定基因的区域，画出各种统计图
 * 
 * @author zong0jie
 * 
 */
public class GffChrMap {
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	Logger logger = Logger.getLogger(GffChrMap.class);
	String fileName = "";
	int maxresolution = 10000;
	/** 绘图区域，也用于tss和tes的范围 */
	int[] plotRange;
	MapReads mapReads;
	EnumMapNormalizeType mapNormType = EnumMapNormalizeType.allreads;
	
	
	public GffChrMap() {
	}
	
	public GffChrMap(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 将GffChrAbs导入
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setPlotRange(int[] plotRange) {
		this.plotRange = plotRange;
	}
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}
	
	public void setMapReads(MapReads mapReads) {
		this.mapReads = mapReads;
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
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param cis5to3 是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, booUniqueMapping, cis5to3);
	}
	
	/**
	 * 主要用于全基因组做图的，所以结果不按照基因方向进行颠倒
	 * @param geneStructure
	 * @param gffDetailGene1
	 * @param num 具体第几个，譬如马红就想看第一个内含子或者第一个外显子 小于等于0表示看全体
	 * @return
	 */
	public void setFilterChrDistInfo(GeneStructure geneStructure, int num) {
		if (geneStructure == GeneStructure.ALLLENGTH) {
			mapReads.setMapChrID2LsAlignments(null);
			return;
		}
		
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();
		HashMap<String, List<? extends Alignment>> mapChrID2LsAlignment = new HashMap<String, List<? extends Alignment>>();
		for (String chrID : gffHashGene.getMapChrID2LsGff().keySet()) {
			ArrayList<SiteSeqInfo> lsAlignment = new ArrayList<SiteSeqInfo>();
			ListGff listGff = gffHashGene.getMapChrID2LsGff().get(chrID.toLowerCase());
			for (GffDetailGene gffDetailGene : listGff) {
				lsAlignment.addAll(getGeneStructureRangeForChrPlot(geneStructure, gffDetailGene, num));
			}
			SiteSeqInfo.setCompareType(SiteSeqInfo.COMPARE_LOCSITE);
			Collections.sort(lsAlignment);
			mapChrID2LsAlignment.put(chrID.toLowerCase(), lsAlignment);
		}
		mapReads.setMapChrID2LsAlignments(mapChrID2LsAlignment);
	}
	/**
	 * 主要用于全基因组做图的，所以结果不按照基因方向进行颠倒
	 * @param geneStructure
	 * @param gffDetailGene
	 * @param num 具体第几个，譬如马红就想看第一个内含子或者第一个外显子 小于等于0表示看全体
	 * @return
	 */
	private ArrayList<SiteSeqInfo> getGeneStructureRangeForChrPlot(GeneStructure geneStructure, GffDetailGene gffDetailGene, int num) {
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		ArrayList<SiteSeqInfo> lsResult = new ArrayList<SiteSeqInfo>();
		
		if (geneStructure == GeneStructure.TSS) {
			SiteSeqInfo siteInfo = new SiteSeqInfo(gffDetailGene.getRefID());
			if (gffGeneIsoInfo.isCis5to3()) {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() + plotRange[0], gffGeneIsoInfo.getTSSsite() + plotRange[1]);
			} else {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() - plotRange[1], gffGeneIsoInfo.getTSSsite() - plotRange[0]);
			}
			lsResult.add(siteInfo);
		}
		
		else if (geneStructure == GeneStructure.TES) {
			SiteSeqInfo siteInfo = new SiteSeqInfo(gffDetailGene.getRefID());
			if (gffGeneIsoInfo.isCis5to3()) {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() + plotRange[0], gffGeneIsoInfo.getTESsite() + plotRange[1]);
			} else {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() - plotRange[1], gffGeneIsoInfo.getTESsite() - plotRange[0]);
			}
			lsResult.add(siteInfo);
		}
		
		else if (geneStructure == GeneStructure.EXON) {
			if (num <= 0) {
				for (ExonInfo exonInfo : gffGeneIsoInfo) {
					SiteSeqInfo siteInfo = new SiteSeqInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
					lsResult.add(siteInfo);
				}
			} else {
				if (gffGeneIsoInfo.size() > num) {
					SiteSeqInfo siteInfo = new SiteSeqInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(gffGeneIsoInfo.get(num - 1).getStartAbs(), gffGeneIsoInfo.get(num - 1).getEndAbs());
					lsResult.add(siteInfo);
				}
			}
		}
		
		else if (geneStructure == GeneStructure.INTRON) {
			if (num <= 0) {
				for (ExonInfo exonInfo : gffGeneIsoInfo.getLsIntron()) {
					SiteSeqInfo siteInfo = new SiteSeqInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
					lsResult.add(siteInfo);
				}
			} else {
				ArrayList<ExonInfo> lsIntron = gffGeneIsoInfo.getLsIntron();
				if (lsIntron.size() >= num) {
					SiteSeqInfo siteInfo = new SiteSeqInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(lsIntron.get(num - 1).getStartAbs(), lsIntron.get(num - 1).getEndAbs());
					lsResult.add(siteInfo);
				}
			}
			
		} else if (geneStructure == GeneStructure.UTR5) {
			for (ExonInfo exonInfo : gffGeneIsoInfo.getUTR5seq()) {
				SiteSeqInfo siteInfo = new SiteSeqInfo(gffDetailGene.getRefID());
				siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
				lsResult.add(siteInfo);
			}
		} else if (geneStructure == GeneStructure.UTR3) {
			for (ExonInfo exonInfo : gffGeneIsoInfo.getUTR3seq()) {
				SiteSeqInfo siteInfo = new SiteSeqInfo(gffDetailGene.getRefID());
				siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
				lsResult.add(siteInfo);
			}
		}
		return lsResult;
	}
	
	/**
	 * 返回某条染色体上的reads情况，不是密度图，只是简单的计算reads在一个染色体上的情况 主要用于RefSeq时，一个基因上的reads情况
	 * @param chrID
	 * @param thisInvNum 每个区间几bp
	 * @parm type 取样方法 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 没有的话就返回null
	 */
	public double[] getChrInfo(String chrID, int thisInvNum, int type) {
		double[] tmpResult = mapReads.getRangeInfo(thisInvNum, chrID, 0, 0, type);
		return tmpResult;
	}
	/**
	 * 画出所有染色体上密度图
	 * 用java画
	 * @param outPathPrefix 输出文件夹+前缀
	 * @throws Exception
	 */
	public void plotAllChrDist(String outPathPrefix, double yHeight) {
		ArrayList<String[]> chrlengthInfo = gffChrAbs.getSeqHash().getChrLengthInfo();
		//find the longest chromosome's density
		double[] chrReads = getChrDensity(chrlengthInfo.get(chrlengthInfo.size() - 1)[0], maxresolution);
		if (yHeight <= 0) {
			yHeight = MathComput.median(chrReads, 95)*4;
		}
		for (int i = chrlengthInfo.size() - 1; i >= 0; i--) {
			try {
				plotChrDist(chrlengthInfo.get(i)[0], maxresolution, yHeight, FileOperate.changeFileSuffix(outPathPrefix, "_"+chrlengthInfo.get(i)[0], "png"));
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
	 * @param axisY y轴边界
	 * @param outFileName 输出文件名，带后缀"_chrID"
	 * @throws Exception
	 */
	private void plotChrDist(String chrID, int maxresolution, double axisY, String outFileName) throws Exception {
		int[] resolution = gffChrAbs.getSeqHash().getChrRes(chrID, maxresolution);
		long chrLengthMax = gffChrAbs.getSeqHash().getChrLenMax();
		double interval = ((int)(chrLengthMax/30)/1000)*1000;
		long chrLength = gffChrAbs.getSeqHash().getChrLength(chrID);
		
		/////////////////////   plotScatter can only accept double data   //////////////////////////////
		double[] resolutionDoub = new double[resolution.length];
		for (int i = 0; i < resolution.length; i++) {
			resolutionDoub[i] = i;
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] chrReads = null;
		try {
			chrReads = getChrDensity(chrID.toLowerCase(), resolution.length);
		} catch (Exception e) {
			logger.error("出现未知chrID：" + chrID);
			return;
		}
		if (chrReads == null) {
			return;
		}
		
		PlotScatter plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
		plotScatter.setAxisX(0, maxresolution);
		plotScatter.setAxisY(0, axisY);
		plotScatter.setMapNum2ChangeX(0, 0, resolution.length, chrLength, interval);

		DotStyle dotStyle = new DotStyle();
		Paint colorGradient = DotStyle.getGridentColor(GraphicsUtils.deriveDarker(Color.blue), Color.blue);
		dotStyle.setColor(colorGradient);
		dotStyle.setStyle(DotStyle.STYLE_AREA);
		plotScatter.addXY(resolutionDoub, chrReads, dotStyle);
		
		//////////////////添加边框///////////////////////////////
		DotStyle dotStyleBroad = new DotStyle();
		dotStyleBroad.setStyle(DotStyle.STYLE_LINE);
		dotStyleBroad.setColor(Color.RED);
		dotStyleBroad.setSize(DotStyle.SIZE_B);
		double[] xstart = new double[]{0,0}; double[] xend= new double[]{resolutionDoub[resolutionDoub.length-1], resolutionDoub[resolutionDoub.length-1]};
		double[] y = new double[]{0, axisY};
		plotScatter.addXY(xend, y, dotStyleBroad);
		plotScatter.addXY(xstart, y, dotStyleBroad.clone());
		//////////////////////////////////////////////////////////////
		
		plotScatter.setBg(Color.WHITE);
		plotScatter.setAlpha(false);
		//坐标轴mapping
		
		//坐标轴mappin
		plotScatter.setTitle(chrID + " Reads Density",  new Font(Font.SANS_SERIF, Font.BOLD, 30), Location.CENTER, 100);
//		plotScatter.setTitle("chr1 Reads Density", null);
		plotScatter.setTitleX("Chromosome Length",  new Font(Font.SANS_SERIF, Font.BOLD, 20), 3, 0);
		plotScatter.setTitleY("Normalized Reads Counts", new Font(Font.SANS_SERIF, Font.BOLD, 22), 3.5, 90);
		plotScatter.setAxisTicksXFont(new Font(Font.SANS_SERIF, Font.PLAIN, 19), 0, 0);
		plotScatter.setAxisTicksYFont(new Font(Font.SANS_SERIF, Font.PLAIN, 19),  PlotBar.getSpace(axisY, 5), 0);
		plotScatter.setInsets(170, 100, 50, 150);
		
		plotScatter.saveToFile(outFileName, 10000, 1000);
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
		return tmpResult;
	}
	/**
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
	 * @return 返回最大值和最小值的设定
	 */
	public double[] plotTssHeatMap(Color color, boolean SortS2M, String txtExcel, int colGeneID, 
			int colScore, int rowStart, double heapMapSmall, double heapMapBig,
			GeneStructure structure, int binNum, String outFile) {
		ArrayList<MapInfo> lsMapInfos = null;
		if (txtExcel != null && !txtExcel.trim().equals("")) {
			lsMapInfos = readFileGeneMapInfo(txtExcel, colGeneID, colScore, rowStart, structure, binNum);
		}
		else {
			lsMapInfos = readGeneMapInfoAll(structure, binNum);
		}
		MapInfo.sortPath(SortS2M);
		Collections.sort(lsMapInfos);
		return plotHeatMap(lsMapInfos, color, heapMapSmall, heapMapBig,
				FileOperate.changeFileSuffix(outFile, "_HeatMap", "png"));
	}

	/**
	 * 获得summit位点，画summit位点附近的reads图
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
	 * @return 返回最大值和最小值的设定
	 */
	public double[] plotSummitHeatMap(boolean SortS2M, String txtExcel,
			int colChrID, int colSummit, int colRegion, int colScore,
			int rowStart, double heapMapSmall, double heapMapBig, double scale,
			int binNum, String outFile) {
		ArrayList<MapInfo> lsMapInfos = gffChrAbs.readFileSiteMapInfo(txtExcel,
				colRegion, colChrID, colSummit, colScore, rowStart);
		MapInfo.sortPath(SortS2M);
		Collections.sort(lsMapInfos);
		mapReads.getRangeLs(binNum, lsMapInfos, 0);
		return plotHeatMap(lsMapInfos, Color.BLUE, heapMapSmall, heapMapBig,
				 FileOperate.changeFileSuffix(outFile, null, null));
	}
	/**
	 * @param lsMapInfo
	 *            基因信息
	 * @param color
	 * @param small
	 *            最小
	 * @param big 
	 *            最大 如果最大小于最小，则选择上95分位点为最高点
	 * @param scale
	 *            scale次方，大于1则稀疏高表达，小于1则稀疏低表达
	 * @param outFile
	 * @return 返回最大值和最小值的设定
	 */
	private static double[] plotHeatMap(ArrayList<MapInfo> lsMapInfo, Color color,double mindata, double maxdata, String outFile) {
		if (maxdata <= mindata) {
			ArrayList<Double> lsDouble = new ArrayList<Double>();
			for (int i = 0; i < 20; i++) {
				if (i >= lsMapInfo.size()) {
					break;
				}
				MapInfo mapInfo = lsMapInfo.get(i);
				double[] info = mapInfo.getDouble();
				for (Double double1 : info) {
					lsDouble.add(double1);
				}
			}
			for (int i = lsMapInfo.size() - 1; i > lsMapInfo.size() - 21; i--) {
				if (i < 0) {
					break;
				}
				MapInfo mapInfo = lsMapInfo.get(i);
				double[] info = mapInfo.getDouble();
				for (Double double1 : info) {
					lsDouble.add(double1);
				}
			}
			maxdata = MathComput.median(lsDouble, 99);
		}
		Color colorwhite = new Color(255, 255, 255, 255);
		Color[] gradientColors = new Color[] { colorwhite, color};
		Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);
		PlotHeatMap heatMap = new PlotHeatMap(lsMapInfo,  customGradient);
		heatMap.setRange(mindata, maxdata);
		heatMap.saveToFile(outFile, 2000, 1000);
		
		
		
//		HeatChart heatChart = new HeatChart(lsMapInfo,mindata,maxdata);
//		Dimension bb = new Dimension();
//		bb.setSize(1, 0.01);
//		heatChart.setCellSize(bb );
//		//Output the chart to a file.
//		Color colorblue = Color.BLUE;
//		Color colorRed = Color.WHITE;
//		//map.setBackgroundColour(color);
//		heatChart.setHighValueColour(colorblue);
//		heatChart.setLowValueColour(colorRed);
//		try {
//			heatChart.saveToFile(new File(outFile));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return new double[]{mindata, maxdata};
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
	public void plotTssPeak(String fileName, int rowStart, int binNum, String resultFile, GeneStructure geneStructure) {
		ArrayList<MapInfo> lsMapInfo = gffChrAbs.readFileRegionMapInfo(fileName, 1, 2, 3, 0, rowStart);
		ArrayList<MapInfo> lsMapTssInfo = getPeakCoveredGeneMapInfo(lsMapInfo, geneStructure, binNum);
		double[] TssDensity = MapInfo.getCombLsMapInfo(lsMapTssInfo);
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		for (double d : TssDensity) {
			txtWrite.writefileln(d+"");
		}
		txtWrite.close();
	}
	/**
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
	 * @param mirandaResultOut
	 * @return 返回最大值和最小值的设定
	 */
	public double[] plotTssPeakHeatMap(Color color, String fileName,int heatMapSmall, int heatMapBig, int rowStart, int binNum, String resultFile, GeneStructure geneStructure, boolean SortS2M) {
		ArrayList<MapInfo> lsMapInfo = gffChrAbs.readFileRegionMapInfo(fileName, 1, 2, 3, 0, rowStart);
		ArrayList<MapInfo> lsMapTssInfo = getPeakCoveredGeneMapInfo(lsMapInfo, geneStructure, binNum);
		MapInfo.sortPath(SortS2M);
		for (MapInfo mapInfo : lsMapTssInfo) {
			mapInfo.setScore(MathComput.mean(mapInfo.getDouble()));
		}
		Collections.sort(lsMapTssInfo);
		return plotHeatMap(lsMapTssInfo, color, heatMapSmall, heatMapBig,
				FileOperate.changeFileSuffix(resultFile, "_HeatMap", "png"));
	}
	/**
	 * 根据全部基因做出TSS图
	 * @param binNum 分成几份
	 * @param resultFile 输出文件
	 * @param geneStructure GffDetailGene.TSS
	 */
	public void plotTssAllGene(int binNum, String resultFile, GeneStructure geneStructure) {
		plotTssGene(null, 0, binNum, resultFile, geneStructure);
	}
	/**
	 * 根据前面设定upBp和downBp 根据指定的基因做出TSS图
	 * @param fileName 基因文件，必须第一列为geneID，内部去重复, 如果没有文件，则返回全体基因
	 * @param rowStart 从第几行开始读
	 * @param binNum 分成几份
	 * @param resultFile 输出文件
	 * @param geneStructure GffDetailGene.TSS
	 */
	public void plotTssGene(String fileName, int rowStart, int binNum, String resultFile, GeneStructure geneStructure) {
		ArrayList<MapInfo> lsMapInfo = null;
		if (fileName == null || fileName.trim().equals("")) {
			lsMapInfo = readGeneMapInfoAll(geneStructure, binNum);
		} else {
			lsMapInfo = readFileGeneMapInfo(fileName, 1, 0, rowStart, geneStructure, binNum);
		}
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
	 * @return 如果没有则返回-1
	 */
	public double getGeneTss(String geneID, int tssUp, int tssDown) {
		GffDetailGene gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
		if (gffDetailGene == null) {
			return -1;
		}
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
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
		double[] siteInfo = mapReads.getRangeInfo(mapReads.getBinNum(), gffGeneIsoInfo.getRefIDlowcase(), tssStart, tssEnd, 0);
		if (siteInfo == null) {
//			System.out.println("stop");
			return -1;
		}
		return MathComput.mean(siteInfo);
	}
	/**
	 * 给定基因的symbol，返回该基因在tss附近区域的mapreads的平均数
	 * @param geneID 基因名字
	 * @param tssUp tss上游多少bp 负数在上游正数在下游
	 * @param tssDown tss下游多少bp 负数在上游正数在下游
	 */
	public double getGeneBodySum(String geneID) {
		GffDetailGene gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
		if (gffDetailGene == null) {
			return -1;
		}
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		int tssSite = gffGeneIsoInfo.getTSSsite();
		int tesSite = gffGeneIsoInfo.getTESsite();
		int tssStart = Math.min(tssSite, tesSite);
		int tssEnd = Math.max(tssSite, tesSite);
		double[] siteInfo = mapReads.getRangeInfo(mapReads.getBinNum(), gffGeneIsoInfo.getRefIDlowcase(), tssStart, tssEnd, 0);
		return MathComput.sum(siteInfo);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		GffGeneIsoInfo gffGeneIsoInfoOut = gffChrAbs.getGffHashGene().searchISO(geneID);
		return combineLoc(tmpResult, gffGeneIsoInfoOut.getLenUTR5() + 1);
	}

	/**
	 * 给定atg位点，获得该atg位点在合并后的序列中应该是第几个，从1开始
	 * 
	 * @param atgAASite
	 * @return
	 */
	public int getCombAtgSite(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfoOut = gffChrAbs.getGffHashGene().searchISO(geneID);
		int atgSite = gffGeneIsoInfoOut.getLenUTR5() + 1;
		// 除以3是指3个碱基
		return (int) Math.ceil((double) (atgSite - 1) / 3);
	}

	/**
	 * 给定基因名，获得该基因的atg位点在mRNA中应该是第几个位点，从1开始
	 * @param atgAASite
	 * @return
	 */
	public int getAtgSite(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfoOut = gffChrAbs.getGffHashGene().searchISO(geneID);
		return gffGeneIsoInfoOut.getLenUTR5() + 1;
		// 除以3是指3个碱基
	}
	/**
	 * 设定peak的bed文件，第一列为chrID，第二列为起点，第三列为终点， 返回去除peak后，每条染色体的bg情况
	 * @param peakFile
	 * @param firstlinls1
	 * @return
	 */
	public ArrayList<String[]> getBG(String peakFile, int firstlinls1) {
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
			mapReads.setNormalType(EnumMapNormalizeType.no_normalization);
			GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(string);
			if (gffGeneIsoInfo.getGeneType() != GeneType.mRNA
					&& gffGeneIsoInfo.getGeneType() != GeneType.mRNA_TE) {
				continue;
			}

			double[] tmp = mapReads.getRangeInfo(mapReads.getBinNum(), string, 0, 0, 0);
			mapReads.setNormalType(mapNormType);
			double[] tmp2 = mapReads.getRangeInfo(mapReads.getBinNum(), string, 0, 0, 0);
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
			mapInfo.setScore(weight);
			mapInfo.setDouble(tmp2);
			mapInfo.setFlagLoc(combatgSite);
			GeneID copedID = new GeneID(string, 0, false);
			mapInfo.setName(copedID.getSymbol());
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
		mapReads.getRangeLs(binNum, lsmapInfo, type);
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
		mapReads.getRange(binNum, mapInfo, type);
	}
	
	/**
	 * 经过标准化，和equations修正
	 * @param lsmapInfo
	 * @param thisInvNum  每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegion(MapInfo mapInfo, int thisInvNum, int type) {
		mapReads.getRange(mapInfo, thisInvNum, type);
	}

	/**
	 * 获得geneID以及相应权重，内部自动去冗余，保留权重高的那个，并且填充相应的reads
	 * 如果没有权重，就按照reads的密度进行排序
	 * 一般用于根据gene express 画heapmap图
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure 基因的哪个部分的结构 
	 * @param binNum 最后结果分成几块
	 */
	public ArrayList<MapInfo> readFileGeneMapInfo(String txtExcel,int colGeneID, int colScore, int rowStart, GeneStructure Structure, int binNum) {
		////////////////////     读 文 件   ////////////////////////////////////////////
		int[] columnID = null;
		if (colScore <= 0 || colScore == colGeneID) {
			 columnID = new int[]{colGeneID};
		}
		else {
			columnID = new int[]{colGeneID, colScore};
		}	
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		return getLsGeneMapInfo(lstmp, Structure, binNum);
	}
	/**
	 * 给定区域，自动获得基因
	 * 根据前面设定upBp和downBp
	 * @param lsMapInfos
	 * @param structure GffDetailGene.TSS等
	 * @param binNum 分成几块
	 * @return
	 */
	public ArrayList<MapInfo> getPeakCoveredGeneMapInfo(ArrayList<? extends MapInfo> lsMapInfos, GeneStructure structure, int binNum) {
		HashMap<GffDetailGene,Double>  hashGffDetailGenes = getPeakGeneStructure( lsMapInfos, structure);
		 ArrayList<MapInfo> lsResult = getMapInfoFromGffGene(hashGffDetailGenes, structure);
		 mapReads.getRangeLs(binNum, lsResult, 0);
		 return lsResult;
	}
	
	/**
	 * 获得geneID以及相应权重，内部自动去冗余，保留权重高的那个，并且填充相应的reads
	 * 如果没有权重，就按照reads的密度进行排序
	 * 一般用于根据gene express 画heapmap图
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure 基因的哪个部分的结构 
	 * @param binNum 最后结果分成几块
	 */
	public ArrayList<MapInfo> readGeneMapInfoAll(GeneStructure Structure, int binNum) {
		ArrayList<String> lsGeneID = gffChrAbs.getGffHashGene().getLsNameAll();
		ArrayList<String[]> lstmp = new ArrayList<String[]>();
		for (String string : lsGeneID) {
			lstmp.add(new String[]{string.split(SepSign.SEP_ID)[0]});
		}
		return getLsGeneMapInfo(lstmp, Structure, binNum);
	}
	/**
	 * 获得geneID以及相应权重，内部自动去冗余，保留权重高的那个，并且填充相应的reads
	 * 一般用于根据gene express 画heapmap图
	 * @param lsGeneValue string[2] 0:geneID 1:value 其中1 可以没有，那么就是string[1] 0:geneID
	 * @param rowStart
	 * @param Structure 基因的哪个部分的结构
	 * @param binNum 最后结果分成几块
	 * @return
	 */
	public ArrayList<MapInfo> getLsGeneMapInfo(ArrayList<String[]> lsGeneValue, GeneStructure Structure, int binNum) {
		//有权重的就使用这个hash
 		HashMap<GffDetailGene, Double> hashGene2Value = new HashMap<GffDetailGene, Double>();

		for (String[] strings : lsGeneValue) {
			GffDetailGene gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(strings[0]);
			if (gffDetailGene == null) {
				continue;
			}
			//have gene score, using the score as value, when the gene is same, add the score bigger one
			if (strings.length > 1) {
				if (hashGene2Value.containsKey(gffDetailGene)) {
					double score = Double.parseDouble(strings[1]);
					if (MapInfo.isMin2max()) {
						if (hashGene2Value.get(gffDetailGene) < score) {
							hashGene2Value.put(gffDetailGene, score);
						}
					} else {
						if (hashGene2Value.get(gffDetailGene) > score) {
							hashGene2Value.put(gffDetailGene, score);
						}
					}
				} else {
					hashGene2Value.put(gffDetailGene, Double.parseDouble(strings[1]));
				}
			}
			//didn't have score
			else {
				hashGene2Value.put(gffDetailGene, 0.0);
			}
		}
		ArrayList<MapInfo> lsMapInfoGene = getMapInfoFromGffGene(hashGene2Value, Structure);
		mapReads.getRangeLs(binNum, lsMapInfoGene, 0);
		if (lsGeneValue.get(0).length <= 1) {
			for (MapInfo mapInfo : lsMapInfoGene) {
				mapInfo.setScore(MathComput.mean(mapInfo.getDouble()));
			}
		}
		return lsMapInfoGene;
	}
	
	/**
	 * 根据前面设定upBp和downBp
	 * 给定一系列gffDetailGene，以及想要的部分，返回对应区域的LsMapInfo
	 * <b>注意里面没有填充reads的double[] value</b>
	 * @param mapGffDetailGenes
	 * @param structure
	 * @return
	 */
	private ArrayList<MapInfo> getMapInfoFromGffGene(HashMap<GffDetailGene, Double> mapGffDetailGenes, GeneStructure structure) {
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (Entry<GffDetailGene, Double> gffDetailValue : mapGffDetailGenes.entrySet()) {
			lsMapInfos.add(getStructureLoc(gffDetailValue.getKey(),gffDetailValue.getValue(), structure));
		}
		return lsMapInfos;
	}
	/**
	 * 给定peak的信息，chrID和起点终点，返回被peak覆盖到Tss的基因名和覆盖情况，用于做Tss图
	 * 自动去冗余基因
	 * @param lsPeakInfo mapInfo必须有 chrID 和 startLoc 和 endLoc 三项 
	 * @param structure GffDetailGene.TSS等
	 * @return
	 * 基因和权重的hash表
	 */
	private HashMap<GffDetailGene,Double> getPeakGeneStructure(ArrayList<? extends MapInfo> lsMapInfos, GeneStructure structure) {
		//存储最后的基因和权重
		HashMap<GffDetailGene,Double> hashGffDetailGenes = new HashMap<GffDetailGene,Double>();
		for (MapInfo mapInfo : lsMapInfos) {
			Set<GffDetailGene> setGffDetailGene = getPeakStructureGene( mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), structure );
			for (GffDetailGene gffDetailGene : setGffDetailGene) {
				if (hashGffDetailGenes.containsKey(gffDetailGene)) {
					if (MapInfo.isMin2max()) {
						if (mapInfo.getScore() < hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
						}
					} else {
						if (mapInfo.getScore() > hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
						}
					}
				} else {
					hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
				}
			}
		}
		return hashGffDetailGenes;
	}
	/**
	 * 给定坐标区域，返回该peak所覆盖的GffDetailGene
	 * @param tsstesRange 覆盖度，tss或tes的范围
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure GffDetailGene.TSS等
	 * @return
	 */
	private Set<GffDetailGene> getPeakStructureGene(String chrID, int startLoc, int endLoc, GeneStructure structure) {
		GffCodGeneDU gffCodGeneDU = gffChrAbs.getGffHashGene().searchLocation(chrID, startLoc, endLoc);
		if (gffCodGeneDU == null) {
			return new HashSet<GffDetailGene>();
		}
		gffCodGeneDU.cleanFilter();
		if (structure.equals(GeneStructure.TSS)) {
			gffCodGeneDU.setTss(plotRange);
			return gffCodGeneDU.getCoveredOverlapGffGene();
		}
		else if (structure.equals(GeneStructure.TES)) {
			gffCodGeneDU.setTes(plotRange);
			return gffCodGeneDU.getCoveredOverlapGffGene();
		}
		else {
			logger.error("暂时没有除Tss和Tes之外的基因结构");
			return null;
		}
	}
	/**
	 * 前面设定upBp和downBp
	 * 给定gffDetailGene，以及想要的部分，返回对应区域的MapInfo
	 * <b>注意里面没有填充reads的double[] value</b>
	 * @param gffDetailGene
	 * @param value 该基因所对应的权重
	 * @param structure GffDetailGene.TSS等
	 * @return
	 */
	private MapInfo getStructureLoc(GffDetailGene gffDetailGene, Double value,GeneStructure structure) {
		int plotUpstream = 0;
		int plotDownstream = 0;
		MapInfo mapInfoResult = null;
		String chrID = gffDetailGene.getRefID(); String geneName = gffDetailGene.getLongestSplitMrna().getName();
		if (gffDetailGene.isCis5to3()) {
			plotUpstream = Math.abs(plotRange[0]);
			plotDownstream = Math.abs(plotRange[1]);
		} else {
			plotUpstream = Math.abs(plotRange[1]);
			plotDownstream = Math.abs(plotRange[0]);
		}
		
		if (structure.equals(GeneStructure.TSS)) {
			int tss = gffDetailGene.getLongestSplitMrna().getTSSsite();
			mapInfoResult = new MapInfo(chrID, tss - plotUpstream, tss + plotDownstream, tss,0, geneName);
			mapInfoResult.setCis5to3(gffDetailGene.isCis5to3());
			mapInfoResult.setScore(value);
		}
		else if (structure.equals(GeneStructure.TES)) {
			int tes = gffDetailGene.getLongestSplitMrna().getTESsite();
			mapInfoResult = new MapInfo(chrID, tes - plotUpstream, tes + plotDownstream, tes, 0, geneName);
			mapInfoResult.setCis5to3(gffDetailGene.isCis5to3());
			mapInfoResult.setScore(value);
		}
		else {
			logger.error("还没添加该种类型的structure");
		}
		return mapInfoResult;
	}

}
