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

import com.novelbio.analysis.seq.mirna.ListMiRNALocation;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controlseq.CtrlMiRNA;

public class GuiSeqMiRNA extends JPanel{
	private static final long serialVersionUID = -5940420720636777182L;
	private JFrame frame;
	private JTextField txtRfamInfo;
	private JTextField txtRNAdataFile;
	private JTextField txtMiRNAbed;
	private JTextField txtRfamBed;
	private JTextField txtNCRNAbed;
	private JTextField txtHairPinRaw;
	private JTextField txtHairRegx;
	private JTextField txtRefseqFile;
	private JTextField txtRfamRaw;
	private JTextField txtOutPathPrefix;
	JCheckBox chkMapAllBedFileToGenome;
	JButton btnRfaminfo;
	JComboBoxData<Integer> combFileType;
	JComboBoxData<Integer> combSpecies;
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
	JButton btnGenomeBed;
	JCheckBox chkbxExtractSeq;
	JScrollPaneData sclpanFastq;
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
	private JButton btnNovelmirnabed;
	private JButton btnDelNovelMiRNAbedFileRow;
	private JTextField txtGenomeBed;
	
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
		
		JLabel lblSettingGenomeIndex = new JLabel("Setting Genome Index");
		lblSettingGenomeIndex.setBounds(24, 169, 159, 14);
		add(lblSettingGenomeIndex);

		//是否将全部的bed文件mapping至基因组上，用于看基因组上的reads分布
		chkMapAllBedFileToGenome = new JCheckBox("mapping all bedFile to Genome");
		chkMapAllBedFileToGenome.setBounds(21, 187, 245, 22);
		add(chkMapAllBedFileToGenome);
		
		txtRfamInfo = new JTextField();
		txtRfamInfo.setBounds(23, 226, 219, 18);
		add(txtRfamInfo);
		txtRfamInfo.setColumns(10);
		
