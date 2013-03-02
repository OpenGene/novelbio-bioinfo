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
	JComboBoxData<GffType> cmbGffType;
		
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
		cmbSpecies.setBounds(12, 16, 196, 23);
		add(cmbSpecies);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(13, 47, 55, 14);
		add(lblVersion);
		
		cmbVersion = new JComboBoxData<String>();
		cmbVersion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectVersion();
			}
		});
		cmbVersion.setBounds(12, 62, 196, 23);
		add(cmbVersion);
		
		JLabel lblDBtype = new JLabel("DBtype");
		lblDBtype.setBounds(13, 94, 52, 14);
		add(lblDBtype);
		
		cmbGffType = new JComboBoxData<GffType>();
		cmbGffType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectGffType();
			}
		});
		cmbGffType.setBounds(12, 112, 196, 23);
		add(cmbGffType);
		initial();
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
			cmbGffType.setMapItem(species.getMapGffTypeAll());
			selectGffType();
		}
	}
	
	private void selectGffType() {
		if (species.getTaxID() != 0) {
			species.setGfftype(cmbGffType.getSelectedValue());
		}
	}
	public Species getSelectSpecies() {
		return species;
	}
}
