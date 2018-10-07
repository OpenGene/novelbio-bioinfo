package com.novelbio.bioinfo.base;

import java.util.Comparator;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.StringOperate;

public interface Alignment {
	int getStartAbs();
	int getEndAbs();
	int getStartCis();
	int getEndCis();
	Boolean isCis5to3();
	int getLength();
	String getChrId();
	
	public static class ComparatorAlignment implements Comparator<Alignment> {
		@Override
		public int compare(Alignment o1, Alignment o2) {
			Integer o1start = o1.getStartAbs();
			Integer o2start = o2.getStartAbs();
			if (o1.isCis5to3() != null && o2.isCis5to3() != null && o1.isCis5to3() == o2.isCis5to3()) {
				if (o1.isCis5to3()) {
					return o1start.compareTo(o2start);
				} else {
					return -o1start.compareTo(o2start);
				}
			} else {
				return o1start.compareTo(o2start);
			}
		}
	}
	
	public static int getDistance(Alignment align1, Alignment align2) {
		if (align1.getStartAbs() > align2.getStartAbs()) {
			return align1.getStartAbs() - align2.getEndAbs();
		} else {
			return align2.getStartAbs() - align1.getEndAbs();
		}
	}
	
	/** 判断Align1是否cover Align2
	 * 注意不考虑染色体号
	 * @param align1
	 * @param align2
	 * @return
	 */
	public static boolean isAlignCoverAnother(Alignment align1, Alignment align2) {
		return align1.getStartAbs() <= align2.getStartAbs() && align1.getEndAbs() >= align2.getEndAbs();
	}
	
	/** 判断两个align是否overlap */
	public static boolean isOverlap(Alignment align1, Alignment align2) {
		return align1.getStartAbs() <= align2.getEndAbs() && align1.getEndAbs() >= align2.getStartAbs();
	}
	
	/** 判断两个align是否overlap */
	public static boolean isSiteInAlign(Alignment align, int site) {
		return site >= align.getStartAbs() && site <= align.getEndAbs();
	}

	public static int overlapLen(Alignment align1, Alignment align2) {
		validateRef(align1, align2);
		if (!isOverlap(align1, align2)) {
			return 0;
		}
		int refStart = align1.getStartAbs();
		int refEnd = align1.getEndAbs();
		
		int altStart = align2.getStartAbs();
		int altEnd = align2.getEndAbs();
		
		int len = Math.min(refEnd, altEnd) - Math.max(refStart, altStart);
		return len;
	}
	
	static void validateRef(Alignment align1, Alignment align2) {
		//这个报错应该不会出现
		if (!StringOperate.isEqual(align1.getChrId(), align2.getChrId())) {
			throw new RuntimeException("refId is differ " + align1.getChrId() + " " + align2.getChrId());
		}
	}
	
	public static boolean isEqual(Boolean bool1, Boolean bool2) {
		if (bool1 == null && bool2 == null) {
			return true;
		}
		if (bool1 == null || bool2 == null) {
			return false;
		}
		return bool1.equals(bool2);
	}
	
	/**
	 * 从小到大排序
	 * @author zong0jie
	 */
	public static class CompS2M implements Comparator<Alignment> {
		@Override
		public int compare(Alignment o1, Alignment o2) {
			Integer o1start = o1.getStartCis();
			Integer o2start = o2.getStartCis();
			int comp = o1start.compareTo(o2start);
			if (comp == 0) {
				Integer o1end = o1.getEndCis();
				Integer o2end = o2.getEndCis();
				return o1end.compareTo(o2end);
			}
			return comp;
		}
	}

	/**
	 * 从小到大排序，用绝对坐标值排序
	 * @author zong0jie
	 */
	public static class CompS2MAbs implements Comparator<Alignment> {
		@Override
		public int compare(Alignment o1, Alignment o2) {
			Integer o1start = o1.getStartAbs();
			Integer o2start = o2.getStartAbs();
			int comp = o1start.compareTo(o2start);
			if (comp == 0) {
				Integer o1end = o1.getEndAbs();
				Integer o2end = o2.getEndAbs();
				return o1end.compareTo(o2end);
			}
			return comp;
		}
	}

	/**
	 * 从大到小排序
	 * @author zong0jie
	 */
	public static class CompM2S implements Comparator<Alignment> {
		@Override
		public int compare(Alignment o1, Alignment o2) {
			Integer o1start = o1.getStartCis();
			Integer o2start = o2.getStartCis();
			int comp = -o1start.compareTo(o2start);
			if (comp == 0) {
				Integer o1end = o1.getEndCis();
				Integer o2end = o2.getEndCis();
				return -o1end.compareTo(o2end);
			}
			return comp;
		}
	}

	/**
	 * 从小到大排序
	 * @author zong0jie
	 */
	public static class CompS2MWithStrand implements Comparator<Alignment> {
		@Override
		public int compare(Alignment o1, Alignment o2) {
			if (!isBooleanEquals(o1.isCis5to3(), o2.isCis5to3())) {
				throw new RuntimeException();
			}
			Integer o1start = o1.getStartCis();
			Integer o2start = o2.getStartCis();
			int comp = o1start.compareTo(o2start);
			if (comp == 0) {
				Integer o1end = o1.getEndCis();
				Integer o2end = o2.getEndCis();
				comp = o1end.compareTo(o2end);
			}
			if (o1.isCis5to3() != null && !o1.isCis5to3()) {
				comp = -comp;
			}
			return comp;
		}
		
		private boolean isBooleanEquals(Boolean boolean1, Boolean boolean2) {
			if (boolean1 == null && boolean2 == null) {
				return true;
			}
			if (boolean1 == null || boolean2 == null) {
				return false;
			}
			return boolean1.equals(boolean2);
		}
		
	}
	
}
