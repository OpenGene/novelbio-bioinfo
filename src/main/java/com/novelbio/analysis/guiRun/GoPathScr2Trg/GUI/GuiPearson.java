package com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Application;

import com.novelbio.analysis.annotation.pathway.kegg.kGpath.Scr2Target;
import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.analysis.coexp.simpCoExp.SimpCoExp;
import com.novelbio.analysis.guiRun.GoPathScr2Trg.control.CtrlPath;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.CtrlNormal;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.NumOnlyDoc;
import com.novelbio.base.gui.NumberOnlyDoc;


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
public class GuiPearson extends JPanel{

	private JTabbedPane jTabbedPanePathResult;
	private JButton jBtbSavePath;
	private JProgressBar jProgressBarPath;
	private JLabel jLabResultReviewPath;
	private JLabel jLabInputReviewPath;
	private JLabel jLabPathPath;
	private JTextField jTxtFilePathPath;
	private JButton jBtnFileOpenPath;
	private JLabel jLabAccColPath;
	private JTextField jTxtAccColPath;
	private JComboBox jCombSelSpePath;
	private JLabel jLabPathQtaxID;
	private JScrollPane jScrollPaneInputPath;
	////////////
	static int QtaxID = 0;//��ѯ����ID
	static int StaxID = 9606;//blast����ID
	
	
	public GuiPearson() 
	{
		GroupLayout jPanPathLayout = new GroupLayout((JComponent)this);
		

		setPreferredSize(new java.awt.Dimension(1046, 617));
		this.setLayout(jPanPathLayout);
	
		setAlignmentX(0.0f);
		setComponent();
		setGroup(jPanPathLayout);
	}
	private void setComponent() {
		{
			jLabPathQtaxID = new JLabel();
			jLabPathQtaxID.setText("Query Species");
		}
		{
			jTxtFilePathPath = new JTextField();
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
			jTxtAccColPath = new JTextField();
			jTxtAccColPath.setDocument(new NumberOnlyDoc(5));
		}
		{
			jLabAccColPath = new JLabel();
			;
			jLabAccColPath.setAlignmentY(0.0f);
			jLabAccColPath.setName("jLabAccColPath");
		}

		{
			jScrollPaneInputPath = new JScrollPane();
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
			jTabbedPanePathResult = new JTabbedPane();
		}
		{
			jBtbSavePath = new JButton();
			jBtbSavePath.setText("Save As");
			jBtbSavePath.setMargin(new java.awt.Insets(1, 0, 1, 0));
			jBtbSavePath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					GUIFileOpen guiFileOpen = new GUIFileOpen();
					String savefilename = guiFileOpen.saveFileName("xls", "xls");
					CtrlPath ctrlPath = CtrlPath.getCtrlPathUsed();
					if (!FileOperate.getFileNameSep(savefilename)[1].equals("xls")) {
						savefilename = savefilename+".xls";
					}
					try {
						ExcelOperate excelOperate = new ExcelOperate();
						excelOperate.openExcel(jTxtFilePathPath.getText());
						int ColNum = excelOperate.getColCount(1);
						String[][] aaa = excelOperate.ReadExcel(1, 1, 1, ColNum);
						ColNum = 0;
						for (int i = 0; i < aaa[0].length; i++) {
							if (aaa[0][i]!=null && !aaa[0][i].trim().equals("")) {
								ColNum++;
							}
						}
						int[] columnID = new int[ColNum];
						for (int i = 0; i < ColNum ; i++) {
							columnID[i] = i+1;
						}
						SimpCoExp.getCoExpInfo(jTxtFilePathPath.getText(), columnID, 9606, 0.05, savefilename, false);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			});
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
	}
	
	
	private void setGroup(GroupLayout jPanPathLayout)
	{	jPanPathLayout.setHorizontalGroup(jPanPathLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(jPanPathLayout.createParallelGroup()
			    .addGroup(jPanPathLayout.createSequentialGroup()
			        .addGroup(jPanPathLayout.createParallelGroup()
			            .addComponent(jTxtFilePathPath, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 217, GroupLayout.PREFERRED_SIZE)
			            .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
			                .addComponent(jCombSelSpePath, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
			                .addGap(43))
			            .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
			                .addComponent(jLabPathQtaxID, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
			                .addGap(88))
			            .addGroup(jPanPathLayout.createSequentialGroup()
			                .addGroup(jPanPathLayout.createParallelGroup()
			                    .addComponent(jLabAccColPath, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
			                    .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
			                        .addComponent(jLabPathPath, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
			                        .addGap(19))
			                    .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
			                        .addComponent(jBtbSavePath, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
			                        .addGap(12)))
			                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			                .addGroup(jPanPathLayout.createParallelGroup()
			                    .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
			                        .addComponent(jTxtAccColPath, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
			                        .addGap(62))
			                    .addComponent(jBtnFileOpenPath, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE))))
			        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
			        .addGroup(jPanPathLayout.createParallelGroup()
			            .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
			                .addComponent(jLabInputReviewPath, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
			                .addGap(0, 697, Short.MAX_VALUE))
			            .addGroup(jPanPathLayout.createSequentialGroup()
			                .addComponent(jScrollPaneInputPath, GroupLayout.PREFERRED_SIZE, 794, GroupLayout.PREFERRED_SIZE)
			                .addGap(0, 0, Short.MAX_VALUE))
			            .addGroup(jPanPathLayout.createSequentialGroup()
			                .addGap(0, 0, Short.MAX_VALUE)
			                .addComponent(jTabbedPanePathResult, GroupLayout.PREFERRED_SIZE, 794, GroupLayout.PREFERRED_SIZE))
			            .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
			                .addGap(7)
			                .addComponent(jLabResultReviewPath, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
			                .addGap(0, 660, Short.MAX_VALUE))))
			    .addComponent(jProgressBarPath, GroupLayout.Alignment.LEADING, 0, 1023, Short.MAX_VALUE))
			.addContainerGap());
		jPanPathLayout.setVerticalGroup(jPanPathLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(jPanPathLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jLabPathPath, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jLabInputReviewPath, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(jPanPathLayout.createParallelGroup()
				    .addComponent(jScrollPaneInputPath, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
				    .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
				        .addComponent(jTxtFilePathPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jBtnFileOpenPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jLabPathQtaxID, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jCombSelSpePath, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
				        .addGap(92)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jLabResultReviewPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(jPanPathLayout.createParallelGroup()
				    .addComponent(jTabbedPanePathResult, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 331, GroupLayout.PREFERRED_SIZE)
				    .addGroup(GroupLayout.Alignment.LEADING, jPanPathLayout.createSequentialGroup()
				        .addGap(10)
				        .addGroup(jPanPathLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				            .addComponent(jTxtAccColPath, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
				            .addComponent(jLabAccColPath, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 46, GroupLayout.PREFERRED_SIZE)
				        .addComponent(jBtbSavePath, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
				        .addGap(232)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(jProgressBarPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(0, 6, Short.MAX_VALUE));
		Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);
	}
	/**
	 * �鿴�ļ�����������¼���Ӧʱ����
	 */
	private void setPathProview(String filePath)
	{
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(filePath);
		String[][] PathRawData = excelOperate.ReadExcel(1, 1, excelOperate.getRowCount(), excelOperate.getColCount());
		String[][] tableValue = null;
		DefaultTableModel jTabInputPath = new DefaultTableModel(tableValue,PathRawData[0]);
		JTable jTabFInputPath = new JTable();
		jScrollPaneInputPath.setViewportView(jTabFInputPath);
		jTabFInputPath.setModel(jTabInputPath);
		for (int i = 1; i < PathRawData.length; i++) {
			jTabInputPath.addRow(PathRawData[i]);
		}
		
	}
	
}