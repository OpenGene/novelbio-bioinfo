package com.novelbio.analysis.seq.genomeNew.mappingOperate;


import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.log4j.Logger;
import org.junit.experimental.max.MaxCore;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 不考虑内存限制的编
 * 
 * @author zong0jie
 * 
 */
public class MapReads extends MapReadsAbs{
	private static Logger logger = Logger.getLogger(MapReads.class);
	
	/**
	 * 将每个double[]求和/double.length
	 * 也就是将每个点除以该gene的平均测序深度
	 */
	public static final int NORMALIZATION_PER_GENE = 128;
	/**
	 * 将每个double[]*1million/AllReadsNum
	 * 也就是将每个点除以测序深度
	 */
	public static final int NORMALIZATION_ALL_READS = 256;
	/** 不标准化 */
	public static final int NORMALIZATION_NO = 64;
	
	 /** 每条序列的reads数量，long[]只有0有效，只是为了地址传递 */
	 HashMap<String, long[]> mapChrID2ReadsNum = new HashMap<String, long[]>();
	 /** 每条序列的堆叠bp数量，long[]只有0有效，只是为了地址传递 */
	 HashMap<String, long[]> mapChrID2PipNum = new HashMap<String, long[]>();
	 /** 每条序列的平均堆叠高度，int[]只有0有效，只是为了地址传递 */
	 HashMap<String, Double> mapChrID2PipMean = new HashMap<String, Double>();
	 
	 /** ChrID所在的列 */
	 int colChrID = 0;
	 /** 起点所在的列 */
	 int colStartNum = 1;
	 /** 终点所在的列 */
	 int colEndNum = 2;
	 /** 方向列,bed文件一般在第六列 */
	 int colCis5To3 = 5;
	 /**
	  * 起点是否为开区间
	  * 常规的bed是1
	  */
	 int startRegion = 1;
	 /** 终点是否为开区间 */
	 int endRegion = 0;
	 /**
	  * 是否有剪接列，如果没有则小于0, 从bam转到的bed文件中才有的列，主要在RNA-Seq中使用
	  * 为第11列
	  */
	 int colSplit = -1;
	 /**
	  * 剪接列的起点等：如0,34,68，如果没有则小于0, 从bam转到的bed文件中才有的列，主要在RNA-Seq中使用
	  * 为第12列
	  */
	 int splitStart = -1;
	 /** 对于结果的标准化方法 */
	 int NormalType = NORMALIZATION_ALL_READS;
	 /** 总共有多少reads参与了mapping，这个从ReadMapFile才能得到。*/
	 long allReadsNum = 0;
	 boolean uniqReads = false;
	 int startCod = -1;
	 /**
	  * 标记mapping个数的列
	  */
	 int colUnique = BedRecord.COL_MAPNUM + 1;
	 boolean booUniqueMapping = true;
	 /** 仅选取某个方向的reads */
	 Boolean FilteredStrand = null;

	
	 /** 总共有多少reads参与了mapping，这个从ReadMapFile才能得到。 */
	public long getAllReadsNum() {
		return allReadsNum;
	}
	/**
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique  Unique的reads在哪一列 novelbio的标记在第七列，从1开始计算
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param cis5to3 是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique, boolean booUniqueMapping, Boolean FilteredStrand) {
		this.uniqReads = uniqReads;
		this.startCod = startCod;
		this.colUnique = colUnique;
		this.booUniqueMapping = booUniqueMapping;
		this.FilteredStrand = FilteredStrand;
	}

	/**
	 * @param invNum
	 *            每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 * @param mapFile
	 *            mapping的结果文件，一般为bed格式
	 */
	public MapReads(int invNum, String mapFile) {
		super(invNum, mapFile);
	}
	/**
	 * @param chrLenFile 给定文件，指定每条染色体的长度<br>
	 * 文件格式为： chrID \t chrLen   如 chr1 \t  23456
	 * @param invNum 每隔多少位计数
	 * @param mapFile mapping的结果文件，一般为bed格式
	 */
	public MapReads(String chrLenFile,int invNum, String mapFile) {
		super(chrLenFile, invNum, mapFile);
	}
	/**
	 * 从这里得到的实际某条染色体所包含的reads书目
	 */
	public long getChrReadsNum(String chrID) {
		return mapChrID2ReadsNum.get(chrID.toLowerCase())[0];
	}
	/**
	 * 从这里得到的实际某条染色体的长度
	 */
	public long getChrReadsPipNum(String chrID) {
		return mapChrID2PipNum.get(chrID.toLowerCase())[0];
	}
	/**
	 * 从这里得到的实际某条染色体高度的平均值
	 */
	public double getChrReadsPipMean(String chrID) {
		return mapChrID2PipMean.get(chrID.toLowerCase());
	}
	 /**
	  * 设定标准化方法，可以随时设定，不一定要在读取文件前
	  * 默认是NORMALIZATION_ALL_READS
	  * @param normalType
	  */
	 public void setNormalType(int normalType) {
		NormalType = normalType;
	}
	 /**
	  * <b>RNA-Seq使用</b><br>
	  * 剪接位点列的设定
	  * @param colSplit 是否有剪接列，如果没有则小于0, 从bam转到的bed文件中才有的列，主要在RNA-Seq中使用。如果有的话，一般为11列
	  * @param splitStart 剪接列的起点等：如0,34,68，如果没有则小于0, 从bam转到的bed文件中才有的列，主要在RNA-Seq中使用。如果有的话，一般为12列
	  */
	 public void setSplit( int colSplit, int splitStart)  {
		 colSplit--; splitStart--;
		 this.colSplit = colSplit;
		 this.splitStart = splitStart;
	 }
	 /**
	  * 起点是否为开区间,
	  * 常规的bed是1，bed文件不用修改
	  */
	public void setstartRegion(int startRegion) {
		this.startRegion = startRegion;
	}
	/**
	 * 设定坐标文件中ChrID和 起点，终点的列数，<b>如果是常规的bed文件，那么这个不用修改</b>
	 * @param colChrID ChrID所在的列
	 * @param colStartNum 起点所在的
	 * @param colEndNum 终点所在的列
	 */
	public void setColNum(int colChrID,int colStartNum,int colEndNum, int colCis5To3) {
		colChrID--; colStartNum--;colEndNum--;colCis5To3--;
		this.colChrID = colChrID;
		this.colStartNum = colStartNum;
		this.colEndNum = colEndNum;
		this.colCis5To3 = colCis5To3;
	}

