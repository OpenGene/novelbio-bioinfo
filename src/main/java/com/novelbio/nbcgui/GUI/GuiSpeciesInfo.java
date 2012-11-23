package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;

import com.novelbio.analysis.seq.genome.GffSpeciesInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiLayeredPaneSpeciesVersionGff;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class GuiSpeciesInfo extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7042827927643252396L;
	private JTextField txtOutPath;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	JScrollPaneData scrollPaneData;
	JComboBoxData<String> comboBoxData;
	GuiLayeredPaneSpeciesVersionGff guiLayeredPanSpeciesVersion;
	GffSpeciesInfo specieInformation = new GffSpeciesInfo();
	
	/**
	 * Create the panel.
	 */
	public GuiSpeciesInfo() {
		setForeground(Color.WHITE);
		setLayout(null);
		
		JLabel lblSpecieinformation = new JLabel("SpecieInformation");
		lblSpecieinformation.setFont(new Font("Dialog", Font.BOLD, 16));
		lblSpecieinformation.setBounds(138, 34, 177, 24);
		add(lblSpecieinformation);
		
		
		
		guiLayeredPanSpeciesVersion = new GuiLayeredPaneSpeciesVersionGff();
		guiLayeredPanSpeciesVersion.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				
			}
		});
		guiLayeredPanSpeciesVersion.setBounds(96, 70, 237, 160);
		add(guiLayeredPanSpeciesVersion);
		
		txtOutPath = new JTextField();
		txtOutPath.setBounds(582, 101, 189, 31);
		add(txtOutPath);
		txtOutPath.setColumns(10);
		
		JButton btnOutFile = new JButton("OUTFile");
		btnOutFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			 String outFile =  guiFileOpen.saveFileNameAndPath("", "");
			 if (FileOperate.isFileDirectory(outFile)) {
				 outFile =  FileOperate.addSep(outFile);
			 }
			 else {
				 outFile =  FileOperate.getParentPathName(outFile);
				 outFile = FileOperate.addSep(outFile);
			}
			 txtOutPath.setText(outFile);
			}
		});
		btnOutFile.setBounds(345, 101, 189, 31);
		add(btnOutFile);
		
		JButton btnSaveInfo = new JButton("Save  Info");
		btnSaveInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Species species = guiLayeredPanSpeciesVersion.getSelectSpecies();
				
			
				String selectType =  comboBoxData.getSelectedValue();
				String outpath = txtOutPath.getText();
				if (selectType.equals("Exon")) {
					specieInformation.writeExonLength(species, outpath);
				}
				if (selectType.equals("Intron")) {
					specieInformation.writeIntronLength(species, outpath);
				}
				if (selectType.equals("GeneBG")) {
					specieInformation.writeGeneBG(species, outpath);
				}
				if (selectType.equals("GeneDescription")) {
					specieInformation.writeGeneDescription(species, outpath);
				}

			}
		});
		btnSaveInfo.setBounds(582, 158, 189, 31);
		add(btnSaveInfo);
		
		scrollPaneData = new JScrollPaneData();
		scrollPaneData.setBounds(96, 264, 676, 333);
		add(scrollPaneData);
		
		HashMap<String, String> mapselectType = new HashMap<String, String>();
		mapselectType.put("Exon", "Exon");
		mapselectType.put("Intron", "Intron");
		mapselectType.put("GeneBG", "GeneBG");
		mapselectType.put("GeneDescription", "GeneDescription");
		comboBoxData = new JComboBoxData<String>();
		comboBoxData.setBounds(345, 158, 189, 31);
		comboBoxData.setMapItem(mapselectType);
		add(comboBoxData);
		
		JButton btnSelectChrLength = new JButton("SelectChrLength");
		btnSelectChrLength.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scrollPaneData.clean();
				Species species = guiLayeredPanSpeciesVersion.getSelectSpecies();
				ArrayList<String[]> lsChrAndLength = new ArrayList<String[]>();
				lsChrAndLength = specieInformation.sortLsChrInfo( specieInformation.getChrLength(species));
				String[] title = {"Chromosome","Length"};
 				scrollPaneData.setTitle(title);
				scrollPaneData.addItemLs(lsChrAndLength);
				
			}
		});
		btnSelectChrLength.setBounds(96, 231, 189, 31);
		add(btnSelectChrLength);
		
		JButton btnSaveTo = new JButton("Save  ChrInfo");
		btnSaveTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String saveToPathAndFile= guiFileOpen.saveFileName("", "");
				ArrayList<String[]> dataInfo = scrollPaneData.getLsDataInfo();
				TxtReadandWrite txtWrite = new TxtReadandWrite(saveToPathAndFile, true);
				for (String[] strings : dataInfo) {
					txtWrite.writefileln(strings[0]  + "\t" + strings[1]);
				}
				txtWrite.close();
			}
		});
		btnSaveTo.setBounds(625, 231, 146, 28);
		add(btnSaveTo);
		
		
	}
}
