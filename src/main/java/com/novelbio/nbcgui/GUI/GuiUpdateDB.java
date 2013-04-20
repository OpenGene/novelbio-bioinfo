package com.novelbio.nbcgui.GUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.novelbio.analysis.seq.genome.GffSpeciesInfo;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.updatedb.database.UpdateGene2GO;
import com.novelbio.database.updatedb.database.UpdateGeneInfoNorm;

public class GuiUpdateDB extends JPanel {
	private static final long serialVersionUID = -7042827927643252396L;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	GffSpeciesInfo specieInformation = new GffSpeciesInfo();
	JScrollPaneData sclUpdateGeneInfo;
	JScrollPaneData sclUpdateGene2GO;
	

	UpdateGene2GO updateGene2GO = new UpdateGene2GO();
	
	
	/**
	 * Create the panel.
	 */
	public GuiUpdateDB() {
		setForeground(Color.WHITE);
		setLayout(null);
		
		JButton btnOpengeneidinfo = new JButton("OpenGeneIDInfo");
		btnOpengeneidinfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> lsFiles = guiFileOpen.openLsFileName("", "");
				List<String[]> lsFiles2isGeneID = new ArrayList<String[]>();
				for (String string : lsFiles) {
					lsFiles2isGeneID.add(new String[]{string, "false"});
				}
				sclUpdateGeneInfo.setItemLs(lsFiles2isGeneID);
			}
		});
		btnOpengeneidinfo.setBounds(30, 234, 160, 24);
		add(btnOpengeneidinfo);
		
		JButton btnOpenGOinfo = new JButton("OpenGOInfo");
		btnOpenGOinfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> lsFiles = guiFileOpen.openLsFileName("", "");
				List<String[]> lsFiles2isGeneID = new ArrayList<String[]>();
				for (String string : lsFiles) {
					lsFiles2isGeneID.add(new String[]{string, "false"});
				}
				sclUpdateGene2GO.setItemLs(lsFiles2isGeneID);
			}
		});
		btnOpenGOinfo.setBounds(30, 488, 160, 24);
		add(btnOpenGOinfo);
		
		JButton btnUpdate = new JButton("UpDate");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String[]> lsUpdateGeneInfo = sclUpdateGeneInfo.getLsDataInfo();
				for (String[] strings : lsUpdateGeneInfo) {
					UpdateGeneInfoNorm updateGeneInfoNorm = new UpdateGeneInfoNorm();
					updateGeneInfoNorm.setOverlap(true);
					updateGeneInfoNorm.setRefGeneID(strings[1].equalsIgnoreCase("true"));
					updateGeneInfoNorm.setTxtWriteExcep(FileOperate.changeFileSuffix(strings[0], "_failedUpdate", null));
					updateGeneInfoNorm.updateFile(strings[0]);
				}
				
				List<String[]> lsUpdateGO = sclUpdateGene2GO.getLsDataInfo();
				for (String[] strings : lsUpdateGO) {
					UpdateGene2GO updateGene2GO = new UpdateGene2GO();
					updateGene2GO.setRefGeneID(strings[1].equalsIgnoreCase("true"));
					updateGene2GO.setTxtWriteExcep(FileOperate.changeFileSuffix(strings[0], "_failedUpdate", null));
					updateGene2GO.updateFile(strings[0]);
				}
			}
		});
		btnUpdate.setBounds(721, 524, 118, 24);
		add(btnUpdate);
		
		JLabel lblNewLabel = new JLabel("taxID \\t accID \\t geneIDgeneID(refAccID) \\t dbinfo \\t symbol \\t synoms \\t fullName \\t description \\t pubmedID(Number)");
		lblNewLabel.setBounds(25, 23, 870, 18);
		add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("taxID \\t geneID(refAccID) \\t  goID \\t evidence \\t pubmedID(Num) \\t qualifier \\t dbinfo");
		lblNewLabel_1.setBounds(25, 281, 830, 14);
		add(lblNewLabel_1);
		
		sclUpdateGeneInfo = new JScrollPaneData();
		sclUpdateGeneInfo.setBounds(30, 53, 809, 169);
		add(sclUpdateGeneInfo);
		
		JButton btnDel = new JButton("Del");
		btnDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclUpdateGeneInfo.deleteSelRows();
			}
		});
		btnDel.setBounds(721, 234, 118, 24);
		add(btnDel);
		
		sclUpdateGene2GO = new JScrollPaneData();
		sclUpdateGene2GO.setBounds(30, 307, 809, 169);
		add(sclUpdateGene2GO);
		
		JButton btnDel_1 = new JButton("Del");
		btnDel_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclUpdateGene2GO.deleteSelRows();
			}
		});
		btnDel_1.setBounds(721, 488, 118, 24);
		add(btnDel_1);
		
		JButton btnImportspecies = new JButton("ImportSpeciesInfo");
		btnImportspecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String speciesFile = guiFileOpen.openFileName("txt/xls", "");
				if (FileOperate.isFileExistAndBigThanSize(speciesFile, 0.1)) {
					Species species = new Species();
					species.setUpdateSpeciesFile(speciesFile);
					species.update();
				}
			}
		});
		
		btnImportspecies.setBounds(30, 524, 185, 24);
		add(btnImportspecies);
		
		JButton btnImportsoftware = new JButton("ImportSoftware");
		btnImportsoftware.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String softToolsFile = guiFileOpen.openFileName("txt/xls", "");
				if (FileOperate.isFileExistAndBigThanSize(softToolsFile, 0.1)) {
					SoftWareInfo.updateInfo(softToolsFile);
				}
			}
		});
		btnImportsoftware.setBounds(284, 524, 160, 24);
		add(btnImportsoftware);
		
		JButton btnImportDB = new JButton("ImportDataBase");
		btnImportDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String dbFile = guiFileOpen.openFileName("txt/xls", "");
				if (FileOperate.isFileExistAndBigThanSize(dbFile, 0.01)) {
					DBInfo.updateDBinfo(dbFile);
				}
			}
		});
		btnImportDB.setBounds(505, 524, 185, 24);
		add(btnImportDB);
		
		HashMap<String, String> mapselectType = new HashMap<String, String>();
		mapselectType.put("Exon", "Exon");
		mapselectType.put("Intron", "Intron");
		mapselectType.put("GeneBG", "GeneBG");
		mapselectType.put("GeneDescription", "GeneDescription");
		mapselectType.put("GTFfile", "GTFfile");
		
		initial();
	}
	
	private void initial() {
		JComboBoxData<Boolean> comboBoxData = new JComboBoxData<Boolean>();
		Map<String, Boolean> mapBool2Bool = new HashMap<String, Boolean>();
		mapBool2Bool.put("true", true);
		mapBool2Bool.put("false", false);
		comboBoxData.setMapItem(mapBool2Bool);
		
		sclUpdateGeneInfo.setTitle(new String[]{"file", "isGeneID"});
		sclUpdateGeneInfo.setItem(1, comboBoxData);
		sclUpdateGene2GO.setTitle(new String[]{"file", "isGeneID"});
		sclUpdateGene2GO.setItem(1, comboBoxData);
		
	}
	
}
