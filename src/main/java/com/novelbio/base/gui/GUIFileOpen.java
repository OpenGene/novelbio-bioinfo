package com.novelbio.base.gui;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class GUIFileOpen  extends JFrame {
	/**
	 * 打开文本选择器
	 * @param description 如"txt/excel 2003"
	 * @param extensions 如 "txt","xls"
	 * @return
	 */
	public String openFileName(String  description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}
	/**
	 * 打开文本选择器
	 * @param description 如"txt/excel 2003"
	 * @param extensions 如 "txt","xls"
	 * @return
	 */
	public ArrayList<String> openLsFileName(String  description, String... extensions) {
		ArrayList<String> lsResult = new ArrayList<String>();
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			for (File file : files) {
				lsResult.add(file.getAbsolutePath());
			}
			return lsResult;
		}
		return null;
	}
	/**
	 * 打开文本选择器
	 * @param description 如"txt/excel 2003"
	 * @param extensions 如 "txt","xls"
	 * @return
	 */
	public String saveFileName(String  description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return FileOperate.addSuffix(chooser.getSelectedFile().getAbsolutePath(),extensions[0]);
		}
		return null;
	}
}
