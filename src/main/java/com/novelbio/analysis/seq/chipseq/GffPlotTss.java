package com.novelbio.analysis.seq.chipseq;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingoperate.EnumMapNormalizeType;
import com.novelbio.analysis.seq.genome.mappingoperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingoperate.RegionInfo;
import com.novelbio.analysis.seq.genome.mappingoperate.RegionInfo.RegionInfoComparator;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
/**
 * setTsstesRange 和 setPlotTssTesRange这两个方法要第一时间设定<br>
 * 每次设定好后必须运行{@link #fillLsMapInfos()}
 * @author zong0jie
 *
 */
@Deprecated
public class GffPlotTss {
	private static final Logger logger = Logger.getLogger(GffPlotTss.class);

	GffChrAbs gffChrAbs;
	
	int[] tsstesRange = new int[]{-5000, 5000};
	/** 仅绘制指定区间。譬如马红要求仅看0-500的图 */
	double[] xStartEnd;
	GeneStructure geneStructure = GeneStructure.TSS;

	MapReads mapReads;
	EnumMapNormalizeType mapNormType = EnumMapNormalizeType.allreads;

	/** 绘制图片的区域 */
	List<RegionInfo> lsRegions;
	/** 绘制图片的gene */
	List<Gene2Value> lsGeneID2Value;
	
	/**
	 * 结果图片分割为1000份
	 * 小于0表示不进行分割，直接按照长度合并起来
	 */
	int splitNum = 1000;
	/**  tss或tes的扩展绘图区域，默认哺乳动物为 -5000到5000 */
	int[] plotTssTesRange = new int[]{-5000, 5000};
	
	/** 给定iso和所需要的exonNum的区间，返回该iso是否满足条件
	 * @param iso
	 * @param exonNumRegion int[2]<br>
	 *  0：至少多少exon 小于0表示最小1个exon即可<br>
	 *  1：至多多少exon 小于0表示最多可以无限个exon<br>
	 *  <br>
	 *  int[]{2,3}表示本iso的exon数量必须在2-3之间<br>
	 *  int[]{-1,3}表示本iso的exon数量必须小于等于3个<br>
	 *  int[]{2,-1}表示本iso的exon数量必须大于等于2个<br>
	 * @return
	 */
	int[] exonNumRegion;
	
	//=================== heatmap参数 ================================
	/** heatmap最浅颜色的值 */
	double heatmapMin = 0;
	/** heatmap最深颜色的值 */
	double heatmapMax = 20;
	Color heatmapColorMin = Color.white;
	Color heatmapColorMax = Color.blue;
	boolean heatmapSortS2M = true;
	
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	boolean pileupExonIntron = false;
	//==============================================================
	/** 设定需要提取，或不提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron
	 * null 就不分析
	 * 为实际数量
	 * -1为倒数第一个
	 * -2为倒数第二个
	 */
	ArrayList<Integer> lsExonIntronNumGetOrExclude;
	/** 对于lsExonIntronNumGetOrExclude选择include还是exclude，true为include，false为exclude */
	boolean getOrExclude = true;
	
	String sampleName;
	
	public GffPlotTss() { }
	
