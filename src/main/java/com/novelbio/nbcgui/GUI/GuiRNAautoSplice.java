package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
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

public class GuiRNAautoSplice extends JPanel {
	private JTextField txtGff;
	JScrollPaneData scrlJunction;
	JScrollPaneData scrlBam;
	JComboBoxData<Species> combSpecies;
	JComboBoxData<String> combVersion;
	
	JButton btnOpenjunction;
	JButton btnDeljunction;
	JButton btnOpeanbam;
	JButton btnDelbam;
	JButton btnRun;
	JButton btnOpengtf;
	
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
		combSpecies.setBounds(31, 376, 223, 23);
		add(combSpecies);
		
		combVersion = new JComboBoxData<String>();
		combVersion.setBounds(370, 376, 197, 23);
		add(combVersion);
		
		scrlJunction = new JScrollPaneData();
		scrlJunction.setBounds(31, 23, 691, 146);
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
		btnOpenjunction.setBounds(747, 23, 129, 24);
		add(btnOpenjunction);
		
		btnDeljunction = new JButton("DelJunction");
		btnDeljunction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlJunction.deleteSelRows();
			}
		});
		btnDeljunction.setBounds(747, 145, 129, 24);
		add(btnDeljunction);
		
		scrlBam = new JScrollPaneData();
		scrlBam.setBounds(31, 202, 691, 142);
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
		btnOpeanbam.setBounds(747, 202, 118, 24);
		add(btnOpeanbam);
		
		btnDelbam = new JButton("DelBam");
		btnDelbam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlBam.deleteSelRows();
			}
		});
		btnDelbam.setBounds(747, 320, 118, 24);
		add(btnDelbam);
		
		txtGff = new JTextField();
		txtGff.setBounds(31, 417, 536, 18);
		add(txtGff);
		txtGff.setColumns(10);
		
		btnRun = new JButton("run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		btnRun.setBounds(747, 465, 118, 24);
		add(btnRun);
		
		btnOpengtf = new JButton("OpenGTF");
		btnOpengtf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtGff.setText(guiFileOpen.openFileName("GTFfile", ""));
			}
		});
		btnOpengtf.setBounds(605, 410, 118, 24);
		add(btnOpengtf);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(31, 356, 69, 14);
		add(lblSpecies);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(370, 356, 69, 14);
		add(lblVersion);
		
		JLabel lblAddbamfile = new JLabel("AddBamFile");
		lblAddbamfile.setBounds(31, 181, 129, 14);
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
		btnSaveto.setBounds(604, 465, 118, 24);
		add(btnSaveto);
		
		initial();
	}
	private void initial() {
		combSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		selectSpecies();
		scrlBam.setTitle(new String[]{"BamFile", "Prefix"});
		scrlJunction.setTitle(new String[]{"JunctionFile", "Prefix"});
		
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
		for (String[] strings : scrlJunction.getLsDataInfo()) {
			exonJunction.setIsoJunFile(strings[0], strings[1]); 
		}
		for (String[] strings : scrlBam.getLsDataInfo()) {
			exonJunction.addBamFile_Sorted(strings[1], strings[0]);
		}
		exonJunction.loadingBamFile(getSpecies());
		exonJunction.writeToFile(txtSaveTo.getText());
	}
}
