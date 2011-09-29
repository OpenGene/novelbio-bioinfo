package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodAbs;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.java.HeatChartDataInt;

public class MapInfo implements Comparable<MapInfo>, HeatChartDataInt{
	String chrID = "";
	int startLoc = GffCodAbs.LOC_ORIGINAL;
	int endLoc = GffCodAbs.LOC_ORIGINAL;
	double weight = 0; // 比较的标签，可以是表达等
	static boolean min2max = true;
	String title = "";
	double[] value = null;
	int summitLoc = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 
	 * @param chrID
	 * @param startLoc 从0开始
	 * @param endLoc 从0开始
	 * @param flag 比较的标签，可以是表达值等
	 * @param title 本条目的名字，譬如基因名等
	 */
	public MapInfo(String chrID, int startLoc, int endLoc, int summitLoc ,double flag, String title)
	{
		this.chrID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.weight = flag;
		this.title = title;
		this.summitLoc = summitLoc;
	}
	/**
	 * 是否从小到大排序
	 */
	public static void sortPath(boolean min2max) {
		MapInfo.min2max = min2max;
	}
	public String getChrID() {
		return chrID;
	}
	public int getSummit() {
		return summitLoc;
	}
	/**
	 * 获得起点坐标
	 * @return
	 */
	public int getStart()
	{
		return startLoc;
	}
	/**
	 * 获得终点坐标
	 * @return
	 */
	public int getEnd()
	{
		return endLoc;
	}
	/**
	 * 获得该基因的名称
	 * 应该是一个唯一标识名用来确定每一个基因，暂时无法做到确定转录本
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * 用于比较的，从小到大比
	 */
	@Override
	public int compareTo(MapInfo map) {
		if (weight == map.weight) {
			return 0;
		}
		if (min2max) {
			return weight < map.weight ? -1:1;
		}
		else {
			return weight > map.weight ? -1:1;
		}
	}
	
	public void setDouble(double[] value) {
		this.value = value;
	}
	
	@Override
	public double[] getDouble() {
		return value;
	}
	/**
	 * 
	 * @return
	 */
	public double getWeight() {
		return weight;
	}
	/**
	 * 
	 * 给定mapInfo的序列，用mapInfo的summit点来筛选peak，将summit点距离在distance以内的删除，只保留权重最大的那个mapInfo
	 * @param lsmapinfo 用mapInfo的summit点来筛选peak
	 * @param distance 将summit点距离在distance以内的删除
	 * @param max true：选择权重最大的 false：选择权重最小的
	 * @return
	 */
	public static List<MapInfo> sortLsMapInfo(List<MapInfo> lsmapinfo, double distance) {
		Collections.sort(lsmapinfo, new Comparator<MapInfo>() {
			@Override
			public int compare(MapInfo o1, MapInfo o2) {
				if (o1.getChrID().equals(o2.getChrID())) {
					if (o1.getSummit() == o2.getSummit()) {
						return 0;
					}
					return o1.getSummit() < o2.getSummit() ? -1:1;
				}
				return o1.getChrID().compareTo(o2.getChrID());
			}
		});
		String chrIDOld = "";
		HashMap<String, ArrayList<double[]>> hashLsMapInfo = new HashMap<String, ArrayList<double[]>>();
		HashMap<String, MapInfo> hashMapInfo = new HashMap<String, MapInfo>();
		for (MapInfo mapInfo : lsmapinfo) {
			ArrayList<double[]> lsTmp = null;
			if (hashLsMapInfo.containsKey(mapInfo.getChrID())) {
				lsTmp = new ArrayList<double[]>();
				hashLsMapInfo.put(mapInfo.getChrID(), lsTmp);
			}
			else {
				lsTmp = hashLsMapInfo.get(mapInfo.chrID);
			}
			double[] info = new double[2];
			info[0] = mapInfo.getSummit();
			info[1] = mapInfo.getWeight();
			lsTmp.add(info);
			hashMapInfo.put(mapInfo.getChrID()+mapInfo.getSummit(), mapInfo);
		}
		
		ArrayList<MapInfo> lsResult = new ArrayList<MapInfo>();
		
		for (Entry<String, ArrayList<double[]>> entry : hashLsMapInfo.entrySet()) {
			String chrID = entry.getKey();
			ArrayList<double[]> lsDouble = entry.getValue();
			lsDouble = MathComput.combLs(lsDouble, distance, min2max);
			for (double[] ds : lsDouble) {
				lsResult.add(hashMapInfo.get(chrID+ ds[0]));
			}
		}
		return lsResult;
	}
}