	public GffPlotTss(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** 样本名 */
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	
	/** 仅绘制指定区间。譬如马红要求仅看0-500的图 */
	public void setxStartEnd(double[] xStartEnd) {
		this.xStartEnd = xStartEnd;
	}
	
	/**
	 * 将GffChrAbs导入
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (gffChrAbs == null || 
				(this.gffChrAbs != null 
				&& this.gffChrAbs.getSpecies() != null
				&& this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies()))) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	
	/** 设定需要提取，或不提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron
	 * @param lsExonIntronNumGetOrExclude null 就不分析
	 * 为实际数量
	 * -1为倒数第一个
	 * -2为倒数第二个
	 * @param isGetOrExclude 对于lsExonIntronNumGetOrExclude选择get还是exclude，true为get，false为exclude
	 */
	public void setLsExonIntronNumGetOrExclude(
			ArrayList<Integer> lsExonIntronNumGetOrExclude, boolean isGetOrExclude) {
		this.lsExonIntronNumGetOrExclude = lsExonIntronNumGetOrExclude;
		this.getOrExclude = isGetOrExclude;
	}
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	public void setPileupExonIntron(boolean pileupExonIntron) {
		this.pileupExonIntron = pileupExonIntron;
	}
	
	/** 设定切割分数，默认为1000 
	 * 小于0表示不进行分割，直接按照长度合并起来
	 */
	public void setSplitNum(int splitNum) {
		this.splitNum = splitNum;
	}
	/**
	 * 务必最早设定，在查看peak是否覆盖某个基因的tss时使用
	 * 默认 -5000 5000
	 * @param tsstesRange
	 */
	public void setTsstesRange(int[] tsstesRange) {
		this.tsstesRange = tsstesRange;
	}
	/**
	 * @param plotTssTesRange tss或tes的扩展区域，默认是哺乳动物为 -5000到5000
	 */
	public void setPlotTssTesRange(int[] plotTssTesRange) {
		this.plotTssTesRange = plotTssTesRange;
	}
	
	public void setGeneStructure(GeneStructure geneStructure) {
		this.geneStructure = geneStructure;
	}

	public void setMapReads(MapReads mapReads) {
		this.mapReads = mapReads;
	}
	
	/** 设定heatmap最浅颜色以及最深颜色所对应的值 */
	public void setHeatmapBoundValue(double heatmapMin, double heatmapMax) {
		this.heatmapMin = heatmapMin;
		this.heatmapMax = heatmapMax;
	}
	
	/** 设定heatmap最浅颜色以及最深颜色 */
	public void setHeatmapColor(Color heatmapColorMin, Color heatmapColorMax) {
		this.heatmapColorMin = heatmapColorMin;
		this.heatmapColorMax = heatmapColorMax;
	}
	/**
	 * heatmap是否按照mapinfo的score从小到大排序
	 * @param heatmapSortS2M false 从大到小排序
	 */
	public void setHeatmapSortS2M(boolean heatmapSortS2M) {
		this.heatmapSortS2M = heatmapSortS2M;
	}
	
	/**
	 * 每隔多少位取样,如果设定为1，则算法会变化，然后会很精确
	 * @return
	 */
	public MapReads getMapReads() {
		return mapReads;
	}
	
	/**
	 * <b>在最开始设定</b><br><br>
	 * 给定iso和所需要的exonNum的区间，返回该iso是否满足条件
	 * @param iso
	 * @param exonNumRegion int[2]<br>
	 *  0：至少多少exon 小于0表示最小1个exon即可<br>
	 *  1：至多多少exon 小于0表示最多可以无限个exon<br>
	 *  <br>
	 *  int[]{2,3}表示本iso的exon数量必须在2-3之间<br>
	 *  int[]{-1,3}表示本iso的exon数量必须小于等于3个<br>
	 *  int[]{2,-1}表示本iso的exon数量必须大于等于2个<br>
	 * @return
	 */
	public void setExonNumRegion(int[] exonNumRegion) {
	    this.exonNumRegion = exonNumRegion;
    }
	
	/** 
	 * 设定本方法后<b>不需要</b>运行{@link #fillLsMapInfos()}<br>
	 * 用来做给定区域的图。mapinfo中设定坐标位点和value
	 * 这个和输入gene，2选1。谁先设定选谁
	 *  */
	public void setSiteRegion(List<RegionInfo> lsMapInfos) {
		this.lsRegions = RegionInfo.getCombLsMapInfoBigScore(lsMapInfos, 1000, true);
	}
	
	/** 
	 * 设定本方法后需要运行{@link #fillLsMapInfos()}<br>
	 * 设定为全基因组 */
	public void setGeneIDGenome() {
		lsGeneID2Value = Gene2Value.readGeneMapInfoAll(gffChrAbs, exonNumRegion);
	}
	
	/**
	* 设定本方法后需要运行{@link #fillLsMapInfos()}<br>
	 * 给定要画tss图的基因list
	 * 内部去重复
	 * 会根据MapInfo.isMin2max()标签确定遇到重复项是取value大的还是小的
	 * 获得geneID以及相应权重，内部自动去冗余，保留权重高的那个，并且填充相应的reads
	 * 一般用于根据gene express 画heapmap图
	 * @param lsGeneValue string[2] <br>
	 * 0:geneID <br>
	 * 1:value 其中1 可以没有，那么就是string[1] 0:geneID<br>
	 * @return
	 */
	public void setGeneID2ValueLs(List<String[]> lsGeneValue) {
		lsGeneID2Value = Gene2Value.getLsGene2Vale(gffChrAbs, lsGeneValue, exonNumRegion);
	}

	/**
	 * <b>首先设定GeneStructure</b><br><br>
	 * 设定本方法后需要运行{@link #fillLsMapInfos()}<br>
	 * <b>如果genestructure设定为tss或tes，那么务必首先设定tsstesRange</b><br>
	 * 给定区域，获得被该区域覆盖的基因然后再做图。mapinfo中设定坐标位点和value
	 * @param lsPeakInfo 给定的区域
	 * @param geneStructure
	 */
	public void setSiteCoveredGene(List<RegionInfo> lsPeakInfo) {
		this.lsGeneID2Value = Gene2Value.getLsGene2Vale(tsstesRange, gffChrAbs, lsPeakInfo, geneStructure, exonNumRegion);
	}
	
	/**
	 * @param dotStyle
	 * @param xStartEnd 仅绘制指定区间。譬如马红要求仅看0-500的图
	 * null 表示没有这个限制
	 * @return
	 */
	public PlotScatter plotLine(DotStyle dotStyle) {
		ArrayList<double[]> lsXY = getLsXYtsstes();
		double[] yInfo = new double[lsXY.size()];
		for (int i = 0; i < yInfo.length; i++) {
			yInfo[i] = lsXY.get(i)[1];
		}
		double ymax = MathComput.max(yInfo);
		double xStart = lsXY.get(0)[0]; double xEnd = lsXY.get(lsXY.size() - 1)[0];

		PlotScatter plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
		plotScatter.addXY(lsXY, dotStyle);
		double xLen = xEnd - xStart;
		plotScatter.setAxisX(xStart - xLen * 0.001, (double)xEnd + xLen * 0.001);

		plotScatter.setAxisY(0, ymax * 1.1);
		
		double xmin = lsXY.get(0)[0]; double xmax = lsXY.get(lsXY.size() - 1)[0];
		double length = (xmax - xmin)/8;
		
		plotScatter.setTitleX("Site Near " + geneStructure.toString(), null, length);
		plotScatter.setTitleY("Normalized Reads Counts", null, 0);
		plotScatter.setTitle(geneStructure.toString() + " Reads Destribution", null);
		
		return plotScatter;
	}
	
	/**
	 * @param xStartEnd 仅绘制指定区间。譬如马红要求仅看0-500的图
	 * @return jfreechart 可以使用的对象
	 */
	public XYSeries getXySeries() {
		ArrayList<double[]> lsXY = getLsXYtsstes();
		XYSeries xySeries = new XYSeries(sampleName);
		for (double[] ds : lsXY) {
			xySeries.add(ds[0], ds[1]);
		}
		return xySeries;
	}
	/**
	 * 提取前务必设定{@link #fillLsMapInfos()}
	 * @return
	 */
	public ArrayList<double[]> getLsXYtsstes() {
		ArrayList<double[]> lsResult = new ArrayList<double[]>();
		double[] yvalue = RegionInfo.getCombLsMapInfo(lsRegions);
		//如果lsRegion是不等长的，coverage就是每个位点region的数量
		List<Integer> lsCoverage = getLsGeneCoverage(lsRegions);
		
		double[] xvalue = getXvalue(yvalue.length);
		
		double yStartValue = Double.MAX_VALUE;
		double yEndValue = Double.MAX_VALUE;
		for (int i = 0; i < xvalue.length; i++) {
			int coverage = lsCoverage.get(i);
			if (xStartEnd != null) {
				if (xvalue[i] < xStartEnd[0]) {
					yStartValue = yvalue[i] / coverage;
					continue;
				} else if (xvalue[i] > xStartEnd[1]) {
					yEndValue = yvalue[i] / coverage;
					break;
				}
			}
			
			double[] tmpResult= new double[2];
			tmpResult[0] = xvalue[i];
			tmpResult[1] = yvalue[i] / coverage;
			lsResult.add(tmpResult);
		}
		if (xStartEnd != null) {
			/** 将结果的头尾设定为输入的头尾 */
			if (lsResult.get(0)[0] > xStartEnd[0]) {
				double[] start = new double[]{xStartEnd[0], yStartValue};
				lsResult.add(0, start);
			}
			if (lsResult.get(lsResult.size() - 1)[0] < xStartEnd[1]) {
				double[] end = new double[]{xStartEnd[1], yEndValue};
				lsResult.add(end);
			}
		}

		return lsResult;
	}
	/**
	 * 根据设定的yvalue值和画出两边的边界，设定x的value值
	 * 如果是不同长度的exon等不标准化，根据马红的要求直接合并
	 * @return
	 */
	private double[] getXvalue(int length) {
		double[] xResult = new double[length];
		//Gene2Value里面对于tss和tes会加上1，因为有0点
		if (geneStructure == GeneStructure.TSS || geneStructure == GeneStructure.TES) {
			xResult[0] = plotTssTesRange[0];
			double intervalNum = (double)(plotTssTesRange[1] - plotTssTesRange[0] )/(length - 1);
			for (int i = 1; i < xResult.length; i++) {
				xResult[i] = xResult[i-1] + intervalNum;
			}
			for (int i = 0; i < xResult.length; i++) {
				xResult[i] = (int)xResult[i];
			}
		} else if(splitNum > 0) {
			//不是 tss，则用小数形式来表示x轴，如 0 0.1 0.2 0.3....1
			for (int i = 0; i < xResult.length; i++) {
				xResult[i] = (double)i/(length - 1); 
			}
		} else {
			for (int i = 0; i < xResult.length; i++) {
				xResult[i] = mapReads.getBinNum() * i;
			}
		}
		return xResult;
	}
	
	/** 将lsGeneID2Value中的信息填充到 lsMapInfos 中去 */
	public void fillLsMapInfos() {
		if (lsGeneID2Value == null || lsGeneID2Value.size() == 0) {
			return;
		}
		lsRegions = new ArrayList<RegionInfo>();
		for (Gene2Value gene2Value : lsGeneID2Value) {
			gene2Value.setPlotTssTesRegion(plotTssTesRange);
			gene2Value.setExonIntronPileUp(pileupExonIntron);
			gene2Value.setGetNum(lsExonIntronNumGetOrExclude, getOrExclude);
			gene2Value.setSplitNum(splitNum);
			RegionInfo mapInfo = gene2Value.getRegionInfo(mapReads, geneStructure);
			if (mapInfo != null) {
				lsRegions.add(mapInfo);
			}
		}
		
		RegionInfoComparator regionInfoComparator = new RegionInfoComparator();
		regionInfoComparator.setMin2max(heatmapSortS2M);
		regionInfoComparator.setCompareType(RegionInfoComparator.COMPARE_SCORE);
		Collections.sort(lsRegions, regionInfoComparator);
		
		logger.debug("finished reading");
	}
	
	/** 将每个基因的覆盖信息写入文本，每行一个基因 */
	public void writeLsReadsInfoToFile(String filename) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(filename, true);
		for (RegionInfo regionInfo : lsRegions) {
			txtWrite.writefileln(regionInfo.toString());
		}
		txtWrite.close();
	}
	
