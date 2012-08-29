package com.novelbio.nbcgui.GUI;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JProgressBar;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.SnpGroupFilterInfo;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.gui.JComboBoxData;
import com.novelbio.base.gui.JScrollPaneData;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.controlseq.CtrlSnpCalling;
import com.novelbio.nbcgui.controlseq.CtrlSnpGetInfo;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JSpinner;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.UIManager;

/** snpCalling的界面 */
public class GuiSnpCalling extends JPanel implements GuiNeedOpenFile {
	private JTextField txtHetoSnpProp;
	private JTextField txtHetoMoreSnpProp;
	JTextPane txtInformation;
	
	JComboBoxData<Integer> combSnpLevel;
	JButton btnAddPileupFile;
	JButton btnDeletePileupFile;
	JButton btnRun;
	JProgressBar progressBar;
	
	GUIFileOpen guiFileOpen = new GUIFileOpen();
	
	JScrollPaneData sclInputFile;
	JScrollPaneData sclSnpFile;
	
	private JButton btnAddSnpfile;
	private JButton btnDeleteSnpFile;
	private JTextField txtOutput;
	
	ButtonGroup buttonGroupSnpCallingFinding;
	JRadioButton rdbtnSnpcalling;
	JRadioButton rdbtnGetSnpDetail;
	
	JSpinner spinColChrID;
	JSpinner spinColSnpStartSite;

	JComboBoxData<Species> cmbSpecies;
	JComboBoxData<String> cmbVersion;
	
	JButton btnOutput;
	
