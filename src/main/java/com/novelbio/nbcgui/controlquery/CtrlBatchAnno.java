package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.analysis.annotation.genAnno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.base.RunGetInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiAnnoGene;

/**
 * 批量注释常规accID的annotation或者是peak的annotation
 * @author zong0jie
 *
 */
public abstract class CtrlBatchAnno implements RunGetInfo<AnnoQuery.AnnoQueryDisplayInfo> {

	
	public abstract void setSpecies(Species species);
	public abstract void setSpecies(int taxID);
	public abstract void setListQuery(ArrayList<String[]> lsGeneInfo);
	public abstract void setColumnAccIDFrom1(int colAccID);
	public abstract void setBlastTo(boolean blast, int subjectID);
	public abstract void setColPeakSummit(int colSummit);
	public abstract void setColPeakStartEnd(int start, int end);
	public abstract void setIsSummitSearch(boolean summitSearch);
	public abstract ArrayList<String[]> getResult();
	public abstract void execute();
	public abstract String[] getTitle();
	


}