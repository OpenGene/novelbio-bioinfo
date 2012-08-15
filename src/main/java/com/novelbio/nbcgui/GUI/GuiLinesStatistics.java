package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.nbcgui.controlseq.CtrlSamStatistics;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GuiLinesStatistics extends JPanel {
	private JTextField txtOpenFile;
	private JTextField txtOutFile;
	CtrlSamStatistics ctrlSamStatistics = new CtrlSamStatistics();
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	JCheckBox chckbxSambamfile;
	/**
	 * Create the panel.
	 */
	public GuiLinesStatistics() {
		setLayout(null);
		
		txtOpenFile = new JTextField();
		txtOpenFile.setBounds(42, 37, 251, 18);
		add(txtOpenFile);
		txtOpenFile.setColumns(10);
		
		chckbxSambamfile = new JCheckBox("SamBamFile");
		chckbxSambamfile.setBounds(42, 74, 131, 22);
		add(chckbxSambamfile);
		
		JButton btnOpen = new JButton("OpenFile");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtOpenFile.setText(guiFileOpen.openFileName("", ""));
			}
		});
		btnOpen.setBounds(323, 34, 131, 24);
		add(btnOpen);
		
		txtOutFile = new JTextField();
		txtOutFile.setBounds(42, 143, 251, 18);
		add(txtOutFile);
		txtOutFile.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("btnSavePath");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtOutFile.setText(guiFileOpen.openFileName("", ""));
			}
		});
		btnNewButton_1.setBounds(323, 140, 131, 24);
		add(btnNewButton_1);
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxSambamfile.isSelected()) {
					ctrlSamStatistics.setSamFile(txtOpenFile.getText());
				}
				else {
					ctrlSamStatistics.setTxtFile(txtOpenFile.getText());
				}
				ctrlSamStatistics.setPrefix(FileOperate.getFileNameSep(txtOpenFile.getText())[0]);
				ctrlSamStatistics.setOutFile(txtOutFile.getText());
				if (chckbxSambamfile.isSelected()) {
					ctrlSamStatistics.writeSamStatistics();
				}
				else {
					ctrlSamStatistics.writeTxtStatistics();
				}
			}
		});
		btnRun.setBounds(323, 222, 118, 24);
		add(btnRun);

	}
}
