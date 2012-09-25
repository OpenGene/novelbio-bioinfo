package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.rnaseq.ExonJunction;
import com.novelbio.analysis.seq.rnaseq.ExonSplicingTest;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JCheckBox;

public class GuiRNAautoSplice extends JPanel {
	private JTextField txtGff;
	JScrollPaneData scrlJunction;
	JScrollPaneData scrlBam;
	JScrollPaneData scrlCompare;
	JComboBoxData<Species> combSpecies;
	JComboBoxData<String> combVersion;
	
	JButton btnOpenjunction;
	JButton btnDeljunction;
	JButton btnOpeanbam;
	JButton btnDelbam;
	JButton btnRun;
	JButton btnOpengtf;
	JCheckBox chckbxDisplayAllSplicing;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private JTextField txtSaveTo;
	/**
	 * Create the panel.
	 */
	public GuiRNAautoSplice() {
		setLayout(null);
		
		combSpecies = new JComboBoxData<Species>();
		combSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectSpecies();
			}
		});
		combSpecies.setBounds(642, 49, 223, 23);
		add(combSpecies);
		
		combVersion = new JComboBoxData<String>();
		combVersion.setBounds(642, 110, 223, 23);
		add(combVersion);
		
		scrlJunction = new JScrollPaneData();
		scrlJunction.setBounds(31, 23, 599, 124);
		add(scrlJunction);
		
		btnOpenjunction = new JButton("openJunction");
		btnOpenjunction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFile = guiFileOpen.openLsFileName("JunctionFile", "");
				ArrayList<String[]> lsInfo = new ArrayList<String[]>();
				for (String string : lsFile) {
					String[] tmResult = new String[2];
					tmResult[0] = string; tmResult[1] = "";
					lsInfo.add(tmResult);
				}
				scrlJunction.addItemLs(lsInfo);
			}
		});
		btnOpenjunction.setBounds(31, 159, 129, 24);
		add(btnOpenjunction);
		
		btnDeljunction = new JButton("DelJunction");
		btnDeljunction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlJunction.deleteSelRows();
			}
		});
		btnDeljunction.setBounds(358, 159, 129, 24);
		add(btnDeljunction);
		
		scrlBam = new JScrollPaneData();
		scrlBam.setBounds(31, 213, 599, 124);
		add(scrlBam);
		
		btnOpeanbam = new JButton("OpeanBam");
		btnOpeanbam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFile = guiFileOpen.openLsFileName("BamFile", "");
				ArrayList<String[]> lsInfo = new ArrayList<String[]>();
				for (String string : lsFile) {
					String[] tmResult = new String[2];
					tmResult[0] = string; tmResult[1] = "";
					lsInfo.add(tmResult);
				}
				scrlBam.addItemLs(lsInfo);
			}
		});
		btnOpeanbam.setBounds(31, 349, 118, 24);
		add(btnOpeanbam);
		
		btnDelbam = new JButton("DelBam");
		btnDelbam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlBam.deleteSelRows();
			}
		});
		btnDelbam.setBounds(369, 349, 118, 24);
		add(btnDelbam);
		
		txtGff = new JTextField();
		txtGff.setBounds(642, 162, 223, 18);
		add(txtGff);
		txtGff.setColumns(10);
		
		btnRun = new JButton("run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run();
			}
		});
		btnRun.setBounds(747, 459, 118, 24);
		add(btnRun);
		
		btnOpengtf = new JButton("OpenGTF");
		btnOpengtf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtGff.setText(guiFileOpen.openFileName("GTFfile", ""));
			}
		});
		btnOpengtf.setBounds(747, 190, 118, 24);
		add(btnOpengtf);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(642, 23, 69, 14);
		add(lblSpecies);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(642, 84, 69, 14);
		add(lblVersion);
		
		JLabel lblAddbamfile = new JLabel("AddBamFile");
		lblAddbamfile.setBounds(32, 195, 129, 14);
		add(lblAddbamfile);
		
		JLabel lblAddjunctionfile = new JLabel("AddJunctionFile");
		lblAddjunctionfile.setBounds(31, 5, 118, 14);
		add(lblAddjunctionfile);
		
		txtSaveTo = new JTextField();
		txtSaveTo.setBounds(34, 462, 532, 18);
		add(txtSaveTo);
		txtSaveTo.setColumns(10);
		
		JButton btnSaveto = new JButton("SaveTo");
		btnSaveto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtSaveTo.setText(guiFileOpen.saveFileName("Out", ""));
			}
		});
		btnSaveto.setBounds(589, 459, 118, 24);
		add(btnSaveto);
		
		scrlCompare = new JScrollPaneData();
		scrlCompare.setBounds(642, 239, 223, 76);
		add(scrlCompare);
		
		JLabel lblCompare = new JLabel("Compare");
		lblCompare.setBounds(642, 214, 69, 14);
		add(lblCompare);
		
		JButton btnAddCompare = new JButton("AddCompare");
		btnAddCompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlCompare.addItem(new String[]{"",""});
			}
		});
		btnAddCompare.setBounds(642, 327, 80, 24);
		add(btnAddCompare);
		
		JButton btnDeleteCompare = new JButton("DeleteCompare");
		btnDeleteCompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlCompare.deleteSelRows();
			}
		});
		btnDeleteCompare.setBounds(785, 327, 80, 24);
		add(btnDeleteCompare);
		
		chckbxDisplayAllSplicing = new JCheckBox("Display All Splicing Events");
		chckbxDisplayAllSplicing.setBounds(31, 432, 237, 22);
		add(chckbxDisplayAllSplicing);
		
		initial();
	}
	private void initial() {
		combSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		selectSpecies();
		scrlBam.setTitle(new String[]{"BamFile", "Prefix"});
		scrlJunction.setTitle(new String[]{"JunctionFile", "Prefix"});
		scrlCompare.setTitle(new String[] {"group1", "group2"});
	}
	private void selectSpecies() {
		Species species = combSpecies.getSelectedValue();
		combVersion.setMapItem(species.getMapVersion());
	}
	/** 如果txt存在，优先获得txt对应的gtf文件*/
	private GffHashGene getGffhashGene() {
		GffHashGene gffHashGeneResult = null;
		Species species = combSpecies.getSelectedValue();
		String gtfFile = txtGff.getText();
		if (FileOperate.isFileExist(gtfFile)) {
			gffHashGeneResult = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, txtGff.getText());
		}
		else if (species.getTaxID() != 0) {
			species.setVersion(combVersion.getSelectedValue());
			GffChrAbs gffChrAbs = new GffChrAbs(species);
			gffHashGeneResult = gffChrAbs.getGffHashGene();
		}
		return gffHashGeneResult;
	}
	private Species getSpecies() {
		Species species = combSpecies.getSelectedValue();
		if (species.getTaxID() != 0) {
			species.setVersion(combVersion.getSelectedValue());
		}
		return species;
	}
	private void run() {
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(getGffhashGene());
		exonJunction.setOneGeneOneSpliceEvent(!chckbxDisplayAllSplicing.isSelected());
		String outFile = txtSaveTo.getText();
		if (FileOperate.isFileDirectory(outFile)) {
			outFile = FileOperate.addSep(outFile);
		}
		for (String[] strings : scrlJunction.getLsDataInfo()) {
			exonJunction.setIsoJunFile(strings[1], strings[0]); 
		}
		for (String[] strings : scrlBam.getLsDataInfo()) {
			exonJunction.addBamFile_Sorted(strings[1], strings[0]);
		}
		exonJunction.loadingBamFile(getSpecies());
		for (String[] compareGroups : scrlCompare.getLsDataInfo()) {
			exonJunction.setCompareGroups(compareGroups[0], compareGroups[1]);
			exonJunction.writeToFile(outFile + compareGroups[0] + "vs" +compareGroups[1] + ".xls");
		}
	}
}
