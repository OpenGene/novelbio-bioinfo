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
public class MapReads {
	Equations equations;
	public void setEquations(Equations equations) {
		this.equations = equations;
		//默认设定基因组上reads的最小值为0，凡是校正小于0的都改为0
		equations.setMin(0);
	}
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
	/**
	 * 不标准化
	 */
	public static final int NORMALIZATION_NO = 64;
	/**
	 * 用来保存每个染色体中的基因坐标-invNum精度里面的reads数目
	 * chrID(小写)--int[]
	 * 直接从0开始记录，1代表第二个invNum,也和实际相同
	 */
	 Hashtable<String, int[]> hashChrBpReads=new Hashtable<String, int[]>();
	/**
	 * 用来保存mapping文件中出现过的每个chr 的长度
	 */
	 ArrayList<String[]> lsChrLength=new ArrayList<String[]>();
	 int invNum=10;
	 int tagLength=300;//由ReadMapFile方法赋值
	 String sep = "\t";
	 /**
	  * 序列信息,名字都为小写
	  */
	 HashMap<String, Long> hashChrLen = new HashMap<String, Long>();
	 /**
	  * 每条序列的reads数量，long[]只有0有效，只是为了地址传递
	  */
	 HashMap<String, long[]> hashChrReadsNum = new HashMap<String, long[]>();
	 /**
	  * 每条序列的堆叠bp数量，long[]只有0有效，只是为了地址传递
	  */
	 HashMap<String, long[]> hashChrPipNum = new HashMap<String, long[]>();
	 /**
	  * 每条序列的平均堆叠高度，int[]只有0有效，只是为了地址传递
	  */
	 HashMap<String, Double> hashChrPipMean = new HashMap<String, Double>();
	 /**
	  * 具体的mapping文件
	  */
	 String mapFile = "";
	 /**
	  * ChrID所在的列
	  */
	 int colChrID = 0;
	 /**
	  * 起点所在的列
	  */
	 int colStartNum = 1;
	 /**
	  * 终点所在的列
	  */
	 int colEndNum = 2;
	 /**
	  * 方向列,bed文件一般在第六列
	  */
	 int colCis5To3 = 5;
	 /**
	  * 起点是否为开区间
	  * 常规的bed是1
	  */
	 int startRegion = 1;
	 /**
	  * 终点是否为开区间
	  */
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
	 int NormalType = NORMALIZATION_ALL_READS;
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
	 public void setSplit( int colSplit, int splitStart)
	 {
		 colSplit--; splitStart--;
		 this.colSplit = colSplit;
		 this.splitStart = splitStart;
	 }
	 /**
	  * 总共有多少reads参与了mapping，这个从ReadMapFile才能得到。
	  */
	 long allReadsNum = 0;
	 /**
	  * 总共有多少reads参与了mapping，这个从ReadMapFile才能得到。
	  */
	 public long getAllReadsNum() {
		return allReadsNum;
	 }
	 /**
	  * 设定peak的bed文件，第一列为chrID，第二列为起点，第三列为终点，
	  * 返回去除peak后，每条染色体的bg情况
	  * @param peakBedFile
	  * @param firstlinels1
	  * @return ls-0：chrID 1：bg
	  * 其中第一位是chrAll的信息
	  */
	 public ArrayList<String[]> getChIPBG(String peakBedFile, int firstlinels1)
	 {
		 ArrayList<String[]> lsResult = new ArrayList<String[]>();
		 ListHashBin gffHashPeak = new ListHashBin(true, 1, 2, 3, firstlinels1);
		 gffHashPeak.ReadGffarray(peakBedFile);
		 
		 double allReads = 0; int numAll = 0; double max = 0;
		 ArrayList<Integer> lsMidAll = new ArrayList<Integer>();
		 for (Entry<String, int[]> entry : hashChrBpReads.entrySet()) {
			String chrID = entry.getKey();
			double allReadsChr = 0; int numChr = 0; double maxChr = 0;
			ArrayList<Integer> lsMidChr = new ArrayList<Integer>();
			int[] info = entry.getValue();
			for (int i = 0; i < info.length; i++) {
				if (info[i] == 0) { 
					continue;
				}
				ListCodAbs<ListDetailBin> gffcodPeak = gffHashPeak.searchLocation(chrID, i*invNum);
				if (gffcodPeak != null && gffcodPeak.isInsideLoc()) {
					continue;
				}
				if (maxChr < info[i]) {
					maxChr = info[i];
				}
				if (lsMidChr.size() < 50000) {
					lsMidChr.add(info[i]);
				}
				allReadsChr = allReadsChr + info[i];
				numChr ++;
			}
			if (numChr != 0) {
				double med75 = MathComput.median(lsMidChr, 75);
				lsResult.add(new String[]{chrID, (double)allReadsChr/numChr+"", maxChr+"",  med75 + ""});
			}
			if (max < maxChr) {
				max = maxChr;
			}
			lsMidAll.addAll(lsMidChr);
			allReads = allReads + allReadsChr;
			numAll = numAll + numChr;
		 }
		 double med75All = MathComput.median(lsMidAll, 75);
		 lsResult.add(0, new String[]{"chrAll", (double)allReads/numAll + "", max + "", med75All + ""});
		 return lsResult;
	 }
	 
	 
	 static boolean booPrintChrID = true;
	 public static void setBooPrintChrID(boolean booPrintChrID) {
		 MapReads.booPrintChrID = booPrintChrID;
	 }
	 
