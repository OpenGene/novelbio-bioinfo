package com.novelbio.base.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.novelbio.base.dataStructure.MathComput;
/**
 * JScrollPane的扩展，方便添加和删除行
 * @author zong0jie
 *
 */
public class JScrollPaneData extends JScrollPane{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4238706503361283499L;
	DefaultTableModel defaultTableModel = null;
	JTable jTabFInputGo = null;
	/**
	 * 往jScrollPane中添加表格，第一列为表头
	 */
	public void setProview( List<String[]> lsInfo)
	{
		String[][] tableValue = null;
		defaultTableModel = new DefaultTableModel(tableValue, lsInfo.get(0));
		jTabFInputGo = new JTable();
		setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(defaultTableModel);
		for (int i = 1; i < lsInfo.size(); i++) {
			defaultTableModel.addRow(lsInfo.get(i));
		}
	}
	/**
	 * 往jScrollPane中添加表格，第一列为表头
	 */
	public void setTitle( String[] title)
	{
		String[][] tableValue = null;
		defaultTableModel = new DefaultTableModel(tableValue, title);
		jTabFInputGo = new JTable();
		setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(defaultTableModel);
	}
	/**
	 * 往jScrollPane中添加表格，第一列为表头
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
	 * 往jScrollPane中添加表格，第一列为表头
	 */
	public void addProview(String[] info)
	{
		defaultTableModel.addRow(info);
	}
	
	public ArrayList<String[]> getLsDataInfo()
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
	/**
	 * 删除实际行
	 * @param rowNum
	 */
	public void removeRow(int... rowNum)
	{
		MathComput.sort(rowNum, false);
		for (int i : rowNum) {
			if (i < 0 || i > defaultTableModel.getRowCount()) {
				continue;
			}
			defaultTableModel.removeRow(i - 1);
//			defaultTableModel.setRowCount(i);// 删除行比较简单，只要用DefaultTableModel的removeRow()方法即可。删除
//			// 行完毕后必须重新设置列数，也就是使用DefaultTableModel的setRowCount()方法来设置。
		}
	}
	/**
	 * 获得绝对行数
	 * @return
	 */
	public int[] getSelectRows()
	{
		int selectRows=jTabFInputGo.getSelectedRows().length;// 取得用户所选行的行数
		//单行
		if(selectRows==1) {
			int selectedRowIndex = jTabFInputGo.getSelectedRow(); // 取得用户所选单行
			return new int[]{selectedRowIndex + 1};
		}
		int[] selRowIndexs = null;
		if(selectRows>1) {
			selRowIndexs =jTabFInputGo.getSelectedRows();// 用户所选行的序列
			for (int i = 0; i < selRowIndexs.length; i++) {
				selRowIndexs[i] = selRowIndexs[i] + 1;
			}
			return selRowIndexs;
		}
		return null;
	}
	
	public void removeSelRows()
	{
		removeRow(getSelectRows());
	}
}