	private void setChrLenFromReadBed() {
		if (mapChrID2Len.size() > 0)
			return;
		
		TxtReadandWrite txtMap = new TxtReadandWrite(mapFile, false);
		String chrID = ""; String[] lastSs = null;
		for (String content : txtMap.readlines()) {
			String[] ss = content.split("\t");
			if (!ss[colChrID].equals(chrID)) {
				if (lastSs != null) {
					mapChrID2Len.put(chrID.toLowerCase(), Long.parseLong(lastSs[colEndNum]));
				}
				chrID = ss[colChrID];
			}
			lastSs = ss;
		}
		mapChrID2Len.put(lastSs[colChrID].toLowerCase(), Long.parseLong(lastSs[colEndNum]));
	}
	/**
	 * 经过标准化，和equations修正
	 * 给定染色体，与起点和终点，返回该染色体上tag的密度分布，如果该染色体在mapping时候不存在，则返回null
	 * @param chrID 小写
	 * @param startLoc 起点坐标，为实际起点 如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endLoc 
	 * @param binNum 待分割的块数
	 * @return
	 */
	public  double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) {
		//首先将reads标准化为一个400-500bp宽的大块，每一块里面应该是该区域里面tags的总数，所以求该区域里面的最大值
		//然后再在大块上面统计，
		//大概估算了一下，基本上宽度在一个tag的1.5倍的时候计数会比较合理
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRengeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	/**
	 * 当输入为macs的bed文件时，自动<b>跳过chrm项目</b><br>
	 * 所有chr项目都小写
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。注意，mapping文件中的chrID和chrLengthFile中的chrID要一致，否则会出错
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique Unique的reads在哪一列
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param FilteredStrand 是否仅选取某一方向的reads，null不考虑
	 * @return 返回所有mapping的reads数量
	 * @throws Exception
	 */
	protected long ReadMapFileExp() throws Exception {
		setChrLenFromReadBed();
		colUnique--;
		if (startCod > 0 && colCis5To3 < 0) {
			logger.error("不能设定startCod，因为没有设定方向列");
			return -1;
		}
//		看一下startRegion是否起作用
 		//所谓结算就是说每隔invNum的bp就把这invNumbp内每个bp的Reads叠加数取平均或中位数，保存进chrBpReads中
		/////////////////////////////////////////获得每条染色体的长度并保存在hashChrLength中////////////////////////////////////////////////////
		int[] chrBpReads=null;//保存每个bp的reads累计数
		int[] SumChrBpReads=null;//直接从0开始记录，1代表第二个invNum,也和实际相同
		/////////////////读文件的准备工作///////////////////////////////////////////////////
		TxtReadandWrite txtmap=new TxtReadandWrite(mapFile,false);
		String lastChr="";
		long[] readsChrNum = new long[1];
		long[] readsPipNum = new long[1];
		////////////////////////////////////////////////////////////////////////////////////////////////
		//先假设mapping结果已经排序好，并且染色体已经分开好。
		boolean flag = true;// 当没有该染色体时标记为false并且跳过所有该染色体上的坐标
		int[] tmpOld = new int[2];
		for (String content : txtmap.readlines()) {
			String[] tmp = content.split(sep);
			if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) // 出现了新的chrID，则开始剪切老的chrBpReads,然后新建chrBpReads，最后装入哈希表
			{
				tmpOld = new int[2];//更新 tmpOld
				if (!lastChr.equals("") && flag){ // 前面已经有了一个chrBpReads，那么开始总结这个chrBpReads
					sumChrBp(chrBpReads, 1, SumChrBpReads, readsPipNum);
				}
				lastChr = tmp[colChrID].trim().toLowerCase();// 实际这是新出现的ChrID
				if (booPrintChrID) {
					System.out.println(lastChr);
				}
				int chrLength = 0;
				try {
					chrLength =  mapChrID2Len.get(lastChr.toLowerCase()).intValue();
					flag = true;
				} catch (Exception e) {
					logger.error("出现未知chrID "+lastChr);
					flag = false; continue;
				}

				chrBpReads = new int[chrLength + 1];// 同样为方便，0位记录总长度。这样实际bp就是实际长度
				chrBpReads[0] = (int) chrLength;
				// //////////SumChrBpReads设定//////////////////////////////////
				// 这个不是很精确，最后一位可能不准，但是实际应用中无所谓了,为方便，0位记录总长度。这样实际bp就是实际长度
				int SumLength = chrBpReads.length / invNum + 1;// 保证不会溢出，这里是要让SumChrBpReads长一点
				SumChrBpReads = new int[SumLength];// 直接从0开始记录，1代表第二个invNum,也和实际相同
				// //////////将新出现的chr装入哈希表////////////////////////////////
				mapChr2BpReads.put(lastChr, SumChrBpReads);// 将新出现的chrID和新建的SumChrBpReads装入hash表
				readsChrNum = new long[1];
				mapChrID2ReadsNum.put(lastChr, readsChrNum);
				readsPipNum = new long[1];
				mapChrID2PipNum.put(lastChr, readsPipNum);
				// ///////////将每一条序列长度装入lsChrLength///////////////////
				String[] tmpChrLen = new String[2];
				tmpChrLen[0] = lastChr;
				tmpChrLen[1] = chrLength + "";
				lsChrLength.add(tmpChrLen);
			}
			////////////////////按照位点加和chrBpReads////////////////////////////////
			if (flag == false) //没有该基因则跳过
				continue;
			//TODO 这里 uniqe mapping 的设置需要修改，因为万一 tmp[colUnique]里面不是数字呢？
			if (!booUniqueMapping || colUnique < 0 || tmp.length <= colUnique || Integer.parseInt(tmp[colUnique]) <= 1) {
				tmpOld = addLoc(tmp, uniqReads, tmpOld, startCod, FilteredStrand, chrBpReads,readsChrNum);
			}
		}
		///////////////////循环结束后还要将最后一次的内容做总结////////////////////////////////////
		if (flag) {
			sumChrBp(chrBpReads, 1, SumChrBpReads, readsPipNum);
		}
		 ////////////////////////////把lsChrLength按照chrLen从小到大进行排序/////////////////////////////////////////////////////////////////////////////
		  Collections.sort(lsChrLength,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1)
	            {
	               if( Integer.parseInt(arg0[1])<Integer.parseInt(arg1[1]))
	            	   return -1;
	            else if (Integer.parseInt(arg0[1])==Integer.parseInt(arg1[1])) 
					return 0;
	             else 
					return 1;
	            }
	        });
		  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		  allReadsNum = 0;
		  for (Entry<String, long[]> entry : mapChrID2ReadsNum.entrySet()) {
			allReadsNum = allReadsNum + entry.getValue()[0];
			mapChrID2PipMean.put(entry.getKey(), (double)mapChrID2PipNum.get(entry.getKey())[0]/mapChrID2Len.get(entry.getKey()));
		  }
		  return allReadsNum;
	}
	/**
	 * 具体加和的处理方法
	 * 给定一行信息，将具体内容加到对应的坐标上
	 * @param tmp 本行分割后的信息
	 * @param uniqReads 同一位点叠加后是否读取
	 * @param tmpOld 上一组的起点终点，用于判断是否是在同一位点叠加
	 * @param startCod 只截取前面一段的长度
	 * @param cis5to3 是否只选取某一个方向的序列，也就是其他方向的序列会被过滤，不参与叠加
	 * null表示不进行方向过滤
	 * @param chrBpReads 具体需要叠加的染色体信息
	 * @param readsNum 记录总共mapping的reads数量，为了能够传递下去，采用数组方式
	 * @return
	 * 本位点的信息，用于下一次判断是否是同一位点
	 */
	protected int[] addLoc(String[] tmp,boolean uniqReads,int[] tmpOld,int startCod, Boolean cis5to3, int[] chrBpReads, long[] readsNum) {
		boolean cis5to3This = true;
		if (colCis5To3 >= 0 && tmp.length > colCis5To3) {
			cis5to3This = tmp[colCis5To3].trim().equals("+");
		}
		if (cis5to3 != null && cis5to3This != cis5to3.booleanValue()) {
			return tmpOld;
		}
		
		int[] tmpStartEnd = new int[2];
		tmpStartEnd[0] = Integer.parseInt(tmp[colStartNum]) + startRegion;//本reads 的起点
		tmpStartEnd[1] = Integer.parseInt(tmp[colEndNum]) + endRegion;//本reads的终点

		//如果本reads和上一个reads相同，则认为是线性扩增，跳过
		if (uniqReads && tmpStartEnd[0] == tmpOld[0] && tmpStartEnd[1] == tmpOld[1] ) {
			return tmpOld;
		}

		ArrayList<int[]> lsadd = null;
		//如果没有可变剪接
		if (colSplit >= 0 && splitStart >=0) {
			lsadd = getStartEndLoc(tmpStartEnd[0], tmpStartEnd[1], tmp[colSplit], tmp[splitStart]);
			lsadd = setStartCod(lsadd, startCod, cis5to3This);
		}
		else {
			lsadd = getStartEndLoc(tmpStartEnd[0], tmpStartEnd[1], null,null);
			lsadd = setStartCod(lsadd, startCod, cis5to3This);
		}
		addChrLoc(chrBpReads, lsadd);
		readsNum[0]++;
		return tmpStartEnd;
	}
	/**
	 * Chr1	5242	5444	A80W3KABXX:8:44:8581:122767#GGCTACAT/2	255	-	5242	5444	255,0,0	2	30,20,40	0,120,160
	 * @param start 起点坐标 5242，绝对坐标闭区间
	 * @param end 终点坐标 5444，绝对坐标闭区间
	 * @param split 分割情况 30,20,40
	 * @param splitStart 每个分割点的起点 0,35,68
	 * @return 返回一组start和end
	 * 根据间隔进行区分
	 */
	private ArrayList<int[]> getStartEndLoc(int start, int end, String split, String splitStart) {
		ArrayList<int[]> lsStartEnd = new ArrayList<int[]>();
		if (split == null || split.equals("") || !split.contains(",")) {
			int[] startend = new int[2];
			startend[0] = start;
			startend[1] = end;
			lsStartEnd.add(startend);
			return lsStartEnd;
		}
		String[] splitLen = split.trim().split(",");
		String[] splitLoc = splitStart.trim().split(",");
		for (int i = 0; i < splitLen.length; i++) {
			int[] startend = new int[2];
			startend[0] = start + Integer.parseInt(splitLoc[i]);
			startend[1] = startend[0] + Integer.parseInt(splitLen[i]) - 1;
			lsStartEnd.add(startend);
		}
		return lsStartEnd;
	}
	/**
	 * 根据正反向截取相应的区域，最后返回需要累加的ArrayList<int[]>
	 * @param lsStartEnd
	 * @param cis5to3
	 * @return 如果cis5to3 = True，那么正着截取startCod长度的序列
	 * 如果cis5to3 = False，那么反着截取startCod长度的序列
	 */
	private ArrayList<int[]> setStartCod(ArrayList<int[]> lsStartEnd, int StartCodLen, boolean cis5to3) {
		if (StartCodLen <= 0) {
			return lsStartEnd;
		}
		ArrayList<int[]> lsResult = new ArrayList<int[]>();
		if (cis5to3) {
			for (int i = 0; i < lsStartEnd.size(); i++) {
				if (StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1) > 0) {
					lsResult.add(lsStartEnd.get(i));
					StartCodLen = StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1);
				}
				else {
					int[] last = new int[2];
					last[0] = lsStartEnd.get(i)[0];
					last[1] = lsStartEnd.get(i)[0] + StartCodLen - 1;
					lsResult.add(last);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				if (StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1) > 0) {
					lsResult.add(0,lsStartEnd.get(i));
					StartCodLen = StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] + 1);
				}
				else {
					int[] last = new int[2];
					last[1] = lsStartEnd.get(i)[1];
					last[0] = last[1] - StartCodLen + 1;
					lsResult.add(0,last);
				}
			}
		}
		return lsResult;
	}
	/**
	 * 给定一条序列的坐标信息，以及本次需要累加的坐标区域
	 * 将该区域的坐标累加到目的坐标上去
	 * @param chrLoc 坐标位点，0为坐标长度，1开始为具体坐标，所以chrLoc[123] 就是实际123位的坐标
	 * @param lsAddLoc 间断的坐标区域，为int[2] 的list，譬如 100-250，280-300这样子，注意提供的坐标都是闭区间，所以首位两端都要加上
	 */
	private void addChrLoc(int[] chrLoc, ArrayList<int[]> lsAddLoc) {
		for (int[] is : lsAddLoc) {
			for (int i = is[0]; i <= is[1]; i++) {
				if (i >= chrLoc.length) {
					logger.info("超出范围："+ i);
					break;
				}
				if (i < 0) {
					logger.info("超出范围："+ i);
					continue;
				}
				chrLoc[i]++;
			}
		}
	}
	/**
	 * 提取的原始数据需要经过标准化再输出。
	 * 本方法进行标准化
	 * 输入的double直接修改，不返回。<br>
	 * 最后得到的结果都要求均值
	 * 给定double数组，按照reads总数进行标准化,reads总数由读取的mapping文件自动获得<br>
	 * 最后先乘以1million然后再除以每个double的值<br>
	 * @param doubleInfo 提取得到的原始value
	 * @return 
	 */
	public void normDouble(double[] doubleInfo) {
		if (doubleInfo == null) {
			return;
		}
		if (NormalType == NORMALIZATION_NO) {
			//就是啥也不干
		}		
		else if (NormalType == NORMALIZATION_ALL_READS) {
			for (int i = 0; i < doubleInfo.length; i++) {
				doubleInfo[i] = doubleInfo[i]*1000000/allReadsNum;
			}
		}
		else if (NormalType == NORMALIZATION_PER_GENE) {
			double avgSite = MathComput.mean(doubleInfo);
			if (avgSite != 0) {
				for (int i = 0; i < doubleInfo.length; i++) {
					doubleInfo[i] = doubleInfo[i]/avgSite;
				}
			}
		}
	}
}
