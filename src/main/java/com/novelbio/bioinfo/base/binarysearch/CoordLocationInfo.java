package com.novelbio.bioinfo.base.binarysearch;

/**
 * 前提，第一个element的起点就是list的起点，最后一个element的终点就是list的终点
 * 否则就要<b>重写getElementNumThisAbs() 方法</b>
 * 
 * 二分法查找location所在的位点所保存的信息
 * 返回一个int[3]数组，<br>
 * 0: 1-基因内 2-基因外<br>
 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
 * 2：下个基因的序号 -1表示后面没有基因
 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
 * 不在为0
 * 从0开始的数目，可以直接用get(i)提取
 */
public class CoordLocationInfo {
	/**待查找的list的元素个数 */
	int listSize;
	/** 表示该点在第几个元素中<br>
	 * 正数表示在第几个元素中，譬如在第几个exon中或第几个基因中，实际数目<br>
	 * 负数表示在第几个intron中或第几个间隔中，实际数目。
	 * 如果在list最前面，则为0。如果在list最后面，则为负数的list.size()
	 */
	int elementInsideOutSideNumAbs = 0;
	
	public CoordLocationInfo(int listSize) {
		this.listSize = listSize;
	}
	/** 表示该点在第几个元素中，<b>实际数目</b><br>
	 * 正数表示在第几个元素中，譬如在第几个exon中或第几个基因中，实际数目<br>
	 * 负数表示在第几个intron中或第几个间隔中。
	 * 如果在list最前面，则为0。如果在list最后面，则为负数的list.size()
	 */
	public void setElementInsideOutSideNum(int elementInsideOutSideNumAbs) {
		this.elementInsideOutSideNumAbs = elementInsideOutSideNumAbs;
	}
	
	public boolean isInsideElement() {
		if (elementInsideOutSideNumAbs > 0) {
			return true;
		}
		return false;
	}
	/**
	 * 返回该点上一个元素的序号，一直返回正数。如果在list外，返回-1<br>
	 * 计数从0开始<br>
	 * <b>-1表示前面没有基因</b>
	 * @return
	 */
	public int getElementNumLastElementFrom0() {
		if (elementInsideOutSideNumAbs > 0) {
			return elementInsideOutSideNumAbs - 2;
		}
		else if (elementInsideOutSideNumAbs < 0) {
			return Math.abs(elementInsideOutSideNumAbs) - 1;
		}
		else {
			return -1;
		}
	}
	/**
	 * 返回该点所在的Element，一直返回正数。如果在element之外，返回-1
	 * 计数从0开始
	 * @return
	 */
	public int getElementNumThisElementFrom0() {
		if (elementInsideOutSideNumAbs > 0) {
			return elementInsideOutSideNumAbs - 1;
		}
		else {
			return -1;
		}
	}
	/**
	 * 返回该点下一个元素的序号，一直返回正数。如果在list外，返回-1<br>
	 * 计数从0开始<br>
	 * <b>-1表示后面没有基因</b>
	 * @return
	 */
	public int getElementNumNextElementFrom0() {
		if (elementInsideOutSideNumAbs >= 0 && elementInsideOutSideNumAbs < listSize) {
			return elementInsideOutSideNumAbs;
		}
		else if (elementInsideOutSideNumAbs < 0 && Math.abs(elementInsideOutSideNumAbs) < listSize) {
			return Math.abs(elementInsideOutSideNumAbs);
		}
		else {
			return -1;
		}
	}
	/**
	 * 前提，第一个element的起点就是list的起点，最后一个element的终点就是list的终点<br>
	 * 返回该点所在的元素，从1开始，<br>
	 * 正数表示在第几个元素中，譬如在第几个exon中或第几个基因中，实际数目<br>
	 * 负数表示在第几个intron中或第几个间隔中。<br>
	 * 如果<b>在list最前面或最后面，则为0</b>。
	 */
	public int getElementNumThisAbs() {
		if (elementInsideOutSideNumAbs == -listSize) {
			return 0;
		}
		return elementInsideOutSideNumAbs;
	}
}
