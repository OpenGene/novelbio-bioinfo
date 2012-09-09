package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import com.novelbio.analysis.microarray.AffyNormalization;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mirna.MiRNAtargetRNAhybrid;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controlseq.CtrlFastQMapping;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.ButtonGroup;

public class GuiAffyCelNormJpanel extends JPanel {
	private JTextField txtSavePathAndPrefix;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private final ButtonGroup groupLibrary = new ButtonGroup();
	JScrollPaneData scrollPaneCelFile;
	JComboBoxData<Integer> cmbNormalizedType;
	JButton btnSaveto;
	JButton btnOpenFastqLeft;
	JButton btnDelFastqLeft;
	JButton btnRun;
	AffyNormalization affyNormalization = new AffyNormalization();
	
	public GuiAffyCelNormJpanel() {
		setLayout(null);
		
		JLabel lblFastqfile = new JLabel("FastQFile");
		lblFastqfile.setBounds(10, 10, 68, 14);
		add(lblFastqfile);
		
		scrollPaneCelFile = new JScrollPaneData();
		scrollPaneCelFile.setBounds(10, 30, 783, 188);
		scrollPaneCelFile.setTitle(new String[]{"CelFileName"});
		add(scrollPaneCelFile);
		
		btnOpenFastqLeft = new JButton("Open");
		btnOpenFastqLeft.setBounds(805, 26, 82, 24);
		add(btnOpenFastqLeft);
		
		cmbNormalizedType = new JComboBoxData<Integer>();
		
		cmbNormalizedType.setMapItem(AffyNormalization.getMapNormStr2ID());
		cmbNormalizedType.setBounds(175, 252, 194, 23);
		add(cmbNormalizedType);
		
		JLabel lblReadsQuality = new JLabel("NormalizedType");
		lblReadsQuality.setBounds(10, 256, 168, 14);
		add(lblReadsQuality);
		
		JLabel lblAlgrethm = new JLabel("algrethm");
		lblAlgrethm.setBounds(12, 187, 66, 14);
		add(lblAlgrethm);
		//≥ı ºªØcmbSpeciesVersion
		try {} catch (Exception e) { }
		
		JLabel lblExtendto = new JLabel("ExtendTo");
		lblExtendto.setBounds(17, 450, -137, -132);
		add(lblExtendto);
		
		txtSavePathAndPrefix = new JTextField();
		txtSavePathAndPrefix.setBounds(10, 388, 783, 24);
		add(txtSavePathAndPrefix);
		txtSavePathAndPrefix.setColumns(10);
		
		JLabel lblResultpath = new JLabel("ResultPath");
		lblResultpath.setBounds(10, 369, 80, 14);
		add(lblResultpath);
		
		btnSaveto = new JButton("SaveTo");
		btnSaveto.setBounds(805, 388, 88, 24);
		btnSaveto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filePathName = guiFileOpen.openFilePathName("", "");
				txtSavePathAndPrefix.setText(filePathName);
			}
		});
		add(btnSaveto);
		
		btnDelFastqLeft = new JButton("Delete");
		btnDelFastqLeft.setBounds(805, 194, 82, 24);
		
		btnDelFastqLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneCelFile.deleteSelRows();
			}
		});
		add(btnDelFastqLeft);
		
		btnRun = new JButton("Run");
		btnRun.setBounds(775, 469, 118, 24);
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsCelFileName = scrollPaneCelFile.getLsDataInfo();
				ArrayList<String> lsCelFile = new ArrayList<String>();
				for (String[] string : lsCelFileName) {
					lsCelFile.add(string[0]);
				}
				affyNormalization.setLsRawCelFile(lsCelFile);
				affyNormalization.setNormalizedType(cmbNormalizedType.getSelectedValue());
				affyNormalization.setOutFileName(txtSavePathAndPrefix.getText());
				affyNormalization.run();
			}
		});
		add(btnRun);

		
		btnOpenFastqLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileLeft = guiFileOpen.openLsFileName("fastqFile","");
				for (String string : lsFileLeft) {
					scrollPaneCelFile.addItem(new String[]{string});
				}
			}
		});
		initialize();
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		cmbNormalizedType.setSelectedIndex(0);
	}
}
