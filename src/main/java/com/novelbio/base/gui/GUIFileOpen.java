package com.novelbio.base.gui;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.novelbio.base.dataOperate.ExcelOperate;

public class GUIFileOpen  extends JFrame {
	/**
	 * ���ı�ѡ����
	 * @param description ��"txt/excel 2003"
	 * @param extensions �� "txt","xls"
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
	 * ���ı�ѡ����
	 * @param description ��"txt/excel 2003"
	 * @param extensions �� "txt","xls"
	 * @return
	 */
	public String saveFileName(String  description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}
}
