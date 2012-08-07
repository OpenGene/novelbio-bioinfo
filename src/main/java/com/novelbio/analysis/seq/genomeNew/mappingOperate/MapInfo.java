package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.plot.java.HeatChartDataInt;
/**
 * 内建根据weight排序的方法
 * @author zong0jie
 *
 */
public class MapInfo extends SiteInfo implements HeatChartDataInt, Cloneable{
	Logger logger = Logger.getLogger(MapInfo.class);
	/** 比较mapinfo的起点终点 */
	public static final int COMPARE_LOCSITE = 100;
	/** 比较mapinfo的flag site */
	public static final int COMPARE_LOCFLAG = 200;
	/** 比较mapinfo的score */
	public static final int COMPARE_SCORE = 300;
	
	static int compareInfo = COMPARE_SCORE;
	
	private double[] value = null;
	/**
	 * 是否用cis5to3的信息来翻转Value的double[]
	 * 默认翻转
	 */
	boolean correctUseCis5to3ToConvertValue = true;
	
	public MapInfo() { }
	/**
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flagLoc 特定的一个位点坐标，譬如ATGsite，summitSite等
	 * @param weight
	 * @param title 本条目的名字，譬如基因名等
	 */
	public MapInfo(String chrID, int startLoc, int endLoc, int flagLoc ,double weight, String title) {
		super(chrID, startLoc, endLoc, flagLoc, weight, title);
	}
	
	/**
	 * 是否用cis5to3的信息来翻转Value的double[]
	 * 默认翻转
	 * @param correct
	 */
	public void setCorrectUseCis5to3(boolean correctUseCis5to3ToConvertValue) {
		this.correctUseCis5to3ToConvertValue = correctUseCis5to3ToConvertValue;
	}
	/**
	 * 如果startLoc < endLoc,则cis5to3设定为反向
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 */
	public MapInfo(String chrID, int startLoc, int endLoc) {
		super(chrID, startLoc, endLoc);
	}
	/**
	 * 是否从小到大排序
	 * @return
	 */
	public static boolean isMin2max() {
		return min2max;
	}
	/**
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flag 比较的标签，可以是表达值等
	 * @param name 本条目的名字，譬如基因名等
	 */
	public MapInfo(String chrID) {
		this.refID = chrID;
	}
	/**
	 * 选择COMPARE_LOCSITE等
	 * 默认COMPARE_WEIGHT
	 * @param COMPARE_TYPE
	 */
	public static void setCompareType(int COMPARE_TYPE) {
		compareInfo = COMPARE_TYPE;
	}
	/**
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flag 比较的标签，可以是表达值等
	 * @param title 本条目的名字，譬如基因名等
	 */
	public MapInfo(String chrID,double weight, String title) {
		this.refID = chrID;
		this.score = weight;
		this.name = title;
	}
	/** 获得value的均值。如果不存在，则返回null */
	public Double getMean() {
		if (value == null) {
			return null;
		}
		return MathComput.mean(value);
	}
	/** 获得value的中位数。如果不存在，则返回null */
	public Double getMedian() {
		if (value == null) {
			return null;
		}
		return MathComput.median(value);
	}
	/**
	 * 这个在设定的时候，会根据mapinfo的方向进行，也就是说如果该mapInfo为正向，则直接赋值
	 * 反向的话就颠倒一下然后再赋值
	 * @param value
	 */
	public void setDouble(double[] value) {
		this.value = value;
	}
	
	/**
	 * 会根据cis5to3以及correct的信息来返回value的值
	 * 如果既反向，又标记了修正，则将value颠倒
	 */
	@Override
	public double[] getDouble() {
		if (value == null) {
			return null;
		}
		double[] valueTmp = new double[value.length];
		for (int i = 0; i < valueTmp.length; i++) {
			valueTmp[i] = value[i];
		}
		if (cis5to3 != null && !cis5to3 && correctUseCis5to3ToConvertValue) {
			ArrayOperate.convertArray(valueTmp);
		}
		return valueTmp;
	}