	CtrlSnpCalling ctrlSnpCalling = new CtrlSnpCalling(this);
	CtrlSnpGetInfo ctrlSnpGetInfo = new CtrlSnpGetInfo(this);
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	/**
	 * Create the panel.
	 */
	public GuiSnpCalling() {
		setLayout(null);
		
		sclInputFile = new JScrollPaneData();
		sclInputFile.setBounds(14, 38, 757, 137);
		add(sclInputFile);
		
		btnAddPileupFile = new JButton("AddFile");
		btnAddPileupFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("PileupFile", "");
				for (String fileName : lsFileName) {
					if (rdbtnSnpcalling.isSelected()) {
						String outSnpFileName = FileOperate.changeFileSuffix(fileName, "_SnpInfo", "txt");
						sclInputFile.addItem(new String[]{fileName, outSnpFileName});
					}
					else {
						String sampleName = FileOperate.getFileNameSep(fileName)[0];
						sclInputFile.addItem(new String[]{fileName, sampleName});
					}
				}
			}
		});
		btnAddPileupFile.setBounds(783, 38, 118, 24);
		add(btnAddPileupFile);
		
		btnDeletePileupFile = new JButton("Delete");
		btnDeletePileupFile.setBounds(783, 151, 118, 24);
		btnDeletePileupFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclInputFile.deleteSelRows();
			}
		});
		add(btnDeletePileupFile);
		
		combSnpLevel = new JComboBoxData<Integer>();
		combSnpLevel.setBounds(14, 213, 190, 23);
		add(combSnpLevel);
		
		JLabel lblSnpfilterquality = new JLabel("SnpFilterQuality");
		lblSnpfilterquality.setBounds(14, 187, 152, 14);
		add(lblSnpfilterquality);
		
		JLabel lblHetolessinfo = new JLabel("Heto Contain Snp Reads Prop Min");
		lblHetolessinfo.setBounds(296, 187, 279, 14);
		add(lblHetolessinfo);
		
		JLabel lblHetomorecontainreadsmin = new JLabel("Heto More Contain Snp Reads Prop Min");
		lblHetomorecontainreadsmin.setBounds(296, 217, 324, 14);
		add(lblHetomorecontainreadsmin);
		
		txtHetoSnpProp = new JTextField();
		txtHetoSnpProp.setBounds(657, 185, 114, 18);
		add(txtHetoSnpProp);
		txtHetoSnpProp.setColumns(10);
		
		txtHetoMoreSnpProp = new JTextField();
		txtHetoMoreSnpProp.setBounds(657, 215, 114, 18);
		add(txtHetoMoreSnpProp);
		txtHetoMoreSnpProp.setColumns(10);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(14, 511, 767, 14);
		add(progressBar);
		
		btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rdbtnSnpcalling.isSelected()) {
					runSnpCalling();
				}
				else {
					runSnpGetInfo();
				}
				
			}
		});
		btnRun.setBounds(793, 501, 118, 24);
		add(btnRun);
		
		sclSnpFile = new JScrollPaneData();
		sclSnpFile.setBounds(19, 301, 754, 115);
		add(sclSnpFile);
		
		btnAddSnpfile = new JButton("AddSnpFile");
		btnAddSnpfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> lsFileName = guiFileOpen.openLsFileName("SnpFile", "");
				for (String fileName : lsFileName) {
					sclSnpFile.addItem(new String[]{fileName});
				}
			}
		});
		btnAddSnpfile.setBounds(795, 301, 118, 24);
		add(btnAddSnpfile);
		
		btnDeleteSnpFile = new JButton("Delete");
		btnDeleteSnpFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sclSnpFile.deleteSelRows();
			}
		});
		btnDeleteSnpFile.setBounds(795, 392, 118, 24);
		add(btnDeleteSnpFile);
		
		rdbtnSnpcalling = new JRadioButton("SnpCalling");
		rdbtnSnpcalling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSnpCalling();
			}
		});
		rdbtnSnpcalling.setBounds(14, 8, 151, 22);
		add(rdbtnSnpcalling);
		
		rdbtnGetSnpDetail = new JRadioButton("get snp detail");
		rdbtnGetSnpDetail.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSnpGetInfo();
			}
		});
		rdbtnGetSnpDetail.setBounds(185, 8, 151, 22);
		add(rdbtnGetSnpDetail);
		
		txtOutput = new JTextField();
		txtOutput.setBounds(18, 435, 565, 18);
		add(txtOutput);
		txtOutput.setColumns(10);
		
		btnOutput = new JButton("OutPut");
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = guiFileOpen.saveFileName("snpDetail", "");
				txtOutput.setText(fileName);
			}
		});
		btnOutput.setBounds(613, 432, 118, 24);
		add(btnOutput);
		
		JLabel lblSnpsitefile = new JLabel("SnpSiteFile");
		lblSnpsitefile.setBounds(16, 275, 107, 14);
		add(lblSnpsitefile);
		
		txtInformation = new JTextPane();
		txtInformation.setText("Information");
		txtInformation.setBackground(UIManager.getColor("Button.background"));
		txtInformation.setEditable(false);
		txtInformation.setBounds(19, 465, 537, 34);
		add(txtInformation);
		
		JLabel lblColChrid = new JLabel("Col ChrID");
		lblColChrid.setBounds(115, 275, 69, 14);
		add(lblColChrid);
		
		spinColChrID = new JSpinner();
		spinColChrID.setBounds(185, 270, 43, 24);
		add(spinColChrID);
		
		JLabel lblColSnpStart = new JLabel("Col Snp Start Site");
		lblColSnpStart.setBounds(271, 275, 152, 14);
		add(lblColSnpStart);
		
		spinColSnpStartSite = new JSpinner();
		spinColSnpStartSite.setBounds(424, 271, 50, 23);
		add(spinColSnpStartSite);
		
		cmbSpecies = new JComboBoxData<Species>();
		cmbSpecies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cmbVersion.setMapItem(cmbSpecies.getSelectedValue().getMapVersion());
			}
		});
		cmbSpecies.setBounds(367, 8, 189, 23);
		add(cmbSpecies);
		
		cmbVersion = new JComboBoxData<String>();
		cmbVersion.setBounds(591, 8, 152, 23);
		add(cmbVersion);

		initial();
	}
	
	private void initial() {
		combSnpLevel.setMapItem(SnpGroupFilterInfo.getMap_Str2SnpLevel());
		buttonGroupSnpCallingFinding = new ButtonGroup();
		buttonGroupSnpCallingFinding.add(rdbtnGetSnpDetail);
		buttonGroupSnpCallingFinding.add(rdbtnSnpcalling);
		rdbtnSnpcalling.setSelected(true);
		
		sclSnpFile.setTitle(new String[]{"Input Snp File"});

		cmbSpecies.setMapItem(Species.getSpeciesName2Species(Species.SEQINFO_SPECIES));
		cmbVersion.setMapItem(cmbSpecies.getSelectedValue().getMapVersion());
		
		setSnpCalling();
	}
	/** 当为snpcalling时候的界面 */
	private void setSnpCalling() {
		sclInputFile.setTitle(new String[]{"Input PileUp File","Output Snp File"});
		combSnpLevel.setEnabled(true);
		txtHetoMoreSnpProp.setEnabled(true);
		txtHetoSnpProp.setEnabled(true);
		
		sclSnpFile.setEnabled(false);
		spinColChrID.setEnabled(false);
		spinColSnpStartSite.setEnabled(false);
		sclSnpFile.setEnabled(false);
		btnAddSnpfile.setEnabled(false);
		btnDeleteSnpFile.setEnabled(false);
		btnOutput.setEnabled(false);
		txtOutput.setEnabled(false);
	}
	
	/** 当为获得每个snp信息的时候的界面 */
	private void setSnpGetInfo() {
		sclInputFile.setTitle(new String[]{"Input PileUp File", "Sample Name"});
		combSnpLevel.setEnabled(false);
		txtHetoMoreSnpProp.setEnabled(false);
		txtHetoSnpProp.setEnabled(false);
		
		sclSnpFile.setEnabled(true);
		spinColChrID.setEnabled(true);
		spinColSnpStartSite.setEnabled(true);
		sclSnpFile.setEnabled(true);
		btnAddSnpfile.setEnabled(true);
		btnDeleteSnpFile.setEnabled(true);
		btnOutput.setEnabled(true);
		txtOutput.setEnabled(true);
	}
	
	private void runSnpCalling() {
		setGffChrAbs(cmbSpecies.getSelectedValue());
		ctrlSnpCalling.setGffChrAbs(gffChrAbs);

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
	private void runSnpGetInfo() {
		setGffChrAbs(cmbSpecies.getSelectedValue());
		
		ctrlSnpGetInfo.setGffChrAbs(gffChrAbs);
		ArrayList<String[]> lsPileupFile = sclInputFile.getLsDataInfo();
		for (String[] strings : lsPileupFile) {
			ctrlSnpGetInfo.addPileupFile(strings[1], strings[0]);
		}
		ArrayList<String> lsFiles = new ArrayList<String>();
		for (String[] strings : sclSnpFile.getLsDataInfo()) {
			lsFiles.add(strings[0]);
		}
		int colChrID = (Integer)spinColChrID.getValue();
		int colSiteStart = (Integer) spinColSnpStartSite.getValue();
		ctrlSnpGetInfo.setLsReadFile(lsFiles, colChrID, colSiteStart);
		ctrlSnpGetInfo.setOutfile(txtOutput.getText());
		ctrlSnpGetInfo.runSnpGetInfo();
	}
	
	private void setGffChrAbs(Species species) {
		if (species.getTaxID() == 0) {
			return;
		}
		if (gffChrAbs.getTaxID() == 0 || !gffChrAbs.getSpecies().equals(species)) {
			gffChrAbs.setSpecies(species);
		}
	}
	
	public void setGuiFileOpen(GUIFileOpen guiFileOpen) {
		this.guiFileOpen = guiFileOpen;
	}
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	/** 读取信息 */
	public JTextPane getTxtInfo() {
		return txtInformation;
	}
	public JButton getBtnAddPileupFile() {
		return btnAddPileupFile;
	}
	public JButton getBtnDeletePileup() {
		return btnDeletePileupFile;
	}
	public JButton getBtnAddSnpFile() {
		return btnAddSnpfile;
	}
	public JButton getBtnDeleteSnp() {
		return btnDeleteSnpFile;
	}
	public JButton getBtnRun() {
		return btnRun;
	}
}
