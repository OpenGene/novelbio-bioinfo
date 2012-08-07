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
 * �ڽ�����weight����ķ���
 * @author zong0jie
 *
 */
public class MapInfo extends SiteInfo implements HeatChartDataInt, Cloneable{
	Logger logger = Logger.getLogger(MapInfo.class);
	/** �Ƚ�mapinfo������յ� */
	public static final int COMPARE_LOCSITE = 100;
	/** �Ƚ�mapinfo��flag site */
	public static final int COMPARE_LOCFLAG = 200;
	/** �Ƚ�mapinfo��score */
	public static final int COMPARE_SCORE = 300;
	
	static int compareInfo = COMPARE_SCORE;
	
	private double[] value = null;
	/**
	 * �Ƿ���cis5to3����Ϣ����תValue��double[]
	 * Ĭ�Ϸ�ת
	 */
	boolean correctUseCis5to3ToConvertValue = true;
	
	public MapInfo() { }
	/**
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flagLoc �ض���һ��λ�����꣬Ʃ��ATGsite��summitSite��
	 * @param weight
	 * @param title ����Ŀ�����֣�Ʃ���������
	 */
	public MapInfo(String chrID, int startLoc, int endLoc, int flagLoc ,double weight, String title) {
		super(chrID, startLoc, endLoc, flagLoc, weight, title);
	}
	
	/**
	 * �Ƿ���cis5to3����Ϣ����תValue��double[]
	 * Ĭ�Ϸ�ת
	 * @param correct
	 */
	public void setCorrectUseCis5to3(boolean correctUseCis5to3ToConvertValue) {
		this.correctUseCis5to3ToConvertValue = correctUseCis5to3ToConvertValue;
	}
	/**
	 * ���startLoc < endLoc,��cis5to3�趨Ϊ����
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 */
	public MapInfo(String chrID, int startLoc, int endLoc) {
		super(chrID, startLoc, endLoc);
	}
	/**
	 * �Ƿ��С��������
	 * @return
	 */
	public static boolean isMin2max() {
		return min2max;
	}
	/**
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flag �Ƚϵı�ǩ�������Ǳ��ֵ��
	 * @param name ����Ŀ�����֣�Ʃ���������
	 */
	public MapInfo(String chrID) {
		this.refID = chrID;
	}
	/**
	 * ѡ��COMPARE_LOCSITE��
	 * Ĭ��COMPARE_WEIGHT
	 * @param COMPARE_TYPE
	 */
	public static void setCompareType(int COMPARE_TYPE) {
		compareInfo = COMPARE_TYPE;
	}
	/**
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flag �Ƚϵı�ǩ�������Ǳ��ֵ��
	 * @param title ����Ŀ�����֣�Ʃ���������
	 */
	public MapInfo(String chrID,double weight, String title) {
		this.refID = chrID;
		this.score = weight;
		this.name = title;
	}
	/** ���value�ľ�ֵ����������ڣ��򷵻�null */
	public Double getMean() {
		if (value == null) {
			return null;
		}
		return MathComput.mean(value);
	}
	/** ���value����λ������������ڣ��򷵻�null */
	public Double getMedian() {
		if (value == null) {
			return null;
		}
		return MathComput.median(value);
	}
	/**
	 * ������趨��ʱ�򣬻����mapinfo�ķ�����У�Ҳ����˵�����mapInfoΪ������ֱ�Ӹ�ֵ
	 * ����Ļ��͵ߵ�һ��Ȼ���ٸ�ֵ
	 * @param value
	 */
	public void setDouble(double[] value) {
		this.value = value;
	}
	
	/**
	 * �����cis5to3�Լ�correct����Ϣ������value��ֵ
	 * ����ȷ����ֱ������������value�ߵ�
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
	 * ���Ƚ�refID��startLoc,endLoc,score.flagLoc
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
	 * ��lsmapinfo1 ��ȥ lsmapinfo2����Ϣ
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

	
}
