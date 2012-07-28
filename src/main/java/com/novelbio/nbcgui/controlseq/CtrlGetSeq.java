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
	/** Ĭ���� true */
	public void setSaveToFile(boolean saveToFile) {
		gffChrSeq.setSaveToFile(saveToFile);
	}
	
	/** ��ȡ���������ʱ��<br>
	 * true����ȡ�û����Ӧ��ת¼��<br>
	 * false ��ȡ�û������ڻ�����ת¼��<br>
	 * @param absIso
	 */
	public void setAbsIso(boolean absIso) {
		gffChrSeq.setAbsIso(absIso);
	}
	/** ��ȡȫ���������е�ʱ����ÿ��LOC��ȡһ�����л�����ȡȫ�� */
	public void setGetAllIso(boolean getAllIso) {
		gffChrSeq.setGetAllIso(getAllIso);
	}
	/**
	 * ��ȡ�����ʱ�������ں��ӣ�����ȡ������������ȥ
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
	/** ����ȡ�������һ������ */
	public void setGeneStructure(GeneStructure geneStructure) {
		gffChrSeq.setGeneStructure(geneStructure);
	}
	/**
	 * ����������ȡ���У��ڲ���ȥ���ظ�����
	 * @param lsIsoName
	 */
	public void setGetSeqIso(ArrayList<String> lsIsoName) {
		gffChrSeq.setGetSeqIso(lsIsoName);
	}
	/**
	 * ����������ȡ���У��ڲ���ȥ���ظ�����
	 * @param lsIsoName
	 */
	public void setGetSeqIsoGenomWide() {
		gffChrSeq.setGetSeqIsoGenomWide();
	}
	/**
	 * ����λ����ȡ����
	 * @param lsIsoName
	 */
	public void setGetSeqSite(ArrayList<SiteInfo> lsIsoName) {
		gffChrSeq.setGetSeqSite(lsIsoName);
	}
	/** ������Ǳ������ļ��У��Ϳ���ͨ���������ý�� */
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
