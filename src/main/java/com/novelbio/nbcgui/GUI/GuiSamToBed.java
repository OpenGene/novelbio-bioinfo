package com.novelbio.nbcgui.GUI;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GuiSamToBed extends JPanel {
	private JTextField txtExtend;

	JCheckBox chckbxExtend;
	JCheckBox chckbxFilterreads;
	JCheckBox chckbxCis;
	JCheckBox chckbxTrans;
	JCheckBox chckbxSortBed;
	JCheckBox chckbxSortBam;
	JCheckBox chckbxIndex;
	JCheckBox chckbxRealign;
	JButton btnSamtobed;

	JSpinner spinMapNumSmall;
	JSpinner spinMapNumBig;
	
	JScrollPaneData scrlSamFile;
	JScrollPaneData scrlBedFile;
	
	ButtonGroup buttonGroupRad;
	JRadioButton radGenome;
	JRadioButton radRefRNA;
	
	JButton btnRefseqFile;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private JLabel lblTo;
	private JCheckBox chckbxNonUniqueMapping;
	private JComboBoxData<Species> cmbSpecies;
	JComboBoxData<String> cmbVersion;
	private JTextField txtReferenceSequence;
	private JCheckBox chckbxGeneratepileupfile;
	/**
	 * Create the panel.
	 */
	public GuiSamToBed() {
		setLayout(null);
		
		JButton btnNewButton = new JButton("OpenSamFile");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("SamBam", "");
				ArrayList<String[]> lsToScrlFile = new ArrayList<String[]>();
				for (String string : lsFileName) {
					String prefix = FileOperate.getFileNameSep(string)[0].split("_")[0];
					String[] strings = new String[]{string, prefix};
					lsToScrlFile.add(strings);
				}
				scrlSamFile.addItemLs(lsToScrlFile);
			}
		});
		btnNewButton.setBounds(576, 44, 157, 24);
		add(btnNewButton);
		
		JButton btnConvertSam = new JButton("ConvertSam");
		btnConvertSam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxRealign.isSelected() && cmbSpecies.getSelectedValue().getTaxID() == 0) {
					JOptionPane.showConfirmDialog(null, "ReAlign Need the Species Info", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				ArrayList<String[]> lsInfo = scrlSamFile.getLsDataInfo();
				ArrayListMultimap<String, String> mapPrefix2FileName = ArrayListMultimap.create();
				HashSet<String> setTmp = new HashSet<String>();
				for (String[] strings : lsInfo) {
					if (FileOperate.isFileExist(strings[0])) {
						String prefix = getPrefix(strings[1], setTmp);
						mapPrefix2FileName.put(prefix, strings[0]);
					}
				}
				
				for (String prefix : mapPrefix2FileName.keySet()) {
					List<String> lsSamFiles = mapPrefix2FileName.get(prefix);
					convertSamFile(prefix, lsSamFiles);
				}
			}
			
			/** 如果prefix为null或""，则返回一个全新的prefix，意思不在任何分组中 */
			private String getPrefix(String prefixOld, HashSet<String> setTmp) {
				if (prefixOld != null && !prefixOld.equals("")) {
					return prefixOld;
				}
				int i = 0;
				while (setTmp.contains(i + "")) {
					i++;
				}
				return i + "";
			}
		});
		btnConvertSam.setBounds(576, 178, 157, 24);
		add(btnConvertSam);
		
		JLabel lblSamfile = new JLabel("SamToBam");
		lblSamfile.setBounds(12, 21, 121, 14);
		add(lblSamfile);
		
		JButton btnBedFile = new JButton("OpenBedFile");
		btnBedFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					ArrayList<String> lsFileName = guiFileOpen.openLsFileName("bed", "");
					ArrayList<String[]> lsToScrlFile = new ArrayList<String[]>();
					for (String string : lsFileName) {
						String[] strings = new String[]{string};
						lsToScrlFile.add(strings);
					}
					scrlBedFile.addItemLs(lsToScrlFile);
				}
		});
		btnBedFile.setBounds(402, 330, 153, 24);
		add(btnBedFile);
		
		JLabel lblBedfile = new JLabel("BedFile");
		lblBedfile.setBounds(12, 304, 69, 14);
		add(lblBedfile);
		
		txtExtend = new JTextField();
		txtExtend.setBounds(137, 451, 114, 18);
		add(txtExtend);
		txtExtend.setColumns(10);
		
		JButton btnConvertBed = new JButton("ConvertBed");
		btnConvertBed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsInfo = scrlBedFile.getLsDataInfo();
				for (String[] strings : lsInfo) {
					if (FileOperate.isFileExist(strings[0])) {
						convertBedFile(strings[0]);
					}
				}
			}
		});
		btnConvertBed.setBounds(370, 526, 147, 24);
		add(btnConvertBed);
		
		JLabel lblMappingnum = new JLabel("MappingNum");
		lblMappingnum.setBounds(297, 481, 99, 14);
		add(lblMappingnum);
		
		lblTo = new JLabel("To");
		lblTo.setBounds(453, 481, 30, 14);
		add(lblTo);
		
		chckbxNonUniqueMapping = new JCheckBox("Non Unique Mapping Get Random Reads");
		chckbxNonUniqueMapping.setBounds(12, 274, 320, 22);
		add(chckbxNonUniqueMapping);
		
		chckbxExtend = new JCheckBox("Extend");
		chckbxExtend.setBounds(12, 449, 99, 22);
		add(chckbxExtend);
		
		chckbxFilterreads = new JCheckBox("FilterReads");
		chckbxFilterreads.setBounds(12, 477, 131, 22);
		add(chckbxFilterreads);
		
		chckbxCis = new JCheckBox("Cis");
		chckbxCis.setBounds(147, 477, 53, 22);
		add(chckbxCis);
		
		chckbxTrans = new JCheckBox("Trans");
		chckbxTrans.setBounds(214, 477, 69, 22);
		add(chckbxTrans);
		
		chckbxSortBed = new JCheckBox("SortBed");
		chckbxSortBed.setBounds(12, 511, 131, 22);
		add(chckbxSortBed);
		
		chckbxSortBam = new JCheckBox("sortBam");
		chckbxSortBam.setBounds(14, 205, 91, 22);
		add(chckbxSortBam);
		
		chckbxIndex = new JCheckBox("index");
		chckbxIndex.setBounds(191, 204, 69, 22);
		add(chckbxIndex);
		
		chckbxRealign = new JCheckBox("realignRemoveDupRecalibrate");
		chckbxRealign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				selectRealign();
			}
		});
		chckbxRealign.setBounds(12, 223, 256, 22);
		add(chckbxRealign);
		
		btnSamtobed = new JButton("SamtoBed");
		btnSamtobed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsInfo = scrlSamFile.getLsDataInfo();
				for (String[] strings : lsInfo) {
					if (FileOperate.isFileExist(strings[0])) {
						samToBed(strings[0]);
					}
				}
			}
		});
		btnSamtobed.setBounds(402, 273, 118, 24);
		add(btnSamtobed);
		
		spinMapNumSmall = new JSpinner();
		spinMapNumSmall.setBounds(401, 479, 49, 18);
		add(spinMapNumSmall);
		
		spinMapNumBig = new JSpinner();
		spinMapNumBig.setBounds(485, 479, 49, 18);
		add(spinMapNumBig);
		
		scrlBedFile = new JScrollPaneData();
		scrlBedFile.setBounds(12, 330, 384, 109);
		add(scrlBedFile);
		
		JButton btnDelBed = new JButton("DeleteBed");
		btnDelBed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlBedFile.deleteSelRows();
			}
		});
		btnDelBed.setBounds(402, 415, 153, 24);
		add(btnDelBed);
		
		scrlSamFile = new JScrollPaneData();
		scrlSamFile.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getID() == KeyEvent.VK_DELETE) {
					scrlSamFile.deleteSelRows();
				}
			}
		});
		scrlSamFile.setBounds(12, 44, 552, 158);
		add(scrlSamFile);
		
		JButton btnDelScrSam = new JButton("DeleteSam");
		btnDelScrSam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlSamFile.deleteSelRows();
			}
		});
		btnDelScrSam.setBounds(576, 98, 157, 24);
		add(btnDelScrSam);
		
		cmbSpecies = new JComboBoxData<Species>();
		cmbSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectCombSpecies();
			}
		});
		cmbSpecies.setBounds(775, 64, 171, 23);
		add(cmbSpecies);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(778, 43, 69, 14);
		add(lblSpecies);
		
		cmbVersion = new JComboBoxData<String>();
		cmbVersion.setBounds(775, 128, 171, 23);
		add(cmbVersion);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(771, 102, 69, 14);
		add(lblVersion);
		
		txtReferenceSequence = new JTextField();
		txtReferenceSequence.setBounds(772, 246, 231, 18);
		add(txtReferenceSequence);
		txtReferenceSequence.setColumns(10);
		
		btnRefseqFile = new JButton("RefseqFile");
		btnRefseqFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtReferenceSequence.setText(guiFileOpen.openFileName("refseq", ""));
			}
		});
		btnRefseqFile.setBounds(886, 272, 118, 24);
		add(btnRefseqFile);
		
		radGenome = new JRadioButton("Genome");
		radGenome.setBounds(776, 178, 91, 22);
		add(radGenome);
		
		radRefRNA = new JRadioButton("RNA");
		radRefRNA.setBounds(871, 178, 69, 22);
		add(radRefRNA);
		
		chckbxGeneratepileupfile = new JCheckBox("GeneratePileUpFile");
		chckbxGeneratepileupfile.setBounds(297, 223, 187, 22);
		add(chckbxGeneratepileupfile);
		initial();
	}
	
	private void initial() {
		buttonGroupRad = new ButtonGroup();
		buttonGroupRad.add(radGenome);
		buttonGroupRad.add(radRefRNA);
		
		scrlSamFile.setTitle(new String[]{"SamBamFile", "prefix"});
		scrlBedFile.setTitle(new String[]{"BedFile"});
		
		radGenome.setSelected(true);
		
		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		
		selectCombSpecies();
	}
	
	private void selectRealign() {
		if (chckbxRealign.isSelected()) {
			chckbxSortBam.setEnabled(false);
			chckbxIndex.setEnabled(false);
			cmbSpecies.setEnabled(true);
			cmbVersion.setEnabled(true);
			radGenome.setEnabled(true);
			radRefRNA.setEnabled(true);
			txtReferenceSequence.setEnabled(true);
			btnRefseqFile.setEnabled(true);
			selectCombSpecies();
		}
		else {
			chckbxSortBam.setEnabled(true);
			chckbxIndex.setEnabled(true);
			cmbSpecies.setEnabled(false);
			cmbVersion.setEnabled(false);
			radGenome.setEnabled(false);
			radRefRNA.setEnabled(false);
			txtReferenceSequence.setEnabled(false);
			btnRefseqFile.setEnabled(false);		
		}
	}
	
	private void selectCombSpecies() {
		Species species = cmbSpecies.getSelectedValue();
		if (species.getTaxID() == 0) {
			radGenome.setEnabled(false);
			radRefRNA.setEnabled(false);
			cmbVersion.setEnabled(false);
			txtReferenceSequence.setEnabled(true);
			btnRefseqFile.setEnabled(true);
		} 
		else {
			radGenome.setEnabled(true);
			radRefRNA.setEnabled(true);
			cmbVersion.setEnabled(true);
			txtReferenceSequence.setEnabled(false);
			btnRefseqFile.setEnabled(false);
			radGenome.setSelected(true);
			cmbVersion.setMapItem(species.getMapVersion());
		}
	}
	private void samToBed(String samFilestr) {
		SamFile samFile = new SamFile(samFilestr);
		samFile.setUniqueRandomSelectOneRead(chckbxNonUniqueMapping.isSelected());
		samFile.toBedSingleEnd();
	}
	private void convertSamFile(String prefix, List<String> lsSamFilestr) {
		String refFile = "";
		Species species = cmbSpecies.getSelectedValue();
		
		if (species.getTaxID() == 0) {
			refFile = txtReferenceSequence.getText();
		}
		else {
			species.setVersion(cmbVersion.getSelectedValue());
			if (radGenome.isSelected()) {
				refFile = species.getChromSeq();
			}
			else if (radRefRNA.isSelected()) {
				refFile = species.getRefseqFile();
			}
		}
		
		List<SamFile> lsSamFiles = new ArrayList<SamFile>(); 
		for (String string : lsSamFilestr) {
			SamFile samFile = new SamFile(string);
			samFile.setReferenceFileName(refFile);
			lsSamFiles.add(samFile);
		}
		
		SamFile samFileMerge = mergeSamFile(prefix, lsSamFiles);
		if (chckbxSortBam.isSelected()) {
			samFileMerge = samFileMerge.sort();
		}
		if (chckbxIndex.isSelected()) {
			samFileMerge.indexMake();
		}
		if (chckbxRealign.isSelected()) {
			try { samFileMerge = samFileMerge.copeSamFile2Snp(); } catch (Exception e) { }
		}
		if (chckbxGeneratepileupfile.isSelected()) {
			samFileMerge.pileup();
		}
	}
	
	/**
	 * 将输入的文件转化为bam文件，并合并
	 * @param prefix
	 * @param lsSamFile
	 * @return
	 */
	private SamFile mergeSamFile(String prefix, List<SamFile> lsSamFile) {
		if (lsSamFile.size() == 1) {
			return lsSamFile.get(0).convertToBam();
		}
		List<SamFile> lsBamFile = new ArrayList<SamFile>();
		for (SamFile samFile : lsSamFile) {
			lsBamFile.add(samFile.convertToBam());
		}
		String resultPrefix = guiFileOpen.saveFileName("", "");
		if (FileOperate.isFileDirectory(resultPrefix)) {
			resultPrefix = FileOperate.addSep(resultPrefix);
		}
		String resultName = resultPrefix + prefix;
		resultName = FileOperate.changeFileSuffix(resultName, "_merge", "bam");
		SamFile samFileMerge = SamFile.mergeBamFile(resultName , lsBamFile);
		return samFileMerge;
	}
	
	private void convertBedFile(String bedFile) {
		BedSeq bedSeq = new BedSeq(bedFile);
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
			try { small = (Integer)spinMapNumSmall.getValue(); } catch (Exception e2) { e2.printStackTrace();}
			int big = 1;
			try { big =  (Integer)spinMapNumBig.getValue(); } catch (Exception e2) { }
			bedSeq = bedSeq.filterSeq(small, big, strand);
		}
		if (chckbxSortBed.isSelected()) {
			bedSeq.sort();
		}
	
	}
}
