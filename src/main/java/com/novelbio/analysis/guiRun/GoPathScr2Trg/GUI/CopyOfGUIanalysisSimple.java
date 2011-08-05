package com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.swing.SwingConstants;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlGO;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.CtrlNormal;
import com.novelbio.base.gui.DoubleOnlyDoc;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.NumOnlyDoc;


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
public class CopyOfGUIanalysisSimple extends javax.swing.JFrame {
	private JTabbedPane jTabbedPane1;
	private JPanel jPanGo;
	private JPanel jPanPath;
	private JPanel jPanSrctrg;
	private JPanel jPanCoExp;
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
	private JLabel jLabGoQtaxID;
	
	///////  数据框  /////
	private JScrollPane jScrollPaneInputGo;
	private JTable jTabFInputGo;
	private JTable jTabFResult;
	private DefaultTableModel jTabInputGo;
	private DefaultTableModel jTabResult;
	////////////
	static int QtaxID = 0;//查询物种ID
	static int StaxID = 9606;//blast物种ID
	String GoClass = "";
	
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CopyOfGUIanalysisSimple inst = new CopyOfGUIanalysisSimple();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public CopyOfGUIanalysisSimple() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		
		try {
			btnGroupGoMethod = new ButtonGroup();
			btnGroupGoClass = new ButtonGroup();
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jTabbedPane1 = new JTabbedPane();
				getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
				jTabbedPane1.setPreferredSize(new java.awt.Dimension(1035, 682));
				{
					jPanGo = new JPanel();
					jTabbedPane1.addTab("GO Analysis", null, jPanGo, null);
					GroupLayout jPanGoLayout = new GroupLayout((JComponent)jPanGo);
					jPanGo.setLayout(jPanGoLayout);
					jPanGo.setPreferredSize(new java.awt.Dimension(1046, 617));
					jPanGo.setAlignmentX(0.0f);
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
						jTxtUpValueGo.setDocument(new DoubleOnlyDoc());
						jTxtUpValueGo.addKeyListener(new KeyAdapter() {
							public void keyTyped(KeyEvent evt) {
								String old = jTxtUpValueGo.getText();
								if (old.contains(".")&&evt.getKeyChar() == '.') {
									evt.setKeyChar('\0');//沉默
								}
							}
						});

					}
					{
						jLabDownValueGo = new JLabel();
						jLabDownValueGo.setText("DownValue");
					}
					{
						jTxtDownValueGo = new JTextField();
						jTxtDownValueGo.setDocument(new DoubleOnlyDoc());
						jTxtDownValueGo.addKeyListener(new KeyAdapter() {
							public void keyTyped(KeyEvent evt) {
								String old = jTxtDownValueGo.getText();
								if (old.contains(".")&&evt.getKeyChar() == '.') {
									evt.setKeyChar('\0');//沉默
								}
							}
						});

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
						jTabbedPaneGoResult = new JTabbedPane();
					}
					{
						jBtbSaveGo = new JButton();
						jBtbSaveGo.setText("Save As");
						jBtbSaveGo.setMargin(new java.awt.Insets(1, 0, 1, 0));
						jBtbSaveGo.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								GUIFileOpen guiFileOpen = new GUIFileOpen();
								String savefilename = guiFileOpen.saveFileName("excel2003", "xls");
								CtrlGO ctrlGO = CtrlGO.getCtrlGoUsed();
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
					jPanGoLayout.setHorizontalGroup(jPanGoLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanGoLayout.createParallelGroup()
						    .addGroup(jPanGoLayout.createSequentialGroup()
						        .addGroup(jPanGoLayout.createParallelGroup()
						            .addGroup(jPanGoLayout.createSequentialGroup()
						                .addGroup(jPanGoLayout.createParallelGroup()
						                    .addComponent(jRadBtnElim, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
						                    .addComponent(jLabBGGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabAccColGo, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
						                        .addGap(19))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabPathGo, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
						                        .addGap(26))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabDownValueGo, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
						                        .addGap(24))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jBtbSaveGo, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
						                        .addGap(19))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabUpValueGo, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
						                        .addGap(39))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabAlgorithm, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
						                        .addGap(33))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jChkBlastGo, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
						                        .addGap(31)))
						                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						                .addGroup(jPanGoLayout.createParallelGroup()
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jTxtAccColGo, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
						                        .addGroup(jPanGoLayout.createParallelGroup()
						                            .addComponent(jCombBlastTaxGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)
						                            .addComponent(jCombSelSpeGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE))
						                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
						                    .addGroup(jPanGoLayout.createSequentialGroup()
						                        .addGroup(jPanGoLayout.createParallelGroup()
						                            .addComponent(jTxtDownValueGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
						                            .addComponent(jTxtUpValueGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE))
						                        .addGap(43)
						                        .addGroup(jPanGoLayout.createParallelGroup()
						                            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                                .addComponent(jBtnFileOpenGo, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)
						                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
						                            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                                .addPreferredGap(jBtnFileOpenGo, jBtnBGFileGo, LayoutStyle.ComponentPlacement.INDENT)
						                                .addComponent(jBtnBGFileGo, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE))))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jRadBtnQM, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
						                        .addGap(8)
						                        .addComponent(jButRunGo, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
						                        .addGap(6))
						                    .addComponent(jTxtBGGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE)
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jTxtFilePathGo, GroupLayout.PREFERRED_SIZE, 217, GroupLayout.PREFERRED_SIZE)
						                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))))
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addGroup(jPanGoLayout.createParallelGroup()
						                    .addComponent(jRadBtnGoClassP, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabGoQtaxID, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
						                        .addGap(46))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabGoType, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
						                        .addGap(39))
						                    .addComponent(jRadBtnGoClassC, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
						                    .addComponent(jRadBtnGoClassF, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE))
						                .addComponent(jLabValueColGo, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
						                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						                .addComponent(jTxtValColGo, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
						                .addGap(7)))
						        .addGap(8)
						        .addGroup(jPanGoLayout.createParallelGroup()
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addComponent(jLabResultReviewGo, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
						                .addGap(0, 556, Short.MAX_VALUE))
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addComponent(jLabInputReviewGo, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
						                .addGap(0, 586, Short.MAX_VALUE))
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addComponent(jScrollPaneInputGo, GroupLayout.PREFERRED_SIZE, 675, GroupLayout.PREFERRED_SIZE)
						                .addGap(0, 8, Short.MAX_VALUE))
						            .addComponent(jTabbedPaneGoResult, GroupLayout.Alignment.LEADING, 0, 683, Short.MAX_VALUE)))
						    .addComponent(jProgressBarGo, GroupLayout.Alignment.LEADING, 0, 1028, Short.MAX_VALUE))
						.addGap(6));
					jPanGoLayout.setVerticalGroup(jPanGoLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						    .addComponent(jTxtFilePathGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						    .addComponent(jLabInputReviewGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						    .addComponent(jLabPathGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanGoLayout.createParallelGroup()
						    .addComponent(jScrollPaneInputGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 182, GroupLayout.PREFERRED_SIZE)
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
						        .addGap(25)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jTxtAccColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jLabAccColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jLabValueColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jTxtValColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
						        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						    .addComponent(jTxtUpValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						    .addComponent(jLabResultReviewGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						    .addComponent(jLabUpValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
						.addGroup(jPanGoLayout.createParallelGroup()
						    .addComponent(jTabbedPaneGoResult, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 331, GroupLayout.PREFERRED_SIZE)
						    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						        .addGap(29)
						        .addGroup(jPanGoLayout.createParallelGroup()
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addComponent(jLabDownValueGo, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
						                .addGap(11))
						            .addComponent(jTxtDownValueGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
						        .addGap(28)
						        .addComponent(jLabAlgorithm, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jRadBtnQM, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jRadBtnElim, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
						        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						        .addComponent(jLabGoType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						        .addComponent(jRadBtnGoClassP, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						        .addComponent(jRadBtnGoClassF, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						        .addComponent(jRadBtnGoClassC, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						        .addGroup(jPanGoLayout.createParallelGroup()
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addComponent(jChkBlastGo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						                .addGap(8))
						            .addComponent(jCombBlastTaxGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
						        .addGap(34)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jBtbSaveGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jButRunGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
						        .addGap(13)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(jProgressBarGo, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
				}
				{
					jPanPath = new JPanel();
					jTabbedPane1.addTab("Pathway Analysis", null, jPanPath, null);
					GroupLayout jPanPathLayout = new GroupLayout((JComponent)jPanPath);
					jPanPath.setLayout(jPanPathLayout);
					jPanPathLayout.setVerticalGroup(jPanPathLayout.createParallelGroup());
					jPanPathLayout.setHorizontalGroup(jPanPathLayout.createParallelGroup());
				}
				{
					jPanCoExp = new JPanel();
					jTabbedPane1.addTab("CoExpression", null, jPanCoExp, null);
				}
				{
					jPanSrctrg = new JPanel();
					jTabbedPane1.addTab("Signal Network", null, jPanSrctrg, null);
				}
			}
			pack();
			this.setSize(1061, 673);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
///////////////////////////////////////////////////
	/**
	 * 查看文件的鼠标或键盘事件响应时调用
	 */
	private void setGoProview(String filePath)
	{
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(filePath);
		String[][] goRawData = excelOperate.ReadExcel(1, 1, excelOperate.getRowCount(), excelOperate.getColCount());
		String[][] tableValue = null;
		jTabInputGo = new DefaultTableModel(tableValue,goRawData[0]);
		jTabFInputGo = new JTable();
		jScrollPaneInputGo.setViewportView(jTabFInputGo);
		jTabFInputGo.setModel(jTabInputGo);
		for (int i = 1; i < goRawData.length; i++) {
			jTabInputGo.addRow(goRawData[i]);
		}
		
	}
	/////////////////////////////////////////////////
	
	/**
	 * analysis按下去后得到结果
	 */
	private void getResult()
	{
		//jScrollPaneInputGo 最外层的方框
		//jTabbedPaneGOTest 里面的标签框
		//jPanGoTest 具体的标签
		//jScrollPaneGOtest 标签里面的方框
		//jTabFInputGo 方框里面的数据框
		//jTabInputGo 具体数据
		String geneFileXls = jTxtFilePathGo.getText();
		String GOClass = "";
		if (jRadBtnGoClassC.isSelected()) {
			GOClass = "C";
		}
		else if (jRadBtnGoClassP.isSelected()) {
			GOClass = "P";
		}
		else if (jRadBtnGoClassF.isSelected()) {
			GOClass = "F";
		}
		int colAccID = Integer.parseInt(jTxtAccColGo.getText());
		int colFC = Integer.parseInt(jTxtValColGo.getText());
		double up = Double.parseDouble(jTxtUpValueGo.getText());
		double down = Double.parseDouble(jTxtDownValueGo.getText());
		String backGroundFile = jTxtBGGo.getText();
		boolean blast = jChkBlastGo.isSelected();
		double evalue = 1e-10;
		CtrlGO ctrlGO = CtrlGO.getInstance(geneFileXls, GOClass, colAccID, colFC, up, down, backGroundFile, QtaxID, blast, StaxID, evalue);
		try {
			ctrlGO.doInBackground();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error!! the program is stopped");
		}
		ArrayList<ArrayList<String[]>> lsUpResult = ctrlGO.getLsResultUp();
		ArrayList<ArrayList<String[]>> lsDownResult = ctrlGO.getLsResultDown();
		
		
		if (lsUpResult != null) {
			settab(jTabbedPaneGoResult, "UpGoAnalysis", lsUpResult.get(0));
			settab(jTabbedPaneGoResult, "UpGene2GO", lsUpResult.get(1));
			if (blast) {
				settab(jTabbedPaneGoResult, "UpGO2Gene", lsUpResult.get(2));
			}
		}
		if (lsDownResult != null) {
			settab(jTabbedPaneGoResult, "DownGoAnalysis", lsDownResult.get(0));
			settab(jTabbedPaneGoResult, "DownGene2GO", lsDownResult.get(1));
			if (blast) {
				settab(jTabbedPaneGoResult, "DownGO2Gene", lsDownResult.get(2));
			}
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
