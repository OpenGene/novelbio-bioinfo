package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.base.RunProcess;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;

/**
 * 不考虑内存限制的编
 * T: 本次running打算输出的中间信息用MapReadsProcessInfo来保存
 * 进度条多线程，需要以下操作 <br>
 * 1. 在循环中添加 suspendCheck()  来挂起线程<br>
 * 2. 在循环中检查 flagRun 来终止循环<br>
 * 3: 在循环中添加 setRunInfo() 方法来获取运行时出现的信息
 * @author zong0jie
 *
 * @author zong0jie
 * 
 */
public abstract class MapReadsAbs extends RunProcess<MapReadsAbs.MapReadsProcessInfo> {
	private static Logger logger = Logger.getLogger(MapReadsAbs.class);
	/**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的中位数 */
	public static final int SUM_TYPE_MEDIAN = 2;
	/**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的平均数 */
	public static final int SUM_TYPE_MEAN = 3;
	
	 HashMap<String, ChrMapReadsInfo> mapChrID2ReadsInfo = new HashMap<String, ChrMapReadsInfo>();
 
	 /**每隔多少位计数，如果设定为1，则算法会变化，然后会很精确*/
	 int invNum = 10;
	 
	 int tagLength = 300;//由ReadMapFile方法赋值
	 /** 序列信息,名字都为小写 */
	 HashMap<String, Long> mapChrID2Len = new HashMap<String, Long>();
	 
	 Equations FormulatToCorrectReads;
	 
	 BedSeq bedSeq;
	 
	 int summeryType = SUM_TYPE_MEAN;
	 
