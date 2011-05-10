package com.novelBio.base.genome;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MaskFormatter;

import com.novelBio.base.dataStructure.MathComput;
import com.novelBio.base.genome.getChrSequence.ChrSearch;
import com.novelBio.base.genome.getChrSequence.ChrStringHash;
import com.novelBio.base.genome.gffOperate.GffCodInfo;
import com.novelBio.base.genome.gffOperate.GffCodInfoUCSCgene;
import com.novelBio.base.genome.gffOperate.GffDetail;
import com.novelBio.base.genome.gffOperate.GffDetailUCSCgene;
import com.novelBio.base.genome.gffOperate.GffHash;
import com.novelBio.base.genome.gffOperate.GffHashCG;
import com.novelBio.base.genome.gffOperate.GffHashGene;
import com.novelBio.base.genome.gffOperate.GffHashPeak;
import com.novelBio.base.genome.gffOperate.GffHashPlantGene;
import com.novelBio.base.genome.gffOperate.GffHashRepeat;
import com.novelBio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelBio.base.genome.gffOperate.Gffsearch;
import com.novelBio.base.genome.gffOperate.GffsearchCG;
import com.novelBio.base.genome.gffOperate.GffsearchGene;
import com.novelBio.base.genome.gffOperate.GffsearchPeak;
import com.novelBio.base.genome.gffOperate.GffsearchRepeat;
import com.novelBio.base.genome.gffOperate.GffsearchUCSCgene;
import com.novelBio.base.genome.mappingOperate.MapReads;
import com.novelbio.generalConf.NovelBioConst;






 
/**
 * 其中的ChrFa读取时候，必须将每行的换行符限定为"\n",有小工具能用
 * @author zong0jie
 *
 */
public class GffChrUnion {
	


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
	public void setDownStreamTssbp(int DownStreamTssbp) {
		this.DownStreamTssbp=DownStreamTssbp;
	}
	
