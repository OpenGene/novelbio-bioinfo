package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JComboBox;

public class GuiDifGeneFinder extends JPanel {
	private static final long serialVersionUID = -5920221480221563242L;

	/**
	 * Create the panel.
	 */
	public GuiDifGeneFinder() {
		setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 12, 605, 206);
		add(scrollPane);
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.setBounds(636, 12, 118, 24);
		add(btnNewButton);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(639, 71, 118, 23);
		add(comboBox);
		
		JButton btnNewButton_1 = new JButton("New button");
		btnNewButton_1.setBounds(639, 198, 118, 24);
		add(btnNewButton_1);

	}
}