		btnRfaminfo = new JButton("RfamInfo");
		btnRfaminfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtRfamInfo.setText(fileName);
			}
		});
		btnRfaminfo.setBounds(254, 224, 128, 24);
		add(btnRfaminfo);
		
		combFileType = new JComboBoxData<Integer>();
		combFileType.setBounds(22, 440, 166, 23);
		add(combFileType);
		
		combSpecies = new JComboBoxData<Integer>();
		combSpecies.setBounds(24, 323, 173, 23);
		add(combSpecies);
		
		txtRNAdataFile = new JTextField();
		txtRNAdataFile.setBounds(23, 478, 247, 18);
		add(txtRNAdataFile);
		txtRNAdataFile.setColumns(10);
		
		btnRnadata = new JButton("RNAdata");
		btnRnadata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtRNAdataFile.setText(fileName);
			}
		});
		btnRnadata.setBounds(294, 475, 96, 24);
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
		btnFastqfile.setBounds(258, 12, 118, 24);
		add(btnFastqfile);
		
		sclpanFastq = new JScrollPaneData();
		sclpanFastq.setBounds(22, 10, 228, 143);
		add(sclpanFastq);
		
		sclNovelMiRNAbed = new JScrollPaneData();
		sclNovelMiRNAbed.setBounds(429, 169, 237, 119);
		add(sclNovelMiRNAbed);
		
		btnDelFastQfilerow = new JButton("DelRow");
		btnDelFastQfilerow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclpanFastq.removeSelRows();
			}
		});
		btnDelFastQfilerow.setBounds(258, 126, 118, 24);
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
		btnNovelmirnabed.setBounds(689, 168, 145, 24);
		add(btnNovelmirnabed);
		
		btnDelNovelMiRNAbedFileRow = new JButton("DelRow");
		btnDelNovelMiRNAbedFileRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclNovelMiRNAbed.removeSelRows();
			}
		});
		btnDelNovelMiRNAbedFileRow.setBounds(697, 262, 118, 24);
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
		
		JLabel lblFiletype = new JLabel("FileType");
		lblFiletype.setBounds(24, 414, 69, 14);
		add(lblFiletype);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(24, 297, 69, 14);
		add(lblSpecies);
		
		txtGenomeBed = new JTextField();
		txtGenomeBed.setBounds(446, 110, 229, 18);
		add(txtGenomeBed);
		txtGenomeBed.setColumns(10);
		
		btnGenomeBed = new JButton("GenomeBed");
		btnGenomeBed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("bed", "");
				txtGenomeBed.setText(fileName);
			}
		});
		btnGenomeBed.setBounds(706, 106, 138, 24);
		add(btnGenomeBed);
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
		
		lsComponentsMapping.add(txtRfamInfo);
		lsComponentsMapping.add(sclpanFastq);
		
		lsComponentsMapping.add(btnDelFastQfilerow);
		lsComponentsMapping.add(chkMapAllBedFileToGenome);
		lsComponentsMapping.add(btnRfaminfo);
		lsComponentsMapping.add(btnFastqfile);
		lsComponentsMapping.add(combSpecies);
		

		lsComponentsAnalysis.add(txtMiRNAbed);
		lsComponentsAnalysis.add(txtRfamBed);
		lsComponentsAnalysis.add(txtNCRNAbed);
		lsComponentsAnalysis.add(txtRNAdataFile);
		lsComponentsAnalysis.add(txtRfamInfo);
		lsComponentsAnalysis.add(txtGenomeBed);
		
		lsComponentsAnalysis.add(btnMirnabed);
		lsComponentsAnalysis.add(btnRfambed);
		lsComponentsAnalysis.add(btnNCRNAbed);
		lsComponentsAnalysis.add(combFileType);
		lsComponentsAnalysis.add(combSpecies);
		lsComponentsAnalysis.add(btnRnadata);
		lsComponentsAnalysis.add(btnRfaminfo);
		lsComponentsAnalysis.add(btnGenomeBed);

		
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
		
		lsComponentsMappingAndAnalysis.add(combFileType);
		lsComponentsMappingAndAnalysis.add(combSpecies);
		lsComponentsMappingAndAnalysis.add(txtRNAdataFile);
		lsComponentsMappingAndAnalysis.add(btnRnadata);		
		
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
		hashFileType.put("miRNAdata",ListMiRNALocation.TYPE_RNA_DATA);
		combFileType.setItemHash(hashFileType);
		
		HashMap<String, Integer> hashSpecies = Species.getSpeciesNameTaxID(false);           // new HashMap<String, String>();
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
		String[] outPrefix = getTxtOutPathAndPrefix();

		ctrlMiRNA = new CtrlMiRNA();
		ctrlMiRNA.setTaxID(combSpecies.getSelectedValue());
		ctrlMiRNA.setOutPath(outPrefix[0] + FileOperate.getFileNameSep(fastqFile)[0] , outPrefix[1]);
		ctrlMiRNA.setFastqFile(fastqFile);
		ctrlMiRNA.setGenome(chkMapAllBedFileToGenome.isSelected());
		ctrlMiRNA.setMiRNAinfo(combFileType.getSelectedValue(), combSpecies.getSelectedValue(), txtRNAdataFile.getText());
		ctrlMiRNA.mapping();
	}
	
	private void runPredict(boolean solo) {
		if (solo) {
			String[] outPrefix = getTxtOutPathAndPrefix();
			ctrlMiRNA = new CtrlMiRNA();
			ctrlMiRNA.setTaxID(combSpecies.getSelectedValue());
			ctrlMiRNA.setOutPath(outPrefix[0], outPrefix[1]);
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
		ctrlMiRNA.runMiRNApredict();
	}
	/**
	 * 是否单独运行，就是前面是否有mapping
	 * @param solo 前面是否有mapping
	 */
	private void runCount(boolean solo) {
		if (solo) {
			String[] outPrefix = getTxtOutPathAndPrefix();
			ctrlMiRNA = new CtrlMiRNA();
			ctrlMiRNA.setTaxID(combSpecies.getSelectedValue());
			ctrlMiRNA.setOutPath(outPrefix[0], outPrefix[1]);
//			ctrlMiRNA.set
		}
		ctrlMiRNA.setMiRNAinfo(combFileType.getSelectedValue(), combSpecies.getSelectedValue(), txtRNAdataFile.getText());
		ctrlMiRNA.setBedFileCountMiRNA(txtMiRNAbed.getText(),  txtGenomeBed.getText(), txtRfamBed.getText(), txtNCRNAbed.getText());
		ctrlMiRNA.setRfamFile(txtRfamInfo.getText());
		ctrlMiRNA.exeRunning(solo);
	}
	/**
	 * 将输出的那个txtprefix分割为outpath和prefix
	 * @return
	 * 1: prefix<br>
	 * 0: path
	 */
	private String[] getTxtOutPathAndPrefix() {
		String[] result = new String[2];
		String out = txtOutPathPrefix.getText();
		if (out.endsWith("\\|/")) {
			result[0] = "";
			result[1] = out;
			return result;
		}
		result[0] = FileOperate.getFileName(out);
		result[1] = FileOperate.getParentPathName(out);
		return result;
	}
}
