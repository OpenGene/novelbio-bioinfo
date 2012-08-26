package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JProgressBar;

import com.novelbio.analysis.seq.resequencing.SnpGroupFilterInfo;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.nbcgui.controlseq.CtrlSnpCalling;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JScrollPane;
/** snpCalling的界面 */
public class GuiSnpCalling extends JPanel implements GuiNeedOpenFile {
	private JTextField txtHetoSnpProp;
	private JTextField txtHetoMoreSnpProp;
	JComboBoxData<Integer> combSnpLevel;
	JButton btnAddFile;
	JButton btnDelete;
	JButton btnRun;
	JScrollPaneData sclInputFile;
	JProgressBar progressBar;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private JLabel lblInfo;
	
	CtrlSnpCalling ctrlSnpCalling = new CtrlSnpCalling(this);
	private JScrollPane scrollPane;
	private JButton btnAddsnpfile;
	private JButton btnDelete_1;
	
	/**
	 * Create the panel.
	 */
	public GuiSnpCalling() {
		setLayout(null);
		
		sclInputFile = new JScrollPaneData();
		sclInputFile.setBounds(14, 10, 757, 137);
		add(sclInputFile);
		
		btnAddFile = new JButton("AddFile");
		btnAddFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.openFileName("PileUpFile", "");
				String outSnpFileName = FileOperate.changeFileSuffix(fileName, "_SnpInfo", "txt");
				sclInputFile.addItem(new String[]{fileName, outSnpFileName});
			}
		});
		btnAddFile.setBounds(782, 14, 118, 24);
		add(btnAddFile);
		
		btnDelete = new JButton("Delete");
		btnDelete.setBounds(783, 54, 118, 24);
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclInputFile.deleteSelRows();
			}
		});
		add(btnDelete);
		
		combSnpLevel = new JComboBoxData<Integer>();
		combSnpLevel.setBounds(14, 185, 190, 23);
		add(combSnpLevel);
		
		JLabel lblSnpfilterquality = new JLabel("SnpFilterQuality");
		lblSnpfilterquality.setBounds(14, 159, 152, 14);
		add(lblSnpfilterquality);
		
		JLabel lblHetolessinfo = new JLabel("Heto Contain Snp Reads Prop Min");
		lblHetolessinfo.setBounds(296, 159, 279, 14);
		add(lblHetolessinfo);
		
		JLabel lblHetomorecontainreadsmin = new JLabel("Heto More Contain Snp Reads Prop Min");
		lblHetomorecontainreadsmin.setBounds(296, 189, 324, 14);
		add(lblHetomorecontainreadsmin);
		
		txtHetoSnpProp = new JTextField();
		txtHetoSnpProp.setBounds(657, 157, 114, 18);
		add(txtHetoSnpProp);
		txtHetoSnpProp.setColumns(10);
		
		txtHetoMoreSnpProp = new JTextField();
		txtHetoMoreSnpProp.setBounds(657, 187, 114, 18);
		add(txtHetoMoreSnpProp);
		txtHetoMoreSnpProp.setColumns(10);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(14, 511, 767, 14);
		add(progressBar);
		
		btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrlSnpCalling.set(combSnpLevel.getSelectedValue());
				ArrayList<String[]> lsFile = sclInputFile.getLsDataInfo();
				for (String[] strings : lsFile) {
					ctrlSnpCalling.addSnpFromPileUpFile(strings[0], strings[1]);
				}
				try {
					double hetoSnpProp = Double.parseDouble(txtHetoSnpProp.getText());
					ctrlSnpCalling.setSnp_Hete_Contain_SnpProp_Min(hetoSnpProp);
				} catch (Exception e2) { }
				
				try {
					double hetoMoreSnpProp = Double.parseDouble(txtHetoMoreSnpProp.getText());
					ctrlSnpCalling.setSnp_HetoMore_Contain_SnpProp_Min(hetoMoreSnpProp);
				} catch (Exception e2) { }
				
				ctrlSnpCalling.runSnpCalling();
			}
		});
		btnRun.setBounds(793, 501, 118, 24);
		add(btnRun);
		
		lblInfo = new JLabel("");
		lblInfo.setBounds(12, 475, 383, 24);
		add(lblInfo);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(18, 273, 754, 115);
		add(scrollPane);
		
		btnAddsnpfile = new JButton("AddSnpFile");
		btnAddsnpfile.setBounds(798, 273, 118, 24);
		add(btnAddsnpfile);
		
		btnDelete_1 = new JButton("Delete");
		btnDelete_1.setBounds(798, 364, 118, 24);
		add(btnDelete_1);

		initial();
	}
	
	private void initial() {
		combSnpLevel.setMapItem(SnpGroupFilterInfo.getMap_Str2SnpLevel());
		sclInputFile.setTitle(new String[]{"Input PileUp File","Output Snp File"});
	}
	
	public void setGuiFileOpen(GUIFileOpen guiFileOpen) {
		this.guiFileOpen = guiFileOpen;
	}
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	/** 读取信息 */
	public JLabel getLblInfo() {
		return lblInfo;
	}
	public JButton getBtnAddFile() {
		return btnAddFile;
	}
	public JButton getBtnDelete() {
		return btnDelete;
	}
	public JButton getBtnRun() {
		return btnRun;
	}
}
