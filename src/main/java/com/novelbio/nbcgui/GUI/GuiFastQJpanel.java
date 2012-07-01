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

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mirna.MiRNAtargetRNAhybrid;
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

public class GuiFastQJpanel extends JPanel {
	private JTextField txtMinReadsLen;
	private JTextField txtMappingIndex;
	private JTextField txtSavePathAndPrefix;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private final ButtonGroup groupLibrary = new ButtonGroup();
	JScrollPaneData scrollPaneFastqLeft;
	JScrollPaneData scrollPaneFastqRight;
	private JTextField txtRightAdaptor;
	private JTextField txtLeftAdaptor;
	private JTextField txtMisMatch;
	private JTextField txtGapLength;
	private JTextField txtThreadNum;
	JCheckBox chckbxFilterreads;
	JCheckBox chckbxTrimEnd;
	JComboBoxData<Integer> cmbReadsQuality;
	JCheckBox chckbxMapping;
	JComboBoxData<String> cmbSpeciesVersion;
	JComboBoxData<Species> cmbSpecies;
	JComboBoxData<Integer> cmbLibrary;
	JCheckBox chckbxUniqMapping;
	JButton btnSaveto;
	JButton btnOpenFastqLeft;
	JButton btnDelFastqLeft;
	JButton btnMappingindex;
	JButton btnRun;
	JButton btnOpenFastQRight;
	JButton btnDeleteFastQRight;
	JCheckBox chckbxLowcaseAdaptor;
	CtrlFastQMapping ctrlFastQMapping = new CtrlFastQMapping();
	
	ArrayList<Component> lsComponentsMapping = new ArrayList<Component>();
	ArrayList<Component> lsComponentsFiltering = new ArrayList<Component>();
	
