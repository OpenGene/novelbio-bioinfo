package com.novelbio.base.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import com.novelbio.database.model.modcopeid.CopedID;

/**
 * JComboBox����չ���������
 * @author zong0jie
 *
 */
public class JComboBoxData extends JComboBox{
	/**
	 * ����key��value��map
	 */
	HashMap<String, ?> hashInfo = null;
	/**
	 * null������
	 * true������
	 * false������
	 */
	Boolean resultSort = null;
	/**
	 * null������
	 * true������
	 * false������
	 */
	public void setResultSort(Boolean resultSort) {
		this.resultSort = resultSort;
	}
	/**
	 * װ��hash��
	 * @param hashInfo
	 */
	public void setItemHash(HashMap<String, ?> hashInfo) {
		this.hashInfo = hashInfo;
		setCombBox();
	}
	
	private void setCombBox()
	{
		ArrayList<String> lsInfo = new ArrayList<String>();
		for (String string : hashInfo.keySet()) {
			if (string != null) {
				lsInfo.add(string);
			}
		}
		//����///////////////////
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
	 * ���򣬸��ݸ����ķ�ʽ������������߲�����
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
	
	public Object getSelectedValue() {
		String key = (String) getSelectedItem();
		if (hashInfo.get(key) == null) {
			return null;
		}
		else return hashInfo.get(key);
	}
}
