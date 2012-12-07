package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
/**
 * setTsstesRange 和 setPlotTssTesRange这两个方法要第一时间设定
 * @author zong0jie
 *
 */
public class GffChrPlotTss {
	private static final Logger logger = Logger.getLogger(GffChrMap.class);

	GffChrAbs gffChrAbs;
	
	int[] tsstesRange = new int[]{-2000, 2000};
	GeneStructure geneStructure = GeneStructure.TSS;

	MapReads mapReads;
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	
	/** 绘制图片的区域 */
	ArrayList<MapInfo> lsMapInfos;
	/** 绘制图片的gene */
	ArrayList<Gene2Value> lsGeneID2Value;
	
	/** 结果图片分割为1000份 */
	int splitNum = 1001;
	/**  tss或tes的扩展绘图区域，默认哺乳动物为 -5000到5000 */
	int[] plotTssTesRange = new int[]{-5000, 5000};
	/** heatmap最浅颜色的值 */
	double heatmapMin = 0;
	/** heatmap最深颜色的值 */
	double heatmapMax = 20;
	Color heatmapColorMin = Color.white;
	Color heatmapColorMax = Color.blue;
	boolean heatmapSortS2M = true;
	
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	boolean pileupExonIntron = false;
	/** 设定需要提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron
	 * null 就不分析
	 * 为实际数量
	 *  */
	ArrayList<Integer> lsExonIntronNum;
	
	
	public GffChrPlotTss() { }
	