	public GuiFastQJpanel() {
		setLayout(null);
		
		JLabel lblFastqfile = new JLabel("FastQFile");
		lblFastqfile.setBounds(10, 10, 68, 14);
		add(lblFastqfile);
		
		scrollPaneFastqLeft = new JScrollPaneData();
		scrollPaneFastqLeft.setBounds(10, 30, 371, 186);
		scrollPaneFastqLeft.setTitle(new String[]{"FileName","Prix"});
		add(scrollPaneFastqLeft);
		
		scrollPaneFastqRight = new JScrollPaneData();
		scrollPaneFastqRight.setBounds(487, 30, 322, 191);
		scrollPaneFastqRight.setTitle(new String[]{"FileName"});
		add(scrollPaneFastqRight);
		
		btnOpenFastqLeft = new JButton("Open");
		btnOpenFastqLeft.setBounds(393, 38, 82, 24);
		add(btnOpenFastqLeft);
		
		chckbxFilterreads = new JCheckBox("FilterReads");
		chckbxFilterreads.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!chckbxFilterreads.isSelected()) {
					for (Component component : lsComponentsFiltering) {
						component.setEnabled(false);
					}
				}
				else {
					for (Component component : lsComponentsFiltering) {
						component.setEnabled(true);
					}
				}
			}
		});
		chckbxFilterreads.setBounds(10, 226, 108, 22);
		add(chckbxFilterreads);
		
		chckbxTrimEnd = new JCheckBox("TrimEnd");
		chckbxTrimEnd.setBounds(347, 226, 95, 22);
		add(chckbxTrimEnd);
		
		txtMinReadsLen = new JTextField();
		txtMinReadsLen.setBounds(96, 287, 76, 18);
		add(txtMinReadsLen);
		txtMinReadsLen.setColumns(10);
		
		cmbReadsQuality = new JComboBoxData<Integer>();
		cmbReadsQuality.setItemHash(FastQ.getMapReadsQuality());
		cmbReadsQuality.setBounds(121, 252, 153, 23);
		add(cmbReadsQuality);
		
		JLabel lblReadsQuality = new JLabel("Reads Quality");
		lblReadsQuality.setBounds(10, 256, 114, 14);
		add(lblReadsQuality);
		
		JLabel lblRetainBp = new JLabel("Retain Bp");
		lblRetainBp.setBounds(10, 289, 69, 14);
		add(lblRetainBp);
		
		chckbxMapping = new JCheckBox("Mapping");
		chckbxMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!chckbxMapping.isSelected()) {
					for (Component component : lsComponentsMapping) {
						component.setEnabled(false);
					}
				}
				else {
					for (Component component : lsComponentsMapping) {
						component.setEnabled(true);
					}
				}
			}
		});
		chckbxMapping.setBounds(8, 346, 86, 22);
		add(chckbxMapping);
		
		cmbSpeciesVersion = new JComboBoxData<String>();
		cmbSpeciesVersion.setBounds(173, 402, 207, 23);
		add(cmbSpeciesVersion);
		
		JLabel lblAlgrethm = new JLabel("algrethm");
		lblAlgrethm.setBounds(12, 187, 66, 14);
		add(lblAlgrethm);
		
		cmbSpecies = new JComboBoxData<Species>();
		cmbSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Species species = cmbSpecies.getSelectedValue();
				cmbSpeciesVersion.setItemHash(species.getMapVersion());
			}
		});
		cmbSpecies.setItemHash(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		cmbSpecies.setBounds(10, 402, 147, 23);
		//≥ı ºªØcmbSpeciesVersion
		try { cmbSpeciesVersion.setItemHash(cmbSpecies.getSelectedValue().getMapVersion()); 	} catch (Exception e) { }
		
		add(cmbSpecies);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(12, 376, 56, 14);
		add(lblSpecies);
		
		chckbxUniqMapping = new JCheckBox("Uniq Mapping");
		chckbxUniqMapping.setBounds(173, 346, 121, 22);
		add(chckbxUniqMapping);
		
		txtMappingIndex = new JTextField();
		txtMappingIndex.setBounds(10, 461, 337, 24);
		add(txtMappingIndex);
		txtMappingIndex.setColumns(10);
		
		JLabel lblExtendto = new JLabel("ExtendTo");
		lblExtendto.setBounds(17, 450, -137, -132);
		add(lblExtendto);
		
		txtSavePathAndPrefix = new JTextField();
		txtSavePathAndPrefix.setBounds(10, 512, 337, 24);
		add(txtSavePathAndPrefix);
		txtSavePathAndPrefix.setColumns(10);
		
		JLabel lblResultpath = new JLabel("ResultPath");
		lblResultpath.setBounds(10, 486, 80, 14);
		add(lblResultpath);
		
		btnSaveto = new JButton("SaveTo");
		btnSaveto.setBounds(387, 512, 88, 24);
		btnSaveto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filePathName = guiFileOpen.openFilePathName("", "");
				txtSavePathAndPrefix.setText(filePathName);
			}
		});
		add(btnSaveto);
		
		btnDelFastqLeft = new JButton("Delete");
		btnDelFastqLeft.setBounds(393, 74, 82, 24);
		
		btnDelFastqLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneFastqLeft.removeSelRows();
			}
		});
		add(btnDelFastqLeft);
		
		JLabel lblSpeciesVersio = new JLabel("SpeciesVersion");
		lblSpeciesVersio.setBounds(173, 376, 134, 14);
		add(lblSpeciesVersio);
		
		JLabel lblMappingToFile = new JLabel("Mapping To File");
		lblMappingToFile.setBounds(10, 437, 121, 14);
		add(lblMappingToFile);
		
		btnMappingindex = new JButton("MappingIndex");
		btnMappingindex.setBounds(387, 461, 134, 24);
		btnMappingindex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				txtMappingIndex.setText()
			}
		});
		add(btnMappingindex);
		
		btnRun = new JButton("Run");
		btnRun.setBounds(653, 512, 118, 24);
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlFastQMapping = new CtrlFastQMapping();
				ArrayList<String[]> lsInfoLeftAndPrefix = scrollPaneFastqLeft.getLsDataInfo();
				ArrayList<String[]> lsInfoRight = scrollPaneFastqRight.getLsDataInfo();
				ArrayList<String> lsLeftFq = new ArrayList<String>();
				ArrayList<String> lsPrefix = new ArrayList<String>();
				ArrayList<String> lsRightFq = new ArrayList<String>();
				for (String[] strings : lsInfoLeftAndPrefix) {
					lsLeftFq.add(strings[0]);
					lsPrefix.add(strings[1]);
				}
				for (String[] string : lsInfoRight) {
					lsRightFq.add(string[0]);	
				}
				ctrlFastQMapping.setLsFastQfileLeft(lsLeftFq);
				ctrlFastQMapping.setLsFastQfileRight(lsRightFq);
				ctrlFastQMapping.setLsPrefix(lsPrefix);
				ctrlFastQMapping.setFilter(false);
				ctrlFastQMapping.setMapping(false);
				if (chckbxFilterreads.isSelected()) {
					ctrlFastQMapping.setFilter(true);
					ctrlFastQMapping.setAdaptorLeft(txtLeftAdaptor.getText());
					ctrlFastQMapping.setAdaptorRight(txtRightAdaptor.getText());
					ctrlFastQMapping.setAdaptorLowercase(chckbxLowcaseAdaptor.isSelected());
					ctrlFastQMapping.setFastqQuality(cmbReadsQuality.getSelectedValue());
					ctrlFastQMapping.setTrimNNN(chckbxTrimEnd.isSelected());
				}
				if (chckbxMapping.isSelected()) {
					ctrlFastQMapping.setMapping(true);					
					try { ctrlFastQMapping.setGapLen(Integer.parseInt(txtGapLength.getText())); } catch (Exception e2) { 	}
					try { ctrlFastQMapping.setMismatch(Integer.parseInt(txtMisMatch.getText())); } catch (Exception e2) { 	}
					try { ctrlFastQMapping.setThread(Integer.parseInt(txtThreadNum.getText())); } catch (Exception e2) { 	}
					ctrlFastQMapping.setChrIndexFile(txtMappingIndex.getText());
					//TODO
					ctrlFastQMapping.setLibraryType(cmbLibrary.getSelectedValue());
					Species species = cmbSpecies.getSelectedValue();
					species.setVersion(cmbSpeciesVersion.getSelectedValue());
					ctrlFastQMapping.setSpecies(species);
					ctrlFastQMapping.setUniqMapping(chckbxUniqMapping.isSelected());
				}
				ctrlFastQMapping.setOutFilePrefix(txtSavePathAndPrefix.getText());
				ctrlFastQMapping.running();
			}
		});
		add(btnRun);
		
		btnOpenFastQRight = new JButton("Open");
		btnOpenFastQRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileRigth = guiFileOpen.openLsFileName("fastqFile", "");
				for (String string : lsFileRigth) {
					scrollPaneFastqRight.addProview(new String[]{string});
				}
			}
		});
		btnOpenFastQRight.setBounds(821, 38, 86, 24);
		add(btnOpenFastQRight);
		
		btnDeleteFastQRight = new JButton("Delete");
		btnDeleteFastQRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneFastqRight.removeSelRows();
			}
		});
		btnDeleteFastQRight.setBounds(821, 74, 86, 24);
		add(btnDeleteFastQRight);
		
		JLabel lblLeftadaptor = new JLabel("LeftAdaptor");
		lblLeftadaptor.setBounds(298, 260, 109, 14);
		add(lblLeftadaptor);
		
		txtRightAdaptor = new JTextField();
		txtRightAdaptor.setBounds(417, 284, 166, 18);
		add(txtRightAdaptor);
		txtRightAdaptor.setColumns(10);
		
		JLabel lblRightadaptor = new JLabel("RightAdaptor");
		lblRightadaptor.setBounds(298, 286, 109, 14);
		add(lblRightadaptor);
		
		txtLeftAdaptor = new JTextField();
		txtLeftAdaptor.setBounds(417, 254, 166, 18);
		add(txtLeftAdaptor);
		txtLeftAdaptor.setColumns(10);
		
		JLabel lblMismatch = new JLabel("mismatch");
		lblMismatch.setBounds(341, 350, 69, 14);
		add(lblMismatch);
		
		txtMisMatch = new JTextField();
		txtMisMatch.setBounds(417, 348, 68, 18);
		add(txtMisMatch);
		txtMisMatch.setColumns(10);
		
		JLabel lblGaplength = new JLabel("gapLength");
		lblGaplength.setBounds(514, 350, 95, 14);
		add(lblGaplength);
		
		txtGapLength = new JTextField();
		txtGapLength.setBounds(604, 348, 114, 18);
		add(txtGapLength);
		txtGapLength.setColumns(10);
		
		JLabel lblThread = new JLabel("Thread");
		lblThread.setBounds(555, 466, 69, 14);
		add(lblThread);
		
		txtThreadNum = new JTextField();
		txtThreadNum.setBounds(614, 464, 114, 18);
		add(txtThreadNum);
		txtThreadNum.setColumns(10);
		
		chckbxLowcaseAdaptor = new JCheckBox("LowCase Adaptor");
		chckbxLowcaseAdaptor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxLowcaseAdaptor.isSelected()) {
					txtLeftAdaptor.setText("");
					txtLeftAdaptor.setEnabled(false);
					txtRightAdaptor.setText("");
					txtRightAdaptor.setEnabled(false);
				}
				else {
					txtLeftAdaptor.setText("");
					txtLeftAdaptor.setEnabled(true);
					txtRightAdaptor.setText("");
					txtRightAdaptor.setEnabled(true);
				}
			}
		});
		chckbxLowcaseAdaptor.setBounds(614, 252, 196, 22);
		add(chckbxLowcaseAdaptor);
		
		cmbLibrary = new JComboBoxData<Integer>();
		cmbLibrary.setItemHash(CtrlFastQMapping.getMapLibrary());
		cmbLibrary.setBounds(470, 402, 134, 23);
		add(cmbLibrary);
		
		JLabel lblLibrary = new JLabel("Library");
		lblLibrary.setBounds(398, 406, 69, 14);
		add(lblLibrary);

		
		btnOpenFastqLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileLeft = guiFileOpen.openLsFileName("fastqFile", "");
				for (String string : lsFileLeft) {
					scrollPaneFastqLeft.addProview(new String[]{string, ""});
				}
			}
		});
		initialize();
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		lsComponentsFiltering.add(chckbxTrimEnd);
		lsComponentsFiltering.add(txtLeftAdaptor);
		lsComponentsFiltering.add(txtRightAdaptor);
		lsComponentsFiltering.add(txtMinReadsLen);
		lsComponentsFiltering.add(cmbReadsQuality);
		lsComponentsFiltering.add(chckbxLowcaseAdaptor);
		
		lsComponentsMapping.add(txtGapLength);
		lsComponentsMapping.add(txtMappingIndex);
		lsComponentsMapping.add(txtMisMatch);
		lsComponentsMapping.add(txtThreadNum);
		lsComponentsMapping.add(txtGapLength);
		lsComponentsMapping.add(cmbSpecies);
		lsComponentsMapping.add(cmbSpeciesVersion);
		lsComponentsMapping.add(chckbxUniqMapping);
		lsComponentsMapping.add(btnMappingindex);
		lsComponentsMapping.add(cmbLibrary);
		
		chckbxMapping.setSelected(true);
		chckbxFilterreads.setSelected(true);
	}
}