	/**
	 * 设定基因的转录起点TSS上游长度，默认为3000bp
	 * 最后统计时，在TSS上游之内的peak会计算进基因内
	 */
	public void setUpstreamTSSbp(int upstreamTSSbp) {
		UpStreamTSSbp=upstreamTSSbp;
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
	static int GeneEnd3UTR=100;
	/**
	 * 设定基因结尾向外延伸的长度，默认为100bp
	 * 就是说将基因结束点向后延伸100bp，认为是3’UTR,最后统计时，在基因尾部之内的peak会计算进基因内 
	 * 那么在统计peak区域的时候，如果这段区域里面没有被peak所覆盖，则不统计该区域内reads的情况
	 */
	public void setGeneEnd3UTR(int geneEnd3UTR) {
		GeneEnd3UTR=geneEnd3UTR;
	}
	
	/**
	 * 设定motif的花式，默认为CANNTG
	 */
	public String motifregex="CA\\w{2}TG";
	
	
	////////////////////////////////////////////用到的类/////////////////////////////////////////////////////////
 
	/**
	 * 本类用到的一个gffHash，用来读取gff文件
	 */
	protected GffHash gffHash;
	/**
	 * 本类用到的一个gffsearch，用来查找gffHash类
	 */
	protected Gffsearch gffSearch=null;
	/**
	 * 本类用到的一个mapreads，用来处理map文件
	 */
	protected MapReads mapReads=null;
	
	/**
	 * mapping到的reads的数量
	 */
	protected long readsNum = 0;
	
	
	///////////////////////////////////////////准备工作 Loading 方   法////////////////////////////////////////////////////////////
	/**
	 * 指定相应的待实例化Gffhash子类
	 * 读取相应的gff文件
	 * @param gffClass 待实例化的Gffhash子类，只能有 "TIGR","TAIR","CG","UCSC","Peak","Repeat"这几种<br>
	 * 以后根据新添加的gffhash子类这里要继续添加<br>
	 * @param Gfffilename
	 */
	 public void loadGff(String gffClass,String Gfffilename)
	 {
		 if (gffClass.equals("TIGR")) 
		 {
			 gffHash=new GffHashPlantGene();
			 //引用传递，这里改了gffHash也会改变
			GffHashPlantGene gffHash2 = (GffHashPlantGene)gffHash;
			gffHash2.GeneName= "LOC_Os\\d{2}g\\d{5}";
			gffHash2.splitmRNA = "(?<=LOC_Os\\d{2}g\\d{5}\\.)\\d";
			 gffSearch=new GffsearchUCSCgene();
		 }
		 if (gffClass.equals("TAIR")) 
		 {
			 gffHash=new GffHashPlantGene();
			 gffSearch=new GffsearchUCSCgene();
		 }
		 else if (gffClass.equals("CG")) 
		 {
			 gffHash=new GffHashCG();
			 gffSearch=new GffsearchCG();
		 }
		 else if (gffClass.equals("UCSC")) 
		 {
			 gffSearch=new GffsearchUCSCgene();
			 gffHash=new GffHashUCSCgene();
		 }
		 else if (gffClass.equals("Peak")) 
		 {
			 gffHash=new GffHashPeak();
			 gffSearch=new GffsearchPeak();
		 }
		 else if (gffClass.equals("Repeat")) 
		 {
			 gffHash=new GffHashRepeat();
			 gffSearch=new GffsearchRepeat();
		 }
		 try {
			gffHash.ReadGffarray(Gfffilename);
		} catch (Exception e) {e.printStackTrace();
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
	    try {    ChrStringHash.setChrFilePath(ChrFilePath);  } catch (Exception e) {  e.printStackTrace();   }	
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
	public void loadMap(String mapFile,String chrFilePath,String sep,int colChrID,int colStartNum,int colEndNum,int invNum,int tagLength) 
	{
		mapReads=new MapReads();
		try {
			readsNum = mapReads.ReadMapFile(mapFile, chrFilePath, sep, colChrID, colStartNum, colEndNum, invNum);
			if (tagLength>20) {
				mapReads.setTagLength(tagLength);
			}
			
		} catch (Exception e) {	e.printStackTrace();	}
	}
	

	////////////////////////////////////提   取   序   列//////////////////////////////////////////////////////////////////////////
	/**
	 * 输入Item和上游长度，返回获得的上游序列与item起点的距离，指定是否要考虑序列正反向
	 * @param LOCID item名，各个gffHash有不同的LOCID名
	 * @param length
	 * @param considerDirection 考虑正反向
	 * @param direction 如果不考虑正反向，那么true返回全局正向,false返回全局反向。否则返回该基因正向/反向。
	 * 如果考虑正反向，那么true返回该基因正向，false返回该基因反向
	 * @return
	 */
	public String getUpItemSeq(String Item,int length,boolean considerDirection,boolean direction)
	{
		GffDetail locinfo = Gffsearch.LOCsearch(Item, gffHash);
		if(locinfo==null)
			 return null;
			int StartNum=0;
			if(considerDirection)//考虑正反向，返回的都是本基因的正向
			{
			  if(locinfo.cis5to3)
			  {
				  StartNum=locinfo.numberstart;
				  return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum-length, StartNum);
			  }
			  else 
			  {
				  StartNum=locinfo.numberend;	
				  return	ChrSearch.getSeq(!direction,locinfo.ChrID, StartNum, StartNum+length);
			  }
			}
			else //不考虑正反向，返回的就是默认正向或反向
			{
				if(locinfo.cis5to3)
				{
					StartNum=locinfo.numberstart;
					return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum-length, StartNum);
				}
				else 
				{
					StartNum=locinfo.numberend;	
					return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum, StartNum+length);
				}
			}
	}
	/**
	 * 输入geneID和上游长度，返回获得的上游序列与gene的Tss起点的距离，指定是否要考虑序列正反向。
	 * 注意gene一定是GffDetailUCSCgene，目前TIGR，TAIR和UCSC都是这个类
	 * @param LOCID item名，注意该LOCID必须在gff文件中出现，所以如果给定gene symbol的话，就要考虑数据库再建一个表，专门用于symobl和gffID的对应
	 * @param length
	 * @param considerDirection 考虑正反向
	 * @param direction 如果不考虑正反向，那么true返回全局正向,false返回全局反向。否则返回该基因正向/反向。
	 * @param GffClass 目前有TIGR,TAIR,UCSC这三个
	 * 如果考虑正反向，那么true返回该基因正向，false返回该基因反向
	 * @return
	 */
	public String getUpGenSeq(String LOCID,int length,boolean considerDirection,boolean direction,String GffClass)
	{
		if (GffClass.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR)) {
			return getUpItemSeq(LOCID, length, considerDirection, direction);
		}
		GffDetailUCSCgene locinfo = (GffDetailUCSCgene)Gffsearch.LOCsearch(LOCID, gffHash);
		if(locinfo==null)
			 return null;
		int StartNum=0;
		if(considerDirection)//考虑正反向，返回的都是本基因的正向
		{
			if(locinfo.getCis5to3(LOCID))
			{
				StartNum = locinfo.getExonlist(LOCID).get(2);
				return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum-length, StartNum);
			}
			else 
			{
				ArrayList<Integer> lsExon = locinfo.getExonlist(LOCID);
				StartNum = lsExon.get(lsExon.size()-1);
				return	ChrSearch.getSeq(!direction,locinfo.ChrID, StartNum, StartNum+length);
			}
		}
		else //不考虑正反向，返回的就是默认正向或反向
		{
			if(locinfo.getCis5to3(LOCID))
			{
				StartNum=locinfo.getExonlist(LOCID).get(2);
				return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum-length, StartNum);
			}
			else 
			{
				ArrayList<Integer> lsExon = locinfo.getExonlist(LOCID);
				StartNum = lsExon.get(lsExon.size()-1);
				return	ChrSearch.getSeq(direction,locinfo.ChrID, StartNum, StartNum+length);
			}
		}	
	}
	/**
	 * 输入染色体序号，坐标，坐标两边长度，返回该坐标的左右两边序列
	 * 当坐标在基因内部时，考虑条目的方向,如果在基因间，则返回正链<br>
	 * 所谓坐标在基因内部，指坐标在条目上游UpstreamTSSbp到下游GeneEnd3UTR之间的区域
	 * @param ChrID ,chr采用正则表达式抓取，无所谓大小写，会自动转变为小写
	 * @param codloc peak坐标
	 * @param lenght peak左右两端长度
	 * @param condition 为 0,1,2 三种情况<br>
	 * 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向<br>
	 * 1: 通通提取正向<br>
	 * 2: 通通提取反向<br>
	 * @return
	 */
	public String getPeakSeq(String ChrID, int codloc ,int lenght,int condition)
	{
		if (condition==0) 
		{
			GffCodInfo peakInfo = gffSearch.searchLocation(ChrID, codloc, gffHash);
			boolean flaginside=false;//是否在上游3000bp以内，默认在以外
			boolean cis5to3=true;
			/**
			 * 当在基因间时
			 */
			if(!peakInfo.insideLOC)
			{	
				/**
				 *  如果在前一个基因的扩大范围(上游3k向下多100bp)内
				 */
				if (Math.abs(peakInfo.distancetoLOCStart[0])<UpStreamTSSbp|| Math.abs(peakInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
				{
					flaginside=true;
					cis5to3=peakInfo.begincis5to3;
				}
				/**
				 *  如果后一个基因的方向为正向----->那么就是peak和后一个基因转录起点的距离
				 */
				if (Math.abs(peakInfo.distancetoLOCStart[1])<UpStreamTSSbp|| Math.abs(peakInfo.distancetoLOCEnd[1])<GeneEnd3UTR)
				{ 
					flaginside=true;
					cis5to3=peakInfo.endcis5to3;
				}
			}
			/**
			 * 当在基因内部时
			 */
			else 
			{
				cis5to3=peakInfo.begincis5to3;
				flaginside=true;
			}
		   
			if(flaginside)//坐标在基因内部，在基因上游或者是3‘UTR内部
			{
				return ChrSearch.getSeq(ChrID, codloc, lenght, cis5to3);
			}
			return ChrSearch.getSeq(ChrID, codloc, lenght,true);
		}
		else if (condition==1) 
		{
			return ChrSearch.getSeq(ChrID, codloc, lenght, true);
		}
		else if (condition==2) {
			return ChrSearch.getSeq(ChrID, codloc, lenght, false);
		}
		else {
			System.out.println("error");
			return null;
		}
	}

	
	/**
	 * 	返回外显子总长度，内含子总长度等信息
	 * 有问题
	 * 为一个ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength <br>
	 * 3: allIntronLength <br>
	 * 4: allup2kLength
	 * 5: allInterGenic <br>
	 * 6: allGeneLength <br>
	 * @return 
	 */
	public ArrayList<Long> getGeneStructureLength()
	{
		return ((GffHashGene)gffHash).getGeneStructureLength();
	}

	
	/**
	 * 给陈德桂优化过的序列提取，就是在已有的序列上，查找距离中点最近的一个特殊字母，然后以该字母为中心，左右各扩展指定长度再次提取序列
	 * 输入染色体序号，坐标，坐标两边长度，返回该坐标的左右两边序列
	 * 当坐标在基因内部时，考虑条目的方向,如果在基因间，则返回正链<br>
	 * 所谓坐标在基因内部，指坐标在条目上游UpstreamTSSbp到下游GeneEnd3UTR之间的区域
	 * @param ChrID
	 * @param centerChar,距离中点最近的一个特殊字母,需要大写，程序会自动转换为小写
	 * @param codloc peak坐标
	 * @param lenght peak左右两端长度
	 * @param condition 为 0,1,2 三种情况<br>
	 * 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向<br>
	 * 1: 通通提取正向<br>
	 * 2: 通通提取反向<br>
	 * @return
	 */
	public String getCDGPeakSeq(String ChrID,String centerChar, int codloc ,int lenght,int condition)
	{
		String tmpSeq="";
		boolean flaginside=false;//是否在上游3000bp以内，默认在以外
		boolean cis5to3=true;
		
		if (condition==0) 
		{
			GffCodInfo peakInfo = gffSearch.searchLocation(ChrID, codloc, gffHash);
		
			/**
			 * 当在基因间时
			 */
			if(!peakInfo.insideLOC)
			{	
				/**
				 *  如果在前一个基因的扩大范围(上游3k向下多100bp)内
				 */
				if (Math.abs(peakInfo.distancetoLOCStart[0])<UpStreamTSSbp|| Math.abs(peakInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
				{
					flaginside=true;
					cis5to3=peakInfo.begincis5to3;
				}
				/**
				 *  如果后一个基因的方向为正向----->那么就是peak和后一个基因转录起点的距离
				 */
				if (Math.abs(peakInfo.distancetoLOCStart[1])<UpStreamTSSbp|| Math.abs(peakInfo.distancetoLOCEnd[1])<GeneEnd3UTR)
				{ 
					flaginside=true;
					cis5to3=peakInfo.endcis5to3;
				}
			}
			/**
			 * 当在基因内部时
			 */
			else 
			{
				cis5to3=peakInfo.begincis5to3;
				flaginside=true;
			}
		   
			if(flaginside)//坐标在基因内部，在基因上游或者是3‘UTR内部
			{
				tmpSeq= ChrSearch.getSeq(ChrID, codloc, lenght, cis5to3);
			}
			tmpSeq=  ChrSearch.getSeq(ChrID, codloc, lenght,true);
		}
		else if (condition==1) 
		{
			tmpSeq=  ChrSearch.getSeq(ChrID, codloc, lenght, true);
		}
		else if (condition==2) {
			tmpSeq=  ChrSearch.getSeq(ChrID, codloc, lenght, false);
		}
		else {
			System.out.println("error");
			return null;
		}
		if (!tmpSeq.contains("c")&&!tmpSeq.contains("C")) {
			return "";
		}
		
		
		int centerTmpSeq=tmpSeq.length()/2;
		int bias=0;
		for (int i = 0; i <=centerTmpSeq; i++)
		{		
			String leftindexchar=tmpSeq.charAt(centerTmpSeq-i)+"";
			String rightindexchar=tmpSeq.charAt(centerTmpSeq+i)+"";
			if (centerChar.equals(leftindexchar)) {
				bias=-i;
				break;
			}
			if (centerChar.toLowerCase().equals(leftindexchar)) {
				bias=-i;
				break;
			}
			if (centerChar.equals(rightindexchar)) {
				bias=i;
				break;
			}
			if (centerChar.toLowerCase().equals(rightindexchar)) {
				bias=i;
				break;
			}
		}
		
		String resultSeq="";
		if (condition==0) 
		{
			if(flaginside&&cis5to3)//坐标在基因内部，在基因上游或者是3‘UTR内部
			{
				resultSeq= ChrSearch.getSeq(ChrID, codloc+bias, lenght, true);
			}
			else if (flaginside&&!cis5to3) {
				resultSeq=  ChrSearch.getSeq(ChrID, codloc-bias, lenght,false);
			} 
			else if (!flaginside) {
				resultSeq=  ChrSearch.getSeq(ChrID, codloc+bias, lenght,true);
			}
		}
		else if (condition==1) 
		{
			resultSeq=ChrSearch.getSeq(ChrID, codloc+bias, lenght, true);
		}
		else if (condition==2) 
		{
			resultSeq=  ChrSearch.getSeq(ChrID, codloc-bias, lenght, false);
		}
		
		return resultSeq;
		
	}

	
	 /**
	   * 次底层的查找
	   * @cisseq true：获得正向序列 false：获得反向序列
	   */
	public String getSeq(boolean cisseq,String chr, int startnum, int endnum) 
	{   
		return ChrSearch.getSeq(cisseq,chr,startnum,endnum);
	}
	  
	  /**
	   * 给出染色体编号位置和方向返回序列
	   * @param chrlocation染色体编号方向如：Chr:1000-2000
	   * @param cisseq方向，true:正向 false:反向互补
	   */
	public String GetSequence(String chrlocation, boolean cisseq)
	{
		return ChrSearch.getSeq(chrlocation, cisseq);
	}
	  
	 
	  
	
	/**
	 * 给定chrID和具体坐标区间，以及分辨率，返回double[]数组，数组中是该区间内reads的分布情况
	 * 如果该染色体上没有mapping结果，则返回null
	 * @param chrID
	 * @param locStart
	 * @param locEnd
	 * @param binBp 每个区域内所含的bp数
	 * @return
	 */
	public double[] getRangReadsDist(String chrID,int locStart,int locEnd,int binBp) 
	{
		return mapReads.getRengeInfo(binBp,chrID, locStart, locEnd, 0);
	}
	
	/**
	 * 给定chrID和具体坐标区间，以及分辨率，返回double[]数组，数组中是该区间内reads的分布情况
	 * 如果该染色体上没有mapping结果，则返回null
	 * @param binNum 待分割的块数
	 * @param chrID
	 * @param locStart
	 * @param locEnd
	 * @return
	 */
	public double[] getRangReadsDist(int binNum,String chrID,int locStart,int locEnd) 
	{
		return mapReads.getRengeInfo(chrID, locStart, locEnd, binNum, 0);
	}
	
	
	
	
	/**
	 * 给定chrID和具体坐标区间，以及分辨率，返回double[]数组:该染色体上tag的密度分布，数组中是该区间内reads的分布情况
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param binNum 待分割的块数
	 * @return
	 */
	public double[] getChrReadsDist(String chrID,int startLoc,int endLoc,int binNum) 
	{
		return mapReads.getReadsDensity(chrID, startLoc, endLoc, binNum);
	}
	
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * 当 chrID=""时，获得总chr的长度
	 * @param chrID
	 * @return
	 */
	public long getChrLength(String chrID) 
	{
		if (!chrID.equals("")) {
			return (long) ChrSearch.getChrLength(chrID);
		}
		else {
			ArrayList<String[]> chrInfo=ChrSearch.getChrLengthInfo();
			long chrAllLength=0;
			for (int i = 0; i < chrInfo.size(); i++) {
				chrAllLength=chrAllLength+Long.parseLong(chrInfo.get(i)[1]);
			}
			return chrAllLength;
		}
		 
		
		
	}

	/**
	 * 在读取chr长度文件后，可以通过此获得所有chr的长度信息
	 * @param chrID
	 * @return
	 * 0: chrID 
	 * 1: chr长度
	 */
	public ArrayList<String[]> getChrLengthInfo() 
	{
		return ChrSearch.getChrLengthInfo();
	}
	
	
	/**
	 * 在读取chr长度文件后，可以通过此获得最长和最短chr的长度
	 * @param chrID
	 * @return
	 * 0: 最短chr长度
	 *  1: 最长chr长度
	 */
	public int[] getThreshodChrLength() 
	{ 
		int[] chrLen=new int[2];
		ArrayList<String[]> chrInfo=ChrSearch.getChrLengthInfo();
		chrLen[0]=Integer.parseInt(chrInfo.get(0)[1]);
		chrLen[1]=Integer.parseInt(chrInfo.get(chrInfo.size()-1)[1]);
		// mapReads.getLimChrLength();
		return chrLen;
	}
	
	
	/**
	 * 给定染色体，与起点和终点，返回该染色体上tag的密度分布，如果该染色体在mapping时候不存在，则返回null
	 * @param chrID 
	 * @param startLoc 起点坐标，为实际起点
	 * @param endLoc 当终点为-1时，则直到染色体的结尾。
	 * @param binNum 待分割的块数
	 * @return
	 */
	public  double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) 
	{
		return mapReads.getReadsDensity(chrID, startLoc, endLoc, binNum);
	}
	
	/**
	 * 获得本染色体文本每一行所包含的bp数目
	 * @return
	 */
	public int getChrLineLength() {
		return ChrSearch.getChrLineLength();
	}
	
	/**
	 * 获得某条染色体对应的BufferedReader类，方便从头读取
	 * @param chrID
	 * @return
	 */
	public BufferedReader getBufChrSeq(String chrID)
	{
		return ChrSearch.getBufChrSeq(chrID);
	}
	
	/**
	 * 当设定Chr文件后，可以将序列长度输出到文件 输出文件为 chrID(小写)+“\t”+chrLength+换行 不是顺序输出
	 * @param outFile
	 */
	 public void saveChrLengthToFile(String outFile) 
	 {
		 ChrSearch.saveChrLengthToFile(outFile);
	 }
	 
		/**
		 * 指定最长染色体的值，返回按比例每条染色体相应值下染色体的坐标数组,resolution和int[resolution]，可用于画图
		 * 那么resolution就是返回的int[]的长度
		 * @param chrID
		 * @param maxresolution
		 */
	 public int[] getChrRes(String chrID,int maxresolution) throws Exception
		{
			return ChrSearch.getChrRes(chrID, maxresolution);
		}
		
	 
		
	 /**
	  * 保存每个被peak所覆盖的基因的具体情况 <br>
	  * string： 基因名<br>
	  * arraylist：具体信息，包含四个int[]<br>
	  * 0：Tss int[1]，记录peak在Tss的左边还是右边，共四个值 0：none 1：left 2：right 3：both<br>
	  * 1：Exon int[n]. 记录哪些外显子被peak所覆盖，0：没有被覆盖 1：被覆盖。n为每个基因最长转录本的外显子数目,从0到exonNum-1。<br>
	  * 2：Intron int[n]. 记录哪些内含子被peak所覆盖，0：没有被覆盖 1：被覆盖。n为每个基因最长转录本的内含子数目。从0到intronNum-1<br>
	  * 3：GeneEnd int[1]:记录peak在GeneEnd的左边还是右边，共四个值 0：none 1：left 2：right 3：both
	  */
	 Hashtable<String, ArrayList<int[]>> hashGenePeakInfo=new Hashtable<String, ArrayList<int[]>>();
		  
	 /**
	  * 输入peak信息，将hashGenePeakInfo表填满
	  * 该表格的作用： 保存每个被peak所覆盖的基因的具体情况 <br>
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  */
	 private void setHashGenePeakInfo(String[][] LOCInfo)
	 {
		 for (int i = 0; i < LOCInfo.length; i++) {
			 ArrayList<ArrayList<String[]>> lstmpGeneInfo=null;
			 try {
				 lstmpGeneInfo=getGeneInfo(LOCInfo[i][0], Integer.parseInt(LOCInfo[i][1]), Integer.parseInt(LOCInfo[i][2]));
				 setHashGenePeakInfo(lstmpGeneInfo);
			} catch (Exception e) {
				System.out.println(LOCInfo[i][0]+"   "+LOCInfo[i][1]+"   "+LOCInfo[i][2]);
			}
			
		 }
	 }
	 
	 /**
	  * 输入Peak信息，待统计的Tss左右两端的距离，待切割的块数，最后返回所有被peak覆盖到Tss的基因的Tss两边reads统计结果
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  * @param range
	  * @param binNum
	  * @return
	  */
	 public double[] getUCSCTssRange(String[][] LOCInfo,int range,int binNum) {
		setHashGenePeakInfo(LOCInfo);
		return getTssRange(range, binNum);
	}
	 
	 /**
	  * 输入Peak信息，待统计的GeneEnd左右两端的距离，待切割的块数，最后返回所有被peak覆盖到GeneEnd的基因的GeneEnd两边reads统计结果
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  * @param range
	  * @param binNum
	  * @return
	  */
	 public double[] getUCSCGeneEndRange(String[][] LOCInfo,int range,int binNum) {
		setHashGenePeakInfo(LOCInfo);
		return getGeneEndRange(range, binNum);
	}
		 
	 /**
	  * 输入Peak信息，待统计的Tss左右两端的距离，待切割的块数，最后返回所有被peak覆盖到Tss的基因的Tss两边reads统计结果
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  * @param range
	  * @param binNum
	  * @return
	  */
	 public double[][] getUCSCTssRangeArray(String[][] LOCInfo,int range,int binNum) {
		setHashGenePeakInfo(LOCInfo);
		return getTssRangeArray(range, binNum);
	}
	 
	 /**
	  * 输入Peak信息，待统计的GeneEnd左右两端的距离，待切割的块数，最后返回所有被peak覆盖到GeneEnd的基因的GeneEnd两边reads统计结果
	  * @param LOCInfo
	  * 0:ChrID  1:PeakStartNum  2:PeakEndNum
	  * @param range
	  * @param binNum
	  * @return
	  */
	 public double[][] getUCSCGeneEndRangeArray(String[][] LOCInfo,int range,int binNum) {
		setHashGenePeakInfo(LOCInfo);
		return getGeneEndRangeArray(range, binNum);
	}
	 
	 
	 /**
	  * 输入peak所覆盖的基因的情况，填写hashGenePeakInfo表
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  *  <b>ls0: 起点所在基因情况</b>，如果以下某一项没有，则为"" <br>
	  * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
	  * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":表示peak所占tss的哪一边，当0为""时，说明该基因tss与基因没关系 <br>
	  * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID 包含从0：第起点个外显子到1：第结束个外显子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接exonID*2就是所需要的外显子的起点。也就是从 exonID[0]*2(起点)-exonID[1]*2(起点)的外显子 <br>
	  * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID 包含从0：第起点个内含子到1：第结束个内含子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接intronID*2+1就是所需要的内含子的起点。也就是从 intronID[0]*2+1(起点)-intronID[1]*2+1(起点)的内含子 <br>
	  * &nbsp;&nbsp; 4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":表示peak所占GeneEnd的哪一边，当0为""时，说明该基因GeneEnd与基因没关系 <br>
	  *<b> ls1: 终点所在基因情况</b>，如果以下某一项没有，或起点终点在同一个基因内，则为"" <br>
	  * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
	  * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":表示peak所占tss的哪一边，当0为""时，说明该基因tss与基因没关系 <br>
	  * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID 包含从0：第起点个外显子到1：第结束个外显子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接exonID*2就是所需要的外显子的起点。也就是从 exonID[0]*2(起点)-exonID[1]*2(起点)的外显子 <br>
	  * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID 包含从0：第起点个内含子到1：第结束个内含子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接intronID*2+1就是所需要的内含子的起点。也就是从 intronID[0]*2+1(起点)-intronID[1]*2+1(起点)的内含子 <br>
	  * &nbsp;&nbsp;4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":表示peak所占GeneEnd的哪一边，当0为""时，说明该基因GeneEnd与基因没关系 <br>
	  * <b>ls2-lsend: peak两端中间所包含的基因</b> <br>
	  * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
	  * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":表示peak所占tss的哪一边，当0为""时，说明该基因tss与基因没关系 <br>
	  * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID 包含从0：第起点个外显子到1：第结束个外显子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接exonID*2就是所需要的外显子的起点。也就是从 exonID[0]*2(起点)-exonID[1]*2(起点)的外显子 <br>
	  * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID 包含从0：第起点个内含子到1：第结束个内含子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接intronID*2+1就是所需要的内含子的起点。也就是从 intronID[0]*2+1(起点)-intronID[1]*2+1(起点)的内含子 <br>
	  * &nbsp;&nbsp;4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":表示peak所占GeneEnd的哪一边，当0为""时，说明该基因GeneEnd与基因没关系 <br>
	  * @param range 统计时tss左右两边的距离   
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有tss上reads的累加情况
	  */
	 @SuppressWarnings({ "unchecked" })
	 private void setHashGenePeakInfo(ArrayList<ArrayList<String[]>> lsReadsInfo) 
	 {
		 GffHash gffHashgene = gffHash;
		 
		 //getGeneInfo(1, "", 12, 21);
		 for(int i=0;i<lsReadsInfo.size();i++)
		 {
			 ArrayList<String[]> lsTmp=lsReadsInfo.get(i);
			 ArrayList<Integer> lsLongestSplit=null;
			 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////v
			 if(lsTmp.get(0)[0]==null)
			 {
				 System.out.println("error");
			 }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			 if (!lsTmp.get(0)[0].equals("")) 
			 {
				 String tmpGeneID=lsTmp.get(0)[0].split("/")[0];
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashgene.LOCsearch(tmpGeneID);
				 lsLongestSplit=(ArrayList<Integer>)(gffDetailUCSCgene.getLongestSplit().get(1));
				 int exonNum= (lsLongestSplit.size()-2)/2;//最长转录本包含多少个外显子
				 if (hashGenePeakInfo.containsKey(lsTmp.get(0)[0]))
				 {
					 ArrayList<int[]> lsTmpPeakInfo=hashGenePeakInfo.get(lsTmp.get(0)[0]);
					 if (!lsTmp.get(2)[0].equals("")) //输入的ArrayList<ArrayList<String[]>>中Exon不为""
					 {
						 int exonStartNum=Integer.parseInt(lsTmp.get(2)[0]);
						 int exonEndNum=Integer.parseInt(lsTmp.get(2)[1]);
						 for (int j = exonStartNum-1; j <exonEndNum; j++) {
							 lsTmpPeakInfo.get(1)[j]=1;
						 }
					 }
					 if (!lsTmp.get(3)[0].equals("")) //Intron不为""
					 {
						 int intronStartNum=Integer.parseInt(lsTmp.get(3)[0]);
						 int intronEndNum=Integer.parseInt(lsTmp.get(3)[1]);
						 for (int j = intronStartNum-1; j <intronEndNum; j++) {
							 lsTmpPeakInfo.get(2)[j]=1;
						 }
					 }
					 /////////////////TSS///////////////////////////////////////
					 if (!lsTmp.get(1)[0].equals("")) 
					 {
						 int tmpCod=0;
						 if (lsTmp.get(1)[1].equals("right"))  
							 tmpCod=2;
						 else if (lsTmp.get(1)[1].equals("left")) 
							 tmpCod=1;
						 else if (lsTmp.get(1)[1].equals("both"))  
							 tmpCod=3;
						 
						 if (lsTmpPeakInfo.get(0)[0]==0) 
							 lsTmpPeakInfo.get(0)[0]=tmpCod;
						 else if (lsTmpPeakInfo.get(0)[0]!=tmpCod&&tmpCod!=0)//新的方向和老的方向不一致并且新方向存在，则为both
							 lsTmpPeakInfo.get(0)[0]=3;
						 
					 }
					 /////////////GeneEnd/////////////////////////////////////////////////
					 if (!lsTmp.get(4)[0].equals("")) 
					 {
						 int tmpCod=0;
						 if (lsTmp.get(4)[1].equals("right"))  
							 tmpCod=2;
						 else if (lsTmp.get(4)[1].equals("left")) 
							 tmpCod=1;
						 else if (lsTmp.get(4)[1].equals("both"))  
							 tmpCod=3;
						 
						 if (lsTmpPeakInfo.get(3)[0]==0) 
							 lsTmpPeakInfo.get(3)[0]=tmpCod;
						 else if (lsTmpPeakInfo.get(3)[0]!=tmpCod&&tmpCod!=0)//新的方向和老的方向不一致并且新方向存在，则为both
							 lsTmpPeakInfo.get(3)[0]=3;
					 }
				 }
				 
						/////////////////////////////////////////////////////////////////////////////////////////////////////如果以前已经有过该基因信息，则补上去//////////////////////////////////////////////////
				 else
				 {
					 String tmpChrID=lsTmp.get(0)[0];
					 
					 ArrayList<int[]> lsTmpPeakInfo=new ArrayList<int[]>();
					 int[] TSS=new int[1];
					 int[] exonInfo=new int[exonNum];
					 int[] intronInfo=new int[exonNum-1];
					 int[] geneEnd=new int[1];
					 if (!lsTmp.get(2)[0].equals("")) //Exon不为""
					 {
						 int exonStartNum=Integer.parseInt(lsTmp.get(2)[0]);
						 int exonEndNum=Integer.parseInt(lsTmp.get(2)[1]);
						 for (int j = exonStartNum-1; j <exonEndNum; j++) {
							 exonInfo[j]=1;
						 }
					 }
					 if (!lsTmp.get(3)[0].equals("")) //Intron不为""
					 {
						 int intronStartNum=Integer.parseInt(lsTmp.get(3)[0]);
						 if (lsTmp.get(3)[1].equals("")) {
							System.out.println(lsTmp.get(0)[0]);
						}
						 int intronEndNum=Integer.parseInt(lsTmp.get(3)[1]);
						 for (int j = intronStartNum-1; j <intronEndNum; j++) {
							 intronInfo[j]=1;
						 }
					 }
							 /////////////////TSS///////////////////////////////////////
					 if (lsTmp.get(1)[0]==null) {
						System.out.println("error");
					}
					 if (!lsTmp.get(1)[0].equals("")) 
					 {
						 if (lsTmp.get(1)[1].equals("right"))  
							 TSS[0]=2;
						 else if (lsTmp.get(1)[1].equals("left")) 
							 TSS[0]=1;
						 else if (lsTmp.get(1)[1].equals("both"))  
							 TSS[0]=3;
					 }
					 /////////////GeneEnd/////////////////////////////////////////////////
					 if (!lsTmp.get(4)[0].equals("")) 
					 {
						 
						 if (lsTmp.get(4)[1].equals("right"))  
							 geneEnd[0]=2;
						 else if (lsTmp.get(4)[1].equals("left")) 
							 geneEnd[0]=1;
						 else if (lsTmp.get(4)[1].equals("both"))  
							 geneEnd[0]=3;
					 }
					 lsTmpPeakInfo.add(TSS);lsTmpPeakInfo.add(exonInfo);lsTmpPeakInfo.add(intronInfo);lsTmpPeakInfo.add(geneEnd);
					 hashGenePeakInfo.put(tmpChrID, lsTmpPeakInfo);
				 }
			 }
		 }
	 }
  
	 /**
	  * 在hashGenePeakInfo已经存在的情况下
	  * 输入peak所覆盖的基因的情况，同时指定条件和切割份数，返回该条件和切割份数下的reads密度值,已经经过了校正
	  * 校正方法是：获得reads密度*fold数/总reads数/涉及到的gene数
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  * @param range 统计时tss左右两边的距离，就是说画出tss左右两边多少的距离，这个最好与UpstreamTSSbp和DownstreamTss相一致，UpstreamTSSbp和DownstreamTss在这里用来划定区域，在这个区域内有peak则将该tss加入统计
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有tss上reads的累加情况
	  */
	 @SuppressWarnings("unchecked")
	private double[] getTssRange(int range, int binNum) 
	 {
		 tmpGeneNum = 0;
		 GffHash gffHashGene=gffHash;
		 Iterator iter = hashGenePeakInfo.entrySet().iterator();
		 double[] binResult=new double[binNum];//清空binResult
		 ArrayList<double[]> lsTss = new ArrayList<double[]>();
		 while (iter.hasNext()) 
		 {
			 Map.Entry entry = (Map.Entry) iter.next();
			 String tmpGeneID = ((String) entry.getKey()).split("/")[0];
			 ArrayList<int[]> lsGeneInfo = (ArrayList<int[]>) entry.getValue();
			 
			 double[] tmpTssBin=null;
			 
			 if (lsGeneInfo.get(0)[0]!=0) 
			 {
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(tmpGeneID);

				 int startNum=0; int endNum=0;
				 if (gffDetailUCSCgene.cis5to3) 
				 {
					 endNum=gffDetailUCSCgene.numberstart+range;
					 startNum=gffDetailUCSCgene.numberstart-range;
					 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 if (tmpTssBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
				 }
				 else//因为序列反向，所以需要将结果也反一下 
				 {
					 startNum=gffDetailUCSCgene.numberend-range;
					 endNum=gffDetailUCSCgene.numberend+range;
					 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 //看看都是哪些会出错
					 if (tmpTssBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
					 MathComput.convertArray(tmpTssBin);
				 }
				 tmpGeneNum++;
				 lsTss.add(tmpTssBin);
			 }
		 }
		 binResult = MathComput.getMeanByColdou(lsTss);
		 for (int i = 0; i < binResult.length; i++) {
			binResult[i] = binResult[i]*fold/readsNum;
		}
		 return binResult;
	 }
	 /**
	  * 在hashGenePeakInfo已经存在的情况下
	  * 输入peak所覆盖的基因的情况，同时指定条件和切割份数，返回该条件和切割份数下的reads密度值,已经经过了校正
	  * 校正方法是：获得reads密度*fold数/总reads数/涉及到的gene数
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  * @param range 统计时tss左右两边的距离，就是说画出tss左右两边多少的距离，这个最好与UpstreamTSSbp和DownstreamTss相一致，UpstreamTSSbp和DownstreamTss在这里用来划定区域，在这个区域内有peak则将该tss加入统计
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有tss上reads的情况的数组
	  */
	 @SuppressWarnings("unchecked")
	private double[][] getTssRangeArray(int range, int binNum) 
	 {
		 tmpGeneNum = 0;
		 GffHash gffHashGene = gffHash;
		 Iterator iter = hashGenePeakInfo.entrySet().iterator();
		 double[] binResult=new double[binNum];//清空binResult
		 ArrayList<double[]> lsTss = new ArrayList<double[]>();
		 while (iter.hasNext()) 
		 {
			 Map.Entry entry = (Map.Entry) iter.next();
			 String tmpGeneID = ((String) entry.getKey()).split("/")[0];
			 ArrayList<int[]> lsGeneInfo = (ArrayList<int[]>) entry.getValue();
			 
			 double[] tmpTssBin=null;
			 
			 if (lsGeneInfo.get(0)[0]!=0) 
			 {
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(tmpGeneID);

				 int startNum=0; int endNum=0;
				 if (gffDetailUCSCgene.cis5to3) 
				 {
					 endNum=gffDetailUCSCgene.numberstart+range;
					 startNum=gffDetailUCSCgene.numberstart-range;
					 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 if (tmpTssBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
				 }
				 else//因为序列反向，所以需要将结果也反一下 
				 {
					 startNum=gffDetailUCSCgene.numberend-range;
					 endNum=gffDetailUCSCgene.numberend+range;
					 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 //看看都是哪些会出错
					 if (tmpTssBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
					 MathComput.convertArray(tmpTssBin);
				 }
				 tmpGeneNum++;
				 lsTss.add(tmpTssBin);
			 }
		 }
		 double[][] result = lsTss.toArray(new double[1][1]);
//		 for (int i = 0; i < result.length; i++) {
//			for (int j = 0; j < result[0].length; j++) {
//				result[i][j] = result[i][j] *fold/readsNum;
//			}
//		 }
		 return result;
	 }
	 
	 int tmpGeneNum = 0;
	 /**
	  * 当调用getTssRange、getGeneEndRange方法后，该方法会返回该区域内进行计数的gene的个数
	  * @return
	  */
	 public int getRegGenNum() {
		return tmpGeneNum;
	}
	 
	 /**
	  * 指定geneID，注意是RefSeq内的geneID，也就是说，该ID必须在gff文件中出现过。如果没初见过，那么就跳过。然而因为在处理的时候已经将同一基因的多个转录本进行了合并，所以最后获得的是最长转录本的信息
	  * 输入peak所覆盖的基因的情况，同时指定条件和切割份数，返回该条件和切割份数下的reads密度值,已经经过了校正
	  * 校正方法是：获得reads密度*fold数/总reads数/涉及到的gene数
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  * @param range 统计时tss左右两边的距离，就是说画出tss左右两边多少的距离，这个最好与UpstreamTSSbp和DownstreamTss相一致，UpstreamTSSbp和DownstreamTss在这里用来划定区域，在这个区域内有peak则将该tss加入统计
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有tss上reads的累加情况
	  */
	 @SuppressWarnings("unchecked")
	public double[] getTssRange(String[] geneID, int range, int binNum) 
	 {
		 tmpGeneNum = 0;
		 
		 GffHash gffHashGene=gffHash;
		 //将输入的geneID中重复的geneID进行合并，得到唯一的GeneID
		 double[] binResult=new double[binNum];//清空binResult
		 ArrayList<double[]> lsTss = new ArrayList<double[]>();
		 HashSet<String> hashUniGeneID = new HashSet<String>();//用于去重复的
		 for (int i = 0; i < geneID.length; i++)
		 {
			 //这里可以考虑将输入的geneID进入数据库转换成refseqID
			 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(geneID[i]);
			 if (gffDetailUCSCgene == null) {
				continue;
			}
			 if (hashUniGeneID.contains(gffDetailUCSCgene.locString)) {
				continue;
			 }
			 hashUniGeneID.add(gffDetailUCSCgene.locString);
			 double[] tmpTssBin=null;
			 int startNum=0; int endNum=0;
			 if (gffDetailUCSCgene.cis5to3) 
			 {
				 startNum=gffDetailUCSCgene.numberstart-range;
				 endNum=gffDetailUCSCgene.numberstart+range;
				 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
				 if (tmpTssBin == null) {
					 System.out.println(gffDetailUCSCgene.ChrID);
					continue;
				}
			 }
			 else//因为序列反向，所以需要将结果也反一下 
			 {
				 startNum=gffDetailUCSCgene.numberend-range;
				 endNum=gffDetailUCSCgene.numberend+range;
				 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
				 //看看都是哪些会出错
				 if (tmpTssBin == null) {
					 System.out.println(gffDetailUCSCgene.ChrID);
					continue;
				}
				 MathComput.convertArray(tmpTssBin);
			 }
			 tmpGeneNum++;
			 lsTss.add(tmpTssBin);	
		 }
		 binResult = MathComput.getMeanByColdou(lsTss);
		 for (int i = 0; i < binResult.length; i++) {
			binResult[i] = binResult[i]*fold/readsNum;
		}
		 return binResult;
	 }
	 
	 /**
	  * 指定geneID，注意是RefSeq内的geneID，也就是说，该ID必须在gff文件中出现过。如果没初见过，那么就跳过。然而因为在处理的时候已经将同一基因的多个转录本进行了合并，所以最后获得的是最长转录本的信息
	  * 输入peak所覆盖的基因的情况，同时指定条件和切割份数，返回该条件和切割份数下的reads密度值,已经经过了校正
	  * 校正方法是：获得reads密度*fold数/总reads数/涉及到的gene数
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  * @param range 统计时geneEnd左右两边的距离，就是说画出geneEnd左右两边多少的距离
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有geneEnd上reads的累加情况
	  */
	 public double[] getGeneEndRange(String[] geneID,int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		 //将输入的geneID中重复的geneID进行合并，得到唯一的GeneID
		 tmpGeneNum = 0;
		 double[] binResult=null;//清空binResult
		 ArrayList<double[]> lsGeneEnd = new ArrayList<double[]>();
		 HashSet<String> hashUniGeneID = new HashSet<String>();//用于去重复的
		 for (int i = 0; i < geneID.length; i++)
		 {
			 //这里可以考虑将输入的geneID进入数据库转换成refseqID
			 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(geneID[i]);
			 if (gffDetailUCSCgene == null) {
				continue;
			}
			 if (hashUniGeneID.contains(gffDetailUCSCgene.locString)) {
				continue;
			 }
			 double[] tmpGeneEndBin=null;
			 int startNum=0; int endNum=0;
			 if (gffDetailUCSCgene.cis5to3) 
			 {
				 startNum=gffDetailUCSCgene.numberend-range;
				 endNum=gffDetailUCSCgene.numberend+range;
				 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
				 //看看都是哪些会出错
				 if (tmpGeneEndBin == null) {
					 System.out.println(gffDetailUCSCgene.ChrID);
					continue;
				}
			 }
			 else//因为序列反向，所以需要将结果也反一下 
			 {
				 startNum=gffDetailUCSCgene.numberstart-range;
				 endNum=gffDetailUCSCgene.numberstart+range;
				 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
				 if (tmpGeneEndBin == null) {
					 System.out.println(gffDetailUCSCgene.ChrID);
					continue;
				}
				 MathComput.convertArray(tmpGeneEndBin);
			 }
			 tmpGeneNum++;
			 lsGeneEnd.add(tmpGeneEndBin);
		 }
		 binResult = MathComput.getMeanByColdou(lsGeneEnd);//中位数
		 for (int i = 0; i < binResult.length; i++) {
			binResult[i] = binResult[i]*fold/readsNum;
		}
		 return binResult;
	 }
	 
	 
	 /**
	  * 指定单个geneID，注意是RefSeq内的geneID，也就是说，该ID必须在gff文件中出现过。如果没初见过，那么就跳过。然而因为在处理的时候已经将同一基因的多个转录本进行了合并，所以最后获得的是最长转录本的信息
	  * 输入peak所覆盖的基因的情况，同时指定条件和切割份数，返回该条件和切割份数下的reads密度值,不进行矫正
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  * @param range 统计时tss左右两边的距离，就是说画出tss左右两边多少的距离，这个最好与UpstreamTSSbp和DownstreamTss相一致，UpstreamTSSbp和DownstreamTss在这里用来划定区域，在这个区域内有peak则将该tss加入统计
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有tss上reads的累加情况，如果没查到，就返回null
	  */
	 @SuppressWarnings("unchecked")
	public double[] getTssRange(String geneID, int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		//这里可以考虑将输入的geneID进入数据库转换成refseqID
		 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(geneID);
		 if (gffDetailUCSCgene == null) {
			return null;
		}
		 double[] tmpTssBin=null;
		 int startNum=0; int endNum=0;
		 if (gffDetailUCSCgene.cis5to3) 
		 {
			 startNum=gffDetailUCSCgene.numberstart-range;
			 endNum=gffDetailUCSCgene.numberstart+range;
			 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
			 
			 if (tmpTssBin == null) {
				 System.out.println(gffDetailUCSCgene.ChrID);
				return null;
			}
		 }
		 else//因为序列反向，所以需要将结果也反一下 
		 {
			 startNum=gffDetailUCSCgene.numberend-range;
			 endNum=gffDetailUCSCgene.numberend+range;
			 tmpTssBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
			 //看看都是哪些会出错
			 if (tmpTssBin == null) {
				 System.out.println(gffDetailUCSCgene.ChrID);
				 return null;
			}
			 MathComput.convertArray(tmpTssBin);
		 }
		 return tmpTssBin;
	 }
	 
	 
	 /**
	  *指定单个geneID，注意是RefSeq内的geneID，也就是说，该ID必须在gff文件中出现过。如果没初见过，那么就跳过。然而因为在处理的时候已经将同一基因的多个转录本进行了合并，所以最后获得的是最长转录本的信息
	  * 输入peak所覆盖的基因的情况，同时指定条件和切割份数，返回该条件和切割份数下的reads密度值,已经经过了校正
	  * 校正方法是：获得reads密度*fold数/总reads数/涉及到的gene数
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  * @param range 统计时geneEnd左右两边的距离，就是说画出geneEnd左右两边多少的距离
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有geneEnd上reads的累加情况，如果没查到，就返回null
	  */
	 public double[] getGeneEndRange(String geneID,int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		 //这里可以考虑将输入的geneID进入数据库转换成refseqID
		 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(geneID);
		 if (gffDetailUCSCgene == null) {
			return null;
		}
		 double[] tmpGeneEndBin=null;
		 int startNum=0; int endNum=0;
		 if (gffDetailUCSCgene.cis5to3) 
		 {
			 startNum=gffDetailUCSCgene.numberend-range;
			 endNum=gffDetailUCSCgene.numberend+range;
			 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
			 //看看都是哪些会出错
			 if (tmpGeneEndBin == null) {
				 System.out.println(gffDetailUCSCgene.ChrID);
				 return null;
			}
		 }
		 else//因为序列反向，所以需要将结果也反一下 
		 {
			 startNum=gffDetailUCSCgene.numberstart-range;
			 endNum=gffDetailUCSCgene.numberstart+range;
			 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
			 if (tmpGeneEndBin == null) {
				 System.out.println(gffDetailUCSCgene.ChrID);
				return null;
			}
			 MathComput.convertArray(tmpGeneEndBin);
		 }	
		 return tmpGeneEndBin;
	 }
	 
	 
	 
	 /**
	  * 在hashGenePeakInfo已经存在的情况下
	  * 输入peak所覆盖的基因的情况，同时指定条件和切割份数，返回该条件和切割份数下的reads密度值
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  * @param range 统计时tss左右两边的距离，就是说画出tss左右两边多少的距离，这个最好与UpstreamTSSbp和DownstreamTss相一致，UpstreamTSSbp和DownstreamTss在这里用来划定区域，在这个区域内有peak则将该tss加入统计
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有tss上reads的累加情况
	  */
	 private double[] getGeneEndRange(int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		 Iterator iter = hashGenePeakInfo.entrySet().iterator();
		 double[] binResult = null;//清空binResult
		 tmpGeneNum = 0;
		 ArrayList<double[]> lsGeneEnd = new ArrayList<double[]>();
		 while (iter.hasNext()) 
		 {
			 Map.Entry entry = (Map.Entry) iter.next();
			 String tmpGeneID = ((String) entry.getKey()).split("/")[0];

			 ArrayList<int[]> lsGeneInfo = (ArrayList<int[]>) entry.getValue();
			 double[] tmpGeneEndBin=null;
			 if (lsGeneInfo.get(3)[0]!=0) 
			 {
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(tmpGeneID);
				 int startNum=0; int endNum=0;
				 if (gffDetailUCSCgene.cis5to3) 
				 {
					 endNum=gffDetailUCSCgene.numberend+range;
					 startNum=gffDetailUCSCgene.numberend-range;
					 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 //看看都是哪些会出错
					 if (tmpGeneEndBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
				 }
				 else//因为序列反向，所以需要将结果也反一下 
				 {
					 startNum=gffDetailUCSCgene.numberstart-range;
					 endNum=gffDetailUCSCgene.numberstart+range;
					 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 if (tmpGeneEndBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
					 MathComput.convertArray(tmpGeneEndBin);
				 }
				 tmpGeneNum++;
				lsGeneEnd.add(tmpGeneEndBin);
				// MathComput.addArray(binResult,tmpGeneEndBin);
				 //tmpGeneNum++;
			 }
		 }
		 binResult = MathComput.getMeanByColdou(lsGeneEnd);//中位数
		 for (int i = 0; i < binResult.length; i++) {
			binResult[i] = binResult[i]*fold/readsNum;
		}
		 return binResult;
	 }
	 
	 /**
	  * 在hashGenePeakInfo已经存在的情况下
	  * 输入peak所覆盖的基因的情况，同时指定条件和切割份数，返回该条件和切割份数下的reads密度值
	  * @param lsReadsInfo getGeneInfo方法返回的结果
	  * @param range 统计时tss左右两边的距离，就是说画出tss左右两边多少的距离，这个最好与UpstreamTSSbp和DownstreamTss相一致，UpstreamTSSbp和DownstreamTss在这里用来划定区域，在这个区域内有peak则将该tss加入统计
	  * @param gffHashUCSCgene 
	  * @return 返回该区域上有peak覆盖的所有tss上reads的累加情况
	  */
	 private double[][] getGeneEndRangeArray(int range, int binNum) 
	 {
		 GffHash gffHashGene=gffHash;
		 Iterator iter = hashGenePeakInfo.entrySet().iterator();
		 double[] binResult = null;//清空binResult
		 tmpGeneNum = 0;
		 ArrayList<double[]> lsGeneEnd = new ArrayList<double[]>();
		 while (iter.hasNext()) 
		 {
			 Map.Entry entry = (Map.Entry) iter.next();
			 String tmpGeneID = ((String) entry.getKey()).split("/")[0];

			 ArrayList<int[]> lsGeneInfo = (ArrayList<int[]>) entry.getValue();
			 double[] tmpGeneEndBin=null;
			 if (lsGeneInfo.get(3)[0]!=0) 
			 {
				 GffDetailUCSCgene gffDetailUCSCgene=(GffDetailUCSCgene) gffHashGene.LOCsearch(tmpGeneID);
				 int startNum=0; int endNum=0;
				 if (gffDetailUCSCgene.cis5to3) 
				 {
					 endNum=gffDetailUCSCgene.numberend+range;
					 startNum=gffDetailUCSCgene.numberend-range;
					 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 //看看都是哪些会出错
					 if (tmpGeneEndBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
				 }
				 else//因为序列反向，所以需要将结果也反一下 
				 {
					 startNum=gffDetailUCSCgene.numberstart-range;
					 endNum=gffDetailUCSCgene.numberstart+range;
					 tmpGeneEndBin=mapReads.getRengeInfo(gffDetailUCSCgene.ChrID, startNum, endNum, binNum, 0);
					 if (tmpGeneEndBin == null) {
						 System.out.println(gffDetailUCSCgene.ChrID);
						continue;
					}
					 MathComput.convertArray(tmpGeneEndBin);
				 }
				 tmpGeneNum++;
				lsGeneEnd.add(tmpGeneEndBin);
				// MathComput.addArray(binResult,tmpGeneEndBin);
				 //tmpGeneNum++;
			 }
		 }
		 double[][] result = lsGeneEnd.toArray(new double[1][1]);
//		 for (int i = 0; i < result.length; i++) {
//			for (int j = 0; j < result[0].length; j++) {
//				result[i][j] = result[i][j] *fold/readsNum;
//			}
//		 }
		 return result;
	 }
	 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		  /**
		   * 给定peak的两个端点，返回这两个端点内部所涉及到的基因及具体情况，目前仅支持UCSCgene，可以扩展为CG等<br>
		   * 首先要设定UpStreamTSSbp和DownstreamTss，UpstreamTSSbp和DownstreamTss在这里用来划定区域，peak在此范围内则将该基因计入统计<br>
		   * 然后要设定GeneEnd3UTR：peak处在GeneEnd左右的GeneEnd3UTR范围内则将该基因计入统计<br>
		   * 注意所谓涉及到tss区域，表示peak能够覆盖到TSS两侧的UpstreamTSSbp距离
		   * 后面在填充HashGenepeakInfo的时候会合并同一个基因的信息
		   * @param chrID 第几个染色体
		   * @param startLoc peak起点
		   * @param endLoc peak终点
		   * @return 
		   * 返回 ArrayList--ArrayList--String[] <br>
		   *  <b>ls0: 起点所在基因情况</b>，如果以下某一项没有，则为"" <br>
	  	   * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
	  	   * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":表示peak所占tss的哪一边，当0为""时，说明该基因tss与基因没关系 <br>
	  	   * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID 包含从0：第起点个外显子到1：第结束个外显子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接exonID*2就是所需要的外显子的起点。也就是从 exonID[0]*2(起点)-exonID[1]*2(起点)的外显子 <br>
	  	   * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID 包含从0：第起点个内含子到1：第结束个内含子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接intronID*2+1就是所需要的内含子的起点。也就是从 intronID[0]*2+1(起点)-intronID[1]*2+1(起点)的内含子 <br>
	  	   * &nbsp;&nbsp; 4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":表示peak所占GeneEnd的哪一边，当0为""时，说明该基因GeneEnd与基因没关系 <br>
		   *<b> ls1: 终点所在基因情况</b>，如果以下某一项没有，或起点终点在同一个基因内，则为"" <br>
		   * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
		   * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":表示peak所占tss的哪一边，当0为""时，说明该基因tss与基因没关系 <br>
		   * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID 包含从0：第起点个外显子到1：第结束个外显子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接exonID*2就是所需要的外显子的起点。也就是从 exonID[0]*2(起点)-exonID[1]*2(起点)的外显子 <br>
		   * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID 包含从0：第起点个内含子到1：第结束个内含子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接intronID*2+1就是所需要的内含子的起点。也就是从 intronID[0]*2+1(起点)-intronID[1]*2+1(起点)的内含子 <br>
		   * &nbsp;&nbsp;4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":表示peak所占GeneEnd的哪一边，当0为""时，说明该基因GeneEnd与基因没关系 <br>
		   * <b>ls2-lsend: peak两端中间所包含的基因</b> <br>
		   * &nbsp;&nbsp;0 <b>GeneID</b> string[0] 0:GeneID <br>
		   * &nbsp;&nbsp;1 <b>TSS</b> string[2] 0:"TSS",1:"right"/"left"/"both":表示peak所占tss的哪一边，当0为""时，说明该基因tss与基因没关系 <br>
		   * &nbsp;&nbsp;2 <b>exonID</b> string[2] exonID 包含从0：第起点个外显子到1：第结束个外显子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接exonID*2就是所需要的外显子的起点。也就是从 exonID[0]*2(起点)-exonID[1]*2(起点)的外显子 <br>
		   * &nbsp;&nbsp;3 <b>intronID</b> string[2] intronID 包含从0：第起点个内含子到1：第结束个内含子，本方法根据UCSCgene开发，因此只要获得UCSCgeneDetail后，不管正反向，直接intronID*2+1就是所需要的内含子的起点。也就是从 intronID[0]*2+1(起点)-intronID[1]*2+1(起点)的内含子 <br>
		   * &nbsp;&nbsp;4 <b>GeneEnd</b>  string[2] 0:"GeneEnd",1:"right"/"left"/"both":表示peak所占GeneEnd的哪一边，当0为""时，说明该基因GeneEnd与基因没关系 <br>
		   */
		  @SuppressWarnings({ "unchecked" })
		private  ArrayList<ArrayList<String[]>> getGeneInfo(String chrID,int startLoc,int endLoc)
		{
			  ArrayList<Object> gffPairEndDetail = gffSearch.searchLocation(chrID, startLoc, endLoc, gffHash);
			  GffCodInfoUCSCgene startCodInfo = (GffCodInfoUCSCgene) ((Object[])gffPairEndDetail.get(0))[0];
			  GffCodInfoUCSCgene endCodInfo = (GffCodInfoUCSCgene) ((Object[])gffPairEndDetail.get(1))[0];
			  ArrayList<ArrayList<String[]>> lsReadsInfo=new ArrayList<ArrayList<String[]>>();
			  ////////////////////////////// 开 始 位 点 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  if (startCodInfo.insideLOC) //在基因内
			  {
				  GffDetail startDetail=startCodInfo.geneDetail[0];//获得该基因的具体信息
				  ArrayList<String[]> lsStartReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1]; GeneID[0]=startCodInfo.LOCID[0]; 
				  String[] tss=new String[2];//0:TSS 1:是在哪一边含有，none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:在哪一边含有，none，left，both
				  ArrayList<Integer> lsLongestSplit = (ArrayList<Integer>) ((GffDetailUCSCgene)startDetail).getLongestSplit().get(1);
				  int ExonNum=(lsLongestSplit.size()-2)/2;
				  ////////////// 测 试 代 码 //////////////////////////////////////////////////
				  if (lsLongestSplit.size()%2==1) 
					  System.out.println("lsLongestSplit数目不为偶数");
				  /////////////////////////////////////////////////////////////////////////////////
				  String[] exonID=new String[2];  //都按照split内部的顺序来，也就是说，如果基因反向，那么就从后往前数第几个外显子或内含子，这样子后期会比较好处理
				  String[] intronID=new String[2];//0:Exon/Intron的起点 1:Exon/Intron的终点
				  if (startCodInfo.begincis5to3) //基因正向
				  {
					  //////TSS设定//////////
					  if(startCodInfo.distancetoLOCStart[0]<DownStreamTssbp)
					  {
						  tss[0]="TSS";
						  tss[1]="right";
					  }
					  else {
						  tss[0]="";
						  tss[1]="";
					  }
					  //////////// Exon Intron 设 定 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					  /////////// peak 在 一 个 基 因 内 ///////////////////////////////////////////////////////////////
					  if (endCodInfo.insideLOC && endCodInfo.LOCID[0].equals(startCodInfo.LOCID[0])) 
					  {
						  ///////GeneEnd设定///////////////////
						  if (Math.abs(endCodInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
						  {
							  geneEnd[0]="GeneEnd";
							  geneEnd[1]="left";
						  }
						  else {
							  geneEnd[0]="";
							  geneEnd[1]="";
						  }
						  ///////////////////////////////////////////////
						  int startExInType=startCodInfo.GeneInfo.get(0)[0];//起点所在区域类型 exon/intron
						  int startExInNum=startCodInfo.GeneInfo.get(0)[1];//exon/intron的位置
						  int endExInType=endCodInfo.GeneInfo.get(0)[0];//终点所在区域类型 exon/intron
						  int endExInNum=endCodInfo.GeneInfo.get(0)[1];//exon/intron的位置
						  ///////////peak 在一个Exon/Intron中/////////////////////////////
						  if (startExInType==endExInType && startExInNum==endExInNum) 
						  {
							  if (startExInType==1) {
								  exonID[0]=startExInNum+"";exonID[1]=startExInNum+"";
								  intronID[0]="";intronID[1]="";
							  }
							  else {
								  intronID[0]=startExInNum+"";intronID[1]=startExInNum+"";
								  exonID[0]="";exonID[1]="";
							  }
						  }
						  ///////////peak 不在一个Exon/Intron中////////////////////////////
						  else {
							  ///////起点落在外显子
							  if (startExInType==1) 
							  {
								  exonID[0]=startExInNum+"";
								  if (startExInNum!=ExonNum) 
									  intronID[0]=startExInNum+"";
								  else 
									  intronID[0]="";
							  }
							  else {//起点落在内含子
								  intronID[0]=startExInNum+"";
								  exonID[0]=startExInNum+1+"";
							  }
							  ///////终点落在外显子
							  if (endExInType==1) 
							  {
								  exonID[1]=endExInNum+"";
								  intronID[1]=endExInNum-1+"";
							  }
							  else 
							  {//终点落在内含子
								  intronID[1]=endExInNum+"";
								  exonID[1]=endExInNum+"";
							  }
						  }
						  ///////////////考虑放在最后/////////////////////////////////  
						  lsStartReadsInfo.add(GeneID);
						  lsStartReadsInfo.add(tss);
						  lsStartReadsInfo.add(exonID);
						  lsStartReadsInfo.add(intronID);
						  lsStartReadsInfo.add(geneEnd);
						  /////////////////////////////////////////////////////////////////////
					  }
					  /////////// peak 不 在 一 个 基 因 内 ///////////////////////////////////////////////////////////////
					 else 
					 {
						 geneEnd[0]="GeneEnd";
						 geneEnd[1]="both";					 
						 int startExInType=startCodInfo.GeneInfo.get(0)[0];//起点所在区域类型 exon/intron
						 int startExInNum=startCodInfo.GeneInfo.get(0)[1];//exon/intron的位置
						  ///////起点落在外显子
						  if (startExInType==1) {
							  exonID[0]=startExInNum+"";
							  if (startExInNum!=ExonNum) 
								  intronID[0]=startExInNum+"";
							  else 
								  intronID[0]="";
						  }
						  else {//起点落在内含子
							  intronID[0]=startExInNum+"";
							  exonID[0]=startExInNum+1+"";
						  }
						  exonID[1]=ExonNum+"";
						  if(intronID[0].equals(""))
							  intronID[1]=""; 
						  else
							  intronID[1]=ExonNum-1+"";
						  ///////////////考虑放在最后/////////////////////////////////  
						  lsStartReadsInfo.add(GeneID);
						  lsStartReadsInfo.add(tss);
						  lsStartReadsInfo.add(exonID);
						  lsStartReadsInfo.add(intronID);
						  lsStartReadsInfo.add(geneEnd);
						  /////////////////////////////////////////////////////////////////////
					 }
				  }
				  else //基因反向 
				  {
					  ///////GeneEnd设定///////////////////
					  if (Math.abs(startCodInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
					  {
						  geneEnd[0]="GeneEnd";
						  geneEnd[1]="left";
					  }
					  else {
						  geneEnd[0]="";
						  geneEnd[1]="";
					  }
					  //////////////////////////////////////////// 
					  //////////// Exon Intron 设 定 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					  /////////// peak 在 一 个 基 因 内 ///////////////////////////////////////////////////////////////
					  if (endCodInfo.insideLOC && endCodInfo.LOCID[0].equals(startCodInfo.LOCID[0])) 
					  {
						  if (endCodInfo.distancetoLOCStart[0]<DownStreamTssbp)
						  {
							  tss[0]="TSS";
							  tss[1]="right";
						  }
						  else {
							  tss[0]="";
							  tss[1]="";
						  }
						  int startExInType=startCodInfo.GeneInfo.get(0)[0];//起点所在区域类型 exon/intron
						  int startExInNum=startCodInfo.GeneInfo.get(0)[1];//exon/intron的位置
						  int endExInType=endCodInfo.GeneInfo.get(0)[0];//终点所在区域类型 exon/intron
						  int endExInNum=endCodInfo.GeneInfo.get(0)[1];//exon/intron的位置
						  ///////////peak 在一个Exon/Intron中/////////////////////////////
						  if (startExInType==endExInType&&startExInNum==endExInNum) 
						  {
							  if (startExInType==1) {
								  exonID[0]=ExonNum+1-startExInNum+"";exonID[1]=ExonNum+1-startExInNum+"";
								  intronID[0]="";intronID[1]="";
							  }
							  else {
								  intronID[0]=ExonNum-startExInNum+"";intronID[1]=ExonNum-startExInNum+"";
								  exonID[0]="";exonID[1]="";
							  }
						  }
						  ///////////peak 不在一个Exon/Intron中////////////////////////////
						  else {
							  ///////起点落在外显子
							  if (startExInType==1)
							  {
								  exonID[0]=ExonNum+1-startExInNum+"";
								  if (startExInNum!=1) 
									  intronID[0]=ExonNum+1-startExInNum+"";
								  else 
									  intronID[0]="";
							  }
							  else {//起点落在内含子
								  intronID[0]=ExonNum-startExInNum+"";
								  exonID[0]=ExonNum-startExInNum+1+"";
							  }
							  ///////终点落在外显子
							  if (endExInType==1) {
								  exonID[1]=ExonNum+1-endExInNum+"";
								  intronID[1]=ExonNum-endExInNum+"";
							  }
							  else {//终点落在内含子
								  intronID[1]=ExonNum-endExInNum+"";
								  exonID[1]=ExonNum-endExInNum+"";
							  }
						  }
						  ///////////////考虑放在最后/////////////////////////////////  
						  lsStartReadsInfo.add(GeneID);
						  lsStartReadsInfo.add(tss);
						  lsStartReadsInfo.add(exonID);
						  lsStartReadsInfo.add(intronID);
						  lsStartReadsInfo.add(geneEnd);
						  /////////////////////////////////////////////////////////////////////
					  }
					  /////////// peak 不 在 一 个 基 因 内 ///////////////////////////////////////////////////////////////
					 else 
					 {
						 tss[0]="TSS";
						 tss[1]="both";
						 int startExInType=startCodInfo.GeneInfo.get(0)[0];//起点所在区域类型 exon/intron
						 int startExInNum=startCodInfo.GeneInfo.get(0)[1];//exon/intron的位置
						  ///////起点落在外显子
						 if (startExInType==1) {
							  exonID[0]=ExonNum+1-startExInNum+"";
							  if (startExInNum!=1) 
								  intronID[0]=ExonNum+1-startExInNum+"";
							  else 
								  intronID[0]="";
						 }
						 ////////////////////////////////////////////////////
						 else {//起点落在内含子
							 intronID[0]=ExonNum-startExInNum+"";
							 exonID[0]=ExonNum-startExInNum+1+"";
						 }
						 exonID[1]=ExonNum+"";
						 if (intronID[0].equals("")) 
							 intronID[1]="";
						 else
							 intronID[1]=ExonNum-1+"";
						 ///////////////考虑放在最后/////////////////////////////////  
						 lsStartReadsInfo.add(GeneID);
						 lsStartReadsInfo.add(tss);
						 lsStartReadsInfo.add(exonID);
						 lsStartReadsInfo.add(intronID);
						 lsStartReadsInfo.add(geneEnd);
						 /////////////////////////////////////////////////////////////////////
					 }
				  }
				  lsReadsInfo.add(lsStartReadsInfo);
			  }
			  //起点不在基因内，那么只考虑本起点与上一个基因的关系
			  else {
				  ArrayList<String[]> lsStartReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1];
				  String[] tss=new String[2];//0:TSS 1:是在哪一边含有，none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:在哪一边含有，none，left，both
				  String[] exonID=new String[2];  //都按照split内部的顺序来，也就是说，如果基因反向，那么就从后往前数第几个外显子或内含子，这样子后期会比较好处理
				  String[] intronID=new String[2];//0:Exon/Intron的起点 1:Exon/Intron的终点
				  GeneID[0]="";
				  tss[0]="";tss[1]="";
				  geneEnd[0]="";geneEnd[1]="";
				  exonID[0]="";exonID[1]="";
				  intronID[0]="";intronID[1]="";			  
				  /////////上个基因正向并且距离上个基因的end很近
				  if (startCodInfo.LOCID[1]!=null&&startCodInfo.begincis5to3 && Math.abs(startCodInfo.distancetoLOCEnd[0])<GeneEnd3UTR) {
					  GeneID[0]=startCodInfo.LOCID[1];
					  geneEnd[0]="GeneEnd";geneEnd[1]="right";
				  }
				  else if (startCodInfo.LOCID[1]!=null&&!startCodInfo.begincis5to3 && Math.abs(startCodInfo.distancetoLOCStart[0])<UpStreamTSSbp) {
					  GeneID[0]=startCodInfo.LOCID[1];
					  tss[0]="TSS";tss[1]="left";	
				  }
				  ///////////////考虑放在最后/////////////////////////////////  
				  lsStartReadsInfo.add(GeneID);
				  lsStartReadsInfo.add(tss);
				  lsStartReadsInfo.add(exonID);
				  lsStartReadsInfo.add(intronID);
				  lsStartReadsInfo.add(geneEnd);
				  /////////////////////////////////////////////////////////////////////
				  lsReadsInfo.add(lsStartReadsInfo);
			  }
			  ////////////////////////////// 结 束 位 点 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  if (endCodInfo.insideLOC) //在基因内
			  {
				  GffDetail endDetail=endCodInfo.geneDetail[0];//获得该基因的具体信息
				  ArrayList<String[]> lsEndReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1]; GeneID[0]=endCodInfo.LOCID[0]; 
			
				  String[] tss=new String[2];//0:TSS 1:是在哪一边含有，none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:在哪一边含有，none，left，both
				  ArrayList<Integer> lsLongestSplit = (ArrayList<Integer>) ((GffDetailUCSCgene)endDetail).getLongestSplit().get(1);
				  int ExonNum=(lsLongestSplit.size()-2)/2;
				  ////////////// 测 试 代 码 //////////////////////////////////////////////////
				  if (lsLongestSplit.size()%2==1) 
					  System.out.println("lsLongestSplit数目不为偶数");
				  /////////////////////////////////////////////////////////////////////////////////
				  String[] exonID=new String[2];  //都按照split内部的顺序来，也就是说，如果基因反向，那么就从后往前数第几个外显子或内含子，这样子后期会比较好处理
				  String[] intronID=new String[2];//0:Exon/Intron的起点 1:Exon/Intron的终点
				  if (endCodInfo.begincis5to3) //基因正向
				  {
					  //////////// Exon Intron 设 定 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					  /////////// peak 不 在 一 个 基 因 内。peak在一个基因内的情况已经处理过了//////////////////////////////////////////////////////////////
					  if (startCodInfo.insideLOC && startCodInfo.LOCID[0].equals(endCodInfo.LOCID[0])) 
					  {
						  GeneID[0]="";
						  tss[0]="";tss[1]="";
						  geneEnd[0]="";geneEnd[1]="";
						  exonID[0]="";exonID[1]="";
						  intronID[0]="";intronID[1]="";	
						  lsEndReadsInfo.add(GeneID);
						  lsEndReadsInfo.add(tss);
						  lsEndReadsInfo.add(exonID);
						  lsEndReadsInfo.add(intronID);
						  lsEndReadsInfo.add(geneEnd);
						  lsReadsInfo.add(lsEndReadsInfo);
					  }
					  else
					  {
						  tss[0]="TSS";
						  tss[1]="both";
						  //////到本基因终点的设定//////////
						  if(Math.abs(endCodInfo.distancetoLOCEnd[0])<GeneEnd3UTR)
						  {
							  geneEnd[0]="GeneEnd";
							  geneEnd[1]="left";
						  }
						  else {
							  geneEnd[0]="";
							  geneEnd[1]="";
						  }
						 int endExInType=endCodInfo.GeneInfo.get(0)[0];//终点所在区域类型 exon/intron
						 int endExInNum=endCodInfo.GeneInfo.get(0)[1];//exon/intron的位置
						  /////// 终点落在外显子
						  if (endExInType==1) {
							  exonID[1]=endExInNum+"";
							  if (endExInNum==1) 
								  intronID[1]="";
							  else
								  intronID[1]=endExInNum-1+"";
						  }
						  else {// 终点落在内含子
							  intronID[1]=endExInNum+"";
							  exonID[1]=endExInNum+"";
						  }
						  exonID[0]=1+"";
						  if (intronID[1].equals("")) 
							  intronID[0]="";
						  else
							  intronID[0]=1+"";
						  ///////////////考虑放在最后/////////////////////////////////  
						  lsEndReadsInfo.add(GeneID);
						  lsEndReadsInfo.add(tss);
						  lsEndReadsInfo.add(exonID);
						  lsEndReadsInfo.add(intronID);
						  lsEndReadsInfo.add(geneEnd);
						  /////////////////////////////////////////////////////////////////////
						  lsReadsInfo.add(lsEndReadsInfo);
					 }
				  }
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				  else //基因反向 
				  {
					  geneEnd[0]="GeneEnd";
					  geneEnd[1]="both";
				  
					  //////////////////////////////////////////// 
					  //////////// Exon Intron 设 定 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					  /////////// peak 不 在 一 个 基 因 内 ///////////////////////////////////////////////////////////////
					  if (endCodInfo.insideLOC && endCodInfo.LOCID[0].equals(startCodInfo.LOCID[0])) {
						  GeneID[0]="";
						  tss[0]="";tss[1]="";
						  geneEnd[0]="";geneEnd[1]="";
						  exonID[0]="";exonID[1]="";
						  intronID[0]="";intronID[1]="";	
						  lsEndReadsInfo.add(GeneID);
						  lsEndReadsInfo.add(tss);
						  lsEndReadsInfo.add(exonID);
						  lsEndReadsInfo.add(intronID);
						  lsEndReadsInfo.add(geneEnd);
						  lsReadsInfo.add(lsEndReadsInfo);
					  }
					  else
					  {
						  //////到本基因终点的设定//////////
						  if(endCodInfo.distancetoLOCStart[0]<DownStreamTssbp)
						  {
							  tss[0]="TSS";
							  tss[1]="right";
						  }
						  else {
							  tss[0]="";
							  tss[1]="";
						  }
						 int endExInType=endCodInfo.GeneInfo.get(0)[0];//终点所在区域类型 exon/intron
						 int endExInNum=endCodInfo.GeneInfo.get(0)[1];//exon/intron的位置
						  ///////终点落在外显子
						 if (endExInType==1) {
							  exonID[1]=ExonNum+1-endExInNum+"";
							  if (endExInNum==ExonNum) 
								  intronID[1]="";
							  else
								  intronID[1]=ExonNum-endExInNum+"";
						 }
						 ////////////////////////////////////////////////////
						 else {//终点落在内含子
							 intronID[1]=ExonNum-endExInNum+"";
							 exonID[1]=ExonNum-endExInNum+"";
						 }
						 exonID[0]=1+"";
						 if (intronID[1].equals("")) 
							 intronID[0]="";
						 else
							 intronID[0]=1+"";
						 ///////////////考虑放在最后/////////////////////////////////  
						 lsEndReadsInfo.add(GeneID);
						 lsEndReadsInfo.add(tss);
						 lsEndReadsInfo.add(exonID);
						 lsEndReadsInfo.add(intronID);
						 lsEndReadsInfo.add(geneEnd);
						 /////////////////////////////////////////////////////////////////////
						 lsReadsInfo.add(lsEndReadsInfo);
					 }
				  }
			  }
			  //终点不在基因内，那么只考虑本终点与下一个基因的关系
			  else {
				  ArrayList<String[]> lsEndReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1];
				  String[] tss=new String[2];//0:TSS 1:是在哪一边含有，none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:在哪一边含有，none，left，both
				  String[] exonID=new String[2];  //都按照split内部的顺序来，也就是说，如果基因反向，那么就从后往前数第几个外显子或内含子，这样子后期会比较好处理
				  String[] intronID=new String[2];//0:Exon/Intron的起点 1:Exon/Intron的终点
				  GeneID[0]="";
				  tss[0]="";tss[1]="";
				  geneEnd[0]="";geneEnd[1]="";
				  exonID[0]="";exonID[1]="";
				  intronID[0]="";intronID[1]="";			  
				  ////////////////////////////////////////////////////////下个基因正向并且距离下个基因的TSS很近/////////////////////////////////////////////////////////////////////////
				  if (endCodInfo.LOCID[1]!=null&&endCodInfo.begincis5to3 && Math.abs(endCodInfo.distancetoLOCStart[0])<UpStreamTSSbp) {
					  GeneID[0]=endCodInfo.LOCID[1];
					  tss[0]="TSS";tss[1]="left";	
				  }
				  else if (endCodInfo.LOCID[1]!=null&&!endCodInfo.begincis5to3 && Math.abs(endCodInfo.distancetoLOCStart[0])<GeneEnd3UTR) {
					  GeneID[0]=endCodInfo.LOCID[1];
					  geneEnd[0]="GeneEnd";geneEnd[1]="right";
				  }
				  ////////////////////////////////////////////////////////////考虑放在最后///////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
				  lsEndReadsInfo.add(GeneID);
				  lsEndReadsInfo.add(tss);
				  lsEndReadsInfo.add(exonID);
				  lsEndReadsInfo.add(intronID);
				  lsEndReadsInfo.add(geneEnd);
				  /////////////////////////////////////////////////////////////////////	 
				  lsReadsInfo.add(lsEndReadsInfo);
			  }
			  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  int medGene=gffPairEndDetail.size();
			  for (int i = 2; i < medGene; i++)
			  {
				  GffDetailUCSCgene medDetailUCSCgene=(GffDetailUCSCgene) gffPairEndDetail.get(i);
				  ArrayList<String[]> lsMedReadsInfo=new ArrayList<String[]>();
				  String[] GeneID=new String[1];
				  String[] tss=new String[2];//0:TSS 1:是在哪一边含有，none, left,right,both  
				  String[] geneEnd=new String[2]; //0: GeneEnd 1:在哪一边含有，none，left，both
				  String[] exonID=new String[2];  //都按照split内部的顺序来，也就是说，如果基因反向，那么就从后往前数第几个外显子或内含子，这样子后期会比较好处理
				  String[] intronID=new String[2];//0:Exon/Intron的起点 1:Exon/Intron的终点
				  GeneID[0]=medDetailUCSCgene.locString;
				  tss[0]="TSS";tss[1]="both";
				  geneEnd[0]="GeneEnd";geneEnd[1]="both";
				  ArrayList<Integer> lsLongestSplit = (ArrayList<Integer>) medDetailUCSCgene.getLongestSplit().get(1);
				  exonID[0]="1";exonID[1]=(lsLongestSplit.size()-2)/2+"";
				  intronID[0]="1";intronID[1]=(lsLongestSplit.size()-2)/2-1+"";	
				  lsMedReadsInfo.add(GeneID);
				  lsMedReadsInfo.add(tss);
				  lsMedReadsInfo.add(exonID);
				  lsMedReadsInfo.add(intronID);
				  lsMedReadsInfo.add(geneEnd);
				  lsReadsInfo.add(lsMedReadsInfo);
			  }
			  return lsReadsInfo;
		}
		
		

	
}
