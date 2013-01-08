package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controlquery.CtrlBatchAnnoGene;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JProgressBar;
/**
 * 批量注释，各种注释
 * @author zong0jie
 *
 */
public class GuiAnnoGene extends JPanel implements GuiNeedOpenFile {
	private static final long serialVersionUID = -4438216387830519443L;
	
	private JTextField txtColAccID;
	JProgressBar progressBar;
	JScrollPaneData scrollPaneData;
	JButton btnSave;
	private JScrollPaneData scrollPaneDataResult;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlBatchAnnoGene ctrlBatchAnno;
	private JButton btnRun;
	private JCheckBox chckbxBlastto;
	JComboBoxData<Species> cmbBlastSpecies;
	JComboBoxData<Species> cmbSpecies;
	ArrayList<String[]> lsGeneInfo;
	/**
	 * Create the panel.
	 */
	public GuiAnnoGene() {
		setLayout(null);
		
		scrollPaneData = new JScrollPaneData();
		scrollPaneData.setBounds(12, 30, 693, 207);
		add(scrollPaneData);
		
		JButton btnOpenfile = new JButton("OpenFile");
		btnOpenfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String excelFile = guiFileOpen.openFileName("excel/txt", "");
				lsGeneInfo = ExcelTxtRead.readLsExcelTxt(excelFile, 1);
				scrollPaneData.setItemLs(lsGeneInfo);
			}
		});
		btnOpenfile.setBounds(717, 30, 118, 24);
		add(btnOpenfile);
		
		txtColAccID = new JTextField();
		txtColAccID.setBounds(718, 187, 114, 18);
		add(txtColAccID);
		txtColAccID.setColumns(10);
		
		JLabel lblAccidcolumn = new JLabel("AccIDColumn");
		lblAccidcolumn.setBounds(718, 161, 118, 14);
		add(lblAccidcolumn);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileName("txt", "");
				TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
				txtOut.ExcelWrite(ctrlBatchAnno.getResult());
			}
		});
		btnSave.setBounds(717, 561, 118, 24);
		add(btnSave);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(12, 571, 693, 14);
		add(progressBar);
		
		scrollPaneDataResult = new JScrollPaneData();
		scrollPaneDataResult.setBounds(12, 262, 693, 290);
		add(scrollPaneDataResult);
		
		btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnRun.setEnabled(false);
				ctrlBatchAnno.setListQuery(lsGeneInfo);
				
				scrollPaneDataResult.setTitle(ctrlBatchAnno.getTitle());
				try {
					ctrlBatchAnno.setColumnAccIDFrom1(Integer.parseInt(txtColAccID.getText()));
				} catch (Exception e2) { }
				try {
					ctrlBatchAnno.setBlastTo(chckbxBlastto.isSelected(), cmbBlastSpecies.getSelectedValue().getTaxID());
				} catch (Exception e2) { }
				ctrlBatchAnno.setSpecies(cmbSpecies.getSelectedValue().getTaxID());
				
				ctrlBatchAnno.execute();
				btnSave.setEnabled(false);
			}
		});
		btnRun.setBounds(717, 525, 118, 24);
		add(btnRun);
		
		chckbxBlastto = new JCheckBox("BlastTo");
		chckbxBlastto.setBounds(713, 223, 131, 22);
		add(chckbxBlastto);
		
		cmbBlastSpecies = new JComboBoxData<Species>();
		cmbBlastSpecies.setBounds(717, 247, 118, 23);
		add(cmbBlastSpecies);
		
		cmbSpecies = new JComboBoxData<Species>();
		cmbSpecies.setBounds(717, 66, 118, 23);
		add(cmbSpecies);
		initial();
	}
	
	private void initial() {
		selectRadAnno();
		cmbBlastSpecies.setMapItem(Species.getSpeciesName2Species(Species.KEGGNAME_SPECIES));
		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.KEGGNAME_SPECIES));
		btnSave.setEnabled(false);
	}
	
	private void selectRadAnno() {
		ctrlBatchAnno = new CtrlBatchAnnoGene(this);
		txtColAccID.setEditable(true);
		chckbxBlastto.setEnabled(true);
		cmbBlastSpecies.setEnabled(true);
	}
	
	public void setGuiFileOpen(GUIFileOpen guiFileOpen) {
		this.guiFileOpen = guiFileOpen;
	}
	
	public JProgressBar getProcessBar() {
		return progressBar;
	}
	public JScrollPaneData getJScrollPaneDataResult() {
		return scrollPaneDataResult;
	}
	public JButton getBtnSave() {
		return btnSave;
	}
	public JButton getBtnRun() {
		return btnRun;
	}
}
