package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * �ڽ����򷽷�
 * ������ListAbs�ϲ�����ÿ��Ԫ�ص���Ϣ
 * �ϲ�����Ϊ��������listAbs����һ��Ȼ������
 * �������element�н������ͽ�������element����һ����Ϊһ��element
 * ���ֻ�е���һ��element���ͷŸ�element
 * ���Բ����пյ�ElementComb
 * @author zong0jie
 *
 * @param <T>
 */
public class ElementCombDouble<T extends ElementAbsDouble> implements ElementAbsDouble {
	/**
	 * ������--int[2]:
	 * 0: ��ele���ڵ����
	 * 1: ��ele���ڵ��յ㡣���1<0����ʾ��ele�������ģ���ô0��ʾ����ǰһ��ele��λ��
	 */
	HashMap<String, double[]> hashList2Num = new HashMap<String, double[]>();
	/**
	 * ����ÿ��listabs�е�element
	 */
	ArrayList<ArrayList<T>> lsElement = new ArrayList<ArrayList<T>>();
	ArrayList<T> lsSortEle = new ArrayList<T>();
	String SEP = "\\";
	boolean sorted = false;
	/**
	 * ����һ��exon��list���Լ���list��ռԭʼlist�����λ���յ�λ��element
	 * Ʃ�磬������һ��list--����һ��element������2λ���յ��2λ
	 * ������һ����list����û��list������0��element������2λ���յ��-2λ--���յ�Ϊ����ʱ��ʾ��element��������
	 * @param element
	 * @param numStart
	 * @param numEnd
	 */
	protected void addLsElement(String lsName, ArrayList<T> element, double numStart, double numEnd) {
		hashList2Num.put(lsName, new double[]{numStart, numEnd});
		if (element == null || element.size() == 0) {
			return;
		}
		lsElement.add(element);
		///// ������Բ����� ////////////////
//		for (T t : element) {
//			t.getParentName()
//		}
		//////////////////////////////////////////////
	}
	protected void sort() {
		if (sorted) {
			return;
		}
		for (ArrayList<T> lsele : lsElement) {
			for (T t : lsele) {
				lsSortEle.add(t);
			}
		}
		/**
		 * ��list�е�Ԫ�ؽ����������element���� start > end����ô�ʹӴ�С����
		 * ���element����start < end����ô�ʹ�С��������
		 */
		if ( isCis5to3() == null) {
			Collections.sort(lsSortEle, new CompS2MAbsDouble());
		}
		if ( isCis5to3()) {
			Collections.sort(lsSortEle, new CompS2MDouble());
		}
		else {
			Collections.sort(lsSortEle, new CompM2SDouble());
		}
		sorted = true;
	}
	@Override
	public Boolean isCis5to3() {
		return lsSortEle.get(0).isCis5to3();
	}
	/**
	 * ����֤
	 */
	@Override
	public double getStartCis() {
		sort();
		return lsSortEle.get(0).getStartCis();
	}
	@Override
	public double getStartAbs() {
		sort();
		return Math.min(lsSortEle.get(0).getStartAbs(), lsSortEle.get(lsSortEle.size() - 1).getStartAbs());
	}
	/**
	 * ����֤
	 */
	@Override
	public double getEndCis() {
		sort();
		return lsSortEle.get(lsSortEle.size() - 1).getEndCis();
	}
	/**
	 * ����֤
	 */
	@Override
	public double getEndAbs() {
		sort();
		return Math.min(lsSortEle.get(0).getEndAbs(), lsSortEle.get(lsSortEle.size() - 1).getEndAbs());
	}
	
	@Override
	public double Length() {
		return Math.abs(getStartAbs()- getEndAbs());
	}

	@Override
	public String getName() {
		String name = lsElement.get(0).get(0).getName();
		for (int i = 1; i < lsElement.size(); i++) {
			name = name + SEP + lsElement.get(i).get(0).getName();
		}
		return name;
	}
	@Override
	public String getRefID() {
		String name = lsElement.get(0).get(0).getName();
		for (int i = 1; i < lsElement.size(); i++) {
			name = name + SEP + lsElement.get(i).get(0).getName();
		}
		return name;
	}
	/**
	 * ����ļ���exon�ǲ���һ����
	 * @return
	 */
	public boolean isSameEle()
	{
		if (lsElement.size() != hashList2Num.size()) {
			return false;
		}
		if (lsElement.get(0).size() > 1) {
			return false;
		}
		ArrayList<T> ele = lsElement.get(0);
		HashSet<String> hashEle = new HashSet<String>();
		hashEle.add(ele.get(0).getStartAbs() + "_ " +ele.get(0).getEndAbs());
		for (int i = 1; i < lsElement.size(); i++) {
			if (lsElement.get(i).size() > 1) {
				return false;
			}
			else if (!hashEle.contains(lsElement.get(i).get(0).getStartAbs() + "_ " +lsElement.get(i).get(0).getEndAbs())) {
				return false;
			}
		}
		return true;
	}
}
