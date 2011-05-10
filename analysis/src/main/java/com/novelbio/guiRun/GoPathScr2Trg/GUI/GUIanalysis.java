package com.novelbio.guiRun.GoPathScr2Trg.GUI;
import java.awt.BorderLayout;
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
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;


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
public class GUIanalysis extends javax.swing.JFrame {
	private JTabbedPane jTabbedPane1;
	private JPanel jPanGo;
	private JPanel jPanPath;
	private JPanel jPanSrctrg;
	private JPanel jPanCoExp;
	private JButton jButRunGo;
	private JProgressBar jProgressBarGo;
	private JLabel jLabResultReviewGo;
	private JLabel jLabInputReviewGo;
	private JScrollPane jScrollPaneResultGo;
	private JScrollPane jScrollPaneInputGo;
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

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIanalysis inst = new GUIanalysis();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public GUIanalysis() {
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
				{
					jPanGo = new JPanel();
					jTabbedPane1.addTab("GO Analysis", null, jPanGo, null);
					GroupLayout jPanGoLayout = new GroupLayout((JComponent)jPanGo);
					jPanGo.setLayout(jPanGoLayout);
					jPanGo.setPreferredSize(new java.awt.Dimension(1030, 637));
					{
						jLabGoQtaxID = new JLabel();
						jLabGoQtaxID.setText("Query Species");
					}
					{
						jTxtFilePathGo = new JTextField();
					}
					{
						jBtnFileOpenGo = new JButton();
						jBtnFileOpenGo.setText("LoadData");
						jBtnFileOpenGo.setMargin(new java.awt.Insets(1, 1, 1, 1));
					}
					{
						jLabPathGo = new JLabel();
						jLabPathGo.setText("InputData");
					}
					{
						jTxtAccColGo = new JTextField();
					}
					{
						jLabAccColGo = new JLabel();
						jLabAccColGo.setText("AccIDColNum");
						jLabAccColGo.setAlignmentY(0.0f);
					}
					{
						jLabValueColGo = new JLabel();
						jLabValueColGo.setText("ValueColNum");
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
					}
					{
						jLabDownValueGo = new JLabel();
						jLabDownValueGo.setText("DownValue");
					}
					{
						jTxtDownValueGo = new JTextField();
					}
					{
						jBtnBGFileGo = new JButton();
						jBtnBGFileGo.setText("BackGround");
						jBtnBGFileGo.setMargin(new java.awt.Insets(1, 0, 1, 0));
					}
					{
						jTxtValColGo = new JTextField();
					}

					{
						jScrollPaneInputGo = new JScrollPane();
					}
					{
						jScrollPaneResultGo = new JScrollPane();
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
						jButRunGo = new JButton();
						jButRunGo.setText("Analysis");
						jButRunGo.setMargin(new java.awt.Insets(1, 1, 1, 1));
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
					}
					{
						jTxtBGGo = new JTextField();
					}
					{
						ComboBoxModel jCombSelSpeGoModel = 
							new DefaultComboBoxModel(
									new String[] { "Item One", "Item Two" });
						jCombSelSpeGo = new JComboBox();
						jCombSelSpeGo.setModel(jCombSelSpeGoModel);
					}
					{
						jChkBlastGo = new JCheckBox();
						jChkBlastGo.setText("Blast");
					}
					{
						ComboBoxModel jCombBlastTaxModel = 
							new DefaultComboBoxModel(
									new String[] { "Item One", "Item Two" });
						jCombBlastTaxGo = new JComboBox();
						jCombBlastTaxGo.setModel(jCombBlastTaxModel);
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
						                    .addComponent(jLabAccColGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jButRunGo, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
						                        .addGap(7))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabAlgorithm, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
						                        .addGap(16))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jChkBlastGo, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
						                        .addGap(16))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabPathGo, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
						                        .addGap(7))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabUpValueGo, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
						                        .addGap(16))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabDownValueGo, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
						                        .addGap(7))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabBGGo, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
						                        .addGap(7)))
						                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						                .addGroup(jPanGoLayout.createParallelGroup()
						                    .addComponent(jTxtBGGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE)
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addGroup(jPanGoLayout.createParallelGroup()
						                            .addComponent(jTxtDownValueGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
						                            .addComponent(jTxtAccColGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
						                            .addComponent(jTxtUpValueGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE))
						                        .addGroup(jPanGoLayout.createParallelGroup()
						                            .addComponent(jRadBtnQM, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
						                            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                                .addPreferredGap(jRadBtnQM, jLabValueColGo, LayoutStyle.ComponentPlacement.INDENT)
						                                .addComponent(jLabValueColGo, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)))
						                        .addComponent(jTxtValColGo, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
						                        .addGap(8))
						                    .addComponent(jTxtFilePathGo, GroupLayout.Alignment.LEADING, 0, 191, Short.MAX_VALUE)))
						            .addGroup(jPanGoLayout.createSequentialGroup()
						                .addGroup(jPanGoLayout.createParallelGroup()
						                    .addGroup(jPanGoLayout.createSequentialGroup()
						                        .addPreferredGap(jLabGoType, jRadBtnElim, LayoutStyle.ComponentPlacement.INDENT)
						                        .addGroup(jPanGoLayout.createParallelGroup()
						                            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                                .addComponent(jRadBtnElim, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
						                                .addGap(62))
						                            .addComponent(jRadBtnGoClassC, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)
						                            .addComponent(jRadBtnGoClassF, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)
						                            .addComponent(jRadBtnGoClassP, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabGoType, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
						                        .addGap(49))
						                    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                        .addComponent(jLabGoQtaxID, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
						                        .addGap(56)))
						                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						                .addGroup(jPanGoLayout.createParallelGroup()
						                    .addGroup(jPanGoLayout.createSequentialGroup()
						                        .addComponent(jCombBlastTaxGo, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
						                        .addGap(0, 0, Short.MAX_VALUE))
						                    .addGroup(jPanGoLayout.createSequentialGroup()
						                        .addComponent(jCombSelSpeGo, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
						                        .addGap(0, 0, Short.MAX_VALUE))
						                    .addGroup(jPanGoLayout.createSequentialGroup()
						                        .addGap(17)
						                        .addGroup(jPanGoLayout.createParallelGroup()
						                            .addComponent(jBtnFileOpenGo, GroupLayout.Alignment.LEADING, 0, 94, Short.MAX_VALUE)
						                            .addComponent(jBtnBGFileGo, GroupLayout.Alignment.LEADING, 0, 94, Short.MAX_VALUE))))))
						        .addGap(18)
						        .addGroup(jPanGoLayout.createParallelGroup()
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addComponent(jScrollPaneInputGo, GroupLayout.PREFERRED_SIZE, 694, GroupLayout.PREFERRED_SIZE)
						                .addGap(7))
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addComponent(jLabResultReviewGo, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
						                .addGap(574))
						            .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						                .addComponent(jLabInputReviewGo, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
						                .addGap(604))
						            .addComponent(jScrollPaneResultGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 701, GroupLayout.PREFERRED_SIZE))
						        .addGap(12))
						    .addComponent(jProgressBarGo, GroupLayout.Alignment.LEADING, 0, 1032, Short.MAX_VALUE)));
					jPanGoLayout.setVerticalGroup(jPanGoLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						    .addComponent(jTxtFilePathGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						    .addComponent(jLabInputReviewGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						    .addComponent(jLabPathGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanGoLayout.createParallelGroup()
						    .addComponent(jScrollPaneInputGo, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 182, GroupLayout.PREFERRED_SIZE)
						    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						        .addComponent(jBtnFileOpenGo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						        .addGap(18)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jCombSelSpeGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jLabGoQtaxID, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
						        .addGap(19)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jTxtBGGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jLabBGGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
						        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						        .addComponent(jBtnBGFileGo, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jTxtAccColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jLabValueColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jTxtValColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jLabAccColGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						        .addGap(27)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						    .addComponent(jTxtUpValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						    .addComponent(jLabResultReviewGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						    .addComponent(jLabUpValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanGoLayout.createParallelGroup()
						    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						        .addComponent(jScrollPaneResultGo, GroupLayout.PREFERRED_SIZE, 331, GroupLayout.PREFERRED_SIZE)
						        .addGap(0, 9, Short.MAX_VALUE))
						    .addGroup(GroupLayout.Alignment.LEADING, jPanGoLayout.createSequentialGroup()
						        .addGap(7)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jTxtDownValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jLabDownValueGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						        .addGap(36)
						        .addComponent(jLabAlgorithm, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						        .addGap(11)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jRadBtnQM, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jRadBtnElim, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						        .addGap(35)
						        .addComponent(jLabGoType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						        .addGap(14)
						        .addComponent(jRadBtnGoClassP, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						        .addComponent(jRadBtnGoClassF, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						        .addComponent(jRadBtnGoClassC, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						        .addGap(23)
						        .addGroup(jPanGoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						            .addComponent(jCombBlastTaxGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						            .addComponent(jChkBlastGo, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						        .addGap(15)
						        .addComponent(jButRunGo, 0, 31, Short.MAX_VALUE)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(jProgressBarGo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap());
				}
				{
					jPanPath = new JPanel();
					jTabbedPane1.addTab("Pathway Analysis", null, jPanPath, null);
					GroupLayout jPanPathLayout = new GroupLayout((JComponent)jPanPath);
					jPanPath.setLayout(jPanPathLayout);
					jPanPathLayout.setVerticalGroup(jPanPathLayout.createSequentialGroup());
					jPanPathLayout.setHorizontalGroup(jPanPathLayout.createSequentialGroup());
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
			this.setSize(1049, 699);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
	

}