	public GffChrPlotTss(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/**
	 * 将GffChrAbs导入
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/** 设定切割分数，默认为1000 */
	public void setSplitNum(int splitNum) {
		//因为是从0开始计数，所以要+1
		this.splitNum = splitNum + 1;
	}
	/**
	 * 务必最早设定，在查看peak是否覆盖某个基因的tss时使用
	 * 默认 -2000 2000
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
	
	/**
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param cis5to3 是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, booUniqueMapping, cis5to3);
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
	
	/** 用来做给定区域的图。mapinfo中设定坐标位点和value
	 * 这个和输入gene，2选1。谁先设定选谁
	 *  */
	public void setSiteRegion(ArrayList<MapInfo> lsMapInfos) {
		this.lsMapInfos = MapInfo.getCombLsMapInfoBigScore(lsMapInfos, 1000, true);
		//清空
		this.lsGeneID2Value = new ArrayList<Gene2Value>();
	}
	/** 设定为全基因组 */
	public void setGeneIDGenome() {
		//清空
		lsMapInfos = new ArrayList<MapInfo>();
		lsGeneID2Value = Gene2Value.readGeneMapInfoAll(gffChrAbs);
	}
	/**
	 * 给定要画tss图的基因list
	 * 内部去重复
	 * 会根据MapInfo.isMin2max()标签确定遇到重复项是取value大的还是小的
	 * 获得geneID以及相应权重，内部自动去冗余，保留权重高的那个，并且填充相应的reads
	 * 一般用于根据gene express 画heapmap图
	 * @param lsGeneValue string[2] 0:geneID 1:value 其中1 可以没有，那么就是string[1] 0:geneID
	 * @return
	 */
	public void setGeneID2ValueLs(ArrayList<String[]> lsGeneValue) {
		//清空
		lsMapInfos = new ArrayList<MapInfo>();
		
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
		
		lsGeneID2Value = new ArrayList<Gene2Value>();
		for (GffDetailGene gffDetailGene : hashGene2Value.keySet()) {
			Gene2Value gene2Value = new Gene2Value(gffChrAbs);
			gene2Value.setGffGeneIsoInfo(gffDetailGene.getLongestSplitMrna());
			gene2Value.setValue(hashGene2Value.get(gffDetailGene));
			lsGeneID2Value.add(gene2Value);
		}
	}

	/** <b>如果genestructure设定为tss或tes，那么务必首先设定tsstesRange</b><br>
	 * 给定区域，获得被该区域覆盖的基因然后再做图。mapinfo中设定坐标位点和value */
	public void setSiteCoveredGene(ArrayList<MapInfo> lsMapInfos, GeneStructure geneStructure) {
		this.lsGeneID2Value = Gene2Value.getLsGene2Vale(tsstesRange, gffChrAbs, lsMapInfos, geneStructure);
		//清空
		this.lsMapInfos = new ArrayList<MapInfo>();
	}
	
	public PlotScatter plotLine(DotStyle dotStyle) {
		ArrayList<double[]> lsXY = getLsXYtsstes();
		double[] yInfo = new double[lsXY.size()];
		for (int i = 0; i < yInfo.length; i++) {
			yInfo[i] = lsXY.get(i)[1];
		}
		double ymax = MathComput.max(yInfo);
		
		PlotScatter plotScatter = new PlotScatter();
		plotScatter.addXY(lsXY, dotStyle);
		double xLen = plotTssTesRange[1] - plotTssTesRange[0];
		plotScatter.setAxisX((double)plotTssTesRange[0] - xLen * 0.005, (double)plotTssTesRange[1] + xLen * 0.005);

		plotScatter.setAxisY(0, ymax * 1.1);
		
		double xmin = lsXY.get(0)[0]; double xmax = lsXY.get(lsXY.size() - 1)[0];
		double length = (xmax - xmin)/8;
		
		plotScatter.setTitleX("Site Near " + geneStructure.toString(), null, length);
		plotScatter.setTitleY("Normalized Reads Counts", null, 0);
		plotScatter.setTitle(geneStructure.toString() + " Reads Destribution", null);
		
		return plotScatter;
	}
	
	public ArrayList<double[]> getLsXYtsstes() {
		setLsMapInfos();
		
		ArrayList<double[]> lsResult = new ArrayList<double[]>();
		double[] yvalue = MapInfo.getCombLsMapInfo(lsMapInfos);
		double[] xvalue = getXvalue();
		if (xvalue.length != yvalue.length) {
			logger.error("xvalue和yvalue的长度不一致，请检查");
		}
		for (int i = 0; i < xvalue.length; i++) {
			double[] tmpResult= new double[2];
			tmpResult[0] = xvalue[i];
			tmpResult[1] = yvalue[i];
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	/**
	 * 根据设定的yvalue值和画出两边的边界，设定x的value值
	 * @return
	 */
	private double[] getXvalue() {
		double[] xResult = null;
		xResult = new double[splitNum];
		//Gene2Value里面对于tss和tes会加上1，因为有0点
		if (geneStructure == GeneStructure.TSS || geneStructure == GeneStructure.TES) {
			xResult[0] = plotTssTesRange[0];
			double intervalNum = (double)(plotTssTesRange[1] - plotTssTesRange[0] )/(splitNum - 1);
			for (int i = 1; i < xResult.length; i++) {
				xResult[i] = xResult[i-1] + intervalNum;
			}
			for (int i = 0; i < xResult.length; i++) {
				xResult[i] = (int)xResult[i];
			}
		} else {
			for (int i = 0; i < xResult.length; i++) {
				xResult[i] = (double)i/splitNum; 
			}
		}
		return xResult;
	}
	
	/** 首先要设定好lsMapInfos */
	public PlotHeatMap plotHeatMap() {
		setLsMapInfos();
		if (heatmapMax <= heatmapMin) {
			heatmapMax = getMaxData(lsMapInfos, 99);
		}
		
		MapInfo.sortPath(heatmapSortS2M);
		Collections.sort(lsMapInfos);
		
		Color[] gradientColors = new Color[] {heatmapColorMin, heatmapColorMax};
		Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);
		PlotHeatMap heatMap = new PlotHeatMap(lsMapInfos,  customGradient);
		heatMap.setRange(heatmapMin, heatmapMax);
		return heatMap;
	}
	
	/** 将lsGeneID2Value中的信息填充到lsMapInfos中去 */
	private void setLsMapInfos() {
		if (lsMapInfos.size() > 0 && lsGeneID2Value.size() == 0) {
			return;
		}
		for (Gene2Value gene2Value : lsGeneID2Value) {
			gene2Value.setPlotTssTesRegion(plotTssTesRange);
			gene2Value.setExonIntronPileUp(pileupExonIntron);
			gene2Value.setGetNum(lsExonIntronNum);
			gene2Value.setSplitNum(splitNum);
			MapInfo mapInfo = gene2Value.getMapInfo(mapReads, geneStructure);
			if (mapInfo != null) {
				lsMapInfos.add(mapInfo);
			}
		}
	}
	
	/**
	 * 根据输入的 lsMapInfos，获得指定分位点的值
	 * @param lsMapInfos
	 * @param percentage 分为点，譬如99表示最大的99%分位点
	 * @return
	 */
	private double getMaxData(List<MapInfo> lsMapInfos, int percentage) {
		ArrayList<Double> lsDouble = new ArrayList<Double>();
		for (MapInfo mapInfo : lsMapInfos) {
			double[] info = mapInfo.getDouble();
			for (double d : info) {
				lsDouble.add(d);
			}
		}
		return MathComput.median(lsDouble, percentage);
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
		Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);

		PlotHeatMap heatMap = new PlotHeatMap(lsMapInfoFinal, customGradient);
		heatMap.setRange(mindata1, maxdata1);
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
}

class Gene2Value {
	private static final Logger logger = Logger.getLogger(Gene2Value.class);
	
	GffChrAbs gffChrAbs;
	
	GffGeneIsoInfo gffGeneIsoInfo;
	double value;
	
	/** tss或tes的扩展区域，一般哺乳动物为 -5000到5000 */
	int[] plotTssTesRegion = new int[]{-5000, 5000};
	
	int splitNum = 1000;
	
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	boolean pileupExonIntron = false;
	/** 设定需要提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron
	 * null 就不分析
	 * 为实际数量
	 *  */
	ArrayList<Integer> lsExonIntronNum;
	
	
	public Gene2Value(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * @param plotTssTesRegion tss或tes的扩展区域，一般哺乳动物为 -5000到5000
	 */
	public void setPlotTssTesRegion(int[] plotTssTesRegion) {
		this.plotTssTesRegion = plotTssTesRegion;
	}
	/**
	 * 如果提取的是exon或者intron的区域，因为exon和intron每个基因都不是等长的，所以要设定划分的分数.
	 * 如果是tss和tes区域，也需要划分成指定的份数
	 * @param splitNumExonIntron 默认为500份
	 */
	public void setSplitNum(int splitNum) {
		this.splitNum = splitNum;
	}
	/**
	 * 如果只知道gene名字，就用这个来设定。
	 * 最好能直接设定GffGeneIso
	 * @param geneName
	 */
	public void setGeneName(String geneName) {
		gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(geneName);
	}
	
	public void setGffGeneIsoInfo(GffGeneIsoInfo gffGeneIsoInfo) {
		this.gffGeneIsoInfo = gffGeneIsoInfo;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	public void setExonIntronPileUp(boolean pileupExonIntron) {
		this.pileupExonIntron = pileupExonIntron;
	}
	/** 设定需要提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron 
	 * 输入的是实际数量，譬如1表示第一个exon或intron
	 * */
	public void setGetNum(ArrayList<Integer> lsExonIntronNum) {
		this.lsExonIntronNum = lsExonIntronNum;
		if (lsExonIntronNum != null) {
			Collections.sort(lsExonIntronNum);
		}
	}
	/**
	 * 如果没有，譬如没有intron，那么就返回一个null
	 * 如果是tss，tes这种带0点的，splitNum会加上1
	 * @param mapReads
	 * @param geneStructure
	 * @return
	 */
	public MapInfo getMapInfo(MapReads mapReads, GeneStructure geneStructure) {
		boolean sucess = true;
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(), value, gffGeneIsoInfo.getName());
		mapInfo.setCis5to3(gffGeneIsoInfo.isCis5to3());
		int upstream = plotTssTesRegion[0]; int downstream = plotTssTesRegion[1];
		if (!gffGeneIsoInfo.isCis5to3()) {
			upstream = -upstream; downstream = -downstream;
		}
		
		if (geneStructure == GeneStructure.TSS) {
			mapInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() + upstream, gffGeneIsoInfo.getTSSsite() + downstream);
			mapReads.getRange(splitNum, mapInfo, 0);
		} else if (geneStructure == GeneStructure.TES) {
			mapInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() + upstream, gffGeneIsoInfo.getTESsite() + downstream);
			mapReads.getRange(splitNum, mapInfo, 0);
		} else if (geneStructure == GeneStructure.EXON) {
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo);
		} else if (geneStructure == GeneStructure.INTRON) {
			if (gffGeneIsoInfo.getLsIntron().size() == 0) {
				return null;
			}
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getLsIntron());
		} else if (geneStructure == GeneStructure.ALLLENGTH) {
			mapInfo.setStartEndLoc(gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs());
			mapReads.getRange(splitNum, mapInfo, 0);
		} else if (geneStructure == GeneStructure.CDS) {
			if (!gffGeneIsoInfo.ismRNA()) {
				return null;
			}
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getIsoInfoCDS());
		} else if (geneStructure == GeneStructure.UTR3) {
			if (gffGeneIsoInfo.getLenUTR3() < 20) {
				return null;
			}
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getUTR3seq());
		} else if (geneStructure == GeneStructure.UTR5) {
			if (gffGeneIsoInfo.getLenUTR5() < 20) {
				return null;
			}
			sucess = setMapInfo(mapInfo, mapReads, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getUTR5seq());
		} else {
			return null;
		}
		
		if (!sucess || mapInfo.getDouble() == null) {
			return null;
		}
		return mapInfo;
	}
	
	private boolean setMapInfo(MapInfo mapInfo, MapReads mapReads, String chrID, List<? extends Alignment> lsExonInfos) {
		double[] result = new double[splitNum];
		List<Alignment> lsNew = new ArrayList<Alignment>();
		if (lsExonIntronNum == null || lsExonIntronNum.size() == 0) {
			for (Alignment alignment : lsExonInfos) {
				lsNew.add(alignment);
			}
		} else {
			for (Integer i : lsExonIntronNum) {
				i = i - 1;
				if (i < lsExonInfos.size()) {
					lsNew.add(lsExonInfos.get(i));
				}
			}
		}
		
		if (lsNew.size() == 0) {
			return false;
		}
		
		
		if (pileupExonIntron) {
			ArrayList<double[]> lsResult = new ArrayList<double[]>();
			for (Alignment alignment : lsNew) {
				double[] info = mapReads.getRangeInfo(chrID, alignment.getStartAbs(), alignment.getEndAbs(), 0);
				info = MathComput.mySpline(info, splitNum, 0, 0, 0);
				lsResult.add(info);
			}
			for (double[] ds : lsResult) {
				for (int i = 0; i < ds.length; i++) {
					result[i] = result[i] + ds[i];
				}
			}
		} else {
			double[] info = mapReads.getRangeInfo(chrID, lsNew);
			result = MathComput.mySpline(info, splitNum, 0, 0, 0);
		}
		mapInfo.setDouble(result);
		return true;
	}
	
	
	/**
	 * 根据输入的坐标和权重，返回Gene2Value的list
	 * 会根据MapInfo.isMin2max()的标签来确定遇到重复项选择大的还是小的
	 * @param tssTesRange
	 * @param gffChrAbs
	 * @param colSiteInfo
	 * @param geneStructure
	 * @return
	 */
	public static ArrayList<Gene2Value> getLsGene2Vale(int[] tssTesRange, GffChrAbs gffChrAbs, Collection<MapInfo> colSiteInfo, GeneStructure geneStructure) {
		//存储最后的基因和权重
		HashMap<GffDetailGene,Double> hashGffDetailGenes = new HashMap<GffDetailGene,Double>();
		for (MapInfo mapInfo : colSiteInfo) {
			Set<GffDetailGene> setGffDetailGene = getPeakStructureGene(tssTesRange, gffChrAbs, mapInfo, geneStructure );
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
		ArrayList<Gene2Value> lsGene2Values = new ArrayList<Gene2Value>();
		for (GffDetailGene gffDetailGene : hashGffDetailGenes.keySet()) {
			Gene2Value gene2Value = new Gene2Value(gffChrAbs);
			gene2Value.setGffGeneIsoInfo(gffDetailGene.getLongestSplitMrna());
			gene2Value.setValue(hashGffDetailGenes.get(gffDetailGene));
			lsGene2Values.add(gene2Value);
		}
		return lsGene2Values;
	}
	
	/**
	 * 给定坐标区域，返回该peak所覆盖的GffDetailGene
	 * @param tsstesRange 覆盖度，tss或tes的范围
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure GffDetailGene.TSS等。如果是gene body区域，就返回整个基因
	 * @return
	 */
	private static Set<GffDetailGene> getPeakStructureGene(int[] tssTesRange, GffChrAbs gffChrAbs, SiteInfo siteInfo, GeneStructure structure) {
		GffCodGeneDU gffCodGeneDU = gffChrAbs.getGffHashGene().searchLocation(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs());
		if (gffCodGeneDU == null) {
			return new HashSet<GffDetailGene>();
		}
		gffCodGeneDU.cleanFilter();
		if (structure.equals(GeneStructure.TSS)) {
			gffCodGeneDU.setTss(tssTesRange);
			return gffCodGeneDU.getCoveredGffGene();
		}
		else if (structure.equals(GeneStructure.TES)) {
			gffCodGeneDU.setTes(tssTesRange);
			return gffCodGeneDU.getCoveredGffGene();
		}
		else {
			return gffCodGeneDU.getCoveredGffGene();
		}
	}
	/**
	 * 读取全基因组
	 * @param gffChrAbs
	 * @return
	 */
	public static ArrayList<Gene2Value> readGeneMapInfoAll(GffChrAbs gffChrAbs) {
		ArrayList<Gene2Value> lsGene2Value = new ArrayList<Gene2Value>();
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getGffDetailAll()) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
			Gene2Value gene2Value = new Gene2Value(gffChrAbs);
			gene2Value.setGffGeneIsoInfo(gffGeneIsoInfo);
			lsGene2Value.add(gene2Value);
		}
		return lsGene2Value;
	}
}