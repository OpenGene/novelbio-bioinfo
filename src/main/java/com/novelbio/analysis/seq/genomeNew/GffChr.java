package com.novelbio.analysis.seq.genomeNew;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.Species;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashCG;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGenePlant;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashRepeat;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneUCSC;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.java.HeatChart;






 
/**
 * 其中的ChrFa读取时候，必须将每行的换行符限定为"\n",有小工具能用
 * @author zong0jie
 *
 */
public abstract class GffChr {
	

private static Logger logger = Logger.getLogger(GffChr.class);
//////////////////////////////////////////////////参数设定/////////////////////////////////////////////////////////

	/**
	 * 设定基因的转录起点TSS上游长度，默认为3000bp
	 */
	static int UpStreamTSSbp=3000;
	
	/**
	 * 设定基因的转录起点下游长度，默认为2000bp
	 */
	static int DownStreamTssbp=2000;
	/**
	 * 设定基因的转录起点TSS上游长度，默认为3000bp
	 * 最后统计时，在TSS上游之内的peak会计算进基因内
	 */
	public void setDownStreamTssbp(int downStreamTssbp) {
		DownStreamTssbp = downStreamTssbp;
	}
	
	/**
	 * 设定基因的转录起点TSS上游长度，默认为3000bp
	 * 最后统计时，在TSS上游之内的peak会计算进基因内
	 */
	public void setUpstreamTSSbp(int upstreamTSSbp) {
		UpStreamTSSbp = upstreamTSSbp;
	}
	
	/**
	 * 用来校正reads数量的参数，因为如果tss附近reads直接除以ReadsNum会很小，不方便后续画图
	 * 这时候最好能够乘上一个比较大的数字做矫正，这个数字应该比较接近测序量，这里设定为一百万
	 */
	static int fold=1000000;
	
	/**
	 * 设定基因结尾向外延伸的长度，默认为100bp
	 * 就是说将基因结束点向后延伸100bp，认为是3’UTR
	 * 那么在统计peak区域的时候，如果这段区域里面没有被peak所覆盖，则不统计该区域内reads的情况
	 */
	static int GeneEnd3UTR = 100;
	/**
	 * 设定基因结尾向外延伸的长度，默认为100bp
	 * 就是说将基因结束点向后延伸100bp，认为是3’UTR,最后统计时，在基因尾部之内的peak会计算进基因内 
	 * 那么在统计peak区域的时候，如果这段区域里面没有被peak所覆盖，则不统计该区域内reads的情况
	 */
	public void setGeneEnd3UTR(int geneEnd3UTR) {
		GeneEnd3UTR = geneEnd3UTR;
	}
	
	/**
	 * 设定motif的花式，默认为CANNTG
	 */
	public String motifregex = "CA\\w{2}TG";
	
	
	////////////////////////////////////////////用到的类/////////////////////////////////////////////////////////
 
