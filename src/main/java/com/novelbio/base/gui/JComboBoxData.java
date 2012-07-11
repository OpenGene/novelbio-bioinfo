package com.novelbio.base.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * JComboBox的扩展，方便操作<br>
 * T: 选择的Item所对应的类
 * @author zong0jie
 *
 */
public class JComboBoxData<T> extends JComboBox{
	private static final long serialVersionUID = -1651148386751801706L;
	/**
	 * 保存key和value的map
	 */
	HashMap<String, T> hashInfo = null;
	/**
	 * null不排序
	 * true：正序
	 * false：倒序
	 */
	Boolean resultSort = null;
	/**
	 * null不排序
	 * true：正序
	 * false：倒序
	 */
	public void setResultSort(Boolean resultSort) {
		this.resultSort = resultSort;
	}
	/**
	 * 装载hash表
	 * @param hashInfo
	 */
	public void setMapItem(HashMap<String, T> hashInfo) {
		this.hashInfo = hashInfo;
		setCombBox();
	}
	
	private void setCombBox() {
		ArrayList<String> lsInfo = new ArrayList<String>();
		for (String string : hashInfo.keySet()) {
			if (string != null) {
				lsInfo.add(string);
			}
		}
		//排序///////////////////
		sortList(lsInfo);
		/////////////////////////////////////////////////////
		String[] speciesarray = new String[lsInfo.size()];
		int i = 0;
		for(String string:lsInfo) {
			speciesarray[i] = string; i++;
		}
		ComboBoxModel jCobTaxSelectModel = new DefaultComboBoxModel(speciesarray);
		setModel(jCobTaxSelectModel);
	}
	/**
	 * 排序，根据给定的方式，作正序倒序或者不排序
	 * @param lsInfo
	 */
	private void sortList(ArrayList<String> lsInfo) {
		if (resultSort != null) {
			if (resultSort) {
				Collections.sort(lsInfo);
			}
			else {
				Collections.sort(lsInfo, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return -o1.compareTo(o2);
					}
				} );
			}
		}
	}
	
	public T getSelectedValue() {
		String key = (String) getSelectedItem();
		if (hashInfo.get(key) == null) {
			return null;
		}
		else return hashInfo.get(key);
	}
}
