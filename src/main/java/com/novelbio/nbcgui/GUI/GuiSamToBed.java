package com.novelbio.nbcgui.GUI;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.mapping.SamFile;
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
	JRadioButton rdbtnExtend;
	JRadioButton rdbtnFilterstrand;
	JRadioButton rdbtnSort;
	
	ButtonGroup buttonGroup = new ButtonGroup();
	ButtonGroup buttonGroupCisTrans = new ButtonGroup();
	
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private JRadioButton rdbtnCis;
	private JRadioButton rdbtnTrans;
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
		
		JButton btnConverttobedfile = new JButton("ConvertToBedFile");
		btnConverttobedfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SamFile samFile = new SamFile(txtSamFile.getText());
				if (chckbxNonUniqueMapping.isSelected()) {
					samFile.setUniqueRandomSelectOneRead(true);
				}
				else {
					samFile.setUniqueRandomSelectOneRead(false);
				}
				samFile.toBedSingleEnd();
			}
		});
		btnConverttobedfile.setBounds(12, 77, 194, 24);
		add(btnConverttobedfile);
		
		JLabel lblSamfile = new JLabel("SamFile");
		lblSamfile.setBounds(12, 21, 69, 14);
		add(lblSamfile);
		
		txtBedFile = new JTextField();
		txtBedFile.setBounds(12, 162, 316, 18);
		add(txtBedFile);
		txtBedFile.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("OpenBedFile");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtBedFile.setText(guiFileOpen.openFileName("", ""));
			}
		});
		btnNewButton_1.setBounds(351, 159, 183, 24);
		add(btnNewButton_1);
		
		JLabel lblBedfile = new JLabel("BedFile");
		lblBedfile.setBounds(12, 143, 69, 14);
		add(lblBedfile);
		
		txtExtend = new JTextField();
		txtExtend.setBounds(164, 198, 114, 18);
		add(txtExtend);
		txtExtend.setColumns(10);
		
		JButton btnConvert = new JButton("Convert");
		btnConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BedSeq bedSeq = new BedSeq(txtBedFile.getText());
				if (rdbtnExtend.isSelected()) {
					int extendTo = Integer.parseInt(txtExtend.getText());
					bedSeq.extend(extendTo);
				}
				else if (rdbtnSort.isSelected()) {
					bedSeq.sortBedFile();
				}
				else if (rdbtnFilterstrand.isSelected()) {
					int mappingNumSmall = 1; int mappingNumBig = 1;
					try { mappingNumSmall = Integer.parseInt(txtMappingNumSmall.getText().trim()); 
					mappingNumBig = Integer.parseInt(txtMappingNumBig.getText().trim()); 
					} catch (Exception e2) { 	}
					
					Boolean strand = null;
					if (rdbtnCis.isSelected()) {
						strand = true;
					}
					else if (rdbtnTrans.isSelected()) {
						strand = false;
					}
					
					String bedFile = FileOperate.changeFileSuffix(txtBedFile.getText(), "_filtered", null);
					BedSeq bedSeq2 = new BedSeq(bedFile, true);
					for (BedRecord bedRecord : bedSeq.readlines()) {
						if (strand != null && bedRecord.isCis5to3() != strand) {
							continue;
						}
						if (bedRecord.getMappingNum() >= mappingNumSmall && bedRecord.getMappingNum() <= mappingNumBig) {
							bedSeq2.writeBedRecord(bedRecord);
						}
					}
					bedSeq2.closeWrite();
				}
			}
		});
		btnConvert.setBounds(393, 273, 118, 24);
		add(btnConvert);
		
		rdbtnExtend = new JRadioButton("Extend");
		rdbtnExtend.setBounds(12, 196, 98, 22);
		add(rdbtnExtend);
		
		rdbtnFilterstrand = new JRadioButton("FilterStrand");
		rdbtnFilterstrand.setBounds(12, 233, 131, 22);
		add(rdbtnFilterstrand);
		
		rdbtnSort = new JRadioButton("Sort");
		rdbtnSort.setBounds(12, 274, 98, 22);
		add(rdbtnSort);
		
		rdbtnCis = new JRadioButton("Cis");
		rdbtnCis.setBounds(163, 233, 46, 22);
		add(rdbtnCis);
		
		rdbtnTrans = new JRadioButton("Trans");
		rdbtnTrans.setBounds(223, 233, 80, 22);
		add(rdbtnTrans);
		initial();
		buttonGroupCisTrans.add(rdbtnCis);
		buttonGroupCisTrans.add(rdbtnTrans);
		
		txtMappingNumSmall = new JTextField();
		txtMappingNumSmall.setText("1");
		txtMappingNumSmall.setBounds(413, 235, 30, 18);
		add(txtMappingNumSmall);
		txtMappingNumSmall.setColumns(10);
		
		JLabel lblMappingnum = new JLabel("MappingNum");
		lblMappingnum.setBounds(306, 237, 114, 14);
		add(lblMappingnum);
		
		lblTo = new JLabel("To");
		lblTo.setBounds(447, 237, 30, 14);
		add(lblTo);
		
		txtMappingNumBig = new JTextField();
		txtMappingNumBig.setText("1");
		txtMappingNumBig.setBounds(486, 235, 36, 18);
		add(txtMappingNumBig);
		txtMappingNumBig.setColumns(10);
		
		chckbxNonUniqueMapping = new JCheckBox("Non Unique Mapping Get Random Reads");
		chckbxNonUniqueMapping.setBounds(214, 78, 320, 22);
		add(chckbxNonUniqueMapping);
	}
	
	private void initial() {
		buttonGroup.add(rdbtnExtend);
		buttonGroup.add(rdbtnFilterstrand);
		buttonGroup.add(rdbtnSort);
	}
}
