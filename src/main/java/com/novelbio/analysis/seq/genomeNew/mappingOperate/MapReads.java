package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.model.species.Species;
/**
 * 输入的mapping结果已经排序好，并且染色体已经分开好。
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
 
	 

	 /** 对于结果的标准化方法 */
	 int NormalType = NORMALIZATION_ALL_READS;
	 boolean uniqReads = false;
	 int startCod = -1;
	 boolean booUniqueMapping = true;
	 /** 仅选取某个方向的reads */
	 Boolean FilteredStrand = null;
	 Species species;
	 
	 long readsSize = 0;
	 
	 public void setSpecies(Species species) {
		mapChrID2Len = species.getMapChromInfo();
	}
	
	 /** 总共有多少reads参与了mapping，这个从ReadMapFile才能得到。 */
	public long getAllReadsNum() {
		long allReadsNum = 0;
		for (ChrMapReadsInfo chrMapReadsInfo : mapChrID2ReadsInfo.values()) {
			allReadsNum = allReadsNum + chrMapReadsInfo.getReadsChrNum();
		}
		return allReadsNum;
	}
	/**
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique  Unique的reads在哪一列 novelbio的标记在第七列，从1开始计算
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param cis5to3 是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean FilteredStrand) {
		this.uniqReads = uniqReads;
		this.startCod = startCod;
		this.booUniqueMapping = booUniqueMapping;
		this.FilteredStrand = FilteredStrand;
	}
	/**
	 * 从这里得到的实际某条染色体所包含的reads书目
	 */
	public long getChrReadsNum(String chrID) {
		return mapChrID2ReadsInfo.get(chrID.toLowerCase()).getReadsChrNum();
	}
	/**
	 * 从这里得到的实际某条染色体的高度总和
	 */
	public long getChrReadsPipNum(String chrID) {
		return mapChrID2ReadsInfo.get(chrID.toLowerCase()).getReadsPipNum();
	}
	/**
	 * 从这里得到的实际某条染色体高度的平均值
	 */
	public double getChrReadsPipMean(String chrID) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		return chrMapReadsInfo.getReadsPipNum()/chrMapReadsInfo.chrLength;
	}
	 /**
	  * 设定标准化方法，可以随时设定，不一定要在读取文件前
	  * 默认是NORMALIZATION_ALL_READS
	  * @param normalType
	  */
	 public void setNormalType(int normalType) {
		NormalType = normalType;
	}
	 
	private void setChrLenFromReadBed() {
		if (mapChrID2Len.size() > 0)
			return;
		
		String chrID = ""; BedRecord lastBedRecord = null;
		for (BedRecord bedRecord : bedSeq.readlines()) {
			if (!bedRecord.getRefID().equals(chrID)) {
				if (lastBedRecord != null) {
					mapChrID2Len.put(chrID.toLowerCase(), (long)lastBedRecord.getEndAbs());
				}
				chrID = bedRecord.getRefID();
			}
			lastBedRecord = bedRecord;
		}
		mapChrID2Len.put(lastBedRecord.getRefID().toLowerCase(), (long)lastBedRecord.getEndAbs());
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
	 * @throws Exception
	 */
	protected void ReadMapFileExp() throws Exception {
		setChrLenFromReadBed();
		BedRecord bedRecordFirst = bedSeq.readFirstLine();
		if (startCod > 0 && bedRecordFirst.isCis5to3() == null) {
			logger.error("不能设定startCod，因为没有设定方向列");
			return;
		}
		
		int[] chrBpReads = null;//保存每个bp的reads累计数
		String lastChr="";
		boolean flag = true;// 当没有该染色体时标记为false并且跳过所有该染色体上的坐标
		ChrMapReadsInfo chrMapReadsInfo = null;
		int[] tmpOld = new int[2];//更新 tmpOld
		
		int readsNum = 0;
		
		for (BedRecord bedRecord : bedSeq.readlines()) {
			readsNum++;
			
			String tmpChrID = bedRecord.getRefID().toLowerCase();
			if (!tmpChrID.equals(lastChr)) {
				tmpOld = new int[2];//更新 tmpOld
				
				if (!lastChr.equals("") && flag) { // 前面已经有了一个chrBpReads，那么开始总结这个chrBpReads
					chrMapReadsInfo.sumChrBp(chrBpReads);
				}
				lastChr = tmpChrID;// 实际这是新出现的ChrID
				logger.info(lastChr);
				
				Long chrLength = mapChrID2Len.get(lastChr.toLowerCase());
				flag = true;
				if (chrLength == null) {
					logger.error("出现未知chrID "+lastChr);
					flag = false; continue;
				}

				chrBpReads = new int[(int) (chrLength + 1)];// 同样为方便，0位记录总长度。这样实际bp就是实际长度
				chrBpReads[0] = chrLength.intValue();
				chrMapReadsInfo = new ChrMapReadsInfo(lastChr, getChrLen(lastChr), invNum, summeryType, FormulatToCorrectReads);
				mapChrID2ReadsInfo.put(lastChr, chrMapReadsInfo);
			}
			if (flag == false) //没有该染色体则跳过
				continue;
			tmpOld = addLoc(bedRecord, uniqReads, tmpOld, startCod, FilteredStrand, chrBpReads,chrMapReadsInfo);
			
			suspendCheck();
			if (flagStop) {
				break;
			}
			readsSize = readsSize + bedRecord.getRawStringInfo().getBytes().length;
			if (readsNum%1000 == 0) {
				MapReadsProcessInfo mapReadsProcessInfo = new MapReadsProcessInfo(readsSize);
				setRunInfo(mapReadsProcessInfo);
			}
		}
		if (flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads);
		}		
		return;
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
	protected int[] addLoc(BedRecord bedRecord,boolean uniqReads,int[] tmpOld,int startCod, Boolean cis5to3, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		boolean cis5to3This = bedRecord.isCis5to3();
		if ((cis5to3 != null && bedRecord.isCis5to3() != cis5to3)
				|| (booUniqueMapping && bedRecord.getMappingNum() > 1)
				) {
			return tmpOld;
		}
		
		int[] tmpStartEnd = new int[2];
		tmpStartEnd[0] = bedRecord.getStartAbs();
		tmpStartEnd[1] = bedRecord.getEndAbs();

		//如果本reads和上一个reads相同，则认为是线性扩增，跳过
		if (uniqReads && tmpStartEnd[0] == tmpOld[0] && tmpStartEnd[1] == tmpOld[1] ) {
			return tmpOld;
		}

		ArrayList<? extends Alignment> lsadd = null;
		//如果没有可变剪接
		lsadd = bedRecord.getAlignmentBlocks();
		lsadd = setStartCod(lsadd, startCod, cis5to3This);

		addChrLoc(chrBpReads, lsadd);
		chrMapReadsInfo.readsAllNum = chrMapReadsInfo.readsAllNum + 1;
		return tmpStartEnd;
	}
	/**
	 * 根据正反向截取相应的区域，最后返回需要累加的ArrayList<int[]>
	 * @param lsStartEnd
	 * @param cis5to3
	 * @return 如果cis5to3 = True，那么正着截取startCod长度的序列
	 * 如果cis5to3 = False，那么反着截取startCod长度的序列
	 */
	private ArrayList<? extends Alignment> setStartCod(ArrayList<? extends Alignment> lsStartEnd, int StartCodLen, boolean cis5to3) {
		if (StartCodLen <= 0) {
			return lsStartEnd;
		}
		ArrayList<Align> lsResult = new ArrayList<Align>();
		
		if (cis5to3) {
			for (int i = 0; i < lsStartEnd.size(); i++) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - lsStartEnd.get(i).Length() > 0) {
					Align align = new Align(alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align lastAlign = new Align(alignment.getStartAbs(), alignment.getStartAbs() + StartCodLen - 1);
					lsResult.add(lastAlign);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - alignment.Length() > 0) {
					Align align = new Align(alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					
					lsResult.add(0,align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align align = new Align(alignment.getEndAbs() - StartCodLen + 1, alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(0,align);
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
	private void addChrLoc(int[] chrLoc, ArrayList<? extends Alignment> lsAddLoc) {
		for (Alignment is : lsAddLoc) {
			for (int i = is.getStartAbs(); i <=is.getEndAbs(); i++) {
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
			return;
		}
		else if (NormalType == NORMALIZATION_ALL_READS) {
			for (int i = 0; i < doubleInfo.length; i++) {
				doubleInfo[i] = doubleInfo[i]*1000000/getAllReadsNum();
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


