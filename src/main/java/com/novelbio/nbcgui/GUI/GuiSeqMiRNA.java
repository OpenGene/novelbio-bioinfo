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
import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.domain.geneanno.SpeciesFile.ExtractSmallRNASeq;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controlseq.CtrlMiRNA;
import javax.swing.JComboBox;

public class GuiSeqMiRNA extends JPanel{
	private static final long serialVersionUID = -5940420720636777182L;
	private JFrame frame;
	private JTextField txtMiRNAbed;
	private JTextField txtRfamBed;
	private JTextField txtNCRNAbed;
	private JTextField txtRefseqFile;
	private JTextField txtOutPathPrefix;
	JCheckBox chkMapAllBedFileToGenome;
	JComboBoxData<Integer> combFileType;
	JComboBoxData<Species> combSpecies;
	JComboBoxData<String> comboVersion;
	
	JButton btnRunning;
	JCheckBox chkMapping;
	JButton btnMirnabed;
	JButton btnRfambed;
	JButton btnNCRNAbed;
	JCheckBox chkAnalysis;
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

		//是否将全部的bed文件mapping至基因组上，用于看基因组上的reads分布
		chkMapAllBedFileToGenome = new JCheckBox("mapping all bedFile to Genome");
		chkMapAllBedFileToGenome.setBounds(23, 310, 245, 22);
		add(chkMapAllBedFileToGenome);
		
		combFileType = new JComboBoxData<Integer>();
		combFileType.setBounds(23, 496, 173, 23);
		add(combFileType);

		
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
		txtMiRNAbed.setBounds(582, 19, 233, 18);
		add(txtMiRNAbed);
		txtMiRNAbed.setColumns(10);
		
