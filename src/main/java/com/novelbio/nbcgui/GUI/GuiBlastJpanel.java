package com.novelbio.nbcgui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Application;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.nbcgui.controlquery.CtrlBlastAnno;
import com.novelbio.nbcgui.controlquery.CtrlBlastGo;
import com.novelbio.nbcgui.controlquery.CtrlBlastPath;
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
public class GuiBlastJpanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -610030039829839849L;
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
		

			this.setPreferredSize(new java.awt.Dimension(1046, 630));
			setAlignmentX(0.0f);
			setComponent();
			springLayout = new SpringLayout();
			springLayout.putConstraint(SpringLayout.NORTH, jScrollPane1, 32, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jScrollPane1, 212, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.SOUTH, jScrollPane1, 262, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.EAST, jScrollPane1, 1034, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.NORTH, jChBlast, 270, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jChBlast, 212, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.SOUTH, jChBlast, 290, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.EAST, jChBlast, 279, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.NORTH, jBtnGetFile, 3, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jBtnGetFile, 109, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.SOUTH, jBtnGetFile, 23, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.NORTH, jLbGeneID, 0, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jLbGeneID, 7, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.SOUTH, jLbGeneID, 26, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.EAST, jLbGeneID, 109, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.NORTH, jScroxTxtGeneID, 32, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jScroxTxtGeneID, 7, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.SOUTH, jScroxTxtGeneID, 306, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.EAST, jScroxTxtGeneID, 206, SpringLayout.WEST, this);
			setLayout(springLayout);
			add(getJLbGOandPath());
			add(jScroxTxtGeneID);
			add(jLbGeneID);
			add(jBtnGetFile);
			add(getJBtnGoPath());
			add(getJRadioButtonGO());
			add(getJCmbGOClassSelect());
			add(getJRadioButtonPath());
			add(getJBtnSaveGO());
			add(getJBtnSaveAno());
			add(getJSeparator1());
			add(jChBlast);
			add(getJCmbSpeciesBlast());
			add(getJBtnAnno());
			add(getJLblCond());
			add(getJLabelTax());
			add(getJCobTaxSelect());
			add(jScrollPane1);
			add(getJScrlGOTable());
			add(getJProgressBar1());
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
			springLayout.putConstraint(SpringLayout.NORTH, jBtnSaveAno, 268, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jBtnSaveAno, 894, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jBtnSaveAno, 987, SpringLayout.WEST, this);
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
			springLayout.putConstraint(SpringLayout.NORTH, jSeparator1, 298, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jSeparator1, 312, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.SOUTH, jSeparator1, 304, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.EAST, jSeparator1, 1034, SpringLayout.WEST, this);
		}
		return jSeparator1;
	}
	
	private JScrollPane getJScrlGOTable() {
		if(jScrlGOTable == null) {
			jScrlGOTable = new JScrollPane();
			springLayout.putConstraint(SpringLayout.NORTH, jScrlGOTable, 336, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jScrlGOTable, 7, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.SOUTH, jScrlGOTable, 609, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.EAST, jScrlGOTable, 1034, SpringLayout.WEST, this);
		}
		return jScrlGOTable;
	}
	static int mm = 0;
	public JButton getJBtnSaveGO() {
		if(jBtnSaveGO == null) {
			jBtnSaveGO = new JButton();
			springLayout.putConstraint(SpringLayout.NORTH, jBtnSaveGO, 306, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jBtnSaveGO, 894, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jBtnSaveGO, 987, SpringLayout.WEST, this);
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
				
	}
	
	
	
	public JProgressBar getJProgressBar1() {
		if(jProgressBar1 == null) {
			jProgressBar1 = new JProgressBar();
			springLayout.putConstraint(SpringLayout.NORTH, jProgressBar1, 615, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jProgressBar1, 7, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.SOUTH, jProgressBar1, 623, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.EAST, jProgressBar1, 1034, SpringLayout.WEST, this);
		}
		return jProgressBar1;
	}
	
 
	
	private JRadioButton getJRadioButtonGO() {
		if(jRadioButtonGO == null) {
			jRadioButtonGO = new JRadioButton();
			springLayout.putConstraint(SpringLayout.NORTH, jRadioButtonGO, 307, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jRadioButtonGO, 312, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jRadioButtonGO, 444, SpringLayout.WEST, this);
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
			springLayout.putConstraint(SpringLayout.NORTH, jRadioButtonPath, 307, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jRadioButtonPath, 755, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jRadioButtonPath, 890, SpringLayout.WEST, this);
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
			springLayout.putConstraint(SpringLayout.NORTH, jBtnAnno, 268, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jBtnAnno, 524, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jBtnAnno, 620, SpringLayout.WEST, this);
			jBtnAnno.setText("Query");
			jBtnAnno.setSize(96, 21);
			jBtnAnno.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					
					String[] queryID = jTxtGenID.getText().split("\n");
					ArrayList<String> lsAccID = new ArrayList<String>();
					for (int i = 0; i < queryID.length; i++) {
						if (queryID[i].trim().equals("")) {
							continue;
						}
						lsAccID.add(queryID[i]);
					}
					List<String> lsGenID2 = null;
					//////////////////一次只能读取3000个
					if (lsAccID.size()>numLimit) {
						JOptionPane.showMessageDialog(null, "To ensure the stability of the database, the gene number of each query is limited in."+numLimit, "alert", JOptionPane.INFORMATION_MESSAGE); 
						lsGenID2 = lsAccID.subList(0, numLimit);
					}
					else
						lsGenID2 = lsAccID;
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
								titleAnno = new String[4];
								titleAnno[0] ="QueryID";titleAnno[1] ="Symbol/AccID";titleAnno[2] ="Description";titleAnno[3] ="KeggID";
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
								titleAnno = new String[7];
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
			springLayout.putConstraint(SpringLayout.NORTH, jBtnGoPath, 306, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jBtnGoPath, 212, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jBtnGoPath, 308, SpringLayout.WEST, this);
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
					if (lsGenID.size() > numLimit) {
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
			springLayout.putConstraint(SpringLayout.NORTH, jLblCond, 6, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jLblCond, 236, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jLblCond, 554, SpringLayout.WEST, this);
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
			springLayout.putConstraint(SpringLayout.NORTH, jLbGOandPath, 311, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jLbGOandPath, 7, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jLbGOandPath, 212, SpringLayout.WEST, this);
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
			final HashMap<String, Integer> hashTaxID = CopedID.getSpeciesNameTaxID(false);
			
			
//			String[] speciesarray = new String[hashTaxID.size()];
			int i = 0;
			ArrayList<String> keys = CopedID.getSpeciesName(false);
			String[] speciesarray = new String[keys.size()+1];
			for(String key:keys)
			{
				speciesarray[i] = key; i++;
			}
			speciesarray[i] = "test";
			ComboBoxModel jCobTaxSelectModel = 
				new DefaultComboBoxModel(speciesarray);
			jCobTaxSelect = new JComboBox();
			springLayout.putConstraint(SpringLayout.NORTH, jCobTaxSelect, 2, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jCobTaxSelect, 800, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jCobTaxSelect, 1028, SpringLayout.WEST, this);
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
			springLayout.putConstraint(SpringLayout.NORTH, jLabelTax, 6, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jLabelTax, 661, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jLabelTax, 800, SpringLayout.WEST, this);
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
						new String[] { Go2Term.GO_BP, Go2Term.GO_MF,Go2Term.GO_CC, Go2Term.GO_ALL});
			jComGOClassSelect = new JComboBox();
			springLayout.putConstraint(SpringLayout.NORTH, jComGOClassSelect, 307, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jComGOClassSelect, 448, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jComGOClassSelect, 751, SpringLayout.WEST, this);
			jComGOClassSelect.setModel(jCmbGOClassSelectModel);
			GoClass = (String) jComGOClassSelect.getSelectedItem();
			jComGOClassSelect.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent evt) {
					GoClass = (String) jComGOClassSelect.getSelectedItem();
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
			final HashMap<String, Integer> hashTaxID = CopedID.getSpeciesNameTaxID(false);
			int i = 0;
			ArrayList<String> keys = CopedID.getSpeciesName(false);
			String[] speciesarray = new String[keys.size()];
			for(String key:keys)
			{
				speciesarray[i] = key; i++;
			}
			ComboBoxModel jCobTaxSelectModel = 
				new DefaultComboBoxModel(speciesarray);
			jCmbSpeciesBlast = new JComboBox();
			springLayout.putConstraint(SpringLayout.NORTH, jCmbSpeciesBlast, 269, SpringLayout.NORTH, this);
			springLayout.putConstraint(SpringLayout.WEST, jCmbSpeciesBlast, 283, SpringLayout.WEST, this);
			springLayout.putConstraint(SpringLayout.EAST, jCmbSpeciesBlast, 512, SpringLayout.WEST, this);
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

	NBCJDialog nbcjDialog;
	private SpringLayout springLayout;
}
