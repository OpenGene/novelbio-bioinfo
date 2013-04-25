package com.novelbio.nbcgui.GUI;

import javax.swing.JLayeredPane;
import javax.swing.JLabel;

import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.database.model.species.Species;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GuiLayeredPaneSpeciesVersionGff extends JLayeredPane {
	Species species;
	JComboBoxData<Species> cmbSpecies;
	JComboBoxData<String> cmbVersion;
	JComboBoxData<String> cmbGffDB;
		
	public GuiLayeredPaneSpeciesVersionGff() {
		setLayout(null);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(12, 0, 56, 14);
		add(lblSpecies);
		
		cmbSpecies = new JComboBoxData<Species>();
		cmbSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectSpecies();
			}
		});
		cmbSpecies.setBounds(12, 14, 196, 20);
		add(cmbSpecies);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(12, 36, 55, 14);
		add(lblVersion);
		
		cmbVersion = new JComboBoxData<String>();
		cmbVersion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectVersion();
			}
		});
		cmbVersion.setBounds(12, 50, 196, 20);
		add(cmbVersion);
		
		JLabel lblDBtype = new JLabel("DBtype");
		lblDBtype.setBounds(12, 75, 52, 14);
		add(lblDBtype);
		
		cmbGffDB = new JComboBoxData<String>();
		cmbGffDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectGffDB();
			}
		});
		cmbGffDB.setBounds(12, 93, 196, 20);
		add(cmbGffDB);
		initial();
	}
	
	public void addActionListener(ActionListener actionListener) {
		cmbSpecies.addActionListener(actionListener);
	}
	
	private void initial() {
		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		selectSpecies();
	}
	
	private void selectSpecies() {
		species = cmbSpecies.getSelectedValue();
		if (species.getTaxID() != 0) {
			cmbVersion.setMapItem(species.getMapVersion());
			selectVersion();
		}
	}
	
	private void selectVersion() {
		if (species.getTaxID() != 0) {
			species.setVersion(cmbVersion.getSelectedValue());
			cmbGffDB.setMapItem(species.getMapGffDBAll());
			selectGffDB();
		}
	}
	
	private void selectGffDB() {
		if (species.getTaxID() != 0) {
			species.setGffDB(cmbGffDB.getSelectedValue());
		}
	}
	public Species getSelectSpecies() {
		return species;
	}
}
