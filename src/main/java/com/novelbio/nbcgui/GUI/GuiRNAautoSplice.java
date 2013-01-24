package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.rnaseq.ExonJunction;
import com.novelbio.analysis.seq.rnaseq.ExonSplicingTest;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;

public class GuiRNAautoSplice extends JPanel implements RunGetInfo<GuiAnnoInfo> {
	static final int progressLength = 10000;
	private JTextField txtGff;
	JScrollPaneData scrlBam;
	JScrollPaneData scrlCompare;
	JComboBoxData<Species> combSpecies;
	JComboBoxData<String> combVersion;
	JButton btnOpeanbam;
	JButton btnDelbam;
	JButton btnRun;
	JButton btnOpengtf;
	JCheckBox chckbxDisplayAllSplicing;
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	private JTextField txtSaveTo;
	
	JProgressBar progressBar;
	JLabel lblInformation;
	ExonJunction exonJunction;
	/** 设定bar的分级<br>
	 * 现在是3级<br>
	 * 就是读取junction 1级<br>
	 * 读取表达1级<br>
	 * 计算差异1级<br>
	 */
	List<Double> lsProgressBarLevel = new ArrayList<Double>();
	long startBarNum;
	long endBarNum;
	int level;

	
	/**
	 * Create the panel.
	 */
	public GuiRNAautoSplice() {
		setLayout(null);
		
		combSpecies = new JComboBoxData<Species>();
		combSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectSpecies();
			}
		});
		combSpecies.setBounds(642, 49, 223, 23);
		add(combSpecies);
		
		combVersion = new JComboBoxData<String>();
		combVersion.setBounds(642, 110, 223, 23);
		add(combVersion);
		
		scrlBam = new JScrollPaneData();
		scrlBam.setBounds(20, 34, 610, 303);
		add(scrlBam);
		
		btnOpeanbam = new JButton("OpeanBam");
		btnOpeanbam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFile = guiFileOpen.openLsFileName("BamFile", "");
				ArrayList<String[]> lsInfo = new ArrayList<String[]>();
				for (String string : lsFile) {
					//0: fileName   1: sampleName 2:group
					String[] tmResult = new String[3];
					tmResult[0] = string; tmResult[1] = FileOperate.getFileNameSep(string)[0].split("_")[0];
					tmResult[2] = tmResult[1];
					lsInfo.add(tmResult);
				}
				scrlBam.addItemLs(lsInfo);
				
				if (exonJunction == null || !exonJunction.isRunning()) {
					btnRun.setEnabled(true);
				}
			}
		});
		btnOpeanbam.setBounds(31, 349, 118, 24);
		add(btnOpeanbam);
		
		btnDelbam = new JButton("DelBam");
		btnDelbam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlBam.deleteSelRows();
				if (exonJunction == null || !exonJunction.isRunning()) {
					btnRun.setEnabled(true);
				}
			}
		});
		btnDelbam.setBounds(369, 349, 118, 24);
		add(btnDelbam);
		
		txtGff = new JTextField();
		txtGff.setBounds(642, 162, 223, 18);
		add(txtGff);
		txtGff.setColumns(10);
		
		btnRun = new JButton("run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run();
			}
		});
		btnRun.setBounds(769, 427, 118, 24);
		add(btnRun);
		
		btnOpengtf = new JButton("OpenGTF");
		btnOpengtf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtGff.setText(guiFileOpen.openFileName("GTFfile", ""));
			}
		});
		btnOpengtf.setBounds(747, 190, 118, 24);
		add(btnOpengtf);
		
		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setBounds(642, 23, 69, 14);
		add(lblSpecies);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setBounds(642, 84, 69, 14);
		add(lblVersion);
		
		JLabel lblAddbamfile = new JLabel("AddBamFile");
		lblAddbamfile.setBounds(20, 12, 129, 14);
		add(lblAddbamfile);
		
		txtSaveTo = new JTextField();
		txtSaveTo.setBounds(20, 430, 532, 18);
		add(txtSaveTo);
		txtSaveTo.setColumns(10);
		
		JButton btnSaveto = new JButton("SaveTo");
		btnSaveto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtSaveTo.setText(guiFileOpen.saveFileName("Out", ""));
				if (exonJunction == null || !exonJunction.isRunning()) {
					btnRun.setEnabled(true);
				}
			}
		});
		btnSaveto.setBounds(604, 427, 118, 24);
		add(btnSaveto);
		
		scrlCompare = new JScrollPaneData();
		scrlCompare.setBounds(642, 239, 245, 76);
		add(scrlCompare);
		
		JLabel lblCompare = new JLabel("Compare");
		lblCompare.setBounds(642, 214, 69, 14);
		add(lblCompare);
		
		JButton btnAddCompare = new JButton("AddCompare");
		btnAddCompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlCompare.addItem(new String[]{"",""});
				if (exonJunction == null || !exonJunction.isRunning()) {
					btnRun.setEnabled(true);
				}
			}
		});
		btnAddCompare.setBounds(642, 327, 115, 24);
		add(btnAddCompare);
		
		JButton btnDeleteCompare = new JButton("DeleteCompare");
		btnDeleteCompare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrlCompare.deleteSelRows();
			}
		});
		btnDeleteCompare.setBounds(769, 327, 118, 24);
		add(btnDeleteCompare);
		
		chckbxDisplayAllSplicing = new JCheckBox("Display All Splicing Events");
		chckbxDisplayAllSplicing.setBounds(20, 400, 237, 22);
		add(chckbxDisplayAllSplicing);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(20, 487, 867, 14);
		add(progressBar);
		
		lblInformation = new JLabel("");
		lblInformation.setBounds(20, 460, 99, 14);
		add(lblInformation);
		
		initial();
	}
	private void initial() {
		combSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		selectSpecies();
		scrlBam.setTitle(new String[]{"BamFile", "Prefix", "group"});
		scrlCompare.setTitle(new String[] {"group1", "group2"});

		progressBar.setMinimum(0);
		progressBar.setMaximum(progressLength);
	}
	private void selectSpecies() {
		Species species = combSpecies.getSelectedValue();
		combVersion.setMapItem(species.getMapVersion());
	}
	/** 如果txt存在，优先获得txt对应的gtf文件*/
	private GffHashGene getGffhashGene() {
		GffHashGene gffHashGeneResult = null;
		Species species = combSpecies.getSelectedValue();
		String gtfFile = txtGff.getText();
		if (FileOperate.isFileExist(gtfFile)) {
			gffHashGeneResult = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, txtGff.getText());
		}
		else if (species.getTaxID() != 0) {
			species.setVersion(combVersion.getSelectedValue());
			GffChrAbs gffChrAbs = new GffChrAbs(species);
			gffHashGeneResult = gffChrAbs.getGffHashGene();
		}
		return gffHashGeneResult;
	}
	//TODO
	private Species getSpecies() {
		Species species = combSpecies.getSelectedValue();
		if (species.getTaxID() != 0) {
			species.setVersion(combVersion.getSelectedValue());
		}
		return species;
	}
	private void run() {
		progressBar.setValue(progressBar.getMinimum());
		exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(getGffhashGene());
		exonJunction.setOneGeneOneSpliceEvent(!chckbxDisplayAllSplicing.isSelected());
		String outFile = txtSaveTo.getText();
		if (FileOperate.isFileDirectory(outFile)) {
			outFile = FileOperate.addSep(outFile);
		}
		for (String[] strings : scrlBam.getLsDataInfo()) {
			exonJunction.addBamSorted(strings[1], strings[0]);
		}
		//TODO
		exonJunction.setCompareGroupsLs(scrlCompare.getLsDataInfo());
		exonJunction.setResultFile(outFile);
		exonJunction.setRunGetInfo(this);
		
		btnRun.setEnabled(false);
		Thread thread = new Thread(exonJunction);
		thread.start();

	}
	public void setProgressBarLevelLs(List<Double> lsProgressBarLevel) {
		this.lsProgressBarLevel = lsProgressBarLevel;
	}
	/**
	 * 设定本次步骤里面将绘制progressBar的第几部分
	 * 并且本部分的最短点和最长点分别是什么
	 * @param level 本次步骤里面将绘制progressBar的第几部分，也就是跑到第几步了。总共3步
	 * @param startBarNum 本步骤起点，一般为0
	 * @param endBarNum 本步骤终点
	 */
	public void setProcessBarStartEndBarNum(String information, int level, long startBarNum, long endBarNum) {
		this.level = level;
		this.startBarNum = startBarNum;
		this.endBarNum = endBarNum;
		this.lblInformation.setText(information);
	}
	private void setProcessBarValue(long number) {
		long progressNum = number - startBarNum;
		if (progressNum < 0) {
			progressNum = 0;
		} else if (progressNum > endBarNum - startBarNum) {
			progressNum = endBarNum - startBarNum;
		}
		double finalNum = (double)progressNum/(endBarNum - startBarNum);
		int progressBarNum = (int) finalNum;
		
		double startProgress = 0, endProgress = lsProgressBarLevel.get(level);
		if (level != 0) {
			startProgress = lsProgressBarLevel.get(level - 1);
		}
		int num = (int) ((endProgress - startProgress) * progressLength * finalNum + startProgress * progressLength);
		progressBar.setValue(num);
	}
	
	public void setInformation(String info) {
		this.lblInformation.setText(info);
	}
	
	@Override
	public void setRunningInfo(GuiAnnoInfo info) {
		setProcessBarValue((long) info.getNumDouble());
	}
	@Override
	public void done(RunProcess<GuiAnnoInfo> runProcess) {
		btnRun.setEnabled(true);
		progressBar.setValue(progressBar.getMaximum());
	}
	@Override
	public void threadSuspended(RunProcess<GuiAnnoInfo> runProcess) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void threadResumed(RunProcess<GuiAnnoInfo> runProcess) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void threadStop(RunProcess<GuiAnnoInfo> runProcess) {
		btnRun.setEnabled(true);
	}
}
