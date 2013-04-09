package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.list.SetUniqueList;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;

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
	/** 标准化数字会很小很小，乘以一个很大的数防止溢出 */
	private static final int mulNum = 10000000;
	private static Logger logger = Logger.getLogger(MapReadsAbs.class);
	/**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的中位数 */
	public static final int SUM_TYPE_MEDIAN = 2;
	/**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的平均数 */
	public static final int SUM_TYPE_MEAN = 3;
	/**将长的单碱基精度的一条染色体压缩为短的每个inv大约10-20bp的序列，那么压缩方法选择为20bp中的数值的总和 */
	public static final int SUM_TYPE_SUM = 4;
	
	/** 将每个double[]求和/double.length 也就是将每个点除以该gene的平均测序深度 */
	public static final int NORMALIZATION_PER_GENE = 128;
	/** 将每个double[]*1million/AllReadsNum 也就是将每个点除以测序深度 */
	public static final int NORMALIZATION_ALL_READS = 256;
	/** 不标准化 */
	public static final int NORMALIZATION_NO = 64;

	/** 对于结果的标准化方法 */
	protected int NormalType = NORMALIZATION_ALL_READS;

	 /** 序列信息,名字都为小写 */
	 Map<String, Long> mapChrID2Len = new HashMap<String, Long>();
	 
	 Equations FormulatToCorrectReads;
	 protected boolean booUniqueMapping = true;
	 /**
	  * key：chrID必须小写
	  * value： 染色体过滤信息，马红想要只看tss，只看exon等表达
	  */
	 Map<String, List<? extends Alignment>> mapChrID2LsAlignmentFilter;
	 protected long allReadsNum = 0;
	 /**
	  * @param invNum 每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	  * @param mapFile mapping的结果文件，一般为bed格式
	  */
	 public MapReadsAbs() {}

	 public void setSpecies(Species species) {
		 mapChrID2Len = species.getMapChromInfo();
	 }
	 public void setisUniqueMapping(boolean booUniqueMapping) {
		this.booUniqueMapping = booUniqueMapping;
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
	  * 设定保留的区域，譬如马红想看全基因组上tss的分布，那么就将tss的区域装到该ls中间
	  * @param lsAlignments
	  */
	 public void setMapChrID2LsAlignments(Map<String, List<? extends Alignment>> mapChrID2LsAlignmentFilter) {
		 this.mapChrID2LsAlignmentFilter = mapChrID2LsAlignmentFilter;
	 }

	 /** 用species里面来设定
	  * key务必小写
	  *  */
	 public void setMapChrID2Len(Map<String, Long> mapChrID2Len) {
		 this.mapChrID2Len = mapChrID2Len;
	 }
	 public Map<String, Long> getMapChrID2Len() {
		return mapChrID2Len;
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
	 * 填充每个MapInfo，如果没有找到该染色体位点，则填充null
     * 不考虑mapInfo的方向
	 * 经过标准化，和equations修正
	 * @param lsmapInfo
	 * @param thisInvNum  每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRange(MapInfo mapInfo, int thisInvNum, int type) {
		double[] Info = getRangeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), type);
		mapInfo.setDouble(Info);
	}
	/**
	 * 经过标准化
     * 不考虑mapInfo的方向
	 * 将MapInfo中的double填充上相应的reads信息
	 * @param binNum 待分割的区域数目
	 * @param lsmapInfo
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRange(int binNum, MapInfo mapInfo, int type) {
		double[] Info = getRangeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), binNum, type);
		if (Info == null) {
			logger.error("出现未知ID："+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStartAbs() + " "+ mapInfo.getEndAbs());
		}
		mapInfo.setDouble(Info);
	}
	/**
	 * 填充每个MapInfo，直接设定，不考虑方向
	 * 经过标准化，和equations修正
	 * @param lsmapInfo
	 * @param thisInvNum  每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRangeLs(List<MapInfo> lsmapInfo, int thisInvNum, int type) {
		for (MapInfo mapInfo : lsmapInfo) {
			double[] Info = getRangeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), type);
			mapInfo.setDouble(Info);
		}
	}
	/**
	 * 经过标准化
	 * 将MapInfo中的double填充上相应的reads信息，直接设定，不考虑方向
	 * @param binNum 待分割的区域数目
	 * @param lsmapInfo
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 */
	public void getRangeLs(int binNum, List<MapInfo> lsmapInfo, int type) {
		for (int i = 0; i < lsmapInfo.size(); i++) {
			MapInfo mapInfo = lsmapInfo.get(i);
			double[] Info = getRangeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), binNum, type);
			if (Info == null) {
				lsmapInfo.remove(i); i--;
				logger.error("出现未知ID："+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStartAbs() + " "+ mapInfo.getEndAbs());
				continue;
			}
			mapInfo.setDouble(Info);
		}
	}
	/**
	 * 经过标准化，和equations修正，<b>注意返回的值一直都是按照坐标从小到大，不会根据方向而改变方向</b>
	 * 给定坐标范围，返回该区间内的信息，取点为加权平均
	 * @param chrID
	 * @param lsLoc 一个转录本的exon list
	 * @return null表示出错
	 */
	public double[] getRangeInfo(String chrID, List<? extends Alignment> lsLoc) {
		return getRangeInfo(chrID, lsLoc, -1 , 0);
	}
	/**
	 * 经过标准化，和equations修正，<b>注意返回的值一直都是按照坐标从小到大，不会根据方向而改变方向</b>
	 * 给定坐标范围，返回该区间内的信息，取点为加权平均
	 * @param chrID
	 * @param lsLoc 一个转录本的exon list
	 * @return null表示出错
	 */
	public List<double[]> getRangeInfoLs(String chrID, List<? extends Alignment> lsLoc) {
		return getRangeInfoLs(chrID, lsLoc, 0);
	}

	/**
	 *  用于mRNA的计算，经过标准化，和equations修正
	 * 输入坐标区间，需要划分的块数，返回该段区域内reads的数组。如果该染色体在mapping时候不存在，则返回null
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间
	 * @param chrID
	 * @param lsLoc 直接输入gffIso即可，<b>输入的Alignment不考虑方向</b>
	 * @param binNum 分成几份，如果小于0，则不进行合并，直接返回自己的份数
	 * @param type  0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	protected double[] getRangeInfo(String chrID, List<? extends Alignment> lsLoc, int binNum, int type) {
		List<double[]> lstmp = getRangeInfoLs(chrID, lsLoc, type);
				
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
		if (binNum > 0) {
			finalReads =MathComput.mySpline(finalReads, binNum, 0, 0, 0);
		}
		return finalReads;
	}
	
	/**
	 *  用于mRNA的计算，经过标准化，和equations修正
	 * 输入坐标区间，返回该段区域内reads的数组。如果该染色体在mapping时候不存在，则返回null
	 * @param chrID
	 * @param lsLoc 直接输入gffIso即可，<b>输入的Alignment不考虑方向</b>
	 * @param type  0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	protected List<double[]> getRangeInfoLs(String chrID, List<? extends Alignment> lsLoc, int type) {
		ArrayList<double[]> lstmp = new ArrayList<double[]>();
		if (lsLoc.size() > 1 && !lsLoc.get(0).isCis5to3()) {
			lsLoc = sortLsLoc(lsLoc);
		}
	
		for (Alignment is : lsLoc) {
			double[] info = getRangeInfo(0, chrID, is.getStartAbs(), is.getEndAbs(), type);
			if (info == null) {
				return null;
			}
			lstmp.add(info);
		}
		return lstmp;
	}
	
	/** 将输入的loc按照StartAbs从小到大排序，<br>
	 * 并返回排序后的全新的List<br>
	 * 输入的list其自身并不排序
	 */
	protected ArrayList<Alignment> sortLsLoc(List<? extends Alignment> lsLoc) {
		ArrayList<Alignment> lsLocNew = new ArrayList<Alignment>();
		for (Alignment alignment : lsLoc) {
			lsLocNew.add(alignment);
		}
		Collections.sort(lsLocNew, new Comparator<Alignment>() {
			public int compare(Alignment o1, Alignment o2) {
				Integer o1Int = o1.getStartAbs();
				Integer o2Int = o2.getStartAbs();
				return o1Int.compareTo(o2Int);
			}
		});
		return lsLocNew;
	}
	/**
	 * 经过标准化，和equations修正
	 * 输入坐标区间，默认每个区间的bp数为invNum，返回该段区域内reads的数组
	 * 如果该染色体在mapping时候不存在，则返回null
	 * 如果invNum ==1 && thisInvNum == 1，结果会很精确
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点，如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endNum 终点坐标，为实际终点
	 * 如果(endNum - startNum + 1) / thisInvNum >0.7，则将binNum设置为1
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 如果没有找到该染色体位点，则返回null
	 */
	public double[] getRangeInfo(String chrID,int startNum,int endNum,int type) {
		return getRangeInfo(0, chrID, startNum, endNum, type);
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
	public abstract double[] getRangeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type);
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
	protected abstract double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type);
	
	/**
	 * 用输入的公式进行修正
	 * @param input
	 * @return
	 */
	protected double[] equationsCorrect(double[] input) {
		double[] result = null;
		if (FormulatToCorrectReads != null) {
			result = FormulatToCorrectReads.getYinfo(input);
		} else {
			result = input;
		}
		return result;
	}
	 /**
	  * 从这里得到的实际某条染色体的长度
	  */
	 protected long getChrLen(String chrID) {
		 return mapChrID2Len.get(chrID.toLowerCase());
	 }
	 /** 有时候需要用测序量最大的一个样本的reads数来做标准化
	  * <b>在读取结束后设定</b>
	  * @param allReadsNum
	  */
	 public void setAllReadsNum(long allReadsNum) {
		this.allReadsNum = allReadsNum;
	}
	 
	 /** 总共有多少reads参与了mapping，这个从ReadMapFile才能得到。 */
	protected abstract long getAllReadsNum();
	
	/**
	 * 检查输入的start 和 end是否在指定区间范围内，
	 * @param mapChrID2Length key为小写
	 * @param chrID 输入的大小写无所谓
	 * @param startNum 小于0则设置为0
	 * @param endNum 小于0则设置为最长范围
	 * @return
	 */
	public static int[] correctStartEnd(Map<String, ? extends Number> mapChrID2Length, String chrID, int startNum, int endNum) {
		if (startNum <=0) {
			startNum = 1; 
		}
		
		if (!mapChrID2Length.containsKey(chrID.toLowerCase())) {
			logger.error("不存在该染色体：" + chrID);
			return null;
		}
		if (endNum <= 0 || endNum > mapChrID2Length.get(chrID.toLowerCase()).intValue() ) {
			endNum = mapChrID2Length.get(chrID.toLowerCase()).intValue();
		}
		if (startNum > endNum) {
			logger.error("起点不能比终点大: "+chrID+" "+startNum+" "+endNum);
			return null;
		}
		return new int[]{startNum, endNum};
	}
	/**
	 * 给定坐标信息，将比较的比值，也就是均值相除，放入mapInfo的weight内
	 * 内部标准化
	 * @param mapReads 第一个mapReads信息
	 * @param mapReads2 第二个mapReads信息
	 * @param mapInfo
	 */
	public static void CmpMapReg(MapReads mapReads, MapReads mapReads2, MapInfo mapInfo) {
		double[] info1 = mapReads.getRangeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), 0);
		double[] info2 = mapReads.getRangeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), 0);
		
		double value1 = getMean(info1);
		double value2 = getMean(info2);
		
		mapInfo.setScore(value1/value2);
	}
	
	private static double getMean(double[] info) {
		if (info == null) {
			return -1;
		}
		return new Mean().evaluate(info);
	}
	
	/**
	 * <b>如果 allReadsNum == 0 && NormalType != NORMALIZATION_ALL_READS，则不进行标准化</b><br>
	 * 提取的原始数据需要经过标准化再输出。
	 * 本方法进行标准化
	 * 输入的double直接修改，不返回。<br>
	 * 最后得到的结果都要求均值
	 * 给定double数组，按照reads总数进行标准化,reads总数由读取的mapping文件自动获得<br>
	 * 最后先乘以1million然后再除以每个double的值<br>
	 * @param doubleInfo 提取得到的原始value
	 * @return 
	 */
	public static void normDouble(int NormalType, double[] doubleInfo, long allReadsNum) {
		if (doubleInfo == null) {
			return;
		}
		if ((allReadsNum == 0 && NormalType != NORMALIZATION_ALL_READS)|| NormalType == NORMALIZATION_NO) {
			return;
		}
		else if (NormalType == NORMALIZATION_ALL_READS) {
			for (int i = 0; i < doubleInfo.length; i++) {
				doubleInfo[i] = doubleInfo[i]*mulNum/allReadsNum;
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

