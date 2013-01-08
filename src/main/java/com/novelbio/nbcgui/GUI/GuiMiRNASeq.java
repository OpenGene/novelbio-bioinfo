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

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.mirna.CtrlMiRNAfastq;
import com.novelbio.analysis.seq.mirna.CtrlMiRNApredict;
import com.novelbio.analysis.seq.mirna.ListMiRNALocation;
import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.domain.geneanno.SpeciesFile.ExtractSmallRNASeq;
import com.novelbio.database.model.species.Species;
import javax.swing.JComboBox;

public class GuiMiRNASeq extends JPanel{
	private static final long serialVersionUID = -5940420720636777182L;
	private JFrame frame;
	private JTextField txtRefseqFile;
	private JTextField txtOutPathPrefix;
	JCheckBox chkMapAllBedFileToGenome;
	JComboBoxData<Species> combSpecies;
	JComboBoxData<String> comboVersion;
	
	JButton btnRunning;
	JCheckBox chkMapping;
	JButton btnRefseqfile;
	JButton btnOutpath;
	JCheckBox chkbxExtractSeq;
	JScrollPaneData sclpanFastq;
	JCheckBox chkPredictMiRNA;
	JScrollPaneData sclNovelMiRNAbed;
	JButton btnDelFastQfilerow;
	
	ArrayList<Component> lsComponentsMapping = new ArrayList<Component>();
	ArrayList<Component> lsComponentsExtractSeq = new ArrayList<Component>();
	ArrayList<Component> lsComponentsPredictMiRNA = new ArrayList<Component>();
	ArrayList<Component> lsComponentsAnalysisPredictMiRNA = new ArrayList<Component>();
	HashSet<Component> lsComponentsAll = new HashSet<Component>();
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlMiRNAfastq ctrlMiRNAfastq = new CtrlMiRNAfastq();
	CtrlMiRNApredict ctrlMiRNApredict = new CtrlMiRNApredict();

