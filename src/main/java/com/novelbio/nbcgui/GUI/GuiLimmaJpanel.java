package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JScrollPane;
import javax.swing.JButton;

import com.novelbio.analysis.microarray.LimmaAffy;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JScrollPaneData;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.ScrollPaneConstants;

public class GuiLimmaJpanel extends JPanel {
	JScrollPaneData scrollPaneCel;
	JScrollPaneData scrollPaneNormData;
	JScrollPaneData scrollPaneSample;
	JScrollPaneData scrollPaneDesign;
	LimmaAffy limmaAffy = new LimmaAffy();
	/**
	 * Create the panel.
	 */
	public GuiLimmaJpanel() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		scrollPaneCel = new JScrollPaneData();
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneCel, 22, SpringLayout.WEST, this);
		scrollPaneCel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneCel.setTitle(new String[]{"Cel File Name","abbr"});
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneCel, 36, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneCel, -437, SpringLayout.SOUTH, this);
		add(scrollPaneCel);
		
		JButton btnOpenCel = new JButton("OpenCel");
		springLayout.putConstraint(SpringLayout.NORTH, btnOpenCel, 8, SpringLayout.SOUTH, scrollPaneCel);
		springLayout.putConstraint(SpringLayout.WEST, btnOpenCel, 24, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, btnOpenCel, 31, SpringLayout.SOUTH, scrollPaneCel);
		springLayout.putConstraint(SpringLayout.EAST, btnOpenCel, -884, SpringLayout.EAST, this);
		btnOpenCel.setMargin(new Insets(0, 0, 0, 0));
		btnOpenCel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIFileOpen guiFileOpen = new GUIFileOpen();
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("affy raw data", "*");
				for (String string : lsFileName) {
					String abbr = FileOperate.getFileNameSep(string)[0];
					scrollPaneCel.addProview(new String[]{string,abbr});
				}
			}
		});
		add(btnOpenCel);
		
		JButton btnDelCel = new JButton("Delete");
		btnDelCel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneCel.removeSelRows();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnDelCel, 7, SpringLayout.SOUTH, scrollPaneCel);
		springLayout.putConstraint(SpringLayout.SOUTH, btnDelCel, 31, SpringLayout.SOUTH, scrollPaneCel);
		btnDelCel.setMargin(new Insets(0, 0, 0, 0));
		add(btnDelCel);
		
		scrollPaneNormData = new JScrollPaneData();
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneCel, -53, SpringLayout.WEST, scrollPaneNormData);
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneNormData, 36, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneNormData, 385, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneNormData, -10, SpringLayout.EAST, this);
		add(scrollPaneNormData);
		
		JButton btnOpenNormFile = new JButton("NormedFile");
		btnOpenNormFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIFileOpen guiFileOpen = new GUIFileOpen();
				String excelFile = guiFileOpen.openFileName("Normalized Data", "*");
				ArrayList<String[]> lsNormData = ExcelTxtRead.readLsExcelTxt(excelFile, 1);
				limmaAffy.normData(lsNormData);
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneNormData, -6, SpringLayout.NORTH, btnOpenNormFile);
		springLayout.putConstraint(SpringLayout.SOUTH, btnOpenNormFile, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, btnOpenNormFile, -10, SpringLayout.EAST, this);
		btnOpenNormFile.setMargin(new Insets(0, 0, 0, 0));
		add(btnOpenNormFile);
		
		scrollPaneSample = new JScrollPaneData();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneSample, 24, SpringLayout.SOUTH, btnOpenCel);
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneSample, 22, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneSample, -53, SpringLayout.WEST, scrollPaneNormData);
		scrollPaneSample.setTitle(new String[]{"SampleColumn","GroupName"});
		add(scrollPaneSample);
		
		JButton btnSetSample = new JButton("SetSample");
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneSample, -6, SpringLayout.NORTH, btnSetSample);
		springLayout.putConstraint(SpringLayout.WEST, btnSetSample, 0, SpringLayout.WEST, scrollPaneCel);
		btnSetSample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneSample.addProview(new String[]{"",""});
			}
		});
		btnSetSample.setMargin(new Insets(0, 0, 0, 0));
		add(btnSetSample);
		
		JButton btnNorm = new JButton("NormData");
		btnNorm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String[]> lsFile = scrollPaneCel.getLsDataInfo();
				limmaAffy.cleanRawData();
				for (String[] strings : lsFile) {
					if (FileOperate.isFile(strings[0])) {
						limmaAffy.addCelFile(strings[0], strings[1]);
					}
				}
				ArrayList<String[]> lsNorm = limmaAffy.normData();
				scrollPaneNormData.setProview(lsNorm);
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, btnDelCel, -32, SpringLayout.WEST, btnNorm);
		springLayout.putConstraint(SpringLayout.SOUTH, btnNorm, 31, SpringLayout.SOUTH, scrollPaneCel);
		btnNorm.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.NORTH, btnNorm, 6, SpringLayout.SOUTH, scrollPaneCel);
		springLayout.putConstraint(SpringLayout.EAST, btnNorm, 0, SpringLayout.EAST, scrollPaneCel);
		add(btnNorm);
		
		JButton btnDelSample = new JButton("Delete");
		btnDelSample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneSample.removeSelRows();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnDelSample, 0, SpringLayout.NORTH, btnSetSample);
		springLayout.putConstraint(SpringLayout.EAST, btnDelSample, 0, SpringLayout.EAST, scrollPaneCel);
		btnDelSample.setMargin(new Insets(0, 0, 0, 0));
		add(btnDelSample);
		
		scrollPaneDesign = new JScrollPaneData();
		scrollPaneDesign.setTitle(new String[]{"group1","group2"});
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneDesign, 462, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, btnSetSample, -6, SpringLayout.NORTH, scrollPaneDesign);
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneDesign, 22, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneDesign, 0, SpringLayout.EAST, scrollPaneCel);
		add(scrollPaneDesign);
		
		JButton btnSetDesign = new JButton("addDesign");
		btnSetDesign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneDesign.addProview(new String[]{"",""});
			}
		});
		btnSetDesign.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneDesign, -4, SpringLayout.NORTH, btnSetDesign);
		springLayout.putConstraint(SpringLayout.NORTH, btnSetDesign, -2, SpringLayout.NORTH, btnOpenNormFile);
		springLayout.putConstraint(SpringLayout.WEST, btnSetDesign, 0, SpringLayout.WEST, scrollPaneCel);
		add(btnSetDesign);
		
		JButton btnDelDesign = new JButton("delete");
		btnDelDesign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrollPaneDesign.removeSelRows();
			}
		});
		btnDelDesign.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.NORTH, btnDelDesign, -2, SpringLayout.NORTH, btnOpenNormFile);
		springLayout.putConstraint(SpringLayout.WEST, btnDelDesign, 45, SpringLayout.EAST, btnSetDesign);
		btnSetDesign.setMargin(new Insets(0, 0, 0, 0));
		add(btnDelDesign);
		
		JButton btnFindDif = new JButton("FindDif");
		springLayout.putConstraint(SpringLayout.NORTH, btnFindDif, 0, SpringLayout.NORTH, btnOpenNormFile);
		springLayout.putConstraint(SpringLayout.EAST, btnFindDif, 0, SpringLayout.EAST, scrollPaneCel);
		btnFindDif.setMargin(new Insets(0, 0, 0, 0));
		btnFindDif.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				limmaAffy.clearCompInfo();
				limmaAffy.clearGroupInfo();
				GUIFileOpen guiFileOpen = new GUIFileOpen();
				String fileName = guiFileOpen.saveFileName("excel 2003", "xls");
				ArrayList<String[]> lsSample2Group = scrollPaneSample.getLsDataInfo();
				for (String[] strings : lsSample2Group) {
					int[] col = PatternOperate.getNumAll(strings[0]);
					for (int i : col) {
						limmaAffy.addGroupInfo(i, strings[1].trim());
					}
				}
				ArrayList<String[]> lsGroup = scrollPaneDesign.getLsDataInfo();
				for (String[] strings : lsGroup) {
					limmaAffy.addCompInfo(strings[0].trim(), strings[1].trim());
				}
				limmaAffy.difGeneFinder(fileName);
			}
		});
		add(btnFindDif);

	}
}
