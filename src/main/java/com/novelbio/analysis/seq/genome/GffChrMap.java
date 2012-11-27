package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashCG;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneNCBI;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
import com.novelbio.base.plot.java.HeatChart;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.domain.geneanno.SpeciesFile.GFFtype;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

import de.erichseifert.gral.util.GraphicsUtils;

/**
 * ������������򣬻�������ͳ��ͼ
 * 
 * @author zong0jie
 * 
 */
public class GffChrMap {
	public static void main(String[] args) {
		GffChrAbs gffChrAbs = new GffChrAbs();
		Species species = new Species(10090);
		species.setGfftype(GFFtype.GFF_UCSC);
		MapReads mapReads = new MapReads();
		mapReads.setBedSeq("/media/winF/NBC/Project/ChIP-Seq_LXW/mappingc007_filtered_extend_sorted.bed");
		mapReads.setMapChrID2Len(species.getMapChromInfo());
		mapReads.run();
		gffChrAbs.setSpecies(species);
		GffChrMap gffChrMap = new GffChrMap(gffChrAbs);
		String resultFile = "/media/winF/NBC/Project/ChIP-Seq_LXW/tss";
		gffChrMap.plotTssAllGene(1000, resultFile, GeneStructure.TSS);
	}
	
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	Logger logger = Logger.getLogger(GffChrMap.class);
	String fileName = "";
	int maxresolution = 10000;
	/** ��ͼ����Ҳ����tss��tes�ķ�Χ */
	int[] plotRange;
	MapReads mapReads;
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	
	
	public GffChrMap() {
	}
	
	public GffChrMap(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * ��GffChrAbs����
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
	 * ÿ������λȡ��,����趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @return
	 */
	public int getThisInv() {
		return mapReads.getBinNum();
	}
	/**
	 * ����Ⱦɫ������ͳ��ÿ��Ⱦɫ������λ������ÿ��λ������ string[4] 0: chrID 1: readsNum 2: readsPipNum
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
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, booUniqueMapping, cis5to3);
	}
	
	/**
	 * ��Ҫ����ȫ��������ͼ�ģ����Խ�������ջ�������еߵ�
	 * @param geneStructure
	 * @param gffDetailGene
	 * @param num ����ڼ�����Ʃ�������뿴��һ���ں��ӻ��ߵ�һ�������� С�ڵ���0��ʾ��ȫ��
	 * @return
	 */
	public void setFilterChrDistInfo(GeneStructure geneStructure, int num) {
		if (geneStructure == GeneStructure.ALL) {
			mapReads.setMapChrID2LsAlignments(null);
			return;
		}
		
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();
		HashMap<String, List<? extends Alignment>> mapChrID2LsAlignment = new HashMap<String, List<? extends Alignment>>();
		for (String chrID : gffHashGene.getMapChrID2LsGff().keySet()) {
			ArrayList<SiteInfo> lsAlignment = new ArrayList<SiteInfo>();
			ListGff listGff = gffHashGene.getMapChrID2LsGff().get(chrID.toLowerCase());
			for (GffDetailGene gffDetailGene : listGff) {
				lsAlignment.addAll(getGeneStructureRangeForChrPlot(geneStructure, gffDetailGene, num));
			}
			SiteInfo.setCompareType(SiteInfo.COMPARE_LOCSITE);
			Collections.sort(lsAlignment);
			mapChrID2LsAlignment.put(chrID.toLowerCase(), lsAlignment);
		}
		mapReads.setMapChrID2LsAlignments(mapChrID2LsAlignment);
	}
	/**
	 * ��Ҫ����ȫ��������ͼ�ģ����Խ�������ջ�������еߵ�
	 * @param geneStructure
	 * @param gffDetailGene
	 * @param num ����ڼ�����Ʃ�������뿴��һ���ں��ӻ��ߵ�һ�������� С�ڵ���0��ʾ��ȫ��
	 * @return
	 */
	private ArrayList<SiteInfo> getGeneStructureRangeForChrPlot(GeneStructure geneStructure, GffDetailGene gffDetailGene, int num) {
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		ArrayList<SiteInfo> lsResult = new ArrayList<SiteInfo>();
		
		if (geneStructure == GeneStructure.TSS) {
			SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
			if (gffGeneIsoInfo.isCis5to3()) {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() + plotRange[0], gffGeneIsoInfo.getTSSsite() + plotRange[1]);
			} else {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() - plotRange[1], gffGeneIsoInfo.getTSSsite() - plotRange[0]);
			}
			lsResult.add(siteInfo);
		}
		
