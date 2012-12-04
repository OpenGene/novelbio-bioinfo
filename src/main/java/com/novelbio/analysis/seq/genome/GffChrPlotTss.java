package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.domain.geneanno.SpeciesFile.GFFtype;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;

import de.erichseifert.gral.util.GraphicsUtils;

public class GffChrPlotTss {
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	private static final Logger logger = Logger.getLogger(GffChrMap.class);
	
	/** 绘图区域，也用于tss和tes的范围 */
	int[] plotRange;
	MapReads mapReads;
	
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	
	/** 绘制图片的区域 */
	ArrayList<MapInfo> lsMapInfos;
	/** 绘制图片的gene */
	ArrayList<Gene2Value> lsGeneID2Value;
	
	/** heatmap最浅颜色的值 */
	double heatmapMin = 0;
	/** heatmap最深颜色的值 */
	double heatmapMax = 20;
	
	Color heatmapColorMin = Color.white;
	Color heatmapColorMax = Color.blue;
	
	boolean heatmapSortS2M = true;
	
	
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
	
	/**
	 * 如果是Tss或Tes
	 * 绘制正负多少bp的区域
	 * @param plotRange
	 */
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
	 * 读取gene和value信息，一般用来做tss的曲线图和heatmap图
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 */
	public void readGeneID2Value(String txtExcel, int colGeneID, int colScore, int rowStart) {
		int[] colNum;
		if (colScore > 0) {
			colNum = new int[]{colGeneID, colScore};
		} else {
			colNum = new int[]{colGeneID};
		}
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(txtExcel, colNum, rowStart, -1);
		lsGeneID2Value = new ArrayList<Gene2Value>();
		
		for (String[] strings : lsInfo) {
			Gene2Value gene2Value = new Gene2Value();
			gene2Value.setGeneName(strings[0]);
			try {
				gene2Value.setValue(Double.parseDouble(strings[1]));
			} catch (Exception e) {
				continue;
			}
			lsGeneID2Value.add(gene2Value);
		}
	}
	
	/**
	 * 读取坐标位点图，一般用来做给定区域的图
	 * @param txtExcel
	 * @param colChrID
	 * @param colStart
	 * @param colEnd
	 * @param colvalue 如果没有value值，本项就填负数
	 * @param rowStart
	 */
	public void readSiteRegion(String txtExcel, int colChrID, int colStart, int colEnd, int colvalue, int rowStart) {
		int[] colNum = new int[]{colChrID, colStart, colEnd, colvalue};
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(txtExcel, colNum, rowStart, -1);
		for (String[] strings : lsInfo) {
			MapInfo mapInfo = new MapInfo(strings[0], Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
			if (colvalue > 0) {
				mapInfo.setScore(Double.parseDouble(strings[3]));
			}
		}
	}
	
	/**
	 * 
	 * 读取坐标位点图，一般用来做给定区域的图
	 * @param txtExcel
	 * @param colChrID
	 * @param colSummit
	 * @param range summit左右两边的区域
	 * @param colvalue
	 * @param rowStart
	 */
	public void readSiteSummit(String txtExcel, int colChrID, int colSummit, int range, int colvalue, int rowStart) {
		int[] colNum = new int[]{colChrID, colSummit, colvalue};
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(txtExcel, colNum, rowStart, -1);
		for (String[] strings : lsInfo) {
			int summit = Integer.parseInt(strings[1]);			
			MapInfo mapInfo = new MapInfo(strings[0], summit - range, summit + range);
			if (colvalue > 0) {
				mapInfo.setScore(Double.parseDouble(strings[3]));
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private PlotHeatMap plotHeatMap() {
		if (heatmapMax <= heatmapMin) {
			heatmapMax = getMaxData(lsMapInfos, 99);
		}
		
		MapInfo.sortPath(heatmapSortS2M);
		Collections.sort(lsMapInfos);
		
		Color[] gradientColors = new Color[] { heatmapColorMin, heatmapColorMax};
		Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);
		PlotHeatMap heatMap = new PlotHeatMap(lsMapInfos,  customGradient);
		heatMap.setRange(heatmapMin, heatmapMax);
		return heatMap;
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
	private ArrayList<MapInfo> readFileGeneMapInfo(String txtExcel,int colGeneID, int colScore, int rowStart, GeneStructure Structure, int binNum) {
		////////////////////     读 文 件   ////////////////////////////////////////////
		int[] columnID = null;
		if (colScore <= 0 || colScore == colGeneID) {
			 columnID = new int[]{colGeneID};
		} else {
			columnID = new int[]{colGeneID, colScore};
		}	
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		return getLsGeneMapInfo(lstmp, Structure, binNum);
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
		double[] siteInfo = mapReads.getRangeInfo(mapReads.getBinNum(), gffGeneIsoInfo.getChrID(), tssStart, tssEnd, 0);
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
		double[] siteInfo = mapReads.getRangeInfo(mapReads.getBinNum(), gffGeneIsoInfo.getChrID(), tssStart, tssEnd, 0);
		return MathComput.sum(siteInfo);
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
			return gffCodGeneDU.getCoveredGffGene();
		}
		else if (structure.equals(GeneStructure.TES)) {
			gffCodGeneDU.setTes(plotRange);
			return gffCodGeneDU.getCoveredGffGene();
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
}

class Gene2Value {
	private static final Logger logger = Logger.getLogger(Gene2Value.class);
	
	GffChrAbs gffChrAbs;
	
	GffGeneIsoInfo gffGeneIsoInfo;
	double value;
	
	public Gene2Value(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	public void setGeneName(String geneName) {
		gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(geneName);
	}
	
	public void setGffGeneIsoInfo(GffGeneIsoInfo gffGeneIsoInfo) {
		this.gffGeneIsoInfo = gffGeneIsoInfo;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * 根据输入的坐标信息获得全体被覆盖到的gene2value
	 * @param colSiteInfo 有chrID，start，end 位置
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
}