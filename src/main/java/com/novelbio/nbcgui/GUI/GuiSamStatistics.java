package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
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
	JProgressBar progressBar;
	JScrollPaneData scrollPaneData;
	JCheckBox chkRpkmcount;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	CtrlSamRPKMLocate ctrlSamRPKMLocate;
	private JButton btnRun;
	
	ArrayList<String[]> lsGeneInfo;
	private JTextField txtTssUp;
	private JTextField txtTssDown;
	private JTextField txtTesUp;
	private JTextField txtTesDown;
	JButton btnOpenGtf;
	
	String readFile = "";
	
	GuiLayeredPaneSpeciesVersionGff layeredPane;
	
	JButton btnSave;
	private JTextField txtSaveTo;
	private JLabel lblSaveto;
	private JTextField txtGTF;
	JLabel lblInfo;
	/**
	 * Create the panel.
	 */
	public GuiSamStatistics() {
		setLayout(null);
		
		scrollPaneData = new JScrollPaneData();
		scrollPaneData.setBounds(12, 30, 693, 358);
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
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileName("", "");
				if (FileOperate.isFileDirectory(fileName)) {
					fileName = FileOperate.addSep(fileName);
				}
				txtSaveTo.setText(fileName);
			}
		});
		btnSave.setBounds(715, 467, 118, 24);
		add(btnSave);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(12, 553, 957, 14);
		add(progressBar);
		
		btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				progressBar.setMinimum(0);
				progressBar.setValue(0);
				
				btnRun.setEnabled(false);
				ctrlSamRPKMLocate.setQueryFile(scrollPaneData.getLsDataInfo());
				ctrlSamRPKMLocate.setIsCountRPKM(chkRpkmcount.isSelected());
 				int[] tss = new int[]{0,0};
				int[] tes = new int[]{0,0};
				try { tss[0] = Integer.parseInt(txtTssUp.getText()); } catch (Exception e2) { }
				try { tss[1] = Integer.parseInt(txtTssDown.getText()); } catch (Exception e2) { }
				try { tes[0] = Integer.parseInt(txtTesUp.getText()); } catch (Exception e2) { }
				try { tes[1] = Integer.parseInt(txtTesDown.getText()); } catch (Exception e2) { }
				
				ctrlSamRPKMLocate.setTssRange(tss);
				ctrlSamRPKMLocate.setTesRange(tes);
				ctrlSamRPKMLocate.setResultPrefix(txtSaveTo.getText());
				
				Species species = layeredPane.getSelectSpecies();
				if (species.getTaxID() == 0 && !txtGTF.getText().equals("")) {
					String gtfFile = txtGTF.getText();
					GffHashGene gffHashGene = new GffHashGene(gtfFile);
					ctrlSamRPKMLocate.setGffHash(gffHashGene);
				} else {
					ctrlSamRPKMLocate.setSpecies(species);
				}
				
				Thread thread = new Thread(ctrlSamRPKMLocate);
				thread.start();
				btnSave.setEnabled(false);
			}
		});
		btnRun.setBounds(849, 467, 118, 24);
		add(btnRun);
		
		txtTssUp = new JTextField();
		txtTssUp.setText("-1500");
		txtTssUp.setBounds(729, 261, 52, 18);
		add(txtTssUp);
		txtTssUp.setColumns(10);
		
		txtTssDown = new JTextField();
		txtTssDown.setText("1500");
		txtTssDown.setBounds(793, 261, 52, 18);
		add(txtTssDown);
		txtTssDown.setColumns(10);
		
		JLabel lblTss = new JLabel("Tss");
		lblTss.setBounds(729, 226, 69, 14);
		add(lblTss);
		
		JLabel lblUp = new JLabel("Up");
		lblUp.setBounds(727, 240, 52, 14);
		add(lblUp);
		
		JLabel lblDown = new JLabel("Down");
		lblDown.setBounds(793, 240, 52, 14);
		add(lblDown);
		
		txtTesUp = new JTextField();
		txtTesUp.setText("-200");
		txtTesUp.setBounds(729, 332, 52, 18);
		add(txtTesUp);
		txtTesUp.setColumns(10);
		
		JLabel lblUp_1 = new JLabel("Up");
		lblUp_1.setBounds(729, 306, 69, 14);
		add(lblUp_1);
		
		txtTesDown = new JTextField();
		txtTesDown.setText("200");
		txtTesDown.setBounds(791, 332, 52, 18);
		add(txtTesDown);
		txtTesDown.setColumns(10);
		
		JLabel lblDown_1 = new JLabel("Down");
		lblDown_1.setBounds(791, 306, 69, 14);
		add(lblDown_1);
		
		JLabel lblTes = new JLabel("Tes");
		lblTes.setBounds(729, 291, 69, 14);
		add(lblTes);
		
		layeredPane = new GuiLayeredPaneSpeciesVersionGff();
		layeredPane.setBounds(717, 66, 221, 154);
		layeredPane.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (layeredPane.getSelectSpecies().getTaxID() == 0) {
					btnOpenGtf.setEnabled(false);
					txtGTF.setEnabled(false);
				} else {
					btnOpenGtf.setEnabled(true);
					txtGTF.setEnabled(true);
				}
			}
		});
		add(layeredPane);
		
		chkRpkmcount = new JCheckBox("RPKMcount");
		chkRpkmcount.setBounds(722, 364, 131, 22);
		add(chkRpkmcount);
		
		txtSaveTo = new JTextField();
		txtSaveTo.setBounds(10, 473, 693, 18);
		add(txtSaveTo);
		txtSaveTo.setColumns(10);
		
		lblSaveto = new JLabel("SaveTo");
		lblSaveto.setBounds(10, 448, 69, 14);
		add(lblSaveto);
		
		txtGTF = new JTextField();
		txtGTF.setBounds(10, 407, 693, 18);
		add(txtGTF);
		txtGTF.setColumns(10);
		
		btnOpenGtf = new JButton("OpenGTF");
		btnOpenGtf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnOpenGtf.setBounds(715, 404, 118, 24);
		add(btnOpenGtf);
		
		JLabel lblInformation = new JLabel("Information");
		lblInformation.setBounds(10, 506, 102, 14);
		add(lblInformation);
		
		lblInfo = new JLabel("");
		lblInfo.setBounds(12, 527, 628, 14);
		add(lblInfo);
		
		initial();
	}
	
	private void initial() {
		ctrlSamRPKMLocate = new CtrlSamRPKMLocate(this);
		scrollPaneData.setTitle(new String[]{"FileName", "Prefix"});
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
	
	public JLabel getLabel() {
		return lblInfo;
	}
}