	/**
	 * 本类用到的一个gffHash，用来读取gff文件
	 */
	protected GffHash gffHash;
	/**
	 * 本类用到的一个mapreads，用来处理map文件
	 */
	protected MapReads mapReads=null;
	/**
	 * 本类用到的一个ChrStringHash，用来读取序列
	 */
	protected ChrStringHash chrStringHash=null;
	/**
	 * 读取refseq序列
	 */
	protected SeqFastaHash seqFastaHash = null;
	/**
	 * mapping到的reads的数量
	 */
	protected long readsNum = 0;
	/**
	 * Gff文件
	 */
	String GffFile = "";
	
	
	/**
	 * 指定相应的待实例化Gffhash子类
	 * 读取相应的gff文件
	 * @param gffClass 待实例化的Gffhash子类，只能有 "TIGR","TAIR","CG","UCSC","Repeat"这几种<br>
	 * 以后根据新添加的gffhash子类这里要继续添加<br>
	 * @param Gfffilename
	 * @throws Exception 
	 */
	public GffChr(String gffClass,String GffFile, String ChrFilePath, int taxID)
	{
		if (FileOperate.isFile(GffFile)) {
			 try {
					loadGff(gffClass, GffFile, taxID);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (FileOperate.isFile(ChrFilePath)) {
			loadSeq(ChrFilePath);
		}
		if (FileOperate.isFileDirectory(ChrFilePath)) {
			loadChr(ChrFilePath);
		}
	}

	///////////////////////////////////////////准备工作 Loading 方   法////////////////////////////////////////////////////////////
	/**
	 * 指定相应的待实例化Gffhash子类
	 * 读取相应的gff文件
	 * @param gffClass 待实例化的Gffhash子类，只能有 "TIGR","TAIR","CG","UCSC","Repeat"这几种<br>
	 * 以后根据新添加的gffhash子类这里要继续添加<br>
	 * @param Gfffilename
	 * @throws Exception 
	 */
	 private void loadGff(String gffClass,String Gfffilename, int taxID) throws Exception
	 {
		 if (gffClass.equals("TIGR")) {
			 gffHash=new GffHashGenePlant(Species.RICE);
		 }
		 if (gffClass.equals("TAIR")) {
			 gffHash=new GffHashGenePlant(Species.ARABIDOPSIS);
		 }
		 else if (gffClass.equals("CG")) {
			 gffHash=new GffHashCG();
		 }
		 else if (gffClass.equals("UCSC")) {
			 gffHash=new GffHashGeneUCSC(taxID);
		 }
		 else if (gffClass.equals("Repeat")) {
			 gffHash=new GffHashRepeat();
		 }
		gffHash.ReadGffarray(Gfffilename);
	 }
	
	/**
	 * 设定含有序列信息的文件夹，文件夹内应该每个染色体一个fasta文件的序列。最后生成一个哈希表-序列信息<br>
	 * <b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * 具体信息见ChrStringHash类
	 * @param Chrfilename
	 */
	private void loadChr(String ChrFilePath) 
	{
	   try { chrStringHash = new ChrStringHash(ChrFilePath); } catch (Exception e) {e.printStackTrace();} 
	}
	/**
	 * 设定含有序列信息的文件夹，文件夹内应该每个染色体一个fasta文件的序列。最后生成一个哈希表-序列信息<br>
	 * <b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * 具体信息见ChrStringHash类
	 * @param Chrfilename
	 */
	private void loadSeq(String SeqPath) 
	{
		seqFastaHash = new SeqFastaHash(SeqPath);
	}

	
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void drawHeatMap(String resultFilePath, String prefix, int AtgUp, int AtgDown,int normalizedType) throws Exception {
		resultFilePath = FileOperate.addSep(resultFilePath);
		ArrayList<String> lsgenID = gffHash.getLOCChrHashIDList();
		ArrayList<String> lsgeneIDresult = new ArrayList<String>();
		for (String string : lsgenID) {
			lsgeneIDresult.add(string.split("/")[0]);
		}
		ArrayList<SeqInfo> lsResult = getATGDensity(lsgeneIDresult, AtgUp,  AtgDown, -1, normalizedType);
		if (AtgUp <= 0) {
			AtgUp = atgAlign;
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
		HeatChart map = new HeatChart(GeneEndDensity2,0,40);
		map.setTitle("ATGsit: "+ AtgUp+1 );
		map.setXAxisLabel("X Axis");
		map.setYAxisLabel("Y Axis");
//		int[] aa = new String[]{"a","b","c","d","e","f"};
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
		txtReadandWrite.ExcelWrite(GeneEndDensity, "\t");
	}
	
	int atgAlign = 0;
	int maxUTR5 = 0;
	/**
	 * @param lsGeneID
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0
	 */
	private ArrayList<SeqInfo> getATGDensity(ArrayList<String> lsGeneID,int AtgUp, int AtgDown, int filled, int normlizType) {
		GffHashGeneAbs gffHashGene = (GffHashGeneAbs)gffHash;
		ArrayList<SeqInfo> lsAtg = new ArrayList<SeqInfo>();
		for (String string : lsGeneID) {
			SeqInfo seqInfo = new SeqInfo();
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(string);
			GffGeneIsoInfo gffGeneIsoSearch = gffDetailGene.getLongestSplit();
			if (gffGeneIsoSearch.ismRNA()) {
				seqInfo.atg = getReadsInfo(string,gffGeneIsoSearch,normlizType);
				if (seqInfo.atg == null) {
					logger.error("本基因没有相应的信息："+gffGeneIsoSearch.getThisGffDetailGene().getChrID()+" "+ 
							gffGeneIsoSearch.getTSSsite() +"  " +gffGeneIsoSearch.getTESsite() +"  "+gffGeneIsoSearch.getIsoName());
					continue;
				}
				seqInfo.seqName = string;
				lsAtg.add(seqInfo);
			}
		}
		return setMatrix(lsAtg, AtgUp, AtgDown, filled);
	}
	/**
	 * 仅仅针对韩燕做的分析，按照5UTR的长度进行排序，从小到大排列，然后
	 * @param lsAtg key 5UTR的长度，value，总共序列的长度，第一位为atg绝对位点
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0
	 */
	private ArrayList<SeqInfo> setMatrix(ArrayList<SeqInfo> lsAtg, int AtgUp, int AtgDown, int filled) {
		int maxGeneBody = 0;
		atgAlign = getAtgAlign(lsAtg);//要用atg做alignment的，内部还进行了排序
		for (SeqInfo ds : lsAtg) {
			if (ds.atg.length-1 - ds.atg[0] > maxGeneBody) {
				maxGeneBody = (int) (ds.atg.length-1 - ds.atg[0]);
			}
		}
		ArrayList<SeqInfo> lsdouble = new ArrayList<SeqInfo>();
		for (SeqInfo ds : lsAtg) {
			SeqInfo tmpResult = setDouble(ds, atgAlign, maxGeneBody, AtgUp, AtgDown, filled);
			lsdouble.add(tmpResult);
		}
		return lsdouble;
	}
	/**
	 * 获得最大atg位点的值
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
				}
				else if (o1.atg[0] == o2.atg[0]) {
					return 0;
				}
				else {
					return -1;
				}
			}
		});
		return (int) lsAtg.get(0).atg[0];
	}
	
	
	/**
	 * 将输入的数组重排列
	 * @param input 输入数组，第一位为atg绝对位点,也就是需要对齐的位点
	 * @param alignATGSite 最长ATG的位点的前一位，需要对齐位点前面的长度--最长的那个有多长
	 * @param ATGbody Atg下游总共多长，不包括Atg位点,需要对齐位点的下游有多长
	 * @param atgUp 选取ATG上游多少bp，不包括ATG位点 -1为全选 最后对齐位点的上游多少bo，不包括对起位点
	 * @param alignDown 选取ATG下游多少bp,不包括ATG位点。 -1为全选 选取对齐位点的下游多少bp，不包括对齐位点
	 * @param filled 空位用什么填充，如果是heatmap，考虑-1，如果是叠加，考虑0 空位用什么填充
	 * @return
	 */
	public static SeqInfo setDouble(SeqInfo input, int alignATGSite, int ATGbody ,int atgUp, int alignDown,int filled ) {
		int atgOld = (int)input.atg[0];
		int bias = alignATGSite - atgOld;
		double[] tmpresult = null;
		if (alignDown > 0) {
			if (atgUp > 0) 
				tmpresult = new double[atgUp+alignDown+1];
			else 
				tmpresult = new double[alignATGSite+alignDown+1];
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
			for (int i = 0; i < input.atg.length-1; i++) {
				if (i+bias >= tmpresult.length) {
					break;
				}
				tmpresult[i+bias] = input.atg[i+1];
			}
		}
		else {
			if (atgOld > atgUp) {
				int k = 0;
				for (int i = atgOld - atgUp; i < input.atg.length-1; i++) {
					 if (k >= tmpresult.length) {
							break;
					 }
					 tmpresult[k] = input.atg[i+1];
					 k++;
				}
			}
			else {
				int k = 1;
				for (int i = atgUp - atgOld; i < tmpresult.length; i++) {
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
	 *	给定转录本，返回该转录本的mRNA水平坐标
	 * @param chrID
	 * @param gffGeneIsoSearch
	 * @return
	 * double[] 0: atg位点,绝对位点，1-结束 从tss到tes的每个位点的reads数目
	 */
	private double[] getReadsInfo(String geneID, GffGeneIsoInfo gffGeneIsoSearch, int normalizeType) {
		int geneLength = 0;
		try {
			geneLength = seqFastaHash.getHashChrLength().get(geneID.toLowerCase()).intValue();
		} catch (Exception e) {
			return null;
		}
		
		double[] iso = mapReads.getRengeInfo(1, geneID.toLowerCase(), 1, geneLength, 0);
		if (iso == null) {
			return null;
		}
		mapReads.normDouble(iso, normalizeType);
		double[] isoResult = new double[iso.length+1];
		isoResult[0] = gffGeneIsoSearch.getLocDistance(gffGeneIsoSearch.getATGSsite(), gffGeneIsoSearch.getTSSsite());
		for (int i = 0; i < iso.length; i++) {
			isoResult[i+1] = iso[i];
		}
		return isoResult;
	}

 
	
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
class SeqInfo
{
	/**
	 * double[] 0: atg位点,绝对位点，1-结束 从tss到tes的每个位点的reads数目
	 */
	public double[] atg;
	public String seqName = "";
}
