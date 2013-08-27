package com.novelbio.nbcgui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;

public class GuiRNAAssembly extends JPanel {
	private JTextField txtSaveTo;
	JComboBoxData<StrandSpecific> cmbStrandInfo;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	JScrollPaneData sclLeftFq;
	JScrollPaneData sclRightFq;
	JSpinner spnMemNum;
	JSpinner spnInsertSize;
	JSpinner spnThreadNum;
	
	/**
	 * Create the panel.
	 */
	public GuiRNAAssembly() {
		setLayout(null);
		
		sclLeftFq = new JScrollPaneData();
		sclLeftFq.setToolTipText("500");
		sclLeftFq.setBounds(12, 36, 419, 253);
		add(sclLeftFq);
		
		sclRightFq = new JScrollPaneData();
		sclRightFq.setBounds(453, 36, 419, 253);
		add(sclRightFq);
		
		cmbStrandInfo = new JComboBoxData<>();
		cmbStrandInfo.setBounds(12, 363, 244, 22);
		add(cmbStrandInfo);
		
		JLabel lblLeft = new JLabel("Left");
		lblLeft.setBounds(12, 12, 59, 18);
		add(lblLeft);
		
		JLabel lblRight = new JLabel("Right");
		lblRight.setBounds(453, 12, 59, 18);
		add(lblRight);
		
		JButton btnAddLeft = new JButton("add");
		btnAddLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> lsLeftFq = guiFileOpen.openLsFileName("", "");
				sclLeftFq.addItemLsSingle2Prefix(lsLeftFq, "_");
			}
		});
		btnAddLeft.setBounds(12, 293, 102, 28);
		add(btnAddLeft);
		
		JButton btnDelete = new JButton("delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclLeftFq.deleteSelRows();
				sclRightFq.deleteSelRows();
			}
		});
		btnDelete.setBounds(364, 293, 159, 28);
		add(btnDelete);
		
		JButton btnAddRight = new JButton("add");
		btnAddRight.setBounds(770, 293, 102, 28);
		add(btnAddRight);
		
		JLabel lblLibrary = new JLabel("Library");
		lblLibrary.setBounds(12, 343, 59, 18);
		add(lblLibrary);

		spnThreadNum = new JSpinner();
		spnThreadNum.setBounds(268, 363, 78, 22);
		add(spnThreadNum);
		
		JLabel lblThreadnum = new JLabel("ThreadNum");
		lblThreadnum.setBounds(268, 341, 84, 18);
		add(lblThreadnum);
		
		spnMemNum = new JSpinner();
		spnMemNum.setBounds(364, 363, 78, 22);
		add(spnMemNum);
		
		JLabel lblMemory = new JLabel("Memory");
		lblMemory.setBounds(364, 341, 78, 18);
		add(lblMemory);
		
		spnInsertSize = new JSpinner();
		spnInsertSize.setToolTipText("");
		spnInsertSize.setBounds(460, 363, 70, 22);
		add(spnInsertSize);
		
		JLabel lblInsertSize = new JLabel("Insert Size");
		lblInsertSize.setBounds(460, 341, 84, 18);
		add(lblInsertSize);
		
		JCheckBox chckbxHighGeneDensity = new JCheckBox("high gene density with UTR overlap(Fungi only and ask Dr. Wang)");
		chckbxHighGeneDensity.setBounds(12, 389, 441, 26);
		add(chckbxHighGeneDensity);
		
		txtSaveTo = new JTextField();
		txtSaveTo.setBounds(12, 423, 718, 22);
		add(txtSaveTo);
		txtSaveTo.setColumns(10);
		
		JButton btnSaveto = new JButton("SaveTo");
		btnSaveto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String file = guiFileOpen.openFilePathName("", "");
				txtSaveTo.setText(file);
			}
		});
		btnSaveto.setBounds(770, 420, 102, 28);
		add(btnSaveto);
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run();
			}
		});
		btnRun.setBounds(770, 489, 102, 28);
		add(btnRun);
		
		initial();
	}
	
	private void initial() {
		cmbStrandInfo.setMapItem(StrandSpecific.getMapStrandLibrary());
		sclLeftFq.setTitle(new String[]{"FqFile", "Prefix"});
		sclRightFq.setTitle(new String[]{"FqFile", "Prefix"});
	}
	
	private void run() {
		
	}
	
}
