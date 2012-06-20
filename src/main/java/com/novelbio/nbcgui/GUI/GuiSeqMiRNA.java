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
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import com.novelbio.analysis.seq.mirna.ListMiRNALocation;
import com.novelbio.analysis.seq.mirna.MappingMiRNA;
import com.novelbio.analysis.seq.mirna.MiRNACount;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.nbcgui.controlseq.CtrlMiRNA;
import javax.swing.JScrollPane;

public class GuiSeqMiRNA extends JPanel{
	private static final long serialVersionUID = -5940420720636777182L;
	private JFrame frame;
	private JTextField txtHairpineMiRNA;
	private JTextField txtNCRNA;
	private JTextField txtRfamSeq;
	private JTextField txtGenomeSeq;
	private JTextField txtGffGeneFile;
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
	JComboBoxData<Integer> combFileType;
	JComboBoxData<String> combSpecies;
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
	JButton btnRepeatgff;
	JCheckBox chkPredictMiRNA;
	JScrollPaneData sclNovelMiRNAbed;
	JButton btnDelFastQfilerow;
	
	ArrayList<Component> lsComponentsMapping = new ArrayList<Component>();
	ArrayList<Component> lsComponentsAnalysis = new ArrayList<Component>();
	ArrayList<Component> lsComponentsMappingAndAnalysis = new ArrayList<Component>();
	ArrayList<Component> lsComponentsExtractSeq = new ArrayList<Component>();
	ArrayList<Component> lsComponentsPredictMiRNA = new ArrayList<Component>();
	ArrayList<Component> lsComponentsAnalysisPredictMiRNA = new ArrayList<Component>();
	HashSet<Component> lsComponentsAll = new HashSet<Component>();
	private JLabel lblRfamregx;
	private JTextField txtRfamRegx;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlMiRNA ctrlMiRNA = new CtrlMiRNA();
	private JButton btnFastqfile;
	private JTextField txtRepeatGff;
	private JButton btnNovelmirnabed;
	private JButton btnDelNovelMiRNAbedFileRow;
	
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
		
		txtRfamSeq = new JTextField();
		txtRfamSeq.setBounds(23, 137, 224, 18);
		add(txtRfamSeq);
		txtRfamSeq.setColumns(10);
		
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
				txtRfamSeq.setText(fileName);
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
		
		txtGffGeneFile = new JTextField();
		txtGffGeneFile.setBounds(24, 259, 221, 18);
		add(txtGffGeneFile);
		txtGffGeneFile.setColumns(10);
		
		btnGfffile = new JButton("GffGeneFile");
		btnGfffile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtGffGeneFile.setText(fileName);
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
		
		combFileType = new JComboBoxData<Integer>();
		combFileType.setBounds(25, 419, 166, 23);
		add(combFileType);
		
