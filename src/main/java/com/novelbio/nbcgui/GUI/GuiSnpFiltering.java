package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JButton;

import com.novelbio.analysis.seq.resequencing.SnpLevel;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GuiSnpFiltering extends JPanel {
	JScrollPaneData sclSnpFile;
	JScrollPaneData sclSnpPileUp;
	JScrollPaneData sclCompare;
	JComboBoxData<String> cmbGroup = new JComboBoxData<String>();
	JComboBoxData<SnpLevel> cmbSnpLevel = new JComboBoxData<SnpLevel>();
	
	
	/**
	 * Create the panel.
	 */
	public GuiSnpFiltering() {
		setLayout(null);
		
		sclSnpFile = new JScrollPaneData();
		sclSnpFile.setBounds(32, 26, 724, 124);
		add(sclSnpFile);
		
		JButton btnAddSnpFile = new JButton("AddSnpFile");
		btnAddSnpFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclSnpFile.addItem(new String[]{"",""});
			}
		});
		btnAddSnpFile.setBounds(32, 162, 118, 24);
		add(btnAddSnpFile);
		
		JButton btnDelSnpFile = new JButton("DelSnpFile");
		btnDelSnpFile.setBounds(638, 162, 118, 24);
		add(btnDelSnpFile);
		
		sclSnpPileUp = new JScrollPaneData();
		sclSnpPileUp.setBounds(32, 198, 724, 110);
		add(sclSnpPileUp);
		
		JButton btnAddPileUp = new JButton("AddPileUp");
		btnAddPileUp.setBounds(32, 315, 118, 24);
		add(btnAddPileUp);
		
		JButton btnDelFileUp = new JButton("DelFileUp");
		btnDelFileUp.setBounds(638, 320, 118, 24);
		add(btnDelFileUp);
		
		sclCompare = new JScrollPaneData();
		sclCompare.setBounds(32, 362, 401, 104);
		add(sclCompare);
		
		JButton btnAddcompare = new JButton("AddCmp");
		btnAddcompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclCompare.addItem(new String[]{"", ""});
			}
		});
		btnAddcompare.setBounds(445, 362, 91, 24);
		add(btnAddcompare);
		
		JButton btnDelcompare = new JButton("DelCmp");
		btnDelcompare.setBounds(444, 442, 91, 24);
		add(btnDelcompare);

		initial();
	}
	private void initial() {
		cmbGroup.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				changeSclCompareGroup();
			}
		});
		
		sclSnpPileUp.setTitle(new String[]{"PileUpFile","Group"});
		sclSnpPileUp.setItem(1, cmbGroup);
		
		sclCompare.setTitle(new String[]{"Group","SnpLevel","MinNum","MaxNum"});
		cmbSnpLevel.setMapItem(SnpLevel.getMapStr2SnpLevel());
		sclCompare.setItem(1, cmbSnpLevel);

		
		sclSnpFile.setTitle(new String[]{"SnpFile","Group"});
		sclCompare.setItem(0, cmbGroup);
	}
	
	private void changeSclCompareGroup() {
		ArrayList<String[]> lsSnp2Prefix = sclSnpFile.getLsDataInfo();
		Map<String, String> mapString2Value = new HashMap<String, String>();
		for (String[] snp2prefix : lsSnp2Prefix) {
			mapString2Value.put(snp2prefix[1], snp2prefix[1]);
		}
		cmbGroup.setMapItem(mapString2Value);
	}
}
