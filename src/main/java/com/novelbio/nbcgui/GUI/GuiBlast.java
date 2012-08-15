package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JTextFieldData;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.updatedb.database.BlastUp2DB;

import javax.swing.JCheckBox;

public class GuiBlast extends JPanel {
	private JTextField textQueryFasta;
	private JTextField textSubjectFasta;
	private JTextField textEvalue;
	private JTextFieldData textResultNum;
	private JTextField textResultFile;
	
	private BlastNBC blastNBC = new BlastNBC();
	private JTextField textUpDateBlast;
	
	GUIFileOpen fileOpen = new GUIFileOpen();
	private JTextField textQDBinfo;
	private JTextField textSDBinfo;
	JCheckBox chbRefStyle = null;
	JComboBoxData cmbQSpecies = null;
	JComboBoxData cmbSSpecies = null;
	JComboBoxData combBlastType = null;
	JComboBoxData combResultType = null;
	
	int queryType = SeqFasta.SEQ_UNKNOWN;
	int subjectType = SeqFasta.SEQ_UNKNOWN;
	
	private void setCombBlastType() {
		combBlastType.setMapItem(BlastNBC.getHashBlast(queryType, subjectType));
	}
	
	
	/**
	 * Create the panel.
	 */
	public GuiBlast() {
		setLayout(null);
		
		textQueryFasta = new JTextField();
		textQueryFasta.setBounds(10, 50, 249, 21);
		add(textQueryFasta);
		textQueryFasta.setColumns(10);
		
		JButton btnQueryFasta = new JButton("QueryFasta");
		btnQueryFasta.setBounds(279, 48, 119, 24);
		btnQueryFasta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = fileOpen.openFileName("txt", "");
				textQueryFasta.setText(path);
				if (FileOperate.isFileExist(path)) {
					queryType = SeqHash.getSeqType(path);
				}
				setCombBlastType();
			}
		});
		add(btnQueryFasta);
		
		textSubjectFasta = new JTextField();
		textSubjectFasta.setBounds(10, 97, 249, 21);
		add(textSubjectFasta);
		textSubjectFasta.setColumns(10);
		
		JButton btnSubjectDB = new JButton("SubjectFasta");
		btnSubjectDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = fileOpen.openFileName("txt", "");
				textSubjectFasta.setText(path);
				if (FileOperate.isFileExist(path)) {
					subjectType = SeqHash.getSeqType(path);
				}
				setCombBlastType();
			}
		});
		btnSubjectDB.setBounds(279, 95, 130, 24);
		add(btnSubjectDB);
		
		combBlastType = new JComboBoxData();
		combBlastType.setBounds(10, 175, 209, 23);
		add(combBlastType);
		
		combResultType = new JComboBoxData();
		combResultType.setMapItem(BlastNBC.getHashResultType());
		combResultType.setBounds(10, 440, 249, 23);
		add(combResultType);
		
		JLabel lblBlastType = new JLabel("BlastType");
		lblBlastType.setBounds(10, 152, 91, 14);
		add(lblBlastType);
		
		textEvalue = new JTextField();
		textEvalue.setText("0.01");
		textEvalue.setBounds(10, 240, 114, 18);
		add(textEvalue);
		textEvalue.setColumns(10);
		
		JLabel lblEvalue = new JLabel("E-Value");
		lblEvalue.setBounds(10, 216, 54, 14);
		add(lblEvalue);
		
		textResultNum = new JTextFieldData();
		textResultNum.setNumOnly();
		textResultNum.setText("2");
		textResultNum.setBounds(10, 299, 114, 18);
		add(textResultNum);
		textResultNum.setColumns(10);
		
		JLabel lblResultnum = new JLabel("ResultNum");
		lblResultnum.setBounds(10, 279, 77, 14);
		add(lblResultnum);
		
		textResultFile = new JTextField();
		textResultFile.setBounds(10, 367, 249, 21);
		add(textResultFile);
		textResultFile.setColumns(10);
		
		JButton btnResultfile = new JButton("ResultFile");
		btnResultfile.setBounds(279, 365, 105, 24);
		add(btnResultfile);
		
		JButton btnRunblast = new JButton("RunBlast");
		btnRunblast.setBounds(279, 439, 98, 24);
		btnRunblast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				blastNBC.setBlastType((String) combBlastType.getSelectedValue());
				blastNBC.setCpuNum(2);
				blastNBC.setDatabaseSeq(textSubjectFasta.getText());
				blastNBC.setQueryFastaFile(textQueryFasta.getText());
				blastNBC.setEvalue(Double.parseDouble(textEvalue.getText()));
				blastNBC.setResultAlignNum(Integer.parseInt(textResultNum.getText()));
				blastNBC.setResultSeqNum(Integer.parseInt(textResultNum.getText()));
				blastNBC.setResultFile(textResultFile.getText());
				blastNBC.setResultType((Integer) combResultType.getSelectedValue());
				blastNBC.blast();
			}
		});
		add(btnRunblast);
		
		textUpDateBlast = new JTextField();
		textUpDateBlast.setBounds(467, 46, 256, 25);
		add(textUpDateBlast);
		textUpDateBlast.setColumns(10);
		
		JButton btnNewButton = new JButton("OpenBlastFile");
		btnNewButton.setBounds(729, 48, 134, 24);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = fileOpen.openFileName("txt", "");
				textUpDateBlast.setText(path);
			}
		});
		add(btnNewButton);
		
		JButton btnUpdataBlast = new JButton("UpdataBlast");
		btnUpdataBlast.setBounds(729, 365, 123, 24);
		btnUpdataBlast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BlastUp2DB blast = new BlastUp2DB();
				if (!textSDBinfo.getText().trim().equals("")) {
					blast.setBlastDBinfo(textSDBinfo.getText().trim());
				}
				if (!textQDBinfo.getText().trim().equals("")) {
					blast.setQueryDBinfo(textQDBinfo.getText().trim());
				}
				blast.setIDisBlastType(chbRefStyle.isSelected());
				blast.setSubTaxID((Integer)cmbSSpecies.getSelectedValue());
				blast.setTaxID((Integer)cmbQSpecies.getSelectedValue());
				blast.setTxtWriteExcep(FileOperate.changeFileSuffix(textUpDateBlast.getText(), "_cannotUpDate", null));
				blast.updateFile(textUpDateBlast.getText(), false);
			}
		});
		add(btnUpdataBlast);
		
		chbRefStyle = new JCheckBox("ref|NP_002932|  like style");
		chbRefStyle.setBounds(467, 96, 207, 22);
		add(chbRefStyle);
		
		textQDBinfo = new JTextField();
		textQDBinfo.setBounds(467, 160, 155, 24);
		add(textQDBinfo);
		textQDBinfo.setColumns(10);
		
		JLabel lblQuerydbinfo = new JLabel("QueryDBinfo");
		lblQuerydbinfo.setBounds(467, 140, 92, 14);
		add(lblQuerydbinfo);
		
		textSDBinfo = new JTextField();
		textSDBinfo.setBounds(645, 160, 207, 24);
		add(textSDBinfo);
		textSDBinfo.setColumns(10);
		
		JLabel lblSubjectdbinfo = new JLabel("SubjectDBinfo");
		lblSubjectdbinfo.setBounds(645, 140, 103, 14);
		add(lblSubjectdbinfo);
		
		cmbQSpecies = new JComboBoxData();
		cmbQSpecies.setBounds(467, 238, 320, 23);
		cmbQSpecies.setMapItem(Species.getSpeciesNameTaxID(false));
		add(cmbQSpecies);
		
		JLabel lblQueryspecies = new JLabel("QuerySpecies");
		lblQueryspecies.setBounds(467, 216, 100, 14);
		add(lblQueryspecies);
		
		cmbSSpecies = new JComboBoxData();
		cmbSSpecies.setBounds(467, 318, 322, 23);
		cmbSSpecies.setMapItem(Species.getSpeciesNameTaxID(false));
		add(cmbSSpecies);
		
		JLabel lblSubjectspecies = new JLabel("SubjectSpecies");
		lblSubjectspecies.setBounds(467, 279, 111, 14);
		add(lblSubjectspecies);

		
		JLabel lblResulttype = new JLabel("ResultType");
		lblResulttype.setBounds(10, 414, 91, 14);
		add(lblResulttype);

	}
}
