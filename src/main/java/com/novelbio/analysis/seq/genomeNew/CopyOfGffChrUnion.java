package com.novelbio.analysis.seq.genomeNew;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.generalConf.Species;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoSearch;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashCG;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashPeak;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashPlantGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashRepeat;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashUCSCgene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataStructure.MathComput;






 
/**
 * 其中的ChrFa读取时候，必须将每行的换行符限定为"\n",有小工具能用
 * @author zong0jie
 *
 */
public class CopyOfGffChrUnion {
	


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
	 * mapping到的reads的数量
	 */
	protected long readsNum = 0;
	
	
	///////////////////////////////////////////准备工作 Loading 方   法////////////////////////////////////////////////////////////
	/**
	 * 指定相应的待实例化Gffhash子类
	 * 读取相应的gff文件
	 * @param gffClass 待实例化的Gffhash子类，只能有 "TIGR","TAIR","CG","UCSC","Repeat"这几种<br>
	 * 以后根据新添加的gffhash子类这里要继续添加<br>
	 * @param Gfffilename
	 * @throws Exception 
	 */
	 public void loadGff(String gffClass,String Gfffilename) throws Exception
	 {
		 if (gffClass.equals("TIGR")) {
			 gffHash=new GffHashPlantGene(Gfffilename, Species.RICE);
		 }
		 if (gffClass.equals("TAIR")) {
			 gffHash=new GffHashPlantGene(Gfffilename,  Species.ARABIDOPSIS);
		 }
		 else if (gffClass.equals("CG")) {
			 gffHash=new GffHashCG(Gfffilename);
		 }
		 else if (gffClass.equals("UCSC")) {
			 gffHash=new GffHashUCSCgene(Gfffilename);
		 }
		 else if (gffClass.equals("Repeat")) {
			 gffHash=new GffHashRepeat(Gfffilename);
		 }
		
	 }
	
	/**
	 * 设定含有序列信息的文件夹，文件夹内应该每个染色体一个fasta文件的序列。最后生成一个哈希表-序列信息<br>
	 * <b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * 具体信息见ChrStringHash类
	 * @param Chrfilename
	 */
	public void loadChr(String ChrFilePath) 
	{
	   try { chrStringHash = new ChrStringHash(ChrFilePath); } catch (Exception e) {e.printStackTrace();} 
	}
	
	/**
	  * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。
	 * @param mapFile mapping的结果文件，一般为bed格式
	 * @param chrFilePath 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * @param colChrID ChrID在第几列，从1开始
	 * @param colStartNum mapping起点在第几列，从1开始
	 * @param colEndNum mapping终点在第几列，从1开始
	 * @param invNum 每隔多少位计数
	 * @param tagLength 设定双端readsTag拼起来后长度的估算值，大于20才会进行设置。目前solexa双端送样长度大概是200-400bp，不用太精确 ,默认是400
	 */
	public void loadMap(String mapFile,String chrFilePath,int invNum,int tagLength, boolean uniqReads) 
	{
		mapReads=new MapReads(invNum, chrFilePath, mapFile);
		try {
			if (tagLength > 20) {
				mapReads.setTagLength(tagLength);
			}
			readsNum = mapReads.ReadMapFile(uniqReads);
		} catch (Exception e) {	e.printStackTrace();	}
	}
	

	
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @param lsGeneID
	 */
	public void getATGDensity(ArrayList<String> lsGeneID) {
		GffHashGene gffHashGene = (GffHashGene)gffHash;
		TreeMap<Integer, double[]> treeATG = new TreeMap<Integer, double[]>();
		for (String string : lsGeneID) {
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(string);
			gffDetailGene.
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getCoordSearchLongest();
			gffGeneIsoInfo.getATGSsite();
			
			
		}
	}
	
}
