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
 * ������������򣬻�������ͳ��ͼ
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
	 *            ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @param HanYanFstrand
	 *            �Ƿ�ѡ����ģʽ������reads�Ƿ������ķ�����һ�¶����й��˹����������ר����Ժ������Ŀ���ķ�����
	 */
	public GffChrMap(String gffType, String gffFile, String chrFile,
			String readsBed, int binNum, boolean HanYanFstrand) {
		super(gffType, gffFile, chrFile, readsBed, binNum);
		this.HanYanFstrand = HanYanFstrand;
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
	 *            ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @param HanYanFstrand
	 *            �Ƿ�ѡ����ģʽ������reads�Ƿ������ķ�����һ�¶����й��˹����������ר����Ժ������Ŀ���ķ�����
	 */
	public GffChrMap(String gffType, String gffFile, String chrFile,
			String readsBed, int binNum) {
		super(gffType, gffFile, chrFile, readsBed, binNum);
		this.HanYanFstrand = false;
	}

 

	/**
	 * @param readsFile
	 *            mapping�Ľ���ļ��������Ź���һ��Ϊbed��ʽ
	 * @param binNum
	 *            ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
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
	 * ��ȡbed�ļ�
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
	 *            ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod
	 *            ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique
	 *            Unique��reads����һ�� novelbio�ı���ڵ����У���1��ʼ����
	 * @param booUniqueMapping
	 *            �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3
	 *            �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique,
			boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, colUnique, booUniqueMapping,
				cis5to3);
	}

	/**
	 * ����ĳ��Ⱦɫ���ϵ�reads����������ܶ�ͼ��ֻ�Ǽ򵥵ļ���reads��һ��Ⱦɫ���ϵ���� ��Ҫ����RefSeqʱ��һ�������ϵ�reads���
	 * 
	 * @param chrID
	 * @param thisInvNum
	 *            ÿ�����伸bp
	 * @parm type ȡ������ 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return û�еĻ��ͷ���null
	 */
	public double[] getChrInfo(String chrID, int thisInvNum, int type) {
		double[] tmpResult = mapReads.getRengeInfo(thisInvNum, chrID, 0, 0,
				type);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}

	int maxresolution = 10000;

	/**
	 * ��������Ⱦɫ�����ܶ�ͼ
	 * ��R��
	 * @param gffChrMap2
	 *            �Ƿ��еڶ���Ⱦɫ�壬û�еĻ�����null
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
	 * ��������Ⱦɫ�����ܶ�ͼ
	 * ��java��
	 * @param gffChrMap2
	 *            �Ƿ��еڶ���Ⱦɫ�壬û�еĻ�����null
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
	 * ����Ⱦɫ�壬���ظ�Ⱦɫ����reads�ֲ�
	 * 
	 * @param chrID
	 *            �ڼ�����ɫ��
	 * @param maxresolution
	 *            ��ֱ���
	 * @param gffChrMap2
	 *            �����Ҫ���ڶ���Ⱦɫ���ͼ��Ҳ���ǶԳ��˻�
	 * @param ����ļ���
	 *            ������׺"_chrID"
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
	 * ����Ⱦɫ�壬���ظ�Ⱦɫ����reads�ֲ�
	 * 
	 * @param chrID
	 *            �ڼ�����ɫ��
	 * @param maxresolution
	 *            ��ֱ���
	 * @param gffChrMap2
	 *            �����Ҫ���ڶ���Ⱦɫ���ͼ��Ҳ���ǶԳ��˻�
	 * @param ����ļ���
	 *            ������׺"_chrID"
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
			// //////// �� �� �� �� /////////////////////
			txtRparamater.setParameter(
					NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM, true, false);
			txtRparamater.writefile("Item" + "\t" + "Info" + "\r\n");// ����Ҫ���ϵģ�����R��ȡ��������
			txtRparamater.writefile("tihsresolution" + "\t" + chrLength
					+ "\r\n");
			txtRparamater.writefile("maxresolution" + "\t"
					+ seqHash.getChrLenMax() + "\r\n");
			txtRparamater.writefile("ChrID" + "\t" + chrID + "\r\n");

			// //////// �� �� �� �� ///////////////////////
			txtRparamater.setParameter(
					NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X, true, false);
			txtRparamater.Rwritefile(resolution);
			txtRparamater.setParameter(
					NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y, true, false);
			txtRparamater.Rwritefile(chrReads);

			// /////////����ڶ���Ⱦɫ�����ж�������ôҲд���ı�/////////////////////////////////////////
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
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}
	/**
	 * ����R��ͼ
	 * 
	 * @throws Exception
	 */
	private void hist() throws Exception {
		// ����������·���������ڵ�ǰ�ļ���������
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
	 * ���summitλ�㣬��summitλ�㸽����readsͼ
	 * 
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
	 * ����ǰ���趨upBp��downBp ����ָ���Ļ�������TSSͼ
	 * @param fileName �����ļ��������һ��ΪgeneID���ڲ�ȥ�ظ�
	 * @param rowStart �ӵڼ��п�ʼ��
	 * @param binNum �ֳɼ���
	 * @param resultFile ����ļ�
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
	 * ���������symbol�����ظû�����tss���������mapreads��ƽ����
	 * @param geneID ��������
	 * @param tssUp tss���ζ���bp ��������������������
	 * @param tssDown tss���ζ���bp ��������������������
	 */
	private double getGeneTss(String geneID, int tssUp, int tssDown) {
		GffDetailGene gffDetailGene = gffHashGene.searchLOC(geneID);
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
		double[] siteInfo = mapReads.getRengeInfo(mapReads.getBinNum(), gffGeneIsoInfo.getChrID(), tssStart, tssEnd, 0);
		return MathComput.mean(siteInfo);
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
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return combineLoc(tmpResult, gffGeneIsoInfoOut.getLenUTR5() + 1);
	}

	/**
	 * ����atgλ�㣬��ø�atgλ���ںϲ����������Ӧ���ǵڼ�������1��ʼ
	 * 
	 * @param atgSite
	 * @return
	 */
	public int getCombAtgSite(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		int atgSite = gffGeneIsoInfoOut.getLenUTR5() + 1;
		// ����3��ָ3�����
		return (int) Math.ceil((double) (atgSite - 1) / 3);
	}

	/**
	 * ����atgλ�㣬��ø�atgλ���ںϲ����������Ӧ���ǵڼ�������1��ʼ
	 * 
	 * @param atgSite
	 * @return
	 */
	public int getAtgSite(String geneID) {
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return gffGeneIsoInfoOut.getLenUTR5() + 1;
		// ����3��ָ3�����
	}
	/**
	 * �趨peak��bed�ļ�����һ��ΪchrID���ڶ���Ϊ��㣬������Ϊ�յ㣬 ����ȥ��peak��ÿ��Ⱦɫ���bg���
	 * @param peakFile
	 * @param firstlinls1
	 * @return
	 */
	public ArrayList<String[]> getBG(String peakFile, int firstlinls1)
	{
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
	 * ������׼�� ��MapInfo�е�double�������Ӧ��reads��Ϣ
	 * 
	 * @param binNum
	 *            ���ָ��������Ŀ
	 * @param lsmapInfo
	 * @param type
	 *            0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegionLs(int binNum, ArrayList<MapInfo> lsmapInfo, int type) {
		mapReads.getRegionLs(binNum, lsmapInfo, type);
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
		mapReads.getRegion(binNum, mapInfo, type);
	}
	
	/**
	 * ������׼������equations����
	 * @param lsmapInfo
	 * @param thisInvNum  ÿ��������������bp�������ڵ���invNum�������invNum�ı��� ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegion(MapInfo mapInfo, int thisInvNum, int type) {
		mapReads.getRegion(mapInfo, thisInvNum, type);
	}
}
