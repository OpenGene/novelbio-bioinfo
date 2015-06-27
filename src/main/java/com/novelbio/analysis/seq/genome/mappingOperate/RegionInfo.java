package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.java.HeatChartDataInt;
/**
 * 内建根据score排序的方法
 * @author zong0jie
 *
 */
public class RegionInfo extends Align implements HeatChartDataInt, Cloneable,  Alignment {
	private static final Logger logger = Logger.getLogger(RegionInfo.class);

	int summit;
	/** 权重 */
	double score;
	/** 条目名，如基因名等 */
	String name;
	
	private double[] value = null;
	
	/**
	 * 是否用cis5to3的信息来翻转Value的double[]
	 * 默认翻转
	 */
	boolean correctUseCis5to3ToConvertValue = true;
	
	/**
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flagLoc 特定的一个位点坐标，譬如ATGsite，summitSite等
	 * @param score
	 * @param title 本条目的名字，譬如基因名等
	 */
	public RegionInfo(String chrID, int startLoc, int endLoc, double score, String title) {
		super(chrID, startLoc, endLoc);
		this.score = score;
		this.name = title;
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
	public RegionInfo(String chrID, int startLoc, int endLoc) {
		super(chrID, startLoc, endLoc);
	}
	
	/**
	 * 如果startLoc < endLoc,则cis5to3设定为反向
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 */
	public RegionInfo(String chrID, int startLoc, int endLoc, int summit) {
		super(chrID, startLoc, endLoc);
		this.summit = summit;
	}
	
	/**
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flag 比较的标签，可以是表达值等
	 * @param name 本条目的名字，譬如基因名等
	 */
	public RegionInfo(String chrID) {
		setChrID(chrID);
	}

	/**
	 * @param chrID
	 * @param score 权重
	 * @param title 该区间的名字
	 */
	public RegionInfo(String chrID,double score, String title) {
		setChrID(chrID);
		this.score = score;
		this.name = title;
	}
	
	/** 最高点等指向性的位点，必须在start和end之间 */
	public void setSummit(int summit) {
		this.summit = summit;
	}
	/** 最高点等指向性的位点，必须在start和end之间 */
	public int getSummit() {
		return summit;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	public double getScore() {
		return score;
	}
	
	/**
	 * 这个在设定的时候，不会根据mapinfo的方向进行，而是直接赋值
	 * @param value
	 */
	public void setDouble(double[] value) {
		this.value = value;
	}
	/**
	 * 这个在设定的时候，会根据mapinfo的方向进行
	 * @param value
	 */
	public void setDoubleByStrand(double[] value) {
		if (cis5to3 != null && !cis5to3) {
			ArrayOperate.convertArray(value);
		}
		this.value = value;
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
	 * 会根据cis5to3以及correct的信息来返回value的值
	 * 如果标记了修正并且是反向的，则将value颠倒
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
		if (isCis5to3() != null && !isCis5to3() && correctUseCis5to3ToConvertValue) {
			ArrayOperate.convertArray(valueTmp);
		}
		return valueTmp;
	}

	public RegionInfo clone() {
		RegionInfo mapInfo = null;
		try {
			mapInfo = (RegionInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("MapInfo clone error", e);
		}
		mapInfo.correctUseCis5to3ToConvertValue = correctUseCis5to3ToConvertValue;
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
		RegionInfo otherObj = (RegionInfo)obj;
		if (
				cis5to3 == otherObj.cis5to3
				&& getRefID().equals(otherObj.getRefID())
				&& getStartCis() == otherObj.getStartCis()
				&& getEndCis() == otherObj.getEndCis()
				&& score == otherObj.score
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
	 * @param lsPeakInfo peakInfo的信息
	 * @param upNum 上游个数，<=0表示最长上游
	 * @param downNum 下游个数, <=0表示最长下游
	 * @return
	 * 得到的结果会除以lsPeakInfo的长度作为标准化
	 */
	public static double[] mergePeakInfoBySummit(List<RegionInfo> lsPeakInfo, int upNum, int downNum) {
		if (upNum <= 0 || downNum <= 0) {
			int[] max = getLsMapInfoUpDown(lsPeakInfo);
			if (upNum <= 0)
				upNum = max[0];
			if (downNum <= 0)
				downNum = max[1];
		}
		double[] result = new double[upNum+downNum+1];
		for (RegionInfo mapInfo : lsPeakInfo) {
			double[] tmp = ArrayOperate.cuttArray(mapInfo.getDouble(), mapInfo.getSummit(), upNum, downNum, -1);
			for (int i = 0; i < result.length; i++) {
				result[i] = result[i] + tmp[i];
			}
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i]/lsPeakInfo.size();
		}
		return result;
	}
	
	/**
	 * 用lsmapinfo1 减去 lsmapinfo2的信息 lsmapinfo1 和 lsmapinfo2 必须等长，并且每个元素也必须等长
	 * @param lsmapinfo1
	 * @param lsmapinfo2
	 * @param upNum
	 * @param downNum
	 * @return
	 */
	public static ArrayList<RegionInfo> minusListMapInfo(List<RegionInfo> lsmapinfo1, List<RegionInfo> lsmapinfo2) {
		ArrayList<RegionInfo> lsResult = new ArrayList<RegionInfo>();
		for (int i = 0; i < lsmapinfo1.size(); i++) {
			RegionInfo mapInfoTmp = lsmapinfo1.get(i).clone();
			RegionInfo mapInfoTmp2 = lsmapinfo2.get(i);
			for (int j = 0; j < mapInfoTmp.value.length; j++) {
				mapInfoTmp.value[j] = mapInfoTmp.value[j] - mapInfoTmp2.value[j];
			}
			lsResult.add(mapInfoTmp);
		}
		return lsResult;
	}
	
	/**
	 * <b>考虑方向，不排序</b><br>
	 * 给定mapInfo的list
	 * 将里面的double[]合并成一个，假定mapInfo中的value不等长
	 * 按照summit位点对齐，假定summit点为0点
	 * @param lsmapinfo mapInfo的信息
	 * @param upNum 上游个数，<=0表示最长上游
	 * @param downNum 下游个数, <=0表示最长下游
	 * @return
	 */
	public static ArrayList<RegionInfo> getCutLsMapInfo(List<RegionInfo> lsmapinfo, int upNum, int downNum) {
		if (upNum <= 0 || downNum <= 0) {
			int[] max = getLsMapInfoUpDown(lsmapinfo);
			if (upNum <= 0)
				upNum = max[0];
			if (downNum <= 0)
				downNum = max[1];
		}
		ArrayList<RegionInfo> lsResult = new ArrayList<RegionInfo>();
		for (RegionInfo mapInfo : lsmapinfo) {
			double[] tmp = ArrayOperate.cuttArray(mapInfo.getDouble(), mapInfo.getSummit(), upNum, downNum, -1);
			RegionInfo mapInfo2 = mapInfo.clone();
			mapInfo2.setDoubleByStrand(tmp);
			lsResult.add(mapInfo2);
		}
		return lsResult;
	}
	
	/**
	 * 给定mapInfo的list
	 * 将里面的double[]叠加起来，mapInfo中的value可以<b>不等长</b>
	 */
	public static double[] getCombLsMapInfo(List<RegionInfo> lsmapinfo) {
		List<double[]> lsInfo = new ArrayList<double[]>();
		for (RegionInfo mapInfo : lsmapinfo) {
			double[] tmp = mapInfo.getDouble();
			if (tmp == null) {
				continue;
			}
			lsInfo.add(tmp);
		}
		return ArrayOperate.getSumList(lsInfo);
	}
	
	/**
	 * 将靠的太近的mapInfo删掉一些只保留一个score最大的
	 * @param lsMapInfo
	 * @param distance 距离多少算近，建议2000
	 * @param max 保留score大的还是小的
	 * @return
	 */
	public static List<RegionInfo> getCombLsMapInfoBigScore(List<RegionInfo> lsMapInfo, int distance, boolean max) {
		List<RegionInfo> lsMapInfoResult = new ArrayList<RegionInfo>();
		//装入hashmap
		HashMap<Integer, RegionInfo> mapSummitSite2MapInfo = new LinkedHashMap<Integer, RegionInfo>();
		for (RegionInfo mapInfo : lsMapInfo) {
			if (mapSummitSite2MapInfo.containsKey(getSummitSite(mapInfo))) {
				RegionInfo mapInfo2 = mapSummitSite2MapInfo.get(getSummitSite(mapInfo));
				if ((max && mapInfo2.getScore() >= mapInfo.getScore()) || (!max && mapInfo2.getScore() <= mapInfo.getScore())) {
					continue;
				}
			}
			mapSummitSite2MapInfo.put(getSummitSite(mapInfo), mapInfo);
		}
		//整理格式
		ArrayList<double[]> lsSummitSite2Score = new ArrayList<double[]>();
		for (int summitsite : mapSummitSite2MapInfo.keySet()) {
			double[] info = new double[]{summitsite, mapSummitSite2MapInfo.get(summitsite).getScore()};
			lsSummitSite2Score.add(info);
		}
		
		//调用方法过滤合并
		ArrayList<double[]> lsCombine = MathComput.combLs(lsSummitSite2Score, distance, max);
		
		//输出
		for (double[] ds : lsCombine) {
			RegionInfo mapInfo = mapSummitSite2MapInfo.get(ds[0]);
			lsMapInfoResult.add(mapInfo);
		}
		return lsMapInfoResult;
	}
	
	/** 有summit返回 summit，没有则返回media */
	private static int getSummitSite(RegionInfo mapInfo) {
		return mapInfo.getSummit() > 0? mapInfo.getSummit() : mapInfo.getMidSite();
	}
	
	/**
	 * 给定mapInfo的序列，用mapInfo的summit点来筛选peak，将summit点距离在distance以内的删除，只保留权重最大的那个mapInfo
	 * @param lsmapinfo 用mapInfo的summit点来筛选peak
	 * @param distance 将summit点距离在distance以内的删除
	 * @param max true：选择权重最大的 false：选择权重最小的
	 * @return
	 */
	public static List<RegionInfo> sortLsMapInfo(List<RegionInfo> lsmapinfo, double distance, boolean max) {
		Map<String, List<double[]>> mapChrId2LsMapInfo = new HashMap<>();
		Map<String, RegionInfo> hashMapInfo = new HashMap<String, RegionInfo>();
		for (RegionInfo mapInfo : lsmapinfo) {
			List<double[]> lsTmp = null;
			if (!mapChrId2LsMapInfo.containsKey(mapInfo.getRefID())) {
				lsTmp = new ArrayList<double[]>();
				mapChrId2LsMapInfo.put(mapInfo.getRefID(), lsTmp);
			}
			else {
				lsTmp = mapChrId2LsMapInfo.get(mapInfo.getRefID());
			}
			double[] info = new double[2];
			info[0] = mapInfo.getMidSite();
			info[1] = mapInfo.getScore();
			lsTmp.add(info);
			hashMapInfo.put(mapInfo.getRefID() + mapInfo.getMidSite(), mapInfo);
		}
		
		List<RegionInfo> lsResult = new ArrayList<>();
		
		for (String chrId : mapChrId2LsMapInfo.keySet()) {
			List<double[]> lsDouble = mapChrId2LsMapInfo.get(chrId);
			lsDouble = MathComput.combLs(lsDouble, distance, max);
			for (double[] ds : lsDouble) {
				lsResult.add(hashMapInfo.get(chrId + ds[0]));
			}
		}
		return lsResult;
	}
	/**
	 * 给定一个MapInfo，返回该组里面的最长Up和最长Down
	 * 考虑了方向
	 * 所谓Up就是
	 * @param lsPeakInfo
	 * @return
	 */
	public static int[] getLsMapInfoUpDown(List<RegionInfo> lsPeakInfo) {
		int maxUp = 0; int maxDown = 0;
		for (RegionInfo siteInfo : lsPeakInfo) {
			int tmpUp, tmpDown;
			if (siteInfo.isCis5to3()== null || siteInfo.isCis5to3()) {
				tmpUp = siteInfo.getSummit() - siteInfo.getStartAbs();
				tmpDown = siteInfo.getEndAbs() - siteInfo.getSummit();
			} else {
				tmpUp = siteInfo.getStartCis() - siteInfo.getSummit();
				tmpDown = siteInfo.getSummit() - siteInfo.getEndCis();
			}

			if (tmpUp > maxUp) {
				maxUp = tmpUp;
			}
			if (tmpDown > maxDown) {
				tmpDown = maxDown;
			}
		}
		return new int[]{maxUp,maxDown};
	}

	@Override
	public String getName() {
		return name;
	}
	
	public static class RegionInfoComparator implements Comparator<RegionInfo> {
		/** 比较mapinfo的起点终点 */
		public static final int COMPARE_LOCSITE = 100;
		/** 比较mapinfo的flag site */
		public static final int COMPARE_LOCSUMMIT = 200;
		public static final int COMPARE_LOCMIDDLE = 300;
		/** 比较mapinfo的score */
		public static final int COMPARE_SCORE = 400;
		
		int compareType = COMPARE_SCORE;
		
		boolean min2max = true;
		
		/** 是否为从小到大排序 */
		public void setMin2max(boolean min2max) {
			this.min2max = min2max;
		}
		public void setCompareType(int compareType) {
			this.compareType = compareType;
		}
		@Override
		public int compare(RegionInfo o1, RegionInfo o2) {
			int i = o1.getRefID().compareTo(o2.getRefID());
			if (i != 0) return i;
			
			double o1Num = 0, o2Num = 0;
			if (compareType == COMPARE_LOCMIDDLE) {
				o1Num = o1.getMidSite();
				o2Num = o2.getMidSite();
			} else if (compareType == COMPARE_LOCSUMMIT) {
				o1Num = o1.getSummit();
				o2Num = o2.getSummit();
			} else if (compareType == COMPARE_SCORE) {
				o1Num = o1.getScore();
				o2Num = o2.getScore();
			} else if (compareType == COMPARE_LOCSITE) {
				if (min2max) {
					o1Num = o1.getStartAbs();
					o2Num = o2.getStartAbs();
				} else {
					o1Num = o1.getEndAbs();
					o2Num = o2.getEndAbs();
				}
			}
			
			int result = Double.valueOf(o1Num).compareTo(Double.valueOf(o2Num));
			if (!min2max) {
				result = -result;
			}
			return result;
		}
		
		
		
		
	}


}
