package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiAnnoGene;

public class CtrlBatchAnnoGene implements RunGetInfo<AnnoQuery.AnnoQueryDisplayInfo> {
	GuiAnnoGene guiAnnoBatch;	
	List<String[]> lsIn2Out;
	AnnoQuery annoQuery = new AnnoQuery();
	Species species;
	int colAccID = 1;
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
	public void setListQuery(List<String[]> lsIn2Out) {
		guiAnnoBatch.getProcessBar().setMinimum(0);
		guiAnnoBatch.getProcessBar().setMaximum(1000);
		this.lsIn2Out = lsIn2Out;
	}
	public void setColumnAccIDFrom1(int colAccID) {
		annoQuery.setColAccIDFrom1(colAccID);
	}
	public void setBlastTo(boolean blast, int subjectID) {
		annoQuery.setBlast(blast);
		annoQuery.setTaxIDblastTo(subjectID);
	}

	public void execute() {
		for (String[] fileIn2Out : lsIn2Out) {
			annoQuery.setColAccIDFrom1(colAccID);
			annoQuery.setFirstLineFrom1(2);
			annoQuery.setGeneIDFile(fileIn2Out[0]);
			annoQuery.run();
			annoQuery.writeTo(fileIn2Out[1]);
		}
	}
	public ArrayList<String[]> getResult() {
		return annoQuery.getLsResult();
	}

	@Override
	public void setRunningInfo(AnnoQueryDisplayInfo info) {
		guiAnnoBatch.getProcessBar().setValue((int) info.getCountNum());
	}
	
	@Override
	public void done(RunProcess<AnnoQueryDisplayInfo> runProcess) {
		guiAnnoBatch.getProcessBar().setValue(guiAnnoBatch.getProcessBar().getMaximum());
		guiAnnoBatch.getBtnSave().setEnabled(true);
		guiAnnoBatch.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadSuspended(RunProcess<AnnoQueryDisplayInfo> runProcess) {
		guiAnnoBatch.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadResumed(RunProcess<AnnoQueryDisplayInfo> runProcess) {
		guiAnnoBatch.getBtnRun().setEnabled(false);
	}
	@Override
	public void threadStop(RunProcess<AnnoQueryDisplayInfo> runProcess) {
		guiAnnoBatch.getBtnRun().setEnabled(true);
	}

}