	/**
	 * 设定双端readsTag拼起来后长度的估算值，目前solexa双端送样长度大概是300bp，不用太精确
	 * 默认300
	 * 这个是方法：getReadsDensity来算reads密度的东西
	 * @param readsTagLength
	 */
	public  void setTagLength(int thisTagLength) {
		tagLength=thisTagLength;
	}
	/**
	 * <b>一般不用修改</b>
	 * mapFile 里面用什么分隔符进行分隔的，默认是"\t"
	 */
	public void setSep(String sep) {
		this.sep = sep;
	}
	 /**
	  * 起点是否为开区间,
	  * 常规的bed是1，bed文件不用修改
	  */
	public void setstartRegion(int startRegion) {
		this.startRegion = startRegion;
	}
//	/**
//	 * 是否有剪接列，如果没有则小于0, 从bam转到的bed文件中才有的列，主要在RNA-Seq中使用
//	 * 为第11列
//	 */
//	public void setColSplit(int colSplit) {
//		this.colSplit = colSplit;
//	}
	/**
	 * 设定坐标文件中ChrID和 起点，终点的列数，<b>如果是常规的bed文件，那么这个不用修改</b>
	 * @param colChrID ChrID所在的列
	 * @param colStartNum 起点所在的
	 * @param colEndNum 终点所在的列
	 */
	public void setColNum(int colChrID,int colStartNum,int colEndNum, int colCis5To3)
	{
		colChrID--; colStartNum--;colEndNum--;colCis5To3--;
		this.colChrID = colChrID;
		this.colStartNum = colStartNum;
		this.colEndNum = colEndNum;
		this.colCis5To3 = colCis5To3;
	}
	boolean uniqReads = false;
	int startCod = -1;
	/**
	 * 标记mapping个数的列
	 */
	int colUnique = BedRecord.COL_MAPNUM + 1;
	boolean booUniqueMapping = true;
	Boolean cis5to3 = null;
	/**
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique Unique的reads在哪一列 novelbio的标记在第七列，从1开始计算
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param cis5to3 是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique, boolean booUniqueMapping, Boolean cis5to3)
	{
		this.uniqReads = uniqReads;
		this.startCod = startCod;
		this.colUnique = colUnique;
		this.booUniqueMapping = booUniqueMapping;
		this.cis5to3 = cis5to3;
	}
	/**
	 * @param invNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 * @param mapFile mapping的结果文件，一般为bed格式
	 */
	public MapReads(int invNum, String mapFile) 
	{
		this.invNum = invNum;
		this.mapFile = mapFile;
	}
	String chrLenFile = "";
	/**
	 * 设定染色体长度文件
	 * @param chrLenFile
	 */
	public void setChrLenFile(String chrLenFile) {
		this.chrLenFile = chrLenFile;
		readChrLenFile(chrLenFile);
	}
	/**
	 * @param chrLenFile 给定文件，指定每条染色体的长度<br>
	 * 文件格式为： chrID \t chrLen   如 chr1 \t  23456
	 * @param invNum 每隔多少位计数
	 * @param mapFile mapping的结果文件，一般为bed格式
	 */
	public MapReads(String chrLenFile,int invNum, String mapFile) 
	{
		hashChrLen = new HashMap<String, Long>();
		this.invNum = invNum;
		readChrLenFile(chrLenFile);
		this.mapFile = mapFile;
	}
	/**
	 * 每隔多少位取样,如果设定为1，则算法会变化，然后会很精确
	 * @return
	 */
	public int getBinNum() {
		return invNum;
	}
	/**
	 * 从mapping文件中获得每条染色体的长度，
	 * 要求mapping文件必须排过序，然后获得每个chr的最长reads到多长
	 * @throws Exception 
	 */
	private void setHashChrLen() throws Exception
	{
		if (hashChrLen.size() > 0) {
			return;
		}
		if (readChrLenFile(chrLenFile)) {
			return;
		}
		
		
		TxtReadandWrite txtMap = new TxtReadandWrite(mapFile, false);
		BufferedReader readerMap = txtMap.readfile();
		String content = ""; String chrID = ""; 
		String[] preSs = null;
		while ((content = readerMap.readLine()) != null) {
			String[] ss = content.split("\t");
			if (!ss[colChrID].equals(chrID)) {
				if (preSs != null) {
					hashChrLen.put(chrID.toLowerCase(), Long.parseLong(preSs[colEndNum]));
				}
				chrID = ss[colChrID];
			}
			preSs = ss;
		}
		hashChrLen.put(preSs[colChrID].toLowerCase(), Long.parseLong(preSs[colEndNum]));
	}
	
