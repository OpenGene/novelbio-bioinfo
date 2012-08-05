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
	/** �Ƚ�mapinfo������յ� */
	public static final int COMPARE_LOCSITE = 100;
	/** �Ƚ�mapinfo��flag site */
	public static final int COMPARE_LOCFLAG = 200;
	/** �Ƚ�mapinfo��score */
	public static final int COMPARE_SCORE = 300;
	
	static int compareType = COMPARE_SCORE;
	//��С��������
	static boolean min2max = true;
	
	protected String refID = "";
	protected int startLoc = ListCodAbs.LOC_ORIGINAL;
	protected int endLoc = ListCodAbs.LOC_ORIGINAL;
	protected Double score = null; // �Ƚϵı�ǩ�������Ǳ���

	
	protected String name = "";
	protected String description = "";
	protected int flagLoc = ListCodAbs.LOC_ORIGINAL;
	/** null��ʾû�з��� */
	protected Boolean cis5to3 = null;
	//��������
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
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flagLoc �ض���һ��λ�����꣬Ʃ��ATGsite��summitSite��
	 * @param weight
	 * @param title ����Ŀ�����֣�Ʃ���������
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
	 * ���startLoc < endLoc,��cis5to3�趨Ϊ����
	 * @param chrID
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
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
	 * @param startLoc ��0��ʼ�����startLoc��endLoc��С�ڵ���0������Ҫ�Է�����ȫ����Ϣ
	 * @param endLoc ��0��ʼ
	 * @param flag �Ƚϵı�ǩ�������Ǳ��ֵ��
	 * @param title ����Ŀ�����֣�Ʃ���������
	 */
	public SiteInfo(String chrID,double weight, String title) {
		this.refID = chrID;
		this.score = weight;
		this.name = title;
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
	 * ������ķ������ڻ����Tss��Tes������
	 * @param cis5to3
	 */
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	/**
	 * ������ķ������ڻ����Tss��Tes������
	 * ����޷����򷵻�null
	 * @return
	 */
	public Boolean isCis5to3() {
		return cis5to3;
	}
	/**
	 * ��{@link #isCis5to3()} ���ƵĹ��ܣ�ֻ����true����"+"��false����"-"
	 * null ���� ""
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
	 * �Ƿ��С��������
	 * @return
	 */
	public static boolean isMin2max() {
		return min2max;
	}

	/**
	 * ѡ��COMPARE_LOCSITE��
	 * Ĭ��COMPARE_WEIGHT
	 * @param COMPARE_TYPE
	 */
	public static void setCompareType(int COMPARE_TYPE) {
		compareType = COMPARE_TYPE;
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
		if (seqFasta != null) {
			seqFasta.setName(getName());
		}
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
	 * ������ĺ�������
	 * seqfasta��name ��map��nameȥ�趨
	 * @param seqFasta
	 * @param setName 
	 * @param reservecom �Ƿ����cis5to3���з�������
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
	public int getStartAbs() {
		return startLoc;
	}

	/**
	 * ����յ����꣬start��С��end
	 * @return
	 */
	public int getEndAbs() {
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
	/**
	 * ���ڱȽϵģ���С�����
	 * �ȱ�refID��Ȼ���start��end�����߱�flag���߱�score
	 * ��score��ʱ��Ͳ�����refID��
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
		logger.error("��¡����");
		return null;
	}
	/**
	 * ����mapInfo�����У���mapInfo��summit����ɸѡpeak����summit�������distance���ڵ�ɾ����ֻ����Ȩ�������Ǹ�mapInfo
	 * @param lsmapinfo ��mapInfo��summit����ɸѡpeak
	 * @param distance ��summit�������distance���ڵ�ɾ��
	 * @param max true��ѡ��Ȩ������ false��ѡ��Ȩ����С��
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
	 * ����һ��MapInfo�����ظ���������Up���Down
	 * ��νUp����
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
	 * ���Ƚ�refID��startLoc,endLoc,score.flagLoc
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
	 * ���ж������Ƿ�һ��
	 * �����ж�start��end�Ƿ�һ��
	 */
	public boolean equalsLoc(SiteInfo mapInfo) {
		if (mapInfo.getStartAbs() == getStartAbs() && mapInfo.getEndAbs() == getEndAbs()) {
			return true;
		}
		return false;
	}
	

}