		btnMirnabed = new JButton("MiRNABed");
		btnMirnabed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/bed", "");
				txtMiRNAbed.setText(fileName);
			}
		});
		btnMirnabed.setBounds(846, 12, 105, 24);
		add(btnMirnabed);
		
		txtRfamBed = new JTextField();
		txtRfamBed.setBounds(582, 49, 233, 18);
		add(txtRfamBed);
		txtRfamBed.setColumns(10);
		
		btnRfambed = new JButton("RfamBed");
		btnRfambed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/fasta", "");
				txtRfamBed.setText(fileName);
			}
		});
		btnRfambed.setBounds(846, 42, 98, 24);
		add(btnRfambed);
		
		txtNCRNAbed = new JTextField();
		txtNCRNAbed.setBounds(582, 79, 233, 18);
		add(txtNCRNAbed);
		txtNCRNAbed.setColumns(10);
		
		btnNCRNAbed = new JButton("NCRNAbed");
		btnNCRNAbed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("txt/bed", "");
				txtNCRNAbed.setText(fileName);
			}
		});
		btnNCRNAbed.setBounds(846, 72, 108, 24);
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
		
		txtRefseqFile = new JTextField();
		txtRefseqFile.setBounds(437, 423, 233, 18);
		add(txtRefseqFile);
		txtRefseqFile.setColumns(10);
		
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
				String fileName = guiFileOpen.saveFileNameAndPath("prefix", "");
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
		
		btnFastqfile = new JButton("FastqFile");
		btnFastqfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("fastq", "");
				for (String string : lsFileName) {
					sclpanFastq.addItem(new String[]{string});
				}
			}
		});
		btnFastqfile.setBounds(427, 16, 118, 24);
		add(btnFastqfile);
		
		sclpanFastq = new JScrollPaneData();
		sclpanFastq.setBounds(22, 20, 393, 265);
		add(sclpanFastq);
		
		sclNovelMiRNAbed = new JScrollPaneData();
		sclNovelMiRNAbed.setBounds(582, 168, 237, 119);
		add(sclNovelMiRNAbed);
		
		btnDelFastQfilerow = new JButton("DelRow");
		btnDelFastQfilerow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclpanFastq.deleteSelRows();
			}
		});
		btnDelFastQfilerow.setBounds(427, 61, 118, 24);
		add(btnDelFastQfilerow);
		
		btnNovelmirnabed = new JButton("NovelMiRNABed");
		btnNovelmirnabed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("bed file", "");
				for (String string : lsFileName) {
					sclNovelMiRNAbed.addItem(new String[]{string});
				}
				
			}
		});
		btnNovelmirnabed.setBounds(846, 164, 145, 24);
		add(btnNovelmirnabed);
		
		btnDelNovelMiRNAbedFileRow = new JButton("DelRow");
		btnDelNovelMiRNAbedFileRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclNovelMiRNAbed.deleteSelRows();
			}
		});
		btnDelNovelMiRNAbedFileRow.setBounds(846, 265, 118, 24);
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
		lblFiletype.setBounds(23, 474, 69, 14);
		add(lblFiletype);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(23, 350, 69, 14);
		add(lblSpecies);
		
		txtGenomeBed = new JTextField();
		txtGenomeBed.setBounds(582, 114, 237, 18);
		add(txtGenomeBed);
		txtGenomeBed.setColumns(10);
		
		btnGenomeBed = new JButton("GenomeBed");
		btnGenomeBed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("bed", "");
				txtGenomeBed.setText(fileName);
			}
		});
		btnGenomeBed.setBounds(846, 108, 138, 24);
		add(btnGenomeBed);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(23, 401, 69, 14);
		add(lblVersion);
		
		combSpecies = new JComboBoxData<Species>();
		combSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Species species = combSpecies.getSelectedValue();
				comboVersion.setMapItem(species.getMapVersion());
			}
		});
		combSpecies.setBounds(23, 366, 195, 23);
		add(combSpecies);
		
		
		comboVersion = new JComboBoxData<String>();
		comboVersion.setBounds(23, 421, 195, 23);
		add(comboVersion);
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
		lsComponentsMapping.add(sclpanFastq);
		
		lsComponentsMapping.add(btnDelFastQfilerow);
		lsComponentsMapping.add(chkMapAllBedFileToGenome);
		lsComponentsMapping.add(btnFastqfile);
		lsComponentsMapping.add(combSpecies);
		

		lsComponentsAnalysis.add(txtMiRNAbed);
		lsComponentsAnalysis.add(txtRfamBed);
		lsComponentsAnalysis.add(txtNCRNAbed);
		lsComponentsAnalysis.add(txtGenomeBed);
		
		lsComponentsAnalysis.add(btnMirnabed);
		lsComponentsAnalysis.add(btnRfambed);
		lsComponentsAnalysis.add(btnNCRNAbed);
		lsComponentsAnalysis.add(combFileType);
		lsComponentsAnalysis.add(combSpecies);
		lsComponentsAnalysis.add(btnGenomeBed);
		lsComponentsExtractSeq.add(txtRefseqFile);
		lsComponentsExtractSeq.add(btnRefseqfile);
		
		lsComponentsPredictMiRNA.add(btnDelNovelMiRNAbedFileRow);
		lsComponentsPredictMiRNA.add(btnNovelmirnabed);
		lsComponentsPredictMiRNA.add(sclNovelMiRNAbed);
		lsComponentsPredictMiRNA.add(combSpecies);
		lsComponentsPredictMiRNA.add(comboVersion);
				
		lsComponentsMappingAndAnalysis.add(combFileType);
		lsComponentsMappingAndAnalysis.add(combSpecies);
		
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
		combFileType.setMapItem(hashFileType);
		
		HashMap<String, Species> hashSpecies = Species.getSpeciesName2Species(Species.SEQINFO_SPECIES);           // new HashMap<String, String>();
