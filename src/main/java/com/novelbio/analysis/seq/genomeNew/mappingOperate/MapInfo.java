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
 * 内建根据weight排序的方法
 * @author zong0jie
 *
 */
public class MapInfo implements Comparable<MapInfo>, HeatChartDataInt{
	/**
	 * 比较mapinfo的起点终点
	 */
	public static final int COMPARE_LOCSITE = 100;
	/**
	 * 比较mapinfo的flag site
	 */
	public static final int COMPARE_LOCFLAG = 200;
	/**
	 * 比较mapinfo的weight
	 */
	public static final int COMPARE_WEIGHT = 100;
	
	int compareInfo = COMPARE_WEIGHT;
	
	String chrID = "";
	int startLoc = GffCodAbs.LOC_ORIGINAL;
	int endLoc = GffCodAbs.LOC_ORIGINAL;
	double weight = 0; // 比较的标签，可以是表达等
	//从小到大排序
	static boolean min2max = true;
	String title = "";
	String description = "";
	//核酸序列
	String nrSeq = "";
	private double[] value = null;
	int flagLoc = GffCodAbs.LOC_ORIGINAL;
	boolean cis5to3 = true;
	/**
	 * 本坐标的方向，用于基因的Tss和Tes等运算
	 * @param cis5to3
	 */
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	/**
	 * 本坐标的方向，用于基因的Tss和Tes等运算
	 * @return
	 */
	public boolean isCis5to3() {
		return cis5to3;
	}
	boolean correct = true;
	/**
	 * 是否用cis5to3的信息来翻转Value的double[]
	 * 默认翻转
	 * @param correct
	 */
	public void setCorrectUseCis5to3(boolean correct)
	{
		this.correct = correct;
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
	 * @param flagLoc 特定的一个位点坐标，譬如ATGsite，summitSite等
	 * @param flag 比较的标签，可以是表达值等
	 * @param title 本条目的名字，譬如基因名等
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
	 * 如果startLoc < endLoc,则cis5to3设定为反向
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 */
	public MapInfo(String chrID, int startLoc, int endLoc)
	{
		this.chrID = chrID;
		this.startLoc = Math.min(startLoc, endLoc);
		this.endLoc = Math.max(startLoc, endLoc);
		if (startLoc > endLoc) {
			setCis5to3(false);
		}
	}
	/**
	 * 选择COMPARE_LOCSITE等
	 * 默认COMPARE_WEIGHT
	 * @param COMPARE_TYPE
	 */
	public void setCompType(int COMPARE_TYPE) {
		this.compareInfo = COMPARE_TYPE;
	}
	
	
	/**
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flag 比较的标签，可以是表达值等
	 * @param title 本条目的名字，譬如基因名等
	 */
	public MapInfo(String chrID,double weight, String title)
	{
		this.chrID = chrID;
		this.weight = weight;
		this.title = title;
	}
	/**
	 * 设定一个位点，譬如ATGsite，SummitSite之类的
	 * @param flagLoc
	 */
	public void setFlagLoc(int flagLoc) {
		this.flagLoc = flagLoc;
	}
	/**
	 * 设定标题之类的东西，symbol好了
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * 对于该位点的具体描述，可以是序列
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * 对于该区域的具体描述
	 * @param description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 该区域的核酸序列
	 * @param aaSeq
	 */
	public void setNrSeq(String nrSeq) {
		this.nrSeq = nrSeq;
	}
	/**
	 * 该区域的核酸序列
	 * @param aaSeq
	 */
	public String getNrSeq() {
		return nrSeq;
	}
	
	/**
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flag 比较的标签，可以是表达值等
	 * @param title 本条目的名字，譬如基因名等
	 */
	public MapInfo(String chrID)
	{
		this.chrID = chrID;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
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
	/**
	 * 获得指定的位点，譬如summit或者atgsite等等
	 * 如果startLoc <0 或者endLoc <0 那么说明起点和终点没设置，直接返回flagLoc
	 * 如果site <  startLoc 
	 *  或 site > endLoc，那么就取start和end的中间数(四舍五入)
	 * @return
	 */
	public int getFlagSite() {
		if ( startLoc <0 || endLoc <0 || (flagLoc >= startLoc && flagLoc <= endLoc)) {
			return flagLoc;
		}
		return (int)((double)(startLoc+endLoc)/2+0.5) ;
	}
	/**
	 * 获得起点坐标
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
	 * 排序的时候不按照weight排序，而是按照site位点排序
	 * 默认false
	 * @param compFromSite
	 */
	public static void setCompSite(int compFromSite) {
		
	}
	
	/**
	 * 用于比较的，从小到大比
	 * 根据weight排序
	 */
	@Override
	public int compareTo(MapInfo map) {
		if (compareInfo == COMPARE_LOCFLAG) {
			int i = chrID.compareTo(map.chrID);
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
			int i = chrID.compareTo(map.chrID);
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
		else {
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
		double[] valueTmp = new double[value.length];
		for (int i = 0; i < valueTmp.length; i++) {
			valueTmp[i] = value[i];
		}
		if (!cis5to3 && correct) {
			ArrayOperate.convertArray(valueTmp);
		}
		return valueTmp;
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
			if (!hashLsMapInfo.containsKey(mapInfo.getChrID())) {
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
	 * 给定一个MapInfo，返回该组里面的最长Up和最长Down
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
	
//	boolean isExon = false;
//	/**
//	 * 是否在外显子中
//	 * @return
//	 */
//	public boolean isExon()
//	{
//		return isExon;
//	}
//	/**
//	 * 设定是否在外显子中
//	 * @param isExon
//	 */
//	public void setExon(boolean isExon) {
//		this.isExon = isExon;
//	}
	
}
