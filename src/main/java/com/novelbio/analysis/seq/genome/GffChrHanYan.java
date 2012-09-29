package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.java.HeatChart;
import com.novelbio.database.model.modgeneid.GeneID;

import de.erichseifert.gral.util.GraphicsUtils;

/**
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
 * 
 * @author zong0jie
 * 
 */
public abstract class GffChrHanYan {
private static Logger logger = Logger.getLogger(GffChrHanYan.class);
	GffChrAbs gffChrAbs;
	MapReads mapReads;
	/** ����Ǹ�atg��λ�� */
	int atgAlign = 0;
	/** Atg����λ�㣬������ATG */
	int atgUp = 300;
	/** Atg����λ�㣬����ATG */
	int atgDown = 3000;
	/** ����geneID��accID����Ҫ���ڲ���map����ľ������ 
	 * key Сд
	 * */
	HashMap<String, String> mapGeneID2AccID;
	/** accID��seqInfo 
	 * key Сд
	 * */
	HashMap<String, SeqInfo> mapAccID2SeqInfo;
	
	public static void main(String[] args) {
		String mapFile = "/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/RAP3h_filtered_sorted.bed";
		String resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		GffChrUnionHanYanRefSeq gffChrHanYan = new GffChrUnionHanYanRefSeq();
		GffChrAbs gffChrAbs = new GffChrAbs(10090);
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/home/zong0jie/Desktop/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, true, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plotRAP3h_300-3000");
		gffChrHanYan.drawGeneAll("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plotRAP3h_300-3000/RAP3h_");

		mapFile = "/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/20PBS_filtered_sorted.bed";
		resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		gffChrHanYan = new GffChrUnionHanYanRefSeq();
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/home/zong0jie/Desktop/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, true, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plot20PBS_300-3000");
		gffChrHanYan.drawGeneAll("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plot20PBS_300-3000/20PBS_");
		
		mapFile = "/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/LY3h_filtered_sorted.bed";
		resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		gffChrHanYan = new GffChrUnionHanYanRefSeq();
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/home/zong0jie/Desktop/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, true, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plotLY3h_300-3000");
		gffChrHanYan.drawGeneAll("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plotLY3h_300-3000/LY3h_");
		
		mapFile = "/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/TSC2_KO_filtered_sorted.bed";
		resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		gffChrHanYan = new GffChrUnionHanYanRefSeq();
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/home/zong0jie/Desktop/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, true, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plotTSC2_KO_300-3000");
		gffChrHanYan.drawGeneAll("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plotTSC2_KO_300-3000/TSC2_KO_");
		
		mapFile = "/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/TSC2_WT_filtered_sorted.bed";
		resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		gffChrHanYan = new GffChrUnionHanYanRefSeq();
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/home/zong0jie/Desktop/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, true, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plotTSC2_WT_300-3000");
		gffChrHanYan.drawGeneAll("/home/zong0jie/Desktop/TSC2_2nd_Seq/mapping/plotTSC2_WT_300-3000/TSC2_WT_");
		
		
//		GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO("NM_001017429");
//		int length = gffGeneIsoInfo.getLocDistmRNA(gffGeneIsoInfo.getTSSsite(), gffGeneIsoInfo.getATGsite());
//		System.out.println(length);
		
//		Species species = new Species(10090);
//		GffChrSeq gffChrSeq = new GffChrSeq();
//		gffChrSeq.setSpecies(species);
//		gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
//		gffChrSeq.setGetIntron(false);
//		gffChrSeq.setGetAAseq(false);
//		gffChrSeq.setGetAllIso(false);
//		gffChrSeq.setIsGetOnlyMRNA(true);
//		gffChrSeq.setGetSeqIsoGenomWide();
//		gffChrSeq.setOutPutFile("/home/zong0jie/Desktop/TSC2_2nd_Seq/refseq/refMRNA.fa");
//		gffChrSeq.run();
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setNormType(int normalType) {
		mapReads.setNormalType(normalType);
	}
	/** Ĭ�ϻ�ȡatg����100bp��λ�� */
	public void setAtgUp(int atgUp) {
		this.atgUp = atgUp;
	}
	public void setAtgDown(int atgDown) {
		this.atgDown = atgDown;
	}
	
	public void loadMapFile(String mapFile, int tagLength, boolean uniqReads, int startCod, Boolean cis5To3, boolean uniqMapping) {
		mapGeneID2AccID = null;
		mapAccID2SeqInfo = null;
		loadMap(mapFile, tagLength, uniqReads, startCod, cis5To3, uniqMapping);
	}
	/**
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 * @param tagLength �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ������20�Ż�������á�Ŀǰsolexa˫���������ȴ����200-400bp������̫��ȷ ,Ĭ����400
	 * @param uniqReads ͬһλ����ظ��Ƿ������һ��
	 * @param startCod ��ͷ������λ��������3λ
	 * @param cis5To3 �Ƿ���ѡĳһ�������reads
	 * @param uniqMapping �Ƿ���ѡΨһ�ȶԵ� 
	 */
	protected abstract void loadMap(String mapFile, int tagLength, boolean uniqReads, int startCod, Boolean cis5To3, boolean uniqMapping);
	
	public void drawGeneAll(String outPrefix) {
		ArrayList<String> lsgenID = getAllGeneName();
		for (String geneName : lsgenID) {
			drawGene(outPrefix, geneName);
		}
	}
	
	
	public void drawGene(String outPrefix, String geneName) {
		SeqInfo seqInfo = getSeqInfo(geneName);
		if (seqInfo == null) {
			return;
		}
		double[] y = new double[seqInfo.atg.length];//seqInfo.atg;
		for (int i = 0; i < seqInfo.atg.length; i++) {
			y[i] = seqInfo.atg[i];
		}
		
		double[] x = new double[seqInfo.atg.length];
		for (int i = 0; i < x.length; i++) {
			x[i] = i - atgUp;
		}
		
		double yMax = MathComput.max(y) * 1.5;
		double xMin = x[0];
		double xMax = x[x.length - 1];
		
		PlotScatter plotScatter = new PlotScatter();
		plotScatter.setAxisX(xMin, xMax);
		plotScatter.setAxisY(0, yMax);

		DotStyle dotStyle = new DotStyle();
		Paint colorGradient = DotStyle.getGridentColor(GraphicsUtils.deriveDarker(Color.blue), Color.blue);
		dotStyle.setColor(colorGradient);
		dotStyle.setStyle(DotStyle.STYLE_AREA);
		plotScatter.addXY(x, y, dotStyle);
		
		//////////////////��ӱ߿�///////////////////////////////
		DotStyle dotStyleAtg = new DotStyle();
		dotStyleAtg.setStyle(DotStyle.STYLE_LINE);
		dotStyleAtg.setColor(Color.RED);
		dotStyleAtg.setSize(DotStyle.SIZE_B);
		//0�����atg
		double[] xATG= new double[]{0, 0};
		double[] yATG = new double[]{0, yMax};
		plotScatter.addXY(xATG, yATG, dotStyleAtg);
		
		DotStyle dotStyleutg = new DotStyle();
		dotStyleutg.setStyle(DotStyle.STYLE_LINE);
		dotStyleutg.setColor(Color.RED);
		dotStyleutg.setSize(DotStyle.SIZE_B);
		double[] xUAG= new double[]{seqInfo.uagToAtgSite, seqInfo.uagToAtgSite};
		double[] yUAG = new double[]{0, yMax};
		plotScatter.addXY(xUAG, yUAG, dotStyleutg);
		
		//////////////////////////////////////////////////////////////
		
		DotStyle dotStyleTss = new DotStyle();
		dotStyleTss.setStyle(DotStyle.STYLE_LINE);
		dotStyleTss.setColor(Color.green);
		dotStyleTss.setSize(DotStyle.SIZE_B);
		
		//0�����atg
		double[] xTss= new double[]{seqInfo.tssToAtgSite, seqInfo.tssToAtgSite};
		double[] yTss = new double[]{0, yMax};
		plotScatter.addXY(xTss, yTss, dotStyleTss);
		
		DotStyle dotStyleTes = new DotStyle();
		dotStyleTes.setStyle(DotStyle.STYLE_LINE);
		dotStyleTes.setColor(Color.green);
		dotStyleTes.setSize(DotStyle.SIZE_B);
		//0�����atg
		double[] xTes= new double[]{seqInfo.tesToAtgSite, seqInfo.tesToAtgSite};
		double[] yTes = new double[]{0, yMax};
		plotScatter.addXY(xTes, yTes, dotStyleTes);
		
		
		
		
		
		plotScatter.setBg(Color.WHITE);
		plotScatter.setAlpha(false);
		//������mapping
//		plotScatter.setMapNum2ChangeY(0, 0, axisY, 500, 100);
		plotScatter.setTitle(geneName +" Reads Density    ATG site: " + seqInfo.atgSite, null);
		plotScatter.setTitleX("Gene Length", null, 0);
		plotScatter.setTitleY("Normalized Reads Counts", null, (int)yMax/8);
		
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_SM);
		GeneID geneID = new GeneID(geneName, gffChrAbs.getTaxID());
		String fileName= FileOperate.changeFileSuffix(outPrefix, geneName + "_" + geneID.getSymbol(), "png");
		plotScatter.saveToFile(fileName, 2000, 600);
	}
	
