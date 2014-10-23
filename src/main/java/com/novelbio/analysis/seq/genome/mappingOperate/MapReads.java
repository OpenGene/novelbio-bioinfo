package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.model.species.Species;
import com.novelbio.listOperate.ListAbs;
import com.novelbio.listOperate.ListCodAbs;
/**
 * 输入的mapping结果已经排序好，并且染色体已经分开好。
 * 不考虑内存限制的编<br>
 * 注意在添加之前要先执行{@link #prepareAlignRecord(AlignRecord)}
 * @author zong0jie
 * 
 */
public class MapReads extends MapReadsAbs implements AlignmentRecorder {
	/**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的中位数 */
	public static final int SUM_TYPE_MEDIAN = 2;
	/**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的平均数 */
	public static final int SUM_TYPE_MEAN = 3;
	/**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的总和 */
	public static final int SUM_TYPE_SUM = 4;
	

	private static Logger logger = Logger.getLogger(MapReads.class);

	 boolean uniqReads = false;
	 int startCod = -1;

	 /** 仅选取某个方向的reads */
	 Boolean FilteredStrand = null;
	 Species species;
	 
	 AlignSeq alignSeqReader;
	 
	 HashMap<String, ChrMapReadsInfo> mapChrID2ReadsInfo = new HashMap<String, ChrMapReadsInfo>();
	 int tagLength = 300;//由ReadMapFile方法赋值
	 /** 用这个类来依次添加reads */
	 MapReadsAddAlignRecord mapReadsAddAlignRecord;

	 int summeryType = SUM_TYPE_MEAN;
	 
	 /**每隔多少位计数，如果设定为1，则算法会变化，然后会很精确*/
	 int invNum = 10;
	 /** 因为想加入小数，但是double比较占内存，所以就将数据乘以fold，然后最后除掉它就好 */
	 int fold = 100;
	 /**添加samBam的文件用来获得信息
	  * 注意在添加之前要先执行{@link #prepareAlignRecord(AlignRecord)}
	  */
	 public void addAlignRecord(AlignRecord alignRecord) {
		 mapReadsAddAlignRecord.addAlignRecord(alignRecord);
	 }
	 /**
	  * 每隔多少位取样,如果设定为1，则算法会变化，然后会很精确
	  * @return
	  */
	 public int getBinNum() {
		 return invNum;
	 }
		
	 /**
	  * 将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的中位数或平均数<br>
	  * SUM_TYPE_MEDIAN，SUM_TYPE_MEAN<br>
	  * <b>默认为SUM_TYPE_MEAN</b>
	  */
	 public void setSummeryType(int summeryType) {
		this.summeryType = summeryType;
	}
	 
	 /**每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	  * 默认为10
	  * */
	 public void setInvNum(int invNum) {
		this.invNum = invNum;
	}
	 
	 public void setBedSeq(String bedSeqFile) {
		 alignSeqReader = new BedSeq(bedSeqFile);
	}
	 public void setAlignSeqReader(AlignSeq alignSeqReader) {
		 this.alignSeqReader = alignSeqReader;
	}

	 /** 总共有多少reads参与了mapping，这个从ReadMapFile才能得到。 */
	public long getAllReadsNum() {
		if (allReadsNum > 0) {
			return (long)allReadsNum;
		}
		for (ChrMapReadsInfo chrMapReadsInfo : mapChrID2ReadsInfo.values()) {
			allReadsNum = allReadsNum + chrMapReadsInfo.getReadsChrNum();
		}
		return (long)allReadsNum;
	}
	
	/**
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads 默认false
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则延长，默认-1
	 * @param booUniqueMapping 重复的reads是否只选择一条 默认为true
	 * @param FilteredStrand 是否仅选取某一方向的reads，null不考虑 默认为null
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
		return (double)chrMapReadsInfo.getReadsPipNum()/chrMapReadsInfo.chrLength;
	}
	
	/**
	 * 设定双端readsTag拼起来后长度的估算值，目前solexa双端送样长度大概是300bp，不用太精确
	 * 默认300
	 * 这个是方法：getReadsDensity来算reads密度的东西
	 * @param readsTagLength
	 */
	public  void setTagLength(int thisTagLength) {
		tagLength = thisTagLength;
	}
	
