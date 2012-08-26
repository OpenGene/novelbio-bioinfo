package com.novelbio.nbcgui.controlseq;

import javax.swing.JOptionPane;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.SnpCalling;
import com.novelbio.analysis.seq.resequencing.SnpFilterDetailInfo;
import com.novelbio.analysis.seq.resequencing.SnpGroupFilterInfo;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiSnpCalling;

/** 查找snp */
public class CtrlSnpCalling implements RunGetInfo<SnpFilterDetailInfo> {
	SnpCalling snpCalling = new SnpCalling();
	GuiSnpCalling guiSnpCalling;
	GffChrAbs gffChrAbs;
	Species species;
	SnpGroupFilterInfo snpGroupInfoFilter = new SnpGroupFilterInfo();
	
	public CtrlSnpCalling(GuiSnpCalling guiSnpCalling) {
		this.guiSnpCalling = guiSnpCalling;
		snpCalling.setSampleDetail(snpGroupInfoFilter);
	}
	
	public void setSpecies(Species species) {
		gffChrAbs = new GffChrAbs(species);
	}
	/** snp过滤等级 */
	public void set(int snpLevel) {
		snpGroupInfoFilter.setSnpLevel(snpLevel);
	}
	public void addSnpFromPileUpFile(String pileUpFile, String outSnpFile) {
		String sampleName = FileOperate.getFileNameSep(pileUpFile)[0];
		snpCalling.addSnpFromPileUpFile(sampleName, pileUpFile, outSnpFile);
	}
	public void setProcessBar() {
		guiSnpCalling.getProgressBar().setMaximum((int) snpCalling.getFileSizeEvaluateK());
	}
	public void clean() {
		snpCalling.clearSnpFromPileUpFile();
	}
	
	public void setSnp_Hete_Contain_SnpProp_Min(double setSnp_Hete_Contain_SnpProp_Min) {
		snpCalling.setSnp_Hete_Contain_SnpProp_Min(setSnp_Hete_Contain_SnpProp_Min);
	}
	public void setSnp_HetoMore_Contain_SnpProp_Min(double snp_HetoMore_Contain_SnpProp_Min) {
		snpCalling.setSnp_HetoMore_Contain_SnpProp_Min(snp_HetoMore_Contain_SnpProp_Min);
	}
	
	public void runSnpCalling() {
		Thread thread = new Thread(snpCalling);
		thread.start();
		
		guiSnpCalling.getBtnAddFile().setEnabled(false);
		guiSnpCalling.getBtnDelete().setEnabled(false);
		guiSnpCalling.getBtnRun().setEnabled(false);
	}
	@Override
	public void setRunningInfo(SnpFilterDetailInfo info) {
		long kb = info.getAllByte()/1000;
		guiSnpCalling.getProgressBar().setValue((int) kb);
		if (info.getMessage() != null) {
			guiSnpCalling.getLblInfo().setText(info.getMessage());
		}
	}
	
	public void stop() {
		snpCalling.threadStop();
	}
	public void suspend() {
		snpCalling.threadSuspend();
	}
	public void resume() {
		snpCalling.threadResume();
	}
	
	@Override
	public void done(RunProcess<SnpFilterDetailInfo> runProcess) {
		guiSnpCalling.getProgressBar().setValue(guiSnpCalling.getProgressBar().getMaximum());
		guiSnpCalling.getLblInfo().setText("Snp Calling Complete");
		JOptionPane.showMessageDialog(guiSnpCalling, "Snp Calling Complete", "finish", JOptionPane.INFORMATION_MESSAGE);
		
		guiSnpCalling.getBtnAddFile().setEnabled(true);
		guiSnpCalling.getBtnDelete().setEnabled(true);
		guiSnpCalling.getBtnRun().setEnabled(true);
	}

	@Override
	public void threadSuspended(RunProcess<SnpFilterDetailInfo> runProcess) {
		guiSnpCalling.getBtnRun().setEnabled(true);
	}

	@Override
	public void threadResumed(RunProcess<SnpFilterDetailInfo> runProcess) {
		guiSnpCalling.getBtnRun().setEnabled(false);
	}

	@Override
	public void threadStop(RunProcess<SnpFilterDetailInfo> runProcess) {
		guiSnpCalling.getBtnRun().setEnabled(true);
	}
}
