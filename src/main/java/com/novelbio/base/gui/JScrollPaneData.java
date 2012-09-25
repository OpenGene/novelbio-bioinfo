package com.novelbio.base.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.novelbio.base.dataStructure.MathComput;
/**
 * JScrollPane����չ��������Ӻ�ɾ����
 * @author zong0jie
 *
 */
public class JScrollPaneData extends JScrollPane{
	private static final long serialVersionUID = -4238706503361283499L;
	
	DefaultTableModel defaultTableModel = null;
	JTable jTabFInputGo = null;
	String[] title;
	/**
	 * ��jScrollPane����ӱ�񣬵�һ��Ϊtitle
	 */
	public void setItemLs( List<String[]> lsInfo) {
		String[][] tableValue = null;
		title = lsInfo.get(0);
		defaultTableModel = new DefaultTableModel(tableValue, title);
		jTabFInputGo = new JTable();
		setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(defaultTableModel);
		for (int i = 1; i < lsInfo.size(); i++) {
			defaultTableModel.addRow(lsInfo.get(i));
		}
	}
	/**
	 * ��jScrollPane����ӱ�񣬵�һ��Ϊ��ͷ
	 */
	public void setTitle( String[] title) {
		String[][] tableValue = null;
		this.title = title;
		defaultTableModel = new DefaultTableModel(tableValue, title);
		jTabFInputGo = new JTable();
		setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(defaultTableModel);
	}
	/**
	 * ��jScrollPane����ӱ�����û��title�����һ��Ϊtitle
	 */
	public void addItemLs( List<String[]> lsInfo) {
		if (defaultTableModel == null) {
			setItemLs(lsInfo);
			return;
		}
		for (String[] strings : lsInfo) {
			defaultTableModel.addRow(strings);
		}
	}
	/**
	 * ��jScrollPane����ӱ�����û��title�����һ��Ϊtitle
	 */
	public void addItemLsSingle( List<String> lsInfo) {
		int colNum = 1;
		if (defaultTableModel != null) {
			colNum = defaultTableModel.getColumnCount();
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsInfo) {
			String[] tmpResult = new String[colNum];
			tmpResult[0] = string;
			lsResult.add(tmpResult);
		}
		addItemLs(lsResult);
	}
	/**
	 * ��jScrollPane����ӱ��
	 */
	public void addItem(String[] info) {
		if (defaultTableModel == null) {
			String[][] tableValue = null;
			defaultTableModel = new DefaultTableModel(tableValue, info);
			jTabFInputGo = new JTable();
			setViewportView(jTabFInputGo);
			jTabFInputGo.setModel(defaultTableModel);
			return;
		}
		
		defaultTableModel.addRow(info);
	}
	
	public ArrayList<String[]> getLsDataInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
			String[] tmpResult = new String[defaultTableModel.getColumnCount()];
			for (int j = 0; j < defaultTableModel.getColumnCount(); j++) {
				tmpResult[j] = defaultTableModel.getValueAt(i, j).toString();
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	/**
	 * ɾ��ʵ����
	 * @param rowNum
	 */
	public void removeRow(int... rowNum) {
		MathComput.sort(rowNum, false);
		for (int i : rowNum) {
			if (i < 0 || i > defaultTableModel.getRowCount()) {
				continue;
			}
			defaultTableModel.removeRow(i - 1);
//			defaultTableModel.setRowCount(i);// ɾ���бȽϼ򵥣�ֻҪ��DefaultTableModel��removeRow()�������ɡ�ɾ��
//			// ����Ϻ������������������Ҳ����ʹ��DefaultTableModel��setRowCount()���������á�
		}
	}
	/**
	 * ��þ�������
	 * @return
	 */
	public int[] getSelectRows() {
		int selectRows=jTabFInputGo.getSelectedRows().length;// ȡ���û���ѡ�е�����
		//����
		if(selectRows==1) {
			int selectedRowIndex = jTabFInputGo.getSelectedRow(); // ȡ���û���ѡ����
			return new int[]{selectedRowIndex + 1};
		}
		int[] selRowIndexs = null;
		if(selectRows>1) {
			selRowIndexs =jTabFInputGo.getSelectedRows();// �û���ѡ�е�����
			for (int i = 0; i < selRowIndexs.length; i++) {
				selRowIndexs[i] = selRowIndexs[i] + 1;
			}
			return selRowIndexs;
		}
		return null;
	}
	/**
	 * ɾ���û�ѡ������
	 */
	public void deleteSelRows() {
		removeRow(getSelectRows());
	}
	/** ���ȶ� */
	public void clean() {
		String[][] tableValue = null;
		defaultTableModel = new DefaultTableModel(tableValue, title);
		jTabFInputGo = new JTable();
		setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(defaultTableModel);
	}
}

