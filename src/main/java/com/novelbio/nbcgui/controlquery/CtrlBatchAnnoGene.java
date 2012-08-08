package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.base.RunGetInfo;
import com.novelbio.base.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiAnnoGene;

public class CtrlBatchAnnoGene implements RunGetInfo<AnnoQuery.AnnoQueryDisplayInfo> {
	GuiAnnoGene guiAnnoBatch;	

	AnnoQuery annoQuery = new AnnoQuery();
	Species species;

	public CtrlBatchAnnoGene(GuiAnnoGene guiBatchAnno) {
		this.guiAnnoBatch = guiBatchAnno;
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
		guiAnnoBatch.getProcessBar().setMinimum(0);
		guiAnnoBatch.getProcessBar().setMaximum(lsGeneInfo.size() - 1);
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
	public void setRunningInfo(AnnoQueryDisplayInfo info) {
		guiAnnoBatch.getProcessBar().setValue((int) info.getCountNum());
		guiAnnoBatch.getJScrollPaneDataResult().addRow(info.getTmpInfo());
	}
	
	@Override
	public void done(RunProcess<AnnoQueryDisplayInfo> runProcess) {
		guiAnnoBatch.getProcessBar().setValue(guiAnnoBatch.getProcessBar().getMaximum());
		guiAnnoBatch.getBtnSave().setEnabled(true);
		guiAnnoBatch.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadSuspend() {
		// TODO Auto-generated method stub
		guiAnnoBatch.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadResume() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void threadStop() {
		// TODO Auto-generated method stub
		guiAnnoBatch.getBtnRun().setEnabled(true);
	}

}
