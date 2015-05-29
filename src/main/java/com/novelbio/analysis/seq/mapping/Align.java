package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.MathComput;


/**
 * 最简单的Alignment
 * @author zong0jie
 * 重写了hashcode和equals方法，为 chrID+start+end
 */
public class Align implements Alignment {
	int start, end;
	String chrID;
	Boolean cis5to3;
	/**
	 * 根据输入的start和end自动正反向cis5to3
	 * @param chrID
	 * @param start
	 * @param end
	 */
	public Align(String chrID, int start, int end) {
		this.chrID = chrID;
		this.start = Math.min(start, end);
		this.end = Math.max(start, end);
		if (start < end) {
			cis5to3 = true;
		} else if (start > end) {
			cis5to3 = false;
		}
	}
	/**
	 * 输入类似 chr16:77099624-77099746
	 * 这种
	 */
	public Align(String chrInfo) {
		String[] ss = chrInfo.split(":");
		this.chrID = ss[0];
		int start = Integer.parseInt(ss[1].split("-")[0]);
		int end = Integer.parseInt(ss[1].split("-")[1]);
		this.start = Math.min(start, end);
		this.end = Math.max(start, end);
		if (start < end) {
			cis5to3 = true;
		} else if (start > end) {
			cis5to3 = false;
		}
	}
	/** 会覆盖已有的cis5to3 */
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	public void setChrID(String chrID) {
		this.chrID = chrID;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	@Override
	public int getStartAbs() {
		return start;
	}

	@Override
	public int getEndAbs() {
		return end;
	}

	@Override
	public Boolean isCis5to3() {
		return cis5to3;
	}
	
	@Override
	public int getLength() {
		return Math.abs(start - end) + 1;
	}
	@Override
	public int getStartCis() {
		if (cis5to3 == null || isCis5to3()) {
			return Math.min(start, end);
		}
		return Math.max(start, end);
	}
	@Override
	public int getEndCis() {
		if (cis5to3 == null || isCis5to3()) {
			return Math.max(start, end);
		}
		return Math.min(start, end);
	}
	@Override
	public String getRefID() {
		return chrID;
	}
	/** 获得中间位点 */
	public int getMidSite() {
		return (start + end)/2;
	}
	
	@Override
	public int hashCode() {
		return (chrID + start + "_" + end).hashCode();
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		Align otherAlign = (Align)obj;
		if (chrID.equals(otherAlign.chrID) && start == otherAlign.start && end == otherAlign.end && cis5to3 == otherAlign.cis5to3) {
			return true;
		}
		return false;
	}
	
	/**
	 * 不带方向的返回结果string
	 */
	public String toStringNoCis() {
		return chrID + ":" + getStartAbs() + "-" + getEndAbs();
	}
	
	/** 不考虑方向的合并，将overlap的align合并为一个align */
	public static List<Align> mergeLsAlign(List<Align> lsAlign) {
		if (lsAlign.isEmpty()) {
			return new ArrayList<Align>();
		}
		
		List<double[]> lsDouble = new ArrayList<double[]>();
		for (Align align : lsAlign) {
			lsDouble.add(new double[]{align.getStartAbs(), align.getEndAbs()});
		}
		List<double[]> lsMerge = MathComput.combInterval(lsDouble, 0);
		List<Align> lsResult = new ArrayList<Align>();
		for (double[] ds : lsMerge) {
			Align align = new Align(lsAlign.get(0).getRefID(), (int)ds[0], (int)ds[1]);
			lsResult.add(align);
		}
		return lsAlign;
	}
	
}