	protected boolean isUniqueMapping() {
		return booUniqueMapping;
	}
	 
	private void setChrLenFromReadBed() {
		if (mapChrID2Len.size() > 0)
			return;
		
		String chrID = ""; AlignRecord lastAlignRecord = null;
		for (AlignRecord alignRecord : alignSeqReader.readLines()) {
			if (!alignRecord.getRefID().equals(chrID)) {
				if (lastAlignRecord != null) {
					mapChrID2Len.put(chrID.toLowerCase(), (long)lastAlignRecord.getEndAbs());
				}
				chrID = alignRecord.getRefID();
			}
			lastAlignRecord = alignRecord;
		}
		mapChrID2Len.put(lastAlignRecord.getRefID().toLowerCase(), (long)lastAlignRecord.getEndAbs());
	}
	
	 /**
	  * 设定peak的bed文件，第一列为chrID，第二列为起点，第三列为终点，
	  * 返回去除peak后，每条染色体的bg情况
	  * @param peakBedFile
	  * @param firstlinels1
	  * @return ls-0：chrID 1：bg
	  * 其中第一位是chrAll的信息
	  */
	 public ArrayList<String[]> getChIPBG(String peakBedFile, int firstlinels1) {
		 ArrayList<String[]> lsResult = new ArrayList<String[]>();
		 ListHashBin gffHashPeak = new ListHashBin(true, 1, 2, 3, firstlinels1);
		 gffHashPeak.ReadGffarray(peakBedFile);
		 
		 double allReads = 0; int numAll = 0; double max = 0;
		 ArrayList<Integer> lsMidAll = new ArrayList<Integer>();
		 for (Entry<String, ChrMapReadsInfo> entry : mapChrID2ReadsInfo.entrySet()) {
			String chrID = entry.getKey();
			double allReadsChr = 0; int numChr = 0; double maxChr = 0;
			ArrayList<Integer> lsMidChr = new ArrayList<Integer>();
			int[] info = entry.getValue().getSumChrBpReads();
			for (int i = 0; i < info.length; i++) {
				if (info[i] == 0) { 
					continue;
				}
				info[i] = info[i]/fold;
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

	/**
	 * 经过标准化，和equations修正
	 * 给定染色体，与起点和终点，返回该染色体上tag的密度分布，如果该染色体在mapping时候不存在，则返回null
	 * @param chrID 小写
	 * @param startLoc 起点坐标，为实际起点 如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endLoc 
	 * @param binNum 待分割的块数
	 * @return
	 */
	public  double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum ) {
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRangeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	/**
	 * 给定需要计算的区域，装在ArrayList-ExonInfo里面，返回仅仅考虑这些区域的基因组分布密度图<br>
	 * 给马红那边的杨红星开发的。他提出想看全基因组上tss区域的甲基化分布情况，exon区域的甲基化分布情况。<br>
	 * 他的思路是用一定长度的slide window划过基因组然后看该位点内有甲基化的基因的表达情况。<br>
	 * 那么我的做法就是除了tss区域，其他区域的甲基化全部设定为0，也就是仅保留指定lsExonInfos内的甲基化，然后后面走常规步骤<br>
	 * @param lsExonInfos
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param binNum
	 * @return
	 */
	//TODO 还没做好
	public  double[] getReadsDensity(ListAbs<ExonInfo> lsExonInfos, String chrID, int startLoc, int endLoc, int binNum ) {
		//首先将reads标准化为一个400-500bp宽的大块，每一块里面应该是该区域里面tags的总数，所以求该区域里面的最大值
		//然后再在大块上面统计，
		//大概估算了一下，基本上宽度在一个tag的1.5倍的时候计数会比较合理
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRangeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	/**
	 * 经过标准化，和equations修正
	 * 输入坐标区间，和每个区间的bp数，返回该段区域内reads的数组
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间，如果该染色体在mapping时候不存在，则返回null
	 * @param thisInvNum 每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数<br>
	 * 如果thisInvNum <= 0，则thisInvNum = invNum<br>
	 * 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点，如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endNum 终点坐标，为实际终点
	 * 如果(endNum - startNum + 1) / thisInvNum >0.7，则将binNum设置为1
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 如果没有找到该染色体位点，则返回null
	 */
	public double[] getRangeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type) {
		double[] result = null;
		if (!mapChrID2ReadsInfo.containsKey(chrID.toLowerCase())) {
			logger.info("cannot find this chromosome: " + chrID);
			return result;
		}
		////////////////////////不需要分割了////////////////////////////////////////
		if (thisInvNum <= 0) {
			thisInvNum = invNum;
		}
		if (invNum == 1 && thisInvNum == 1) {
			result = getRangeInfoInv1(chrID, startNum, endNum);
		} else {
			result = getRangeInfoNorm(chrID, thisInvNum, startNum, endNum, type);
		}
		return result;
	}
	/**
	 * 间断为1的精确版本，经过标准化，和equations修正
	 * @param chrID 染色体ID
	 * @param startNum 实际起点，从1开始记数
	 * @param endNum 实际终点，从1开始记数
	 */
	private double[] getRangeInfoInv1(String chrID, int startNum, int endNum) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		if (chrMapReadsInfo == null) {
			logger.info("cannot find this chromosome: " + chrID);
			return null;
		}
		int[] startEnd = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		startEnd[0] = startEnd[0] - 1;
		startEnd[1] = startEnd[1] - 1;
		double[] result = new double[startEnd[1] - startEnd[0] + 1];
		
		int[] invNumReads = chrMapReadsInfo.getSumChrBpReads();
		if (mapChrID2LsAlignmentFilter != null && mapChrID2LsAlignmentFilter.containsKey(chrID.toLowerCase())) {
			List<? extends Alignment> lsAlignments = mapChrID2LsAlignmentFilter.get(chrID.toLowerCase());
			invNumReads = cleanInfoNotInAlignment(lsAlignments, invNumReads, 1);
		}
		int k = 0;
		for (int i = startEnd[0]; i <= startEnd[1]; i++) {
			result[k] = (double)invNumReads[i]/fold;
			k++;
		}
		//标准化
		normDouble(NormalType, result, getAllReadsNum());
		result = equationsCorrect(result);
		return result;
	}
	/** 常规的版本，经过标准化，和equations修正
	 * @param lsAlignments 是否仅绘制lsAlignments范围内的信息
	 * @param chrID 染色体ID
	 * @param thisInvNum 每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数
	 * @param startNum
	 * @param endNum
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 *  */
	private double[] getRangeInfoNorm(String chrID, int thisInvNum, int startNum, int endNum, int type) {
		int[] startEndLoc = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		if (startEndLoc == null) {
			return null;
		}
		double binNum = (double)(startEndLoc[1] - startEndLoc[0] + 1) / thisInvNum;
		int binNumFinal = 0;
		if (binNum - (int)binNum >= 0.7) {
			binNumFinal = (int)binNum + 1;
		} else {
			binNumFinal = (int)binNum;
		}
		if (binNumFinal == 0) {
			binNumFinal = 1;
		}
		//内部经过标准化了
		double[] tmp = getRangeInfo(chrID, startEndLoc[0], startEndLoc[1], binNumFinal, type);
		return tmp;
	}
	/**
	 * 经过标准化，和equations修正
	 * 输入坐标区间，需要划分的块数，返回该段区域内reads的数组。如果该染色体在mapping时候不存在，则返回null
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间
	 * @param lsAlignments 将不属于指定区段内的数值全部清空，最好是linkedlist
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点 如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endNum 终点坐标，为实际终点
	 * @param binNum 待分割的区域数目
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 如果没有找到该染色体位点，则返回null
	 * @return
	 */
	protected double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		if (!mapChrID2ReadsInfo.containsKey(chrID.toLowerCase())) {
			logger.error("没有该染色体：" + chrID);
			return null;
		}
		int[] startEnd = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		int[] invNumReads = chrMapReadsInfo.getSumChrBpReads();
		if (invNumReads == null) {
			return null;
		}
		if (mapChrID2LsAlignmentFilter != null && mapChrID2LsAlignmentFilter.containsKey(chrID.toLowerCase())) {
			List<? extends Alignment> lsAlignments = mapChrID2LsAlignmentFilter.get(chrID.toLowerCase());
			invNumReads = cleanInfoNotInAlignment(lsAlignments, invNumReads, invNum);
		}
		
		try {
			return getRengeInfoExp(invNumReads, startEnd[0], startEnd[1], binNum, type);
		} catch (Exception e) {
			return null;
		}
	}
	
	//TODO check
	/**
	 * 给定list区段，和全基因组的信息，将没有被list区段覆盖到的信息全部删除
	 * @param lsAlignments 里面的alignment是实际数目
	 * @param invNumReads 从0开始计数，每个单元表示一个invNum，所以计数的时候要加上1
	 * @param binNum
	 * @return
	 */
	private static int[] cleanInfoNotInAlignment(List<? extends Alignment> lsAlignments, int[] invNumReads, int invNum) {
		Queue<Alignment> lsAlignmentThis = new LinkedList<Alignment>();
		for (Alignment alignment : lsAlignments) {
			lsAlignmentThis.add(alignment);
		}
		int[] result = new int[invNumReads.length];
		int i = 0;
		Alignment alignment = lsAlignmentThis.poll();
		while (!lsAlignmentThis.isEmpty() && i < invNumReads.length) {
			if((i+1) * invNum < alignment.getStartAbs()) {
				i++;
			} else if ((i+1) * invNum > alignment.getEndAbs()) {
				alignment = lsAlignmentThis.poll();
			} else {
				result[i] = invNumReads[i];
				i++;
			}
		}
		return result;
	}

	/**
	 * @param invNumReads 某条染色体上面的reads堆叠情况
	 * @param startNum 实际num
	 * @param endNum 实际num
	 * @param binNum 分割的份数，小于0就直接返回
	 * @param type
	 * @return
	 */
	protected double[] getRengeInfoExp(int[] invNumReads, int startNum,int endNum,int binNum,int type) {
		startNum--; endNum--;
		int leftNum = 0;//在invNumReads中的实际起点
		int rightNum = 0;//在invNumReads中的实际终点

		leftNum = startNum/invNum;
		double leftBias = (double)startNum/invNum-leftNum;//最左边分隔到起点的距离比值
		double rightBias = 0;
		if (endNum%invNum==0) {
			rightNum = endNum/invNum - 1;//java小数转成int 为直接去掉小数点
		} else  {
			rightNum = endNum/invNum;
			rightBias = rightNum + 1 - (double)endNum/invNum;//最右边分隔到终点的距离比值
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] tmpRegReads=new double[rightNum - leftNum + 1];
		int k=0;
		for (int i = leftNum; i <= rightNum; i++) {
			if (i >= invNumReads.length || k >= tmpRegReads.length) {
				break;
			}
			if (i < 0) {
				continue;
			}
			tmpRegReads[k] = (double)invNumReads[i]/fold;
			k++;
		}

		normDouble(NormalType, tmpRegReads, getAllReadsNum());
		double[] tmp = null;
		if (binNum <= 0) {
			tmp = tmpRegReads;
		} else {
			try {
				tmp = MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
			} catch (Exception e) {
				return null;
			}
		}
		
		try {
			tmp = equationsCorrect(tmp);
		} catch (Exception e) {
			return tmp;
		}
		
		return tmp;
	}

	/**
	 * 当输入为macs的bed文件时，自动<b>跳过chrm项目</b><br>
	 * 所有chr项目都小写
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。注意，mapping文件中的chrID和chrLengthFile中的chrID要一致，否则会出错
	 * @throws Exception
	 */
	protected void ReadMapFileExp() throws Exception {
		allReadsNum = 0;
		setChrLenFromReadBed();
		AlignRecord alignRecordFirst = alignSeqReader.readFirstLine();
		if (!prepareAlignRecord(alignRecordFirst)) {
			return;
		}
		int readsNum = 0;
		for (AlignRecord alignRecord : alignSeqReader.readLines()) {
			mapReadsAddAlignRecord.addAlignRecord(alignRecord);
			readsNum++;
			suspendCheck();
			if (flagStop) {
				break;
			}
			if (readsNum%1000 == 0) {
				MapReadsProcessInfo mapReadsProcessInfo = new MapReadsProcessInfo(alignSeqReader.getReadByte());
				setRunInfo(mapReadsProcessInfo);
			}
		}
		mapReadsAddAlignRecord.summary();
	}
	
	/**
	 * 准备添加reads信息。主要是初始化mapReadsAddAlignRecord
	 * 此外就是判定一下startCod是否能用
	 * @param alignRecordFirst
	 * @return 如果设setFilter中定了 startCod > 0 并且reads没有方向
	 * 则返回false
	 */
	public boolean prepareAlignRecord(AlignRecord alignRecordFirst) {
		mapReadsAddAlignRecord = new MapReadsAddAlignRecord(this);
		if (startCod > 0 && alignRecordFirst.isCis5to3() == null) {
			logger.error("不能设定startCod，因为没有设定方向列");
			return false;
		}
		return true;
	}
	@Override 
	public void summary() {
		mapReadsAddAlignRecord.summary();
	}
	
	@Override
	public Align getReadingRegion() {
		return null;
	}
	
	/** 释放内存 */
	public void clear() {
		//TODO
		alignSeqReader = null;
		try {
			for (ChrMapReadsInfo chrMapReadsInfo : mapChrID2ReadsInfo.values()) {
				chrMapReadsInfo.clear();
			}
			mapChrID2ReadsInfo.clear();
		} catch (Exception e) { }
		try {
			mapChrID2LsAlignmentFilter.clear();
		} catch (Exception e) { }
		try {
			mapChrID2Len.clear();
		} catch (Exception e) { }
		
	}
}

/**
 * 本类的作用就是将reads的信息装入MapReads的mapChrID2ReadsInfo中去。
 * 把这个模块独立出来可以方便一次读取bam文件，然后做好多事情
 * @author zong0jie
 *
 */
class MapReadsAddAlignRecord {
	private static final Logger logger = Logger.getLogger(MapReadsAddAlignRecord.class);
	MapReads mapReads;
	int[] chrBpReads = null;//保存每个bp的reads累计数
	String lastChr="";
	boolean flag = true;// 当没有该染色体时标记为false并且跳过所有该染色体上的坐标
	ChrMapReadsInfo chrMapReadsInfo = null;
	int[] tmpOld = new int[2];//更新 tmpOld
	 /** 因为想加入小数，但是double比较占内存，所以就将数据乘以fold，然后最后除掉它就好 */
	int fold;
	public MapReadsAddAlignRecord(MapReads mapReads) {
		this.mapReads = mapReads;
		this.fold = mapReads.fold;
	}
	
