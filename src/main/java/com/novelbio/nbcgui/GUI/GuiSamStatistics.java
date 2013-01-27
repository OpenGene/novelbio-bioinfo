package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controlquery.CtrlPeakStatistics;
import com.novelbio.nbcgui.controlquery.CtrlSamRPKMLocate;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.JLayeredPane;

/**
 * 批量注释，各种注释
 * @author zong0jie
 *
 */
public class GuiSamStatistics extends JPanel {
	private static final long serialVersionUID = -4438216387830519443L;
	private JTextField txtColChrID;
	private JTextField txtColPeakStartMid;
	JCheckBox chckSetColSummit;
	JLabel lblPeakstartcolumn;
	JProgressBar progressBar;
	JScrollPaneData scrollPaneData;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlSamRPKMLocate ctrlPeakStatistics;
	private JButton btnRun;
	
	ArrayList<String[]> lsGeneInfo;
	private JTextField txtTssUp;
	private JTextField txtTssDown;
	private JTextField txtTesUp;
	private JTextField txtTesDown;
	
	String readFile = "";
	
	GuiLayeredPaneSpeciesVersionGff layeredPane;
	/**
	 * Create the panel.
	 */
	public GuiSamStatistics() {
		setLayout(null);
		
		scrollPaneData = new JScrollPaneData();
		scrollPaneData.setBounds(12, 30, 693, 511);
		add(scrollPaneData);
		
		JButton btnOpenfile = new JButton("BamSamBedFile");
		btnOpenfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> lsFileName = guiFileOpen.openLsFileName("", "");
				ArrayList<String[]> lsFile = new ArrayList<String[]>();
				for (String string : lsFileName) {
					String[] tmp = new String[2];
					tmp[0] = string;
					tmp[1] = FileOperate.getFileNameSep(string)[0].split("_")[0];
					lsFile.add(tmp);
				}
				scrollPaneData.addItemLs(lsFile);
			}
		});
		btnOpenfile.setBounds(717, 30, 163, 24);
		add(btnOpenfile);
		
		chckSetColSummit = new JCheckBox("SetColSummit");
		chckSetColSummit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectChckPeakRangeAnno(chckSetColSummit.isSelected());
			}
		});
		chckSetColSummit.setBounds(717, 228, 131, 22);
		add(chckSetColSummit);
		
		txtColChrID = new JTextField();
		txtColChrID.setBounds(717, 276, 114, 18);
		add(txtColChrID);
		txtColChrID.setColumns(10);
		
		JLabel lblChridcolumn = new JLabel("ChrIDColumn");
		lblChridcolumn.setBounds(717, 258, 114, 14);
		add(lblChridcolumn);
		
		txtColPeakStartMid = new JTextField();
		txtColPeakStartMid.setBounds(717, 323, 114, 18);
		add(txtColPeakStartMid);
		txtColPeakStartMid.setColumns(10);
		
		lblPeakstartcolumn = new JLabel("PeakSummitColumn");
		lblPeakstartcolumn.setBounds(717, 306, 157, 14);
		add(lblPeakstartcolumn);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(12, 571, 693, 14);
		add(progressBar);
		
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
				
				Species species = layeredPane.getSelectSpecies();
				
				ctrlPeakStatistics.setSpecies(species);
				ctrlPeakStatistics.execute();
				btnSave.setEnabled(false);
			}
		});
		btnRun.setBounds(851, 561, 118, 24);
		add(btnRun);
		
		txtTssUp = new JTextField();
		txtTssUp.setBounds(717, 388, 52, 18);
		add(txtTssUp);
		txtTssUp.setColumns(10);
		
		txtTssDown = new JTextField();
		txtTssDown.setBounds(781, 388, 52, 18);
		add(txtTssDown);
		txtTssDown.setColumns(10);
		
		JLabel lblTss = new JLabel("Tss");
		lblTss.setBounds(717, 353, 69, 14);
		add(lblTss);
		
		JLabel lblUp = new JLabel("Up");
		lblUp.setBounds(715, 367, 52, 14);
		add(lblUp);
		
		JLabel lblDown = new JLabel("Down");
		lblDown.setBounds(781, 367, 52, 14);
		add(lblDown);
		
		txtTesUp = new JTextField();
		txtTesUp.setBounds(717, 459, 52, 18);
		add(txtTesUp);
		txtTesUp.setColumns(10);
		
		JLabel lblUp_1 = new JLabel("Up");
		lblUp_1.setBounds(717, 433, 69, 14);
		add(lblUp_1);
		
		txtTesDown = new JTextField();
		txtTesDown.setBounds(779, 459, 52, 18);
		add(txtTesDown);
		txtTesDown.setColumns(10);
		
		JLabel lblDown_1 = new JLabel("Down");
		lblDown_1.setBounds(779, 433, 69, 14);
		add(lblDown_1);
		
		JLabel lblTes = new JLabel("Tes");
		lblTes.setBounds(717, 418, 69, 14);
		add(lblTes);
		
		layeredPane = new GuiLayeredPaneSpeciesVersionGff();
		layeredPane.setBounds(717, 66, 221, 154);
		add(layeredPane);
		
		JCheckBox chkRpkmcount = new JCheckBox("RPKMcount");
		chkRpkmcount.setBounds(713, 499, 131, 22);
		add(chkRpkmcount);
		
		initial();
	}
	
	private void initial() {
		ctrlPeakStatistics = new CtrlPeakStatistics(this);
		chckSetColSummit.setSelected(true);
		selectChckPeakRangeAnno(chckSetColSummit.isSelected());
		scrollPaneData.setTitle(new String[]{"FileName", "Prefix"});
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
	
	public JButton getBtnSave() {
		return btnSave;
	}
	public JButton getBtnRun() {
		return btnRun;
	}
}
