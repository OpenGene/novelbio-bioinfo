package com.novelbio.nbcgui.GUI;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.nbcgui.controlseq.CtrlMiRNA;
import javax.swing.JScrollPane;

public class GuiSeqMiRNA extends JPanel{
	private static final long serialVersionUID = -5940420720636777182L;
	private JFrame frame;
	private JTextField txtHairpineMiRNA;
	private JTextField txtNCRNA;
	private JTextField txtRfam;
	private JTextField txtGenomeSeq;
	private JTextField txtGffFile;
	private JTextField txtChromPath;
	private JTextField txtRfamInfo;
	private JTextField txtMatureMiRNA;
	private JTextField txtRNAdataFile;
	private JTextField txtMiRNAbed;
	private JTextField txtRfamBed;
	private JTextField txtNCRNAbed;
	private JTextField txtHairPinRaw;
	private JTextField txtHairRegx;
	private JTextField txtRefseqFile;
	private JTextField txtRfamRaw;
	private JTextField txtOutPathPrefix;
	JButton btnNCrna;
	JButton btnRfam;
	JButton btnGenome;
	JCheckBox chkMapAllBedFileToGenome;
	JButton btnGfffile;
	JButton btnChrompath;
	JButton btnRfaminfo;
	JButton btnMatureMiRNA;
	JComboBox combFileType;
	JComboBox combSpecies;
	JButton btnRnadata;
	JButton btnRunning;
	JCheckBox chkMapping;
	JButton btnMirnabed;
	JButton btnRfambed;
	JButton btnNCRNAbed;
	JCheckBox chkAnalysis;
	JButton btnHairepinfile;
	JButton btnRfamfile;
	JButton btnRefseqfile;
	JButton btnOutpath;
	JCheckBox chkbxExtractSeq;
	JButton btnHairpinMiRNA;
	JScrollPaneData sclpanFastq;
	
