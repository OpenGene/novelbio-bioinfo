package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodAbs;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.java.HeatChartDataInt;
/**
 * �ڽ�����weight����ķ���
 * @author zong0jie
 *
 */
public class MapInfo implements Comparable<MapInfo>, HeatChartDataInt{
	String chrID = "";
	int startLoc = GffCodAbs.LOC_ORIGINAL;
	int endLoc = GffCodAbs.LOC_ORIGINAL;
	double weight = 0; // �Ƚϵı�ǩ�������Ǳ����
	static boolean min2max = true;
	String title = "";
	String description = "";
	String aaSeq = "";
	String nrSeq = "";
	double[] value = null;
	int flagLoc = GffCodAbs.LOC_ORIGINAL;
	/**
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flagLoc �ض���һ��λ�����꣬Ʃ��ATGsite��summitSite��
	 * @param flag �Ƚϵı�ǩ�������Ǳ���ֵ��
	 * @param title ����Ŀ�����֣�Ʃ���������
	 */
	public MapInfo(String chrID, int startLoc, int endLoc, int flagLoc ,double weight, String title)
	{
		this.chrID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.weight = weight;
		this.title = title;
		this.flagLoc = flagLoc;
	}
	/**
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flag �Ƚϵı�ǩ�������Ǳ���ֵ��
	 * @param title ����Ŀ�����֣�Ʃ���������
	 */
	public MapInfo(String chrID,double weight, String title)
	{
		this.chrID = chrID;
		this.weight = weight;
		this.title = title;
	}
	/**
	 * �趨һ��λ�㣬Ʃ��ATGsite��SummitSite֮���
	 * @param flagLoc
	 */
	public void setFlagLoc(int flagLoc) {
		this.flagLoc = flagLoc;
	}
	/**
	 * �趨����֮��Ķ�����symbol����
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * ���ڸ�λ��ľ�������������������
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * ���ڸ�����ľ�������
	 * @param description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * ������İ���������
	 * @param aaSeq
	 */
	public void setAaSeq(String aaSeq) {
		this.aaSeq = aaSeq;
	}
	/**
	 * ������İ���������
	 * @param aaSeq
	 */
	public String getAaSeq() {
		return aaSeq;
	}
	/**
	 * ������ĺ�������
	 * @param aaSeq
	 */
	public void setNrSeq(String nrSeq) {
		this.nrSeq = nrSeq;
	}
	/**
	 * ������ĺ�������
	 * @param aaSeq
	 */
	public String getNrSeq() {
		return nrSeq;
	}
	
	/**
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flag �Ƚϵı�ǩ�������Ǳ���ֵ��
	 * @param title ����Ŀ�����֣�Ʃ���������
	 */
	public MapInfo(String chrID)
	{
		this.chrID = chrID;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
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
	/**
	 * ���ָ����λ�㣬Ʃ��summit����atgsite�ȵ�
	 * ���startLoc <0 ����endLoc <0 ��ô˵�������յ�û���ã�ֱ�ӷ���flagLoc
	 * ���site <  startLoc 
	 *  �� site > endLoc����ô��ȡstart��end���м���(��������)
	 * @return
	 */
	public int getFlagSite() {
		if ( startLoc <0 || endLoc <0 || (flagLoc >= startLoc && flagLoc <= endLoc)) {
			return flagLoc;
		}
		return (int)((double)(startLoc+endLoc)/2+0.5) ;
	}
	/**
	 * ����������
	 * @return
	 */
	public int getStart()
	{
		return startLoc;
	}
	public void setEndLoc(int endLoc) {
		this.endLoc = endLoc;
	}
	public void setStartLoc(int startLoc) {
		this.startLoc = startLoc;
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
	 * ����weight����
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
	
	public MapInfo clone() {
		MapInfo mapInfo = new MapInfo(chrID, startLoc, endLoc, flagLoc, weight, title);
		double[] value2 = new double[value.length];
		for (int i = 0; i < value2.length; i++) {
			value2[i] = value[i];
		}
		mapInfo.setDescription(getDescription());
		mapInfo.setDouble(value2);
		return mapInfo;
	}
	/**
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
					if (o1.getFlagSite() == o2.getFlagSite()) {
						return 0;
					}
					return o1.getFlagSite() < o2.getFlagSite() ? -1:1;
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
			info[0] = mapInfo.getFlagSite();
			info[1] = mapInfo.getWeight();
			lsTmp.add(info);
			hashMapInfo.put(mapInfo.getChrID()+mapInfo.getFlagSite(), mapInfo);
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
	
	/**
	 * ����mapInfo��list
	 * �������double[]�ϲ���һ�����ٶ�mapInfo�е�value���ȳ�
	 * ����summitλ����룬�ٶ�summit��Ϊ0��
	 * @param lsmapinfo mapInfo����Ϣ
	 * @param upNum ���θ�����<=0��ʾ�����
	 * @param downNum ���θ���, <=0��ʾ�����
	 * @return
	 * �õ��Ľ�������lsmapinfo�ĳ�����Ϊ��׼��
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
	 * ������
	 * ����mapInfo��list
	 * �������double[]�ϲ���һ�����ٶ�mapInfo�е�value���ȳ�
	 * ����summitλ����룬�ٶ�summit��Ϊ0��
	 * @param lsmapinfo mapInfo����Ϣ
	 * @param upNum ���θ�����<=0��ʾ�����
	 * @param downNum ���θ���, <=0��ʾ�����
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
	 * ����һ��MapInfo�����ظ���������Up���Down
	 * @param lsmapinfo
	 * @return
	 */
	private static int[] getLsMapInfoUpDown(List<MapInfo> lsmapinfo)
	{
			int maxUp = 0; int maxDown = 0;
			for (MapInfo mapInfo : lsmapinfo) {
				int tmpUp = mapInfo.getFlagSite() - mapInfo.getStart();
				int tmpDown = mapInfo.getEnd() - mapInfo.getFlagSite();
				if (tmpUp > maxUp) {
					maxUp = tmpUp;
				}
				if (tmpDown > maxDown) {
					tmpDown = maxDown;
				}
			}
		return new int[]{maxUp,maxDown};
	}
	
	/**
	 * ����mapInfo��list
	 * �������double[]�ϲ���һ�����ٶ�mapInfo�е�value�ȳ�
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
	
//	boolean isExon = false;
//	/**
//	 * �Ƿ�����������
//	 * @return
//	 */
//	public boolean isExon()
//	{
//		return isExon;
//	}
//	/**
//	 * �趨�Ƿ�����������
//	 * @param isExon
//	 */
//	public void setExon(boolean isExon) {
//		this.isExon = isExon;
//	}
	
}