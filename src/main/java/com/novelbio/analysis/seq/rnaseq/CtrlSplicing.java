package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowFieldMatrix;

import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.nbcgui.GUI.GuiAnnoInfo;
import com.novelbio.nbcgui.GUI.GuiRNAautoSplice;

public class CtrlSplicing implements RunGetInfo<GuiAnnoInfo> , Runnable{
	GuiRNAautoSplice guiRNAautoSplice;
	GffHashGene gffHashGene;
	boolean isDisplayAllEvent = true; 
	String outFile;
	List<String[]> lsBam2Prefix;
	List<String[]> lsCompareGroup;
	
	
	public void setGuiRNAautoSplice(GuiRNAautoSplice guiRNAautoSplice) {
		this.guiRNAautoSplice = guiRNAautoSplice;
	}
	
	public void setLsBam2Prefix(List<String[]> lsBam2Prefix) {
		this.lsBam2Prefix = lsBam2Prefix;
	}
	public void setLsCompareGroup(List<String[]> lsCompareGroup) {
		this.lsCompareGroup = lsCompareGroup;
	}
	
	@Override
	public void setRunningInfo(GuiAnnoInfo info) {
		guiRNAautoSplice.setRunningInfo(info);
	}

	@Override
	public void done(RunProcess<GuiAnnoInfo> runProcess) {
		guiRNAautoSplice.done(runProcess);
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
		// TODO Auto-generated method stub
		
	}
	
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	public void setDisplayAllEvent(boolean isDisplayAllEvent) {
		this.isDisplayAllEvent = isDisplayAllEvent;
	}
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	@Override
	public void run() {
		ExonJunction exonJunction = new ExonJunction();
		exonJunction.setGffHashGene(gffHashGene);
		exonJunction.setOneGeneOneSpliceEvent(!isDisplayAllEvent);
		exonJunction.setRunGetInfo(this);
		
		if (FileOperate.isFileDirectory(outFile)) {
			outFile = FileOperate.addSep(outFile);
		}
		for (String[] strings : lsBam2Prefix) {
			//TODO 暂时没有多对多比较
			exonJunction.addBamSorted(strings[2], strings[0]);
		}
		//TODO
		exonJunction.setCompareGroupsLs(lsCompareGroup);
		exonJunction.setResultFile(outFile);
		exonJunction.setRunGetInfo(this);
		
		Thread thread = new Thread(exonJunction);
		thread.start();
		
		try { Thread.sleep(2000); } catch (InterruptedException e) { }

		while (exonJunction.isRunning()) {
			try { Thread.sleep(300); } catch (InterruptedException e) { }
		}
		if (!exonJunction.isFinished()) {
			guiRNAautoSplice.setMessage("Error");
		} else {
			guiRNAautoSplice.setMessage("Finished");
		}
		guiRNAautoSplice.done(null);
	}

	public void setProgressBarLevelLs(ArrayList<Double> lsLevels) {
		if (guiRNAautoSplice == null) {
			return;
		}
		guiRNAautoSplice.setProgressBarLevelLs(lsLevels);
	}

	public void setProcessBarStartEndBarNum(String string, int level, long startBarNum, long endBarNum) {
		if (guiRNAautoSplice == null) {
			return;
		}
		guiRNAautoSplice.setProcessBarStartEndBarNum(string, level, startBarNum, endBarNum);
	}
	public void setInfo(String string) {
		if (guiRNAautoSplice == null) {
			return;
		}
		guiRNAautoSplice.setInfo(string);
	}
	public void setDetailInfo(String string) {
		if (guiRNAautoSplice == null) {
			return;
		}
		guiRNAautoSplice.setDetailInfo(string);
	}

}
