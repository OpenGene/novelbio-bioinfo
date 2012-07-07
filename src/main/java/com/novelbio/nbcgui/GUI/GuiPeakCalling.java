package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;

public class GuiPeakCalling extends JPanel {

	/**
	 * Create the panel.
	 */
	public GuiPeakCalling() {
		setLayout(null);
		
		JScrollPane sclSamBedFile = new JScrollPane();
		sclSamBedFile.setBounds(12, 26, 320, 73);
		add(sclSamBedFile);
		
		JButton btnOpenBed = new JButton("OpenBed");
		btnOpenBed.setBounds(344, 26, 112, 24);
		add(btnOpenBed);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.setBounds(344, 70, 112, 24);
		add(btnDelete);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(12, 428, 157, 23);
		add(comboBox);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(12, 403, 69, 14);
		add(lblSpecies);
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setBounds(12, 496, 157, 23);
		add(comboBox_1);
		
		JLabel lblAlgorithm = new JLabel("Algorithm");
		lblAlgorithm.setBounds(12, 476, 96, 14);
		add(lblAlgorithm);
		
		JComboBox comboBox_2 = new JComboBox();
		comboBox_2.setBounds(181, 428, 151, 23);
		add(comboBox_2);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(181, 403, 69, 14);
		add(lblVersion);
		
		JComboBox comboBox_3 = new JComboBox();
		comboBox_3.setBounds(181, 496, 151, 23);
		add(comboBox_3);
		
		JLabel lblExperimentType = new JLabel("Experiment Type");
		lblExperimentType.setBounds(181, 476, 139, 14);
		add(lblExperimentType);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(468, 26, 320, 73);
		add(scrollPane);
		
		JLabel lblInputcontrol = new JLabel("InputControl");
		lblInputcontrol.setBounds(468, 12, 112, 14);
		add(lblInputcontrol);
		
		JLabel lblSample = new JLabel("Sample");
		lblSample.setBounds(12, 12, 69, 14);
		add(lblSample);
		
		JButton btnControlBed = new JButton("OpenBed");
		btnControlBed.setBounds(789, 26, 118, 24);
		add(btnControlBed);
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.setBounds(789, 75, 118, 24);
		add(btnNewButton);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 134, 320, 73);
		add(scrollPane_1);
		
		JLabel lblVersus = new JLabel("Versus");
		lblVersus.setBounds(22, 111, 69, 14);
		add(lblVersus);

	}
}
