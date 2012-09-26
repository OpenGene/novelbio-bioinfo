package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.java.HeatChart;

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
	
	int atgAlign = 0;
	
	int atgUp = 0;
	int atgDown = 0;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setNormType(int normalType) {
		mapReads.setNormalType(normalType);
	}
	public void setAtgUp(int atgUp) {
		this.atgUp = atgUp;
	}
	public void setAtgDown(int atgDown) {
		this.atgDown = atgDown;
	}
	/**
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。
	 * @param mapFile mapping的结果文件，一般为bed格式
	 * @param tagLength 设定双端readsTag拼起来后长度的估算值，大于20才会进行设置。目前solexa双端送样长度大概是200-400bp，不用太精确 ,默认是400
	 * @param uniqReads 同一位点的重复是否仅保留一个
	 * @param startCod 开头保留几位，韩燕是3位
	 * @param cis5To3 是否挑选某一个方向的reads
	 * @param uniqMapping 是否挑选唯一比对的 
	 */
	public abstract void loadMap(String mapFile,int tagLength, boolean uniqReads, int startCod, Boolean cis5To3, boolean uniqMapping);
	
	public void drawAtgPlot(String resultFilePath, String prefix) {
		resultFilePath = FileOperate.addSep(resultFilePath);
		ArrayList<String> lsgenID = gffChrAbs.getGffHashGene().getLsNameAll();
		ArrayList<String> lsgeneIDresult = new ArrayList<String>();
		for (String string : lsgenID) {
			lsgeneIDresult.add(string.split("/")[0]);
		}
		ArrayList<SeqInfo> lsResult = getATGDensity(lsgeneIDresult, -1);
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
			
		}
	}
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void drawHeatMap(String resultFilePath, String prefix) throws Exception {
		resultFilePath = FileOperate.addSep(resultFilePath);
		ArrayList<String> lsgenID = gffChrAbs.getGffHashGene().getLsNameAll();
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
			map.saveToFile(new File(resultFilePath+prefix+"Atg.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("图片的高度像素为： "+map.getChartSize().getHeight());
		
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter(resultFilePath+prefix+"Atgmatrix.txt", true, false);
		txtReadandWrite.ExcelWrite(GeneEndDensity);
	}
	/**
	 * @param lsGeneID
	 * @param AtgUp 选取ATG上游多少bp，不包括ATG位点 -1为全选 最后对齐位点的上游多少bp，不包括对起位点
	 * @param AtgDown 选取ATG下游多少bp，不包括ATG位点 -1为全选 最后对齐位点的上游多少bp，不包括对起位点
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0
	 */
	private ArrayList<SeqInfo> getATGDensity(ArrayList<String> lsGeneID, int filled) {
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();;
		ArrayList<SeqInfo> lsAtg = new ArrayList<SeqInfo>();
		for (String string : lsGeneID) {
			SeqInfo seqInfo = new SeqInfo();
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(string);
			GffGeneIsoInfo gffGeneIsoSearch = gffDetailGene.getLongestSplit();
			if (gffGeneIsoSearch.ismRNA()) {
				seqInfo.atg = getReadsInfo(string,gffGeneIsoSearch);
				if (seqInfo.atg == null) {
					logger.error("本基因没有相应的信息："+gffGeneIsoSearch.getParentGffDetailGene().getName()+" "+ 
							gffGeneIsoSearch.getTSSsite() +"  " +gffGeneIsoSearch.getTESsite() +"  "+gffGeneIsoSearch.getName());
					continue;
				}
				seqInfo.seqName = string;
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
		for (SeqInfo seqInfo : lsdouble) {
			if (atgUp > 0) {
				seqInfo.atg = MathComput.mySpline(seqInfo.atg, 3, atgUp%3 + 1, 3);
			} else {
				seqInfo.atg = MathComput.mySpline(seqInfo.atg, 3, (AlignATGSite-1)%3, 3);
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
	 * @param input 输入数组，第一位为atg绝对位点,也就是需要对齐的位点
	 * @param alignATGSite 最长ATG的位点的绝对位置，需要对齐位点前面的长度
	 * @param ATGbody Atg下游总共多长，不包括Atg位点,需要对齐位点的下游有多长
	 * @param atgUp 选取ATG上游多少bp，不包括ATG位点 -1为全选 最后对齐位点的上游多少bo，不包括对起位点
	 * @param atgDown 选取ATG下游多少bp,不包括ATG位点。 -1为全选 选取对齐位点的下游多少bp，不包括对齐位点
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0 空位用什么填充
	 * @return
	 */
	private SeqInfo setDouble(SeqInfo input, int alignATGSite, int ATGbody, int filled ) {
		int atgOld = (int)input.atg[0];
		int bias = alignATGSite - atgOld;
		double[] tmpresult = null;
		if (atgDown > 0) {
			if (atgUp > 0) {
				tmpresult = new double[atgUp+atgDown+1];
			} else { 
				tmpresult = new double[alignATGSite+atgDown];
			}
		} else {
			if (atgUp > 0) {
				tmpresult = new double[atgUp+ATGbody+1];
			} else { 
				tmpresult = new double[alignATGSite+ATGbody];
			}
		}
		//用-1充满数组
		for (int i = 0; i < tmpresult.length; i++) {
			tmpresult[i] = filled;
		}
		//正式计算	
		if (atgUp < 0) {
			for (int i = 0; i < input.atg.length-1; i++) {
				if (i+bias >= tmpresult.length) {
					break;
				}
				tmpresult[i+bias] = input.atg[i+1];
			}
		} else {
			if (atgOld > atgUp) {
				int k = 0;
				for (int i = atgOld - atgUp - 1; i < input.atg.length-1; i++) {
					if (k >= tmpresult.length) {
						break;
					}
					tmpresult[k] = input.atg[i+1];
					k++;
				}
			} else {
				int k = 1;
				for (int i = atgUp - atgOld + 1; i < tmpresult.length; i++) {
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
		seqInfo.seqName = input.seqName;
		return seqInfo;
	}
	
	
	/**
	 * 将输入的数组重排列
	 * @param input 输入数组，第一位为atg绝对位点,也就是需要对齐的位点
	 * @param alignATGSite 最长ATG的位点的绝对位置，需要对齐位点前面的长度
	 * @param ATGbody Atg下游总共多长，不包括Atg位点,需要对齐位点的下游有多长
	 * @param atgUp 选取ATG上游多少bp，不包括ATG位点 -1为全选 最后对齐位点的上游多少bo，不包括对起位点
	 * @param atgDown 选取ATG下游多少bp,不包括ATG位点。 -1为全选 选取对齐位点的下游多少bp，不包括对齐位点
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0 空位用什么填充
	 * @return
	 */
	public double[] setDouble(double[] atg, int alignATGSite, int ATGbody ,int filled ) {
		int atgOld = (int)atg[0];
		int bias = alignATGSite - atgOld;
		double[] tmpresult = null;
		if (atgDown > 0) {
			if (atgUp > 0) 
				tmpresult = new double[atgUp+atgDown+1];
			else 
				tmpresult = new double[alignATGSite+atgDown];
		}
		else {
			if (atgUp > 0) 
				tmpresult = new double[atgUp+ATGbody+1];
			else 
				tmpresult = new double[alignATGSite+ATGbody];			
		}
		//用-1充满数组
		for (int i = 0; i < tmpresult.length; i++) {
			tmpresult[i] = filled;
		}
		//正式计算	
		if (atgUp < 0) {
			for (int i = 0; i < atg.length-1; i++) {
				if (i+bias >= tmpresult.length) {
					break;
				}
				tmpresult[i+bias] = atg[i+1];
			}
		}
		else {
			if (atgOld > atgUp) {
				int k = 0;
				for (int i = atgOld - atgUp - 1; i < atg.length-1; i++) {
					 if (k >= tmpresult.length) {
							break;
					 }
					 tmpresult[k] = atg[i+1];
					 k++;
				}
			}
			else {
				int k = 1;
				for (int i = atgUp - atgOld + 1; i < tmpresult.length; i++) {
					if (k >= atg.length) {
						break;
					}
					tmpresult[i] = atg[k];
					k++;
				}
			}
		}
		return tmpresult;
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
	public String seqName = "";
}