		else if (geneStructure == GeneStructure.TES) {
			SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
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
					SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
					lsResult.add(siteInfo);
				}
			} else {
				if (gffGeneIsoInfo.size() > num) {
					SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(gffGeneIsoInfo.get(num - 1).getStartAbs(), gffGeneIsoInfo.get(num - 1).getEndAbs());
					lsResult.add(siteInfo);
				}
			}
		}
		
		else if (geneStructure == GeneStructure.INTRON) {
			if (num <= 0) {
				for (ExonInfo exonInfo : gffGeneIsoInfo.getLsIntron()) {
					SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
					lsResult.add(siteInfo);
				}
			} else {
				ArrayList<ExonInfo> lsIntron = gffGeneIsoInfo.getLsIntron();
				if (lsIntron.size() >= num) {
					SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(lsIntron.get(num - 1).getStartAbs(), lsIntron.get(num - 1).getEndAbs());
					lsResult.add(siteInfo);
				}
			}
			
		} else if (geneStructure == GeneStructure.UTR5) {
			for (ExonInfo exonInfo : gffGeneIsoInfo.getUTR5seq()) {
				SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
				siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
				lsResult.add(siteInfo);
			}
		} else if (geneStructure == GeneStructure.UTR3) {
			for (ExonInfo exonInfo : gffGeneIsoInfo.getUTR3seq()) {
				SiteInfo siteInfo = new SiteInfo(gffDetailGene.getRefID());
				siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
				lsResult.add(siteInfo);
			}
		}
		return lsResult;
	}
	
	/**
	 * ����ĳ��Ⱦɫ���ϵ�reads����������ܶ�ͼ��ֻ�Ǽ򵥵ļ���reads��һ��Ⱦɫ���ϵ���� ��Ҫ����RefSeqʱ��һ�������ϵ�reads���
	 * @param chrID
	 * @param thisInvNum ÿ�����伸bp
	 * @parm type ȡ������ 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return û�еĻ��ͷ���null
	 */
	public double[] getChrInfo(String chrID, int thisInvNum, int type) {
		double[] tmpResult = mapReads.getRangeInfo(thisInvNum, chrID, 0, 0, type);
		return tmpResult;
	}
	/**
	 * ��������Ⱦɫ�����ܶ�ͼ
	 * ��java��
	 * @param outPathPrefix ����ļ���+ǰ׺
	 * @throws Exception
	 */
	public void plotAllChrDist(String outPathPrefix) {
		ArrayList<String[]> chrlengthInfo = gffChrAbs.getSeqHash().getChrLengthInfo();
		//find the longest chromosome's density
		double[] chrReads = getChrDensity(chrlengthInfo.get(chrlengthInfo.size() - 1)[0], maxresolution);
		double axisY = MathComput.median(chrReads, 95)*4;
		for (int i = chrlengthInfo.size() - 1; i >= 0; i--) {
			try {
				plotChrDist(chrlengthInfo.get(i)[0], maxresolution, axisY, FileOperate.changeFileSuffix(outPathPrefix, "_"+chrlengthInfo.get(i)[0], "png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * ����Ⱦɫ�壬���ظ�Ⱦɫ����reads�ֲ�
	 * 
	 * @param chrID
	 *            �ڼ�����ɫ��
	 * @param maxresolution
	 *            ��ֱ���
	 * @param axisY y��߽�
	 * @param outFileName ����ļ���������׺"_chrID"
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
			logger.error("����δ֪chrID��" + chrID);
			return;
		}
		if (chrReads == null) {
			return;
		}
		
		PlotScatter plotScatter = new PlotScatter();
		plotScatter.setAxisX(0, maxresolution);
		plotScatter.setAxisY(0, axisY);
		plotScatter.setMapNum2ChangeX(0, 0, resolution.length, chrLength, interval);

		DotStyle dotStyle = new DotStyle();
		Paint colorGradient = DotStyle.getGridentColor(GraphicsUtils.deriveDarker(Color.blue), Color.blue);
		dotStyle.setColor(colorGradient);
		dotStyle.setStyle(DotStyle.STYLE_AREA);
		plotScatter.addXY(resolutionDoub, chrReads, dotStyle);
		
		//////////////////��ӱ߿�///////////////////////////////
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
		//������mapping
//		plotScatter.setMapNum2ChangeY(0, 0, axisY, 500, 100);
		plotScatter.setTitle(chrID + " Reads Density", null);
		plotScatter.setTitleX("Chromosome Length", null, 0);
		plotScatter.setTitleY("Normalized Reads Counts", null, (int)axisY/5);
		
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_ML);
		
		plotScatter.saveToFile(outFileName, 10000, 1000);
	}

	/**
	 * ����ĳ��Ⱦɫ���ϵ�reads��������ܶ�ͼ ��Ҫ���ڻ������ϣ�һ��Ⱦɫ���ϵ�reads���
	 * 
	 * @param chrID
	 * @param binNum
	 *            �ֳɼ�������
	 * @parm type ȡ������ 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return û�еĻ��ͷ���null
	 */
	private double[] getChrDensity(String chrID, int binNum) {
		double[] tmpResult = mapReads.getReadsDensity(chrID, 0, 0, binNum);
		return tmpResult;
	}
	/**
	 * @param color
	 * @param SortS2M
	 *            �Ƿ��С��������
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore ���С��0�����colGeneID����ô����ָ�������reads����score
	 * @param rowStart
	 * @param heapMapSmall
	 * @param heapMapBig
	 * @param scale
	 * @param structure
	 *            ����ṹ��Ŀǰֻ�� GffDetailGene.TSS �� GffDetailGene.TES
	 * @param binNum
	 *            ���ֳɼ���
	 * @param outFile
	 * @return �������ֵ����Сֵ���趨
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
	 * ���summitλ�㣬��summitλ�㸽����readsͼ
	 * @param SortS2M
	 *            �Ƿ��С��������
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
	 * @return �������ֵ����Сֵ���趨
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
	 *            ������Ϣ
	 * @param color
	 * @param small
	 *            ��С
	 * @param big 
	 *            ��� ������С����С����ѡ����95��λ��Ϊ��ߵ�
	 * @param scale
	 *            scale�η�������1��ϡ��߱�С��1��ϡ��ͱ��
	 * @param outFile
	 * @return �������ֵ����Сֵ���趨
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
	 *            ������Ϣ
	 * @param structure
	 *            ����ṹ��Ŀǰֻ�� GffDetailGene.TSS �� GffDetailGene.TES
	 * @param color
	 * @param small
	 *            ��С
	 * @param big
	 *            ���
	 * @param scale
	 *            scale�η�������1��ϡ��߱�С��1��ϡ��ͱ��
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
		Color[] customGradient = Gradient.createMultiGradient(gradientColors,
				250);

		PlotHeatMap heatMap = new PlotHeatMap(lsMapInfoFinal, false, customGradient);
		heatMap.setRange(mindata1, maxdata1);
		heatMap.saveToFile(outFile, 6000, 1000);
	}

	/**
	 * ����ǰ���趨upBp��downBp ����Peak�����ǵĻ�������TSSͼ
	 * @param fileName Peak�ļ�
	 * @param rowStart �ӵڼ��п�ʼ��
	 * @param binNum �ֳɼ���
	 * @param resultFile ����ļ�
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
	 *            �Ƿ��С��������
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore ���С��0�����colGeneID����ô����ָ�������reads����score
	 * @param rowStart
	 * @param heapMapSmall
	 * @param heapMapBig
	 * @param scale
	 * @param structure
	 *            ����ṹ��Ŀǰֻ�� GffDetailGene.TSS �� GffDetailGene.TES
	 * @param binNum
	 *            ���ֳɼ���
	 * @param mirandaResultOut
	 * @return �������ֵ����Сֵ���趨
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
	 * ����ȫ����������TSSͼ
	 * @param binNum �ֳɼ���
	 * @param resultFile ����ļ�
	 * @param geneStructure GffDetailGene.TSS
	 */
	public void plotTssAllGene(int binNum, String resultFile, GeneStructure geneStructure) {
		plotTssGene(null, 0, binNum, resultFile, geneStructure);
	}
	/**
	 * ����ǰ���趨upBp��downBp ����ָ���Ļ�������TSSͼ
	 * @param fileName �����ļ��������һ��ΪgeneID���ڲ�ȥ�ظ�, ���û���ļ����򷵻�ȫ�����
	 * @param rowStart �ӵڼ��п�ʼ��
	 * @param binNum �ֳɼ���
	 * @param resultFile ����ļ�
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
	 * ���������symbol�����ظû�����tss���������mapreads��ƽ����
	 * @param geneID ��������
	 * @param tssUp tss���ζ���bp ��������������������
	 * @param tssDown tss���ζ���bp ��������������������
	 * @return ���û���򷵻�-1
	 */
	public double getGeneTss(String geneID, int tssUp, int tssDown) {
		GffDetailGene gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
		if (gffDetailGene == null) {
			return -1;
		}
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		int tssSite = gffGeneIsoInfo.getTSSsite();
		int tssStartR = 0; int tssEndR = 0;
		//����ͬ������Ҳ��ͬ
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
	 * ���������symbol�����ظû�����tss���������mapreads��ƽ����
	 * @param geneID ��������
	 * @param tssUp tss���ζ���bp ��������������������
	 * @param tssDown tss���ζ���bp ��������������������
	 */
	public double getGeneBodySum(String geneID) {
		GffDetailGene gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
		if (gffDetailGene == null) {
			return -1;
		}
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		int tssSite = gffGeneIsoInfo.getTSSsite();
		int tesSite = gffGeneIsoInfo.getTESsite();
		int tssStart = Math.min(tssSite, tesSite);
		int tssEnd = Math.max(tssSite, tesSite);
		double[] siteInfo = mapReads.getRangeInfo(mapReads.getBinNum(), gffGeneIsoInfo.getChrID(), tssStart, tssEnd, 0);
		return MathComput.sum(siteInfo);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * רΪ�������<br>
	 * ��Ϊrefseqʱ����õ�ĳ������ķֲ����������3��barcode����
	 * 
	 * @return û�иû����򷵻�null
	 */
	public double[] getGeneReadsHYRefseq(String geneID) {
		double[] tmpResult = getChrInfo(geneID, 1, 0);
		if (tmpResult == null) {
			return null;
		}
		// ��þ���ת¼������Ϣ
		GffGeneIsoInfo gffGeneIsoInfoOut = gffChrAbs.getGffHashGene().searchISO(geneID);
		return combineLoc(tmpResult, gffGeneIsoInfoOut.getLenUTR5() + 1);
	}

	/**
	 * ����atgλ�㣬��ø�atgλ���ںϲ����������Ӧ���ǵڼ�������1��ʼ
	 * 
	 * @param atgAASite
	 * @return
	 */
	public int getCombAtgSite(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfoOut = gffChrAbs.getGffHashGene().searchISO(geneID);
		int atgSite = gffGeneIsoInfoOut.getLenUTR5() + 1;
		// ����3��ָ3�����
		return (int) Math.ceil((double) (atgSite - 1) / 3);
	}

	/**
	 * ��������������øû����atgλ����mRNA��Ӧ���ǵڼ���λ�㣬��1��ʼ
	 * @param atgAASite
	 * @return
	 */
	public int getAtgSite(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfoOut = gffChrAbs.getGffHashGene().searchISO(geneID);
		return gffGeneIsoInfoOut.getLenUTR5() + 1;
		// ����3��ָ3�����
	}
	/**
	 * �趨peak��bed�ļ�����һ��ΪchrID���ڶ���Ϊ��㣬������Ϊ�յ㣬 ����ȥ��peak��ÿ��Ⱦɫ���bg���
	 * @param peakFile
	 * @param firstlinls1
	 * @return
	 */
	public ArrayList<String[]> getBG(String peakFile, int firstlinls1) {
		return mapReads.getChIPBG(peakFile, firstlinls1);
	}
	/**
	 * רΪ������� ����������ϲ�Ϊ1��coding��ȡ3�������һ�������Ӧ��reads��
	 * 
	 * @param geneReads
	 *            �û����reads��Ϣ�������ǵ��������
	 * @param AtgSite
	 *            �û����atgλ�㣬��1��ʼ����
	 * @return ���ؾ����ϲ��Ľ����Ʃ�� {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17}; atgλ��Ϊ6
	 *         ���{ 2,5,8,11,14,17};
	 */
	private double[] combineLoc(double[] geneReads, int AtgSite) {
		// ��ʱ��SeqInfo��һλ����ʵ�ʵĵ�һλ������atgsite��
		return MathComput.mySplineHY(geneReads, 3, AtgSite, 3);
	}

	/**
	 * ����<b>����</b>ʹ��<br>
	 * ��û������Ϣ��Ȼ�����򣬿��Դ�������ѡ����reads���ļ���Ȼ��ͼ
	 * ���ؾ��������mapinfo��list��ÿһ��mapInfo�����˸û���ĺ�������Ϣ
	 */
	public ArrayList<MapInfo> getChrInfo() {
		ArrayList<String> lsChrID = mapReads.getChrIDLs();
		ArrayList<MapInfo> lsMapInfo = new ArrayList<MapInfo>();
		for (String string : lsChrID) {
			mapReads.setNormalType(MapReads.NORMALIZATION_NO);
			GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(string);
			if (gffGeneIsoInfo.getGeneType() != GeneType.mRNA
					&& gffGeneIsoInfo.getGeneType() != GeneType.mRNA_TE) {
				continue;
			}

			double[] tmp = mapReads.getRangeInfo(mapReads.getBinNum(), string, 0, 0, 0);
			mapReads.setNormalType(mapNormType);
			double[] tmp2 = mapReads.getRangeInfo(mapReads.getBinNum(), string, 0, 0, 0);
			// ///////////////// �� �� �� ��
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
	 * ������׼�� ��MapInfo�е�double�������Ӧ��reads��Ϣ
	 * 
	 * @param binNum
	 *            ���ָ��������Ŀ
	 * @param lsmapInfo
	 * @param type
	 *            0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegionLs(int binNum, ArrayList<MapInfo> lsmapInfo, int type) {
		mapReads.getRangeLs(binNum, lsmapInfo, type);
	}
	/**
	 * ������׼�� ��MapInfo�е�double�������Ӧ��reads��Ϣ
	 * 
	 * @param binNum
	 *            ���ָ��������Ŀ
	 * @param lsmapInfo
	 * @param type
	 *            0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegion(int binNum, MapInfo mapInfo, int type) {
		mapReads.getRange(binNum, mapInfo, type);
	}
	
	/**
	 * ������׼������equations����
	 * @param lsmapInfo
	 * @param thisInvNum  ÿ��������������bp�������ڵ���invNum�������invNum�ı��� ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegion(MapInfo mapInfo, int thisInvNum, int type) {
		mapReads.getRange(mapInfo, thisInvNum, type);
	}

	/**
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * ���û��Ȩ�أ��Ͱ���reads���ܶȽ�������
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure ������ĸ����ֵĽṹ 
	 * @param binNum ������ֳɼ���
	 */
	public ArrayList<MapInfo> readFileGeneMapInfo(String txtExcel,int colGeneID, int colScore, int rowStart, GeneStructure Structure, int binNum) {
		////////////////////     �� �� ��   ////////////////////////////////////////////
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
	 * ���������Զ���û���
	 * ����ǰ���趨upBp��downBp
	 * @param lsMapInfos
	 * @param structure GffDetailGene.TSS��
	 * @param binNum �ֳɼ���
	 * @return
	 */
	public ArrayList<MapInfo> getPeakCoveredGeneMapInfo(ArrayList<? extends MapInfo> lsMapInfos, GeneStructure structure, int binNum) {
		HashMap<GffDetailGene,Double>  hashGffDetailGenes = getPeakGeneStructure( lsMapInfos, structure);
		 ArrayList<MapInfo> lsResult = getMapInfoFromGffGene(hashGffDetailGenes, structure);
		 mapReads.getRangeLs(binNum, lsResult, 0);
		 return lsResult;
	}
	
	/**
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * ���û��Ȩ�أ��Ͱ���reads���ܶȽ�������
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure ������ĸ����ֵĽṹ 
	 * @param binNum ������ֳɼ���
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
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param lsGeneValue string[2] 0:geneID 1:value ����1 ����û�У���ô����string[1] 0:geneID
	 * @param rowStart
	 * @param Structure ������ĸ����ֵĽṹ
	 * @param binNum ������ֳɼ���
	 * @return
	 */
	public ArrayList<MapInfo> getLsGeneMapInfo(ArrayList<String[]> lsGeneValue, GeneStructure Structure, int binNum) {
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
	 * ����ǰ���趨upBp��downBp
	 * ����һϵ��gffDetailGene���Լ���Ҫ�Ĳ��֣����ض�Ӧ�����LsMapInfo
	 * <b>ע������û�����reads��double[] value</b>
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
	 * ����peak����Ϣ��chrID������յ㣬���ر�peak���ǵ�Tss�Ļ������͸��������������Tssͼ
	 * �Զ�ȥ�������
	 * @param lsPeakInfo mapInfo������ chrID �� startLoc �� endLoc ���� 
	 * @param structure GffDetailGene.TSS��
	 * @return
	 * �����Ȩ�ص�hash��
	 */
	private HashMap<GffDetailGene,Double> getPeakGeneStructure(ArrayList<? extends MapInfo> lsMapInfos, GeneStructure structure) {
		//�洢���Ļ����Ȩ��
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
	 * �����������򣬷��ظ�peak�����ǵ�GffDetailGene
	 * @param tsstesRange ���Ƕȣ�tss��tes�ķ�Χ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure GffDetailGene.TSS��
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
			logger.error("��ʱû�г�Tss��Tes֮��Ļ���ṹ");
			return null;
		}
	}
	/**
	 * ǰ���趨upBp��downBp
	 * ����gffDetailGene���Լ���Ҫ�Ĳ��֣����ض�Ӧ�����MapInfo
	 * <b>ע������û�����reads��double[] value</b>
	 * @param gffDetailGene
	 * @param value �û�������Ӧ��Ȩ��
	 * @param structure GffDetailGene.TSS��
	 * @return
	 */
	private MapInfo getStructureLoc(GffDetailGene gffDetailGene, Double value,GeneStructure structure) {
		int plotUpstream = 0;
		int plotDownstream = 0;
		MapInfo mapInfoResult = null;
		String chrID = gffDetailGene.getRefID(); String geneName = gffDetailGene.getLongestSplit().getName();
		if (gffDetailGene.isCis5to3()) {
			plotUpstream = Math.abs(plotRange[0]);
			plotDownstream = Math.abs(plotRange[1]);
		} else {
			plotUpstream = Math.abs(plotRange[1]);
			plotDownstream = Math.abs(plotRange[0]);
		}
		
		if (structure.equals(GeneStructure.TSS)) {
			int tss = gffDetailGene.getLongestSplit().getTSSsite();
			mapInfoResult = new MapInfo(chrID, tss - plotUpstream, tss + plotDownstream, tss,0, geneName);
			mapInfoResult.setCis5to3(gffDetailGene.isCis5to3());
			mapInfoResult.setScore(value);
		}
		else if (structure.equals(GeneStructure.TES)) {
			int tes = gffDetailGene.getLongestSplit().getTESsite();
			mapInfoResult = new MapInfo(chrID, tes - plotUpstream, tes + plotDownstream, tes, 0, geneName);
			mapInfoResult.setCis5to3(gffDetailGene.isCis5to3());
			mapInfoResult.setScore(value);
		}
		else {
			logger.error("��û��Ӹ������͵�structure");
		}
		return mapInfoResult;
	}

}
