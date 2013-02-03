package com.novelbio.nbcgui.GUI;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JButton;

import org.apache.commons.math.stat.correlation.SpearmansCorrelation;

import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GuiSnpFiltering extends JPanel {
	JScrollPaneData scrollPane;
	/**
	 * Create the panel.
	 */
	public GuiSnpFiltering() {
		setLayout(null);
		
		scrollPane = new JScrollPaneData();
		scrollPane.setBounds(32, 26, 724, 124);
		add(scrollPane);
		
		JButton btnAddSnpFile = new JButton("AddSnpFile");
		btnAddSnpFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPane.addItem(new String[]{"sese","sfefse"});
			}
		});
		btnAddSnpFile.setBounds(32, 162, 118, 24);
		add(btnAddSnpFile);
		
		JButton btnDelSnpFile = new JButton("DelSnpFile");
		btnDelSnpFile.setBounds(638, 162, 118, 24);
		add(btnDelSnpFile);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(32, 198, 724, 110);
		add(scrollPane_1);
		
		JButton btnAddPileUp = new JButton("AddPileUp");
		btnAddPileUp.setBounds(32, 315, 118, 24);
		add(btnAddPileUp);
		
		JButton btnDelFileUp = new JButton("DelFileUp");
		btnDelFileUp.setBounds(638, 320, 118, 24);
		add(btnDelFileUp);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(32, 362, 401, 104);
		add(scrollPane_2);
		
		JButton btnAddcompare = new JButton("AddCmp");
		btnAddcompare.setBounds(445, 362, 91, 24);
		add(btnAddcompare);
		
		JButton btnDelcompare = new JButton("DelCmp");
		btnDelcompare.setBounds(444, 442, 91, 24);
		add(btnDelcompare);
		initial();
	}
	private void initial() {
		JComboBoxData<Species> jComboBox = new JComboBoxData<Species>();
		jComboBox.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		scrollPane.setTitle(new String[]{"adsf","fsefe"});
		scrollPane.setItem(1, jComboBox);
	}
}
