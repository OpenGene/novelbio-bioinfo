package com.novelBio.guiRun.Degree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.novelBio.guiRun.BlastGUI.control.CtrlOther;



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
public class DegreeGUI extends javax.swing.JFrame {
	private JComboBox jCBSelectTaxID;
	private JButton jBtnSave;
	private JButton jBtnRun;
	private JTextField jTxtSave;
	private JButton jBtnOpen;
	private JTextField jTxtOpenPath;

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DegreeGUI inst = new DegreeGUI();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public DegreeGUI() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
//				ComboBoxModel jCBSelectTaxIDModel = 
//					new DefaultComboBoxModel(
//							new String[] { "Item One", "Item Two" });
//				jCBSelectTaxID = new JComboBox();
//				jCBSelectTaxID.setModel(jCBSelectTaxIDModel);
				final HashMap<String, Integer> hashTaxID = CtrlOther.getSpecies();
				
				String[] speciesarray = new String[hashTaxID.size()+1];
				int i = 0;
				Set<String> keys = hashTaxID.keySet();
				for(String key:keys)
				{
					speciesarray[i] = key; i++;
				}
				speciesarray[i] = "test";
				ComboBoxModel jCobTaxSelectModel = 
					new DefaultComboBoxModel(speciesarray);
				jCBSelectTaxID = new JComboBox();
				jCBSelectTaxID.setModel(jCobTaxSelectModel);
				String species = (String) jCBSelectTaxID.getSelectedItem();
				if (hashTaxID.get(species) == null) {
					QtaxID = 0;
				}
				else {
					QtaxID =hashTaxID.get(species);
				}
				
				jCBSelectTaxID.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						String species = (String) jCBSelectTaxID.getSelectedItem();
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
				jTxtOpenPath = new JTextField();
			}
			{
				jBtnOpen = new JButton();
				jBtnOpen.setText("OpenPath");
				jBtnOpen.setMargin(new java.awt.Insets(1, 1, 1, 1));
				jBtnOpen.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						String pathOpen = openFileName();
						jTxtOpenPath.setText(pathOpen);
					}
				});
			}
			{
				jBtnSave = new JButton();
				jBtnSave.setText("SavePath");
				jBtnSave.setMargin(new java.awt.Insets(1, 1, 1, 1));
				jBtnSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						String pathSave = saveFileName();
						jTxtSave.setText(pathSave);
					}
				});
			}
			{
				jTxtSave = new JTextField();
			}
			{
				jBtnRun = new JButton();
				jBtnRun.setText("Run");
				jBtnRun.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						String inFile = jTxtOpenPath.getText();
						String outFile = jTxtSave.getText();
						Control.getresult(inFile, outFile, QtaxID);
					}
				});
			}
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addContainerGap(19, 19)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jTxtOpenPath, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jBtnOpen, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(19)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jBtnSave, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jTxtSave, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(25)
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(getJCBSelectTaxID(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, Short.MAX_VALUE))
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jBtnRun, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 0, Short.MAX_VALUE)))
				.addContainerGap(139, 139));
			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(getJCBSelectTaxID(), GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
				        .addGap(122)
				        .addComponent(jBtnRun, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 10, Short.MAX_VALUE))
				    .addGroup(thisLayout.createSequentialGroup()
				        .addGroup(thisLayout.createParallelGroup()
				            .addComponent(jBtnOpen, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
				            .addComponent(jBtnSave, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE))
				        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				        .addGroup(thisLayout.createParallelGroup()
				            .addGroup(thisLayout.createSequentialGroup()
				                .addComponent(jTxtOpenPath, GroupLayout.PREFERRED_SIZE, 269, GroupLayout.PREFERRED_SIZE)
				                .addGap(0, 0, Short.MAX_VALUE))
				            .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                .addComponent(jTxtSave, GroupLayout.PREFERRED_SIZE, 264, GroupLayout.PREFERRED_SIZE)
				                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 0, Short.MAX_VALUE)))))
				.addContainerGap());
			pack();
			setSize(400, 300);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
	int QtaxID = 0;
	public JComboBox getJCBSelectTaxID() {
		return jCBSelectTaxID;
	}
///////////////////////////////////////////////////viewer方法实现///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//打开文本选择器
	private String openFileName() {
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
}