	/**
	 * ����������, ����seqInfo
	 * @param geneName
	 * @return
	 */
	private SeqInfo getSeqInfo(String geneName) {
		String accID;
		setMapAccID2SeqInfo();
		if (mapAccID2SeqInfo.containsKey(geneName.toLowerCase())) {
			accID = geneName;
		} else {
			setMapGeneID2AccID();
			GeneID geneID = new GeneID(geneName, gffChrAbs.getTaxID());
			accID = mapGeneID2AccID.get(geneID.getGenUniID().toLowerCase());
		}
		return mapAccID2SeqInfo.get(accID.toLowerCase());
	}
	
	private void setMapAccID2SeqInfo() {
		if (mapAccID2SeqInfo != null) {
			return;
		}
		mapAccID2SeqInfo = new HashMap<String, SeqInfo>();
		ArrayList<String> lsgenID = getAllGeneName();
		ArrayList<SeqInfo> lsResult = getATGDensity(lsgenID, 0);
		for (SeqInfo seqInfo : lsResult) {
			mapAccID2SeqInfo.put(seqInfo.seqName.toLowerCase(), seqInfo);
		}
	}
	
	private void setMapGeneID2AccID() {
		if (mapGeneID2AccID != null) {
			return;
		}
		mapGeneID2AccID = new HashMap<String, String>();
		ArrayList<String> lsAccID = mapReads.getChrIDLs();
		for (String string : lsAccID) {
			GeneID geneID = new GeneID(string, gffChrAbs.getSpecies().getTaxID());
			mapGeneID2AccID.put(geneID.getGenUniID().toLowerCase(), string);
		}
	}
	public void drawAtgPlot(String resultFileOut) {
		ArrayList<double[]> lsResult = getAtgPlot();
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFileOut, true);
		for (double[] ds : lsResult) {
			txtWrite.writefileln(ds[0] + "\t" + ds[1]);
		}
		txtWrite.close();
	}
	
	public ArrayList<double[]> getAtgPlot() {
		ArrayList<String> lsgenID = getAllGeneName();
		ArrayList<SeqInfo> lsResult = getATGDensity(lsgenID, 0);
		if (atgUp <= 0) {
			atgUp = atgAlign;
		}
		//����������
		String[][] GeneEndDensity = new String[lsResult.size()][lsResult.get(0).atg.length+1];
		for (int i = 0; i < GeneEndDensity.length; i++) {
			for (int j = 1; j < GeneEndDensity[0].length; j++) {
				GeneEndDensity[i][j] = lsResult.get(i).atg[j-1] + "";
			}
		}
		for (int i = 0; i < GeneEndDensity.length; i++) {
			GeneEndDensity[i][0] = lsResult.get(i).seqName;
		}
		//������������
		double[][] GeneEndDensity2 = new double[lsResult.size()][lsResult.get(0).atg.length];
		for (int i = 0; i < GeneEndDensity2.length; i++) {
			for (int j = 0; j < GeneEndDensity2[0].length; j++) {
				GeneEndDensity2[i][j] = lsResult.get(i).atg[j];
			}
		}
		
		ArrayList<double[]> lsAxisX2AxisY = new ArrayList<double[]>();
		for (SeqInfo seqInfo : lsResult) {
			//����ÿ��exon��ÿ��λ��
			for (int i = 0; i < seqInfo.atg.length; i++) {
				double info = seqInfo.atg[i];
				if (lsAxisX2AxisY.size() <= i) {
//					lsAxisX2AxisY.add(new double[]{i - atgUp/3, info});
					lsAxisX2AxisY.add(new double[]{i - atgUp, info});
				} else {
					double[] value = lsAxisX2AxisY.get(i);
					value[1] = value[1] + seqInfo.atg[i];
				}
			}
		}
		return lsAxisX2AxisY;
	}
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void drawHeatMap(String resultFilePath, String prefix) throws Exception {
		resultFilePath = FileOperate.addSep(resultFilePath);
		ArrayList<String> lsgenID = getAllGeneName();
		ArrayList<String> lsgeneIDresult = new ArrayList<String>();
		for (String string : lsgenID) {
			lsgeneIDresult.add(string.split("/")[0]);
		}
		ArrayList<SeqInfo> lsResult = getATGDensity(lsgeneIDresult, -1);
		if (atgUp <= 0) {
			atgUp = atgAlign;
		}
		
		
		String[][] GeneEndDensity = new String[lsResult.size()][lsResult.get(0).atg.length+1];
		for (int i = 0; i < GeneEndDensity.length; i++) {
			for (int j = 1; j < GeneEndDensity[0].length; j++) {
				GeneEndDensity[i][j] = lsResult.get(i).atg[j-1] + "";
			}
		}
		for (int i = 0; i < GeneEndDensity.length; i++) {
			GeneEndDensity[i][0] = lsResult.get(i).seqName;
		}
		
		double[][] GeneEndDensity2 = new double[lsResult.size()][lsResult.get(0).atg.length];
		for (int i = 0; i < GeneEndDensity2.length; i++) {
			for (int j = 0; j < GeneEndDensity2[0].length; j++) {
				GeneEndDensity2[i][j] = lsResult.get(i).atg[j];
			}
		}
		System.out.println("���з����Ļ�����Ŀ��" + GeneEndDensity.length);
		HeatChart map = new HeatChart(GeneEndDensity2,0,200);
		map.setTitle("ATGsit: "+ (atgUp/3 +1) );
		map.setXAxisLabel("X Axis");
		map.setYAxisLabel("Y Axis");
		map.setXValues(-20, 1);
		String[] yvalue = new String[GeneEndDensity2.length];
		for (int i = 0; i < yvalue.length; i++) {
			yvalue[i] = "";
		}
		map.setYValues(yvalue);
		Dimension bb = new Dimension();
		bb.setSize(12, 0.05);
		map.setCellSize(bb );
		//Output the chart to a file.
		Color colorHigh = Color.BLUE;
		Color colorDown = Color.WHITE;
		//map.setBackgroundColour(color);
		map.setHighValueColour(colorHigh);
		map.setLowValueColour(colorDown);
		try {
			map.saveToFile(new File(resultFilePath+prefix+"Atg.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ͼƬ�ĸ߶�����Ϊ�� "+map.getChartSize().getHeight());
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter(resultFilePath+prefix+"Atgmatrix.txt", true, false);
		txtReadandWrite.ExcelWrite(GeneEndDensity);
	}
	protected abstract ArrayList<String> getAllGeneName();

	/**
	 * @param lsGeneID
	 * @param filled ��λ��ʲô��䣬�����heatmap������-1������ǵ��ӣ�����0
	 */
	private ArrayList<SeqInfo> getATGDensity(ArrayList<String> lsGeneID, int filled) {
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();;
		ArrayList<SeqInfo> lsAtg = new ArrayList<SeqInfo>();
		for (String string : lsGeneID) {
			if (string.equalsIgnoreCase("NM_001013369")) {
				logger.error("stop");
			}
			SeqInfo seqInfo = new SeqInfo();
			GffGeneIsoInfo gffGeneIsoSearch = gffHashGene.searchISO(string);
			if (gffGeneIsoSearch.ismRNA()) {
				seqInfo.atg = getReadsInfo(string,gffGeneIsoSearch);
				if (seqInfo.atg == null) {
					logger.error("������û����Ӧ����Ϣ��"+gffGeneIsoSearch.getParentGffDetailGene().getName()+" "+ 
							gffGeneIsoSearch.getTSSsite() +"  " +gffGeneIsoSearch.getTESsite() +"  "+gffGeneIsoSearch.getName());
					continue;
				}
				seqInfo.seqName = string;
				seqInfo.uagToAtgSite = gffGeneIsoSearch.getLocDistmRNA(gffGeneIsoSearch.getATGsite(), gffGeneIsoSearch.getUAGsite());
				seqInfo.tssToAtgSite = gffGeneIsoSearch.getLocDistmRNA(gffGeneIsoSearch.getATGsite(), gffGeneIsoSearch.getTSSsite());
				seqInfo.tesToAtgSite = gffGeneIsoSearch.getLocDistmRNA(gffGeneIsoSearch.getATGsite(), gffGeneIsoSearch.getTESsite());
				lsAtg.add(seqInfo);
			}
		}
		return setMatrix(lsAtg, filled);
	}
	/**
	 * ������Ժ������ķ���������5UTR�ĳ��Ƚ������򣬴�С�������У�Ȼ��
	 * @param lsAtg key 5UTR�ĳ��ȣ�value���ܹ����еĳ��ȣ���һλΪatg����λ��
	 * @param AtgUp ѡȡATG���ζ���bp��������ATGλ�� -1Ϊȫѡ ������λ������ζ���bp������������λ��
	 * @param AtgDown ѡȡATG���ζ���bp,������ATGλ�㡣 -1Ϊȫѡ ѡȡ����λ������ζ���bp������������λ��
	 * @param filled ��λ��ʲô��䣬�����heatmap������-1������ǵ��ӣ�����0
	 */
	protected ArrayList<SeqInfo> setMatrix(ArrayList<SeqInfo> lsAtg, int filled) {
		int maxGeneBody = 0;
		//������UTR����
		atgAlign = getAtgAlign(lsAtg);//Ҫ��atg��alignment�ģ��ڲ�������������
		//������ATG���γ���,������ATGλ��
		for (SeqInfo ds : lsAtg) {
			if (ds.seqName.equalsIgnoreCase("NM_139149")) {
				logger.error("stop");
			}
			if (ds.atg.length-1 - ds.atg[0] > maxGeneBody) {
				maxGeneBody = (int) (ds.atg.length-1 - ds.atg[0]);
			}
		}
		ArrayList<SeqInfo> lsdouble = new ArrayList<SeqInfo>();
		for (SeqInfo ds : lsAtg) {
			//��ʱ��SeqInfo��һλ����ʵ�ʵĵ�һλ������atgsite��
			SeqInfo tmpResult = setDouble(ds, atgAlign, maxGeneBody, filled);
			lsdouble.add(tmpResult);
		}
		//////////////////////
		combineLoc(lsdouble,atgAlign);
		//////////////////////
		return lsdouble;
	}
	/**
	 * ����������ϲ�Ϊ1��coding
	 * @param AtgUp ѡȡATG���ζ���bp��������ATGλ�� -1Ϊȫѡ ������λ������ζ���bo������������λ��
	 * @param AlignATGSite �ATG��λ��ľ���λ�ã���Ҫ����λ��ǰ��ĳ���
	 */
	private  void combineLoc(ArrayList<SeqInfo> lsdouble, int AlignATGSite) {
		if (true) {
			return;
		}
		for (SeqInfo seqInfo : lsdouble) {
			if (seqInfo.seqName.equalsIgnoreCase("NM_139149")) {
				logger.error("stop");
			}
			if (atgUp > 0) {
				seqInfo.atg = MathComput.mySpline(seqInfo.atg, 3, atgUp%3 + 1, 3);
				System.out.println("stop");
				System.out.println("stop");
			} else {
				seqInfo.atg = MathComput.mySpline(seqInfo.atg, 3, (AlignATGSite - 1)%3, 3);
			}
		}
	}
	/**
	 * ���ȫ�������ϣ�atg����UTR5������Զ����
	 * @param lsAtg value���ܹ����еĳ��ȣ���һλΪatg����λ��
	 * @return
	 */
	public int getAtgAlign( ArrayList<SeqInfo> lsAtg) {
		//�Ӵ�С����
		Collections.sort(lsAtg, new Comparator<SeqInfo>() {
			@Override
			public int compare(SeqInfo o1, SeqInfo o2) {
				if (o1.atg[0] < o2.atg[0]) {
					return 1;
				} else if (o1.atg[0] == o2.atg[0]) {
					return 0;
				} else {
					return -1;
				}
			}
		});
		return (int) lsAtg.get(0).atg[0];
	}
	
	/**
	 * �����������������
	 * @param input �������飬��һλΪatg����λ��,Ҳ������Ҫ�����λ�㣬��0��ʼ����
	 * @param alignATGSite �ATG��λ��ľ���λ�ã���Ҫ����λ��ǰ��ĳ��ȣ�������atgλ��
	 * @param ATGbody Atg�����ܹ��೤������Atgλ��,��Ҫ����λ��������ж೤
	 * @param filled ��λ��ʲô��䣬�����heatmap������-1������ǵ��ӣ�����0 ��λ��ʲô���
	 * @return
	 */
	private SeqInfo setDouble(SeqInfo input, int alignATGSite, int ATGbody, int filled ) {
		if (input.seqName.equalsIgnoreCase("NM_139149")) {
			logger.error("stop");
		}
		int atgThisGene = (int)input.atg[0];
		int bias = alignATGSite - atgThisGene;
		double[] tmpresult = null;
		if (atgDown > 0) {
			if (atgUp > 0) {
				tmpresult = new double[atgUp+atgDown];
			} else { 
				tmpresult = new double[alignATGSite + atgDown];
			}
		} else {
			if (atgUp > 0) {
				tmpresult = new double[atgUp + ATGbody];
			} else { 
				tmpresult = new double[alignATGSite + ATGbody];
			}
		}
		//��-1��������
		for (int i = 0; i < tmpresult.length; i++) {
			tmpresult[i] = filled;
		}
		//��ʽ����	
		if (atgUp < 0) {
			//�������򳤶�
			for (int i = 0; i < input.atg.length - 1; i++) {
				if (i + bias >= tmpresult.length) {
					break;
				}
				tmpresult[i+bias] = input.atg[i+1];
			}
		} else {
			if (atgThisGene > atgUp) {
				int k = 0;
				for (int i = atgThisGene - atgUp; i < input.atg.length-1; i++) {
					if (k >= tmpresult.length) {
						break;
					}
					tmpresult[k] = input.atg[i+1];
					k++;
				}
			} else {
				int k = 1;
				for (int i = atgUp - atgThisGene; i < tmpresult.length; i++) {
					if (k >= input.atg.length) {
						break;
					}
					tmpresult[i] = input.atg[k];
					k++;
				}
			}
		}
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.atg = tmpresult;
		seqInfo.atgSite = atgThisGene;
		seqInfo.seqName = input.seqName;
		seqInfo.tesToAtgSite = input.tesToAtgSite;
		seqInfo.tssToAtgSite = input.tssToAtgSite;
		seqInfo.uagToAtgSite = input.uagToAtgSite;
		
		seqInfo.seqName = input.seqName;
		return seqInfo;
	}
	
	/**
	 *	����ת¼�������ظ�ת¼����mRNAˮƽ����
	 * @param chrID
	 * @param gffGeneIsoSearch
	 * @return
	 * double[] 0: atgλ��,����λ�㣬1-���� ��tss��tes��ÿ��λ���reads��Ŀ
	 */
	protected abstract double[] getReadsInfo(String geneID, GffGeneIsoInfo gffGeneIsoInfo);
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////
}

class SeqInfo {
	/** double[] 0: atgλ��,����λ�㣬1������ ��tss��tes��ÿ��λ���reads��Ŀ*/
	public double[] atg;
	/** ATG�ӵڼ�λ��ʼ */
	public int atgSite = 0;
	public int uagToAtgSite = 0;

	public int tssToAtgSite = 0;
	public int tesToAtgSite = 0;
	public String seqName = "";
}