	public MapInfo clone() {
		MapInfo mapInfo;
		mapInfo = (MapInfo) super.clone();
		mapInfo.correctUseCis5to3ToConvertValue = correctUseCis5to3ToConvertValue;
		mapInfo.seqFasta = seqFasta.clone();				
		double[] value2 = null;
		if (value != null) {
			value2 = new double[value.length];
			for (int i = 0; i < value2.length; i++) {
				value2[i] = value[i];
			}
		}
		mapInfo.value = value2;
		return mapInfo;
	}
	/**
	 * 仅比较refID，startLoc,endLoc,score.flagLoc
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		MapInfo otherObj = (MapInfo)obj;
		if (
				cis5to3 == otherObj.cis5to3
				&& refID.equals(otherObj.refID)
				&& startLoc == otherObj.startLoc
				&& endLoc == otherObj.endLoc
				&& score == otherObj.score
				&& flagLoc == otherObj.flagLoc
			)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 给定mapInfo的list
	 * 将里面的double[]合并成一个，假定mapInfo中的value不等长
	 * 按照summit位点对齐，假定summit点为0点
	 * @param lsmapinfo mapInfo的信息
	 * @param upNum 上游个数，<=0表示最长上游
	 * @param downNum 下游个数, <=0表示最长下游
	 * @return
	 * 得到的结果会除以lsmapinfo的长度作为标准化
	 */
	public static double[] getCutLsMapInfoComb(List<MapInfo> lsmapinfo, int upNum, int downNum) {
		if (upNum <= 0 || downNum <= 0) {
			int[] max = getLsMapInfoUpDown(lsmapinfo);
			if (upNum <= 0)
				upNum = max[0];
			if (downNum <= 0)
				downNum = max[1];
		}
		double[] result = new double[upNum+downNum+1];
		for (MapInfo mapInfo : lsmapinfo) {
			double[] tmp = ArrayOperate.cuttArray(mapInfo.getDouble(), mapInfo.getFlagSite(), upNum, downNum, -1);
			for (int i = 0; i < result.length; i++) {
				result[i] = result[i] + tmp[i];
			}
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i]/lsmapinfo.size();
		}
		return result;
	}
	/**
	 * 用lsmapinfo1 减去 lsmapinfo2的信息
	 * @param lsmapinfo1
	 * @param lsmapinfo2
	 * @param upNum
	 * @param downNum
	 * @return
	 */
	public static ArrayList<MapInfo> minusListMapInfo(List<MapInfo> lsmapinfo1, List<MapInfo> lsmapinfo2) {
		ArrayList<MapInfo> lsResult = new ArrayList<MapInfo>();
		for (int i = 0; i < lsmapinfo1.size(); i++) {
			MapInfo mapInfoTmp = lsmapinfo1.get(i).clone();
			MapInfo mapInfoTmp2 = lsmapinfo2.get(i);
			for (int j = 0; j < mapInfoTmp.value.length; j++) {
				mapInfoTmp.value[j] = mapInfoTmp.value[j] - mapInfoTmp2.value[j];
			}
			lsResult.add(mapInfoTmp);
		}
		return lsResult;
	}
	
	/**
	 * 不排序
	 * 给定mapInfo的list
	 * 将里面的double[]合并成一个，假定mapInfo中的value不等长
	 * 按照summit位点对齐，假定summit点为0点
	 * @param lsmapinfo mapInfo的信息
	 * @param upNum 上游个数，<=0表示最长上游
	 * @param downNum 下游个数, <=0表示最长下游
	 * @return
	 */
	public static ArrayList<MapInfo> getCutLsMapInfo(List<MapInfo> lsmapinfo, int upNum, int downNum) {
		if (upNum <= 0 || downNum <= 0) {
			int[] max = getLsMapInfoUpDown(lsmapinfo);
			if (upNum <= 0)
				upNum = max[0];
			if (downNum <= 0)
				downNum = max[1];
		}
		ArrayList<MapInfo> lsResult = new ArrayList<MapInfo>();
		for (MapInfo mapInfo : lsmapinfo) {
			double[] tmp = ArrayOperate.cuttArray(mapInfo.getDouble(), mapInfo.getFlagSite(), upNum, downNum, -1);
			MapInfo mapInfo2 = mapInfo.clone();
			mapInfo2.setDouble(tmp);
			lsResult.add(mapInfo2);
		}
		return lsResult;
	}
	
	/**
	 * 给定mapInfo的list
	 * 将里面的double[]合并成一个，假定mapInfo中的value等长
	 */
	public static double[] getCombLsMapInfo(List<MapInfo> lsmapinfo) {
		double[] result = new double[lsmapinfo.get(0).getDouble().length];
		for (MapInfo mapInfo : lsmapinfo) {
			double[] tmp = mapInfo.getDouble();
			for (int i = 0; i < result.length; i++) {
				result[i] = result[i] + tmp[i];
			}
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i]/lsmapinfo.size();
		}
		return result;
	}

	
}
