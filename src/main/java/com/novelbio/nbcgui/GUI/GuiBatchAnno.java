package com.novelbio.nbcgui.GUI;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.base.RunGetInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controlquery.CtrlBatchAnno;
import com.novelbio.nbcgui.controlquery.CtrlBatchAnnoGene;
import com.novelbio.nbcgui.controlquery.CtrlBatchAnnoPeak;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JProgressBar;
import javax.swing.JComboBox;
/**
 * 批量注释，各种注释
 * @author zong0jie
 *
 */
public class GuiBatchAnno extends JPanel {
	private static final long serialVersionUID = -4438216387830519443L;
	
	private JTextField txtColAccID;
	private JTextField txtColChrID;
	private JTextField txtColPeakStartMid;
	private JTextField txtColPeakEnd;
	private ButtonGroup btnAnnoTypeGroup;
	JRadioButton rdbtnIdannotation;
	JRadioButton rdbtnPeakannotation;
	JCheckBox chckPeakRange;
	JLabel lblPeakendcolumn;
	JLabel lblPeakstartcolumn;
	JProgressBar progressBar;
	JScrollPaneData scrollPaneData;
	JButton btnSave;
	private JScrollPaneData scrollPaneDataResult;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlBatchAnno ctrlBatchAnno;
	private JButton btnRun;
	private JCheckBox chckbxBlastto;
	JComboBoxData<Species> cmbBlastSpecies;
	JComboBoxData<Species> cmbSpecies;
	ArrayList<String[]> lsGeneInfo;
	/**
	 * Create the panel.
	 */
	public GuiBatchAnno() {
		setLayout(null);
		
		scrollPaneData = new JScrollPaneData();
		scrollPaneData.setBounds(12, 30, 693, 207);
		add(scrollPaneData);
		
		JButton btnOpenfile = new JButton("OpenFile");
		btnOpenfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String excelFile = guiFileOpen.openFileName("excel/txt", "");
				lsGeneInfo = ExcelTxtRead.readLsExcelTxt(excelFile, 1);
				scrollPaneData.setProview(lsGeneInfo);
			}
		});
		btnOpenfile.setBounds(717, 30, 118, 24);
		add(btnOpenfile);
		
		rdbtnIdannotation = new JRadioButton("IdAnnotation");
		rdbtnIdannotation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectRadAnno();
			}
		});
		rdbtnIdannotation.setBounds(717, 131, 151, 22);
		add(rdbtnIdannotation);
		
		rdbtnPeakannotation = new JRadioButton("PeakAnnotation");
		rdbtnPeakannotation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectRadPeakAnno();
			}
		});
		rdbtnPeakannotation.setBounds(713, 301, 151, 22);
		add(rdbtnPeakannotation);
		
		chckPeakRange = new JCheckBox("PeakRange");
		chckPeakRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectChckPeakRangeAnno(chckPeakRange.isSelected());
			}
		});
		chckPeakRange.setBounds(737, 320, 131, 22);
		add(chckPeakRange);
		
		txtColAccID = new JTextField();
		txtColAccID.setBounds(718, 187, 114, 18);
		add(txtColAccID);
		txtColAccID.setColumns(10);
		
		JLabel lblAccidcolumn = new JLabel("AccIDColumn");
		lblAccidcolumn.setBounds(718, 161, 118, 14);
		add(lblAccidcolumn);
		
		txtColChrID = new JTextField();
		txtColChrID.setBounds(717, 366, 114, 18);
		add(txtColChrID);
		txtColChrID.setColumns(10);
		
		JLabel lblChridcolumn = new JLabel("ChrIDColumn");
		lblChridcolumn.setBounds(717, 350, 114, 14);
		add(lblChridcolumn);
		
		txtColPeakStartMid = new JTextField();
		txtColPeakStartMid.setBounds(717, 415, 114, 18);
		add(txtColPeakStartMid);
		txtColPeakStartMid.setColumns(10);
		
		lblPeakstartcolumn = new JLabel("PeakStartColumn");
		lblPeakstartcolumn.setBounds(717, 396, 191, 14);
		add(lblPeakstartcolumn);
		
		txtColPeakEnd = new JTextField();
		txtColPeakEnd.setBounds(717, 464, 114, 18);
		add(txtColPeakEnd);
		txtColPeakEnd.setColumns(10);
		
		lblPeakendcolumn = new JLabel("PeakEndColumn");
		lblPeakendcolumn.setBounds(717, 445, 131, 14);
		add(lblPeakendcolumn);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileName("txt", "");
				TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
				txtOut.ExcelWrite(ctrlBatchAnno.getResult(), "\t", 1, 1);
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
				try {
					ctrlBatchAnno.setColPeakStartEnd(Integer.parseInt(txtColPeakStartMid.getText()), Integer.parseInt(txtColPeakEnd.getText()));
				} catch (Exception e2) { }
				try {
					ctrlBatchAnno.setColPeakSummit(Integer.parseInt(txtColPeakStartMid.getText()));
				} catch (Exception e2) {
				}
				
				ctrlBatchAnno.setIsSummitSearch(!chckPeakRange.isSelected());
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
		btnAnnoTypeGroup = new ButtonGroup();
		btnAnnoTypeGroup.add(rdbtnIdannotation);
		btnAnnoTypeGroup.add(rdbtnPeakannotation);
		rdbtnIdannotation.setSelected(true);
		rdbtnPeakannotation.setSelected(false);
		chckPeakRange.setSelected(true);
		selectRadAnno();
		selectChckPeakRangeAnno(chckPeakRange.isSelected());
		cmbBlastSpecies.setMapItem(Species.getSpeciesName2Species(Species.KEGGNAME_SPECIES));
		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.KEGGNAME_SPECIES));
		btnSave.setEnabled(false);
	}
	
	private void selectRadAnno() {
		ctrlBatchAnno = new CtrlBatchAnnoGene(this);
		txtColAccID.setEditable(true);
		chckbxBlastto.setEnabled(true);
		cmbBlastSpecies.setEnabled(true);
		txtColChrID.setEditable(false);
		txtColPeakEnd.setEditable(false);
		txtColPeakStartMid.setEnabled(false);
		chckPeakRange.setEnabled(false);
	}
	private void selectRadPeakAnno() {
		ctrlBatchAnno = new CtrlBatchAnnoPeak(this);
		chckbxBlastto.setEnabled(false);
		cmbBlastSpecies.setEnabled(false);
		txtColAccID.setEditable(false);
		txtColChrID.setEditable(true);
		txtColPeakStartMid.setEnabled(true);
		chckPeakRange.setEnabled(true);
		selectChckPeakRangeAnno(chckPeakRange.isSelected());
	}
	private void selectChckPeakRangeAnno(boolean isSelected) {
		if (isSelected) {
			lblPeakstartcolumn.setText("PeakStartColumn");
		}
		else {
			txtColPeakEnd.setEditable(false);
			lblPeakstartcolumn.setText("PeakMiddleColumn");
		}
		if (isSelected && rdbtnPeakannotation.isSelected()) {
			txtColPeakEnd.setEditable(true);
		}
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
