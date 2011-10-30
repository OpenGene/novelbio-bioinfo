package com.novelbio.analysis.guiRun.BlastGUI.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Application;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.guiRun.BlastGUI.control.CtrlBlastAnno;
import com.novelbio.analysis.guiRun.BlastGUI.control.CtrlBlastGo;
import com.novelbio.analysis.guiRun.BlastGUI.control.CtrlOther;
import com.novelbio.analysis.guiRun.BlastGUI.control.CtrlBlastPath;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.gui.CtrlNormal;


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
public class GuiBlastJpanel extends JPanel{
	private JTextArea jTxtGenID;
	private JLabel jLbGeneID;
	private JScrollPane jScrollPane1;
	private JRadioButton jRadioButtonPath;
	private JRadioButton jRadioButtonGO;
	private ButtonGroup buttonGroup1;
	private JScrollPane jScrlGOTable;
	private JComboBox jCmbSpeciesBlast;
	private JComboBox jComGOClassSelect;
	private JLabel jLabelTax;
	private JComboBox jCobTaxSelect;
	private JLabel jLbGOandPath;
	private JLabel jLblCond;
	private JButton jBtnGoPath;
	private JButton jBtnAnno;
	private JProgressBar jProgressBar1;
	private JButton jBtnSaveGO;
	private JSeparator jSeparator1;
	private JButton jBtnSaveAno;
	private JButton jBtnGetFile;
	private JCheckBox jChBlast;
	private JTable jTabFAnno;
	private JTable jTabFGoandPath;
	private JScrollPane jScroxTxtGeneID;
	private DefaultTableModel jTabAnno;
	private DefaultTableModel jTabGoandPath;
	
	static int QtaxID = 0;//查询物种ID
//	static int StaxID = 4932;//blast物种ID
	static int StaxID = 9606;//blast物种ID
	String GoClass = "";
	
	/**
	 * 一次最多查询的个数
	 */
	static int numLimit = 100000;
	
