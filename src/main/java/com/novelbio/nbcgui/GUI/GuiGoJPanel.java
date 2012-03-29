package com.novelbio.nbcgui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.table.DefaultTableModel;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.NumberOnlyDoc;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.NumOnlyDoc;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.nbcgui.controltest.CtrlGO;
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
public class GuiGoJPanel extends JPanel{
	private JTabbedPane jTabbedPaneGoResult;
	private JButton jBtbSaveGo;
	private JButton jButRunGo;
	private JProgressBar jProgressBarGo;
	private JLabel jLabResultReviewGo;
	private JLabel jLabInputReviewGo;
	private JLabel jLabGoType;
	private JLabel jLabAlgorithm;
	private JTextField jTxtDownValueGo;
	private JLabel jLabDownValueGo;
	private JTextField jTxtUpValueGo;
	private JLabel jLabUpValueGo;
	private JButton jBtnBGFileGo;
	private JTextField jTxtBGGo;
	private JLabel jLabBGGo;
	private JLabel jLabPathGo;
	private JTextField jTxtFilePathGo;
	private JButton jBtnFileOpenGo;
	private JTextField jTxtValColGo;
	private JLabel jLabValueColGo;
	private JLabel jLabAccColGo;
	private JTextField jTxtAccColGo;
	private JRadioButton jRadBtnGoClassC;
	private JRadioButton jRadBtnGoClassF;
	private JRadioButton jRadBtnGoClassP;
	private JComboBox jCombBlastTaxGo;
	private JCheckBox jChkBlastGo;
	private JRadioButton jRadBtnQM;
	private JRadioButton jRadBtnElim;
	private ButtonGroup btnGroupGoMethod;
	private ButtonGroup btnGroupGoClass;
	private JComboBox jCombSelSpeGo;
	private JCheckBox jChkCluster;
	private JLabel jLabGoQtaxID;
	private JScrollPane jScrollPaneInputGo;
	////////////
	static int QtaxID = 0;//��ѯ����ID
	static int StaxID = 9606;//blast����ID
	String GoClass = "";
	
	
	public GuiGoJPanel() {
	

		this.setPreferredSize(new java.awt.Dimension(1046, 644));
		setAlignmentX(0.0f);
		setComponent();
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.NORTH, jProgressBarGo, 617, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jProgressBarGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jProgressBarGo, 1039, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabResultReviewGo, 249, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabResultReviewGo, 332, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabResultReviewGo, 459, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabInputReviewGo, 14, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabInputReviewGo, 332, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabInputReviewGo, 429, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jScrollPaneInputGo, 36, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jScrollPaneInputGo, 316, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jScrollPaneInputGo, 241, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jScrollPaneInputGo, 1039, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTabbedPaneGoResult, 263, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTabbedPaneGoResult, 316, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTabbedPaneGoResult, 594, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTabbedPaneGoResult, 1039, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtFilePathGo, 12, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtFilePathGo, 97, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtFilePathGo, 303, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabAlgorithm, 330, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabAlgorithm, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabAlgorithm, 345, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabAlgorithm, 90, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabUpValueGo, 266, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabUpValueGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabUpValueGo, 283, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabUpValueGo, 84, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jChkBlastGo, 512, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jChkBlastGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jChkBlastGo, 92, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabPathGo, 12, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabPathGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabPathGo, 30, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabPathGo, 97, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jButRunGo, 555, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jButRunGo, 223, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jButRunGo, 579, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jButRunGo, 303, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jBtnBGFileGo, 143, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jBtnBGFileGo, 207, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jBtnBGFileGo, 166, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jBtnBGFileGo, 304, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jBtnFileOpenGo, 36, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jBtnFileOpenGo, 200, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jBtnFileOpenGo, 303, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtAccColGo, 172, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtAccColGo, 117, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTxtAccColGo, 192, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtAccColGo, 160, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtValColGo, 224, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtValColGo, 117, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTxtValColGo, 243, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtValColGo, 161, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtUpValueGo, 263, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtUpValueGo, 117, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTxtUpValueGo, 285, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtUpValueGo, 186, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtDownValueGo, 291, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtDownValueGo, 117, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jTxtDownValueGo, 313, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtDownValueGo, 186, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabValueColGo, 226, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabValueColGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jRadBtnGoClassC, 475, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jRadBtnGoClassC, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jRadBtnGoClassC, 495, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jRadBtnGoClassC, 186, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jRadBtnGoClassF, 450, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jRadBtnGoClassF, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jRadBtnGoClassF, 471, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jRadBtnGoClassF, 185, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabGoType, 403, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabGoType, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabGoType, 416, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabGoType, 148, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jRadBtnGoClassP, 424, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jRadBtnGoClassP, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jRadBtnGoClassP, 187, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jRadBtnQM, 361, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jRadBtnQM, 139, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jRadBtnQM, 385, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jRadBtnQM, 266, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jCombSelSpeGo, 77, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jCombSelSpeGo, 131, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jCombSelSpeGo, 304, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jCombBlastTaxGo, 510, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jCombBlastTaxGo, 131, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jCombBlastTaxGo, 536, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jCombBlastTaxGo, 303, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabGoQtaxID, 79, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabGoQtaxID, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabGoQtaxID, 97, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabGoQtaxID, 123, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jRadBtnElim, 362, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jRadBtnElim, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jRadBtnElim, 385, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jRadBtnElim, 123, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jTxtBGGo, 119, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jTxtBGGo, 104, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jTxtBGGo, 304, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jChkCluster, 200, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jChkCluster, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jChkCluster, 101, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabBGGo, 121, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabBGGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabBGGo, 104, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabAccColGo, 175, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabAccColGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jLabDownValueGo, 297, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jLabDownValueGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jLabDownValueGo, 308, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jLabDownValueGo, 99, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, jBtbSaveGo, 556, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, jBtbSaveGo, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, jBtbSaveGo, 579, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, jBtbSaveGo, 104, SpringLayout.WEST, this);
		setLayout(springLayout);
		add(jBtbSaveGo);
		add(jLabDownValueGo);
		add(jLabAccColGo);
		add(jLabBGGo);
		add(jChkCluster);
		add(jTxtBGGo);
		add(jRadBtnElim);
		add(jLabGoQtaxID);
		add(jCombBlastTaxGo);
		add(jCombSelSpeGo);
		add(jRadBtnQM);
		add(jRadBtnGoClassP);
		add(jLabGoType);
		add(jRadBtnGoClassF);
		add(jRadBtnGoClassC);
		add(jLabValueColGo);
		add(jTxtDownValueGo);
		add(jTxtUpValueGo);
		add(jTxtValColGo);
		add(jTxtAccColGo);
		add(jBtnFileOpenGo);
		add(jBtnBGFileGo);
		add(jButRunGo);
		add(jLabPathGo);
		add(jChkBlastGo);
		add(jLabUpValueGo);
		add(jLabAlgorithm);
		add(jTxtFilePathGo);
		add(jTabbedPaneGoResult);
		add(jScrollPaneInputGo);
		add(jLabInputReviewGo);
		add(jLabResultReviewGo);
		add(jProgressBarGo);
	}
	private void setComponent() {
		btnGroupGoMethod = new ButtonGroup();
		btnGroupGoClass = new ButtonGroup();
		{
			jLabGoQtaxID = new JLabel();
			jLabGoQtaxID.setText("Query Species");
		}
		{
			jTxtFilePathGo = new JTextField();
			jTxtFilePathGo.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent evt) {
					if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
						try {
							setGoProview(jTxtFilePathGo.getText());
						} catch (Exception e) {
							System.out.println("mei you wen jian");
						}
						
					}
				}
			});
		}
		{
			jBtnFileOpenGo = new JButton();
			jBtnFileOpenGo.setText("LoadData");
			jBtnFileOpenGo.setMargin(new java.awt.Insets(1, 1, 1, 1));
			jBtnFileOpenGo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					GUIFileOpen guiFileOpen = new GUIFileOpen();
					String filename = guiFileOpen.openFileName("txt/excel2003", "txt","xls");
					jTxtFilePathGo.setText(filename);
					try {
						setGoProview(jTxtFilePathGo.getText());
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("mei you wen jian");
					}
					
					
				}
			});
		}
		{
			jLabPathGo = new JLabel();
			jLabPathGo.setText("InputData");
		}
		{
			jTxtAccColGo = new JTextField();
			jTxtAccColGo.setDocument(new NumOnlyDoc());
		}
		{
			jLabAccColGo = new JLabel();
			jLabAccColGo.setText("AccIDColNum");
			jLabAccColGo.setAlignmentY(0.0f);
		}
		{
			jLabValueColGo = new JLabel();
			jLabValueColGo.setText("ValueColNum");
			jLabValueColGo.setAlignmentY(0.0f);
		}
		{
			jLabGoType = new JLabel();
			jLabGoType.setText("GO Type");
		}
		{
			jLabUpValueGo = new JLabel();
			jLabUpValueGo.setText("UpValue");
		}
		{
			jTxtUpValueGo = new JTextField();
			jTxtUpValueGo.setDocument(new NumberOnlyDoc(10,4));
		}
		{
			jLabDownValueGo = new JLabel();
			jLabDownValueGo.setText("DownValue");
		}
		{
			jTxtDownValueGo = new JTextField();
			jTxtDownValueGo.setDocument(new NumberOnlyDoc(10,4));
		}
		{
			jBtnBGFileGo = new JButton();
			jBtnBGFileGo.setText("BackGround");
			jBtnBGFileGo.setMargin(new java.awt.Insets(1, 0, 1, 0));
			jBtnBGFileGo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					GUIFileOpen guiFileOpen = new GUIFileOpen();
					String filename = guiFileOpen.openFileName("txt/excel2003", "txt","xls");
					jTxtBGGo.setText(filename);
				}
			});
		}
		{
			jTxtValColGo = new JTextField();
			jTxtValColGo.setDocument(new NumOnlyDoc());
		}

		{
			jScrollPaneInputGo = new JScrollPane();
		}
		{
			jLabInputReviewGo = new JLabel();
			jLabInputReviewGo.setText("InputReview");
		}
		{
			jLabResultReviewGo = new JLabel();
			jLabResultReviewGo.setText("ResultReview");
		}
		{
			jProgressBarGo = new JProgressBar();
		}
		{
			jChkCluster = new JCheckBox();
			jChkCluster.setText("ClusterGO");
			jChkCluster.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (jChkCluster.isSelected()) {
						jTxtDownValueGo.setEnabled(false);
						jTxtUpValueGo.setEnabled(false);
					}
					else {
						jTxtDownValueGo.setEnabled(true);
						jTxtUpValueGo.setEnabled(true);
					}
				}
			});
		}
		{
			jTabbedPaneGoResult = new JTabbedPane();
		}
		{
			jBtbSaveGo = new JButton();
			jBtbSaveGo.setText("Save As");
			jBtbSaveGo.setMargin(new java.awt.Insets(1, 0, 1, 0));
			jBtbSaveGo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					GUIFileOpen guiFileOpen = new GUIFileOpen();
					String savefilename = guiFileOpen.saveFileName("excel2007", "xls");
					CtrlGO ctrlGO = CtrlGO.getInstance();
					if (!FileOperate.getFileNameSep(savefilename)[1].equals("xls")) {
						savefilename = savefilename+".xls";
					}
					ctrlGO.saveExcel(savefilename);
				}
			});
		}
		{
			jButRunGo = new JButton();
			jButRunGo.setText("Analysis");
			jButRunGo.setMargin(new java.awt.Insets(1, 1, 1, 1));
			jButRunGo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					getResult();
				}
			});
		}
		{
			jLabAlgorithm = new JLabel();
			jLabAlgorithm.setText("Algorithm");
		}
		{
			jRadBtnGoClassP = new JRadioButton();
			jRadBtnGoClassP.setText("Biological Process");
			jRadBtnGoClassP.setSelected(true);
			btnGroupGoClass.add(jRadBtnGoClassP);
		}
		{
			jRadBtnGoClassF = new JRadioButton();
			jRadBtnGoClassF.setText("Molecular Function");
			btnGroupGoClass.add(jRadBtnGoClassF);
		}
		{
			jRadBtnGoClassC = new JRadioButton();
			jRadBtnGoClassC.setText("Cellular Component");
			btnGroupGoClass.add(jRadBtnGoClassC);
		}
		{
			jLabBGGo = new JLabel();
			jLabBGGo.setText("BackGround");
			jLabBGGo.setAlignmentY(0.0f);
			jLabBGGo.setAutoscrolls(true);
		}
		{
			jTxtBGGo = new JTextField();
		}
		{
			final HashMap<String, Integer> hashTaxID = CopedID.getSpeciesNameTaxID(false);
			int i = 0;
			ArrayList<String> keys = CopedID.getSpeciesName(false);
			String[] speciesarray = new String[keys.size()+1];
			for(String key:keys)
			{
				speciesarray[i] = key; i++;
			}
			speciesarray[i] = "all";
			ComboBoxModel jCombSelSpeGoModel = new DefaultComboBoxModel(speciesarray);
			jCombSelSpeGo = new JComboBox();
			jCombSelSpeGo.setModel(jCombSelSpeGoModel);
			jCombSelSpeGo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String species = (String) jCombSelSpeGo.getSelectedItem();
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
			jChkBlastGo = new JCheckBox();
			jChkBlastGo.setText("Blast");
		}
		{
			final HashMap<String, Integer> hashTaxID = CopedID.getSpeciesNameTaxID(false);
			int i = 0;
			ArrayList<String> keys = CopedID.getSpeciesName(false);
			String[] speciesarray = new String[keys.size()+1];
			for(String key:keys)
			{
				speciesarray[i] = key; i++;
			}
			speciesarray[i] = "all";
			ComboBoxModel jCombBlastTaxModel = 
				new DefaultComboBoxModel(speciesarray);
			jCombBlastTaxGo = new JComboBox();
			jCombBlastTaxGo.setModel(jCombBlastTaxModel);
			jCombBlastTaxGo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String species = (String) jCombBlastTaxGo.getSelectedItem();
					if (hashTaxID.get(species) == null) {
						StaxID = 0;
					}
					else {
						StaxID =hashTaxID.get(species);
					}
				}
			});
		}
		{
			jRadBtnElim = new JRadioButton();
			jRadBtnElim.setText("Elim Fisher");
			jRadBtnElim.setSelected(true);
			btnGroupGoMethod.add(jRadBtnElim);
		}
		{
			jRadBtnQM = new JRadioButton();
			jRadBtnQM.setText("Novel Fisher");
			btnGroupGoMethod.add(jRadBtnQM);
		}
	}
	
	private void setGroup(GroupLayout jPanGoLayout)
	{
	}
	
	
	
	/**
	 * �鿴�ļ�����������¼���Ӧʱ����
	 */
	private void setGoProview(String filePath)
	{
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(filePath, 1);
		String[][] tableValue = null;
		DefaultTableModel jTabInputGo = new DefaultTableModel(tableValue, lsInfo.get(0));
		JTable jTabFInputGo = new JTable();
		jScrollPaneInputGo.setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(jTabInputGo);
		for (int i = 1; i < lsInfo.size(); i++) {
			jTabInputGo.addRow(lsInfo.get(i));
		}
	}
	/**
	 * analysis����ȥ��õ����
	 */
	private void getResult()
	{
		String geneFileXls = jTxtFilePathGo.getText();
		String GOClass = "";
		if (jRadBtnGoClassC.isSelected()) {
			GOClass = Go2Term.GO_CC;
		}
		else if (jRadBtnGoClassP.isSelected()) {
			GOClass = Go2Term.GO_BP;
		}
		else if (jRadBtnGoClassF.isSelected()) {
			GOClass = Go2Term.GO_MF;
		}
		int colAccID = Integer.parseInt(jTxtAccColGo.getText());
		int colFC = Integer.parseInt(jTxtValColGo.getText());
		String backGroundFile = jTxtBGGo.getText();
		boolean blast = jChkBlastGo.isSelected();
		double evalue = 1e-10;
		boolean elimGo = jRadBtnElim.isSelected();
		CtrlGO ctrlGO = null;
		
		ArrayList<String[]> lsAccID = null;
		if (colAccID != colFC)
			 lsAccID = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID, colFC}, 1, 0);
		else
			lsAccID = ExcelTxtRead.readLsExcelTxt(geneFileXls, new int[]{colAccID}, 1, 0);
		
		ctrlGO = CtrlGO.getInstance(elimGo, GOClass, QtaxID, blast, evalue, StaxID);
		ctrlGO.setLsBG(backGroundFile);
		
		if (!jChkCluster.isSelected() || colAccID == colFC) {
			double up = 0; double down = 0;
			if ( colAccID != colFC) {
				up = Double.parseDouble(jTxtUpValueGo.getText());
				down = Double.parseDouble(jTxtDownValueGo.getText());
			}
			ctrlGO.doInBackgroundNorm(lsAccID, up, down);
			setNormalGo(ctrlGO);
		}
		else {
			ctrlGO.doInBackgroundCluster(lsAccID);
			setNormalGo(ctrlGO);
		}
	}
	
	private void setNormalGo(CtrlGO ctrlGO) {
		//jScrollPaneInputGo �����ķ���
		//jTabbedPaneGOTest ����ı�ǩ��
		//jPanGoTest ����ı�ǩ
		// jScrollPaneGOtest ��ǩ����ķ���
		// jTabFInputGo ������������ݿ�
		// jTabInputGo ��������
		HashMap<String, LinkedHashMap<String, ArrayList<String[]>>> hashResult = ctrlGO.getHashResult();
		jTabbedPaneGoResult.removeAll();
		int i = 0;
		for (Entry<String, LinkedHashMap<String, ArrayList<String[]>>> entry : hashResult.entrySet()) {
			if (i > 2) {
				break;
			}
			for (Entry<String, ArrayList<String[]>> entryTable : entry.getValue().entrySet()) {
				settab(jTabbedPaneGoResult, entry.getKey()+entryTable.getKey(), entryTable.getValue());
			}
			i++;
		}
	}
	
	private void settab(JTabbedPane jTabbedPaneGoResult, String tabName , ArrayList<String[]> lsResult) {
		//���
		String[][] tableValue = null;
		DefaultTableModel jTabResult = new DefaultTableModel(tableValue,lsResult.get(0));
		//�в�
		JTable jTabFResult = new JTable();
		jTabFResult.setModel(jTabResult);
		//���
		JScrollPane jScrollPanelResult = new JScrollPane();
		jScrollPanelResult.setPreferredSize(new java.awt.Dimension(566, 305));
		jScrollPanelResult.setViewportView(jTabFResult);
		//�����
		jTabbedPaneGoResult.addTab(tabName, null, jScrollPanelResult, null);
		for (int i = 1; i < lsResult.size(); i++) {
			jTabResult.addRow(lsResult.get(i));
		}
	}
}