	public void addAlignRecord(AlignRecord alignRecord) {
		String tmpChrID = alignRecord.getRefID().toLowerCase();
		if (!tmpChrID.equals(lastChr)) {
			tmpOld = new int[2];//更新 tmpOld
			summary();
			lastChr = tmpChrID;// 实际这是新出现的ChrID
			logger.info(lastChr);
			
			Long chrLength = mapReads.mapChrID2Len.get(lastChr.toLowerCase());
			flag = true;
			if (chrLength == null) {
				logger.error("出现未知chrID "+lastChr);
				flag = false; return;
			}

			chrBpReads = new int[(int) (chrLength + 1)];// 同样为方便，0位记录总长度。这样实际bp就是实际长度
			chrBpReads[0] = chrLength.intValue();
			chrMapReadsInfo = mapReads.mapChrID2ReadsInfo.get(lastChr);
			if (chrMapReadsInfo == null) {
				chrMapReadsInfo = new ChrMapReadsInfo(lastChr, mapReads);
			}
			mapReads.mapChrID2ReadsInfo.put(lastChr, chrMapReadsInfo);
		}
		//没有该染色体则跳过
		if (flag == false) return;
		tmpOld = addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
	
		chrMapReadsInfo.addReadsAllNum((double)1/alignRecord.getMappedReadsWeight());
	}
	
