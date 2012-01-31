package com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI;

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

import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlGO;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.CtrlNormal;
import com.novelbio.base.gui.NumberOnlyDoc;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.NumOnlyDoc;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgo.GOInfoAbs;


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
	static int QtaxID = 0;//查询物种ID
	static int StaxID = 9606;//blast物种ID
	String GoClass = "";
	
	
	public GuiGoJPanel() {
		GroupLayout jPanGoLayout = new GroupLayout((JComponent)this);
		setLayout(jPanGoLayout);
	

		this.setPreferredSize(new java.awt.Dimension(1046, 644));
		setAlignmentX(0.0f);
		setComponent();
		setGroup(jPanGoLayout);
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
					String savefilename = guiFileOpen.saveFileName("excel2007", "xlsx");
					CtrlGO ctrlGO = CtrlGO.getInstance();
					if (!FileOperate.getFileNameSep(savefilename)[1].equals("xlsx")) {
						savefilename = savefilename+".xlsx";
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
			final HashMap<String, Integer> hashTaxID = CtrlNormal.getSpecies();
			String[] speciesarray = new String[hashTaxID.size()+1];
			int i = 0;
			Set<String> keys = hashTaxID.keySet();
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
			final HashMap<String, Integer> hashTaxID = CtrlNormal.getSpecies();
			String[] speciesarray = new String[hashTaxID.size()+1];
			int i = 0;
			Set<String> keys = hashTaxID.keySet();
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
	{jPanGoLayout.setVerticalGroup(jPanGoLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    .addComponent(jTxtFilePathGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			    .addComponent(jLabInputReviewGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			    .addComponent(jLabPathGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addGroup(jPanGoLayout.createParallelGroup()
			    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
			        .addComponent(jBtnFileOpenGo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			        .addGap(19)
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jCombSelSpeGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jLabGoQtaxID, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
			        .addGap(19)
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jTxtBGGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jLabBGGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
			        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			        .addComponent(jBtnBGFileGo, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
			        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jTxtAccColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jLabAccColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
			        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
			        .addComponent(jChkCluster, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jTxtValColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jLabValueColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
			    .addComponent(jScrollPaneInputGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE))
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addComponent(jLabResultReviewGo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(jPanGoLayout.createParallelGroup()
			    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jTxtUpValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jLabUpValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
			        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jTxtDownValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jLabDownValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE))
			        .addGap(17)
			        .addComponent(jLabAlgorithm, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
			        .addGap(16)
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jRadBtnQM, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jRadBtnElim, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
			        .addGap(18)
			        .addComponent(jLabGoType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
			        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
			        .addComponent(jRadBtnGoClassP, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
			        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
			        .addComponent(jRadBtnGoClassF, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
			        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
			        .addComponent(jRadBtnGoClassC, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
			        .addGap(15)
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jCombBlastTaxGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jChkBlastGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
			        .addGap(19)
			        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			            .addComponent(jButRunGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
			            .addComponent(jBtbSaveGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
			        .addGap(7))
			    .addComponent(jTabbedPaneGoResult, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 331, GroupLayout.PREFERRED_SIZE))
			.addGap(23)
			.addComponent(jProgressBarGo, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		jPanGoLayout.setHorizontalGroup(jPanGoLayout.createSequentialGroup()
		.addContainerGap()
		.addGroup(jPanGoLayout.createParallelGroup()
		    .addGroup(jPanGoLayout.createSequentialGroup()
		        .addGroup(jPanGoLayout.createParallelGroup()
		            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                .addGroup(jPanGoLayout.createParallelGroup()
		                    .addComponent(jBtbSaveGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
		                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                        .addComponent(jLabDownValueGo, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
		                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
		                    .addComponent(jLabAccColGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jLabBGGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jChkCluster, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE))
		                .addComponent(jTxtBGGo, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
		            .addGroup(jPanGoLayout.createSequentialGroup()
		                .addGroup(jPanGoLayout.createParallelGroup()
		                    .addComponent(jRadBtnElim, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jLabGoQtaxID, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE))
		                .addGap(8)
		                .addGroup(jPanGoLayout.createParallelGroup()
		                    .addComponent(jCombBlastTaxGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jCombSelSpeGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)
		                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                        .addGap(8)
		                        .addComponent(jRadBtnQM, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
		                        .addGap(38))))
		            .addGroup(jPanGoLayout.createSequentialGroup()
		                .addGroup(jPanGoLayout.createParallelGroup()
		                    .addComponent(jRadBtnGoClassP, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
		                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                        .addComponent(jLabGoType, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
		                        .addGap(39))
		                    .addComponent(jRadBtnGoClassF, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jRadBtnGoClassC, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
		                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                        .addComponent(jLabValueColGo, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
		                        .addGap(12)
		                        .addGroup(jPanGoLayout.createParallelGroup()
		                            .addComponent(jTxtDownValueGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
		                            .addComponent(jTxtUpValueGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
		                            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                                .addComponent(jTxtValColGo, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
		                                .addGap(25))
		                            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                                .addComponent(jTxtAccColGo, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
		                                .addGap(26)))))
		                .addGap(13)
		                .addGroup(jPanGoLayout.createParallelGroup()
		                    .addComponent(jBtnFileOpenGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
		                    .addGroup(jPanGoLayout.createSequentialGroup()
		                        .addGap(7)
		                        .addGroup(jPanGoLayout.createParallelGroup()
		                            .addComponent(jBtnBGFileGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
		                            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                                .addGap(16)
		                                .addComponent(jButRunGo, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))))))
		            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                .addGroup(jPanGoLayout.createParallelGroup()
		                    .addComponent(jLabPathGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
		                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                        .addComponent(jChkBlastGo, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
		                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
		                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                        .addComponent(jLabUpValueGo, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
		                        .addGap(13))
		                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                        .addComponent(jLabAlgorithm, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
		                        .addGap(7)))
		                .addComponent(jTxtFilePathGo, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE)))
		        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
		        .addGroup(jPanGoLayout.createParallelGroup()
		            .addGroup(jPanGoLayout.createSequentialGroup()
		                .addComponent(jTabbedPaneGoResult, GroupLayout.PREFERRED_SIZE, 723, GroupLayout.PREFERRED_SIZE)
		                .addGap(0, 0, Short.MAX_VALUE))
		            .addGroup(jPanGoLayout.createSequentialGroup()
		                .addComponent(jScrollPaneInputGo, GroupLayout.PREFERRED_SIZE, 723, GroupLayout.PREFERRED_SIZE)
		                .addGap(0, 0, Short.MAX_VALUE))
		            .addGroup(jPanGoLayout.createSequentialGroup()
		                .addGap(16)
		                .addGroup(jPanGoLayout.createParallelGroup()
		                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		                        .addComponent(jLabInputReviewGo, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
		                        .addGap(0, 30, Short.MAX_VALUE))
		                    .addGroup(jPanGoLayout.createSequentialGroup()
		                        .addComponent(jLabResultReviewGo, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
		                        .addGap(0, 0, Short.MAX_VALUE)))
		                .addGap(580))))
		    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
		        .addComponent(jProgressBarGo, 0, 1027, Short.MAX_VALUE)
		        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
		.addGap(7));
	}
	
	
	
	/**
	 * 查看文件的鼠标或键盘事件响应时调用
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
	 * analysis按下去后得到结果
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
		//jScrollPaneInputGo 最外层的方框
		//jTabbedPaneGOTest 里面的标签框
		//jPanGoTest 具体的标签
		// jScrollPaneGOtest 标签里面的方框
		// jTabFInputGo 方框里面的数据框
		// jTabInputGo 具体数据
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
		//里层
		String[][] tableValue = null;
		DefaultTableModel jTabResult = new DefaultTableModel(tableValue,lsResult.get(0));
		//中层
		JTable jTabFResult = new JTable();
		jTabFResult.setModel(jTabResult);
		//外层
		JScrollPane jScrollPanelResult = new JScrollPane();
		jScrollPanelResult.setPreferredSize(new java.awt.Dimension(566, 305));
		jScrollPanelResult.setViewportView(jTabFResult);
		//最外层
		jTabbedPaneGoResult.addTab(tabName, null, jScrollPanelResult, null);
		for (int i = 1; i < lsResult.size(); i++) {
			jTabResult.addRow(lsResult.get(i));
		}
	}
}
