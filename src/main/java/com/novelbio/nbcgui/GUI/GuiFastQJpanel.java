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
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		JLabel lblFastqfile = new JLabel("FastQFile");
		springLayout.putConstraint(SpringLayout.NORTH, lblFastqfile, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblFastqfile, 10, SpringLayout.WEST, this);
		add(lblFastqfile);
		
		final JScrollPaneData scrollPaneInputFile = new JScrollPaneData();
		scrollPaneInputFile.setTitle(new String[]{"FileName","Prix","Group"});
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneInputFile, 6, SpringLayout.SOUTH, lblFastqfile);
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneInputFile, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneInputFile, 188, SpringLayout.SOUTH, lblFastqfile);
		add(scrollPaneInputFile);
		
		JButton btnOpenInputFile = new JButton("Open");

		springLayout.putConstraint(SpringLayout.NORTH, btnOpenInputFile, 30, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, btnOpenInputFile, 457, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneInputFile, -6, SpringLayout.WEST, btnOpenInputFile);
		add(btnOpenInputFile);
		
		JCheckBox chckbxFilterreads = new JCheckBox("FilterReads");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxFilterreads, 14, SpringLayout.SOUTH, scrollPaneInputFile);
		springLayout.putConstraint(SpringLayout.WEST, chckbxFilterreads, 0, SpringLayout.WEST, lblFastqfile);
		add(chckbxFilterreads);
		
		JCheckBox chckbxCutend = new JCheckBox("CutEnd");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxCutend, 11, SpringLayout.SOUTH, chckbxFilterreads);
		springLayout.putConstraint(SpringLayout.WEST, chckbxCutend, 0, SpringLayout.WEST, lblFastqfile);
		add(chckbxCutend);
		
		textField = new JTextField();
		add(textField);
		textField.setColumns(10);
		
		JComboBox cmbReadsQuality = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, textField, 14, SpringLayout.SOUTH, cmbReadsQuality);
		springLayout.putConstraint(SpringLayout.WEST, textField, 0, SpringLayout.WEST, cmbReadsQuality);
		springLayout.putConstraint(SpringLayout.NORTH, cmbReadsQuality, 0, SpringLayout.NORTH, chckbxFilterreads);
		springLayout.putConstraint(SpringLayout.WEST, cmbReadsQuality, 135, SpringLayout.EAST, chckbxFilterreads);
		springLayout.putConstraint(SpringLayout.EAST, cmbReadsQuality, 0, SpringLayout.EAST, btnOpenInputFile);
		add(cmbReadsQuality);
		
		JLabel lblReadsQuality = new JLabel("Reads Quality");
		springLayout.putConstraint(SpringLayout.NORTH, lblReadsQuality, 4, SpringLayout.NORTH, chckbxFilterreads);
		springLayout.putConstraint(SpringLayout.WEST, lblReadsQuality, 6, SpringLayout.EAST, chckbxFilterreads);
		springLayout.putConstraint(SpringLayout.EAST, lblReadsQuality, -15, SpringLayout.WEST, cmbReadsQuality);
		add(lblReadsQuality);
		
		JLabel lblRetainBp = new JLabel("Retain Bp");
		springLayout.putConstraint(SpringLayout.NORTH, lblRetainBp, 4, SpringLayout.NORTH, chckbxCutend);
		springLayout.putConstraint(SpringLayout.EAST, lblRetainBp, 0, SpringLayout.EAST, lblReadsQuality);
		add(lblRetainBp);
		
		JComboBox cmbFileFormat = new JComboBox();
		springLayout.putConstraint(SpringLayout.WEST, cmbFileFormat, 8, SpringLayout.EAST, scrollPaneInputFile);
		springLayout.putConstraint(SpringLayout.SOUTH, cmbFileFormat, -39, SpringLayout.NORTH, cmbReadsQuality);
		springLayout.putConstraint(SpringLayout.EAST, cmbFileFormat, 2, SpringLayout.EAST, btnOpenInputFile);
		add(cmbFileFormat);
		
		JLabel lblFileFormat = new JLabel("FileFormat");
		springLayout.putConstraint(SpringLayout.WEST, lblFileFormat, 6, SpringLayout.EAST, scrollPaneInputFile);
		springLayout.putConstraint(SpringLayout.SOUTH, lblFileFormat, -7, SpringLayout.NORTH, cmbFileFormat);
		add(lblFileFormat);
		
		JCheckBox chckbxMapping = new JCheckBox("Mapping");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxMapping, 25, SpringLayout.SOUTH, chckbxCutend);
		springLayout.putConstraint(SpringLayout.WEST, chckbxMapping, 10, SpringLayout.WEST, this);
		add(chckbxMapping);
		
		JComboBox comboBox_2 = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboBox_2, 364, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, comboBox_2, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, comboBox_2, 140, SpringLayout.WEST, this);
		add(comboBox_2);
		
		JLabel lblAlgrethm = new JLabel("algrethm");
		springLayout.putConstraint(SpringLayout.SOUTH, lblAlgrethm, -337, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, lblAlgrethm, 0, SpringLayout.EAST, lblFastqfile);
		add(lblAlgrethm);
		
		JComboBox comboBox_3 = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboBox_3, 364, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, comboBox_3, 191, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, comboBox_3, -627, SpringLayout.EAST, this);
		add(comboBox_3);
		
		JLabel lblSpecies = new JLabel("Species");
		springLayout.putConstraint(SpringLayout.NORTH, lblSpecies, 344, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblSpecies, 113, SpringLayout.EAST, lblAlgrethm);
		add(lblSpecies);
		
		JCheckBox chckbxUniqMapping = new JCheckBox("Uniq Mapping");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxUniqMapping, 7, SpringLayout.SOUTH, comboBox_2);
		springLayout.putConstraint(SpringLayout.WEST, chckbxUniqMapping, 0, SpringLayout.WEST, lblFastqfile);
		add(chckbxUniqMapping);
		
		JCheckBox chckbxConvertbed = new JCheckBox("convert2bed");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxConvertbed, 9, SpringLayout.SOUTH, comboBox_3);
		springLayout.putConstraint(SpringLayout.WEST, chckbxConvertbed, 65, SpringLayout.EAST, chckbxUniqMapping);
		add(chckbxConvertbed);
		
		JCheckBox chckbxPairendextend = new JCheckBox("PairEnd/Extend");
		springLayout.putConstraint(SpringLayout.WEST, chckbxPairendextend, 9, SpringLayout.WEST, this);
		add(chckbxPairendextend);
		
		textField_1 = new JTextField();
		springLayout.putConstraint(SpringLayout.SOUTH, textField_1, -29, SpringLayout.NORTH, chckbxPairendextend);
		springLayout.putConstraint(SpringLayout.EAST, textField_1, -790, SpringLayout.EAST, this);
		add(textField_1);
		textField_1.setColumns(10);
		
		JLabel lblExtendto = new JLabel("ExtendTo");
		springLayout.putConstraint(SpringLayout.WEST, textField_1, 3, SpringLayout.EAST, lblExtendto);
		springLayout.putConstraint(SpringLayout.NORTH, lblExtendto, 63, SpringLayout.SOUTH, comboBox_3);
		springLayout.putConstraint(SpringLayout.WEST, lblExtendto, 17, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, lblExtendto, -27, SpringLayout.NORTH, chckbxPairendextend);
		springLayout.putConstraint(SpringLayout.EAST, lblExtendto, -1012, SpringLayout.EAST, this);
		add(lblExtendto);
		
		textField_2 = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, textField_2, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, textField_2, -96, SpringLayout.SOUTH, this);
		add(textField_2);
		textField_2.setColumns(10);
		
		JLabel lblResultpath = new JLabel("ResultPath");
		springLayout.putConstraint(SpringLayout.SOUTH, chckbxPairendextend, -31, SpringLayout.NORTH, lblResultpath);
		springLayout.putConstraint(SpringLayout.NORTH, textField_2, 6, SpringLayout.SOUTH, lblResultpath);
		springLayout.putConstraint(SpringLayout.SOUTH, lblResultpath, -126, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblResultpath, 0, SpringLayout.WEST, lblFastqfile);
		add(lblResultpath);
		
		JButton btnSaveto = new JButton("SaveTo");
		btnSaveto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				
				
				
				
				
				
				
				
				
				
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textField_2, -94, SpringLayout.WEST, btnSaveto);
		springLayout.putConstraint(SpringLayout.NORTH, btnSaveto, 0, SpringLayout.NORTH, textField_2);
		springLayout.putConstraint(SpringLayout.EAST, btnSaveto, 0, SpringLayout.EAST, btnOpenInputFile);
		add(btnSaveto);
		
		JRadioButton rdbtnSingleend = new JRadioButton("SingleEnd");
		groupLibrary.add(rdbtnSingleend);
		springLayout.putConstraint(SpringLayout.WEST, rdbtnSingleend, 66, SpringLayout.EAST, lblFastqfile);
		springLayout.putConstraint(SpringLayout.SOUTH, rdbtnSingleend, 0, SpringLayout.SOUTH, lblFastqfile);
		add(rdbtnSingleend);
		
		JRadioButton rdbtnPairend = new JRadioButton("PairEnd");
		groupLibrary.add(rdbtnPairend);
		springLayout.putConstraint(SpringLayout.NORTH, rdbtnPairend, 0, SpringLayout.NORTH, rdbtnSingleend);
		springLayout.putConstraint(SpringLayout.WEST, rdbtnPairend, 23, SpringLayout.EAST, rdbtnSingleend);
		add(rdbtnPairend);
		
		JCheckBox chckbxSort = new JCheckBox("Sort");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxSort, 28, SpringLayout.SOUTH, textField_1);
		springLayout.putConstraint(SpringLayout.WEST, chckbxSort, 46, SpringLayout.EAST, chckbxPairendextend);
		add(chckbxSort);
		
		JButton btnNewButton = new JButton("Delete");
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneInputFile.removeSelRows();
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, btnNewButton, 9, SpringLayout.EAST, scrollPaneInputFile);
		springLayout.putConstraint(SpringLayout.SOUTH, btnNewButton, -22, SpringLayout.NORTH, lblFileFormat);
		springLayout.putConstraint(SpringLayout.EAST, btnNewButton, 73, SpringLayout.EAST, scrollPaneInputFile);
		add(btnNewButton);
		
		btnOpenInputFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsFile = ctrlFastQMapping.getlsGetFileInfo();
				scrollPaneInputFile.addProview(lsFile);
			}
		});
	}
}
