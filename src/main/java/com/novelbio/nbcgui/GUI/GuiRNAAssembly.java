package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

public class GuiRNAAssembly extends JPanel {
	private JTextField textField;

	/**
	 * Create the panel.
	 */
	public GuiRNAAssembly() {
		setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setToolTipText("500");
		scrollPane.setBounds(12, 36, 419, 253);
		add(scrollPane);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(453, 36, 419, 253);
		add(scrollPane_1);
		
		JSpinner spinner = new JSpinner();
		spinner.setBounds(268, 363, 78, 22);
		add(spinner);
		
		JLabel lblThreadnum = new JLabel("ThreadNum");
		lblThreadnum.setBounds(268, 341, 84, 18);
		add(lblThreadnum);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(12, 363, 244, 22);
		add(comboBox);
		
		JLabel lblLeft = new JLabel("Left");
		lblLeft.setBounds(12, 12, 59, 18);
		add(lblLeft);
		
		JLabel lblRight = new JLabel("Right");
		lblRight.setBounds(453, 12, 59, 18);
		add(lblRight);
		
		JButton btnAdd = new JButton("add");
		btnAdd.setBounds(12, 293, 102, 28);
		add(btnAdd);
		
		JButton btnDelete = new JButton("delete");
		btnDelete.setBounds(364, 293, 159, 28);
		add(btnDelete);
		
		JButton btnAdd_1 = new JButton("add");
		btnAdd_1.setBounds(770, 293, 102, 28);
		add(btnAdd_1);
		
		JLabel lblLibrary = new JLabel("Library");
		lblLibrary.setBounds(12, 343, 59, 18);
		add(lblLibrary);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setBounds(364, 363, 78, 22);
		add(spinner_1);
		
		JLabel lblMemory = new JLabel("Memory");
		lblMemory.setBounds(364, 341, 78, 18);
		add(lblMemory);
		
		JSpinner spinner_2 = new JSpinner();
		spinner_2.setToolTipText("");
		spinner_2.setBounds(460, 363, 70, 22);
		add(spinner_2);
		
		JLabel lblInsertSize = new JLabel("Insert Size");
		lblInsertSize.setBounds(460, 341, 84, 18);
		add(lblInsertSize);
		
		JCheckBox chckbxHighGeneDensity = new JCheckBox("high gene density with UTR overlap(Fungi only and ask Dr. Wang)");
		chckbxHighGeneDensity.setBounds(538, 361, 441, 26);
		add(chckbxHighGeneDensity);
		
		textField = new JTextField();
		textField.setBounds(12, 423, 718, 22);
		add(textField);
		textField.setColumns(10);
		
		JButton btnSaveto = new JButton("SaveTo");
		btnSaveto.setBounds(770, 420, 102, 28);
		add(btnSaveto);

	}
}