	private boolean readChrLenFile(String chrLenFile)
	{
		if (FileOperate.isFileExist(chrLenFile)) {
			try {
				TxtReadandWrite txtChrLen = new TxtReadandWrite(chrLenFile, false);
				ArrayList<String> lsChrLen = txtChrLen.readfileLs();
				for (String string : lsChrLen) {
					String[] ss = string.split("\t");
					hashChrLen.put(ss[0].toLowerCase(), Long.parseLong(ss[1]));
				}
				return true;
			} catch (Exception e) {
				logger.error("no chrLenFile file");
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	public long ReadMapFile()
	{
		try {
			return ReadMapFileExp();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 当输入为macs的bed文件时，自动<b>跳过chrm项目</b><br>
	 * 所有chr项目都小写
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。注意，mapping文件中的chrID和chrLengthFile中的chrID要一致，否则会出错
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique Unique的reads在哪一列
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param cis5to3 是否仅选取某一方向的reads，null不考虑
	 * @return 返回所有mapping的reads数量
	 * @throws Exception
	 */
	private long ReadMapFileExp() throws Exception 
	{
		setHashChrLen();
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
		int[] tmpOld = new int[2]; int count = 0;
		for (String content : txtmap.readlines()) {
			String[] tmp = content.split(sep);
			if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) // 出现了新的chrID，则开始剪切老的chrBpReads,然后新建chrBpReads，最后装入哈希表
			{
				tmpOld = new int[2];//更新 tmpOld
				if (!lastChr.equals("") && flag){ // 前面已经有了一个chrBpReads，那么开始总结这个chrBpReads
					sumChrBp(chrBpReads, 1, SumChrBpReads, readsPipNum);
				}
				lastChr = tmp[colChrID].trim().toLowerCase();// 实际这是新出现的ChrID
				// ////////////////释放内存，感觉加上这段有点用，本来内存到1.2g，加了后降到990m///////////////////////////
				if (booPrintChrID) {
//					if (count%200 == 0) {
						System.out.println(lastChr);
//					}
				}
//				chrBpReads = null;// 看看能不能释放掉内存
//				System.gc();// 显式调用gc
				int chrLength = 0;
				// ///////chrBpReads设定/////////////////////////
				try {
					chrLength =  hashChrLen.get(lastChr.toLowerCase()).intValue();
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
				hashChrBpReads.put(lastChr, SumChrBpReads);// 将新出现的chrID和新建的SumChrBpReads装入hash表
				readsChrNum = new long[1];
				hashChrReadsNum.put(lastChr, readsChrNum);
				readsPipNum = new long[1];
				hashChrPipNum.put(lastChr, readsPipNum);
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
				tmpOld = addLoc(tmp, uniqReads, tmpOld, startCod, cis5to3, chrBpReads,readsChrNum);
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
		  for (Entry<String, long[]> entry : hashChrReadsNum.entrySet()) {
			allReadsNum = allReadsNum + entry.getValue()[0];
			hashChrPipMean.put(entry.getKey(), (double)hashChrPipNum.get(entry.getKey())[0]/hashChrLen.get(entry.getKey()));
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
	 * 给定一条序列的坐标信息，以及本次需要累加的坐标区域
	 * 将该区域的坐标累加到目的坐标上去
	 * @param chrLoc 坐标位点，0为坐标长度，1开始为具体坐标，所以chrLoc[123] 就是实际123位的坐标
	 * @param lsAddLoc 间断的坐标区域，为int[2] 的list，譬如 100-250，280-300这样子，注意提供的坐标都是闭区间，所以首位两端都要加上
	 */
	protected void addChrLoc(int[] chrLoc, ArrayList<int[]> lsAddLoc)
	{
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
	 * Chr1	5242	5444	A80W3KABXX:8:44:8581:122767#GGCTACAT/2	255	-	5242	5444	255,0,0	2	30,20,40	0,120,160
	 * @param start 起点坐标 5242，绝对坐标闭区间
	 * @param end 终点坐标 5444，绝对坐标闭区间
	 * @param split 分割情况 30,20,40
	 * @param splitStart 每个分割点的起点 0,35,68
	 * @return 返回一组start和end
	 * 根据间隔进行区分
	 */
	protected ArrayList<int[]> getStartEndLoc(int start, int end, String split, String splitStart) {
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
	protected ArrayList<int[]> setStartCod(ArrayList<int[]> lsStartEnd, int StartCodLen, boolean cis5to3) {
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
	 * 给定chrBpReads，将chrBpReads里面的值按照invNum区间放到SumChrBpReads里面
	 * 因为是引用传递，里面修改了SumChrBpReads后，外面会变掉
	 * @param chrBpReads 每个碱基的reads累计值
	 * @param invNum 区间
	 * @param type 取值类型，中位数或平均值，0中位数，1均值 其他的默认中位数
	 * @param SumChrBpReads 将每个区间内的
	 */
	protected void sumChrBp(int[] chrBpReads,int type,int[] SumChrBpReads, long[] chrReadsPipNum) 
	{
		 int SumLength = chrBpReads.length/invNum - 1;//保证不会溢出，因为java默认除数直接忽略小数而不是四舍五入
		 if (invNum == 1) {
			for (int i = 0; i < SumLength; i++) {
				SumChrBpReads[i] = chrBpReads[i+1];
				chrReadsPipNum[0] = chrReadsPipNum[0] + chrBpReads[i+1];
			}
			return;
		 }
		 for (int i = 0; i < SumLength; i++)
		 {
			 int[] tmpSumReads=new int[invNum];//将总的chrBpReads里的每一段提取出来
			 int sumStart=i*invNum + 1; int k=0;//k是里面tmpSumReads的下标，实际下标就行，不用-1
			 for (int j = sumStart; j < sumStart + invNum; j++) 
			 {
				 tmpSumReads[k] = chrBpReads[j];
				 chrReadsPipNum[0] = chrReadsPipNum[0] + chrBpReads[j];
				 k++;
			 }
			 if (type==0) //每隔一段区域取样，建议每隔10bp取样，取中位数
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
			 else if (type==1) 
				 SumChrBpReads[i]=(int) MathComput.mean(tmpSumReads);
			 else //默认取中位数
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
		 }
	}
	/**
	 * 考虑将非unique mapping的reads进行减分处理
	 * 给定chrBpReads，将chrBpReads里面的值按照invNum区间放到SumChrBpReads里面
	 * 因为是引用传递，里面修改了SumChrBpReads后，外面会变掉
	 * @param chrBpReads 每个碱基的reads累计值
	 * @param invNum 区间
	 * @param type 取值类型，中位数或平均值，0中位数，1均值 其他的默认中位数
	 * @param SumChrBpReads 将每个区间内的
	 */
	protected void sumChrBp(double[] chrBpReads,int type,int[] SumChrBpReads, long[] chrReadsPipNum) 
	{
		 int SumLength = chrBpReads.length/invNum - 1;//保证不会溢出，因为java默认除数直接忽略小数而不是四舍五入
		 if (invNum == 1) {
			for (int i = 0; i < SumLength; i++) {
				SumChrBpReads[i] = (int) Math.round(chrBpReads[i+1]);
				chrReadsPipNum[0] = chrReadsPipNum[0] + (int) Math.round(chrBpReads[i+1]);
			}
			return;
		}
		 for (int i = 0; i < SumLength; i++)
		 {
			 int[] tmpSumReads=new int[invNum];//将总的chrBpReads里的每一段提取出来
			 int sumStart=i*invNum + 1; int k=0;//k是里面tmpSumReads的下标，实际下标就行，不用-1
			 for (int j = sumStart; j < sumStart + invNum; j++) 
			 {
				 tmpSumReads[k] = (int) Math.round(chrBpReads[j]);
				 chrReadsPipNum[0] = chrReadsPipNum[0] + (int) Math.round(chrBpReads[j]);
				 k++;
			 }
			 if (type==0) //每隔一段区域取样，建议每隔10bp取样，取中位数
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
			 else if (type==1) 
				 SumChrBpReads[i]=(int) MathComput.mean(tmpSumReads);
			 else //默认取中位数
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
		 }
	}
	/**
	 * 经过标准化，和equations修正
	 * 输入坐标区间，和每个区间的bp数，返回该段区域内reads的数组
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间，如果该染色体在mapping时候不存在，则返回null
	 * @param thisInvNum 每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数
	 * 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点，如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endNum 终点坐标，为实际终点
	 * 如果(endNum - startNum + 1) / thisInvNum >0.7，则将binNum设置为1
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 如果没有找到该染色体位点，则返回null
	 */
	public  double[] getRengeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type)
	{	
		if (startNum <=0 && endNum <=0) {
			startNum = 1; endNum = (int)getChrLen(chrID);
		}
		if (startNum > endNum) {
			logger.error("起点不能比终点大: "+chrID+" "+startNum+" "+endNum);
		}
		////////////////////////不需要分割了////////////////////////////////////////
		if (invNum == 1 && thisInvNum == 1) {
			double[] result = new double[endNum - startNum + 1];
			startNum--; endNum--;
			int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
			if (invNumReads == null) {
				logger.info("没有该染色体： " + chrID);
				return null;
			}
			int k = 0;
			for (int i = startNum; i <= endNum; i++) {
				result[k] = invNumReads[i];
				k++;
			}
			//标准化
			normDouble(result, NormalType);
			if (equations != null) {
				result = equations.getYinfo(result);
			}
			return result;
		}
		///////////////////////////////////////////////////////////////////////////////
		double binNum = (double)(endNum - startNum + 1) / thisInvNum;
		int binNumFinal = 0;
		if (binNum - (int)binNum >= 0.7) {
			binNumFinal = (int)binNum + 1;
		}
		else {
			binNumFinal = (int)binNum;
		}
		//内部经过标准化了
		double[] tmp = getRengeInfo( chrID, startNum, endNum, binNumFinal,type);
		return tmp;
	}
	
	
	/**
	 * 经过标准化，和equations修正
	 * 输入坐标区间，需要划分的块数，返回该段区域内reads的数组。如果该染色体在mapping时候不存在，则返回null
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点 如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endNum 终点坐标，为实际终点
	 * @param binNum 待分割的区域数目
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 如果没有找到该染色体位点，则返回null
	 */
	public  double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type) 
	{
		if (startNum <=0 && endNum <=0) {
			startNum = 1; endNum = (int)getChrLen(chrID);
		}
		
		if (startNum > endNum) {
			logger.error("起点不能比终点大: "+chrID+" "+startNum+" "+endNum);
		}
		
		int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
		if (invNumReads == null) {
			return null;
		}
		startNum--; endNum--;
		////////////////确定要提取区域的左端点和右端点/////////////////////////////////
		int leftNum = 0;//在invNumReads中的实际起点
		int rightNum = 0;//在invNumReads中的实际终点

		leftNum = startNum/invNum;
		double leftBias = (double)startNum/invNum-leftNum;//最左边分隔到起点的距离比值
		double rightBias = 0;
		if (endNum%invNum==0) 
			rightNum = endNum/invNum-1;//前提是java小数转成int通通直接去掉小数点
		else 
		{
			rightNum = endNum/invNum;//前提是java小数转成int通通直接去掉小数点
			rightBias = rightNum + 1 - (double)endNum/invNum;//最右边分隔到终点的距离比值
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] tmpRegReads=new double[rightNum - leftNum + 1];
		int k=0;
		try {
			for (int i = leftNum; i <= rightNum; i++) {
				if (i >= invNumReads.length) {
					break;
				}
				if (i < 0) {
					continue;
				}
				tmpRegReads[k] = invNumReads[i];
				k++;
			}
		} catch (Exception e) {
			logger.error("下标越界"+e.toString());
		}
		normDouble(tmpRegReads, NormalType);
		double[] tmp = null;
		try {
			tmp =  MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
		} catch (Exception e) {
//			tmp =  MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
			return null;
		}
		if (equations != null) {
			tmp = equations.getYinfo(tmp);
		}
		return tmp;
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
	public  double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) 
	{
		//首先将reads标准化为一个400-500bp宽的大块，每一块里面应该是该区域里面tags的总数，所以求该区域里面的最大值
		//然后再在大块上面统计，
		//大概估算了一下，基本上宽度在一个tag的1.5倍的时候计数会比较合理
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum= getRengeInfo(tagBinLength, chrID, startLoc, endLoc,1);
//		normDouble(tmpReadsNum, NormalType);
	/**	for (int i = 0; i < tmpReadsNum.length; i++) {
			if(tmpReadsNum[i]>1)
				System.out.println(tmpReadsNum[i]);
		}
		*/
		if (tmpReadsNum==null) {
			return null;
		}
		
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	/**
	 * 获得Mapping文件中最长和最短chr的长度
	 * @param refID
	 * @return int[]
	 * 0: 最短chr长度
	 * 1: 最长chr长度
	 */
	public int[] getLimChrLength()
	{
		int[] result=new int[2];
		result[0]=Integer.parseInt(lsChrLength.get(0)[1]);
		result[1]=Integer.parseInt(lsChrLength.get(lsChrLength.size()-1)[1]);
		return result;
	}
	
	/**
	 *  用于mRNA的计算，经过标准化，和equations修正
	 * 输入坐标区间，需要划分的块数，返回该段区域内reads的数组。如果该染色体在mapping时候不存在，则返回null
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间
	 * @param chrID 一定要小写
	 * @param binNum 待分割的区域数目<b>当binNum为-1时，不进行总结，直接返回invNum统计的结果，返回的结果不等长</b>
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @param lsIsoform 该区域的转录本情况，<b>正向int[0]&lt;int[1]，反向int[0]&gt;int[1]</b>
	 * @return 返回的都是正向，结果还需要后续处理，根据正反向做反向
	 * 如果没找到转录本，如chr1_random之类ID的，那就跳过
	 */
	public double[] getRengeInfo(String chrID,int binNum,int type,ArrayList<int[]> lsIsoform) {
		if (lsIsoform == null || lsIsoform.size() == 0) {
			return null;
		}
		boolean cis5to3 = true;
		ArrayList<double[]> lsExonInfo = new ArrayList<double[]>();
		/**
		 * 如果lsIsoform中int[0]<int[1]说明正向
		 * 如果lsIsoform中int[1]<int[0]说明反向
		 */
		for (int[] is : lsIsoform) {
			if (is[0] < is[1]) {
				break;
			}
			else if (is[0] > is[1]) {
				cis5to3 = false;
				break;
			}
			else {
				logger.error("转录本中外显子的起点和终点坐标一样"+is[0]+" "+is[1]);
			}
		}
		
		if (cis5to3) {
			for (int[] is : lsIsoform) {
				double[] isoInfo = getRengeInfo(invNum, chrID, is[0], is[1], type);
				if (isoInfo != null) {
					lsExonInfo.add(isoInfo);
				}
			}
		}
		else {
			for (int i = lsIsoform.size() - 1; i >= 0; i--) {
				double[] isoInfo = getRengeInfo(invNum, chrID, lsIsoform.get(i)[1], lsIsoform.get(i)[0], type);
				if (isoInfo != null) {
					lsExonInfo.add(isoInfo);
				}
			}
		}
		int num = 0;
		for (double[] ds : lsExonInfo) {
			if (ds == null) {
				logger.error("无法提出reads信息，染色体为: " + chrID);
				return null;
			}
			num = num + ds.length;
		}
		double[] result = new double[num];
		int i = 0;
		for (double[] ds : lsExonInfo) {
			for (double d : ds) {
				result[i] = d;
				i++;
			}
		}
		if (binNum <= 0) {
			return result;
		}
		double[] resultTagDensityNum=MathComput.mySpline(result, binNum, 0, 0, 0);
		return resultTagDensityNum;
	}
	
	/**
	 * 输入的double直接修改，不返回。<br>
	 * 最后得到的结果都要求均值
	 * 给定double数组，按照reads总数进行标准化,reads总数由读取的mapping文件自动获得<br>
	 * 最后先乘以1million然后再除以每个double的值<br>
	 * @param doubleInfo
	 * @param NormType 参数选择MapReads的NORMALIZATION类,如果不在其中，则不修改
	 * @return 
	 */
	public void normDouble(double[] doubleInfo, int NormalType) {
		if (doubleInfo == null) {
			return;
		}
		if (NormalType == NORMALIZATION_NO) 
		{
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
	
	/**
	 * 经过标准化
	 * 给定坐标范围，返回该区间内最小值
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double
	 */
	public double regionMin(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		return new Min().evaluate(info);
	}
	
	/**
	 * 给定坐标范围，返回该区间内最大值
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double
	 */
	public double regionMax(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		return new Max().evaluate(info);
	}
	/**
	 * 经过标准化
	 * 给定坐标范围，返回该区间内平均值
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double
	 */
	public double regionMean(String chrID, int startLoc, int endLoc)
	{
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		if (info == null) {
			return -1;
		}
		return new Mean().evaluate(info);
	}
	/**
	 * 如果invNum不为1，则可能不精确
	 * 给定坐标范围，看该区域内有多少0区域
	 * @param chrID
	 * @param startLoc 无所谓哪个在前，绝对坐标从1开始
	 * @param endLoc
	 * @return arrayList[]:0区域的绝对坐标区间
	 * 
	 */
	public ArrayList<int[]> region0Info(String chrID, int startLocT, int endLocT)
	{
		int startLoc = Math.min(startLocT, endLocT);
		int endLoc = Math.max(startLocT, endLocT);
		startLoc--; endLoc--;
		int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
		if (startLoc < 0 || endLoc >= invNumReads.length) {
			logger.error("越界了："+ chrID + " " + startLoc + " " + endLoc);
			return null;
		}
		ArrayList<int[]> lsResult = new ArrayList<int[]>();
		
		boolean flag0 = false;
		int[] region = null;
		for (int i = startLoc; i < endLoc; i++) {
			if (invNumReads[i] == 0 && !flag0) {
				region = new int[2];
				region[0] = i+1;
				region[1] = i + 1;
				lsResult.add(region);
				flag0 = true;
			}
			else if (invNumReads[i] == 0 && flag0) {
				region[1] = i+1;
			}
			else if (invNumReads[i] != 0) {
				flag0 = false;
			}
		}
		return lsResult;
	}
	/**
	 * 给定坐标范围，返回该区间内标准差
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double
	 */
	public double regionSD(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		return new StandardDeviation().evaluate(info);
	}
	/**
	 * 
	 * 给定坐标范围，返回该区间内标准差
	 * @param chrID 染色体编号
	 * @param lsLoc 一个转录本的exon list
	 * @return
	 */
	public double regionSD(String chrID, List<int[]> lsLoc) {
		return new StandardDeviation().evaluate(getRegionInfo(chrID, lsLoc));
	}
	/**
	 * 给定坐标范围，返回该区间内平均值
	 * @param chrID
	 * @param lsLoc 一个转录本的exon list
	 * @return
	 */
	public double regionMean(String chrID, List<int[]> lsLoc)
	{
		return new Mean().evaluate(getRegionInfo(chrID, lsLoc));
	}
	/**
	 * 给定坐标范围，返回该区间内的信息
	 * @param chrID
	 * @param lsLoc 一个转录本的exon list
	 * @return
	 */
	public double[] getRegionInfo(String chrID, List<int[]> lsLoc) {
		ArrayList<double[]> lstmp = new ArrayList<double[]>();
		for (int[] is : lsLoc) {
			int min = Math.min(is[0], is[1]);
			int max = Math.max(is[0], is[1]);
			double[] info = getRengeInfo(invNum, chrID, min,max, 0);
			lstmp.add(info);
		}
		int len = 0;
		for (double[] ds : lstmp) {
			len = len + ds.length;
		}
		//生成最终长度的double
		double[] finalReads = new double[len];
		int index = 0;
		for (double[] ds : lstmp) {
			for (double d : ds) {
				finalReads[index] = d;
				index ++ ;
			}
		}
		return finalReads;
	}
	
	
	
	
	
	/**
	 * 填充每个MapInfo
	 * 经过标准化，和equations修正
	 * @param lsmapInfo
	 * @param thisInvNum  每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegionLs(List<MapInfo> lsmapInfo, int thisInvNum, int type) {
		for (MapInfo mapInfo : lsmapInfo) {
			double[] Info = getRengeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), type);
			mapInfo.setDouble(Info);
		}
	}
	/**
	 * 填充每个MapInfo，如果没有找到该染色体位点，则填充null

	 * 经过标准化，和equations修正
	 * @param lsmapInfo
	 * @param thisInvNum  每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegion(MapInfo mapInfo, int thisInvNum, int type) {
		double[] Info = getRengeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), type);
		mapInfo.setDouble(Info);
	}
	/**
	 * 经过标准化
	 * 将MapInfo中的double填充上相应的reads信息
	 * @param binNum 待分割的区域数目
	 * @param lsmapInfo
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegionLs(int binNum, List<MapInfo> lsmapInfo, int type) {
		for (int i = 0; i < lsmapInfo.size(); i++) {
			MapInfo mapInfo = lsmapInfo.get(i);
			double[] Info = getRengeInfo(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), binNum, type);
			if (Info == null) {
				lsmapInfo.remove(i); i--;
				logger.error("出现未知ID："+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStart() + " "+ mapInfo.getEnd());
				continue;
			}
			mapInfo.setDouble(Info);
		}
	}
	/**
	 * 经过标准化
	 * 将MapInfo中的double填充上相应的reads信息
	 * @param binNum 待分割的区域数目
	 * @param lsmapInfo
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRegion(int binNum, MapInfo mapInfo, int type) {
			double[] Info = getRengeInfo(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), binNum, type);
			if (Info == null) {
				logger.error("出现未知ID："+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStart() + " "+ mapInfo.getEnd());
			}
			mapInfo.setDouble(Info);
	}
	/**
	 * 从这里得到的实际某条染色体的长度
	 */
	public long getChrLen(String chrID) {
		return hashChrLen.get(chrID.toLowerCase());
	}
	/**
	 * 从这里得到的实际某条染色体所包含的reads书目
	 */
	public long getChrReadsNum(String chrID) {
		return hashChrReadsNum.get(chrID.toLowerCase())[0];
	}
	/**
	 * 从这里得到的实际某条染色体的长度
	 */
	public long getChrReadsPipNum(String chrID) {
		return hashChrPipNum.get(chrID.toLowerCase())[0];
	}
	/**
	 * 从这里得到的实际某条染色体高度的平均值
	 */
	public double getChrReadsPipMean(String chrID) {
		return hashChrPipMean.get(chrID.toLowerCase());
	}
	
	/**
	 * 返回所有chrID的list
	 * @return
	 */
	public ArrayList<String> getChrIDLs() {
		return ArrayOperate.getArrayListKey(hashChrLen);
	}
	
	/**
	 * 给定坐标信息，将比较的比值，也就是均值相除，放入mapInfo的weight内
	 * 内部标准化
	 * @param mapReads 第一个mapReads信息
	 * @param mapReads2 第二个mapReads信息
	 * @param mapInfo
	 */
	public static void CmpMapReg(MapReads mapReads, MapReads mapReads2, MapInfo mapInfo)
	{
		double value1 = mapReads.regionMean(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd());
		double value2 = mapReads2.regionMean(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd());
		mapInfo.setScore(value1/value2);
	}
	
}
