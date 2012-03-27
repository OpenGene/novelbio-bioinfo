package com.novelbio.base.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.novelbio.analysis.project.cdg.TmpScript;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;

public class JScrollPaneData extends JScrollPane{
	DefaultTableModel defaultTableModel = null;
	/**
	 * ��jScrollPane����ӱ�񣬵�һ��Ϊ��ͷ
	 */
	public void setProview( List<String[]> lsInfo)
	{
		String[][] tableValue = null;
		defaultTableModel = new DefaultTableModel(tableValue, lsInfo.get(0));
		JTable jTabFInputGo = new JTable();
		setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(defaultTableModel);
		for (int i = 1; i < lsInfo.size(); i++) {
			defaultTableModel.addRow(lsInfo.get(i));
		}
	}
	/**
	 * ��jScrollPane����ӱ�񣬵�һ��Ϊ��ͷ
	 */
	public void setTitle( String[] title)
	{
		String[][] tableValue = null;
		defaultTableModel = new DefaultTableModel(tableValue, title);
		JTable jTabFInputGo = new JTable();
		setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(defaultTableModel);
	}
	/**
	 * ��jScrollPane����ӱ�񣬵�һ��Ϊ��ͷ
	 */
	public void addProview( List<String[]> lsInfo)
	{
		if (defaultTableModel == null) {
			setProview(lsInfo);
			return;
		}
		for (String[] strings : lsInfo) {
			defaultTableModel.addRow(strings);
		}
	}
	/**
	 * ��jScrollPane����ӱ�񣬵�һ��Ϊ��ͷ
	 */
	public void addProview(String[] info)
	{
		defaultTableModel.addRow(info);
	}
	
	public ArrayList<String[]> getLsFileInfo()
	{
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
}
