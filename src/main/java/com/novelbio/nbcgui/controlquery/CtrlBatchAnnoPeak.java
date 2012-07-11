package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiBatchAnno;

public class CtrlBatchAnnoPeak extends CtrlBatchAnno{
	GffChrAnno gffChrAnno = new GffChrAnno();
	Species species;
	
	public CtrlBatchAnnoPeak(GuiBatchAnno guiBatchAnno) {
		super.guiBatchAnno = guiBatchAnno;
		gffChrAnno.setRunGetInfo(this);
	}
	public void setSpecies(Species species) {
		this.species = species;
		gffChrAnno.setSpecies(species.getTaxID());
	}
	public void setSpecies(int taxID) {
		this.species = new Species(taxID);
		gffChrAnno.setSpecies(taxID);
	}
	public void setListQuery(ArrayList<String[]> lsGeneInfo) {
		guiBatchAnno.getProcessBar().setMinimum(0);
		guiBatchAnno.getProcessBar().setMaximum(lsGeneInfo.size() - 1);
		gffChrAnno.setLsGeneInfo(lsGeneInfo);
	}
	//不发挥功能
	public void setColumnAccIDFrom1(int colAccID) {
		return;
	}
	//不发挥功能
	public void setBlastTo(boolean blast, int subjectID) {
		return;
	}
	public void setColPeakSummit(int colSummit) {
		gffChrAnno.setColSummit(colSummit);
	}
	public void setColPeakStartEnd(int start, int end) {
		gffChrAnno.setColStartEnd(start, end);
	}
	public void setIsSummitSearch(boolean summitSearch) {
		gffChrAnno.setSearchSummit(summitSearch);
	}
	public void execute() {
		Thread thread = new Thread(gffChrAnno);
		thread.start();
	}
	public ArrayList<String[]> getResult() {
		return gffChrAnno.getLsResult();
	}
	@Override
	public String[] getTitle() {
		return gffChrAnno.getTitleGeneInfoFilterAnno();
		
	}
}
