package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JButton;

import com.novelbio.analysis.seq.resequencing.SnpLevel;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;

public class GuiSnpFiltering extends JPanel {
	JScrollPaneData sclSnpFile;
	JScrollPaneData sclSnpPileUp;
	JScrollPaneData sclGroup2Sample;
	JScrollPaneData sclGropuInfo;

	JComboBoxData<String> cmbSample = new JComboBoxData<String>();
	/** 在sclGroup2Prefix中使用，可以添加新的group */
	JComboBoxData<String> cmbGroupSet = new JComboBoxData<String>();
	/** 在sclGropuInfo中使用，不可以添加新的group */
	JComboBoxData<String> cmbGroupGet = new JComboBoxData<String>();
	JComboBoxData<SnpLevel> cmbSnpLevel = new JComboBoxData<SnpLevel>();
	
	
	/**
	 * Create the panel.
	 */
	public GuiSnpFiltering() {
		setLayout(null);
		
		sclSnpFile = new JScrollPaneData();
		sclSnpFile.setBounds(32, 29, 724, 124);
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
		btnAddPileUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclSnpPileUp.addItem(new String[]{"", ""});
			}
		});
		btnAddPileUp.setBounds(32, 315, 118, 24);
		add(btnAddPileUp);
		
		JButton btnDelFileUp = new JButton("DelFileUp");
		btnDelFileUp.setBounds(638, 320, 118, 24);
		add(btnDelFileUp);
		
		sclGropuInfo = new JScrollPaneData();
		sclGropuInfo.setBounds(324, 362, 432, 104);
		add(sclGropuInfo);
		
		JButton btnAddcompare = new JButton("AddCmp");
		btnAddcompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclGropuInfo.addItem(new String[]{"", ""});
			}
		});
		btnAddcompare.setBounds(324, 479, 91, 24);
		add(btnAddcompare);
		
		JButton btnDelcompare = new JButton("DelCmp");
		btnDelcompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclGropuInfo.deleteSelRows();
			}
		});
		btnDelcompare.setBounds(665, 479, 91, 24);
		add(btnDelcompare);
		
		sclGroup2Sample = new JScrollPaneData();
		sclGroup2Sample.setBounds(32, 362, 267, 104);
		add(sclGroup2Sample);
		
		JButton btnAddgroup = new JButton("AddGroup");
		btnAddgroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclGroup2Sample.addItem(new String[]{"",""});
			}
		});
		btnAddgroup.setBounds(32, 478, 102, 27);
		add(btnAddgroup);
		
		JButton btnDelGroup = new JButton("DelGroup");
		btnDelGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclGroup2Sample.deleteSelRows();
			}
		});
		btnDelGroup.setBounds(189, 478, 109, 27);
		add(btnDelGroup);
		
		JComboBox comboBox = new JComboBox();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		comboBox.setBounds(324, 523, 102, 24);
		add(comboBox);

		initial();
	}
	private void initial() {
		cmbSample.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				changeSclComparePrefix();
			}
		});
		cmbGroupSet.setEditable(true);
		cmbGroupSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeSclCompareGroup();
			}
		});
		cmbGroupGet.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				changeSclCompareGroup();
			}
		});
		
		sclSnpFile.setTitle(new String[]{"SnpFile","Sample"});
		
		sclSnpPileUp.setTitle(new String[]{"PileUpFile","Sample"});
		sclSnpPileUp.setItem(1, cmbSample);

		sclGroup2Sample.setTitle(new String[]{"Group","Sample"});
		cmbSnpLevel.setMapItem(SnpLevel.getMapStr2SnpLevel());
		sclGroup2Sample.setItem(0, cmbGroupSet);
		sclGroup2Sample.setItem(1, cmbSample);

		sclGropuInfo.setTitle(new String[]{"Group","SnpLevel","MinNum","MaxNum"});
		cmbSnpLevel.setMapItem(SnpLevel.getMapStr2SnpLevel());
		sclGropuInfo.setItem(1, cmbSnpLevel);
		sclGropuInfo.setItem(0, cmbGroupGet);
	}
	
	private void changeSclComparePrefix() {
		ArrayList<String[]> lsSnp2Prefix = sclSnpFile.getLsDataInfo();
		Map<String, String> mapString2Value = new HashMap<String, String>();
		for (String[] snp2prefix : lsSnp2Prefix) {
			mapString2Value.put(snp2prefix[1], snp2prefix[1]);
		}
		cmbSample.setMapItem(mapString2Value);
	}
	
	private void changeSclCompareGroup() {
		ArrayList<String[]> lsSnp2Prefix = sclGroup2Sample.getLsDataInfo();
		Map<String, String> mapString2Value = new HashMap<String, String>();
		for (String[] group2Prefix : lsSnp2Prefix) {
			if (group2Prefix[0].trim().equals("")) {
				continue;
			}
			mapString2Value.put(group2Prefix[0].trim(), group2Prefix[0].trim());
		}
		cmbGroupGet.setMapItem(mapString2Value);
		cmbGroupSet.setMapItem(mapString2Value);
	}
}
