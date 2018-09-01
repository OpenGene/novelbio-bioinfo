package com.novelbio.bioinfo.base.binarysearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.bioinfo.base.Alignment;

/**
 * 双坐标
 * @author zong0jie
 *
 */
public class BsearchSiteDu<T extends Alignment>  {
	/** peak与左端Item交集时，交集在左端Item中所占的比例 */
	protected double opLeftInItem = -1;
	/** peak与左端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内  */
	protected double opLeftInCod = -1;
	/**  peak与左端Item交集时，实际交集的bp数  */
	protected int opLeftBp = -1;
	/**  peak与右端Item交集时，交集在左端Item中所占的比例 */
	protected double opRightInItem = -1;
	/**  peak与右端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内  */
	protected double opRightInCod = -1;
	/** peak与右端Item交集时，实际交集的bp数 */
	protected int opRightBp = -1;
	
	protected List<T> lsAlignMid = new ArrayList<T>();
	
	protected BsearchSite<T> siteLeft;
	protected BsearchSite<T> siteRight;	

	public BsearchSiteDu(List<T> lsAlignMid, BsearchSite<T> siteLeft, BsearchSite<T> siteRight) {
		this.lsAlignMid = lsAlignMid;
		this.siteLeft = siteLeft;
		this.siteRight = siteRight;
		calInfo();
	}
	public BsearchSiteDu(BsearchSite<T> siteLeft, BsearchSite<T> siteRight) {
		this.siteLeft = siteLeft;
		this.siteRight = siteRight;
		calInfo();
	}
	public BsearchSite<T> getSiteLeft() {
		return siteLeft;
	}
	public BsearchSite<T> getSiteRight() {
		return siteRight;
	}
	public void setLsAlignMid(List<T> lsAlignMid) {
		this.lsAlignMid = lsAlignMid;
	}
	/**
	 *  peak与左端Item交集时，交集在左端Item中所占的比例
	 * @return
	 */
	public double getOpLeftInItem() {
		return opLeftInItem;
	}
	/**
	 * peak与右端Item交集时，交集在左端Item中所占的比例
	 * @return
	 */
	public double getOpRightInItem() {
		return opRightInItem;
	}
	/**
	 *  peak与左端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内
	 * @return
	 */
	public double getOpLeftInCod() {
		return opLeftInCod;
	}
	/**
	 *  peak与右端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内
	 * @return
	 */
	public double getOpRightInCod() {
		return opRightInCod;
	}
	/**
	 *  peak与左端Item交集时，实际交集的bp数
	 * @return
	 */
	public int getOpLeftBp() {
		return opLeftBp;
	}
	/**
	 *  peak与右端Item交集时，实际交集的bp数
	 * @return
	 */
	public int getOpRightBp() {
		return opRightBp;
	}
	
	/** 返回两个坐标中间夹着的的元素 */
//	private List<T> getLsAlignMid() {
//		return lsAlignMid;
//	}
	
