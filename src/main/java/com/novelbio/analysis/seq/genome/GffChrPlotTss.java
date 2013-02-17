package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
/**
 * setTsstesRange 和 setPlotTssTesRange这两个方法要第一时间设定<br>
 * 每次设定好后必须运行{@link #fillLsMapInfos()}
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
	
	/**
	 * 结果图片分割为1000份
	 * 小于0表示不进行分割，直接按照长度合并起来
	 */
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
	
	/** 设定需要提取，或不提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron
	 * null 就不分析
	 * 为实际数量
	 * -1为倒数第一个
	 * -2为倒数第二个
	 */
	ArrayList<Integer> lsExonIntronNumGetOrExclude;
	/** 对于lsExonIntronNumGetOrExclude选择get还是exclude，true为get，false为exclude */
	boolean getOrExclude = true;
	
	
	public GffChrPlotTss() { }
	
	public GffChrPlotTss(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
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
	 * null 就不分析
	 * 为实际数量
	 * -1为倒数第一个
	 * -2为倒数第二个
	 */
	public void setLsExonIntronNumGetOrExclude(
			ArrayList<Integer> lsExonIntronNumGetOrExclude) {
		this.lsExonIntronNumGetOrExclude = lsExonIntronNumGetOrExclude;
	}
	/** 提取的exon和intron，是叠在一起成为一体呢，还是头尾相连成为一体 */
	public void setPileupExonIntron(boolean pileupExonIntron) {
		this.pileupExonIntron = pileupExonIntron;
	}
	/** 对于lsExonIntronNumGetOrExclude选择get还是exclude，true为get，false为exclude */
	public void setGetOrExclude(boolean getOrExclude) {
		this.getOrExclude = getOrExclude;
	}
	
	/** 设定切割分数，默认为1000 
	 * 小于0表示不进行分割，直接按照长度合并起来
	 */
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
	
	/** 
	 * 设定本方法后<b>不需要</b>运行{@link #fillLsMapInfos()}<br>
	 * 用来做给定区域的图。mapinfo中设定坐标位点和value
	 * 这个和输入gene，2选1。谁先设定选谁
	 *  */
	public void setSiteRegion(ArrayList<MapInfo> lsMapInfos) {
		this.lsMapInfos = MapInfo.getCombLsMapInfoBigScore(lsMapInfos, 1000, true);
	}
	
	/** 
	 * 设定本方法后需要运行{@link #fillLsMapInfos()}<br>
	 * 设定为全基因组 */
	public void setGeneIDGenome() {
		lsGeneID2Value = Gene2Value.readGeneMapInfoAll(gffChrAbs);
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
	public void setGeneID2ValueLs(ArrayList<String[]> lsGeneValue) {
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
			Gene2Value gene2Value = new Gene2Value(gffDetailGene.getLongestSplitMrna());
			gene2Value.setValue(hashGene2Value.get(gffDetailGene));
			lsGeneID2Value.add(gene2Value);
		}
	}

	/**
	 * 设定本方法后需要运行{@link #fillLsMapInfos()}<br>
	 * <b>如果genestructure设定为tss或tes，那么务必首先设定tsstesRange</b><br>
	 * 给定区域，获得被该区域覆盖的基因然后再做图。mapinfo中设定坐标位点和value
	 * @param lsMapInfos 给定的区域
	 * @param geneStructure
	 */
	public void setSiteCoveredGene(ArrayList<MapInfo> lsMapInfos, GeneStructure geneStructure) {
		this.lsGeneID2Value = Gene2Value.getLsGene2Vale(tsstesRange, gffChrAbs, lsMapInfos, geneStructure);
	}
	
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
	 * 提取前务必设定{@link #fillLsMapInfos()}
	 * @return
	 */
	public ArrayList<double[]> getLsXYtsstes() {		
		ArrayList<double[]> lsResult = new ArrayList<double[]>();
		double[] yvalue = MapInfo.getCombLsMapInfo(lsMapInfos);
		List<Integer> lsCoverage = getLsGeneCoverage(lsMapInfos);
		
		double[] xvalue = getXvalue(yvalue.length);
		if (xvalue.length != yvalue.length) {
			logger.error("xvalue 和 yvalue 的长度不一致，请检查");
		}
		for (int i = 0; i < xvalue.length; i++) {
			int coverage = lsCoverage.get(i);
			double[] tmpResult= new double[2];
			tmpResult[0] = xvalue[i];
			tmpResult[1] = yvalue[i] / coverage;
			lsResult.add(tmpResult);
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
	
	/** 首先要设定好lsMapInfos */
	public PlotHeatMap plotHeatMap() {
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
	
	/** 将lsGeneID2Value中的信息填充到 lsMapInfos 中去 */
	public void fillLsMapInfos() {
		if (lsGeneID2Value == null || lsGeneID2Value.size() == 0) {
			return;
		}
		lsMapInfos = new ArrayList<MapInfo>();
		for (Gene2Value gene2Value : lsGeneID2Value) {
			gene2Value.setPlotTssTesRegion(plotTssTesRange);
			gene2Value.setExonIntronPileUp(pileupExonIntron);
			gene2Value.setGetNum(lsExonIntronNumGetOrExclude, getOrExclude);
			gene2Value.setSplitNum(splitNum);
			MapInfo mapInfo = gene2Value.getMapInfo(mapReads, geneStructure);
			if (mapInfo != null) {
				lsMapInfos.add(mapInfo);
			}
		}
		logger.debug("finished reading");
	}
	
	public void writeLsMapInfoToFile(String filename) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(filename, true);
		for (MapInfo mapInfo : lsMapInfos) {
			txtWrite.writefileln(mapInfo.getName());
			double[] value = mapInfo.getDouble();
			String[] info = new String[value.length];
			for (int i = 0; i < info.length; i++) {
				info[i] = value[i] + "";
			}
			txtWrite.writefileln(info);
		}
		txtWrite.close();
	}
	
	/**
	 * 根据输入的 lsMapInfos，获得指定分位点的值，用于heatmap设定最大值的颜色
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
	 * 根据给定的List-MapInfo lsMapInfos<br>
	 * 获得从第一个碱基开始，含有该位点的基因数量。也就是覆盖度<br>
	 * 譬如总共3个mapinfos<br>
	 * 1:4bp长   2:3bp长   3:1bp长<br>
	 * 那么第一位有3个基因，第二位有2个基因，第三位有2个基因，第四位有1个基因<br>
	 * 则返回 3-2-2-1<br>
	 * @return
	 */
	private List<Integer> getLsGeneCoverage(List<MapInfo> lsMapInfos) {
		List<double[]> lsDouble = new ArrayList<double[]>();
		for (MapInfo mapInfo : lsMapInfos) {
			lsDouble.add(mapInfo.getDouble());
		}
		return ArrayOperate.getLsCoverage(lsDouble);
	}
	
	/**
	 * 清空以下三个list<br>
	 * 1. 绘制图片的区域  ArrayList< MapInfo > lsMapInfos;<br>
	 * 2. 绘制图片的gene  ArrayList< Gene2Value > lsGeneID2Value;<br>
	 * 3. 设定需要提取，或不提取的exon或intron的个数，譬如杨红星要求仅分析第一位的intron 
		ArrayList< Integer > lsExonIntronNumGetOrExclude<br><br>
		<b>执行该方法后需重新设定 {@link  #setGeneIDGenome()} 等方法</b>
	 */
	public void clearCollectionInfo() {
		try { lsMapInfos.clear(); } catch (Exception e) { }
		try { lsGeneID2Value.clear(); } catch (Exception e) { }
		try { lsExonIntronNumGetOrExclude.clear(); } catch (Exception e) { }
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

