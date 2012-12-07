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
	/** �趨��Ҫ��ȡ��exon��intron�ĸ�����Ʃ�������Ҫ���������һλ��intron
	 * null �Ͳ�����
	 * Ϊʵ������
	 *  */
	ArrayList<Integer> lsExonIntronNum;
	
	
	public GffChrPlotTss() { }
	
	public GffChrPlotTss(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/**
	 * ��GffChrAbs����
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
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
		//���
		this.lsGeneID2Value = new ArrayList<Gene2Value>();
	}
	/** �趨Ϊȫ������ */
	public void setGeneIDGenome() {
		//���
		lsMapInfos = new ArrayList<MapInfo>();
		lsGeneID2Value = Gene2Value.readGeneMapInfoAll(gffChrAbs);
	}
	/**
	 * ����Ҫ��tssͼ�Ļ���list
	 * �ڲ�ȥ�ظ�
	 * �����MapInfo.isMin2max()��ǩȷ�������ظ�����ȡvalue��Ļ���С��
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param lsGeneValue string[2] 0:geneID 1:value ����1 ����û�У���ô����string[1] 0:geneID
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
			Gene2Value gene2Value = new Gene2Value(gffChrAbs);
			gene2Value.setGffGeneIsoInfo(gffDetailGene.getLongestSplitMrna());
			gene2Value.setValue(hashGene2Value.get(gffDetailGene));
			lsGeneID2Value.add(gene2Value);
		}
	}

	/** <b>���genestructure�趨Ϊtss��tes����ô��������趨tsstesRange</b><br>
	 * �������򣬻�ñ������򸲸ǵĻ���Ȼ������ͼ��mapinfo���趨����λ���value */
	public void setSiteCoveredGene(ArrayList<MapInfo> lsMapInfos, GeneStructure geneStructure) {
		this.lsGeneID2Value = Gene2Value.getLsGene2Vale(tsstesRange, gffChrAbs, lsMapInfos, geneStructure);
		//���
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
			logger.error("xvalue��yvalue�ĳ��Ȳ�һ�£�����");
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
				xResult[i] = (double)i/splitNum; 
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

class Gene2Value {
	private static final Logger logger = Logger.getLogger(Gene2Value.class);
	
	GffChrAbs gffChrAbs;
	
	GffGeneIsoInfo gffGeneIsoInfo;
	double value;
	
	/** tss��tes����չ����һ�㲸�鶯��Ϊ -5000��5000 */
	int[] plotTssTesRegion = new int[]{-5000, 5000};
	
	int splitNum = 1000;
	
	/** ��ȡ��exon��intron���ǵ���һ���Ϊһ���أ�����ͷβ������Ϊһ�� */
	boolean pileupExonIntron = false;
	/** �趨��Ҫ��ȡ��exon��intron�ĸ�����Ʃ�������Ҫ���������һλ��intron
	 * null �Ͳ�����
	 * Ϊʵ������
	 *  */
	ArrayList<Integer> lsExonIntronNum;
	
	
	public Gene2Value(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * @param plotTssTesRegion tss��tes����չ����һ�㲸�鶯��Ϊ -5000��5000
	 */
	public void setPlotTssTesRegion(int[] plotTssTesRegion) {
		this.plotTssTesRegion = plotTssTesRegion;
	}
	/**
	 * �����ȡ����exon����intron��������Ϊexon��intronÿ�����򶼲��ǵȳ��ģ�����Ҫ�趨���ֵķ���.
	 * �����tss��tes����Ҳ��Ҫ���ֳ�ָ���ķ���
	 * @param splitNumExonIntron Ĭ��Ϊ500��
	 */
	public void setSplitNum(int splitNum) {
		this.splitNum = splitNum;
	}
	/**
	 * ���ֻ֪��gene���֣�����������趨��
	 * �����ֱ���趨GffGeneIso
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
	
	/** ��ȡ��exon��intron���ǵ���һ���Ϊһ���أ�����ͷβ������Ϊһ�� */
	public void setExonIntronPileUp(boolean pileupExonIntron) {
		this.pileupExonIntron = pileupExonIntron;
	}
	/** �趨��Ҫ��ȡ��exon��intron�ĸ�����Ʃ�������Ҫ���������һλ��intron 
	 * �������ʵ��������Ʃ��1��ʾ��һ��exon��intron
	 * */
	public void setGetNum(ArrayList<Integer> lsExonIntronNum) {
		this.lsExonIntronNum = lsExonIntronNum;
		if (lsExonIntronNum != null) {
			Collections.sort(lsExonIntronNum);
		}
	}
	/**
	 * ���û�У�Ʃ��û��intron����ô�ͷ���һ��null
	 * �����tss��tes���ִ�0��ģ�splitNum�����1
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
	 * ��������������Ȩ�أ�����Gene2Value��list
	 * �����MapInfo.isMin2max()�ı�ǩ��ȷ�������ظ���ѡ���Ļ���С��
	 * @param tssTesRange
	 * @param gffChrAbs
	 * @param colSiteInfo
	 * @param geneStructure
	 * @return
	 */
	public static ArrayList<Gene2Value> getLsGene2Vale(int[] tssTesRange, GffChrAbs gffChrAbs, Collection<MapInfo> colSiteInfo, GeneStructure geneStructure) {
		//�洢���Ļ����Ȩ��
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
	 * �����������򣬷��ظ�peak�����ǵ�GffDetailGene
	 * @param tsstesRange ���Ƕȣ�tss��tes�ķ�Χ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure GffDetailGene.TSS�ȡ������gene body���򣬾ͷ�����������
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
	 * ��ȡȫ������
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