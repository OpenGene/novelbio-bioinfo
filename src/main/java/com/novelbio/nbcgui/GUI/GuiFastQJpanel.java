package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import java.awt.CardLayout;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public class GuiFastQJpanel extends JPanel {
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	public GuiFastQJpanel() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		JLabel lblFastqfile = new JLabel("FastQFile");
		springLayout.putConstraint(SpringLayout.NORTH, lblFastqfile, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblFastqfile, 10, SpringLayout.WEST, this);
		add(lblFastqfile);
		
		JScrollPane scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, lblFastqfile);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 188, SpringLayout.SOUTH, lblFastqfile);
		add(scrollPane);
		
		JButton btnOpen = new JButton("Open");
		springLayout.putConstraint(SpringLayout.NORTH, btnOpen, 30, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, btnOpen, 457, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -6, SpringLayout.WEST, btnOpen);
		add(btnOpen);
		
		JCheckBox chckbxFilterreads = new JCheckBox("FilterReads");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxFilterreads, 14, SpringLayout.SOUTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, chckbxFilterreads, 0, SpringLayout.WEST, lblFastqfile);
		add(chckbxFilterreads);
		
		JCheckBox chckbxCutend = new JCheckBox("CutEnd");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxCutend, 11, SpringLayout.SOUTH, chckbxFilterreads);
		springLayout.putConstraint(SpringLayout.WEST, chckbxCutend, 0, SpringLayout.WEST, lblFastqfile);
		add(chckbxCutend);
		
		textField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textField, 2, SpringLayout.NORTH, chckbxCutend);
		springLayout.putConstraint(SpringLayout.EAST, textField, 0, SpringLayout.EAST, scrollPane);
		add(textField);
		textField.setColumns(10);
		
		JComboBox comboBox = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboBox, 0, SpringLayout.NORTH, chckbxFilterreads);
		springLayout.putConstraint(SpringLayout.WEST, comboBox, 219, SpringLayout.EAST, chckbxFilterreads);
		springLayout.putConstraint(SpringLayout.EAST, comboBox, -497, SpringLayout.EAST, this);
		add(comboBox);
		
		JLabel lblReadsQuality = new JLabel("Reads Quality");
		springLayout.putConstraint(SpringLayout.NORTH, lblReadsQuality, 18, SpringLayout.SOUTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, lblReadsQuality, -146, SpringLayout.WEST, comboBox);
		springLayout.putConstraint(SpringLayout.EAST, lblReadsQuality, -48, SpringLayout.WEST, comboBox);
		add(lblReadsQuality);
		
		JLabel lblRetainBp = new JLabel("Retain Bp");
		springLayout.putConstraint(SpringLayout.WEST, lblRetainBp, 0, SpringLayout.WEST, lblReadsQuality);
		springLayout.putConstraint(SpringLayout.SOUTH, lblRetainBp, 0, SpringLayout.SOUTH, textField);
		add(lblRetainBp);
		
		JComboBox comboBox_1 = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboBox_1, 65, SpringLayout.SOUTH, btnOpen);
		springLayout.putConstraint(SpringLayout.WEST, comboBox_1, 6, SpringLayout.EAST, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, comboBox_1, 0, SpringLayout.EAST, btnOpen);
		add(comboBox_1);
		
		JLabel lblFastqformat = new JLabel("FastQFormat");
		springLayout.putConstraint(SpringLayout.WEST, lblFastqformat, 6, SpringLayout.EAST, scrollPane);
		springLayout.putConstraint(SpringLayout.SOUTH, lblFastqformat, -6, SpringLayout.NORTH, comboBox_1);
		add(lblFastqformat);
		
		JCheckBox chckbxMapping = new JCheckBox("Mapping");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxMapping, 25, SpringLayout.SOUTH, chckbxCutend);
		springLayout.putConstraint(SpringLayout.WEST, chckbxMapping, 10, SpringLayout.WEST, this);
		add(chckbxMapping);
		
		JComboBox comboBox_2 = new JComboBox();
		springLayout.putConstraint(SpringLayout.WEST, comboBox_2, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, comboBox_2, 60, SpringLayout.SOUTH, chckbxMapping);
		springLayout.putConstraint(SpringLayout.EAST, comboBox_2, 140, SpringLayout.WEST, this);
		add(comboBox_2);
		
		JLabel lblAlgrethm = new JLabel("algrethm");
		springLayout.putConstraint(SpringLayout.SOUTH, lblAlgrethm, -301, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.NORTH, comboBox_2, 6, SpringLayout.SOUTH, lblAlgrethm);
		springLayout.putConstraint(SpringLayout.EAST, lblAlgrethm, 0, SpringLayout.EAST, lblFastqfile);
		add(lblAlgrethm);
		
		JComboBox comboBox_3 = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboBox_3, 85, SpringLayout.SOUTH, lblRetainBp);
		springLayout.putConstraint(SpringLayout.WEST, comboBox_3, 51, SpringLayout.EAST, comboBox_2);
		springLayout.putConstraint(SpringLayout.EAST, comboBox_3, -627, SpringLayout.EAST, this);
		add(comboBox_3);
		
		JLabel lblSpecies = new JLabel("Species");
		springLayout.putConstraint(SpringLayout.NORTH, lblSpecies, 0, SpringLayout.NORTH, lblAlgrethm);
		springLayout.putConstraint(SpringLayout.WEST, lblSpecies, 0, SpringLayout.WEST, lblReadsQuality);
		add(lblSpecies);
		
		JCheckBox chckbxUniqMapping = new JCheckBox("Uniq Mapping");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxUniqMapping, 6, SpringLayout.SOUTH, comboBox_2);
		springLayout.putConstraint(SpringLayout.WEST, chckbxUniqMapping, 0, SpringLayout.WEST, lblFastqfile);
		add(chckbxUniqMapping);
		
		JCheckBox chckbxConvertbed = new JCheckBox("convert2bed");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxConvertbed, 23, SpringLayout.SOUTH, chckbxUniqMapping);
		springLayout.putConstraint(SpringLayout.WEST, chckbxConvertbed, 0, SpringLayout.WEST, lblFastqfile);
		add(chckbxConvertbed);
		
		JCheckBox chckbxPairendextend = new JCheckBox("PairEnd/Extend");
		springLayout.putConstraint(SpringLayout.SOUTH, chckbxPairendextend, 0, SpringLayout.SOUTH, chckbxConvertbed);
		springLayout.putConstraint(SpringLayout.EAST, chckbxPairendextend, 0, SpringLayout.EAST, comboBox_3);
		add(chckbxPairendextend);
		
		textField_1 = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textField_1, 164, SpringLayout.SOUTH, textField);
		springLayout.putConstraint(SpringLayout.WEST, textField_1, 445, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, textField_1, -452, SpringLayout.EAST, this);
		add(textField_1);
		textField_1.setColumns(10);
		
		JLabel lblExtendto = new JLabel("ExtendTo");
		springLayout.putConstraint(SpringLayout.NORTH, lblExtendto, 0, SpringLayout.NORTH, chckbxConvertbed);
		springLayout.putConstraint(SpringLayout.WEST, lblExtendto, 48, SpringLayout.EAST, chckbxPairendextend);
		springLayout.putConstraint(SpringLayout.SOUTH, lblExtendto, 0, SpringLayout.SOUTH, chckbxConvertbed);
		springLayout.putConstraint(SpringLayout.EAST, lblExtendto, -7, SpringLayout.WEST, textField_1);
		add(lblExtendto);
		
		textField_2 = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, textField_2, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, textField_2, 102, SpringLayout.SOUTH, chckbxConvertbed);
		springLayout.putConstraint(SpringLayout.EAST, textField_2, 87, SpringLayout.EAST, lblRetainBp);
		add(textField_2);
		textField_2.setColumns(10);
		
		JLabel lblResultpath = new JLabel("ResultPath");
		springLayout.putConstraint(SpringLayout.SOUTH, lblResultpath, -126, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.NORTH, textField_2, 6, SpringLayout.SOUTH, lblResultpath);
		springLayout.putConstraint(SpringLayout.WEST, lblResultpath, 0, SpringLayout.WEST, lblFastqfile);
		add(lblResultpath);
		
		JButton btnSaveto = new JButton("SaveTo");
		springLayout.putConstraint(SpringLayout.NORTH, btnSaveto, 78, SpringLayout.SOUTH, textField_1);
		springLayout.putConstraint(SpringLayout.WEST, btnSaveto, 0, SpringLayout.WEST, lblExtendto);
		add(btnSaveto);
		
		JCheckBox chckbxPairEnd = new JCheckBox("Pair End");
		springLayout.putConstraint(SpringLayout.NORTH, chckbxPairEnd, -4, SpringLayout.NORTH, lblFastqfile);
		springLayout.putConstraint(SpringLayout.EAST, chckbxPairEnd, 0, SpringLayout.EAST, scrollPane);
		add(chckbxPairEnd);
	}
}