	/** 将总的覆盖信息写入文本，第一列为坐标，第二列为具体的值 */
	public void writeReadsPileupToFile(String filename) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(filename, true);
		txtWrite.writefileln(new String[]{"Distance to " + geneStructure, "Normalized Reads"});
		List<double[]> lsInfo = getLsXYtsstes();
		for (double[] ds : lsInfo) {
			txtWrite.writefileln(ds[0] + "\t" + ds[1]);
		}
		txtWrite.close();
	}
	
	/** 首先要设定好lsMapInfos */
	public PlotHeatMap plotHeatMap() {
		if (heatmapMax <= heatmapMin) {
			heatmapMax = getMaxData(lsRegions, 99);
		}
		Color[] gradientColors = new Color[] {heatmapColorMin, heatmapColorMax};
		Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);
		PlotHeatMap heatMap = new PlotHeatMap(lsRegions,  customGradient);
		heatMap.setRange(heatmapMin, heatmapMax);
		return heatMap;
	}
	
	/**
	 * 根据输入的 lsMapInfos，获得指定分位点的值，用于heatmap设定最大值的颜色
	 * @param lsMapInfos
	 * @param percentage 分为点，譬如99表示最大的99%分位点
	 * @return
	 */
	private double getMaxData(List<RegionInfo> lsMapInfos, int percentage) {
		ArrayList<Double> lsDouble = new ArrayList<Double>();
		for (RegionInfo mapInfo : lsMapInfos) {
			double[] info = mapInfo.getDouble();
			for (double d : info) {
				lsDouble.add(d);
			}
		}
		return MathComput.median(lsDouble, percentage);
	}
	
	/**
	 * 根据给定的List-MapInfo lsRegionInfos<br>
	 * 获得从第一个碱基开始，含有该位点的Region数量。也就是覆盖度<br>
	 * 譬如总共3个mapinfos<br>
	 * 1:4bp长   2:3bp长   3:1bp长<br>
	 * 那么第一位有3个Region，第二位有2个Region，第三位有2个Region，第四位有1个Region<br>
	 * 则返回 3-2-2-1<br>
	 * @return
	 */
	private List<Integer> getLsGeneCoverage(List<RegionInfo> lsRegionInfos) {
		List<double[]> lsDouble = new ArrayList<double[]>();
		for (RegionInfo mapInfo : lsRegionInfos) {
			lsDouble.add(mapInfo.getDouble());
		}
		return ArrayOperate.getLsCoverage(lsDouble);
	}
	
	/**
	 * <b>绘图前调用</b><br>
	 * 清空以下三个list<br>
	 * 1. 绘制图片的区域  ArrayList< MapInfo > lsMapInfos;<br>
	 * 2. 绘制图片的gene  ArrayList< Gene2Value > lsGeneID2Value;<br>
	 * 3. 设定需要提取，或不提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron 
		ArrayList< Integer > lsExonIntronNumGetOrExclude<br><br>
		<b>执行该方法后需重新设定 {@link  #setGeneIDGenome()} 等方法</b>
	 */
	public void clearCollectionInfo() {
		try { lsRegions.clear(); } catch (Exception e) { }
		try { lsGeneID2Value.clear(); } catch (Exception e) { }
		try { lsExonIntronNumGetOrExclude.clear(); } catch (Exception e) { }
	}
	
	public void readLsRegionInfoFromFile(String regionFile) {
		List<RegionInfo> lsRegion = new ArrayList<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(regionFile);
		for (String content : txtRead.readlines()) {
			RegionInfo regionInfo = new RegionInfo();
			regionInfo.readFromStr(content);
			lsRegion.add(regionInfo);
		}
		txtRead.close();
		this.lsRegions = lsRegion;
	}
	
	/**
	 * @param lsMapInfo1
	 * @param lsMapInfo2
	 * @param outFile
	 * @param mindata1 热图上的所能显示最深颜色的最小值
	 * @param maxdata1 热图上的所能显示最深颜色的最大值
	 */
	public static void plotHeatMapMinus(ArrayList<RegionInfo> lsMapInfo1,
			ArrayList<RegionInfo> lsMapInfo2, String outFile, double mindata, double maxdata) {
		ArrayList<RegionInfo> lsMapInfoFinal = RegionInfo.minusListMapInfo(
				lsMapInfo1, lsMapInfo2);
		Color colorgreen = new Color(0, 255, 0, 255);
		Color colorwhite = new Color(255, 255, 255, 255);
		Color colorred = new Color(255, 0, 0, 255);

		Color[] gradientColors = new Color[] { colorgreen, colorwhite, colorred };
		Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);

		PlotHeatMap heatMap = new PlotHeatMap(lsMapInfoFinal, customGradient);
		heatMap.setRange(mindata, maxdata);
		heatMap.saveToFile(outFile, 6000, 1000);
	}
	
	/**
	 * @param lsMapInfo  基因信息
	 * @param structure 基因结构，目前只有 GffDetailGene.TSS 和 GffDetailGene.TES
	 * @param color
	 * @param small 最小
	 * @param big 最大
	 * @param scale scale次方，大于1则稀疏高表达，小于1则稀疏低表达
	 * @param outFile
	 */
	public static void plotHeatMap2(ArrayList<RegionInfo> lsMapInfo,
			ArrayList<RegionInfo> lsMapInfo2, String outFile, double mindata1,
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
	
	public void drawImage(String savePath, int[] width2Height) {
		XYSeries xySeries = getXySeries();
		Map<XYSeries, Color> mapXy2Color = new HashMap<>();
		mapXy2Color.put(xySeries, Color.blue);
		drawImage(mapXy2Color, geneStructure, savePath, width2Height);
	}
	
	public static void drawImageFromFile(Map<String, Color> mapFile2Color, GeneStructure geneStructure, String savePath, int[] width2Height) {
		Map<XYSeries, Color> mapData2Color = new HashMap<>();
		for (String tssFile : mapFile2Color.keySet()) {
			XYSeries xySeries = new XYSeries(FileOperate.getFileNameSep(tssFile)[0].replace("_" + geneStructure, ""));
			TxtReadandWrite txtRead = new TxtReadandWrite(tssFile);
			for (String content : txtRead.readlines(2)) {
				String[] ss = content.split("\t");
				xySeries.add(Double.parseDouble(ss[0]), Double.parseDouble(ss[1]));
			}
			txtRead.close();
			mapData2Color.put(xySeries, mapFile2Color.get(tssFile));
		}
		drawImage(mapData2Color, geneStructure, savePath, width2Height);
	}
	
	/**
	 * 给定一系列坐标，来画图
	 * @param xySeriesCollection
	 */
	public static void drawImage(Map<XYSeries, Color> mapData2Color, GeneStructure geneStructure, String savePath, int[] width2Height) {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		XYSplineRenderer renderer = new XYSplineRenderer();
		
		int i = 0;
		double maxY = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
		for (XYSeries xySeries : mapData2Color.keySet()) {
			xySeriesCollection.addSeries(xySeries);
			renderer.setSeriesPaint(i++, mapData2Color.get(xySeries)); //设置0号数据的颜色。如果一个图中绘制多条曲线，可以手工设置颜色
			maxY = Math.max(maxY, xySeries.getMaxY());
			minX = Math.min(minX, xySeries.getMinX());
			maxX = Math.max(maxX, xySeries.getMaxX());
		}
		
		renderer.setBaseShapesVisible(false); //绘制的线条上不显示图例，如果显示的话，会使图片变得很丑陋
		renderer.setPrecision(1); //设置精度，大概就是在源数据两个点之间插入5个点以拟合出一条平滑曲线
		renderer.setSeriesShapesVisible(0, false);//设置三条线是否显示 点 的形状
		//create plot
		NumberAxis xAxis = new NumberAxis("Distance to " + geneStructure);		
		xAxis.setAutoRangeIncludesZero(false);
		xAxis.setLowerBound(minX);
		xAxis.setUpperBound(maxX);
		
		NumberAxis yAxis = new NumberAxis("Normalized Reads");
    		yAxis.setAutoRangeIncludesZero(false);
    		yAxis.setUpperMargin(0.35);
    		//设置最低的一个 Item 与图片底端的距离
    		yAxis.setLowerMargin(0.45);
    		//设置Y轴的最小值
    		yAxis.setLowerBound(0);
    		//设置Y轴的最大值
    		yAxis.setUpperBound(maxY * 1.2);
    		
    		XYPlot plot = new XYPlot(xySeriesCollection, xAxis, yAxis, renderer);
    		plot.setBackgroundPaint(Color.white);
    		plot.setDomainGridlinePaint(Color.white);
    		plot.setRangeGridlinePaint(Color.white);
    		plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4)); //设置坐标轴与绘图区域的距离
//    		ValueAxis xAxis = plot.getDomainAxis();
//    		
//    		ValueAxis yAxis = plot.getRangeAxis();
    		//设置最高的一个 Item 与图片顶端的距离

    		JFreeChart chart = new JFreeChart(geneStructure + " Plot", //标题
    				JFreeChart.DEFAULT_TITLE_FONT, //标题的字体，这样就可以解决中文乱码的问题
    				plot,
    				true //不在图片底部显示图例
    				);
    		ImageUtils.saveBufferedImage(chart.createBufferedImage(width2Height[0], width2Height[1]), savePath);
	}
}

