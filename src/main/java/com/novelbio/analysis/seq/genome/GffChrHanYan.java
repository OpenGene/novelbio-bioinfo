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
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.mappingOperate.EnumMapNormalizeType;
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
 * 其中的ChrFa读取时候，必须将每行的换行符限定为"\n",有小工具能用
 * 
 * @author zong0jie
 * 
 */
public abstract class GffChrHanYan {
private static Logger logger = Logger.getLogger(GffChrHanYan.class);
	GffChrAbs gffChrAbs;
	MapReads mapReads;
	/** 最长的那个atg的位置 */
	int atgAlign = 0;
	/** Atg上游位点，不包含ATG */
	int atgUp = 300;
	/** Atg下游位点，包含ATG */
	int atgDown = 3000;
	/** 保存geneID到accID，主要用于查找map里面的具体基因 
	 * key 小写
	 * */
	HashMap<String, String> mapGeneID2AccID;
	/** accID到seqInfo 
	 * key 小写
	 * */
	HashMap<String, SeqInfo> mapAccID2SeqInfo;
	
	public static void main(String[] args) {
		String mapFile = "/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/RAP3h_filtered_sorted.bed";
		String resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		GffChrUnionHanYanRefSeq gffChrHanYan = new GffChrUnionHanYanRefSeq();
		GffChrAbs gffChrAbs = new GffChrAbs(10090);
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plotRAP3h_300-3000");
		gffChrHanYan.drawGeneAll("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plotRAP3h_300-3000/RAP3h_");

		mapFile = "/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/20PBS_filtered_sorted.bed";
		resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		gffChrHanYan = new GffChrUnionHanYanRefSeq();
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plot20PBS_300-3000");
		gffChrHanYan.drawGeneAll("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plot20PBS_300-3000/20PBS_");
		
		mapFile = "/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/LY3h_filtered_sorted.bed";
		resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		gffChrHanYan = new GffChrUnionHanYanRefSeq();
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plotLY3h_300-3000");
		gffChrHanYan.drawGeneAll("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plotLY3h_300-3000/LY3h_");
		
		mapFile = "/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/TSC2_KO_filtered_sorted.bed";
		resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		gffChrHanYan = new GffChrUnionHanYanRefSeq();
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plotTSC2_KO_300-3000");
		gffChrHanYan.drawGeneAll("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plotTSC2_KO_300-3000/TSC2_KO_");
		
		mapFile = "/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/TSC2_WT_filtered_sorted.bed";
		resultFileOut = FileOperate.changeFileSuffix(mapFile, "_info", "txt");
		gffChrHanYan = new GffChrUnionHanYanRefSeq();
		gffChrHanYan.setGffChrAbs(gffChrAbs);
		gffChrHanYan.setRefSeq("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/refseq/refMRNA.fa");
		gffChrHanYan.loadMapFile(mapFile, 200, false, 3, false);
		gffChrHanYan.drawAtgPlot(resultFileOut);
		FileOperate.createFolders("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plotTSC2_WT_300-3000");
		gffChrHanYan.drawGeneAll("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/mapping/plotTSC2_WT_300-3000/TSC2_WT_");
		
		
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
//		gffChrSeq.setOutPutFile("/media/winF/NBC/Project/Project_HY/TSC2_2nd_Seq/refseq/refMRNA.fa");
//		gffChrSeq.run();
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setNormType(EnumMapNormalizeType normalType) {
		mapReads.setNormalType(normalType);
	}
	/** 默认获取atg上游300bp的位点 */
	public void setAtgUp(int atgUp) {
		this.atgUp = atgUp;
	}
	/** 默认获取atg下游3000bp的位点 */
	public void setAtgDown(int atgDown) {
		this.atgDown = atgDown;
	}
	
	public void loadMapFile(String mapFile, int tagLength, boolean uniqReads, int startCod, boolean uniqMapping) {
		mapGeneID2AccID = null;
		mapAccID2SeqInfo = null;
		loadMap(mapFile, tagLength, uniqReads, startCod, uniqMapping);
	}
	/**
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。
	 * @param mapFile mapping的结果文件，一般为bed格式
	 * @param tagLength 设定双端readsTag拼起来后长度的估算值，大于20才会进行设置。目前solexa双端送样长度大概是200-400bp，不用太精确 ,默认是400
	 * @param uniqReads 同一位点的重复是否仅保留一个
	 * @param startCod 开头保留几位，韩燕是3位
	 * @param uniqMapping 是否挑选唯一比对的 
	 */
	protected abstract void loadMap(String mapFile, int tagLength, boolean uniqReads, int startCod, boolean uniqMapping);
	
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
		
		PlotScatter plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
		plotScatter.setAxisX(xMin, xMax);
		plotScatter.setAxisY(0, yMax);

		DotStyle dotStyle = new DotStyle();
		Paint colorGradient = DotStyle.getGridentColor(GraphicsUtils.deriveDarker(Color.blue), Color.blue);
		dotStyle.setColor(colorGradient);
		dotStyle.setStyle(DotStyle.STYLE_AREA);
		plotScatter.addXY(x, y, dotStyle);
		
		//////////////////添加边框///////////////////////////////
		DotStyle dotStyleAtg = new DotStyle();
		dotStyleAtg.setStyle(DotStyle.STYLE_LINE);
		dotStyleAtg.setColor(Color.RED);
		dotStyleAtg.setSize(DotStyle.SIZE_B);
		//0点就是atg
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
		
		//0点就是atg
		double[] xTss= new double[]{seqInfo.tssToAtgSite, seqInfo.tssToAtgSite};
		double[] yTss = new double[]{0, yMax};
		plotScatter.addXY(xTss, yTss, dotStyleTss);
		
		DotStyle dotStyleTes = new DotStyle();
		dotStyleTes.setStyle(DotStyle.STYLE_LINE);
		dotStyleTes.setColor(Color.green);
		dotStyleTes.setSize(DotStyle.SIZE_B);
		//0点就是atg
		double[] xTes= new double[]{seqInfo.tesToAtgSite, seqInfo.tesToAtgSite};
		double[] yTes = new double[]{0, yMax};
		plotScatter.addXY(xTes, yTes, dotStyleTes);
		
		
		
		
		
		plotScatter.setBg(Color.WHITE);
		plotScatter.setAlpha(false);
		//坐标轴mapping
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
	 * 给定基因名, 返回seqInfo
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
			accID = mapGeneID2AccID.get(geneID.getGeneUniID().toLowerCase());
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
			mapGeneID2AccID.put(geneID.getGeneUniID().toLowerCase(), string);
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
		//带基因名字
		String[][] GeneEndDensity = new String[lsResult.size()][lsResult.get(0).atg.length+1];
		for (int i = 0; i < GeneEndDensity.length; i++) {
			for (int j = 1; j < GeneEndDensity[0].length; j++) {
				GeneEndDensity[i][j] = lsResult.get(i).atg[j-1] + "";
			}
		}
		for (int i = 0; i < GeneEndDensity.length; i++) {
			GeneEndDensity[i][0] = lsResult.get(i).seqName;
		}
		//不带基因名字
		double[][] GeneEndDensity2 = new double[lsResult.size()][lsResult.get(0).atg.length];
		for (int i = 0; i < GeneEndDensity2.length; i++) {
			for (int j = 0; j < GeneEndDensity2[0].length; j++) {
				GeneEndDensity2[i][j] = lsResult.get(i).atg[j];
			}
		}
		
		ArrayList<double[]> lsAxisX2AxisY = new ArrayList<double[]>();
		for (SeqInfo seqInfo : lsResult) {
			//遍历每个exon的每个位点
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
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		System.out.println("进行分析的基因数目：" + GeneEndDensity.length);
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
			map.saveToFile(resultFilePath+prefix+"Atg.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("图片的高度像素为： "+map.getChartSize().getHeight());
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(resultFilePath+prefix+"Atgmatrix.txt", true);
		List<String[]> lsThisResult = new ArrayList<String[]>();
		for (int i = 0; i < GeneEndDensity.length; i++) {
			lsThisResult.add(GeneEndDensity[i]);
		}
		txtReadandWrite.ExcelWrite(lsThisResult);
		txtReadandWrite.close();
	}
	protected abstract ArrayList<String> getAllGeneName();

	/**
	 * @param lsGeneID
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0
	 */
	private ArrayList<SeqInfo> getATGDensity(ArrayList<String> lsGeneID, int filled) {
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();;
		ArrayList<SeqInfo> lsAtg = new ArrayList<SeqInfo>();
		for (String string : lsGeneID) {
			SeqInfo seqInfo = new SeqInfo();
			GffGeneIsoInfo gffGeneIsoSearch = gffHashGene.searchISO(string);
			if (gffGeneIsoSearch.ismRNA()) {
				seqInfo.atg = getReadsInfo(string,gffGeneIsoSearch);
				if (seqInfo.atg == null) {
					logger.error("本基因没有相应的信息："+gffGeneIsoSearch.getParentGffDetailGene().getNameSingle()+" "+ 
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
	 * 仅仅针对韩燕做的分析，按照5UTR的长度进行排序，从小到大排列，然后
	 * @param lsAtg key 5UTR的长度，value，总共序列的长度，第一位为atg绝对位点
	 * @param AtgUp 选取ATG上游多少bp，不包括ATG位点 -1为全选 最后对齐位点的上游多少bp，不包括对起位点
	 * @param AtgDown 选取ATG下游多少bp,不包括ATG位点。 -1为全选 选取对齐位点的下游多少bp，不包括对齐位点
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0
	 */
	protected ArrayList<SeqInfo> setMatrix(ArrayList<SeqInfo> lsAtg, int filled) {
		int maxGeneBody = 0;
		//获得最长的UTR长度
		atgAlign = getAtgAlign(lsAtg);//要用atg做alignment的，内部还进行了排序
		//获得最长的ATG下游长度,不包括ATG位点
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
			//此时的SeqInfo第一位就是实际的第一位，不是atgsite了
			SeqInfo tmpResult = setDouble(ds, atgAlign, maxGeneBody, filled);
			lsdouble.add(tmpResult);
		}
		//////////////////////
		combineLoc(lsdouble,atgAlign);
		//////////////////////
		return lsdouble;
	}
	/**
	 * 将三个碱基合并为1个coding
	 * @param AtgUp 选取ATG上游多少bp，不包括ATG位点 -1为全选 最后对齐位点的上游多少bo，不包括对起位点
	 * @param AlignATGSite 最长ATG的位点的绝对位置，需要对齐位点前面的长度
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
	 * 获得全基因祖上，atg距离UTR5起点的最远距离
	 * @param lsAtg value，总共序列的长度，第一位为atg绝对位点
	 * @return
	 */
	public int getAtgAlign( ArrayList<SeqInfo> lsAtg) {
		//从大到小排列
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
	 * 将输入的数组重排列
	 * @param input 输入数组，第一位为atg绝对位点,也就是需要对齐的位点，从0开始记数
	 * @param alignATGSite 最长ATG的位点的绝对位置，需要对齐位点前面的长度，不包括atg位点
	 * @param ATGbody Atg下游总共多长，包括Atg位点,需要对齐位点的下游有多长
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0 空位用什么填充
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
		//用-1充满数组
		for (int i = 0; i < tmpresult.length; i++) {
			tmpresult[i] = filled;
		}
		//正式计算	
		if (atgUp < 0) {
			//遍历基因长度
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
	 *	给定转录本，返回该转录本的mRNA水平坐标
	 * @param chrID
	 * @param gffGeneIsoSearch
	 * @return
	 * double[] 0: atg位点,绝对位点，1-结束 从tss到tes的每个位点的reads数目
	 */
	protected abstract double[] getReadsInfo(String geneID, GffGeneIsoInfo gffGeneIsoInfo);
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////
}

class SeqInfo {
	/** double[] 0: atg位点,绝对位点，1到结束 从tss到tes的每个位点的reads数目*/
	public double[] atg;
	/** ATG从第几位开始 */
	public int atgSite = 0;
	public int uagToAtgSite = 0;

	public int tssToAtgSite = 0;
	public int tesToAtgSite = 0;
	public String seqName = "";
}
