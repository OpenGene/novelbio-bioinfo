package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
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
	String fileName = "";
	int maxresolution = 10000;
	/** ��ͼ����Ҳ����tss��tes�ķ�Χ */
	int[] plotRange;
	MapReads mapReads;
	int mapNormType = MapReads.NORMALIZATION_ALL_READS;
	
	
	public GffChrPlotTss() {
	}
	
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
	
	/**
	 * �����Tss��Tes
	 * ������������bp������
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
	 * ÿ������λȡ��,����趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @return
	 */
	public MapReads getMapReads() {
		return mapReads;
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
		return plotHeatMap(lsMapInfos, color, heapMapSmall, heapMapBig, FileOperate.changeFileSuffix(outFile, "_HeatMap", "png"));
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
	 *            scale�η�������1��ϡ��߱��С��1��ϡ��ͱ���
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
		return new double[]{mindata, maxdata};
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
	private ArrayList<MapInfo> readFileGeneMapInfo(String txtExcel,int colGeneID, int colScore, int rowStart, GeneStructure Structure, int binNum) {
		////////////////////     �� �� ��   ////////////////////////////////////////////
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
			logger.error("��û���Ӹ������͵�structure");
		}
		return mapInfoResult;
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
	 *            scale�η�������1��ϡ��߱��С��1��ϡ��ͱ���
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