		combSpecies = new JComboBoxData<String>();
		combSpecies.setBounds(208, 419, 173, 23);
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
				running();
			}
		});
		btnRunning.setBounds(705, 526, 92, 24);
		add(btnRunning);
		
		chkMapping = new JCheckBox("Mapping");
		chkMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!chkMapping.isSelected() && chkAnalysis.isSelected()) {
					chkPredictMiRNA.setSelected(false);
				}
				chkbxExtractSeq.setSelected(false);
				chkSelected(chkMapping.isSelected(), chkAnalysis.isSelected(), chkPredictMiRNA.isSelected(), false);
			}
		});
		chkMapping.setBounds(79, 527, 86, 22);
		add(chkMapping);
		
		txtMiRNAbed = new JTextField();
		txtMiRNAbed.setBounds(442, 15, 233, 18);
		add(txtMiRNAbed);
		txtMiRNAbed.setColumns(10);
		
		btnMirnabed = new JButton("MiRNABed");
		btnMirnabed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/bed", "");
				txtMiRNAbed.setText(fileName);
			}
		});
		btnMirnabed.setBounds(705, 12, 105, 24);
		add(btnMirnabed);
		
		txtRfamBed = new JTextField();
		txtRfamBed.setBounds(442, 45, 233, 18);
		add(txtRfamBed);
		txtRfamBed.setColumns(10);
		
		btnRfambed = new JButton("RfamBed");
		btnRfambed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtRfamBed.setText(fileName);
			}
		});
		btnRfambed.setBounds(705, 42, 98, 24);
		add(btnRfambed);
		
		txtNCRNAbed = new JTextField();
		txtNCRNAbed.setBounds(442, 75, 233, 18);
		add(txtNCRNAbed);
		txtNCRNAbed.setColumns(10);
		
		btnNCRNAbed = new JButton("NCRNAbed");
		btnNCRNAbed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/bed", "");
				txtNCRNAbed.setText(fileName);
			}
		});
		btnNCRNAbed.setBounds(705, 72, 108, 24);
		add(btnNCRNAbed);
		
		chkAnalysis = new JCheckBox("Analysis");
		chkAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!chkMapping.isSelected()) {
					chkPredictMiRNA.setSelected(false);
				}
				chkbxExtractSeq.setSelected(false);
				chkSelected(chkMapping.isSelected(), chkAnalysis.isSelected(), chkPredictMiRNA.isSelected(), false);
				
			}
		});
		chkAnalysis.setBounds(221, 527, 84, 22);
		add(chkAnalysis);
		
		txtHairPinRaw = new JTextField();
		txtHairPinRaw.setBounds(435, 325, 233, 18);
		add(txtHairPinRaw);
		txtHairPinRaw.setColumns(10);
		
		btnHairepinfile = new JButton("HairPinFile");
		btnHairepinfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt", "");
				txtHairPinRaw.setText(fileName);
			}
		});
		btnHairepinfile.setBounds(698, 322, 111, 24);
		add(btnHairepinfile);
		
		txtHairRegx = new JTextField();
		txtHairRegx.setBounds(554, 301, 114, 18);
		add(txtHairRegx);
		txtHairRegx.setColumns(10);
		
		JLabel lblHairregx = new JLabel("HairRegx");
		lblHairregx.setBounds(435, 301, 65, 14);
		add(lblHairregx);
		
		txtRefseqFile = new JTextField();
		txtRefseqFile.setBounds(437, 423, 233, 18);
		add(txtRefseqFile);
		txtRefseqFile.setColumns(10);
		
		txtRfamRaw = new JTextField();
		txtRfamRaw.setBounds(437, 396, 233, 18);
		add(txtRfamRaw);
		txtRfamRaw.setColumns(10);
		
		btnRfamfile = new JButton("RfamFile");
		btnRfamfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt", "");
				txtRfamRaw.setText(fileName);
			}
		});
		btnRfamfile.setBounds(700, 390, 96, 24);
		add(btnRfamfile);
		
		btnRefseqfile = new JButton("RefseqFile");
		btnRefseqfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt", "");
				txtRefseqFile.setText(fileName);
			}
		});
		btnRefseqfile.setBounds(698, 420, 110, 24);
		add(btnRefseqfile);
		
		txtOutPathPrefix = new JTextField();
		txtOutPathPrefix.setBounds(439, 470, 233, 18);
		add(txtOutPathPrefix);
		txtOutPathPrefix.setColumns(10);
		
		JLabel lblOutpathprefix = new JLabel("OutPathPrefix");
		lblOutpathprefix.setBounds(440, 447, 105, 20);
		add(lblOutpathprefix);
		
		btnOutpath = new JButton("OutPath");
		btnOutpath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileName("prefix", "");
				txtOutPathPrefix.setText(fileName);
			}
		});
		btnOutpath.setBounds(709, 465, 95, 24);
		add(btnOutpath);
		
		chkbxExtractSeq = new JCheckBox("extract seq");
		chkbxExtractSeq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkAnalysis.setSelected(false);
				chkMapping.setSelected(false);
				chkPredictMiRNA.setSelected(false);
				chkSelected(false, false, false, chkbxExtractSeq.isSelected());

			}
		});
		chkbxExtractSeq.setBounds(534, 527, 110, 22);
		add(chkbxExtractSeq);
		
		lblRfamregx = new JLabel("RfamRegx");
		lblRfamregx.setBounds(435, 373, 92, 14);
		add(lblRfamregx);
		
		txtRfamRegx = new JTextField();
		txtRfamRegx.setBounds(554, 371, 114, 18);
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
		btnFastqfile.setBounds(257, 6, 118, 24);
		add(btnFastqfile);
		
		sclpanFastq = new JScrollPaneData();
		sclpanFastq.setBounds(22, 10, 228, 60);
		add(sclpanFastq);
		
		txtRepeatGff = new JTextField();
		txtRepeatGff.setBounds(442, 105, 233, 18);
		add(txtRepeatGff);
		txtRepeatGff.setColumns(10);
		
		btnRepeatgff = new JButton("RepeatGff");
		btnRepeatgff.setBounds(705, 100, 118, 24);
		add(btnRepeatgff);
		
		sclNovelMiRNAbed = new JScrollPaneData();
		sclNovelMiRNAbed.setBounds(442, 152, 233, 104);
		add(sclNovelMiRNAbed);
		
		btnDelFastQfilerow = new JButton("DelRow");
		btnDelFastQfilerow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclpanFastq.removeSelRows();
			}
		});
		btnDelFastQfilerow.setBounds(257, 42, 118, 24);
		add(btnDelFastQfilerow);
		
		btnNovelmirnabed = new JButton("NovelMiRNABed");
		btnNovelmirnabed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("bed file", "");
				for (String string : lsFileName) {
					sclNovelMiRNAbed.addProview(new String[]{string});
				}
				
			}
		});
		btnNovelmirnabed.setBounds(705, 152, 145, 24);
		add(btnNovelmirnabed);
		
		btnDelNovelMiRNAbedFileRow = new JButton("DelRow");
		btnDelNovelMiRNAbedFileRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclNovelMiRNAbed.removeSelRows();
			}
		});
		btnDelNovelMiRNAbedFileRow.setBounds(705, 233, 118, 24);
		add(btnDelNovelMiRNAbedFileRow);
		
		chkPredictMiRNA = new JCheckBox("PredictMiRNA");
		chkPredictMiRNA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkbxExtractSeq.setSelected(false);
				if (!chkMapping.isSelected()) {
					chkAnalysis.setSelected(false);
				}
				chkSelected(chkMapping.isSelected(), chkAnalysis.isSelected(), chkPredictMiRNA.isSelected(), false);
				
			}
		});
		chkPredictMiRNA.setBounds(369, 527, 131, 22);
		add(chkPredictMiRNA);
		initialize();
	}
	
	
	private void chkSelected(boolean booChkMap, boolean booChkAnalysis, boolean booChkPredictMiRNA, boolean booChkExtractSeq) {
		if (booChkMap && !booChkAnalysis && !booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsMapping)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		else if (booChkMap && booChkAnalysis && !booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsMapping)
				component.setEnabled(true);
			for (Component component : lsComponentsMappingAndAnalysis)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		else if (booChkMap && booChkAnalysis && booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsMapping)
				component.setEnabled(true);
			for (Component component : lsComponentsMappingAndAnalysis)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		else if (!booChkMap && booChkAnalysis && !booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsAnalysis)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		else if (!booChkMap && booChkAnalysis && booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsAnalysis)
				component.setEnabled(true);
			for (Component component : lsComponentsAnalysisPredictMiRNA)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		else if (!booChkMap && !booChkAnalysis && booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsPredictMiRNA)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		
		else if (booChkExtractSeq) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsExtractSeq)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		else if (!booChkMap && !booChkAnalysis && !booChkExtractSeq && !booChkPredictMiRNA) {
			for (Component component : lsComponentsAll) {
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
		lsComponentsMapping.add(txtRfamSeq);
		lsComponentsMapping.add(txtGenomeSeq);
		lsComponentsMapping.add(txtGffGeneFile);
		lsComponentsMapping.add(txtChromPath);
		lsComponentsMapping.add(txtRfamInfo);
		lsComponentsMapping.add(sclpanFastq);
		
		lsComponentsMapping.add(btnDelFastQfilerow);
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
		lsComponentsAnalysis.add(txtRepeatGff);
		
		lsComponentsAnalysis.add(btnHairpinMiRNA);
		lsComponentsAnalysis.add(btnMirnabed);
		lsComponentsAnalysis.add(btnRfambed);
		lsComponentsAnalysis.add(btnNCRNAbed);
		lsComponentsAnalysis.add(combFileType);
		lsComponentsAnalysis.add(combSpecies);
		lsComponentsAnalysis.add(btnRnadata);
		lsComponentsAnalysis.add(btnMatureMiRNA);
		lsComponentsAnalysis.add(btnRepeatgff);

		
		lsComponentsExtractSeq.add(txtHairPinRaw);
		lsComponentsExtractSeq.add(txtHairRegx);
		lsComponentsExtractSeq.add(txtRfamRegx);
		lsComponentsExtractSeq.add(txtRefseqFile);
		lsComponentsExtractSeq.add(txtRfamRaw);
		
		lsComponentsExtractSeq.add(btnHairepinfile);
		lsComponentsExtractSeq.add(btnRfamfile);
		lsComponentsExtractSeq.add(btnRefseqfile);
		
		
		lsComponentsPredictMiRNA.add(btnDelNovelMiRNAbedFileRow);
		lsComponentsPredictMiRNA.add(btnNovelmirnabed);
		lsComponentsPredictMiRNA.add(sclNovelMiRNAbed);
		lsComponentsPredictMiRNA.add(txtGffGeneFile);
		lsComponentsPredictMiRNA.add(txtChromPath);
		lsComponentsPredictMiRNA.add(btnGfffile);
		lsComponentsPredictMiRNA.add(btnChrompath);
		
		lsComponentsMappingAndAnalysis.add(combFileType);
		lsComponentsMappingAndAnalysis.add(combSpecies);
		lsComponentsMappingAndAnalysis.add(txtRNAdataFile);
		lsComponentsMappingAndAnalysis.add(btnRnadata);
		lsComponentsMappingAndAnalysis.add(btnMatureMiRNA);
		lsComponentsMappingAndAnalysis.add(txtMatureMiRNA);
		
		lsComponentsAnalysisPredictMiRNA.add(txtGffGeneFile);
		lsComponentsAnalysisPredictMiRNA.add(txtChromPath);
		lsComponentsAnalysisPredictMiRNA.add(btnGfffile);
		lsComponentsAnalysisPredictMiRNA.add(btnChrompath);
		
		
		lsComponentsAll.addAll(lsComponentsAnalysis);
		lsComponentsAll.addAll(lsComponentsExtractSeq);
		lsComponentsAll.addAll(lsComponentsMapping);
		lsComponentsAll.addAll(lsComponentsMappingAndAnalysis);
		lsComponentsAll.addAll(lsComponentsPredictMiRNA);
		
		
		
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
		sclpanFastq.setTitle(new String[]{"FastqFile"});
		sclNovelMiRNAbed.setTitle(new String[]{"BedFile"});
		HashMap<String, Integer> hashFileType = new HashMap<String, Integer>();
		hashFileType.put("miReapFile",ListMiRNALocation.TYPE_MIREAP);
		hashFileType.put("miReapFile",ListMiRNALocation.TYPE_RNA_DATA);
		combFileType.setItemHash(hashFileType);
		
		HashMap<String, String> hashSpecies = new HashMap<String, String>();
		hashSpecies.put("human", "HSA");
//		hashSpecies.put("miReapFile",ListMiRNALocation.TYPE_RNA_DATA);
		combSpecies.setItemHash(hashSpecies);
	}
	
	
	private void running() {
		if (chkMapping.isSelected()) {
			ArrayList<String> lsPredictBed = new ArrayList<String>();
			ArrayList<String[]> lsFile = sclpanFastq.getLsDataInfo();
			for (String[] strings : lsFile) {
				if (!FileOperate.isFileExist(strings[0])) {
					continue;
				}
				runMapping(strings[0]);
				String bedFile = ctrlMiRNA.getGenomeBed();
				if (FileOperate.isFileExist(bedFile) && FileOperate.getFileSize(bedFile) > 1000) {
					lsPredictBed.add(bedFile);
				}
				if (chkAnalysis.isSelected()) {
					runCount(false);
				}
			}
			if (chkPredictMiRNA.isSelected()) {
				ctrlMiRNA.setLsBedFile(lsPredictBed);
				runPredict(false);
			}
		}
		else if (!chkMapping.isSelected() && chkAnalysis.isSelected()) {
			runCount(true);
		}
		else if (!chkMapping.isSelected() && chkPredictMiRNA.isSelected()) {
			runPredict(true);
		}
	}
	
	private void runMapping(String fastqFile) {
		ctrlMiRNA = new CtrlMiRNA();
		ctrlMiRNA.setOutPathPrix(txtOutPathPrefix.getText() + FileOperate.getFileNameSep(fastqFile)[0]);
		ctrlMiRNA.setFastqFile(fastqFile);
		ctrlMiRNA.setGenome(chkMapAllBedFileToGenome.isSelected(), txtGenomeSeq.getText());
		ctrlMiRNA.setMirnaFile(txtMatureMiRNA.getText());
		ctrlMiRNA.setMiRNAinfo(combFileType.getSelectedValue(), combSpecies.getSelectedValue(), txtRNAdataFile.getText());
		ctrlMiRNA.setRefFile(txtHairpineMiRNA.getText(),txtRfamSeq.getText(), txtNCRNA.getText());
		ctrlMiRNA.mapping();
	}
	
	private void runPredict(boolean solo) {
		if (solo) {
			ctrlMiRNA = new CtrlMiRNA();
			ctrlMiRNA.setOutPathPrix(txtOutPathPrefix.getText());
			ArrayList<String[]> lsBedFileArray = sclNovelMiRNAbed.getLsDataInfo();
			ArrayList<String> lsBedFile = new ArrayList<String>();
			for (String[] strings : lsBedFileArray) {
				if (FileOperate.isFileExist(strings[0]) && FileOperate.getFileSize(strings[0]) > 1000) {
					lsBedFile.add(strings[0]);
				}
			}
			if (lsBedFile.size() > 0) {
				ctrlMiRNA.setLsBedFile(lsBedFile);
			}
		}
		ctrlMiRNA.setGffInfo(NovelBioConst.GENOME_GFF_TYPE_UCSC, txtGffGeneFile.getText(), txtChromPath.getText());
		ctrlMiRNA.runMiRNApredict();
	}
	/**
	 * 是否单独运行，就是前面是否有mapping
	 * @param solo 前面是否有mapping
	 */
	private void runCount(boolean solo) {
		if (solo) {
			ctrlMiRNA = new CtrlMiRNA();
			ctrlMiRNA.setOutPathPrix(txtOutPathPrefix.getText());
//			ctrlMiRNA.set
		}
		ctrlMiRNA.setMirnaFile(txtMatureMiRNA.getText());
		ctrlMiRNA.setMiRNAinfo(combFileType.getSelectedValue(), combSpecies.getSelectedValue(), txtRNAdataFile.getText());
		ctrlMiRNA.setGffInfo(NovelBioConst.GENOME_GFF_TYPE_UCSC, txtGffGeneFile.getText(), txtChromPath.getText());
		ctrlMiRNA.setRepeat(txtRepeatGff.getText());
		ctrlMiRNA.setRfamFile(txtRfamInfo.getText());
		ctrlMiRNA.exeRunning(solo);
	}
}
