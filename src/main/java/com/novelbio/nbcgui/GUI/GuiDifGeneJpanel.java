package com.novelbio.nbcgui.GUI;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;

import com.novelbio.analysis.diffexpress.DiffExpAbs;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JTextField;
import javax.swing.JLabel;

public class GuiDifGeneJpanel extends JPanel {
	JScrollPaneData scrollPaneNormData;
	JScrollPaneData scrollPaneSample;
	JScrollPaneData scrollPaneDesign;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	ArrayList<String[]> lsGeneInfo;
	DiffExpAbs diffExpAbs;
	JComboBoxData<Integer> cmbMethod;
	
	private JTextField txtSave;
	private JTextField txtColAccID;
	/**
	 * Create the panel.
	 */
	public GuiDifGeneJpanel() {
		setLayout(null);
		
		scrollPaneNormData = new JScrollPaneData();
		scrollPaneNormData.setBounds(12, 24, 611, 501);
		add(scrollPaneNormData);
		
		scrollPaneSample = new JScrollPaneData();
		scrollPaneSample.setBounds(635, 24, 310, 195);
		scrollPaneSample.setTitle(new String[]{"SampleColumn","GroupName"});
		add(scrollPaneSample);
		
		JButton btnSetSample = new JButton("SetSample");
		btnSetSample.setBounds(634, 231, 83, 20);
		btnSetSample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneSample.addItem(new String[]{"",""});
			}
		});
		btnSetSample.setMargin(new Insets(0, 0, 0, 0));
		add(btnSetSample);
		
		JButton btnDelSample = new JButton("Delete");
		btnDelSample.setBounds(870, 231, 75, 20);
		btnDelSample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneSample.deleteSelRows();
			}
		});
		btnDelSample.setMargin(new Insets(0, 0, 0, 0));
		add(btnDelSample);
		
		scrollPaneDesign = new JScrollPaneData();
		scrollPaneDesign.setBounds(635, 268, 310, 150);
		scrollPaneDesign.setTitle(new String[]{"group1","group2","FileName"});
		add(scrollPaneDesign);
		
		JButton btnSetDesign = new JButton("addDesign");
		btnSetDesign.setBounds(635, 431, 82, 20);
		btnSetDesign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneDesign.addItem(new String[]{"",""});
			}
		});
		btnSetDesign.setMargin(new Insets(0, 0, 0, 0));
		add(btnSetDesign);
		
		JButton btnDelDesign = new JButton("delete");
		btnDelDesign.setBounds(858, 431, 87, 20);
		btnDelDesign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneDesign.deleteSelRows();
			}
		});
		btnDelDesign.setMargin(new Insets(0, 0, 0, 0));
		btnSetDesign.setMargin(new Insets(0, 0, 0, 0));
		add(btnDelDesign);
		
		JButton btnSave = new JButton("Save");
		btnSave.setBounds(912, 497, 57, 20);
		btnSave.setMargin(new Insets(0, 0, 0, 0));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileNameAndPath("SavePathAndPrefix", "");
				txtSave.setText(fileName);
			}
		});
		add(btnSave);
		
		cmbMethod = new JComboBoxData<Integer>();
		cmbMethod.setBounds(635, 463, 239, 23);
		add(cmbMethod);
		
		txtSave = new JTextField();
		txtSave.setBounds(635, 498, 252, 18);
		add(txtSave);
		txtSave.setColumns(10);
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsCol2Sample = scrollPaneSample.getLsDataInfo();
				diffExpAbs = DiffExpAbs.createDiffExp(cmbMethod.getSelectedValue());
				if (!txtColAccID.getText().trim().equals("")) {
					diffExpAbs.setColID(Integer.parseInt(txtColAccID.getText()));
				}
				else {
					diffExpAbs.setColID(1);
				}
				diffExpAbs.setGeneInfo(lsGeneInfo);
				diffExpAbs.setCol2Sample(lsCol2Sample);

				String pathPrefix = getPathPrefix();
				if (pathPrefix == null) {
					return;
				}
				
				ArrayList<String[]> lsFileName = scrollPaneDesign.getLsDataInfo();
				for (String[] strings : lsFileName) {
					String fileName = strings[2];
					String[] pair = new String[]{strings[0], strings[1]};
					fileName = pathPrefix + fileName;
					if (!fileName.endsWith("txt") && !fileName.endsWith("xls") && !fileName.endsWith("xlsx")) {
						fileName = FileOperate.changeFileSuffix(fileName, "", "xls");
					}
					diffExpAbs.addFileName2Compare(fileName, pair);
				}
				diffExpAbs.getResultFileName();
				JOptionPane.showMessageDialog(null, "Your Diff Expressed Gene Is Already Finding", "Info",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		btnRun.setBounds(827, 537, 118, 24);
		add(btnRun);
		
		JButton btnOpenfile = new JButton("OpenFile");
		btnOpenfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String geneFile = guiFileOpen.openFileName("txt/excel", "");
				lsGeneInfo = ExcelTxtRead.readLsExcelTxt(geneFile, 1);
				scrollPaneNormData.setItemLs(lsGeneInfo);
			}
		});
		btnOpenfile.setBounds(505, 537, 118, 24);
		add(btnOpenfile);
		
		txtColAccID = new JTextField();
		txtColAccID.setBounds(79, 530, 114, 18);
		add(txtColAccID);
		txtColAccID.setColumns(10);
		
		JLabel lblColaccid = new JLabel("ColAccID");
		lblColaccid.setBounds(11, 532, 69, 14);
		add(lblColaccid);
		initial();
	}
	
	public void initial() {
		cmbMethod.setMapItem(DiffExpAbs.getMapMethod2ID());
	}
	
	private String getPathPrefix() {
		String pathPrefix = txtSave.getText().trim();
		if (pathPrefix.equals("")) {
			JOptionPane.showMessageDialog(null, "no save path", "error",JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if (FileOperate.isFileDirectory(pathPrefix)) {
			return FileOperate.addSep(pathPrefix);
		}
		return pathPrefix;
	}
}
