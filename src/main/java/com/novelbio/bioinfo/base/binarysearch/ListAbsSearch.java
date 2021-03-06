package com.novelbio.bioinfo.base.binarysearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public abstract class ListAbsSearch <E extends ListDetailAbs, T extends ListCodAbs<E>, K extends ListCodAbsDu<E, T>> extends ListAbs<E>  implements Cloneable{
	private static Logger logger = LoggerFactory.getLogger(ListAbsSearch.class);
	private static final long serialVersionUID = 4583552188474447935L;

	/**
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 */
	public T searchLocation(int Coordinate) {
		CoordLocationInfo coordLocationInfo = LocPosition(Coordinate);
		if (coordLocationInfo == null) {
			return null;
		}
		String refID = "";
		if (listName == null) {
			refID = getName();
		}
		T gffCod = creatGffCod(refID, Coordinate);
		if (coordLocationInfo.isInsideElement()) {
			gffCod.setGffDetailThis( get(coordLocationInfo.getIndexEleThis() ) ); 
			gffCod.booFindCod = true;
			gffCod.ChrHashListNumThis = coordLocationInfo.getIndexEleThis();
			gffCod.insideLOC = true;
		}
		if (coordLocationInfo.getIndexEleLast() >= 0) {
			gffCod.setGffDetailUp( get(coordLocationInfo.getIndexEleLast()) );
			gffCod.ChrHashListNumUp = coordLocationInfo.getIndexEleLast();
		}
		if (coordLocationInfo.getIndexEleNext() >= 0) {
			gffCod.setGffDetailDown(get(coordLocationInfo.getIndexEleNext()));
			gffCod.ChrHashListNumDown = coordLocationInfo.getIndexEleNext();
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
	public K searchLocationDu(int cod1, int cod2) {
		if (cod1 < 0 && cod2 < 0) return null;
		
		int codStart = cod1;
		int codEnd = cod2;
		if (cis5to3 != null && !cis5to3) {
			codStart = Math.max(cod1, cod2);
			codEnd = Math.min(cod1, cod2);
		}
		

		T gffCod1 = searchLocation(codStart);
		T gffCod2 = searchLocation(codEnd);
		if (gffCod1 == null) {
			logger.error("error");
		}
		K lsAbsDu = creatGffCodDu(gffCod1, gffCod2);
		
		if (lsAbsDu.getGffCod1().getItemNumDown() >= 0) {
			for (int i = lsAbsDu.getGffCod1().getItemNumDown(); i <= lsAbsDu.getGffCod2().getItemNumUp(); i++) {
				lsAbsDu.lsgffDetailsMid.add(get(i));
			}
		}
		return lsAbsDu;
	}
	/**
	 * 生成一个全新的GffCod类
	 * @param listName
	 * @param Coordinate
	 * @return
	 */
	protected abstract T creatGffCod(String listName, int Coordinate);
	/**
	 * 生成一个全新的GffCod类
	 * @param listName
	 * @param coord
	 * @return
	 */
	protected abstract K creatGffCodDu(T gffCod1, T gffCod2);
	/**
	 * 已测试，能用
	 */
	@SuppressWarnings("unchecked")
	public ListAbsSearch<E, T, K> clone() {
		ListAbsSearch<E, T, K> result = null;
		result = (ListAbsSearch<E, T, K>) super.clone();
		return result;
	}
}
