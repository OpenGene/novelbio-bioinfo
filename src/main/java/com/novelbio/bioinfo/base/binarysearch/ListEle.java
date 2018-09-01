package com.novelbio.bioinfo.base.binarysearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.index.Indexed;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.AlignExtend;
import com.novelbio.bioinfo.base.Alignment;
/**
 * 考虑将其拆分成为三个不同的list，一个cis，一个trans，一个null
 * @author zong0jie
 *
 * @param <E>
 */
public class ListEle <E extends AlignExtend> implements Cloneable, Iterable<E> {
	private static final long serialVersionUID = -3356076601369239937L;
	private static final Logger logger = LoggerFactory.getLogger(ListEle.class);

	/** 本条目的名字 */
	@Indexed
	protected String name;
	/** List的方向 */
	Boolean cis5to3 = null;
	
	protected ArrayList<E> lsElement = new ArrayList<>();
	
	/** 本list的名字，不需要转变为小写 */
	public void setName(String name) {
		this.name = name;
	}
	/** 具体的内容 */
	public List<E> getLsElement() {
		return lsElement;
	}
	public void trimToSize() {
		lsElement.trimToSize();
	}
	public int size() {
		return lsElement.size();
	}
	public E get(int index) {
		return lsElement.get(index);
	}
	public int indexOf(Object o) {
		return lsElement.indexOf(o);
	}
	
