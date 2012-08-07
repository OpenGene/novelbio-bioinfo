package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrStatistics;
import com.novelbio.analysis.seq.genomeNew.GffChrStatistics.GffChrStatiscticsProcessInfo;
import com.novelbio.base.RunGetInfo;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiPeakStatistics;

public class CtrlPeakStatistics implements RunGetInfo<GffChrStatistics.GffChrStatiscticsProcessInfo, GffChrStatistics>{
	GuiPeakStatistics guiPeakStatistics;
	GffChrStatistics gffChrStatistics = new GffChrStatistics();
	
	boolean filterGeneBody = true;
	boolean filterExon = false;
	boolean filterIntron = false;
	boolean filter5UTR = false;
	boolean filter3UTR = false;
	
	Species species;
	
	String readFile = "";
	
	public CtrlPeakStatistics(GuiPeakStatistics guiPeakStatistics) {
		this.guiPeakStatistics = guiPeakStatistics;
		gffChrStatistics.setRunGetInfo(this);
	}
	public void setSpecies(Species species) {
		this.species = species;
	}
	public void setQueryFile(String readFile) {
		this.readFile = readFile;
	}
	public void setColPeakSummit(int colSummit) {
		gffChrStatistics.setColSummit(colSummit);
	}
	public void setColChrID(int colChrID) {
		gffChrStatistics.setColChrID(colChrID);
	}
	public void setTssRange(int[] tss) {
		gffChrStatistics.setTssRegion(tss);
	}
	public void setTesRange(int[] tes) {
		gffChrStatistics.setTesRegion(tes);
	}
	public void setFilterGeneBody(boolean filterGeneBody) {
		this.filterGeneBody = filterGeneBody;
	}
	public void execute() {
		long fileSizeLong = FileOperate.getFileSizeLong(readFile);
		int fileSize = (int)(fileSizeLong/1000000);
		guiPeakStatistics.getProcessBar().setMaximum(fileSize);
		gffChrStatistics.clean();
		gffChrStatistics.setSpecies(species);
		gffChrStatistics.setFileName(readFile);
		
		Thread thread = new Thread(gffChrStatistics);
		thread.start();
	}
	public ArrayList<String[]> getResult() {
		return gffChrStatistics.getStatisticsResult();
	}
	@Override
	public void setRunningInfo(GffChrStatiscticsProcessInfo info) {
		guiPeakStatistics.getProcessBar().setValue(info.getReadsize());
	}
	@Override
	public void done(GffChrStatistics gffChrStatistics) {
		guiPeakStatistics.getProcessBar().setValue(guiPeakStatistics.getProcessBar().getMaximum());
		guiPeakStatistics.getBtnSave().setEnabled(true);
		guiPeakStatistics.getBtnRun().setEnabled(true);
		guiPeakStatistics.getJScrollPaneDataResult().setItemLs(getResult());
	}
	@Override
	public void threadSuspend() {
		guiPeakStatistics.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadResume() {
		gffChrStatistics.threadResume();
		guiPeakStatistics.getBtnRun().setEnabled(false);
	}
	@Override
	public void threadStop() {
		gffChrStatistics.threadSuspend();
		guiPeakStatistics.getBtnRun().setEnabled(true);
	}
}
