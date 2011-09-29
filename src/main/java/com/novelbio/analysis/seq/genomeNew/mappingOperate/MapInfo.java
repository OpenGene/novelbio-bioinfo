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
	double weight = 0; // �Ƚϵı�ǩ�������Ǳ���
	static boolean min2max = true;
	String title = "";
	double[] value = null;
	int summitLoc = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 
	 * @param chrID
	 * @param startLoc ��0��ʼ
	 * @param endLoc ��0��ʼ
	 * @param flag �Ƚϵı�ǩ�������Ǳ��ֵ��
	 * @param title ����Ŀ�����֣�Ʃ���������
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
	 * �Ƿ��С��������
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
	 * ����������
	 * @return
	 */
	public int getStart()
	{
		return startLoc;
	}
	/**
	 * ����յ�����
	 * @return
	 */
	public int getEnd()
	{
		return endLoc;
	}
	/**
	 * ��øû��������
	 * Ӧ����һ��Ψһ��ʶ������ȷ��ÿһ��������ʱ�޷�����ȷ��ת¼��
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * ���ڱȽϵģ���С�����
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
	 * ����mapInfo�����У���mapInfo��summit����ɸѡpeak����summit�������distance���ڵ�ɾ����ֻ����Ȩ�������Ǹ�mapInfo
	 * @param lsmapinfo ��mapInfo��summit����ɸѡpeak
	 * @param distance ��summit�������distance���ڵ�ɾ��
	 * @param max true��ѡ��Ȩ������ false��ѡ��Ȩ����С��
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
