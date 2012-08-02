package com.novelbio.analysis.seq.genomeNew;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.heatmap.Gradient;
import com.novelbio.base.plot.heatmap.PlotHeatMap;
import com.novelbio.base.plot.java.HeatChart;
import com.novelbio.database.model.modcopeid.GeneID;
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
		int taxID = 4932;
		String bedFile = "/media/winF/NBC/Project/Project_Invitrogen/WHB_yeast/mapping/WHB_nonUnique.bed";
		BedSeq bedSeq = new BedSeq(bedFile);
		bedSeq = bedSeq.sortBedFile();
		bedSeq = bedSeq.extend(250);
		GffChrAbs gffChrAbs = new GffChrAbs(taxID);
		gffChrAbs.setMapReads(bedSeq.getFileName(), 1);
		gffChrAbs.loadMapReads();
		gffChrAbs.setPlotRegion(2000, 2000);
		GffChrMap gffChrMap = new GffChrMap(gffChrAbs);
		gffChrMap.plotAllChrDist("/media/winF/NBC/Project/Project_Invitrogen/WHB_yeast/mapping/readsChrDensity_NoneUnique/WHB_nonUniqueMapping");		
//		gffChrMap.plotTssAllGene(1000, "/media/winF/NBC/Project/Project_Invitrogen/WHB_yeast/mapping/tss", GffDetailGene.TSS);
	}
	
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	Logger logger = Logger.getLogger(GffChrMap.class);
	String fileName = "";
	int maxresolution = 10000;
	
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
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}
	/**
	 * ÿ������λȡ��,����趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @return
	 */
	public int getThisInv() {
		return gffChrAbs.getMapReads().getBinNum();
	}
	/**
	 * ����Ⱦɫ������ͳ��ÿ��Ⱦɫ������λ������ÿ��λ������ string[4] 0: chrID 1: readsNum 2: readsPipNum
	 * 3: readsPipMean
	 * 
	 * @return
	 */
	public ArrayList<String[]> getChrLenInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String> lsChrID = gffChrAbs.getMapReads().getChrIDLs();
		for (String string : lsChrID) {
			String[] chrInfoTmp = new String[4];
			chrInfoTmp[0] = string;
			chrInfoTmp[1] = gffChrAbs.getMapReads().getChrReadsNum(string) + "";
			chrInfoTmp[2] = gffChrAbs.getMapReads().getChrReadsPipNum(string) + "";
			chrInfoTmp[3] = gffChrAbs.getMapReads().getChrReadsPipMean(string) + "";
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
		gffChrAbs.getMapReads().setFilter(uniqReads, startCod, booUniqueMapping, cis5to3);
	}
	/**
	 * ����ĳ��Ⱦɫ���ϵ�reads����������ܶ�ͼ��ֻ�Ǽ򵥵ļ���reads��һ��Ⱦɫ���ϵ���� ��Ҫ����RefSeqʱ��һ�������ϵ�reads���
	 * @param chrID
	 * @param thisInvNum ÿ�����伸bp
	 * @parm type ȡ������ 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return û�еĻ��ͷ���null
	 */
	public double[] getChrInfo(String chrID, int thisInvNum, int type) {
		double[] tmpResult = gffChrAbs.getMapReads().getRengeInfo(thisInvNum, chrID, 0, 0, type);
		gffChrAbs.getMapReads().normDouble(tmpResult);
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
		double[] chrReads = getChrDensity(gffChrAbs.getSeqHash().getChrLengthInfo().get(gffChrAbs.getSeqHash().getChrLengthInfo().size() - 1)[0], maxresolution);
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
		double[] tmpResult = gffChrAbs.getMapReads().getReadsDensity(chrID, 0, 0, binNum);
		gffChrAbs.getMapReads().normDouble(tmpResult);
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
	public double[] plotTssHeatMap(Color color, boolean SortS2M,
			String txtExcel, int colGeneID, int colScore, int rowStart,
			double heapMapSmall, double heapMapBig,
			GeneStructure structure, int binNum, String outFile) {
		ArrayList<MapInfo> lsMapInfos = null;
		if (txtExcel != null && !txtExcel.trim().equals("")) {
			lsMapInfos = gffChrAbs.readFileGeneMapInfo(txtExcel, colGeneID, colScore, rowStart, structure, binNum);
		}
		else {
			lsMapInfos = gffChrAbs.readGeneMapInfoAll(structure, binNum);
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
		gffChrAbs.getMapReads().getRegionLs(binNum, lsMapInfos, 0);
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
		ArrayList<MapInfo> lsMapTssInfo = gffChrAbs.getPeakCoveredGeneMapInfo(lsMapInfo, geneStructure, binNum);
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
		ArrayList<MapInfo> lsMapTssInfo = gffChrAbs.getPeakCoveredGeneMapInfo(lsMapInfo, geneStructure, binNum);
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
			lsMapInfo = gffChrAbs.readGeneMapInfoAll(geneStructure, binNum);
		}
		else {
			lsMapInfo = gffChrAbs.readFileGeneMapInfo(fileName, 1, 0, rowStart, geneStructure, binNum);
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
		double[] siteInfo = gffChrAbs.getMapReads().getRengeInfo(gffChrAbs.getMapReads().getBinNum(), gffGeneIsoInfo.getChrID(), tssStart, tssEnd, 0);
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
		double[] siteInfo = gffChrAbs.getMapReads().getRengeInfo(gffChrAbs.getMapReads().getBinNum(), gffGeneIsoInfo.getChrID(), tssStart, tssEnd, 0);
		return MathComput.sum(siteInfo);
	}
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
	 * @param atgSite
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
	 * @param atgSite
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
		return gffChrAbs.getMapReads().getChIPBG(peakFile, firstlinls1);
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
		ArrayList<String> lsChrID = gffChrAbs.getMapReads().getChrIDLs();
		ArrayList<MapInfo> lsMapInfo = new ArrayList<MapInfo>();
		for (String string : lsChrID) {
			gffChrAbs.getMapReads().setNormalType(MapReads.NORMALIZATION_NO);
			GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(string);
			if (gffGeneIsoInfo.getGeneType() != GffGeneIsoInfo.TYPE_GENE_MRNA
					&& gffGeneIsoInfo.getGeneType() != GffGeneIsoInfo.TYPE_GENE_MRNA_TE) {
				continue;
			}

			double[] tmp = gffChrAbs.getMapReads().getRengeInfo(gffChrAbs.getMapReads().getBinNum(), string,
					0, 0, 0);
			gffChrAbs.getMapReads().setNormalType(gffChrAbs.mapNormType);
			double[] tmp2 = gffChrAbs.getMapReads().getRengeInfo(gffChrAbs.getMapReads().getBinNum(), string,
					0, 0, 0);
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
		gffChrAbs.getMapReads().getRegionLs(binNum, lsmapInfo, type);
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
		gffChrAbs.getMapReads().getRegion(binNum, mapInfo, type);
	}
	
	/**
	 * ������׼������equations����
	 * @param lsmapInfo
	 * @param thisInvNum  ÿ��������������bp�������ڵ���invNum�������invNum�ı��� ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegion(MapInfo mapInfo, int thisInvNum, int type) {
		gffChrAbs.getMapReads().getRegion(mapInfo, thisInvNum, type);
	}
}
