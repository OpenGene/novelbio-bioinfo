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
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.mirna.MiRNAtargetRNAhybrid;
import com.novelbio.analysis.seq.rnaseq.CufflinksGTF;
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

public class GuiTranscriptomeCufflinks extends JPanel {
	private JTextField txtSavePathAndPrefix;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	JScrollPaneData scrollPaneSamBamFile;
	JComboBoxData<Species> cmbSpecies;
	JComboBoxData<String> cmbVersion;
	JButton btnSaveto;
	JButton btnOpenFastqLeft;
	JButton btnDelFastqLeft;
	JButton btnRun;
	CufflinksGTF cufflinksGTF = new CufflinksGTF();
	private JComboBoxData<StrandSpecific> cmbStrandSpecific;
	private JLabel lblStrandtype;
	
	public GuiTranscriptomeCufflinks() {
		setLayout(null);
		
		JLabel lblFastqfile = new JLabel("BamFile");
		lblFastqfile.setBounds(10, 10, 68, 14);
		add(lblFastqfile);
		
		scrollPaneSamBamFile = new JScrollPaneData();
		scrollPaneSamBamFile.setBounds(10, 30, 783, 188);
		scrollPaneSamBamFile.setTitle(new String[]{"BamFileName"});
		add(scrollPaneSamBamFile);
		
		btnOpenFastqLeft = new JButton("Open");
		btnOpenFastqLeft.setBounds(805, 26, 82, 24);
		add(btnOpenFastqLeft);
		
		cmbSpecies = new JComboBoxData<Species>();
		cmbSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Species species = cmbSpecies.getSelectedValue();
				cmbVersion.setMapItem(species.getMapVersion());
			}
		});
		
		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		cmbSpecies.setBounds(10, 252, 194, 23);
		add(cmbSpecies);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(10, 230, 168, 14);
		add(lblSpecies);
		
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
				scrollPaneSamBamFile.deleteSelRows();
			}
		});
		add(btnDelFastqLeft);
		
		btnRun = new JButton("Run");
		btnRun.setBounds(775, 469, 118, 24);
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsSamFileName = scrollPaneSamBamFile.getLsDataInfo();
				ArrayList<String> lsSamFile = new ArrayList<String>();
				for (String[] samFile : lsSamFileName) {
					lsSamFile.add(samFile[0]);
				}
				cufflinksGTF.setBam(lsSamFile);
				Species species = cmbSpecies.getSelectedValue();
				species.setVersion(cmbVersion.getSelectedValue());
				GffChrAbs gffChrAbs = new GffChrAbs(species);
				
				cufflinksGTF.setGffChrAbs(gffChrAbs);
				cufflinksGTF.setStrandSpecifictype(cmbStrandSpecific.getSelectedValue());
				cufflinksGTF.setExePath("", species.getChromSeq());
				cufflinksGTF.setOutPathPrefix(txtSavePathAndPrefix.getText());

				cufflinksGTF.runCufflinks();
			}
		});
		add(btnRun);
		
		cmbVersion = new JComboBoxData<String>();
		cmbVersion.setBounds(240, 252, 184, 23);
		add(cmbVersion);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(238, 230, 69, 14);
		add(lblVersion);
		
		cmbStrandSpecific = new JComboBoxData<StrandSpecific>();
		cmbStrandSpecific.setBounds(10, 319, 194, 23);
		add(cmbStrandSpecific);
		
		lblStrandtype = new JLabel("StrandType");
		lblStrandtype.setBounds(10, 298, 118, 14);
		add(lblStrandtype);

		
		btnOpenFastqLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileLeft = guiFileOpen.openLsFileName("fastqFile","");
				for (String string : lsFileLeft) {
					scrollPaneSamBamFile.addItem(new String[]{string});
				}
			}
		});
		initialize();
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		cmbSpecies.setSelectedIndex(0);
		cmbStrandSpecific.setMapItem(StrandSpecific.getMapStrandLibrary());
	}
}
