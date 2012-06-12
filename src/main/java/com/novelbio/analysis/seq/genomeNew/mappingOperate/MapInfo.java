package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.plot.java.HeatChartDataInt;
/**
 * �ڽ�����weight����ķ���
 * @author zong0jie
 *
 */
public class MapInfo implements Comparable<MapInfo>, HeatChartDataInt{
	/**
	 * �Ƚ�mapinfo������յ�
	 */
	public static final int COMPARE_LOCSITE = 100;
	/**
	 * �Ƚ�mapinfo��flag site
	 */
	public static final int COMPARE_LOCFLAG = 200;
	/**
	 * �Ƚ�mapinfo��score
	 */
	public static final int COMPARE_SCORE = 300;
	
	static int compareInfo = COMPARE_SCORE;
	
	protected String refID = "";
	protected int startLoc = ListCodAbs.LOC_ORIGINAL;
	protected int endLoc = ListCodAbs.LOC_ORIGINAL;
	protected Double score = null; // �Ƚϵı�ǩ�������Ǳ���
	//��С��������
	static boolean min2max = true;
	protected String name = "";
	protected String description = "";
	//��������
	SeqFasta seqFasta = new SeqFasta();
	private double[] value = null;
	protected int flagLoc = ListCodAbs.LOC_ORIGINAL;
	/**
	 * null��ʾû�з���
	 */
	protected Boolean cis5to3 = null;
	/**
	 * ������ķ������ڻ����Tss��Tes������
	 * @param cis5to3
	 */
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	/**
	 * ������ķ������ڻ����Tss��Tes������
	 * ����޷����򷵻�true
	 * @return
	 */
	public Boolean isCis5to3() {
		return cis5to3;
	}
	/**
	 * ��{@link #isCis5to3()} ���ƵĹ��ܣ�ֻ����true��null����"+"��false����"-"
	 * @return
	 */
	public String getStrand() {
		if (cis5to3 == null || cis5to3 == true) {
			return "+";
		}
		return "-";
	}
	boolean correct = true;
	/**
	 * �Ƿ���cis5to3����Ϣ����תValue��double[]
	 * Ĭ�Ϸ�ת
	 * @param correct
	 */
	public void setCorrectUseCis5to3(boolean correct)
	{
		this.correct = correct;
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
	 * @param flagLoc �ض���һ��λ�����꣬Ʃ��ATGsite��summitSite��
	 * @param flag �Ƚϵı�ǩ�������Ǳ��ֵ��
	 * @param title ����Ŀ�����֣�Ʃ���������
	 */
	public MapInfo(String chrID, int startLoc, int endLoc, int flagLoc ,double weight, String title)
	{
		this.refID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.score = weight;
		this.name = title;
		this.flagLoc = flagLoc;
	}
	
	/**
	 * ���startLoc < endLoc,��cis5to3�趨Ϊ����
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 */
	public MapInfo(String chrID, int startLoc, int endLoc)
	{
		if (startLoc < 0)
			startLoc = 0;
		if (endLoc < 0)
			endLoc = 0;
		
		this.refID = chrID;
		this.startLoc = Math.min(startLoc, endLoc);
		this.endLoc = Math.max(startLoc, endLoc);
		if (startLoc > endLoc) {
			setCis5to3(false);
		}
	}
	/**
	 * ѡ��COMPARE_LOCSITE��
	 * Ĭ��COMPARE_WEIGHT
	 * @param COMPARE_TYPE
	 */
	public static void setCompType(int COMPARE_TYPE) {
		compareInfo = COMPARE_TYPE;
	}
	/**
	 * ���շ�������ӳ�
	 * ������б��趨�ĳ���Ҫ����������
	 * @param length
	 */
	public void extend(int length) {
		if (Length() >= length) {
			return;
		}
		if (cis5to3 == null || cis5to3) {
			endLoc = startLoc + length;
		}
		else {
			startLoc = endLoc - length;
		}
	}
	/**
	 * �������˸��ӳ�range bp
	 * ����ܳ��ȳ���range * 2���򷵻�
	 * @param length
	 */
	public void extendCenter(int range) {
		if (Length() >= range*2) {
			return;
		}
		int loc = getMidLoc();
		startLoc = loc - range;
		endLoc = loc + range;
	}
	public int Length() {
		return Math.abs(endLoc - startLoc);
	}
	/**
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flag �Ƚϵı�ǩ�������Ǳ��ֵ��
	 * @param title ����Ŀ�����֣�Ʃ���������
	 */
	public MapInfo(String chrID,double weight, String title)
	{
		this.refID = chrID;
		this.score = weight;
		this.name = title;
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
	public void setName(String title) {
		this.name = title;
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
	 * ������ĺ������У�Ĭ�ϸ���cis5to3���з�������
	 * seqfasta��name ��map��nameȥ�趨
	 * @param aaSeq
	 */
	public void setSeq(SeqFasta seqFasta) {
		if (cis5to3 != null && cis5to3 == false) {
			seqFasta = seqFasta.reservecom();
		}
		seqFasta.setName(getName());
		this.seqFasta = seqFasta;
	}
	/**
	 * ������ĺ�������
	 * seqfasta��name ��map��nameȥ�趨
	 * @param seqFasta
	 * @param setName �Ƿ����cis5to3���з�������
	 */
	public void setSeq(SeqFasta seqFasta, boolean reservecom) {
		if (reservecom && cis5to3 != null && cis5to3 == false) {
			seqFasta = seqFasta.reservecom();
		}
		seqFasta.setName(getName());
		this.seqFasta = seqFasta;
	}
	/**
	 * ������ĺ�������
	 * ע���趨��ʱ���Ƿ��Ѿ��������
	 * seqfasta��name ��map��nameȥ�趨
	 * @param aaSeq
	 */
	public SeqFasta getSeqFasta() {
		return seqFasta;
	}
	public MapInfo() { }
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
	
	public void setScore(double score) {
		this.score = score;
	}
	/**
	 * �Ƿ��С��������
	 */
	public static void sortPath(boolean min2max) {
		MapInfo.min2max = min2max;
	}
	public String getRefID() {
		return refID;
	}
	public void setRefID(String refID) {
		this.refID = refID;
	}
	/**
	 * ���ָ����λ�㣬Ʃ��summit����atgsite�ȵ�
	 * ���startLoc <0 ����endLoc <0 ��ô˵�������յ�û���ã�ֱ�ӷ���flagLoc
	 * ���site <  startLoc 
	 *  �� site > endLoc����ô��ȡstart��end���м���(��������)
	 * @return
	 */
	public int getFlagSite() {
		if ( startLoc < -10000 || endLoc < -10000 || (flagLoc >= startLoc && flagLoc <= endLoc)) {
			return flagLoc;
		}
		return (int)((double)(startLoc+endLoc)/2+0.5) ;
	}
	public int getMidLoc() {
		return (startLoc + endLoc)/2;
	}
	/**
	 * ����������
	 * start��С��end
	 * @return
	 */
	public int getStart() {
		return startLoc;
	}
	/** 
	 * ���start ����end�����趨cis5to3Ϊfalse
	 * ���start��С��end
	 * @param start С��0�Զ�����Ϊ0
	 * @param endLoc С��0�Զ�����Ϊ0
	 */
	public void setStartEndLoc(int startLoc, int endLoc) {
		if (startLoc < 0)
			startLoc = 0;
		if (endLoc < 0)
			endLoc = 0;
		
		this.startLoc = Math.min(startLoc, endLoc);
		this.endLoc = Math.max(startLoc, endLoc);
		if (startLoc > endLoc) {
			setCis5to3(false);
		}
	}
	/**
	 * ����յ����꣬start��С��end
	 * @return
	 */
	public int getEnd() {
		return endLoc;
	}
	/**
	 * ��øû��������
	 * Ӧ����һ��Ψһ��ʶ������ȷ��ÿһ��������ʱ�޷�����ȷ��ת¼��
	 * @return
	 */
	public String getName() {
		return name;
	}
	/** ��������ڣ��򷵻�null */
	public Double getMean() {
		if (value == null) {
			return null;
		}
		return MathComput.mean(value);
	}
	/** ��������ڣ��򷵻�null */
	public Double getMedian() {
		if (value == null) {
			return null;
		}
		return MathComput.median(value);
	}
	/**
	 * ���ڱȽϵģ���С�����
	 * �ȱ�refID��Ȼ���start��end�����߱�flag���߱�score
	 * ��score��ʱ��Ͳ�����refID��
	 */
	@Override
	public int compareTo(MapInfo map) {
		if (compareInfo == COMPARE_LOCFLAG) {
			int i = refID.compareTo(map.refID);
			if (i != 0) {
				return i;
			}
			if (flagLoc == map.flagLoc) {
				return 0;
			}
			if (min2max) {
				return flagLoc < map.flagLoc ? -1:1;
			}
			else {
				return flagLoc > map.flagLoc ? -1:1;
			}
		}
		else if (compareInfo == COMPARE_LOCSITE) {
			int i = refID.compareTo(map.refID);
			if (i != 0) {
				return i;
			}
			if (startLoc == map.startLoc) {
				if (endLoc == map.endLoc) {
					return 0;
				}
				if (min2max) {
					return endLoc < map.endLoc ? -1:1;
				}
				else {
					return endLoc > map.endLoc ? -1:1;
				}
			}
			if (min2max) {
				return startLoc < map.startLoc ? -1:1;
			}
			else {
				return startLoc > map.startLoc ? -1:1;
			}
		}
		else if (compareInfo == COMPARE_SCORE) {
			if (score == map.score) {
				return 0;
			}
			if (min2max) {
				return score < map.score ? -1:1;
			}
			else {
				return score > map.score ? -1:1;
			}
		}
		return 0;
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
		if (cis5to3 != null && !cis5to3 && correct) {
			ArrayOperate.convertArray(valueTmp);
		}
		return valueTmp;
	}
	/**
	 * @return
	 */
	public double getScore() {
		if (score == null) {
			return 0;
		}
		return score;
	}
	
	public MapInfo clone() {
		MapInfo mapInfo = new MapInfo(refID, startLoc, endLoc, flagLoc, score, name);
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
		//����
//		Collections.sort(lsmapinfo, new Comparator<MapInfo>() {
//			@Override
//			public int compare(MapInfo o1, MapInfo o2) {
//				if (o1.getRefID().equals(o2.getRefID())) {
//					if (o1.getMidLoc() == o2.getMidLoc()) {
//						return 0;
//					}
//					return o1.getMidLoc() < o2.getMidLoc() ? -1:1;
//				}
//				return o1.getRefID().compareTo(o2.getRefID());
//			}
//		});
		HashMap<String, ArrayList<double[]>> hashLsMapInfo = new HashMap<String, ArrayList<double[]>>();
		HashMap<String, MapInfo> hashMapInfo = new HashMap<String, MapInfo>();
		for (MapInfo mapInfo : lsmapinfo) {
			ArrayList<double[]> lsTmp = null;
			if (!hashLsMapInfo.containsKey(mapInfo.getRefID())) {
				lsTmp = new ArrayList<double[]>();
				hashLsMapInfo.put(mapInfo.getRefID(), lsTmp);
			}
			else {
				lsTmp = hashLsMapInfo.get(mapInfo.refID);
			}
			double[] info = new double[2];
			info[0] = mapInfo.getMidLoc();
			info[1] = mapInfo.getScore();
			lsTmp.add(info);
			hashMapInfo.put(mapInfo.getRefID() + mapInfo.getMidLoc(), mapInfo);
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
	
	/**
	 * ��ûʵ��
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
	
	/**
	 * ���ж������Ƿ�һ��
	 * �����ж�start��end�Ƿ�һ��
	 */
	public boolean equalsLoc(MapInfo mapInfo) {
		if (mapInfo.getStart() == getStart() && mapInfo.getEnd() == getEnd()) {
			return true;
		}
		return false;
	}
	
}