	ArrayList<Component> lsComponentsMapping = new ArrayList<Component>();
	ArrayList<Component> lsComponentsAnalysis = new ArrayList<Component>();
	ArrayList<Component> lsComponentsMappingAndAnalysis = new ArrayList<Component>();
	ArrayList<Component> lsComponentsExtractSeq = new ArrayList<Component>();
	private JLabel lblRfamregx;
	private JTextField txtRfamRegx;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlMiRNA ctrlMiRNA = new CtrlMiRNA();
	private JButton btnFastqfile;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiSeqMiRNA window = new GuiSeqMiRNA();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GuiSeqMiRNA() {
		setLayout(null);
		
		txtHairpineMiRNA = new JTextField();
		txtHairpineMiRNA.setBounds(24, 103, 221, 18);
		add(txtHairpineMiRNA);
		txtHairpineMiRNA.setColumns(10);
		
		txtNCRNA = new JTextField();
		txtNCRNA.setBounds(25, 166, 221, 18);
		add(txtNCRNA);
		txtNCRNA.setColumns(10);
		
		txtRfam = new JTextField();
		txtRfam.setBounds(23, 137, 224, 18);
		add(txtRfam);
		txtRfam.setColumns(10);
		
		btnHairpinMiRNA = new JButton("MiRNArefSeq");
		btnHairpinMiRNA.setBounds(257, 97, 128, 24);
		btnHairpinMiRNA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtHairpineMiRNA.setText(fileName);
			}
		});
		add(btnHairpinMiRNA);
		
		btnNCrna = new JButton("NCrnaRefSeq");
		btnNCrna.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtNCRNA.setText(fileName);
			}
		});
		btnNCrna.setBounds(257, 161, 130, 24);
		add(btnNCrna);
		
		btnRfam = new JButton("RfamRefseq");
		btnRfam.setBounds(256, 134, 125, 24);
		btnRfam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtRfam.setText(fileName);
			}
		});
		add(btnRfam);
		
		JLabel lblSettingRefence = new JLabel("Setting Refence");
		lblSettingRefence.setBounds(24, 77, 118, 14);
		add(lblSettingRefence);
		
		JLabel lblSettingGenomeIndex = new JLabel("Setting Genome Index");
		lblSettingGenomeIndex.setBounds(28, 191, 159, 14);
		add(lblSettingGenomeIndex);
		
		txtGenomeSeq = new JTextField();
		txtGenomeSeq.setBounds(27, 213, 224, 18);
		add(txtGenomeSeq);
		txtGenomeSeq.setColumns(10);
		
		btnGenome = new JButton("Genome");
		btnGenome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtGenomeSeq.setText(fileName);
			}
		});
		btnGenome.setBounds(266, 209, 118, 24);
		add(btnGenome);
		//是否将全部的bed文件mapping至基因组上，用于看基因组上的reads分布
		chkMapAllBedFileToGenome = new JCheckBox("mapping all bedFile to Genome");
		chkMapAllBedFileToGenome.setBounds(25, 234, 245, 22);
		add(chkMapAllBedFileToGenome);
		
		txtGffFile = new JTextField();
		txtGffFile.setBounds(24, 259, 221, 18);
		add(txtGffFile);
		txtGffFile.setColumns(10);
		
		btnGfffile = new JButton("GffGeneFile");
		btnGfffile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtGffFile.setText(fileName);
			}
		});
		btnGfffile.setBounds(254, 256, 128, 24);
		add(btnGfffile);
		
		txtChromPath = new JTextField();
		txtChromPath.setBounds(24, 293, 221, 18);
		add(txtChromPath);
		txtChromPath.setColumns(10);
		
		btnChrompath = new JButton("ChromPath");
		btnChrompath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFilePathName("txt/fasta", "");
				txtChromPath.setText(fileName);
			}
		});
		btnChrompath.setBounds(251, 287, 128, 24);
		add(btnChrompath);
		
		txtRfamInfo = new JTextField();
		txtRfamInfo.setBounds(26, 329, 219, 18);
		add(txtRfamInfo);
		txtRfamInfo.setColumns(10);
		
		btnRfaminfo = new JButton("RfamInfo");
		btnRfaminfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtRfamInfo.setText(fileName);
			}
		});
		btnRfaminfo.setBounds(251, 326, 128, 24);
		add(btnRfaminfo);
		
		txtMatureMiRNA = new JTextField();
		txtMatureMiRNA.setBounds(24, 372, 221, 18);
		add(txtMatureMiRNA);
		txtMatureMiRNA.setColumns(10);
		
		btnMatureMiRNA = new JButton("MatureMirna");
		btnMatureMiRNA.setBounds(251, 369, 130, 24);
		btnMatureMiRNA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtMatureMiRNA.setText(fileName);
			}
		});
		add(btnMatureMiRNA);
		
		combFileType = new JComboBox();
		combFileType.setBounds(25, 419, 120, 23);
		add(combFileType);
		
		combSpecies = new JComboBox();
		combSpecies.setBounds(166, 419, 173, 23);
		add(combSpecies);
		
		txtRNAdataFile = new JTextField();
		txtRNAdataFile.setBounds(30, 460, 217, 18);
		add(txtRNAdataFile);
		txtRNAdataFile.setColumns(10);
		
		btnRnadata = new JButton("RNAdata");
		btnRnadata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtRNAdataFile.setText(fileName);
			}
		});
		btnRnadata.setBounds(260, 457, 96, 24);
		add(btnRnadata);
		
		btnRunning = new JButton("Running");
		btnRunning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO
			}
		});
		btnRunning.setBounds(705, 526, 92, 24);
		add(btnRunning);
		
		chkMapping = new JCheckBox("Mapping");
		chkMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkSelected(chkMapping.isSelected(), chkAnalysis.isSelected(), false);
				chkbxExtractSeq.setSelected(false);
			}
		});
		chkMapping.setBounds(155, 527, 86, 22);
		add(chkMapping);
		
		txtMiRNAbed = new JTextField();
		txtMiRNAbed.setBounds(442, 46, 233, 18);
		add(txtMiRNAbed);
		txtMiRNAbed.setColumns(10);
		
		btnMirnabed = new JButton("MiRNABed");
		btnMirnabed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/bed", "");
				txtMiRNAbed.setText(fileName);
			}
		});
		btnMirnabed.setBounds(705, 43, 105, 24);
		add(btnMirnabed);
		
		txtRfamBed = new JTextField();
		txtRfamBed.setBounds(442, 80, 233, 18);
		add(txtRfamBed);
		txtRfamBed.setColumns(10);
		
		btnRfambed = new JButton("RfamBed");
		btnRfambed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtRfamBed.setText(fileName);
			}
		});
		btnRfambed.setBounds(705, 77, 98, 24);
		add(btnRfambed);
		
		txtNCRNAbed = new JTextField();
		txtNCRNAbed.setBounds(442, 118, 233, 18);
		add(txtNCRNAbed);
		txtNCRNAbed.setColumns(10);
		
		btnNCRNAbed = new JButton("NCRNAbed");
		btnNCRNAbed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/bed", "");
				txtNCRNAbed.setText(fileName);
			}
		});
		btnNCRNAbed.setBounds(705, 115, 108, 24);
		add(btnNCRNAbed);
		
		chkAnalysis = new JCheckBox("Analysis");
		chkAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkSelected(chkMapping.isSelected(), chkAnalysis.isSelected(), false);
				chkbxExtractSeq.setSelected(false);
			}
		});
		chkAnalysis.setBounds(328, 528, 84, 22);
		add(chkAnalysis);
		
		txtHairPinRaw = new JTextField();
		txtHairPinRaw.setBounds(442, 259, 233, 18);
		add(txtHairPinRaw);
		txtHairPinRaw.setColumns(10);
		
		btnHairepinfile = new JButton("HairPinFile");
		btnHairepinfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt", "");
				txtHairPinRaw.setText(fileName);
			}
		});
		btnHairepinfile.setBounds(705, 256, 111, 24);
		add(btnHairepinfile);
		
		txtHairRegx = new JTextField();
		txtHairRegx.setBounds(561, 235, 114, 18);
		add(txtHairRegx);
		txtHairRegx.setColumns(10);
		
		JLabel lblHairregx = new JLabel("HairRegx");
		lblHairregx.setBounds(442, 235, 65, 14);
		add(lblHairregx);
		
		txtRefseqFile = new JTextField();
		txtRefseqFile.setBounds(442, 356, 233, 18);
		add(txtRefseqFile);
		txtRefseqFile.setColumns(10);
		
		txtRfamRaw = new JTextField();
		txtRfamRaw.setBounds(442, 329, 233, 18);
		add(txtRfamRaw);
		txtRfamRaw.setColumns(10);
		
		btnRfamfile = new JButton("RfamFile");
		btnRfamfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt", "");
				txtRfamRaw.setText(fileName);
			}
		});
		btnRfamfile.setBounds(705, 323, 96, 24);
		add(btnRfamfile);
		
		btnRefseqfile = new JButton("RefseqFile");
		btnRefseqfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt", "");
				txtRefseqFile.setText(fileName);
			}
		});
		btnRefseqfile.setBounds(703, 353, 110, 24);
		add(btnRefseqfile);
		
		txtOutPathPrefix = new JTextField();
		txtOutPathPrefix.setBounds(442, 425, 233, 18);
		add(txtOutPathPrefix);
		txtOutPathPrefix.setColumns(10);
		
		JLabel lblOutpathprefix = new JLabel("OutPathPrefix");
		lblOutpathprefix.setBounds(442, 405, 105, 14);
		add(lblOutpathprefix);
		
		btnOutpath = new JButton("OutPath");
		btnOutpath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileName("prefix", "");
				txtOutPathPrefix.setText(fileName);
			}
		});
		btnOutpath.setBounds(705, 418, 95, 24);
		add(btnOutpath);
		
		chkbxExtractSeq = new JCheckBox("extract seq");
		chkbxExtractSeq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkSelected(false, false, chkbxExtractSeq.isSelected());
				chkAnalysis.setSelected(false);
				chkMapping.setSelected(false);
			}
		});
		chkbxExtractSeq.setBounds(486, 527, 110, 22);
		add(chkbxExtractSeq);
		
		lblRfamregx = new JLabel("RfamRegx");
		lblRfamregx.setBounds(442, 307, 92, 14);
		add(lblRfamregx);
		
		txtRfamRegx = new JTextField();
		txtRfamRegx.setBounds(561, 305, 114, 18);
		add(txtRfamRegx);
		txtRfamRegx.setColumns(10);
		
		btnFastqfile = new JButton("FastqFile");
		btnFastqfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("fastq", "");
				for (String string : lsFileName) {
					sclpanFastq.addProview(new String[]{string});
				}
			}
		});
		btnFastqfile.setBounds(257, 12, 118, 24);
		add(btnFastqfile);
		
		sclpanFastq = new JScrollPaneData();
		sclpanFastq.setBounds(22, 10, 228, 60);
		add(sclpanFastq);
		initialize();
	}
	
	
	private void chkSelected(boolean booChkMap, boolean booChkAnalysis, boolean booChkExtractSeq) {
		if (booChkMap && !booChkAnalysis && !booChkExtractSeq) {
			for (Component component : lsComponentsAnalysis) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsExtractSeq) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsMapping) {
				component.setEnabled(true);
			}
		}
		if (booChkMap && booChkAnalysis && !booChkExtractSeq) {
			for (Component component : lsComponentsAnalysis) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsExtractSeq) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsMapping) {
				component.setEnabled(true);
			}
			for (Component component : lsComponentsMappingAndAnalysis) {
				component.setEnabled(true);
			}
		}
		if (!booChkMap && booChkAnalysis && !booChkExtractSeq) {
			for (Component component : lsComponentsMapping) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsExtractSeq) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsAnalysis) {
				component.setEnabled(true);
			}
		}
		if (!booChkMap && !booChkAnalysis && booChkExtractSeq) {
			for (Component component : lsComponentsMapping) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsAnalysis) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsExtractSeq) {
				component.setEnabled(true);
			}
		}
		if (!booChkMap && !booChkAnalysis && !booChkExtractSeq) {
			for (Component component : lsComponentsAnalysis) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsExtractSeq) {
				component.setEnabled(false);
			}
			for (Component component : lsComponentsMapping) {
				component.setEnabled(false);
			}
			btnRunning.setEnabled(false);
		}
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		lsComponentsMapping.add(txtNCRNA);
		lsComponentsMapping.add(txtHairpineMiRNA);
		lsComponentsMapping.add(txtRfam);
		lsComponentsMapping.add(txtGenomeSeq);
		lsComponentsMapping.add(txtGffFile);
		lsComponentsMapping.add(txtChromPath);
		lsComponentsMapping.add(txtRfamInfo);
		lsComponentsMapping.add(sclpanFastq);
		
		lsComponentsMapping.add(btnHairpinMiRNA);
		lsComponentsMapping.add(btnNCrna);
		lsComponentsMapping.add(btnRfam);
		lsComponentsMapping.add(btnGenome);
		lsComponentsMapping.add(chkMapAllBedFileToGenome);
		lsComponentsMapping.add(btnGfffile);
		lsComponentsMapping.add(btnChrompath);
		lsComponentsMapping.add(btnRfaminfo);
		lsComponentsMapping.add(btnFastqfile);
		

		lsComponentsAnalysis.add(txtHairpineMiRNA);
		lsComponentsAnalysis.add(txtMiRNAbed);
		lsComponentsAnalysis.add(txtRfamBed);
		lsComponentsAnalysis.add(txtNCRNAbed);
		lsComponentsAnalysis.add(txtRNAdataFile);
		lsComponentsAnalysis.add(txtMatureMiRNA);
		
		lsComponentsAnalysis.add(btnHairpinMiRNA);
		lsComponentsAnalysis.add(btnMirnabed);
		lsComponentsAnalysis.add(btnRfambed);
		lsComponentsAnalysis.add(btnNCRNAbed);
		lsComponentsAnalysis.add(combFileType);
		lsComponentsAnalysis.add(combSpecies);
		lsComponentsAnalysis.add(btnRnadata);
		lsComponentsAnalysis.add(btnMatureMiRNA);

		
		lsComponentsExtractSeq.add(txtHairPinRaw);
		lsComponentsExtractSeq.add(txtHairRegx);
		lsComponentsExtractSeq.add(txtRfamRegx);
		lsComponentsExtractSeq.add(txtRefseqFile);
		lsComponentsExtractSeq.add(txtRfamRaw);
		
		lsComponentsExtractSeq.add(btnHairepinfile);
		lsComponentsExtractSeq.add(btnRfamfile);
		lsComponentsExtractSeq.add(btnRefseqfile);
		
		lsComponentsMappingAndAnalysis.add(combFileType);
		lsComponentsMappingAndAnalysis.add(combSpecies);
		lsComponentsMappingAndAnalysis.add(txtRNAdataFile);
		lsComponentsMappingAndAnalysis.add(btnRnadata);
		lsComponentsMappingAndAnalysis.add(btnMatureMiRNA);
		lsComponentsMappingAndAnalysis.add(txtMatureMiRNA);
		
		chkMapping.setSelected(true);
		for (Component component : lsComponentsAnalysis) {
			component.setEnabled(false);
		}
		for (Component component : lsComponentsExtractSeq) {
			component.setEnabled(false);
		}
		for (Component component : lsComponentsMapping) {
			component.setEnabled(true);
		}
		chkbxExtractSeq.setSelected(false);
		chkAnalysis.setSelected(false);
		sclpanFastq.setTitle(new String[]{"fastqFile"});
	}
	
	private void runMapping(String fastqFile) {
		ctrlMiRNA.setFastqFile(fastqFile);
		ctrlMiRNA.setGenome(chkMapAllBedFileToGenome.isSelected(), txtGenomeSeq.getText());
		ctrlMiRNA.setMirnaFile(txtMatureMiRNA.getText());
//		ctrlMiRNA.setMiRNAinfo(combFileType.getSelectedItem(), combSpecies.getSelectedItem(), txtRNAdataFile.getText());
	}
}
