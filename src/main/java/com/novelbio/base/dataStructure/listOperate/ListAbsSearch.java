package com.novelbio.base.dataStructure.listOperate;

import org.apache.log4j.Logger;

public abstract class ListAbsSearch <E extends ListDetailAbs, T extends ListCodAbs<E>, K extends ListCodAbsDu<E, T>> extends ListAbs<E>  implements Cloneable{
	private static Logger logger = Logger.getLogger(ListAbsSearch.class);
	private static final long serialVersionUID = 4583552188474447935L;

	/**
	 * ��õ�ÿһ����Ϣ����ʵ�ʵĶ�û��clone
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * ����clone�ķ��������Ϣ
	 * û�ҵ��ͷ���null
	 */
	public T searchLocation(int Coordinate) {
		CoordLocationInfo coordLocationInfo = LocPosition(Coordinate);
		if (coordLocationInfo == null) {
			return null;
		}
		T gffCod = creatGffCod(listName, Coordinate);
		if (coordLocationInfo.isInsideElement()) {
			gffCod.setGffDetailThis( get(coordLocationInfo.getElementNumThisElementFrom0() ) ); 
			gffCod.booFindCod = true;
			gffCod.ChrHashListNumThis = coordLocationInfo.getElementNumThisElementFrom0();
			gffCod.insideLOC = true;
		}
		if (coordLocationInfo.getElementNumLastElementFrom0() >= 0) {
			gffCod.setGffDetailUp( get(coordLocationInfo.getElementNumLastElementFrom0()) );
			gffCod.ChrHashListNumUp = coordLocationInfo.getElementNumLastElementFrom0();
		}
		if (coordLocationInfo.getElementNumNextElementFrom0() >= 0) {
			gffCod.setGffDetailDown(get(coordLocationInfo.getElementNumNextElementFrom0()));
			gffCod.ChrHashListNumDown = coordLocationInfo.getElementNumNextElementFrom0();
		}
		return gffCod;
	}
	
	/**
	 * ����˫�����ѯ�Ľ�����ڲ��Զ��ж� cod1 �� cod2�Ĵ�С
	 * ���cod1 ��cod2 ��һ��С��0����ô���겻���ڣ��򷵻�null
	 * @param chrID �ڲ��Զ�Сд
	 * @param cod1 �������0
	 * @param cod2 �������0
	 * @return
	 */
	public K searchLocationDu(int cod1, int cod2) {
		if (cod1 < 0 && cod2 < 0) {
			return null;
		}
		T gffCod1 = searchLocation(cod1);
		T gffCod2 = searchLocation(cod2);
		if (gffCod1 == null) {
			System.out.println("error");
		}
		K lsAbsDu = creatGffCodDu(gffCod1, gffCod2);
		
		if (lsAbsDu.getGffCod1().getItemNumDown() >= 0) {
			for (int i = lsAbsDu.getGffCod1().getItemNumDown(); i <= lsAbsDu.getGffCod2().getItemNumUp(); i++) {
				lsAbsDu.getLsGffDetailMid().add(get(i));
			}
		}
		return lsAbsDu;
	}
	/**
	 * ����һ��ȫ�µ�GffCod��
	 * @param listName
	 * @param Coordinate
	 * @return
	 */
	protected abstract T creatGffCod(String listName, int Coordinate);
	/**
	 * ����һ��ȫ�µ�GffCod��
	 * @param listName
	 * @param Coordinate
	 * @return
	 */
	protected abstract K creatGffCodDu(T gffCod1, T gffCod2);
	/**
	 * �Ѳ��ԣ�����
	 */
	@SuppressWarnings("unchecked")
	public ListAbsSearch<E, T, K> clone() {
		ListAbsSearch<E, T, K> result = null;
		result = (ListAbsSearch<E, T, K>) super.clone();
		return result;
	}
}