	GuiBlastJpanel jBlastJpanel = null;
	public GuiBlastJpanel() {
			GroupLayout jPanBlastLayout = new GroupLayout((JComponent)this);
			setLayout(jPanBlastLayout);
			

			this.setPreferredSize(new java.awt.Dimension(1046, 630));
			setAlignmentX(0.0f);
			setComponent();
			setGroup(jPanBlastLayout);
			Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);
			jBlastJpanel = this;
		
	}
	
	private void setComponent() {
		buttonGroup1 = new ButtonGroup();
		{
			jScroxTxtGeneID = new JScrollPane();
			{
				jTxtGenID = new JTextArea();
				jScroxTxtGeneID.setViewportView(jTxtGenID);
				//jTxtGenID.setPreferredSize(new java.awt.Dimension(173, 247));
			}
		}
		{
			jLbGeneID = new JLabel();
			jLbGeneID.setText("Enter GeneID");
		}
		{
			jBtnGetFile = new JButton();
			jBtnGetFile.setText("OpenFile");
			jBtnGetFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setJTxtFrame();
				}
			});
		}
		{
			jChBlast = new JCheckBox();
			;
			jChBlast.setMargin(new java.awt.Insets(0, 0, 0, 0));
			jChBlast.setName("jChBlast");
		}
		{
			jScrollPane1 = new JScrollPane();
			jScrlGOTable = new JScrollPane();
		}
	
	}
	
	public JButton getJBtnSaveAno() {
		if(jBtnSaveAno == null) {
			jBtnSaveAno = new JButton();
			jBtnSaveAno.setText("SaveAs");
			jBtnSaveAno.setRolloverEnabled(true);
			jBtnSaveAno.setEnabled(false);
			jBtnSaveAno.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (lsAnno != null && lsAnno.size()>0) {
						saveFile(lsAnno,titleAnno);
					}
				}
			});
		}
		return jBtnSaveAno;
	}
	
	private JSeparator getJSeparator1() {
		if(jSeparator1 == null) {
			jSeparator1 = new JSeparator();
		}
		return jSeparator1;
	}
	
	private JScrollPane getJScrlGOTable() {
		if(jScrlGOTable == null) {
			jScrlGOTable = new JScrollPane();
		}
		return jScrlGOTable;
	}
	static int mm = 0;
	public JButton getJBtnSaveGO() {
		if(jBtnSaveGO == null) {
			jBtnSaveGO = new JButton();
			jBtnSaveGO.setText("SaveAs");
			jBtnSaveGO.setEnabled(false);
			jBtnSaveGO.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (lsGoandPath != null && lsGoandPath.size()>0) {
						saveFile(lsGoandPath,titleGoandPath);
					}
				}
			});
		}
		return jBtnSaveGO;
	}
	
	private void setGroup(GroupLayout jPanBlastLayout) {
		jPanBlastLayout.setVerticalGroup(jPanBlastLayout.createSequentialGroup()
				.addGroup(jPanBlastLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jLbGeneID, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jBtnGetFile, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJLblCond(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJCobTaxSelect(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJLabelTax(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(jPanBlastLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
				        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 230, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addGroup(jPanBlastLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				            .addComponent(jChBlast, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
				            .addComponent(getJBtnSaveAno(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				            .addComponent(getJBtnAnno(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				            .addComponent(getJCmbSpeciesBlast(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(getJSeparator1(), GroupLayout.PREFERRED_SIZE, 6, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
				    .addComponent(jScroxTxtGeneID, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 274, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(jPanBlastLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(getJBtnSaveGO(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJRadioButtonGO(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJRadioButtonPath(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJBtnGoPath(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJLbGOandPath(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJCmbGOClassSelect(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getJScrlGOTable(), GroupLayout.PREFERRED_SIZE, 264, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 1, Short.MAX_VALUE)
				.addComponent(getJProgressBar1(), GroupLayout.PREFERRED_SIZE, 9, GroupLayout.PREFERRED_SIZE)
				.addGap(6));
			jPanBlastLayout.setHorizontalGroup(jPanBlastLayout.createParallelGroup()
			.addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
			    .addComponent(getJProgressBar1(), 0, 1034, Short.MAX_VALUE)
			    .addContainerGap())
			.addGroup(jPanBlastLayout.createSequentialGroup()
			    .addGap(7)
			    .addGroup(jPanBlastLayout.createParallelGroup()
			        .addGroup(jPanBlastLayout.createSequentialGroup()
			            .addGroup(jPanBlastLayout.createParallelGroup()
			                .addComponent(getJLbGOandPath(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE)
			                .addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
			                    .addComponent(jScroxTxtGeneID, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)
			                    .addGap(6))
			                .addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
			                    .addComponent(jLbGeneID, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
			                    .addComponent(jBtnGetFile, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
			                    .addGap(6)))
			            .addGroup(jPanBlastLayout.createParallelGroup()
			                .addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
			                    .addComponent(getJBtnGoPath(), GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
			                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			                    .addGroup(jPanBlastLayout.createParallelGroup()
			                        .addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
			                            .addComponent(getJRadioButtonGO(), GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE)
			                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			                            .addComponent(getJCmbGOClassSelect(), 0, 296, Short.MAX_VALUE)
			                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			                            .addComponent(getJRadioButtonPath(), GroupLayout.PREFERRED_SIZE, 135, GroupLayout.PREFERRED_SIZE)
			                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			                            .addGroup(jPanBlastLayout.createParallelGroup()
			                                .addComponent(getJBtnSaveGO(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
			                                .addComponent(getJBtnSaveAno(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE))
			                            .addGap(47))
			                        .addComponent(getJSeparator1(), GroupLayout.Alignment.LEADING, 0, 721, Short.MAX_VALUE)))
			                .addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
			                    .addGroup(jPanBlastLayout.createParallelGroup()
			                        .addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
			                            .addComponent(jChBlast, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
			                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			                            .addComponent(getJCmbSpeciesBlast(), 0, 227, Short.MAX_VALUE)
			                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
			                            .addComponent(getJBtnAnno(), GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE))
			                        .addGroup(GroupLayout.Alignment.LEADING, jPanBlastLayout.createSequentialGroup()
			                            .addGap(24)
			                            .addComponent(getJLblCond(), GroupLayout.PREFERRED_SIZE, 318, GroupLayout.PREFERRED_SIZE)
			                            .addGap(66)))
			                    .addGap(0, 41, GroupLayout.PREFERRED_SIZE)
			                    .addComponent(getJLabelTax(), GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE)
			                    .addComponent(getJCobTaxSelect(), GroupLayout.PREFERRED_SIZE, 228, GroupLayout.PREFERRED_SIZE)
			                    .addGap(6))
			                .addComponent(jScrollPane1, GroupLayout.Alignment.LEADING, 0, 822, Short.MAX_VALUE)))
			        .addComponent(getJScrlGOTable(), GroupLayout.Alignment.LEADING, 0, 1027, Short.MAX_VALUE))
			    .addContainerGap()));
	}
	
	
	
	public JProgressBar getJProgressBar1() {
		if(jProgressBar1 == null) {
			jProgressBar1 = new JProgressBar();
		}
		return jProgressBar1;
	}
	
 
	
	private JRadioButton getJRadioButtonGO() {
		if(jRadioButtonGO == null) {
			jRadioButtonGO = new JRadioButton();
			jRadioButtonGO.setText("GeneOntology");
			jRadioButtonGO.setSelected(true);
			buttonGroup1.add(jRadioButtonGO);
			jRadioButtonGO.setMargin(new java.awt.Insets(2, 0, 2, 0));
			jRadioButtonGO.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					jComGOClassSelect.setEnabled(true);
				}
			});
		}
		return jRadioButtonGO;
	}
	
	private JRadioButton getJRadioButtonPath() {
		if(jRadioButtonPath == null) {
			jRadioButtonPath = new JRadioButton();
			jRadioButtonPath.setText("KEGG Pathway");
			buttonGroup1.add(jRadioButtonPath);
			jRadioButtonPath.setMargin(new java.awt.Insets(2, 0, 2, 0));
			jRadioButtonPath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					jComGOClassSelect.setEnabled(false);
				}
			});
		}
		return jRadioButtonPath;
	}
	
	public DefaultTableModel getJTabAnnol() {
		return jTabAnno;
	}
	public DefaultTableModel getJTabGoandPath() {
		return jTabGoandPath;
	}
	
	CtrlBlastAnno ctrlAnno;
	String[] titleAnno = null;
	public JButton getJBtnAnno() {
		if(jBtnAnno == null) {
			jBtnAnno = new JButton();
			jBtnAnno.setText("Query");
			jBtnAnno.setSize(96, 21);
			jBtnAnno.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					
					String[] queryID = jTxtGenID.getText().split("\n");
					ArrayList<String> lsGenID = new ArrayList<String>();
					for (int i = 0; i < queryID.length; i++) {
						if (queryID[i].trim().equals("")) {
							continue;
						}
						lsGenID.add(queryID[i]);
					}
					List<String> lsGenID2 = null;
					//////////////////一次只能读取3000个
					if (lsGenID.size()>numLimit) {
						JOptionPane.showMessageDialog(null, "To ensure the stability of the database, the gene number of each query is limited in."+numLimit, "alert", JOptionPane.INFORMATION_MESSAGE); 
						lsGenID2 = lsGenID.subList(0, numLimit);
					}
					else
						lsGenID2 = lsGenID;
					///////////////各种设置
					//设置进度条
					getJBtnSaveAno().setEnabled(false);
					jProgressBar1.setMinimum(0); jProgressBar1.setMaximum(lsGenID2.size()-1);
					jProgressBar1.setValue(0);
					getJBtnAnno().setEnabled(false);
					getJLblCond().setText("Running, please be patient.");
					///////////////////
					
						boolean blast = jChBlast.isSelected();
						if (!blast) 
						{
							//设置anno结果框
							{
								titleAnno = new String[3];
								titleAnno[0] ="QueryID";titleAnno[1] ="Symbol/AccID";titleAnno[2] ="Description";
								String[][] tableValue = null;
								jTabAnno = new DefaultTableModel(tableValue,titleAnno);
								jTabFAnno = new JTable();
								jScrollPane1.setViewportView(jTabFAnno);
								jTabFAnno.setModel(jTabAnno);
							}
						}
						else {
							//设置anno结果框
							{
								titleAnno = new String[6];
								titleAnno[0] ="QueryID";titleAnno[1] ="Symbol/AccID";titleAnno[2] ="Description";
								titleAnno[3] ="evalue";titleAnno[4] ="BlastSymbol/AccID";titleAnno[5] ="BlastDescription";
								String[][] tableValue = null;
								jTabAnno = new DefaultTableModel(tableValue,titleAnno);
								jTabFAnno = new JTable();
								jScrollPane1.setViewportView(jTabFAnno);
								jTabFAnno.setModel(jTabAnno);
							}
						}
						ctrlAnno = new CtrlBlastAnno(blast, QtaxID, StaxID, 1e-10,jBlastJpanel );
						ctrlAnno.prepare(lsGenID2);
						ctrlAnno.execute();
						
						//ctrlAnno.done();
					
				}
			});
		}
		return jBtnAnno;
	}

	CtrlBlastGo ctrlGo;
	CtrlBlastPath ctrlPath;
	String[] titleGoandPath = null;
	public JButton getJBtnGoPath() {
		if(jBtnGoPath == null) {
			jBtnGoPath = new JButton();
			jBtnGoPath.setText("Query");
			jBtnGoPath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {


					String[] queryID = jTxtGenID.getText().split("\n");
					ArrayList<String> lsGenID = new ArrayList<String>();
					for (int i = 0; i < queryID.length; i++) {
						if (queryID[i].trim().equals("")) {
							continue;
						}
						lsGenID.add(queryID[i]);
					}
					List<String> lsGenID2 = new ArrayList<String>();
					for (String string : lsGenID) {
						lsGenID2.add(CopedID.removeDot(string));
					}
					
					
					//////////////////一次只能读取3000个
					if (lsGenID.size()>numLimit) {
						JOptionPane.showMessageDialog(null, "To ensure the stability of the database, the gene number of each query is limited in 3000.", "alert", JOptionPane.INFORMATION_MESSAGE); 
						lsGenID2 = lsGenID2.subList(0, numLimit);
					}
					///////////////各种设置
					///////////////各种设置///////////////
					//设置进度条
					getJBtnSaveGO().setEnabled(false);
					jProgressBar1.setMinimum(0); jProgressBar1.setMaximum(lsGenID2.size()-1);
					jProgressBar1.setValue(0);
					getJBtnGoPath().setEnabled(false);
					getJLbGOandPath().setText("Running, please be patient.");
					///////////////////
					if (jRadioButtonGO.isSelected())
					{
						boolean blast = jChBlast.isSelected();
						if (!blast) 
						{
							//设置anno结果框
							{
								titleGoandPath = new String[4];
								titleGoandPath[0] = "QueryID"; titleGoandPath[1] = "Symbol/AccID"; titleGoandPath[2] ="GOID"; titleGoandPath[3] = "GOTerm";
								String[][] tableValue = null;
								jTabGoandPath = new DefaultTableModel(tableValue,titleGoandPath);
								jTabFGoandPath = new JTable();
								jScrlGOTable.setViewportView(jTabFGoandPath);
								jTabFGoandPath.setModel(jTabGoandPath);
							}
						}
						else 
						{
							//设置anno结果框
							{
								titleGoandPath = new String[8];
								titleGoandPath[0] = "QueryID"; titleGoandPath[1] = "Symbol/AccID"; titleGoandPath[2] ="GOID"; titleGoandPath[3] = "GOTerm";
								titleGoandPath[4] = "evalue"; titleGoandPath[5] = "BlastSymbol/AccID"; titleGoandPath[6] ="BlastGOID"; titleGoandPath[7] = "BlastGOTerm";
								String[] columnName = {  "QueryID", "Symbol/AccID","GOID","GOTerm","evalue", "BlastSymbol/AccID","BlastGOID","BlastGOTerm"};
								String[][] tableValue = null;
								jTabGoandPath = new DefaultTableModel(tableValue,columnName);
								jTabFGoandPath = new JTable();
								jScrlGOTable.setViewportView(jTabFGoandPath);
								jTabFGoandPath.setModel(jTabGoandPath);
							}
						}
						ctrlGo = new CtrlBlastGo(blast, QtaxID, StaxID, 1e-10, jBlastJpanel,GoClass);
						ctrlGo.prepare(lsGenID2);
						ctrlGo.execute();
						//ctrlAnno.done();
					}
					else if (jRadioButtonPath.isSelected()) {

						boolean blast = jChBlast.isSelected();
						if (!blast) 
						{
							//设置anno结果框
							{
								titleGoandPath = new String[4];
								titleGoandPath[0] = "QueryID"; titleGoandPath[1] = "Symbol/AccID"; titleGoandPath[2] ="PathID"; titleGoandPath[3] = "PathTitle";
								String[][] tableValue = null;
								jTabGoandPath = new DefaultTableModel(tableValue,titleGoandPath);
								jTabFGoandPath = new JTable();
								jScrlGOTable.setViewportView(jTabFGoandPath);
								jTabFGoandPath.setModel(jTabGoandPath);
							}
						}
						else 
						{
							//设置anno结果框
							{
								titleGoandPath = new String[8];
								titleGoandPath[0] = "QueryID"; titleGoandPath[1] = "Symbol/AccID"; titleGoandPath[2] ="PathID"; titleGoandPath[3] = "PathTitle";
								titleGoandPath[4] = "evalue"; titleGoandPath[5] = "BlastSymbol/AccID"; titleGoandPath[6] ="BlastPathID"; titleGoandPath[7] = "BlastPathTitle";
								String[][] tableValue = null;
								jTabGoandPath = new DefaultTableModel(tableValue,titleGoandPath);
								jTabFGoandPath = new JTable();
								jScrlGOTable.setViewportView(jTabFGoandPath);
								jTabFGoandPath.setModel(jTabGoandPath);
							}
						}
						ctrlPath = new CtrlBlastPath(blast, QtaxID, StaxID, 1e-10, jBlastJpanel);
						ctrlPath.prepare(lsGenID2);
						ctrlPath.execute();
						//ctrlAnno.done();
					}
				}
			});
		}
		return jBtnGoPath;
	}
	
	public JLabel getJLblCond() {
		if(jLblCond == null) {
			jLblCond = new JLabel();
			jLblCond.setText("Prepare");
		}
		return jLblCond;
	}
	
	private void thisWindowClosing(WindowEvent evt) {
		System.out.println("this.windowClosing, event="+evt);
		//TODO add your code for this.windowClosing
	}
	
	public JLabel getJLbGOandPath() {
		if(jLbGOandPath == null) {
			jLbGOandPath = new JLabel();
			jLbGOandPath.setText("Prepare");
		}
		return jLbGOandPath;
	}

///////////////////////////////////////////////////viewer方法实现///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//打开文本选择器
	private String getFileName() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("txt/excel 2003", "txt","xls");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}
	//打开文本选择器
	private String saveFileName() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("txt/excel 2003", "txt","xls");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}
////////////////////////////////////////保存文件////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	ArrayList<String[]> lsAnno = null;
	ArrayList<String[]> lsGoandPath = null;
	public void setLsAnno(ArrayList<String[]> lsAnno) {
		this.lsAnno = lsAnno;
	}
	public void setLsGoandPath(ArrayList<String[]> lsGoandPath) {
		this.lsGoandPath = lsGoandPath;
	}
	///////////////读取文本的第一列，并保存为arraylist-string,错误则返回null并且弹出错误对话框/////////////////////////////////////////////////////////////
	private ArrayList<String> getFirstCol(String filePath) {
		ArrayList<String[]> lstmp = null;
		try {
			lstmp = ExcelTxtRead.getFileToList(filePath, 1, "\t");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "input file formate is not correct.\n Only accept excel2003 or txt file", "Error", JOptionPane.ERROR_MESSAGE); 
			e.printStackTrace();
			return null;
		}
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (String[] strings : lstmp) {
			lsGeneID.add(strings[0]);
		}
		return lsGeneID;
	}
	
	private void setJTxtFrame() {
		String filePathe = getFileName();
		ArrayList<String> lsGeneID = null;
		if (filePathe == null) 
			return;
		
		lsGeneID = getFirstCol(filePathe);

		if (lsGeneID == null)
			return;
		jTxtGenID.setText("");
		for (String string : lsGeneID) {
			jTxtGenID.append(string+"\n");
		}
		
	}
	private JComboBox getJCobTaxSelect() {
		if(jCobTaxSelect == null) {
			final HashMap<String, Integer> hashTaxID = CtrlNormal.getSpecies();
			
			String[] speciesarray = new String[hashTaxID.size()+1];
//			String[] speciesarray = new String[hashTaxID.size()];
			int i = 0;
			Set<String> keys = hashTaxID.keySet();
			for(String key:keys)
			{
				speciesarray[i] = key; i++;
			}
			speciesarray[i] = "test";
			ComboBoxModel jCobTaxSelectModel = 
				new DefaultComboBoxModel(speciesarray);
			jCobTaxSelect = new JComboBox();
			jCobTaxSelect.setModel(jCobTaxSelectModel);
			String species = (String) jCobTaxSelect.getSelectedItem();
			if (hashTaxID.get(species) == null) {
				QtaxID = 0;
			}
			else {
				QtaxID =hashTaxID.get(species);
			}
			jCobTaxSelect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String species = (String) jCobTaxSelect.getSelectedItem();
					if (hashTaxID.get(species) == null) {
						QtaxID = 0;
					}
					else {
						QtaxID =hashTaxID.get(species);
					}
				}
			});
		}
		return jCobTaxSelect;
	}
	
	private JLabel getJLabelTax() {
		if(jLabelTax == null) {
			jLabelTax = new JLabel();
			jLabelTax.setText("select species");
		}
		return jLabelTax;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void saveFile(ArrayList<String[]> lsResult,String[] title) {
		String filePath = saveFileName();
		System.out.println(filePath);
		if (filePath == null)
			return;
		File file = new File(filePath);
		int result = -100;
		if (file.exists()) {
			result = JOptionPane.showConfirmDialog(null, "Are you sure to overwrite the file?", "File Already exist", JOptionPane.YES_NO_OPTION);
			if(result != JOptionPane.OK_OPTION)
				return;
		}
		ExcelOperate excelSave = new ExcelOperate();
		if (!filePath.endsWith(".xls")) {
			filePath=filePath+".xls";
		}
		excelSave.newExcelOpen(filePath);
		lsResult.add(0, title);
		boolean save = excelSave.WriteExcel( 1, 1, lsResult);
		if (save) {
			JOptionPane.showMessageDialog(null, "Your Data Was Saved!", "Save Finished", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public JComboBox getJCmbGOClassSelect() {
		if(jComGOClassSelect == null) {
			ComboBoxModel jCmbGOClassSelectModel = 
				new DefaultComboBoxModel(
						new String[] { "Biological Process", "Molecular Function","Cellular Component","All" });
			jComGOClassSelect = new JComboBox();
			jComGOClassSelect.setModel(jCmbGOClassSelectModel);
			GoClass = (String) jComGOClassSelect.getSelectedItem();
			GoClass =CtrlOther.getGoClass(GoClass);
			jComGOClassSelect.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent evt) {
					GoClass = (String) jComGOClassSelect.getSelectedItem();
					GoClass =CtrlOther.getGoClass(GoClass);
				}
			});
		}
		return jComGOClassSelect;
	}
	
	private JComboBox getJCmbSpeciesBlast() {
//		if(jCmbSpeciesBlast == null) {
//			ComboBoxModel jCmbSpeciesBlastModel = 
//				new DefaultComboBoxModel(
//						new String[] { "Item One", "Item Two" });
//			jCmbSpeciesBlast = new JComboBox();
//			jCmbSpeciesBlast.setModel(jCmbSpeciesBlastModel);
//		}
//		return jCmbSpeciesBlast;
		
		
		
		
		
		
		
		
		

		

		if(jCmbSpeciesBlast == null) {
			final HashMap<String, Integer> hashTaxID = CtrlNormal.getSpecies();
			
			String[] speciesarray = new String[hashTaxID.size()];
			int i = 0;
			Set<String> keys = hashTaxID.keySet();
			for(String key:keys)
			{
				speciesarray[i] = key; i++;
			}
			ComboBoxModel jCobTaxSelectModel = 
				new DefaultComboBoxModel(speciesarray);
			jCmbSpeciesBlast = new JComboBox();
			jCmbSpeciesBlast.setModel(jCobTaxSelectModel);
			String species = (String) jCmbSpeciesBlast.getSelectedItem();
			if (hashTaxID.get(species) == null) {
				StaxID = 0;
			}
			else {
				StaxID =hashTaxID.get(species);
			}
			jCmbSpeciesBlast.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String species = (String) jCmbSpeciesBlast.getSelectedItem();
					if (hashTaxID.get(species) == null) {
						StaxID = 0;
					}
					else {
						StaxID =hashTaxID.get(species);
					}
				}
			});
		}
		return jCmbSpeciesBlast;
	
	}

//	private void initGUI() {
//		try {
//			{
//				this.setPreferredSize(new java.awt.Dimension(754, 388));
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}

	NBCJDialog nbcjDialog;
}
