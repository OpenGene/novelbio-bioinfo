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
import com.novelbio.nbcgui.controlquery.CtrlBatchAnnoPeak;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JProgressBar;
/**
 * 批量注释，各种注释
 * @author zong0jie
 *
 */
public class GuiAnnoPeak extends JPanel {
	private static final long serialVersionUID = -4438216387830519443L;
	private JTextField txtColChrID;
	private JTextField txtColPeakStartMid;
	private JTextField txtColPeakEnd;
	JCheckBox chckPeakRange;
	JLabel lblPeakendcolumn;
	JLabel lblPeakstartcolumn;
	JProgressBar progressBar;
	JScrollPaneData scrollPaneData;
	JButton btnSave;
	private JScrollPaneData scrollPaneDataResult;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlBatchAnnoPeak ctrlBatchAnno;
	private JButton btnRun;
	
	JComboBoxData<Species> cmbSpecies;
	JComboBoxData<String> cmbSpeciesVersion;
	
	ArrayList<String[]> lsGeneInfo;
	private JTextField txtTssUp;
	private JTextField txtTssDown;
	private JTextField txtTesUp;
	private JTextField txtTesDown;
	private JCheckBox chckbxGenebody;
	/**
	 * Create the panel.
	 */
	public GuiAnnoPeak() {
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
		
		chckPeakRange = new JCheckBox("PeakRange");
		chckPeakRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectChckPeakRangeAnno(chckPeakRange.isSelected());
			}
		});
		chckPeakRange.setBounds(717, 147, 131, 22);
		add(chckPeakRange);
		
		txtColChrID = new JTextField();
		txtColChrID.setBounds(717, 204, 114, 18);
		add(txtColChrID);
		txtColChrID.setColumns(10);
		
		JLabel lblChridcolumn = new JLabel("ChrIDColumn");
		lblChridcolumn.setBounds(717, 178, 114, 14);
		add(lblChridcolumn);
		
		txtColPeakStartMid = new JTextField();
		txtColPeakStartMid.setBounds(717, 253, 114, 18);
		add(txtColPeakStartMid);
		txtColPeakStartMid.setColumns(10);
		
		lblPeakstartcolumn = new JLabel("PeakStartColumn");
		lblPeakstartcolumn.setBounds(717, 236, 157, 14);
		add(lblPeakstartcolumn);
		
		txtColPeakEnd = new JTextField();
		txtColPeakEnd.setBounds(721, 299, 114, 18);
		add(txtColPeakEnd);
		txtColPeakEnd.setColumns(10);
		
		lblPeakendcolumn = new JLabel("PeakEndColumn");
		lblPeakendcolumn.setBounds(717, 283, 131, 14);
		add(lblPeakendcolumn);
		
		cmbSpeciesVersion = new JComboBoxData<String>();
		
		cmbSpeciesVersion.setBounds(717, 101, 118, 23);
		add(cmbSpeciesVersion);
		
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
					ctrlBatchAnno.setColPeakStartEnd(Integer.parseInt(txtColPeakStartMid.getText()), Integer.parseInt(txtColPeakEnd.getText()));
				} catch (Exception e2) { }
				try {
					ctrlBatchAnno.setColPeakSummit(Integer.parseInt(txtColPeakStartMid.getText()));
				} catch (Exception e2) {
				}
				int[] tss = new int[]{0,0};
				int[] tes = new int[]{0,0};
				try { tss[0] = Integer.parseInt(txtTssUp.getText()); } catch (Exception e2) { }
				try { tss[1] = Integer.parseInt(txtTssDown.getText()); } catch (Exception e2) { }
				try { tes[0] = Integer.parseInt(txtTesUp.getText()); } catch (Exception e2) { }
				try { tes[1] = Integer.parseInt(txtTesDown.getText()); } catch (Exception e2) { }
				
				ctrlBatchAnno.setTssRange(tss);
				ctrlBatchAnno.setTesRange(tes);
				ctrlBatchAnno.setFilterGeneBody(chckbxGenebody.isSelected());
				
				ctrlBatchAnno.setIsSummitSearch(!chckPeakRange.isSelected());
				Species species = cmbSpecies.getSelectedValue();
				species.setVersion(cmbSpeciesVersion.getSelectedValue());
				
				ctrlBatchAnno.setSpecies(species);
				ctrlBatchAnno.execute();
				btnSave.setEnabled(false);
			}
		});
		btnRun.setBounds(717, 525, 118, 24);
		add(btnRun);
		
		cmbSpecies = new JComboBoxData<Species>();
		cmbSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Species species = cmbSpecies.getSelectedValue();
				cmbSpeciesVersion.setMapItem(species.getMapVersion());
			}
		});
		cmbSpecies.setBounds(717, 66, 118, 23);
		add(cmbSpecies);
		
		txtTssUp = new JTextField();
		txtTssUp.setBounds(720, 355, 52, 18);
		add(txtTssUp);
		txtTssUp.setColumns(10);
		
		txtTssDown = new JTextField();
		txtTssDown.setBounds(784, 355, 52, 18);
		add(txtTssDown);
		txtTssDown.setColumns(10);
		
		JLabel lblTss = new JLabel("Tss");
		lblTss.setBounds(717, 329, 69, 14);
		add(lblTss);
		
		JLabel lblUp = new JLabel("Up");
		lblUp.setBounds(717, 338, 69, 14);
		add(lblUp);
		
		JLabel lblDown = new JLabel("Down");
		lblDown.setBounds(779, 338, 69, 14);
		add(lblDown);
		
		txtTesUp = new JTextField();
		txtTesUp.setBounds(721, 411, 52, 18);
		add(txtTesUp);
		txtTesUp.setColumns(10);
		
		JLabel lblUp_1 = new JLabel("Up");
		lblUp_1.setBounds(717, 396, 69, 14);
		add(lblUp_1);
		
		txtTesDown = new JTextField();
		txtTesDown.setBounds(784, 411, 52, 18);
		add(txtTesDown);
		txtTesDown.setColumns(10);
		
		JLabel lblDown_1 = new JLabel("Down");
		lblDown_1.setBounds(784, 396, 69, 14);
		add(lblDown_1);
		
		JLabel lblTes = new JLabel("Tes");
		lblTes.setBounds(717, 385, 69, 14);
		add(lblTes);
		
		chckbxGenebody = new JCheckBox("GeneBody");
		chckbxGenebody.setBounds(717, 447, 131, 22);
		add(chckbxGenebody);
		
		initial();

	}
	
	private void initial() {
		ctrlBatchAnno = new CtrlBatchAnnoPeak(this);
		chckPeakRange.setSelected(true);
		selectChckPeakRangeAnno(chckPeakRange.isSelected());
		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		Species species = cmbSpecies.getSelectedValue();
		cmbSpeciesVersion.setMapItem(species.getMapVersion());
		btnSave.setEnabled(false);
	}
	private void selectChckPeakRangeAnno(boolean isSelected) {
		if (isSelected) {
			lblPeakstartcolumn.setText("PeakStartColumn");
			txtColPeakEnd.setEditable(true);
		}
		else {
			txtColPeakEnd.setEditable(false);
			lblPeakstartcolumn.setText("PeakMiddleColumn");
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
