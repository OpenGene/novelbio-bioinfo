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
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
/**
 * setTsstesRange �� setPlotTssTesRange����������Ҫ��һʱ���趨
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

	/** ����ͼƬ������ */
	ArrayList<MapInfo> lsMapInfos;
	/** ����ͼƬ��gene */
	ArrayList<Gene2Value> lsGeneID2Value;
	
	/** ���ͼƬ�ָ�Ϊ1000�� */
	int splitNum = 1001;
	/**  tss��tes����չ��ͼ����Ĭ�ϲ��鶯��Ϊ -5000��5000 */
	int[] plotTssTesRange = new int[]{-5000, 5000};
	/** heatmap��ǳ��ɫ��ֵ */
	double heatmapMin = 0;
	/** heatmap������ɫ��ֵ */
	double heatmapMax = 20;
	Color heatmapColorMin = Color.white;
	Color heatmapColorMax = Color.blue;
	boolean heatmapSortS2M = true;
	
	/** ��ȡ��exon��intron���ǵ���һ���Ϊһ���أ�����ͷβ������Ϊһ�� */
	boolean pileupExonIntron = false;
	
	/** �趨��Ҫ��ȡ������ȡ��exon��intron�ĸ�����Ʃ�������Ҫ���������һλ��intron
	 * null �Ͳ�����
	 * Ϊʵ������
	 * -1Ϊ������һ��
	 * -2Ϊ�����ڶ���
	 */
	ArrayList<Integer> lsExonIntronNumGetOrExclude;
	/** ����lsExonIntronNumGetOrExcludeѡ��get����exclude��trueΪget��falseΪexclude */
	boolean getOrExclude = true;
	
	
	public GffChrPlotTss() { }
	
	public GffChrPlotTss(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/**
	 * ��GffChrAbs����
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
	/** �趨��Ҫ��ȡ������ȡ��exon��intron�ĸ�����Ʃ�������Ҫ���������һλ��intron
	 * null �Ͳ�����
	 * Ϊʵ������
	 * -1Ϊ������һ��
	 * -2Ϊ�����ڶ���
	 */
	public void setLsExonIntronNumGetOrExclude(
			ArrayList<Integer> lsExonIntronNumGetOrExclude) {
		this.lsExonIntronNumGetOrExclude = lsExonIntronNumGetOrExclude;
	}
	/** ����lsExonIntronNumGetOrExcludeѡ��get����exclude��trueΪget��falseΪexclude */
	public void setGetOrExclude(boolean getOrExclude) {
		this.getOrExclude = getOrExclude;
	}
	
	/** �趨�и������Ĭ��Ϊ1000 */
	public void setSplitNum(int splitNum) {
		//��Ϊ�Ǵ�0��ʼ����������Ҫ+1
		this.splitNum = splitNum + 1;
	}
	/**
	 * ��������趨���ڲ鿴peak�Ƿ񸲸�ĳ�������tssʱʹ��
	 * Ĭ�� -2000 2000
	 * @param tsstesRange
	 */
	public void setTsstesRange(int[] tsstesRange) {
		this.tsstesRange = tsstesRange;
	}
	/**
	 * @param plotTssTesRange tss��tes����չ����Ĭ���ǲ��鶯��Ϊ -5000��5000
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
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, booUniqueMapping, cis5to3);
	}
	
	/** �趨heatmap��ǳ��ɫ�Լ�������ɫ����Ӧ��ֵ */
	public void setHeatmapBoundValue(double heatmapMin, double heatmapMax) {
		this.heatmapMin = heatmapMin;
		this.heatmapMax = heatmapMax;
	}
	
	/** �趨heatmap��ǳ��ɫ�Լ�������ɫ */
	public void setHeatmapColor(Color heatmapColorMin, Color heatmapColorMax) {
		this.heatmapColorMin = heatmapColorMin;
		this.heatmapColorMax = heatmapColorMax;
	}
	/**
	 * heatmap�Ƿ���mapinfo��score��С��������
	 * @param heatmapSortS2M false �Ӵ�С����
	 */
	public void setHeatmapSortS2M(boolean heatmapSortS2M) {
		this.heatmapSortS2M = heatmapSortS2M;
	}
	
	/**
	 * ÿ������λȡ��,����趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @return
	 */
	public MapReads getMapReads() {
		return mapReads;
	}
	
	/** ���������������ͼ��mapinfo���趨����λ���value
	 * ���������gene��2ѡ1��˭���趨ѡ˭
	 *  */
	public void setSiteRegion(ArrayList<MapInfo> lsMapInfos) {
		this.lsMapInfos = MapInfo.getCombLsMapInfoBigScore(lsMapInfos, 1000, true);
	}
	
	/** �趨Ϊȫ������ */
	public void setGeneIDGenome() {
		lsGeneID2Value = Gene2Value.readGeneMapInfoAll(gffChrAbs);
	}
	
	/**
	 * ����Ҫ��tssͼ�Ļ���list
	 * �ڲ�ȥ�ظ�
	 * �����MapInfo.isMin2max()��ǩȷ�������ظ�����ȡvalue��Ļ���С��
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param lsGeneValue string[2] 
	 * 0:geneID 
	 * 1:value ����1 ����û�У���ô����string[1] 0:geneID
	 * @return
	 */
	public void setGeneID2ValueLs(ArrayList<String[]> lsGeneValue) {
		//���
		lsMapInfos = new ArrayList<MapInfo>();
		
		//��Ȩ�صľ�ʹ�����hash
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
			Gene2Value gene2Value = new Gene2Value();
			gene2Value.setGffGeneIsoInfo(gffDetailGene.getLongestSplitMrna());
			gene2Value.setValue(hashGene2Value.get(gffDetailGene));
			lsGeneID2Value.add(gene2Value);
		}
	}

	/** <b>���genestructure�趨Ϊtss��tes����ô��������趨tsstesRange</b><br>
	 * �������򣬻�ñ������򸲸ǵĻ���Ȼ������ͼ��mapinfo���趨����λ���value */
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
	
	public ArrayList<double[]> getLsXYtsstes() {
		setLsMapInfos();
		
		ArrayList<double[]> lsResult = new ArrayList<double[]>();
		double[] yvalue = MapInfo.getCombLsMapInfo(lsMapInfos);
		double[] xvalue = getXvalue();
		if (xvalue.length != yvalue.length) {
			logger.error("xvalue �� yvalue �ĳ��Ȳ�һ�£�����");
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
	 * �����趨��yvalueֵ�ͻ������ߵı߽磬�趨x��valueֵ
	 * @return
	 */
	private double[] getXvalue() {
		double[] xResult = null;
		xResult = new double[splitNum];
		//Gene2Value�������tss��tes�����1����Ϊ��0��
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
				xResult[i] = (double)i/(splitNum - 1); 
			}
		}
		return xResult;
	}
	
	/** ����Ҫ�趨��lsMapInfos */
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
	
	/** ��lsGeneID2Value�е���Ϣ��䵽lsMapInfos��ȥ */
	private void setLsMapInfos() {
		//TODO Ӧ�øĳ� ÿ���趨�µ�lsMapInfo��GeneID����GeneStructure��������
		if (lsMapInfos != null && lsMapInfos.size() > 0 && (lsGeneID2Value == null || lsGeneID2Value.size() == 0)) {
			mapReads.getRangeLs(this.splitNum, lsMapInfos, 0);
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
	
	/**
	 * ��������� lsMapInfos�����ָ����λ���ֵ
	 * @param lsMapInfos
	 * @param percentage ��Ϊ�㣬Ʃ��99��ʾ����99%��λ��
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
	 * �������list����Ϣ����Ҫ��
	 * ����ͼƬ������<br>
	   ArrayList< MapInfo > lsMapInfos;<br>
	����ͼƬ��gene<br>
	  ArrayList< Gene2Value > lsGeneID2Value;<br>
	 �趨��Ҫ��ȡ������ȡ��exon��intron�ĸ�����Ʃ�������Ҫ���������һλ��intron<br>
		ArrayList<Integer> lsExonIntronNumGetOrExclude<br><br>
		
		<b>������Ҫ�����趨 {@link  #setGeneIDGenome()} �ȷ���</b>
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
	 * @param mindata1 ��ͼ�ϵ�������ʾ������ɫ����Сֵ
	 * @param maxdata1 ��ͼ�ϵ�������ʾ������ɫ�����ֵ
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
	 * @param lsMapInfo  ������Ϣ
	 * @param structure ����ṹ��Ŀǰֻ�� GffDetailGene.TSS �� GffDetailGene.TES
	 * @param color
	 * @param small ��С
	 * @param big ���
	 * @param scale scale�η�������1��ϡ��߱�С��1��ϡ��ͱ��
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

