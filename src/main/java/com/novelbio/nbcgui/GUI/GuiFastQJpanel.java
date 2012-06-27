package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import java.awt.CardLayout;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.nbcgui.controlseq.CtrlFastQMapping;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.ButtonGroup;

public class GuiFastQJpanel extends JPanel {
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	CtrlFastQMapping ctrlFastQMapping = new CtrlFastQMapping();
	private final ButtonGroup groupLibrary = new ButtonGroup();
	public GuiFastQJpanel() {
		setLayout(null);
		
		JLabel lblFastqfile = new JLabel("FastQFile");
		lblFastqfile.setBounds(10, 10, 68, 14);
		add(lblFastqfile);
		
		final JScrollPaneData scrollPaneInputFile = new JScrollPaneData();
		scrollPaneInputFile.setBounds(10, 30, 715, 182);
		scrollPaneInputFile.setTitle(new String[]{"FileName","Prix","Group"});
		add(scrollPaneInputFile);
		
		JButton btnOpenInputFile = new JButton("Open");
		btnOpenInputFile.setBounds(737, 26, 121, 24);
		add(btnOpenInputFile);
		
		JCheckBox chckbxFilterreads = new JCheckBox("FilterReads");
		chckbxFilterreads.setBounds(10, 226, 108, 22);
		add(chckbxFilterreads);
		
		JCheckBox chckbxCutend = new JCheckBox("CutEnd");
		chckbxCutend.setBounds(414, 226, 76, 22);
		add(chckbxCutend);
		
		textField = new JTextField();
		textField.setBounds(618, 228, 76, 18);
		add(textField);
		textField.setColumns(10);
		
		JComboBox cmbReadsQuality = new JComboBox();
		cmbReadsQuality.setBounds(253, 226, 76, 23);
		add(cmbReadsQuality);
		
		JLabel lblReadsQuality = new JLabel("Reads Quality");
		lblReadsQuality.setBounds(124, 230, 114, 14);
		add(lblReadsQuality);
		
		JLabel lblRetainBp = new JLabel("Retain Bp");
		lblRetainBp.setBounds(531, 230, 69, 14);
		add(lblRetainBp);
		
		JComboBox cmbFileFormat = new JComboBox();
		cmbFileFormat.setBounds(737, 189, 119, 23);
		add(cmbFileFormat);
		
		JLabel lblFileFormat = new JLabel("FileFormat");
		lblFileFormat.setBounds(737, 163, 77, 14);
		add(lblFileFormat);
		
		JCheckBox chckbxMapping = new JCheckBox("Mapping");
		chckbxMapping.setBounds(8, 263, 86, 22);
		add(chckbxMapping);
		
		JComboBox comboBox_2 = new JComboBox();
		comboBox_2.setBounds(187, 352, 237, 23);
		add(comboBox_2);
		
		JLabel lblAlgrethm = new JLabel("algrethm");
		lblAlgrethm.setBounds(12, 187, 66, 14);
		add(lblAlgrethm);
		
		JComboBox comboBox_3 = new JComboBox();
		comboBox_3.setBounds(10, 352, 147, 23);
		add(comboBox_3);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(10, 326, 56, 14);
		add(lblSpecies);
		
		JCheckBox chckbxUniqMapping = new JCheckBox("Uniq Mapping");
		chckbxUniqMapping.setBounds(10, 296, 121, 22);
		add(chckbxUniqMapping);
		
		textField_1 = new JTextField();
		textField_1.setBounds(12, 422, 317, 24);
		add(textField_1);
		textField_1.setColumns(10);
		
		JLabel lblExtendto = new JLabel("ExtendTo");
		lblExtendto.setBounds(17, 450, -137, -132);
		add(lblExtendto);
		
		textField_2 = new JTextField();
		textField_2.setBounds(10, 484, 337, 24);
		add(textField_2);
		textField_2.setColumns(10);
		
		JLabel lblResultpath = new JLabel("ResultPath");
		lblResultpath.setBounds(10, 458, 80, 14);
		add(lblResultpath);
		
		JButton btnSaveto = new JButton("SaveTo");
		btnSaveto.setBounds(386, 484, 88, 24);
		btnSaveto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				
				
				
				
				
				
				
				
				
				
			}
		});
		add(btnSaveto);
		
		JRadioButton rdbtnSingleend = new JRadioButton("SingleEnd");
		rdbtnSingleend.setBounds(143, 6, 95, 22);
		groupLibrary.add(rdbtnSingleend);
		add(rdbtnSingleend);
		
		JRadioButton rdbtnPairend = new JRadioButton("PairEnd");
		rdbtnPairend.setBounds(253, 6, 80, 22);
		groupLibrary.add(rdbtnPairend);
		add(rdbtnPairend);
		
		JCheckBox chckbxSort = new JCheckBox("Sort");
		chckbxSort.setBounds(181, 296, 57, 22);
		add(chckbxSort);
		
		JButton btnNewButton = new JButton("Delete");
		btnNewButton.setBounds(740, 91, 118, 24);
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneInputFile.removeSelRows();
			}
		});
		add(btnNewButton);
		
		JLabel lblMappingTo = new JLabel("Mapping To");
		lblMappingTo.setBounds(191, 326, 101, 14);
		add(lblMappingTo);
		
		JLabel lblMappingToFile = new JLabel("Mapping To File");
		lblMappingToFile.setBounds(10, 401, 121, 14);
		add(lblMappingToFile);
		
		JButton btnMappingindex = new JButton("MappingIndex");
		btnMappingindex.setBounds(386, 422, 134, 24);
		add(btnMappingindex);
		
		btnOpenInputFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsFile = ctrlFastQMapping.getlsGetFileInfo();
				scrollPaneInputFile.addProview(lsFile);
			}
		});
	}
}
