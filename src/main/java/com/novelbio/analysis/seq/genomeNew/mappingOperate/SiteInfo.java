package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;

public class SiteInfo implements Comparable<SiteInfo>, Alignment {

	Logger logger = Logger.getLogger(MapInfo.class);
	/** 比较mapinfo的起点终点 */
	public static final int COMPARE_LOCSITE = 100;
	/** 比较mapinfo的flag site */
	public static final int COMPARE_LOCFLAG = 200;
	/** 比较mapinfo的score */
	public static final int COMPARE_SCORE = 300;
	
	static int compareType = COMPARE_SCORE;
	//从小到大排序
	static boolean min2max = true;
	
	protected String refID = "";
	protected int startLoc = ListCodAbs.LOC_ORIGINAL;
	protected int endLoc = ListCodAbs.LOC_ORIGINAL;
	protected Double score = null; // 比较的标签，可以是表达等

	
	protected String name = "";
	protected String description = "";
	protected int flagLoc = ListCodAbs.LOC_ORIGINAL;
	/** null表示没有方向 */
	protected Boolean cis5to3 = null;
	//核酸序列
	SeqFasta seqFasta = new SeqFasta();
	
	
	public SiteInfo() { }
	/**
	 * @param chrID
	 */
	public SiteInfo(String chrID) {
		this.refID = chrID;
	}
	/**
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flagLoc 特定的一个位点坐标，譬如ATGsite，summitSite等
	 * @param weight
	 * @param title 本条目的名字，譬如基因名等
	 */
	public SiteInfo(String chrID, int startLoc, int endLoc, int flagLoc ,double weight, String title) {
		this.refID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.score = weight;
		this.name = title;
		this.flagLoc = flagLoc;
	}
	/**
	 * 如果startLoc < endLoc,则cis5to3设定为反向
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 */
	public SiteInfo(String chrID, int startLoc, int endLoc) {
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
	 * @param chrID
	 * @param startLoc 从0开始，如果startLoc和endLoc都小于等于0，则需要对方返回全长信息
	 * @param endLoc 从0开始
	 * @param flag 比较的标签，可以是表达值等
	 * @param title 本条目的名字，譬如基因名等
	 */
	public SiteInfo(String chrID,double weight, String title) {
		this.refID = chrID;
		this.score = weight;
		this.name = title;
	}
	/** 
	 * 如果start 大于end，则设定cis5to3为false
	 * 结果start恒小于end
	 * @param start 小于0自动设置为0
	 * @param endLoc 小于0自动设置为0
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
	 * 本坐标的方向，用于基因的Tss和Tes等运算
	 * @param cis5to3
	 */
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	/**
	 * 本坐标的方向，用于基因的Tss和Tes等运算
	 * 如果无方向，则返回null
	 * @return
	 */
	public Boolean isCis5to3() {
		return cis5to3;
	}
	/**
	 * 和{@link #isCis5to3()} 类似的功能，只不过true返回"+"，false返回"-"
	 * null 返回 ""
	 * @return
	 */
	public String getStrand() {
		if (cis5to3 == true) {
			return "+";
		}
		else if (cis5to3 == null) {
			return "";
		}
		return "-";
	}
	/**
	 * 是否从小到大排序
	 * @return
	 */
	public static boolean isMin2max() {
		return min2max;
	}

	/**
	 * 选择COMPARE_LOCSITE等
	 * 默认COMPARE_WEIGHT
	 * @param COMPARE_TYPE
	 */
	public static void setCompareType(int COMPARE_TYPE) {
		compareType = COMPARE_TYPE;
	}
	/**
	 * 按照方向进行延长
	 * 如果序列比设定的长度要长，则跳过
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
	 * 左右两端各延长range bp
	 * 如果总长度超过range * 2，则返回
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
	public void setName(String title) {
		this.name = title;
		if (seqFasta != null) {
			seqFasta.setName(getName());
		}
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
	 * 该区域的核酸序列，默认根据cis5to3进行反向序列
	 * seqfasta的name 用map的name去设定
	 * @param aaSeq
	 */
	public void setSeq(SeqFasta seqFasta) {
		if (seqFasta == null) {
			this.seqFasta = null;
			return;
		}
		if (cis5to3 != null && cis5to3 == false) {
			seqFasta = seqFasta.reservecom();
		}
		seqFasta.setName(getName());
		this.seqFasta = seqFasta;
	}
	/**
	 * 该区域的核酸序列
	 * seqfasta的name 用map的name去设定
	 * @param seqFasta
	 * @param setName 
	 * @param reservecom 是否根据cis5to3进行反向序列
	 */
	public void setSeq(SeqFasta seqFasta, boolean reservecom) {
		if (reservecom && cis5to3 != null && cis5to3 == false) {
			seqFasta = seqFasta.reservecom();
		}
		seqFasta.setName(getName());
		this.seqFasta = seqFasta;
	}
	/**
	 * 该区域的核酸序列
	 * 注意设定的时候是否已经反向过了
	 * seqfasta的name 用map的name去设定
	 * @param aaSeq
	 */
	public SeqFasta getSeqFasta() {
		return seqFasta;
	}
	public void setScore(double score) {
		this.score = score;
	}
	/**
	 * 是否从小到大排序
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
	 * 获得指定的位点，譬如summit或者atgsite等等
	 * 如果startLoc <0 或者endLoc <0 那么说明起点和终点没设置，直接返回flagLoc
	 * 如果site <  startLoc 
	 *  或 site > endLoc，那么就取start和end的中间数(四舍五入)
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
	 * 获得起点坐标
	 * start恒小于end
	 * @return
	 */
	public int getStartAbs() {
		return startLoc;
	}

	/**
	 * 获得终点坐标，start恒小于end
	 * @return
	 */
	public int getEndAbs() {
		return endLoc;
	}
	/**
	 * 获得该基因的名称
	 * 应该是一个唯一标识名用来确定每一个基因，暂时无法做到确定转录本
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * 用于比较的，从小到大比
	 * 先比refID，然后比start，end，或者比flag或者比score
	 * 比score的时候就不考虑refID了
	 */
	public int compareTo(SiteInfo siteInfo) {
		if (compareType == COMPARE_LOCFLAG) {
			int i = refID.compareTo(siteInfo.refID);
			if (i != 0) {
				return i;
			}
			if (flagLoc == siteInfo.flagLoc) {
				return 0;
			}
			if (min2max) {
				return flagLoc < siteInfo.flagLoc ? -1:1;
			}
			else {
				return flagLoc > siteInfo.flagLoc ? -1:1;
			}
		}
		else if (compareType == COMPARE_LOCSITE) {
			int i = refID.compareTo(siteInfo.refID);
			if (i != 0) {
				return i;
			}
			if (startLoc == siteInfo.startLoc) {
				if (endLoc == siteInfo.endLoc) {
					return 0;
				}
				if (min2max) {
					return endLoc < siteInfo.endLoc ? -1:1;
				}
				else {
					return endLoc > siteInfo.endLoc ? -1:1;
				}
			}
			if (min2max) {
				return startLoc < siteInfo.startLoc ? -1:1;
			}
			else {
				return startLoc > siteInfo.startLoc ? -1:1;
			}
		}
		else if (compareType == COMPARE_SCORE) {
			if (score == siteInfo.score) {
				return 0;
			}
			if (min2max) {
				return score < siteInfo.score ? -1:1;
			}
			else {
				return score > siteInfo.score ? -1:1;
			}
		}
		return 0;
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
	
	public SiteInfo clone() {
		
		SiteInfo siteInfo;
		try {
			siteInfo = (SiteInfo) super.clone();
			siteInfo.cis5to3 = cis5to3;
			siteInfo.description = description;
			siteInfo.endLoc = endLoc;
			siteInfo.flagLoc = flagLoc;
			siteInfo.name = name;
			siteInfo.refID = refID;
			siteInfo.score = score;
			siteInfo.seqFasta = seqFasta.clone();
			siteInfo.startLoc = startLoc;
			return siteInfo;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.error("克隆出错");
		return null;
	}
	/**
	 * 给定mapInfo的序列，用mapInfo的summit点来筛选peak，将summit点距离在distance以内的删除，只保留权重最大的那个mapInfo
	 * @param lsmapinfo 用mapInfo的summit点来筛选peak
	 * @param distance 将summit点距离在distance以内的删除
	 * @param max true：选择权重最大的 false：选择权重最小的
	 * @return
	 */
	public static<T extends SiteInfo> List<T> sortLsMapInfo(List<T> lsmapinfo, double distance) {
		HashMap<String, ArrayList<double[]>> hashLsMapInfo = new HashMap<String, ArrayList<double[]>>();
		HashMap<String, T> hashMapInfo = new HashMap<String, T>();
		for (T mapInfo : lsmapinfo) {
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
		
		ArrayList<T> lsResult = new ArrayList<T>();
		
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
	 * 给定一个MapInfo，返回该组里面的最长Up和最长Down
	 * 所谓Up就是
	 * @param lsSiteInfo
	 * @return
	 */
	public static int[] getLsMapInfoUpDown(List<? extends SiteInfo> lsSiteInfo) {
		int maxUp = 0; int maxDown = 0;
		for (SiteInfo siteInfo : lsSiteInfo) {
			int tmpUp = siteInfo.getFlagSite() - siteInfo.getStartAbs();
			int tmpDown = siteInfo.getEndAbs() - siteInfo.getFlagSite();
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
	 * 仅比较refID，startLoc,endLoc,score.flagLoc
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		SiteInfo otherObj = (SiteInfo)obj;
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
	 * 仅判断坐标是否一致
	 * 就是判断start和end是否一致
	 */
	public boolean equalsLoc(SiteInfo mapInfo) {
		if (mapInfo.getStartAbs() == getStartAbs() && mapInfo.getEndAbs() == getEndAbs()) {
			return true;
		}
		return false;
	}
	

}
