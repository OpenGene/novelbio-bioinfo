package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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
		int[] locInfo = LocPosition(Coordinate);// ���ַ�����peaknum�Ķ�λ
		if (locInfo == null) {
			return null;
		}
		T gffCod = creatGffCod(listName, Coordinate);
		if (locInfo[0] == 1) // ��λ�ڻ�����
		{
			gffCod.setGffDetailThis( get(locInfo[1]) ); 
			gffCod.booFindCod = true;
			gffCod.ChrHashListNumThis = locInfo[1];
			gffCod.insideLOC = true;
			if (locInfo[1] - 1 >= 0) {
				gffCod.setGffDetailUp( get(locInfo[1]-1) );
				gffCod.ChrHashListNumUp = locInfo[1]-1;
				
			}
			if (locInfo[2] != -1) {
				gffCod.setGffDetailDown(get(locInfo[2]));
				gffCod.ChrHashListNumDown = locInfo[2];
			}
		} else if (locInfo[0] == 2) {
			gffCod.insideLOC = false;
			if (locInfo[1] >= 0) {
				gffCod.setGffDetailUp( get(locInfo[1]) );
				gffCod.ChrHashListNumUp = locInfo[1];		
			}
			if (locInfo[2] != -1) {
				gffCod.setGffDetailDown( get(locInfo[2]) );
				gffCod.ChrHashListNumDown = locInfo[2];
			}
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