	private JButton btnFastqfile;
	private JButton btnNovelmirnabed;
	private JButton btnDelNovelMiRNAbedFileRow;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiMiRNASeq window = new GuiMiRNASeq();
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
	public GuiMiRNASeq() {
		setLayout(null);

		//是否将全部的bed文件mapping至基因组上，用于看基因组上的reads分布
		chkMapAllBedFileToGenome = new JCheckBox("mapping all bedFile to Genome");
		chkMapAllBedFileToGenome.setBounds(23, 310, 245, 22);
		add(chkMapAllBedFileToGenome);

		
		btnRunning = new JButton("Running");
		btnRunning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				running();
			}
		});
		btnRunning.setBounds(705, 526, 92, 24);
		add(btnRunning);
		
		chkMapping = new JCheckBox("MappingAndAnalysis");
		chkMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!chkMapping.isSelected()) {
					chkPredictMiRNA.setSelected(false);
				}
				chkbxExtractSeq.setSelected(false);
				chkSelected(chkMapping.isSelected(), chkPredictMiRNA.isSelected(), false);
			}
		});
		chkMapping.setBounds(79, 527, 206, 22);
		add(chkMapping);
		
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
				chkMapping.setSelected(false);
				chkPredictMiRNA.setSelected(false);
				chkSelected(false, false, chkbxExtractSeq.isSelected());

			}
		});
		chkbxExtractSeq.setBounds(534, 527, 110, 22);
		add(chkbxExtractSeq);
		
		btnFastqfile = new JButton("FastqFile");
		btnFastqfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("fastq", "");
				for (String string : lsFileName) {
					String prefix = FileOperate.getFileNameSep(string)[0].split("_")[0];
					sclpanFastq.addItem(new String[]{string, prefix});
				}
			}
		});
		btnFastqfile.setBounds(427, 16, 118, 24);
		add(btnFastqfile);
		
		sclpanFastq = new JScrollPaneData();
		sclpanFastq.setBounds(22, 20, 393, 265);
		add(sclpanFastq);
		
		sclNovelMiRNAbed = new JScrollPaneData();
		sclNovelMiRNAbed.setBounds(582, 16, 252, 271);
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
					sclNovelMiRNAbed.addItem(new String[]{string, FileOperate.getFileNameSep(string)[0]});
				}
			}
		});
		btnNovelmirnabed.setBounds(846, 16, 145, 24);
		add(btnNovelmirnabed);
		
		btnDelNovelMiRNAbedFileRow = new JButton("DelRow");
		btnDelNovelMiRNAbedFileRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclNovelMiRNAbed.deleteSelRows();
			}
		});
		btnDelNovelMiRNAbedFileRow.setBounds(846, 52, 118, 24);
		add(btnDelNovelMiRNAbedFileRow);
		
		chkPredictMiRNA = new JCheckBox("PredictMiRNA");
		chkPredictMiRNA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkbxExtractSeq.setSelected(false);
				chkSelected(chkMapping.isSelected(), chkPredictMiRNA.isSelected(), false);
			}
		});
		chkPredictMiRNA.setBounds(315, 527, 131, 22);
		add(chkPredictMiRNA);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(23, 350, 69, 14);
		add(lblSpecies);
		
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
	
	
	private void chkSelected(boolean booChkMap, boolean booChkPredictMiRNA, boolean booChkExtractSeq) {
		if (booChkMap && !booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsMapping)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		else if (booChkMap && booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsMapping)
				component.setEnabled(true);
			btnRunning.setEnabled(true);
		}
		else if (!booChkMap && !booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			btnRunning.setEnabled(true);
		}
		else if (!booChkMap && booChkPredictMiRNA) {
			for (Component component : lsComponentsAll)
				component.setEnabled(false);
			for (Component component : lsComponentsAnalysisPredictMiRNA)
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
		else if (!booChkMap && !booChkExtractSeq && !booChkPredictMiRNA) {
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
		lsComponentsMapping.add(combSpecies);
		
		lsComponentsExtractSeq.add(txtRefseqFile);
		lsComponentsExtractSeq.add(btnRefseqfile);
		
		lsComponentsPredictMiRNA.add(btnDelNovelMiRNAbedFileRow);
		lsComponentsPredictMiRNA.add(btnNovelmirnabed);
		lsComponentsPredictMiRNA.add(sclNovelMiRNAbed);
		lsComponentsPredictMiRNA.add(combSpecies);
		lsComponentsPredictMiRNA.add(comboVersion);
				

		
		lsComponentsAll.addAll(lsComponentsExtractSeq);
		lsComponentsAll.addAll(lsComponentsMapping);
		lsComponentsAll.addAll(lsComponentsPredictMiRNA);

		chkMapping.setSelected(true);
		for (Component component : lsComponentsExtractSeq) {
			component.setEnabled(false);
		}
		for (Component component : lsComponentsMapping) {
			component.setEnabled(true);
		}
		chkbxExtractSeq.setSelected(false);
		sclpanFastq.setTitle(new String[]{"FastqFile", "prefix"});
		sclNovelMiRNAbed.setTitle(new String[]{"BedFile", "prefix"});
		HashMap<String, Integer> hashFileType = new HashMap<String, Integer>();
		hashFileType.put("miReapFile",ListMiRNALocation.TYPE_MIREAP);
		hashFileType.put("miRNAdata",ListMiRNALocation.TYPE_RNA_DATA);
		
		HashMap<String, Species> hashSpecies = Species.getSpeciesName2Species(Species.SEQINFO_SPECIES);           // new HashMap<String, String>();
//		hashSpecies.put("miReapFile",ListMiRNALocation.TYPE_RNA_DATA);
		combSpecies.setMapItem(hashSpecies);
	}
	
	
	private void running() {
		Species species = combSpecies.getSelectedValue();
		species.setVersion(comboVersion.getSelectedValue());
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		ctrlMiRNAfastq.clear();
		ArrayList<String[]> lsBedFile2Prefix = new ArrayList<String[]>();
		if (chkMapping.isSelected()) {
			runMapping(gffChrAbs, species, sclpanFastq.getLsDataInfo());
		
			lsBedFile2Prefix = ctrlMiRNAfastq.getLsGenomeBed2Prefix();
		}
		if (chkPredictMiRNA.isSelected()) {
			//如果没有mapping，则取输入的bed文件
			if (lsBedFile2Prefix.size() == 0) {
				lsBedFile2Prefix = sclNovelMiRNAbed.getLsDataInfo();
			}
			runPredict(lsBedFile2Prefix, gffChrAbs, species);
		}
		
		if (chkbxExtractSeq.isSelected()) {
			runExtractSeq();
		}
	}
	
	private void runMapping(GffChrAbs gffChrAbs, Species species, ArrayList<String[]> lsfastqFile2Prefix) {
		ctrlMiRNAfastq.setMappingAll2Genome(chkbxExtractSeq.isSelected());
		ctrlMiRNAfastq.setSpecies(species);
		ctrlMiRNAfastq.setOutPath(txtOutPathPrefix.getText());
		ctrlMiRNAfastq.setGffChrAbs(gffChrAbs);
		ctrlMiRNAfastq.setLsFastqFile(lsfastqFile2Prefix);
		ctrlMiRNAfastq.setMiRNAinfo(PathDetail.getMiRNADat());
		ctrlMiRNAfastq.setRfamFile(PathDetail.getRfamTab());
		ctrlMiRNAfastq.mappingAndCounting();
		ctrlMiRNAfastq.writeToFile();
	}
	
	private void runPredict(ArrayList<String[]> lsBedFile2Prefix, GffChrAbs gffChrAbs, Species species) {
		ctrlMiRNApredict.setGffChrAbs(gffChrAbs);
		ctrlMiRNApredict.setSpecies(species);
		ctrlMiRNApredict.setLsBedFile2Prefix(lsBedFile2Prefix);
		ctrlMiRNApredict.setOutPath(txtOutPathPrefix.getText());
		
		ctrlMiRNApredict.runMiRNApredict();
		ctrlMiRNApredict.writeToFile();
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
