package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.nbcgui.controlseq.CtrlSamStatistics;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JScrollPane;

public class GuiLinesStatistics extends JPanel {
	CtrlSamStatistics ctrlSamStatistics = new CtrlSamStatistics();
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	JCheckBox chckbxSambamfile;
	JScrollPaneData sclBamSamFile;
	/**
	 * Create the panel.
	 */
	public GuiLinesStatistics() {
		setLayout(null);
		
		chckbxSambamfile = new JCheckBox("SamBamFile");
		chckbxSambamfile.setBounds(22, 349, 131, 22);
		add(chckbxSambamfile);
		
		sclBamSamFile = new JScrollPaneData();
		sclBamSamFile.setBounds(22, 12, 588, 329);
		sclBamSamFile.setTitle(new String[]{"FileName", "OutFileName"});
		add(sclBamSamFile);
		
		JButton btnOpen = new JButton("OpenFile");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("", "");
				sclBamSamFile.addItemLs(JScrollPaneData.getLsFileName2Out(lsFileName,"_report","txt"));
			}
		});
		btnOpen.setBounds(620, 12, 131, 24);
		add(btnOpen);
		
		JButton btnDelSelectRow = new JButton("DelSelectRow");
		btnDelSelectRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclBamSamFile.deleteSelRows();
			}
		});
		btnDelSelectRow.setBounds(620, 48, 131, 24);
		add(btnDelSelectRow);
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsFileName2Out = sclBamSamFile.getLsDataInfo();
				for (String[] fileName2Out : lsFileName2Out) {
					if (chckbxSambamfile.isSelected()) {
						ctrlSamStatistics.setSamFile(fileName2Out[0]);
					} else {
						ctrlSamStatistics.setTxtFile(fileName2Out[0]);
					}
					ctrlSamStatistics.setPrefix(FileOperate.getFileNameSep(fileName2Out[0])[0]);
					ctrlSamStatistics.setOutFile(fileName2Out[1]);
					if (chckbxSambamfile.isSelected()) {
						ctrlSamStatistics.writeSamStatistics();
					}
					else {
						ctrlSamStatistics.writeTxtStatistics();
					}
				}
			}
		});
		btnRun.setBounds(632, 469, 118, 24);
		add(btnRun);
		
	

	}
}
