package com.novelbio.bioinfo.base.binarysearch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.bioinfo.base.Alignment;

public class BinarySearch<T extends Alignment> {
	private static final Logger logger = LoggerFactory.getLogger(BinarySearch.class);
	
	List<T> lsElement;
	Boolean isCis5To3;
	
	/**
	 * @param lsElement 必须排好序
	 * @param isCis5To3 lsElemet是从小到大还是从大到小排
	 */
	public BinarySearch(List<T> lsElement, Boolean isCis5To3) {
		this.lsElement = lsElement;
		this.isCis5To3 = isCis5To3;
	}
	/**
	 * @param lsElement 必须排好序
	 * 自动判定lsElement的排序
	 */
	public BinarySearch(List<T> lsElement) {
		if (lsElement.isEmpty()) {
			throw new RuntimeException("input should not be empty so that the module can judge the strand");
		}
		Boolean isCis5to3 = lsElement.get(0).isCis5to3();
		for (int i = 1; i < 5; i++) {
			if (i >= lsElement.size()) {
				break;
			}
			if (!isEqual(lsElement.get(i-1).isCis5to3(), isCis5to3)) {
				throw new RuntimeException("BinarySearch error, elements in list is not same strand");
			}
			if (isCis5to3 == null || isCis5to3 ) {
				if (lsElement.get(i-1).getStartAbs() >= lsElement.get(i).getStartAbs() ) {
					throw new RuntimeException("BinarySearch error, elements in list is not same strand");
				}
			} else {
				if (lsElement.get(i-1).getEndAbs() <= lsElement.get(i).getEndAbs() ) {
					throw new RuntimeException("BinarySearch error, elements in list is not same strand");
				}
			}
		}
		this.lsElement = lsElement;
		this.isCis5To3 = isCis5to3;
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
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 */
	public BsearchSite<T> searchLocation(int Coordinate) {
		CoordLocationInfo coordLocationInfo = LocPosition(Coordinate);
		if (coordLocationInfo == null) {
			return null;
		}
		BsearchSite<T> gffCod = new BsearchSite<>(Coordinate);
		if (coordLocationInfo.isInsideElement()) {
			gffCod.setAlignThis( lsElement.get(coordLocationInfo.getElementNumThisElementFrom0() ) ); 
			gffCod.booFindCod = true;
			gffCod.indexAlignThis = coordLocationInfo.getElementNumThisElementFrom0();
			gffCod.insideLOC = true;
		}
		if (coordLocationInfo.getElementNumLastElementFrom0() >= 0) {
			gffCod.setAlignUp( lsElement.get(coordLocationInfo.getElementNumLastElementFrom0()) );
			gffCod.indexAlignUp = coordLocationInfo.getElementNumLastElementFrom0();
		}
		if (coordLocationInfo.getElementNumNextElementFrom0() >= 0) {
			gffCod.setAlignDown(lsElement.get(coordLocationInfo.getElementNumNextElementFrom0()));
			gffCod.indexAlignDown = coordLocationInfo.getElementNumNextElementFrom0();
		}
		return gffCod;
	}
	
	/**
	 * 返回双坐标查询的结果，内部自动判断 cod1 和 cod2的大小
	 * 如果cod1 和cod2 有一个小于0，那么坐标不存在，则返回null
	 * @param chrID 内部自动小写
	 * @param cod1 必须大于0
	 * @param cod2 必须大于0
	 * @return
	 */
	public BsearchSiteDu<T> searchLocationDu(int cod1, int cod2) {
		if (cod1 < 0 && cod2 < 0) return null;
		
		int codStart = cod1;
		int codEnd = cod2;
		if (isCis5To3 != null && !isCis5To3) {
			codStart = Math.max(cod1, cod2);
			codEnd = Math.min(cod1, cod2);
		}
		

		BsearchSite<T> site1 = searchLocation(codStart);
		BsearchSite<T> site2 = searchLocation(codEnd);
		if (site1 == null) {
			logger.error("error");
		}
		BsearchSiteDu<T> lsAbsDu = new BsearchSiteDu<>(site1, site2); 
		
		if (lsAbsDu.getSiteLeft().getItemNumDown() >= 0) {
			for (int i = lsAbsDu.getSiteLeft().getItemNumDown(); i <= lsAbsDu.getSiteRight().getItemNumUp(); i++) {
				lsAbsDu.lsAlignMid.add(lsElement.get(i));
			}
		}
		return lsAbsDu;
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
		if (isCis5To3 == null) {
			return BinarySearch.LocPositionAbs(lsElement, Coordinate);
		} else if (isCis5To3) {
			return BinarySearch.LocPositionCis(lsElement, Coordinate);
		} else {
			return BinarySearch.LocPositionTran(lsElement, Coordinate);
		}
	}
	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static CoordLocationInfo LocPositionCis(List<? extends Alignment> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < lsElement.get(beginnum).getStartAbs()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate >= lsElement.get(endnum).getStartAbs()) {
			if (Coordinate > lsElement.get(endnum).getEndAbs()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			}
			else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartAbs()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsElement.get(number).getStartAbs()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate <= lsElement.get(beginnum).getEndAbs())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		else if (Coordinate >= lsElement.get(endnum).getStartAbs())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			coordLocationInfo.setElementInsideOutSideNum(endnum + 1);
			return coordLocationInfo;
		}
		// location在基因外部
		coordLocationInfo.setElementInsideOutSideNum(-beginnum - 1);
		return coordLocationInfo;
	}

	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static CoordLocationInfo LocPositionTran(List<? extends Alignment> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate > lsElement.get(beginnum).getEndAbs()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate <= lsElement.get(endnum).getEndAbs()) {
			if (Coordinate < lsElement.get(endnum).getStartAbs()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			} else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getEndAbs()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate > lsElement.get(number).getEndAbs() && number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate >= lsElement.get(beginnum).getStartAbs()) { // location在基因内部
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		else if (Coordinate <= lsElement.get(endnum).getEndAbs()) 
		{// location在基因内部
			coordLocationInfo.setElementInsideOutSideNum(endnum + 1);
			return coordLocationInfo;
		}
		// location在基因外部
		coordLocationInfo.setElementInsideOutSideNum(-beginnum - 1);
		return coordLocationInfo;
	}
	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static CoordLocationInfo LocPositionAbs(List<? extends Alignment> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < lsElement.get(beginnum).getStartAbs()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate >= lsElement.get(endnum).getStartAbs()) {
			if (Coordinate > lsElement.get(endnum).getEndAbs()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			}
			else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartAbs()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsElement.get(number).getStartAbs()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate <= lsElement.get(beginnum).getEndAbs()) {
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		coordLocationInfo.setElementInsideOutSideNum(-beginnum-1);
		return coordLocationInfo;
	}
	

}
