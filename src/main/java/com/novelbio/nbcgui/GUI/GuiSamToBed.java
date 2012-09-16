package com.novelbio.nbcgui.GUI;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;

public class GuiSamToBed extends JPanel {
	private JTextField txtSamFile;
	private JTextField txtBedFile;
	private JTextField txtExtend;
	
	ButtonGroup buttonGroup = new ButtonGroup();
	ButtonGroup buttonGroupCisTrans = new ButtonGroup();
	JCheckBox chckbxExtend;
	JCheckBox chckbxFilterreads;
	JCheckBox chckbxCis;
	JCheckBox chckbxTrans;
	JCheckBox chckbxSortBed;
	JCheckBox chckbxSortBam;
	JCheckBox chckbxIndex;
	JCheckBox chckbxRealign;
	JButton btnSamtobed;
	JCheckBox chckbxTobam;

	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private JTextField txtMappingNumSmall;
	private JLabel lblTo;
	private JTextField txtMappingNumBig;
	private JCheckBox chckbxNonUniqueMapping;
	/**
	 * Create the panel.
	 */
	public GuiSamToBed() {
		setLayout(null);
		
		txtSamFile = new JTextField();
		txtSamFile.setBounds(12, 47, 316, 18);
		add(txtSamFile);
		txtSamFile.setColumns(10);
		
		JButton btnNewButton = new JButton("OpenSamFile");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtSamFile.setText(guiFileOpen.openFileName("", ""));
			}
		});
		btnNewButton.setBounds(351, 44, 183, 24);
		add(btnNewButton);
		
		JButton btnConvertSam = new JButton("ConvertSam");
		btnConvertSam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SamFile samFile = new SamFile(txtSamFile.getText());
				if (chckbxRealign.isSelected()) {
					samFile.copeSamFile2Snp();
					return;
				}
				
				if (chckbxTobam.isSelected()) {
					samFile = samFile.convertToBam();
				}
				if (chckbxSortBam.isSelected()) {
					samFile = samFile.sort();
				}
				if (chckbxIndex.isSelected()) {
					samFile.index();
				}
			}
		});
		btnConvertSam.setBounds(351, 128, 194, 24);
		add(btnConvertSam);
		
		JLabel lblSamfile = new JLabel("SamFile");
		lblSamfile.setBounds(12, 21, 69, 14);
		add(lblSamfile);
		
		txtBedFile = new JTextField();
		txtBedFile.setBounds(12, 284, 316, 18);
		add(txtBedFile);
		txtBedFile.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("OpenBedFile");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtBedFile.setText(guiFileOpen.openFileName("", ""));
			}
		});
		btnNewButton_1.setBounds(351, 281, 183, 24);
		add(btnNewButton_1);
		
		JLabel lblBedfile = new JLabel("BedFile");
		lblBedfile.setBounds(12, 265, 69, 14);
		add(lblBedfile);
		
		txtExtend = new JTextField();
		txtExtend.setBounds(137, 322, 114, 18);
		add(txtExtend);
		txtExtend.setColumns(10);
		
		JButton btnConvertBed = new JButton("ConvertBed");
		btnConvertBed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BedSeq bedSeq = new BedSeq(txtBedFile.getText());
				if (chckbxExtend.isSelected()) {
					int extendLen = 250;
					try { extendLen = Integer.parseInt(txtExtend.getText()); } catch (Exception e2) { }
					bedSeq = bedSeq.extend(extendLen);
				}
				if (chckbxFilterreads.isSelected()) {
					Boolean strand = null;
					if (chckbxCis.isSelected()) {
						strand = true;
					}
					else if (chckbxTrans.isSelected()) {
						strand = false;
					}
					int small = 1;
					try { small = Integer.getInteger(txtMappingNumSmall.getText()); } catch (Exception e2) { }
					int big = 1;
					try { big = Integer.getInteger(txtMappingNumBig.getText()); } catch (Exception e2) { }
					bedSeq = bedSeq.filterSeq(small, big, strand);
				}
				if (chckbxSortBed.isSelected()) {
					bedSeq.sortBedFile();
				}
			}
		});
		btnConvertBed.setBounds(370, 397, 147, 24);
		add(btnConvertBed);
		initial();
		
		txtMappingNumSmall = new JTextField();
		txtMappingNumSmall.setText("1");
		txtMappingNumSmall.setBounds(399, 352, 30, 18);
		add(txtMappingNumSmall);
		txtMappingNumSmall.setColumns(10);
		
		JLabel lblMappingnum = new JLabel("MappingNum");
		lblMappingnum.setBounds(297, 352, 114, 14);
		add(lblMappingnum);
		
		lblTo = new JLabel("To");
		lblTo.setBounds(433, 354, 30, 14);
		add(lblTo);
		
		txtMappingNumBig = new JTextField();
		txtMappingNumBig.setText("1");
		txtMappingNumBig.setBounds(472, 352, 36, 18);
		add(txtMappingNumBig);
		txtMappingNumBig.setColumns(10);
		
		chckbxNonUniqueMapping = new JCheckBox("Non Unique Mapping Get Random Reads");
		chckbxNonUniqueMapping.setBounds(12, 204, 320, 22);
		add(chckbxNonUniqueMapping);
		
		chckbxExtend = new JCheckBox("Extend");
		chckbxExtend.setBounds(12, 320, 131, 22);
		add(chckbxExtend);
		
		chckbxFilterreads = new JCheckBox("FilterReads");
		chckbxFilterreads.setBounds(12, 348, 131, 22);
		add(chckbxFilterreads);
		
		chckbxCis = new JCheckBox("Cis");
		chckbxCis.setBounds(147, 348, 53, 22);
		add(chckbxCis);
		
		chckbxTrans = new JCheckBox("Trans");
		chckbxTrans.setBounds(214, 348, 69, 22);
		add(chckbxTrans);
		
		chckbxSortBed = new JCheckBox("SortBed");
		chckbxSortBed.setBounds(12, 382, 131, 22);
		add(chckbxSortBed);
		
		chckbxSortBam = new JCheckBox("sortBam");
		chckbxSortBam.setBounds(96, 73, 91, 22);
		add(chckbxSortBam);
		
		chckbxIndex = new JCheckBox("index");
		chckbxIndex.setBounds(191, 73, 69, 22);
		add(chckbxIndex);
		
		chckbxRealign = new JCheckBox("realignRemoveDupRecalibrate");
		chckbxRealign.setBounds(12, 103, 256, 22);
		add(chckbxRealign);
		
		btnSamtobed = new JButton("SamtoBed");
		btnSamtobed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SamFile samFile = new SamFile(txtSamFile.getText());
				samFile.setUniqueRandomSelectOneRead(chckbxNonUniqueMapping.isSelected());
				samFile.toBedSingleEnd();
			}
		});
		btnSamtobed.setBounds(351, 203, 118, 24);
		add(btnSamtobed);
		
		chckbxTobam = new JCheckBox("toBam");
		chckbxTobam.setBounds(12, 73, 81, 22);
		add(chckbxTobam);
	}
	
	private void initial() {
	}
}
