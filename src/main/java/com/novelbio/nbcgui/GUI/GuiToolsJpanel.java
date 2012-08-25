package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.SpringLayout;
import java.awt.CardLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.base.gui.JTextFieldData;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controltools.CtrlCombFile;
import com.novelbio.nbcgui.controltools.CtrlMedian;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class GuiToolsJpanel extends JPanel {
	private JTextField jtxtFileNameMedian;
	private JTextFieldData jtxtAccID;
	private JTextField jtxtColNum;
	private JTextField jtxtAccIDComp;
	GUIFileOpen guiFileOpenComb = new GUIFileOpen();
	GUIFileOpen guiFileOpenMed = new GUIFileOpen();
	JScrollPaneData scrollPane;
	/**
	 * Create the panel.
	 */
	public GuiToolsJpanel() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		jtxtFileNameMedian = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, jtxtFileNameMedian, 36, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jtxtFileNameMedian, 9, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jtxtFileNameMedian, 62, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jtxtFileNameMedian, 373, SpringLayout.WEST, this);
		add(jtxtFileNameMedian);
		jtxtFileNameMedian.setColumns(10);
		
		JButton btnOpenfileMedian = new JButton("OpenFile");
		//选择待取中位数的文件
		btnOpenfileMedian.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIFileOpen guiFileOpen = new GUIFileOpen();
				String filename = guiFileOpen.openFileName("txt/excel2003", "txt","xls");
				jtxtFileNameMedian.setText(filename);
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnOpenfileMedian, 1, SpringLayout.NORTH, jtxtFileNameMedian);
		add(btnOpenfileMedian);
		
		jtxtAccID = new JTextFieldData();
		jtxtAccID.setNumOnly();
		springLayout.putConstraint(SpringLayout.NORTH, jtxtAccID, 94, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jtxtAccID, 9, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jtxtAccID, 120, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jtxtAccID, 105, SpringLayout.WEST, this);
		add(jtxtAccID);
		jtxtAccID.setColumns(10);
		
		JLabel lblAccidcol = new JLabel("AccIDCol");
		springLayout.putConstraint(SpringLayout.NORTH, lblAccidcol, 73, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblAccidcol, 9, SpringLayout.WEST, this);
		add(lblAccidcol);
		
		JLabel lblFilename = new JLabel("FileName");
		springLayout.putConstraint(SpringLayout.NORTH, lblFilename, 15, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblFilename, 9, SpringLayout.WEST, this);
		add(lblFilename);
		
		jtxtColNum = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, jtxtColNum, 94, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jtxtColNum, 145, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jtxtColNum, 120, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jtxtColNum, 373, SpringLayout.WEST, this);
		add(jtxtColNum);
		jtxtColNum.setColumns(10);
		
		JLabel lblColnum = new JLabel("ColNum");
		springLayout.putConstraint(SpringLayout.NORTH, lblColnum, 73, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, lblColnum, 145, SpringLayout.WEST, this);
		add(lblColnum);
		
		JButton btnSaveasMedian = new JButton("SaveAs");
		springLayout.putConstraint(SpringLayout.WEST, btnSaveasMedian, 117, SpringLayout.EAST, jtxtColNum);
		btnSaveasMedian.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filename = guiFileOpenMed.saveFileName("txt/excel2003", "txt","xls");
				String inFile = jtxtFileNameMedian.getText();
				String txtAccID = jtxtAccID.getText();
				String txtColID = jtxtColNum.getText();
				getMedian(inFile, txtAccID, txtColID, filename);
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, btnOpenfileMedian, 0, SpringLayout.WEST, btnSaveasMedian);
		springLayout.putConstraint(SpringLayout.NORTH, btnSaveasMedian, 1, SpringLayout.NORTH, jtxtAccID);
		add(btnSaveasMedian);
		
		scrollPane = new JScrollPaneData();
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, jtxtFileNameMedian);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -83, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 456, SpringLayout.WEST, jtxtFileNameMedian);
		add(scrollPane);
		
		JButton btnAddlineCompare = new JButton("AddFile");
		springLayout.putConstraint(SpringLayout.NORTH, btnAddlineCompare, 110, SpringLayout.SOUTH, btnSaveasMedian);
		springLayout.putConstraint(SpringLayout.WEST, btnAddlineCompare, 0, SpringLayout.WEST, btnOpenfileMedian);
		scrollPane.setTitle(new String[]{"FileName","FilePrix","CombCol"});
		btnAddlineCompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsfilename = guiFileOpenComb.openLsFileName("txt/excel2003", "txt","xls");
				for (String strings : lsfilename) {
					scrollPane.addItem(new String[]{strings,""});
				}
			}
		});
		add(btnAddlineCompare);
		
		JButton btnSaveasCompare = new JButton("SaveAs");
		springLayout.putConstraint(SpringLayout.WEST, btnSaveasCompare, 25, SpringLayout.EAST, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, btnSaveasCompare, 111, SpringLayout.EAST, scrollPane);
		btnSaveasCompare.setVerticalAlignment(SwingConstants.TOP);
		btnSaveasCompare.setHorizontalAlignment(SwingConstants.LEFT);
		springLayout.putConstraint(SpringLayout.NORTH, btnSaveasCompare, 128, SpringLayout.SOUTH, btnAddlineCompare);
		springLayout.putConstraint(SpringLayout.SOUTH, btnSaveasCompare, 152, SpringLayout.SOUTH, btnAddlineCompare);
		btnSaveasCompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIFileOpen guiFileOpen = new GUIFileOpen();
				String filename = guiFileOpen.saveFileName("txt/excel2003", "txt","xls");
				CtrlCombFile ctrlCombFile = new CtrlCombFile();
				String colAccID = jtxtAccIDComp.getText();
				ctrlCombFile.setCompareCol(colAccID);
				ArrayList<String[]> lsInfo = scrollPane.getLsDataInfo();
				for (String[] strings : lsInfo) {
					String fileName = strings[0].trim();
					String filePrix = strings[1].trim();
					String colID = strings[2].trim();
					if (!FileOperate.isFileExist(fileName) || colID.equals("")) {
						continue;
					}
					if (filePrix.equals("")) {
						filePrix = FileOperate.getFileNameSep(fileName)[0];
					}
					ctrlCombFile.setColDetail(fileName, filePrix, colID);
				}
				ctrlCombFile.setOufFile(filename);
				ctrlCombFile.output();
			}
		});
		add(btnSaveasCompare);
		
		jtxtAccIDComp = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 27, SpringLayout.SOUTH, jtxtAccIDComp);
		springLayout.putConstraint(SpringLayout.EAST, jtxtAccIDComp, 349, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jtxtAccIDComp, 43, SpringLayout.SOUTH, jtxtColNum);
		springLayout.putConstraint(SpringLayout.WEST, jtxtAccIDComp, 281, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jtxtAccIDComp, 61, SpringLayout.SOUTH, jtxtColNum);
		add(jtxtAccIDComp);
		jtxtAccIDComp.setColumns(10);
		
		JLabel lblCompareid = new JLabel("compareID");
		springLayout.putConstraint(SpringLayout.NORTH, lblCompareid, 45, SpringLayout.SOUTH, jtxtColNum);
		springLayout.putConstraint(SpringLayout.SOUTH, lblCompareid, -29, SpringLayout.NORTH, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, lblCompareid, -28, SpringLayout.WEST, jtxtAccIDComp);
		add(lblCompareid);
		
		JLabel lblFile = new JLabel("File");
		springLayout.putConstraint(SpringLayout.NORTH, lblFile, 45, SpringLayout.SOUTH, jtxtAccID);
		springLayout.putConstraint(SpringLayout.SOUTH, lblFile, -29, SpringLayout.NORTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, lblCompareid, 111, SpringLayout.EAST, lblFile);
		springLayout.putConstraint(SpringLayout.WEST, lblFile, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, lblFile, 56, SpringLayout.WEST, this);
		add(lblFile);
		
		JSeparator separator = new JSeparator();
		springLayout.putConstraint(SpringLayout.NORTH, separator, 159, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, separator, -23, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, separator, -330, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, separator, -23, SpringLayout.EAST, this);
		add(separator);
		
		JButton btnDelfile = new JButton("DelFile");
		btnDelfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPane.deleteSelRows();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnDelfile, 27, SpringLayout.SOUTH, btnAddlineCompare);
		springLayout.putConstraint(SpringLayout.WEST, btnDelfile, 29, SpringLayout.EAST, scrollPane);
		add(btnDelfile);
		
		JButton btnImportspeciesinfo = new JButton("ImportSpeciesInfo");
		btnImportspeciesinfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String speciesFile = guiFileOpenComb.openFileName("txt/xls", "");
				if (FileOperate.isFileExistAndBigThanSize(speciesFile, 0.1)) {
					Species species = new Species();
					species.setUpdateSpeciesFile(speciesFile);
					species.update();
				}
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, btnImportspeciesinfo, 89, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, btnImportspeciesinfo, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, btnImportspeciesinfo, 335, SpringLayout.WEST, this);
		add(btnImportspeciesinfo);
		
		JButton btnImportSoftwareInfo = new JButton("ImportSoftwareInfo");
		btnImportSoftwareInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String softToolsFile = guiFileOpenComb.openFileName("txt/xls", "");
				if (FileOperate.isFileExistAndBigThanSize(softToolsFile, 0.1)) {
					SoftWareInfo.updateInfo(softToolsFile);
				}
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, btnImportSoftwareInfo, 33, SpringLayout.EAST, btnImportspeciesinfo);
		springLayout.putConstraint(SpringLayout.SOUTH, btnImportSoftwareInfo, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, btnImportSoftwareInfo, 286, SpringLayout.EAST, btnImportspeciesinfo);
		add(btnImportSoftwareInfo);
		
		
		
	}
	
	
	private void getMedian(String inFile, String txtAccID, String txtColID, String outFile) {
		CtrlMedian ctrlMedian = new CtrlMedian();
		ctrlMedian.setAccID(Integer.parseInt(txtAccID));
		ArrayList<String[]> lsColID = PatternOperate.getPatLoc(txtColID, "\\d+", false);
		ArrayList<Integer> lsCol = new ArrayList<Integer>();
		for (String[] strings : lsColID) {
			lsCol.add(Integer.parseInt(strings[0]));
		}
		ctrlMedian.setFile(inFile);
		ctrlMedian.setMedianID(lsCol);
		ctrlMedian.readFile();
		ctrlMedian.getResult();
		ctrlMedian.saveFile(FileOperate.changeFileSuffix(outFile, "", "xls"));
	}
}
