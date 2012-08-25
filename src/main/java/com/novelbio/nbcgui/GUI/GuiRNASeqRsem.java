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

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.MapRsem;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;

import org.springframework.context.annotation.Primary;

public class GuiRNASeqRsem extends JPanel {
	private JTextField txtMappingIndex;
	private JTextField txtSavePathAndPrefix;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private final ButtonGroup groupLibrary = new ButtonGroup();
	JScrollPaneData scrollPaneFastqLeft;
	JScrollPaneData scrollPaneFastqRight;
	private JTextField txtThreadNum;
	JComboBoxData<String> cmbSpeciesVersion;
	JComboBoxData<Species> cmbSpecies;
	JButton btnSaveto;
	JButton btnOpenFastqLeft;
	JButton btnDelFastqLeft;
	JButton btnMappingindex;
	JButton btnRun;
	JButton btnOpenFastQRight;
	JButton btnDeleteFastQRight;
	
	MapRsem mapRsem;
	
	ArrayList<Component> lsComponentsMapping = new ArrayList<Component>();
	ArrayList<Component> lsComponentsFiltering = new ArrayList<Component>();
	
	public GuiRNASeqRsem() {
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
				cmbSpeciesVersion.setMapItem(species.getMapVersion());
			}
		});
		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		cmbSpecies.setBounds(10, 402, 147, 23);
		//≥ı ºªØcmbSpeciesVersion
		try { cmbSpeciesVersion.setMapItem(cmbSpecies.getSelectedValue().getMapVersion()); 	} catch (Exception e) { }
		
		add(cmbSpecies);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(12, 376, 56, 14);
		add(lblSpecies);
		
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
				scrollPaneFastqLeft.deleteSelRows();
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
				txtMappingIndex.setText(guiFileOpen.openFileName("", ""));
			}
		});
		add(btnMappingindex);
		
		btnRun = new JButton("Run");
		btnRun.setBounds(653, 512, 118, 24);
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Species species = cmbSpecies.getSelectedValue();
				species.setVersion(cmbSpeciesVersion.getSelectedValue());
				
				int threadNum = 4;
				try { threadNum = Integer.parseInt(txtThreadNum.getText()); } catch (Exception e1) { }
				String out = txtSavePathAndPrefix.getText();
				
				HashMap<String, ArrayList<ArrayList<FastQ>>> mapPrefix2LsFastq = getMapPrefix2LsFastq();
				for (Entry<String, ArrayList<ArrayList<FastQ>>> entry : mapPrefix2LsFastq.entrySet()) {
					String prefix = entry.getKey();
					ArrayList<ArrayList<FastQ>> lsFastqFR = entry.getValue();
					
					mapRsem = new MapRsem(species);
					mapRsem.setLeftFq(lsFastqFR.get(0));
					mapRsem.setRightFq(lsFastqFR.get(1));
					mapRsem.setThreadNum(threadNum);
					mapRsem.setOutPathPrefix(out + prefix);
					mapRsem.mapReads();
				}
			}
		});
		add(btnRun);
		
		btnOpenFastQRight = new JButton("Open");
		btnOpenFastQRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileRigth = guiFileOpen.openLsFileName("fastqFile", "");
				for (String string : lsFileRigth) {
					scrollPaneFastqRight.addItem(new String[]{string});
				}
			}
		});
		btnOpenFastQRight.setBounds(821, 38, 86, 24);
		add(btnOpenFastQRight);
		
		btnDeleteFastQRight = new JButton("Delete");
		btnDeleteFastQRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneFastqRight.deleteSelRows();
			}
		});
		btnDeleteFastQRight.setBounds(821, 74, 86, 24);
		add(btnDeleteFastQRight);
		
		JLabel lblThread = new JLabel("Thread");
		lblThread.setBounds(555, 466, 69, 14);
		add(lblThread);
		
		txtThreadNum = new JTextField();
		txtThreadNum.setBounds(614, 464, 114, 18);
		add(txtThreadNum);
		txtThreadNum.setColumns(10);

		
		btnOpenFastqLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileLeft = guiFileOpen.openLsFileName("fastqFile", "");
				for (String string : lsFileLeft) {
					scrollPaneFastqLeft.addItem(new String[]{string, ""});
				}
			}
		});
		initialize();
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		lsComponentsMapping.add(txtMappingIndex);
		lsComponentsMapping.add(txtThreadNum);
		lsComponentsMapping.add(cmbSpecies);
		lsComponentsMapping.add(cmbSpeciesVersion);
		lsComponentsMapping.add(btnMappingindex);
		
		txtMappingIndex.setEnabled(false);
		btnMappingindex.setEnabled(false);
	}
	
	/**
	 * key: prefix <br>
	 * value: arraylist-0:lsLeft 1:lsRigth
	 * @return
	 */
	private HashMap<String, ArrayList<ArrayList<FastQ>>> getMapPrefix2LsFastq() {
		HashMap<String, ArrayList<ArrayList<FastQ>>> mapPrefix2LsFastq = new LinkedHashMap<String, ArrayList<ArrayList<FastQ>>>();
		
		ArrayList<String[]> lsInfoLeftAndPrefix = scrollPaneFastqLeft.getLsDataInfo();
		ArrayList<String[]> lsInfoRight = scrollPaneFastqRight.getLsDataInfo();
		for (int i = 0; i < lsInfoLeftAndPrefix.size(); i++) {
			String[] leftAndPrefix = lsInfoLeftAndPrefix.get(i);
			String prefix = leftAndPrefix[1];
			ArrayList<ArrayList<FastQ>> lsFastQLR = getLsFastqLR(mapPrefix2LsFastq, prefix);
			if (FileOperate.isFileExistAndBigThanSize(leftAndPrefix[0], 100)) {
				lsFastQLR.get(0).add(new FastQ(leftAndPrefix[0]));
			}
			
			if (lsInfoRight.size() > i) {
				String[] right = lsInfoRight.get(i);
				if (FileOperate.isFileExistAndBigThanSize(right[0], 100)) {
					lsFastQLR.get(1).add(new FastQ(right[0]));
				}
			}
		}
		return mapPrefix2LsFastq;
	}
	
	private ArrayList<ArrayList<FastQ>> getLsFastqLR(HashMap<String, ArrayList<ArrayList<FastQ>>> mapPrefix2LsFastq, String prefix) {
		ArrayList<ArrayList<FastQ>> lsFastqLR = null;
		if (mapPrefix2LsFastq.containsKey(prefix)) {
			lsFastqLR = mapPrefix2LsFastq.get(prefix);
		}
		else {
			lsFastqLR = new ArrayList<ArrayList<FastQ>>();
			lsFastqLR.add(new ArrayList<FastQ>());
			lsFastqLR.add(new ArrayList<FastQ>());
			mapPrefix2LsFastq.put(prefix, lsFastqLR);
		}
		return lsFastqLR;
	}
	
}