	 /**
	  * @param invNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	  * @param mapFile mapping的结果文件，一般为bed格式
	  */
	 public MapReadsAbs() {}
	 public void setInvNum(int invNum) {
		this.invNum = invNum;
	}
	 public void setBedSeq(String bedSeqFile) {
		 bedSeq = new BedSeq(bedSeqFile);
	}
	 /**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的中位数或平均数<br>
	  * SUM_TYPE_MEDIAN，SUM_TYPE_MEAN
	  *  
	  */
	 public void setSummeryType(int summeryType) {
		this.summeryType = summeryType;
	}
	 /** 用species里面来设定
	  * key务必小写
	  *  */
	 public void setMapChrID2Len(HashMap<String, Long> mapChrID2Len) {
		 this.mapChrID2Len = mapChrID2Len;
	 }
	 /**
	  * 返回所有chrID的list
	  * @return
	  */
	 public ArrayList<String> getChrIDLs() {
		 return ArrayOperate.getArrayListKey(mapChrID2Len);
	 }
	 /**
	  * 用于校正reads数的方程，默认设定基因组上reads的最小值为0，凡是校正小于0的都改为0
	  * @param FormulatToCorrectReads
	  */
	 public void setFormulatToCorrectReads(Equations FormulatToCorrectReads) {
		 this.FormulatToCorrectReads = FormulatToCorrectReads;
		 //默认设定基因组上reads的最小值为0，凡是校正小于0的都改为0
		 FormulatToCorrectReads.setMin(0);
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
	 * 设定双端readsTag拼起来后长度的估算值，目前solexa双端送样长度大概是300bp，不用太精确
	 * 默认300
	 * 这个是方法：getReadsDensity来算reads密度的东西
	 * @param readsTagLength
	 */
	public  void setTagLength(int thisTagLength) {
		tagLength=thisTagLength;
	}
	/**
	 * 每隔多少位取样,如果设定为1，则算法会变化，然后会很精确
	 * @return
	 */
	public int getBinNum() {
		return invNum;
	}
	public void running() {
		try {
			ReadMapFileExp();
		} catch (Exception e) {
			e.printStackTrace();
 		}
 	}
	/**
	 * 当输入为macs的bed文件时，看需要然后<b>跳过chrm项目</b><br>
	 * 所有chr项目都小写
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。注意，mapping文件中的chrID和chrLengthFile中的chrID要一致，否则会出错
	 * @return 返回所有mapping的reads数量
	 * @throws Exception
	 */
	protected abstract void ReadMapFileExp() throws Exception;
	
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
	public double regionMean(String chrID, int startLoc, int endLoc) {
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
	public ArrayList<int[]> region0Info(String chrID, int startLocT, int endLocT) {
		int startLoc = Math.min(startLocT, endLocT);
		int endLoc = Math.max(startLocT, endLocT);
		startLoc--; endLoc--;
		int[] invNumReads = mapChrID2ReadsInfo.get(chrID.toLowerCase()).getSumChrBpReads();
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
	public double regionMean(String chrID, List<int[]> lsLoc) {
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
	public double[] getRengeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type) {
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
			int[] invNumReads = mapChrID2ReadsInfo.get(chrID.toLowerCase()).getSumChrBpReads();
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
			normDouble(result);
			if (FormulatToCorrectReads != null) {
				result = FormulatToCorrectReads.getYinfo(result);
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
	public  double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type) {
		if (startNum <=0 && endNum <=0) {
			startNum = 1; endNum = (int)getChrLen(chrID);
		}
		if (startNum > endNum) {
			logger.error("起点不能比终点大: "+chrID+" "+startNum+" "+endNum);
		}
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		if (chrMapReadsInfo == null) {
			logger.error("没有该染色体：" + chrID);
			return null;
		}
		int[] invNumReads = chrMapReadsInfo.getSumChrBpReads();
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
		normDouble(tmpRegReads);
		double[] tmp = null;
		try {
			tmp =  MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
		} catch (Exception e) {
			return null;
		}
		if (FormulatToCorrectReads != null) {
			tmp = FormulatToCorrectReads.getYinfo(tmp);
		}
		return tmp;
	}
	 /**
	  * 从这里得到的实际某条染色体的长度
	  */
	 protected long getChrLen(String chrID) {
		 return mapChrID2Len.get(chrID.toLowerCase());
	 }
	/**
	 * 提取的原始数据需要经过标准化再输出。
	 * 本方法进行标准化
	 * 输入的double直接修改，不返回。<br>
	 * @param doubleInfo 提取得到的原始value
	 * @return 
	 */
	protected abstract void normDouble(double[] readsInfo);
	/**
	 * 给定坐标信息，将比较的比值，也就是均值相除，放入mapInfo的weight内
	 * 内部标准化
	 * @param mapReads 第一个mapReads信息
	 * @param mapReads2 第二个mapReads信息
	 * @param mapInfo
	 */
	public static void CmpMapReg(MapReads mapReads, MapReads mapReads2, MapInfo mapInfo) {
		double value1 = mapReads.regionMean(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd());
		double value2 = mapReads2.regionMean(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd());
		mapInfo.setScore(value1/value2);
	}
	public static class MapReadsProcessInfo {
		long readsize;
		public MapReadsProcessInfo(long readsize) {
			this.readsize = readsize;
		}
		public long getReadsize() {
			return readsize;
		}
	}
}

class ChrMapReadsInfo {	
	String chrID;
	int invNum = 10;
	int type;
	long chrLength;
	
	/** 最后总结的信息 */
	int[] SumChrBpReads;
	/** 本条染色体上的reads数量 */
	long readsAllNum;
	/** 本条染色体上的reads的堆叠数之和 */
	long readsAllPipNum;
	Equations FormulatToCorrectReads; 
	public ChrMapReadsInfo(String chrID, long chrLen, int invNumm, int sumType, Equations FormulatToCorrectReads) {
		this.chrID = chrID;
		this.chrLength = chrLen;
		this.invNum = invNumm;
		this.type = sumType;
		this.FormulatToCorrectReads = FormulatToCorrectReads;
	}
	
	public String getChrID() {
		return chrID;
	}
	public long getReadsChrNum() {
		return readsAllNum;
	}
	public long getReadsPipNum() {
		return readsAllPipNum;
	}
	public int[] getSumChrBpReads() {
		return SumChrBpReads;
	}
	
	/**
	 * 所谓结算就是说每隔invNum的bp就把这invNumbp内每个bp的Reads叠加数取平均或中位数，保存进chrBpReads中
	 * 给定chrBpReads，将chrBpReads里面的值按照invNum区间放到SumChrBpReads里面
	 * 因为是引用传递，里面修改了SumChrBpReads后，外面会变掉
	 * @param chrBpReads 每个碱基的reads累计值
	 * @param invNum 区间
	 * @param type 取值类型，中位数或平均值，0中位数，1均值 其他的默认中位数
	 * @param SumChrBpReads 将每个区间内的
	 */
	protected void sumChrBp(int[] chrBpReads) {
		// //////////SumChrBpReads设定//////////////////////////////////
		// 这个不是很精确，最后一位可能不准，但是实际应用中无所谓了,为方便，0位记录总长度。这样实际bp就是实际长度
		int SumLength = chrBpReads.length / invNum + 1;// 保证不会溢出，这里是要让SumChrBpReads长一点
		SumChrBpReads = new int[SumLength];// 直接从0开始记录，1代表第二个invNum,也和实际相同
		
		if (invNum == 1) {
			for (int i = 0; i < SumLength - 2; i++) {
				SumChrBpReads[i] = chrBpReads[i+1];
				readsAllPipNum = readsAllPipNum + chrBpReads[i+1];
			}
			return;
		 }
		 for (int i = 0; i < SumLength - 2; i++) {
			 int[] tmpSumReads=new int[invNum];//将总的chrBpReads里的每一段提取出来
			 int sumStart=i*invNum + 1; int k=0;//k是里面tmpSumReads的下标，实际下标就行，不用-1
			 for (int j = sumStart; j < sumStart + invNum; j++) {
				 tmpSumReads[k] = chrBpReads[j];
				 readsAllPipNum = readsAllPipNum + chrBpReads[j];
				 k++;
			 }
			 samplingSite(i, tmpSumReads);
		 }
	}
	/**
	 * 
	 * 总结double类型的chrBpReads，double的可以考虑将非unique mapping的reads进行减分处理
	 * 给定chrBpReads，将chrBpReads里面的值按照invNum区间放到SumChrBpReads里面
	 * 因为是引用传递，里面修改了SumChrBpReads后，外面会变掉
	 * @param chrBpReads 每个碱基的reads累计值
	 * @param invNum 区间
	 * @param type 取值类型，中位数或平均值，0中位数，1均值 其他的默认中位数
	 * @param SumChrBpReads 将每个区间内的
	 */
	protected void sumChrBp(double[] chrBpReads, long[] chrReadsPipNum) {
		int SumLength = chrBpReads.length / invNum + 1;// 保证不会溢出，这里是要让SumChrBpReads长一点
		SumChrBpReads = new int[SumLength];// 直接从0开始记录，1代表第二个invNum,也和实际相同
		
		if (invNum == 1) {
			for (int i = 0; i < SumLength - 2; i++) {
				SumChrBpReads[i] = (int) Math.round(chrBpReads[i+1]);
				chrReadsPipNum[0] = chrReadsPipNum[0] + (int) Math.round(chrBpReads[i+1]);
			}
			return;
		}
		 for (int i = 0; i < SumLength - 2; i++) {
			 int[] tmpSumReads=new int[invNum];//将总的chrBpReads里的每一段提取出来
			 int sumStart=i*invNum + 1; int k=0;//k是里面tmpSumReads的下标，实际下标就行，不用-1
			 for (int j = sumStart; j < sumStart + invNum; j++)  {
				 tmpSumReads[k] = (int) Math.round(chrBpReads[j]);
				 chrReadsPipNum[0] = chrReadsPipNum[0] + (int) Math.round(chrBpReads[j]);
				 k++;
			 }
			 samplingSite(i, tmpSumReads);
		 }
	}
	
	private void samplingSite(int siteNum, int[] tmpSumReads) {
		 if (type == MapReadsAbs.SUM_TYPE_MEDIAN) //每隔一段区域取样，建议每隔10bp取样，取中位数
			 SumChrBpReads[siteNum] = (int) MathComput.median(tmpSumReads);
		 else if (type == MapReadsAbs.SUM_TYPE_MEAN) 
			 SumChrBpReads[siteNum] = (int) MathComput.mean(tmpSumReads);
		 else //默认取中位数
			 SumChrBpReads[siteNum] = (int) MathComput.median(tmpSumReads);
	}

}
