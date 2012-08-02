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
import com.novelbio.nbcgui.controlquery.CtrlPeakStatistics;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JProgressBar;

/**
 * 批量注释，各种注释
 * @author zong0jie
 *
 */
public class GuiPeakStatistics extends JPanel {
	private static final long serialVersionUID = -4438216387830519443L;
	private JTextField txtColChrID;
	private JTextField txtColPeakStartMid;
	JCheckBox chckSetColSummit;
	JLabel lblPeakstartcolumn;
	JProgressBar progressBar;
	JScrollPaneData scrollPaneData;
	JButton btnSave;
	private JScrollPaneData scrollPaneDataResult;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlPeakStatistics ctrlPeakStatistics;
	private JButton btnRun;
	
	JComboBoxData<Species> cmbSpecies;
	JComboBoxData<String> cmbSpeciesVersion;
	
	ArrayList<String[]> lsGeneInfo;
	private JTextField txtTssUp;
	private JTextField txtTssDown;
	private JTextField txtTesUp;
	private JTextField txtTesDown;
	
	String readFile = "";
	
	/**
	 * Create the panel.
	 */
	public GuiPeakStatistics() {
		setLayout(null);
		
		scrollPaneData = new JScrollPaneData();
		scrollPaneData.setBounds(12, 30, 693, 207);
		add(scrollPaneData);
		
		JButton btnOpenfile = new JButton("OpenBedFile");
		btnOpenfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readFile = guiFileOpen.openFileName("excel/txt", "");
				lsGeneInfo = ExcelTxtRead.readLsExcelTxtFile(readFile, 1, 1, 50, -1);
				scrollPaneData.setItemLs(lsGeneInfo);
			}
		});
		btnOpenfile.setBounds(717, 30, 131, 24);
		add(btnOpenfile);
		
		chckSetColSummit = new JCheckBox("SetColSummit");
		chckSetColSummit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectChckPeakRangeAnno(chckSetColSummit.isSelected());
			}
		});
		chckSetColSummit.setBounds(717, 147, 131, 22);
		add(chckSetColSummit);
		
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
		
		lblPeakstartcolumn = new JLabel("PeakSummitColumn");
		lblPeakstartcolumn.setBounds(717, 236, 157, 14);
		add(lblPeakstartcolumn);
		
		cmbSpeciesVersion = new JComboBoxData<String>();
		
		cmbSpeciesVersion.setBounds(717, 101, 118, 23);
		add(cmbSpeciesVersion);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileName("txt", "");
				TxtReadandWrite txtOut = new TxtReadandWrite(fileName, true);
				txtOut.ExcelWrite(ctrlPeakStatistics.getResult(), "\t", 1, 1);
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
				progressBar.setMinimum(0);
				progressBar.setValue(0);
				
				btnRun.setEnabled(false);
				ctrlPeakStatistics.setQueryFile(readFile);

				try { ctrlPeakStatistics.setColPeakSummit(Integer.parseInt(txtColPeakStartMid.getText()));
				} catch (Exception e2) { }
				try { ctrlPeakStatistics.setColChrID(Integer.parseInt(txtColChrID.getText()));
				} catch (Exception e2) { }
				
 				int[] tss = new int[]{0,0};
				int[] tes = new int[]{0,0};
				try { tss[0] = Integer.parseInt(txtTssUp.getText()); } catch (Exception e2) { }
				try { tss[1] = Integer.parseInt(txtTssDown.getText()); } catch (Exception e2) { }
				try { tes[0] = Integer.parseInt(txtTesUp.getText()); } catch (Exception e2) { }
				try { tes[1] = Integer.parseInt(txtTesDown.getText()); } catch (Exception e2) { }
				
				ctrlPeakStatistics.setTssRange(tss);
				ctrlPeakStatistics.setTesRange(tes);
				
				Species species = cmbSpecies.getSelectedValue();
				species.setVersion(cmbSpeciesVersion.getSelectedValue());
				
				ctrlPeakStatistics.setSpecies(species);
				ctrlPeakStatistics.execute();
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
		
		initial();
	}
	
	private void initial() {
		ctrlPeakStatistics = new CtrlPeakStatistics(this);
		chckSetColSummit.setSelected(true);
		selectChckPeakRangeAnno(chckSetColSummit.isSelected());
		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		Species species = cmbSpecies.getSelectedValue();
		cmbSpeciesVersion.setMapItem(species.getMapVersion());
		btnSave.setEnabled(false);
	}
	private void selectChckPeakRangeAnno(boolean isSelected) {
		if (isSelected) {
			lblPeakstartcolumn.setText("PeakStartColumn");
			txtColChrID.setEnabled(true);
			txtColPeakStartMid.setEnabled(true);
		}
		else {
			txtColChrID.setText("");
			txtColChrID.setEnabled(false);
			txtColPeakStartMid.setText("");
			txtColPeakStartMid.setEnabled(false);
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