//		hashSpecies.put("miReapFile",ListMiRNALocation.TYPE_RNA_DATA);
		combSpecies.setMapItem(hashSpecies);
	}
	
	
	private void running() {
		Species species = combSpecies.getSelectedValue();
		species.setVersion(comboVersion.getSelectedValue());
		if (chkMapping.isSelected()) {
			ArrayList<String> lsPredictBed = new ArrayList<String>();
			ArrayList<String[]> lsFile = sclpanFastq.getLsDataInfo();
			for (String[] strings : lsFile) {
				if (!FileOperate.isFileExist(strings[0])) {
					continue;
				}
				runMapping(species, strings[0]);
				String bedFile = ctrlMiRNA.getGenomeBed();
				if (FileOperate.isFileExist(bedFile) && FileOperate.getFileSize(bedFile) > 1000) {
					lsPredictBed.add(bedFile);
				}
				if (chkAnalysis.isSelected()) {
					runCount(species, false);
				}
			}
			if (chkPredictMiRNA.isSelected()) {
				ctrlMiRNA.setLsBedFile(lsPredictBed);
				runPredict(species, false);
			}
		}
		else if (!chkMapping.isSelected() && chkAnalysis.isSelected()) {
			runCount(species, true);
		}
		else if (!chkMapping.isSelected() && chkPredictMiRNA.isSelected()) {
			runPredict(species, true);
		}
		else if (chkbxExtractSeq.isSelected()) {
			runExtractSeq();
		}
	}
	
	private void runMapping(Species species, String fastqFile) {
		String[] outPrefix = getTxtOutPathAndPrefix();

		ctrlMiRNA = new CtrlMiRNA();
		ctrlMiRNA.setSpecies(species);
		ctrlMiRNA.setOutPath(outPrefix[0] + FileOperate.getFileNameSep(fastqFile)[0] , outPrefix[1]);
		ctrlMiRNA.setFastqFile(fastqFile);
		ctrlMiRNA.setGenome(chkMapAllBedFileToGenome.isSelected());
		ctrlMiRNA.setMiRNAinfo(combFileType.getSelectedValue(), PathDetail.getMiRNADat());
		ctrlMiRNA.mapping();
	}
	
	private void runPredict(Species species, boolean solo) {
		String[] outPrefix = getTxtOutPathAndPrefix();
		if (solo) {
			ctrlMiRNA = new CtrlMiRNA();
			ctrlMiRNA.setSpecies(species);
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
		ctrlMiRNA.setOutPath(outPrefix[0], outPrefix[1]);
		ctrlMiRNA.runMiRNApredict();
	}
	/**
	 * 是否单独运行，就是前面是否有mapping
	 * @param species
	 * @param solo 前面是否有mapping
	 */
	private void runCount(Species species, boolean solo) {
		if (solo) {
			String[] outPrefix = getTxtOutPathAndPrefix();
			ctrlMiRNA = new CtrlMiRNA();
			ctrlMiRNA.setSpecies(species);
			ctrlMiRNA.setOutPath(outPrefix[0], outPrefix[1]);
		}
		ctrlMiRNA.setMiRNAinfo(combFileType.getSelectedValue(), PathDetail.getMiRNADat());
		ctrlMiRNA.setBedFileCountMiRNA(txtMiRNAbed.getText(),  txtGenomeBed.getText(), txtRfamBed.getText(), txtNCRNAbed.getText());
		ctrlMiRNA.setRfamFile(PathDetail.getRfamTab());
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
		if (FileOperate.isFileDirectory(out) || out.endsWith("\\|/")) {
			result[0] = "";
			result[1] = out;
			return result;
		}
		result[0] = FileOperate.getFileName(out);
		result[1] = FileOperate.getParentPathName(out);
		return result;
	}
	/**
	 * 是否单独运行，就是前面是否有mapping
	 * @param solo 前面是否有mapping
	 */
	private void runExtractSeq() {
		ExtractSmallRNASeq extractSmallRNASeq = new ExtractSmallRNASeq();
		extractSmallRNASeq.setRefseqFile(txtRefseqFile.getText());
		extractSmallRNASeq.setOutPathPrefix(txtOutPathPrefix.getText());
		extractSmallRNASeq.getSeq();
	}
}
