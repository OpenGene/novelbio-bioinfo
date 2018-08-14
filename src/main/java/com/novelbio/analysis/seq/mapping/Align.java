package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.chipseq.ExceptionNBCChIPAlignError;
import com.novelbio.base.ExceptionNbcBean;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;


/**
 * 最简单的Alignment
 * @author zong0jie
 * 重写了hashcode和equals方法，为 chrID+start+end
 */
public class Align implements Alignment, Cloneable {
	static PatternOperate patternOperate = new PatternOperate("(-{0,1}\\d+)-(-{0,1}\\d+)");
	int start, end;
	String chrID;
	protected Boolean cis5to3;
	
	protected Align() {}
	/**
	 * 根据输入的start和end自动正反向cis5to3
	 * @param chrID
	 * @param start
	 * @param end
	 */
	public Align(String chrID, int start, int end) {
		this.chrID = chrID;
		if (start < end) {
			cis5to3 = true;
		} else if (start > end) {
			cis5to3 = false;
		}
		this.start = Math.min(start, end);
		this.end = Math.max(start, end);

	}
	
	/**
	 * 根据输入的start和end自动正反向cis5to3
	 * @param chrID
	 * @param start
	 * @param end
	 */
	public Align(Alignment alignment) {
		this.chrID = alignment.getRefID();
		this.start = alignment.getStartAbs();
		this.end = alignment.getEndAbs();
		this.cis5to3 = alignment.isCis5to3();
	}
	
	/**
	 * 输入类似 chr16:77099624-77099746
	 * 这种
	 */
	public Align(String chrInfo) {
		String[] ss = chrInfo.split(":");
		this.chrID = ss[0];
		try {
			int start = Integer.parseInt(patternOperate.getPatFirst(ss[1], 1));
			int end = Integer.parseInt(patternOperate.getPatFirst(ss[1], 2));
			if (start < end) {
				cis5to3 = true;
			} else if (start > end) {
				cis5to3 = false;
			}
			this.start = Math.min(start, end);
			this.end = Math.max(start, end);
		} catch (Exception e) {
			throw new ExceptionNBCChIPAlignError("cannot parse location " + chrInfo, e);
		}

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
		
		this.start = Math.min(startLoc, endLoc);
		this.end = Math.max(startLoc, endLoc);
		this.cis5to3 = true;
		if (startLoc > endLoc) {
			this.cis5to3 = false;
		}
	}
	
	/** 会覆盖已有的cis5to3 */
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	public void setChrID(String chrID) {
		this.chrID = chrID;
	}
	public void setStartAbs(int start) {
		this.start = start;
	}
	public void setEndAbs(int end) {
		this.end = end;
	}
	public void setStartCis(int start) {
		if (isCis()) {
			this.start = start;
		} else {
			this.end = start;
		}
	}
	public void setEndCis(int end) {
		if (isCis()) {
			this.end = end;
		} else {
			this.start = end;
		}
	}
	public void startAddLenCis(int len) {
		if (isCis()) {
			start+=len;
		} else {
			end -= len;
		}
		validateCis();
	}
	public void endAddLenCis(int len) {
		if (isCis()) {
			end+=len;
		} else {
			start -= len;
		}
		validateCis();
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
	public boolean isCis() {
		return cis5to3 == null || cis5to3;
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
		return StringOperate.isEqual(chrID, otherAlign.chrID) && start == otherAlign.start && end == otherAlign.end && cis5to3 == otherAlign.cis5to3;
	}
	
	/**
	 * 不带方向的返回结果string
	 */
	public String toStringNoStrand() {
		return chrID + ":" + getStartAbs() + "-" + getEndAbs();
	}
	
	private void validateCis() {
		if (start > end) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * 带方向的返回结果string
	 */
	public String toString() {
		return chrID + ":" + getStartCis() + "-" + getEndCis();
	}
	
	/** 考虑方向的合并，将overlap的align合并为一个align */
	public static List<Align> mergeLsAlign(List<Align> lsAlign) {
		if (lsAlign.isEmpty()) {
			return new ArrayList<Align>();
		}
		
		Boolean isCis = lsAlign.get(0).isCis5to3();
		List<double[]> lsDouble = new ArrayList<double[]>();
		for (Align align : lsAlign) {
			if (!isEqual(isCis, align.isCis5to3())) {
				throw new ExceptionNbcParamError("input lsAlign have inconsistent strand");
			}
			lsDouble.add(new double[]{align.getStartAbs(), align.getEndAbs()});
		}
		List<double[]> lsMerge = MathComput.combInterval(lsDouble, 0);
		List<Align> lsResult = new ArrayList<Align>();
		for (double[] ds : lsMerge) {
			Align align = new Align(lsAlign.get(0).getRefID(), (int)ds[0], (int)ds[1]);
			align.setCis5to3(isCis);
			lsResult.add(align);
		}
		return lsResult;
	}
	
	private static boolean isEqual(Boolean isCis1, Boolean isCis2) {
		if (isCis1 == null && isCis2 == null) {
			return true;
		}
		if (isCis1 == null || isCis2 == null) {
			return false;
		}
		return isCis1.equals(isCis2);
	}
	
	/**
	 * 从一个list中获取其最前的坐标和最后的坐标，组成一个align
	 * @return
	 */
	public static Align getAlignFromList(List<? extends Alignment> lsAlign) {
		Align alignResult = new Align(lsAlign.get(0));
		for (Alignment align : lsAlign) {
			if (align.getStartAbs() < alignResult.getStartAbs()) {
				alignResult.setStartAbs(align.getStartAbs());
			}
			if (align.getEndAbs() > alignResult.getEndAbs()) {
				alignResult.setEndAbs(align.getEndAbs());
			}
		}
		return alignResult;
	}
	
	@Override
	public Align clone() {
		try {
			return (Align) super.clone();
		} catch (CloneNotSupportedException e) {
			//应该不会报错
			throw new ExceptionNbcBean(e);
		}
	}
}
