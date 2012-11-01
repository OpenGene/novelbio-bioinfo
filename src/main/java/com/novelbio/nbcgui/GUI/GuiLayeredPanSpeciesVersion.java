package com.novelbio.nbcgui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.database.domain.geneanno.SpeciesFile.GFFtype;
import com.novelbio.database.model.species.Species;

public class GuiLayeredPanSpeciesVersion extends JLayeredPane {
	/** �ⲿ�������ѡ��species֮����ܻᷢ���ķ�Ӧ */
	SpeciesSelect speciesSelect;
	
	Species species = new Species();
	JComboBoxData<Species> cmbSpecies;
	JComboBoxData<String> cmbVersion;
		
	public GuiLayeredPanSpeciesVersion() {
		setLayout(null);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(12, 0, 56, 14);
		add(lblSpecies);
		
		cmbSpecies = new JComboBoxData<Species>();
		cmbSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectSpecies();
				if (speciesSelect != null) {
					speciesSelect.selectSpecies();
				}
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
		initial();
	}
	/** �ⲿ�������ѡ��species֮����ܻᷢ���ķ�Ӧ */
	public void setSelectSpecies(SpeciesSelect speciesSelect) {
		this.speciesSelect = speciesSelect;
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
		}
	}
	
	public Species getSelectSpecies() {
		return species;
	}
	
	public static interface SpeciesSelect {
		public void selectSpecies();
	}
}