	public void summary() {
		if (!lastChr.equals("") && flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads, fold);
			chrBpReads = null;
			lastChr = "";
		}
	}
	/**
	 * 具体加和的处理方法
	 * 给定一行信息，将具体内容加到对应的坐标上
	 * @param alignRecord reads信息
	 * @param tmpOld 上一组的起点终点，用于判断是否是在同一位点叠加
	 * @param chrBpReads 具体需要叠加的染色体信息 0位：chrLength
	 * @param chrMapReadsInfo 记录总共mapping的reads数量，为了能够传递下去，采用数组方式
	 * @return
	 * 本位点的信息，用于下一次判断是否是同一位点
	 */
	protected int[] addLoc(AlignRecord alignRecord, int[] tmpOld, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		Boolean cis5to3This = alignRecord.isCis5to3();
		if (cis5to3This == null) {
			cis5to3This = true;
		}
		
		if ((mapReads.FilteredStrand != null && alignRecord.isCis5to3() != mapReads.FilteredStrand)
				|| (mapReads.isUniqueMapping() && !alignRecord.isUniqueMapping())
				) {
			return tmpOld;
		}
		
		int[] tmpStartEnd = new int[2];
		tmpStartEnd[0] = alignRecord.getStartAbs();
		tmpStartEnd[1] = alignRecord.getEndAbs();

		//如果本reads和上一个reads相同，则认为是线性扩增，跳过
		if (mapReads.uniqReads && tmpStartEnd[0] == tmpOld[0] && tmpStartEnd[1] == tmpOld[1] ) {
			return tmpOld;
		}
		
		ArrayList<? extends Alignment> lsadd = null;
		//如果没有可变剪接
		lsadd = alignRecord.getAlignmentBlocks();
		lsadd = setStartCod(lsadd, mapReads.startCod, cis5to3This);
		int addNum = (int) ((double)1*fold / alignRecord.getMappedReadsWeight());
		addChrLoc(chrBpReads, lsadd, addNum);
		chrMapReadsInfo.readsAllNum = chrMapReadsInfo.readsAllNum + 1;
		return tmpStartEnd;
	}
	/**
	 * 根据正反向截取相应的区域，最后返回需要累加的ArrayList<int[]>
	 * 譬如韩燕的项目，只需要reads开头的前3个bp，那么就截取前三个就好
	 * @param lsStartEnd
	 * @param StartCodLen 譬如韩燕的项目，只需要reads开头的前3个bp，那么就设定为3
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
				if (StartCodLen - lsStartEnd.get(i).getLength() > 0) {
					if (i == lsStartEnd.size() - 1) {
						Align lastAlign = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getStartAbs() + StartCodLen - 1);
						lsResult.add(lastAlign);
					} else {
						Align align = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
						align.setCis5to3(alignment.isCis5to3());
						lsResult.add(align);
						StartCodLen = StartCodLen - alignment.getLength();
					}
				}
				else {
					Align lastAlign = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getStartAbs() + StartCodLen - 1);
					lsResult.add(lastAlign);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - alignment.getLength() > 0) {
					if (i == 0) {
						Align align = new Align(alignment.getRefID(), alignment.getEndAbs() - StartCodLen + 1, alignment.getEndAbs());
						align.setCis5to3(alignment.isCis5to3());
					} else {						
						Align align = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
						align.setCis5to3(alignment.isCis5to3());
						lsResult.add(0,align);
						StartCodLen = StartCodLen - alignment.getLength();
					}
				}
				else {
					Align align = new Align(alignment.getRefID(), alignment.getEndAbs() - StartCodLen + 1, alignment.getEndAbs());
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
	private void addChrLoc(int[] chrLoc, ArrayList<? extends Alignment> lsAddLoc, int addNum) {
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
				chrLoc[i] = chrLoc[i] + addNum;
			}
		}
	}
	
	public void clear() {
		chrBpReads = null;
		chrMapReadsInfo.clear();
		chrMapReadsInfo = null;
		tmpOld = null;
	}
}


/**
 * 单条染色体信息
 * @author zong0jie
 */
class ChrMapReadsInfo {
	String chrID;
	int invNum = 10;
	int type;
	long chrLength;
	
