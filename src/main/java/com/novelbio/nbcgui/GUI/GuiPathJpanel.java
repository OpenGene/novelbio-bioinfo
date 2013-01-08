package com.novelbio.nbcgui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.table.DefaultTableModel;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.base.gui.JTextFieldData;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controltest.CtrlGO;
import com.novelbio.nbcgui.controltest.CtrlPath;
import javax.swing.SpringLayout;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class GuiPathJpanel extends JPanel{

	private JTabbedPane jTabbedPanePathResult;
	private JButton jBtbSavePath;
	private JButton jButRunPath;
	private JProgressBar jProgressBarPath;
	private JLabel jLabResultReviewPath;
	private JLabel jLabInputReviewPath;
	private JTextFieldData jTxtDownValuePath;
	private JLabel jLabDownValuePath;
	private JTextFieldData jTxtUpValuePath;
	private JLabel jLabUpValuePath;
	private JButton jBtnBGFilePath;
	private JTextFieldData jTxtBGPath;
	private JLabel jLabBGPath;
	private JLabel jLabPathPath;
	private JTextFieldData jTxtFilePathPath;
	private JButton jBtnFileOpenPath;
	private JTextFieldData jTxtValColPath;
	private JLabel jLabValueColPath;
	private JCheckBox jChkCluster;
	private JLabel jLabAccColPath;
	private JTextFieldData jTxtAccColPath;
	private JComboBox jCombBlastTaxPath;
	private JCheckBox jChkBlastPath;
	private ButtonGroup btnGroupPathMethod;
	private ButtonGroup btnGroupPathClass;
	private JComboBox jCombSelSpePath;
	private JLabel jLabPathQtaxID;
	private JScrollPaneData jScrollPaneInputPath;
	////////////
	static int QtaxID = 0;//查询物种ID
	static int StaxID = 9606;//blast物种ID
	
	
	public GuiPathJpanel() 
	{
		

		setPreferredSize(new java.awt.Dimension(1046, 617));
		setAlignmentX(0.0f);
		setComponent();
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.NORTH, jProgressBarPath, 588, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jProgressBarPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jProgressBarPath, 1035, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabResultReviewPath, 234, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabResultReviewPath, 248, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabResultReviewPath, 375, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabInputReviewPath, 14, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabInputReviewPath, 241, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabInputReviewPath, 338, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jScrollPaneInputPath, 36, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jScrollPaneInputPath, 241, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jScrollPaneInputPath, 228, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jScrollPaneInputPath, 1035, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTabbedPanePathResult, 254, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTabbedPanePathResult, 241, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTabbedPanePathResult, 585, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTabbedPanePathResult, 1035, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtBGPath, 176, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtBGPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtBGPath, 227, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jBtnBGFilePath, 200, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jBtnBGFilePath, 130, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jBtnBGFilePath, 223, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jBtnBGFilePath, 227, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jBtnFileOpenPath, 60, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jBtnFileOpenPath, 122, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jBtnFileOpenPath, 227, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtAccColPath, 264, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtAccColPath, 122, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTxtAccColPath, 284, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtAccColPath, 165, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtValColPath, 331, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtValColPath, 122, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTxtValColPath, 351, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtValColPath, 166, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jButRunPath, 564, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jButRunPath, 122, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jButRunPath, 588, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jButRunPath, 202, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabBGPath, 156, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabBGPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabBGPath, 122, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jCombSelSpePath, 112, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jCombSelSpePath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jCombSelSpePath, 186, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabPathQtaxID, 88, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabPathQtaxID, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabPathQtaxID, 106, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabPathQtaxID, 141, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jCombBlastTaxPath, 453, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jCombBlastTaxPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jCombBlastTaxPath, 479, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jCombBlastTaxPath, 212, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtUpValuePath, 373, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtUpValuePath, 105, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTxtUpValuePath, 395, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtUpValuePath, 174, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtDownValuePath, 407, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtDownValuePath, 105, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTxtDownValuePath, 429, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtDownValuePath, 174, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabPathPath, 12, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabPathPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabPathPath, 30, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabPathPath, 97, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jChkBlastPath, 518, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jChkBlastPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jChkBlastPath, 92, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jChkCluster, 309, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jChkCluster, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jChkCluster, 101, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabAccColPath, 267, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabAccColPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabUpValuePath, 376, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabUpValuePath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabUpValuePath, 393, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabUpValuePath, 84, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabDownValuePath, 413, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabDownValuePath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabDownValuePath, 424, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabDownValuePath, 99, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jBtbSavePath, 565, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jBtbSavePath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jBtbSavePath, 588, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jBtbSavePath, 104, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabValueColPath, 334, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabValueColPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtFilePathPath, 36, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtFilePathPath, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtFilePathPath, 229, SpringLayout.WEST, this);
		setLayout(springLayout);
		add(jTxtFilePathPath);
		add(jLabValueColPath);
		add(jBtbSavePath);
		add(jLabDownValuePath);
		add(jLabUpValuePath);
		add(jLabAccColPath);
		add(jChkCluster);
		add(jChkBlastPath);
		add(jLabPathPath);
		add(jTxtDownValuePath);
		add(jTxtUpValuePath);
		add(jCombBlastTaxPath);
		add(jLabPathQtaxID);
		add(jCombSelSpePath);
		add(jLabBGPath);
		add(jButRunPath);
		add(jTxtValColPath);
		add(jTxtAccColPath);
		add(jBtnFileOpenPath);
		add(jBtnBGFilePath);
		add(jTxtBGPath);
		add(jTabbedPanePathResult);
		add(jScrollPaneInputPath);
		add(jLabInputReviewPath);
		add(jLabResultReviewPath);
		add(jProgressBarPath);
	}
	private void setComponent() {
		btnGroupPathMethod = new ButtonGroup();
		btnGroupPathClass = new ButtonGroup();
		{
			jLabPathQtaxID = new JLabel();
			jLabPathQtaxID.setText("Query Species");
		}
		{
			jTxtFilePathPath = new JTextFieldData();
			jTxtFilePathPath.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent evt) {
					if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
						try {
							setPathProview(jTxtFilePathPath.getText());
						} catch (Exception e) {
							System.out.println("mei you wen jian");
						}
						
					}
				}
			});
		}
		{
			jBtnFileOpenPath = new JButton();
			jBtnFileOpenPath.setText("LoadData");
			jBtnFileOpenPath.setMargin(new java.awt.Insets(1, 1, 1, 1));
			jBtnFileOpenPath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					GUIFileOpen guiFileOpen = new GUIFileOpen();
					String filename = guiFileOpen.openFileName("txt/excel2003", "txt","xls");
					jTxtFilePathPath.setText(filename);
					try {
						setPathProview(jTxtFilePathPath.getText());
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("mei you wen jian");
					}
				}
			});
		}
		{
			jLabPathPath = new JLabel();
			jLabPathPath.setText("InputData");
		}
		{
			jTxtAccColPath = new JTextFieldData();
			jTxtAccColPath.setNumOnly();
		}
		{
			jLabAccColPath = new JLabel();
			jLabAccColPath.setText("AccIDColNum");
			jLabAccColPath.setAlignmentY(0.0f);
		}
		{
			jLabValueColPath = new JLabel();
			jLabValueColPath.setText("ValueColNum");
			jLabValueColPath.setAlignmentY(0.0f);
		}
		{
			jLabUpValuePath = new JLabel();
			jLabUpValuePath.setText("UpValue");
		}
		{
			jTxtUpValuePath = new JTextFieldData();
			jTxtUpValuePath.setNumOnly(10,4);
		}
		{
			jLabDownValuePath = new JLabel();
			jLabDownValuePath.setText("DownValue");
		}
		{
			jTxtDownValuePath = new JTextFieldData();
			jTxtDownValuePath.setNumOnly(10,4);

		}
		{
			jBtnBGFilePath = new JButton();
			jBtnBGFilePath.setText("BackGround");
			jBtnBGFilePath.setMargin(new java.awt.Insets(1, 0, 1, 0));
			jBtnBGFilePath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					GUIFileOpen guiFileOpen = new GUIFileOpen();
					String filename = guiFileOpen.openFileName("txt/excel2003", "txt","xls");
					jTxtBGPath.setText(filename);
				}
			});
		}
		{
			jTxtValColPath = new JTextFieldData();
			jTxtValColPath.setNumOnly();
		}

		{
			jScrollPaneInputPath = new JScrollPaneData();
		}
		{
			jLabInputReviewPath = new JLabel();
			jLabInputReviewPath.setText("InputReview");
		}
		{
			jLabResultReviewPath = new JLabel();
			jLabResultReviewPath.setText("ResultReview");
		}
		{
			jProgressBarPath = new JProgressBar();
		}
		{
			jChkCluster = new JCheckBox();
			jChkCluster.setText("ClusterPath");
			jChkCluster.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (jChkCluster.isSelected()) {
						jTxtDownValuePath.setEnabled(false);
						jTxtUpValuePath.setEnabled(false);
					}
					else {
						jTxtDownValuePath.setEnabled(true);
						jTxtUpValuePath.setEnabled(true);
					}
				}
			});
		}
		{
			jTabbedPanePathResult = new JTabbedPane();
		}
		{
			jBtbSavePath = new JButton();
			jBtbSavePath.setText("Save As");
			jBtbSavePath.setMargin(new java.awt.Insets(1, 0, 1, 0));
			jBtbSavePath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					GUIFileOpen guiFileOpen = new GUIFileOpen();
					String savefilename = guiFileOpen.saveFileName("excel2003", "xls");
					CtrlPath ctrlPath = CtrlPath.getInstance();
					if (!FileOperate.getFileNameSep(savefilename)[1].equals("xls")) {
						savefilename = savefilename+".xls";
					}
					ctrlPath.saveExcel(savefilename);
				}
			});
		}
		{
			jButRunPath = new JButton();
			jButRunPath.setText("Analysis");
			jButRunPath.setMargin(new java.awt.Insets(1, 1, 1, 1));
			jButRunPath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					getResult();
				}
			});
		}
		{
			jLabBGPath = new JLabel();
			jLabBGPath.setText("BackGround");
			jLabBGPath.setAlignmentY(0.0f);
			jLabBGPath.setAutoscrolls(true);
		}
		{
			jTxtBGPath = new JTextFieldData();
		}
		{
			final HashMap<String, Integer> hashTaxID = Species.getSpeciesNameTaxID(false);
			int i = 0;
			ArrayList<String> keys = Species.getSpeciesName(false);
			String[] speciesarray = new String[keys.size()+1];
			for(String key:keys)
			{
				speciesarray[i] = key; i++;
			}
			speciesarray[i] = "all";
			ComboBoxModel jCombSelSpePathModel = new DefaultComboBoxModel(speciesarray);
			jCombSelSpePath = new JComboBox();
			jCombSelSpePath.setModel(jCombSelSpePathModel);
			jCombSelSpePath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String species = (String) jCombSelSpePath.getSelectedItem();
					if (hashTaxID.get(species) == null) {
						QtaxID = 0;
					}
					else {
						QtaxID =hashTaxID.get(species);
					}
				}
			});
		}
		{
			jChkBlastPath = new JCheckBox();
			jChkBlastPath.setText("Blast");
		}
		{
			final HashMap<String, Integer> hashTaxID = Species.getSpeciesNameTaxID(false);
			int i = 0;
			ArrayList<String> keys = Species.getSpeciesName(false);
			String[] speciesarray = new String[keys.size()+1];
			for(String key:keys)
			{
				speciesarray[i] = key; i++;
			}
			speciesarray[i] = "all";
			ComboBoxModel jCombBlastTaxModel = 
				new DefaultComboBoxModel(speciesarray);
			jCombBlastTaxPath = new JComboBox();
			jCombBlastTaxPath.setModel(jCombBlastTaxModel);
			jCombBlastTaxPath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String species = (String) jCombBlastTaxPath.getSelectedItem();
					if (hashTaxID.get(species) == null) {
						StaxID = 0;
					}
					else {
						StaxID =hashTaxID.get(species);
					}
				}
			});
		}
	}
	
	/**
	 * 查看文件的鼠标或键盘事件响应时调用
	 */
	private void setPathProview(String filePath)
	{
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(filePath, 1);
		String[][] tableValue = null;
		DefaultTableModel jTabInputPath = new DefaultTableModel(tableValue,lsInfo.get(0));
		JTable jTabFInputPath = new JTable();
		jScrollPaneInputPath.setViewportView(jTabFInputPath);
		jTabFInputPath.setModel(jTabInputPath);
		for (int i = 1; i < lsInfo.size(); i++) {
			jTabInputPath.addRow(lsInfo.get(i));
		}
		
	}
	/**
	 * analysis按下去后得到结果
	 */
	private void getResult()
	{
		//jScrollPaneInputPath 最外层的方框
		//jTabbedPanePathTest 里面的标签框
		//jPanPathTest 具体的标签
		//jScrollPanePathtest 标签里面的方框
		//jTabFInputPath 方框里面的数据框
		//jTabInputPath 具体数据
		String geneFileXls = jTxtFilePathPath.getText();
		int colAccID = Integer.parseInt(jTxtAccColPath.getText());
		int colFC = Integer.parseInt(jTxtValColPath.getText());
		
		String backGroundFile = jTxtBGPath.getText();
		boolean blast = jChkBlastPath.isSelected();
		double evalue = 1e-10;
		
		CtrlPath ctrlPath = null;
		
		ArrayList<String[]> lsAccID = null;
		if (colAccID != colFC)
			 lsAccID = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID, colFC}, 1, 0);
		else
			lsAccID = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID}, 1, 0);
		
		ctrlPath = CtrlPath.getInstance(QtaxID, blast, evalue, StaxID);
		ctrlPath.setLsBG(backGroundFile);
		
		if (!jChkCluster.isSelected() || colAccID == colFC) {
			double up = 0; double down = 0;
			if ( colAccID != colFC) {
				up = Double.parseDouble(jTxtUpValuePath.getText());
				down = Double.parseDouble(jTxtDownValuePath.getText());
			}
			ctrlPath.setUpDown(up, down);
			ctrlPath.setIsCluster(false);
		}
		else {
			ctrlPath.setIsCluster(false);
		}
		ctrlPath.setLsAccID2Value(lsAccID);
		ctrlPath.run();
		setNormalGo(ctrlPath);
	}
	
	private void setNormalGo(CtrlPath ctrlPath) {
		//jScrollPaneInputGo 最外层的方框
		//jTabbedPaneGOTest 里面的标签框
		//jPanGoTest 具体的标签
		// jScrollPaneGOtest 标签里面的方框
		// jTabFInputGo 方框里面的数据框
		// jTabInputGo 具体数据
		HashMap<String, LinkedHashMap<String, ArrayList<String[]>>> hashResult = ctrlPath.getHashResult();
		jTabbedPanePathResult.removeAll();
		int i = 0;
		for (Entry<String, LinkedHashMap<String, ArrayList<String[]>>> entry : hashResult.entrySet()) {
			if (i > 2) {
				break;
			}
			for (Entry<String, ArrayList<String[]>> entryTable : entry.getValue().entrySet()) {
				settab(jTabbedPanePathResult, entry.getKey()+entryTable.getKey(), entryTable.getValue());
			}
			i++;
		}
	}
	
	private void settab(JTabbedPane jTabbedPaneGoResult, String tabName , ArrayList<String[]> lsResult) {
		//里层
		String[][] tableValue = null;
		DefaultTableModel jTabResult = new DefaultTableModel(tableValue,lsResult.get(0));
		//中层
		JTable jTabFResult = new JTable();
		jTabFResult.setModel(jTabResult);
		//外层
		JScrollPane jScrollPanelResult = new JScrollPane();
		jScrollPanelResult.setViewportView(jTabFResult);
		//最外层
		jTabbedPaneGoResult.addTab(tabName, null, jScrollPanelResult, null);
		for (int i = 1; i < lsResult.size(); i++) {
			jTabResult.addRow(lsResult.get(i));
		}
	}
}