	public void clearElements() {
		lsElement.clear();
	}
	public String getName() {
		if (name == null) {
			if (size() > 0) {
				name = get(0).getRefID();
			} else {
				name = "";
			}
		}
		return name;
	}
	/**
	 * 没有方向则返回null
	 * @return
	 */
	public Boolean isCis5to3() {
		return cis5to3;
	}
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
 	}
	/**
	 * 返回实际第num个element间区的长度
	 * @param num 实际数目，必须小于sizeNumber
	 * @return
	 */
	public int getInterGenic(int num) {
		if (num < 0 || num > size() - 1) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (isCis5to3() == null) {
			return Math.abs(get(num + 1).getStartAbs() - get(num).getEndAbs());
		} else if (isCis5to3()) {
			return Math.abs(get(num + 1).getStartAbs() - get(num).getEndAbs());
		} else {
			return Math.abs(get(num).getStartAbs() - get(num+1).getEndAbs());
		}
	}
	/**
	 * 返回实际第num个element的长度
	 * @param num 实际数目
	 * @return
	 */
	public int getEleLen(int num) {
		return get(num-1).getLength();
	}
	
	public int getLen() {
		if (size() == 1) {
			return get(0).getLength();
		} else {
			if (get(0).getStartAbs() < get(1).getStartAbs()) {
				return get(size()-1).getEndAbs() - get(0).getStartAbs() + 1;
			} else {
				return get(0).getEndAbs() - get(size()-1).getStartAbs() + 1;
			}
		}
	}
	/**
	 * 在下游返回正数，上游返回负数
	 * @param loc
	 * @return
	 */
	protected int getLoc2Start(int loc) {
		if (isLocDownStart(loc)) {
			return Math.abs(loc - getStart());
		} else {
			return -Math.abs(loc - getStart());
		}
	}
	/**
	 * 在下游返回正数，上游返回负数
	 * @param loc
	 * @return
	 */
	protected int getLoc2End(int loc) {
		if (isLocDownEnd(loc)) {
			return Math.abs(loc - getEnd());
		} else {
			return -Math.abs(loc - getEnd());
		}
	}
	/**
	 * 输入的loc是否在本list的范围外
	 * @return
	 */
	protected boolean isLocInside(int loc) {
		if (loc >= Math.max(getStart(), getEnd()) || loc <= Math.min(getStart(), getEnd())) {
			return false;
		}
		return true;
	}
	/** 会将该element的parent设置为本list */
	public boolean add(E element) {
		element.setParent(this);
		return lsElement.add(element);
	}
	public void add(int index, E element) {
		element.setParent(this);
		lsElement.add(index, element);
	}
	
	public boolean addAll(Collection<E> colElement) {
		for (E element : colElement) {
			element.setParent(this);
		}
		return lsElement.addAll(colElement);
	}
	
	public boolean addAll(int index, Collection<E> colElement) {
		for (E element : colElement) {
			element.setParent(this);
		}
		return lsElement.addAll(index, colElement);
	}
	
	public E set(int index, E element) {
		return lsElement.set(index, element);
	}
	/**
	 * 输入的loc是否在Start的下游
	 * @return
	 */
	protected boolean isLocDownStart(int loc) {
		if (isCis5to3() && loc >= getStart()
		||
		!isCis5to3() && loc <= getStart()
		) {
			return true;
		}
		return false;
	}
	/**
	 * 输入的loc是否在Start的下游
	 * @return
	 */
	protected boolean isLocDownEnd(int loc) {
		if (isCis5to3() && loc >= getEnd()
		||
		!isCis5to3() && loc <= getEnd()
		) {
			return true;
		}
		return false;
	}
	
	public int getStart() {
		return isCis5to3() ? getStartAbs() : getEndAbs();
	}
	public int getEnd() {
		return isCis5to3() ? getEndAbs() : getStartAbs();
	}
	
	/** 根据方向返回 */
	public int getStartAbs() {
		if (isCis5to3() == null || isCis5to3()) {
			return get(0).getStartAbs();
		} else {
			return get(size() - 1).getStartAbs();
		}
	}
	/** 根据方向返回 */
	public int getEndAbs() {
		if (isCis5to3() == null || isCis5to3()) {
			return get(size() - 1).getEndAbs();
		}
		return get(0).getEndAbs();
	}
	
	/**
	 * 两个坐标之间的距离，仅仅计算他们在mRNA层面的距离，也就是只计算ele上的距离。
	 * 当两者重叠时，返回0
	 * 当loc1在loc2上游时，返回正数，当loc1在loc2下游时，返回负数
	 * 要求这两个坐标都在exon上.如果不符合，则返回GffCodAbs.LOC_ORIGINAL
	 * @param loc1 第一个坐标
	 * @param loc2 第二个坐标
	 */
	public int getLocDistmRNA(int loc1, int loc2) {
		int locNum1 = getNumCodInEle(loc1); int locNum2 = getNumCodInEle(loc2);
		if (locNum1 <= 0 || locNum2 <= 0) return Align.LOC_ORIGINAL;
		
		int locSmall = 0, locBig = 0;
		int locSmallExInNum = 0, locBigExInNum = 0;
		if (locNum1 < locNum2) {
			locSmall = loc1; locSmallExInNum = locNum1;
			locBig = loc2; locBigExInNum = locNum2;
		} else if (locNum1 > locNum2) {
			locSmall = loc2; locSmallExInNum = locNum2;
			locBig = loc1; locBigExInNum = locNum1;
		} else {
			E e = get(locNum1 - 1);
			if (e.isCis5to3() == null || e.isCis5to3()) {
				locSmall = loc1; locBig = loc2;
			} else {
				locSmall = loc2; locBig = loc1;
			}
			locSmallExInNum = locNum1;
			locBigExInNum = locNum1;
		}
		
		int distance = 0;
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locBig - locSmall;
		} else {
			distance = getCod2ExInEnd(locSmall) + getCod2ExInStart(locBig) + 1;
			for (int i = locSmallExInNum + 1; i <= locBigExInNum - 1; i++) {
				distance = distance + get(i).getLength();
			}
		}
		
		if ((isCis5to3() && loc1 < loc2) || (!isCis5to3() && loc1 > loc2)) {
			return Math.abs(distance);
		}
		return -Math.abs(distance);
	}
	/**
	 * <b>结果恒大于0</b><br>
	 * 必须首先设定ListAbs的方向，并且该方向和其内部的element的方向要一致
	 * 坐标到element 起点距离，如果重叠则为0
	 * @param location 坐标
	 */
	public int getCod2ExInStart(int location) {
		int loc2ExInStart = -1000000000;
		int exIntronNum = getNumCodInEle(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		E element = get(NumExon);
		if (exIntronNum > 0) {
			loc2ExInStart = Math.abs(element.getCod2Start(location));//距离本外显子起始 nnnnnnnnC
		} else if(exIntronNum < 0) {   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			loc2ExInStart = Math.abs(element.getCod2End(location)) - 1;// 距前一个外显子 NnnnCnnnn
		}
		return loc2ExInStart;
	}

	/**
	 * <b>结果恒大于0</b><br>
	 * 坐标到element 终点距离，当重叠时，为0
	 * @param location 坐标
	 */
	public int getCod2ExInEnd(int location) {
		int loc2ExInEnd = -1000000000;
		int exIntronNum = getNumCodInEle(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			E element = get(NumExon);
			loc2ExInEnd = Math.abs(element.getCod2End(location));//距离本外显子终止  Cnnnnnnn}
		}
		//0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
		else if(exIntronNum < 0) {
			E element = get(NumExon+1);
			loc2ExInEnd = Math.abs(element.getCod2Start(location)) - 1;
		}
		return loc2ExInEnd;
	}
	/**
	 * 获得所有element的长度之和
	 */
	public int getListLen() {
		int isoLen = 0;
		for (E exons : lsElement) {
			isoLen = isoLen + exons.getLength();
		}
		return isoLen;
	}

	/**
	 * 返回每个ID对应的具体element
	 * 输入一个hashmap，在里面填充信息
	 * key都是小写
	 * @return
	 */
	public LinkedHashMap<String, E> getMapName2DetailAbs() {
		LinkedHashMap<String, E> mapName2DetailAbs = new LinkedHashMap<String, E>();
		for (E ele : lsElement) {
			String name = ele.getName();
			mapName2DetailAbs.put(name.toLowerCase(), ele);
			mapName2DetailAbs.put(removeDot(name.toLowerCase()), ele);
		}
		return mapName2DetailAbs;
	}
	
	/** 从0开始计算 */
	public List<E> subList(int fromIndex, int toIndex) {
		return lsElement.subList(fromIndex, toIndex);
	}
	
	/**
	 *  首先除去空格，如果为""或“-”
	 *  则返回null
	 * 如果类似XM_002121.1类型，那么将.1去除
	 * @param accID
	 * @return accID without .1
	 */
	public static String removeDot(String accID) {
		if (accID == null) {
			return null;
		}
		String tmpGeneID = accID.replace("\"", "").trim();
		if (tmpGeneID.equals("") || accID.equals("-")) {
			return null;
		}
		int dotIndex = tmpGeneID.lastIndexOf(".");
		//如果类似XM_002121.1类型
		if (dotIndex>0 && tmpGeneID.length() - dotIndex <= 3) {
			String subIndex = tmpGeneID.substring(dotIndex + 1, tmpGeneID.length());
			try {
				tmpGeneID = tmpGeneID.substring(0,dotIndex);
			} catch (Exception e) { }
		}
		return tmpGeneID;
	}
	/**
	 * 返回本ListAbs中的所有string名字
	 * 如果两个Item是重叠的，取全部ID
	 * @return
	 */
	public List<String> getLsNameAll() {
		ArrayList<String> lsLocID = new ArrayList<String>();
		for (E ele : lsElement) {
			lsLocID.add(ele.getName());
		}
		return lsLocID;
	}
	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因<br>
	 * 3：该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected CoordLocationInfo LocPosition( int Coordinate) {
		return BinarySearch.searchCoord(lsElement, Coordinate, isCis5to3());
	}
	
	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目，从1开始计数
	 * @return
	 */
	public int getNumCodInEle(int location) {
		return LocPosition(location).getIndexSearch();
	}

	/**
	 * TO BE CHECKED
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 * LnnnnN为5位
	 */
	public int getLocDistmRNASite(int location, int mRNAnum) {
		if (getNumCodInEle(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getCod2ExInStart(location)) {
				return isCis5to3() ? location + mRNAnum : location + Math.abs(mRNAnum);
			}
			else {
				int exonNum = getNumCodInEle(location) - 1;
				int remain = Math.abs(mRNAnum) - getCod2ExInStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					E tmpExon = get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.getLength() > 0) {
						remain = remain - tmpExon.getLength();
						continue;
					}
					else {
						return isCis5to3() ? tmpExon.getEndCis() - remain + 1 : tmpExon.getEndCis() + remain - 1;
					}
				}
				return -1;
			}
		}
		else {
			if (mRNAnum <= getCod2ExInEnd(location)) {
				return isCis5to3() ? location + mRNAnum : location - mRNAnum;
			} 
			else {
				int exonNum = getNumCodInEle(location) - 1;
				int remain = mRNAnum - getCod2ExInEnd(location);
				for (int i = exonNum + 1; i < size(); i++) {
					E tmpExon = get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.getLength() > 0) {
						remain = remain - tmpExon.getLength();
						continue;
					}
					else {
						if (isCis5to3()) {
							return tmpExon.getStartCis() + remain - 1;
						}
						else {
							return tmpExon.getStartCis() - remain + 1;
						}
					}
				}
				return -1;
			}
		}
	}
	/**
	 * 依次比较两个list中的元素是否一致。内部调用每个元素的equals方法
	 * 不比较name，如果需要比较name，那么就用equal
	 * 暂时还没重写equal
	 * 外显子比较如果一模一样则返回true；
	 * @param lsOtherExon
	 * @return
	 */
	public boolean equalsIso(ListEle<E> lsOther) {
		if (lsOther.size() != size() ) {
			return false;
		}
		for (int i = 0; i < lsOther.size(); i++) {
			E otherT = lsOther.get(i);
			E thisT = get(i);
			if (!otherT.equals(thisT)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 将list中的元素进行排序，如果反向，那么就从大到小排序
	 * 如果正向，那么就从小到大排序
	 * 内部有flag，排完后就不会再排第二次了
	 */
	public void sort() {
		if (isCis5to3() == null) {
			Collections.sort(lsElement, new Alignment.CompS2MAbs());
		} else if (isCis5to3()) {
			Collections.sort(lsElement, new Alignment.CompS2M());
		} else {
			Collections.sort(lsElement, new Alignment.CompM2S());
		}
	}
	/**
	 * 已测试，能用
	 */
	@SuppressWarnings("unchecked")
	public ListEle<E> clone() {
		ListEle<E> result = null;
		try {
			result = (ListEle<E>) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.cis5to3 = isCis5to3();
		result.name = name;
		result.lsElement = new ArrayList<>(lsElement);
		return result;
	}

	public boolean isEmpty() {
		return lsElement.isEmpty();
	}

	public boolean contains(Object o) {
		return lsElement.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return lsElement.iterator();
	}

	public boolean remove(Object o) {
		return lsElement.remove(o);
	}
	
	public E remove(int index) {
		return lsElement.remove(index);
	}

	public int lastIndexOf(Object o) {
		return lsElement.lastIndexOf(o);
	}
}


