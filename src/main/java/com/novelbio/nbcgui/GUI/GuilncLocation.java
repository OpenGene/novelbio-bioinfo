package com.novelbio.nbcgui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import com.novelbio.analysis.seq.rnaseq.lnc.LncInfo;
import com.novelbio.analysis.seq.rnaseq.lnc.LncSiteInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JScrollPaneData;

public class GuilncLocation extends JPanel {
	JScrollPaneData scrollPane;
	GuiLayeredPaneSpeciesVersionGff guiLayeredPaneSpeciesVersionGff;
	JSpinner spinner;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	LncSiteInfo lncSiteInfo = new LncSiteInfo();
	
	/**
	 * Create the panel.
	 */
	public GuilncLocation() {
		setLayout(null);
		
		scrollPane = new JScrollPaneData();
		scrollPane.setBounds(12, 51, 572, 313);
		add(scrollPane);
		
		JButton btnAddfile = new JButton("addFile");
		btnAddfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> lsFile = guiFileOpen.openLsFileName("", "");
				List<String[]> lsFile2Result = new ArrayList<String[]>();
				for (String fileName : lsFile) {
					String[] tmpResult = new String[2];
					tmpResult[0] = fileName;
					tmpResult[1] = FileOperate.changeFileSuffix(fileName, "_lncLocation", null);
					lsFile2Result.add(tmpResult);
				}
				scrollPane.addItemLs(lsFile2Result);
			}
		});
		btnAddfile.setBounds(596, 51, 107, 25);
		add(btnAddfile);
		
		JButton btnDelfile = new JButton("delFile");
		btnDelfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPane.deleteSelRows();
			}
		});
		btnDelfile.setBounds(596, 88, 107, 25);
		add(btnDelfile);
		
		guiLayeredPaneSpeciesVersionGff = new GuiLayeredPaneSpeciesVersionGff();
		guiLayeredPaneSpeciesVersionGff.setBounds(596, 125, 214, 137);
		add(guiLayeredPaneSpeciesVersionGff);
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lncSiteInfo.setSpecies(guiLayeredPaneSpeciesVersionGff.getSelectSpecies());
				int colNum = (Integer)spinner.getValue() - 1;
				for (String[] file2Result : scrollPane.getLsDataInfo()) {
					List<List<String>> lsInfo = ExcelTxtRead.readLsExcelTxtls(file2Result[0], 1);
					lncSiteInfo.setLslsExcel(lsInfo, colNum);
					List<LncInfo> lsTmpResult = lncSiteInfo.findLncinfo();
					
					TxtReadandWrite txtWrite = new TxtReadandWrite(file2Result[1], true);
					txtWrite.writefileln(LncInfo.getTitle());
					for (LncInfo lncInfo : lsTmpResult) {
						txtWrite.writefileln(lncInfo.toString());
					}
					txtWrite.close();
				}
			}
		});
		btnRun.setBounds(596, 338, 107, 25);
		add(btnRun);
		
		spinner = new JSpinner();
		spinner.setBounds(596, 311, 92, 20);
		spinner.setValue(1);
		add(spinner);
		
		JLabel lblLncidcol = new JLabel("LncIDCol");
		lblLncidcol.setBounds(596, 284, 116, 15);
		add(lblLncidcol);
		
		initial();
	}
	
	private void initial() {
		scrollPane.setTitle(new String[]{"FileName", "ResultFileName"});
	}
}
