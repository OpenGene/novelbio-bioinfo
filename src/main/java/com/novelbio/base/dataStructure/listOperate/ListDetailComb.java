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
public class ListDetailComb<T extends ListDetailAbs> extends ListDetailAbs {
	public ListDetailComb() {
		super("", "", null);
	}
	/**
	 * ������--int[2]:
	 * 0: ��ele���ڵ����
	 * 1: ��ele���ڵ��յ㡣���1<0����ʾ��ele�������ģ���ô0��ʾ����ǰһ��ele��λ��
	 */
	HashMap<String, int[]> hashList2Num = new HashMap<String, int[]>();
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
	protected void addLsElement(String lsName, ArrayList<T> element, int numStart, int numEnd) {
		hashList2Num.put(lsName, new int[]{numStart, numEnd});
		if (element == null || element.size() == 0) {
			return;
		}
		lsElement.add(element);
		/// ������Բ����� ////////////////
		for (T t : element) {
			this.parentName = t.getParentName();
			break;
		}
		//����Ƚϵ�element�������෴��cis����ô���趨Ϊnull
		for (T t : element) {
			if (isCis5to3() == null) {
				setCis5to3(t.isCis5to3());
			}
			else if (t.isCis5to3() != null || t.isCis5to3() != isCis5to3()) {
				setCis5to3(null);
				break;
			}
		}
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
			Collections.sort(lsSortEle, new CompS2MAbs());
		}
		if ( isCis5to3()) {
			Collections.sort(lsSortEle, new CompS2M());
		}
		else {
			Collections.sort(lsSortEle, new CompM2S());
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
	public int getStartCis() {
		sort();
		return lsSortEle.get(0).getStartCis();
	}
	@Override
	public int getStartAbs() {
		sort();
		return Math.min(lsSortEle.get(0).getStartAbs(), lsSortEle.get(lsSortEle.size() - 1).getStartAbs());
	}
	/**
	 * ����֤
	 */
	@Override
	public int getEndCis() {
		sort();
		return lsSortEle.get(lsSortEle.size() - 1).getEndCis();
	}
	/**
	 * ����֤
	 */
	@Override
	public int getEndAbs() {
		sort();
		return Math.min(lsSortEle.get(0).getEndAbs(), lsSortEle.get(lsSortEle.size() - 1).getEndAbs());
	}
	
	@Override
	public int Length() {
		return Math.abs(getStartAbs()- getEndAbs());
	}

	@Override
	public ArrayList<String> getName() {
		ArrayList<String> lsName = lsElement.get(0).get(0).getName();
		for (int i = 1; i < lsElement.size(); i++) {
			lsName.addAll(lsElement.get(i).get(0).getName());
		}
		return lsName;
	}
	/** �ٶ�����ת¼������Դһ�� */
	@Override
	public String getParentName() {
		String name = lsElement.get(0).get(0).getParentName();
		return name;
	}
	@Override
	public String getNameSingle() {
		return lsElement.get(0).get(0).getName().get(0);
	}
	/**
	 * ����ļ���exon�ǲ���һ����
	 * @return
	 */
	public boolean isSameEle() {
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