	/** 直接从0开始记录，1代表第二个invNum,也和实际相同 */
	int[] SumChrBpReads;
	/** 本条染色体上的reads数量 */
	double readsAllNum;
	/** 本条染色体上的reads的堆叠数之和 */
	double readsAllPipNum;
	/** 用于校正数据 */
	Equations FormulatToCorrectReads; 
	
	/**
	 * @param chrID
	 * @param mapReadsAbs
	 */
	public ChrMapReadsInfo(String chrID, MapReads mapReads) {
		this.chrID = chrID;
		this.chrLength = mapReads.getChrLen(chrID);
		this.invNum = mapReads.invNum;
		this.type = mapReads.summeryType;
		this.FormulatToCorrectReads = mapReads.FormulatToCorrectReads;
	}
	
	public String getChrID() {
		return chrID;
	}
	public long getReadsChrNum() {
		return (long)readsAllNum;
	}
	/** 设定总reads的数量 */
	public void addReadsAllNum(double readsAllNum) {
		this.readsAllNum = this.readsAllNum + readsAllNum;
	}
	public long getReadsPipNum() {
		return (long) readsAllPipNum;
	}
	/**
	 * 直接从0开始记录，1代表第二个invNum,也和实际相同
	 * @return
	 */
	public int[] getSumChrBpReads() {
		return SumChrBpReads;
	}
	
