package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiBatchAnno;

public class CtrlBatchAnnoGene extends CtrlBatchAnno{
	AnnoQuery annoQuery = new AnnoQuery();
	Species species;

	public CtrlBatchAnnoGene(GuiBatchAnno guiBatchAnno) {
		super.guiBatchAnno = guiBatchAnno;
		annoQuery.setRunGetInfo(this);
	}
	public void setSpecies(Species species) {
		this.species = species;
		annoQuery.setTaxIDthis(species.getTaxID());
	}
	public void setSpecies(int taxID) {
		this.species = new Species(taxID);
		annoQuery.setTaxIDthis(taxID);
	}
	public void setListQuery(ArrayList<String[]> lsGeneInfo) {
		guiBatchAnno.getProcessBar().setMinimum(0);
		guiBatchAnno.getProcessBar().setMaximum(lsGeneInfo.size() - 1);
		annoQuery.setLsGeneID(lsGeneInfo);
		annoQuery.setFirstLineFrom1(2);
	}
	public void setColumnAccIDFrom1(int colAccID) {
		annoQuery.setColAccIDFrom1(colAccID);
	}
	public void setBlastTo(boolean blast, int subjectID) {
		annoQuery.setBlast(blast);
		annoQuery.setTaxIDblastTo(subjectID);
	}
	
	public String[] getTitle() {
		return annoQuery.getTitle();
	}
	public void execute() {
		Thread thread = new Thread(annoQuery);
		thread.start();
	}
	public ArrayList<String[]> getResult() {
		return annoQuery.getLsResult();
	}
	@Override
	public void setColPeakSummit(int colSummit) {
		return;
	}
	@Override
	public void setColPeakStartEnd(int start, int end) {
		return;
	}
	@Override
	public void setIsSummitSearch(boolean summitSearch) {
		return;
	}

}
