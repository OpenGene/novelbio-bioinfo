package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.multithread.RunProcess;
import com.novelbio.nbcgui.GUI.GuiAnnoInfo;

public interface GUIinfo {
	
	void setRunningInfo(GuiAnnoInfo string);
	
	void done(RunProcess<GuiAnnoInfo> runProcess);

	void setMessage(String string);

	void setProgressBarLevelLs(List<Double> lsLevels);

	void setProcessBarStartEndBarNum(String string, int level,
			long startBarNum, long endBarNum);

	void setDetailInfo(String string);

	void setInfo(String string);

}