	/**
	 * 所谓结算就是说每隔invNum的bp就把这invNumbp内每个bp的Reads叠加数取平均或中位数，保存进chrBpReads中
	 * 给定chrBpReads，将chrBpReads里面的值按照invNum区间放到SumChrBpReads里面
	 * 因为是引用传递，里面修改了SumChrBpReads后，外面会变掉
	 * @param chrBpReads 每个碱基的reads累计值
	 * @param fold 增加倍数，因为是integer无法取小数，所以搞个fold，扩大1000倍表示小数点三位有效数字
	 */
	protected void sumChrBp(int[] chrBpReads, int fold) {
		// //////////SumChrBpReads设定//////////////////////////////////
		// 这个不是很精确，最后一位可能不准，但是实际应用中无所谓了,为方便，0位记录总长度。这样实际bp就是实际长度
		int SumLength = chrBpReads.length / invNum + 1;// 保证不会溢出，这里是要让SumChrBpReads长一点
		if (SumChrBpReads == null) {
			SumChrBpReads = new int[SumLength];// 直接从0开始记录，1代表第二个invNum,也和实际相同
		}
		
		if (invNum == 1) {
			for (int i = 0; i < SumLength - 2; i++) {
				SumChrBpReads[i] += chrBpReads[i+1];
				readsAllPipNum = readsAllPipNum + (double)chrBpReads[i+1]/fold;
			}
			return;
		 }
		 for (int i = 0; i < SumLength - 2; i++) {
			 int[] tmpSumReads = new int[invNum];//将总的chrBpReads里的每一段提取出来
			 int sumStart = i*invNum + 1; int k=0;//k是里面tmpSumReads的下标，实际下标就行，不用-1
			 for (int j = sumStart; j < sumStart + invNum; j++) {
				 int thisNum = chrBpReads[j];
				 tmpSumReads[k] = thisNum;
				 readsAllPipNum = readsAllPipNum + (double)thisNum/fold;
				 k++;
			 }
			 samplingSite(i, tmpSumReads);
		 }
	}
	
	private void samplingSite(int siteNum, int[] tmpSumReads) {
		 if (type == MapReads.SUM_TYPE_MEDIAN) { //每隔一段区域取样，建议每隔10bp取样，取中位数
			 SumChrBpReads[siteNum] += (int) MathComput.median(tmpSumReads);
		 } else if (type == MapReads.SUM_TYPE_MEAN) { 
			 SumChrBpReads[siteNum] += (int) MathComput.mean(tmpSumReads);
		 } else if (type == MapReads.SUM_TYPE_SUM) {
			SumChrBpReads[siteNum] += MathComput.sum(tmpSumReads);
		}else {//默认取中位数 
			 SumChrBpReads[siteNum] += (int) MathComput.median(tmpSumReads);
		 }
	}
	
	public void clear() {
		FormulatToCorrectReads = null;
		SumChrBpReads = null;
	}
}