	/**
	 * 返回与这个区段有交集的gffDetail信息<br>
	 * <b>考虑</b> element 与两个坐标点 overlap的情况
	 * @return
	 * 空的则返回一个size为0的list
	 */
	public List<T> getAllElement() {
		Set<T> lsGffDetailAll = new LinkedHashSet<T>();
		if (getSiteLeft() != null && getSiteLeft().isInsideLoc()) {
			if (getSiteLeft().isInsideUp())
				lsGffDetailAll.add(getSiteLeft().getAlignUp());
			lsGffDetailAll.add(getSiteLeft().getAlignThis());
		}
		if (lsAlignMid != null) {
			for (T t : lsAlignMid) {
				lsGffDetailAll.add(t);
			}
		}
		if (getSiteRight() != null && getSiteRight().isInsideLoc()) {
			lsGffDetailAll.add(getSiteRight().getAlignThis());
			if (getSiteRight().isInsideDown())
				lsGffDetailAll.add(getSiteRight().getAlignDown());
		}
		List<T> lsResult = new ArrayList<T>(lsGffDetailAll);
		return lsResult;
	}
	/**
	 * 返回被这两个坐标完全覆盖的element信息<br>
	 * <b>不考虑</b> element 与两个坐标点 overlap的情况
	 * @return
	 * 空的则返回一个size为0的list
	 */
	public List<T> getCoveredElement() {
		int start = getSiteLeft().getCoord();
		int end = getSiteRight().getCoord();
		int startAbs = Math.min(start, end);
		int endAbs = Math.max(start, end);
		Set<T> lsGffDetailAll = new LinkedHashSet<T>();
		if (getSiteLeft() != null && getSiteLeft().isInsideLoc()) {
			if (isCoverElement(startAbs, endAbs, getSiteLeft().getAlignUp())) {
				lsGffDetailAll.add(getSiteLeft().getAlignUp());
			}
			if (isCoverElement(startAbs, endAbs, getSiteLeft().getAlignThis())) {
				lsGffDetailAll.add(getSiteLeft().getAlignThis());
			}
			if (isCoverElement(startAbs, endAbs, getSiteLeft().getAlignDown())) {
				lsGffDetailAll.add(getSiteLeft().getAlignDown());
			}
		}
		if (lsAlignMid != null) {
			for (T t : lsAlignMid) {
				lsGffDetailAll.add(t);
			}
		}
		if (getSiteRight() != null && getSiteRight().isInsideLoc()) {
			if (isCoverElement(startAbs, endAbs, getSiteRight().getAlignUp())) {
				lsGffDetailAll.add(getSiteRight().getAlignUp());
			}
			if (isCoverElement(startAbs, endAbs, getSiteRight().getAlignThis())) {
				lsGffDetailAll.add(getSiteRight().getAlignThis());
			}
			if (isCoverElement(startAbs, endAbs, getSiteRight().getAlignDown())) {
				lsGffDetailAll.add(getSiteRight().getAlignDown());
			}
		}
		List<T> lsResult = new ArrayList<T>(lsGffDetailAll);
		Collections.sort(lsResult, new Alignment.ComparatorAlignment());
		return lsResult;
	}
	
	/** 起点和终点完全覆盖element */
	private boolean isCoverElement(int startAbs, int endAbs, T element) {
		if (element == null) {
			return false;
		}
		return element.getStartAbs() >= startAbs && element.getEndAbs() <= endAbs;
	}
	
	/**
	 * 双坐标查找 输入相关的GffHash类，然后填充相关信息<br>
	 */
	private void calInfo() {
		T gffDetail1 = siteLeft.getAlignThis();
		T gffDetail2 = siteRight.getAlignThis();
		int leftItemLength = 0; int leftoverlap = 0;
		int rightItemLength = 0; int rightoverlap = 0;
		int peakLength = siteRight.getCoord() - siteLeft.getCoord();
		if (gffDetail1 != null) {
			leftItemLength = gffDetail1.getEndAbs() - gffDetail1.getStartAbs();
			leftoverlap = gffDetail1.getEndAbs() - siteLeft.getCoord();
		}
		if (gffDetail2 != null) {
			rightItemLength = gffDetail2.getEndAbs() - gffDetail2.getStartAbs();
			rightoverlap = siteRight.getCoord() - gffDetail2.getStartAbs();
		}
		/**
		 * 如果peak两个端点都在在同一条目之内
		 */
		if (siteLeft.insideLOC && siteRight.insideLOC && 
				gffDetail1.equals(gffDetail2)
		)
		{
			opLeftInItem = 100 * (double) peakLength / leftItemLength;
			opLeftInCod = 100;
			opLeftBp = peakLength; opRightInItem = opLeftInItem; opRightInCod = 100; opRightBp = opLeftBp;
		}
		// 如果peak左端点在一个条目内，右端点在另一个条目内
		else if (siteLeft.insideLOC
				&& siteRight.insideLOC
				&& !siteLeft.equals(siteRight) )
		{
			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}
		// peak只有左端点在条目内
		else if (siteLeft.insideLOC && !siteRight.insideLOC) {
			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 0; opRightInCod = 0; opRightBp = 0;
		}
		// peak只有右端点在条目内
		else if (!siteLeft.insideLOC && siteRight.insideLOC) {
			opLeftInItem = 0; opLeftInCod = 0; opLeftBp = 0;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}else {
			opLeftInItem = 0; opLeftInCod = 0; opLeftBp = 0;
			opRightInItem = 0;
			opRightInCod = 0;
			opRightBp = 0;
		}
	}
	
}
