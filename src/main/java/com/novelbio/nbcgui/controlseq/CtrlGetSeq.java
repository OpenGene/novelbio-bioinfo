package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrSeq.GffChrSeqProcessInfo;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;
import com.novelbio.base.RunGetInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiGetSeq;

public class CtrlGetSeq implements RunGetInfo<GffChrSeq.GffChrSeqProcessInfo>{
	int[] upAndDownStream = new int[2];
	Species species;
	GffChrAbs gffChrAbs;
	GuiGetSeq guiGetSeq;
	GffChrSeq gffChrSeq = new GffChrSeq();
	
	public CtrlGetSeq(GuiGetSeq guiGetSeq) {
		this.guiGetSeq = guiGetSeq;
		gffChrSeq.setRunGetInfo(this);
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		gffChrSeq.setGffChrAbs(this.gffChrAbs);
	}
	public void setSpecies(Species species) {
		if (this.species != null && this.species.equals(species)) {
			return;
		}
		this.species = species;
		gffChrAbs = new GffChrAbs(species);
		gffChrSeq.setGffChrAbs(gffChrAbs);
	}
	public void setUpAndDownStream(int[] upAndDownStream) {
		this.upAndDownStream = upAndDownStream;
	}
	/** 默认是 true */
	public void setSaveToFile(boolean saveToFile) {
		gffChrSeq.setSaveToFile(saveToFile);
	}
	
	/** 提取单个基因的时候<br>
	 * true：提取该基因对应的转录本<br>
	 * false 提取该基因所在基因的最长转录本<br>
	 * @param absIso
	 */
	public void setAbsIso(boolean absIso) {
		gffChrSeq.setAbsIso(absIso);
	}
	/** 提取全基因组序列的时候，是每个LOC提取一条序列还是提取全部 */
	public void setGetAllIso(boolean getAllIso) {
		gffChrSeq.setGetAllIso(getAllIso);
	}
	/**
	 * 提取基因的时候遇到内含子，是提取出来还是跳过去
	 * @param getIntron
	 */
	public void setGetIntron(boolean getIntron) {
		gffChrSeq.setGetIntron(getIntron);
	}
	public void setGetAAseq(boolean getAAseq) {
		gffChrSeq.setGetAAseq(getAAseq);
	}
 
	public void setOutPutFile(String outPutFile) {
		gffChrSeq.setOutPutFile(outPutFile);
	}
	/** 待提取基因的哪一个部分 */
	public void setGeneStructure(GeneStructure geneStructure) {
		gffChrSeq.setGeneStructure(geneStructure);
	}
	/**
	 * 输入名字提取序列，内部会去除重复基因
	 * @param lsIsoName
	 */
	public void setGetSeqIso(ArrayList<String> lsIsoName) {
		gffChrSeq.setGetSeqIso(lsIsoName);
	}
	/**
	 * 输入名字提取序列，内部会去除重复基因
	 * @param lsIsoName
	 */
	public void setGetSeqIsoGenomWide() {
		gffChrSeq.setGetSeqIsoGenomWide();
	}
	/**
	 * 输入位点提取序列
	 * @param lsIsoName
	 */
	public void setGetSeqSite(ArrayList<SiteInfo> lsIsoName) {
		gffChrSeq.setGetSeqSite(lsIsoName);
	}
	/** 如果不是保存在文件中，就可以通过这个来获得结果 */
	public ArrayList<SeqFasta> getLsResult() {
		return gffChrSeq.getLsResult();
	}
	@Override
	public void execute() {
		gffChrAbs.setFilterTssTes(upAndDownStream, upAndDownStream);
		guiGetSeq.getProgressBar().setMinimum(0);
		guiGetSeq.getProgressBar().setMaximum(gffChrSeq.getNumOfQuerySeq());
		guiGetSeq.getBtnOpen().setEnabled(false);
		guiGetSeq.getBtnSave().setEnabled(false);
		guiGetSeq.getBtnRun().setEnabled(false);
		Thread thread = new Thread(gffChrSeq);
		thread.start();
	}
	
	@Override
	public void setRunningInfo(GffChrSeqProcessInfo info) {
		guiGetSeq.getProgressBar().setValue(info.getNumber());
	}

	@Override
	public void done() {
		guiGetSeq.getProgressBar().setValue(guiGetSeq.getProgressBar().getMaximum());
		guiGetSeq.getBtnOpen().setEnabled(true);
		guiGetSeq.getBtnSave().setEnabled(true);
		guiGetSeq.getBtnRun().setEnabled(true);
	}

	@Override
	public void threadSuspend() {
		gffChrSeq.threadSuspend();
		
	}

	@Override
	public void threadResume() {
		gffChrSeq.threadResume();
	}

	@Override
	public void threadStop() {
		gffChrSeq.threadStop();
	}
}
