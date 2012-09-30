package com.novelbio.nbcgui.controlseq;

import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs.MapReadsProcessInfo;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiRunningBarAbs;

public class CtrlMapReads implements RunGetInfo<MapReadsAbs.MapReadsProcessInfo>{
	String bedFileName;
	MapReads mapReads;
	GuiRunningBarAbs guiRunningBarAbs;
	

	public CtrlMapReads(GuiRunningBarAbs guiRunningBarAbs) {
		this.guiRunningBarAbs = guiRunningBarAbs;
		mapReads = new MapReads();
		mapReads.setRunGetInfo(this);
	}
	public void setBedFile(String bedFileName) {
		this.bedFileName = bedFileName;
		mapReads.setBedSeq(bedFileName);
	}
	public void setInvNum(int invNum) {
		mapReads.setInvNum(invNum);
	}
	public MapReads getMapReads() {
		return mapReads;
	}
	public void setSpecies(Species species) {
		mapReads.setMapChrID2Len(species.getMapChromInfo());
	}
	/**
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С�ڵ���0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique  Unique��reads����һ�� novelbio�ı���ڵ����У���1��ʼ����
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean FilteredStrand) {
		mapReads.setFilter(uniqReads, startCod, booUniqueMapping, FilteredStrand);
	}
	/**
	 * ����У��reads���ķ��̣�Ĭ���趨��������reads����СֵΪ0������У��С��0�Ķ���Ϊ0
	 * @param FormulatToCorrectReads
	 */
	public void setFormulatToCorrectReads(Equations FormulatToCorrectReads) {
		mapReads.setFormulatToCorrectReads(FormulatToCorrectReads);
	}
	 /**
	  * �趨��׼��������������ʱ�趨����һ��Ҫ�ڶ�ȡ�ļ�ǰ
	  * Ĭ����NORMALIZATION_ALL_READS
	  * @param normalType
	  */
	public void setNormalType(int normalType) {
		mapReads.setNormalType(normalType);
	}
	@Override
	public void setRunningInfo(MapReadsProcessInfo info) {
		guiRunningBarAbs.getProcessBar().setValue((int) (info.getReadsize()/1000000));
	}

	@Override
	public void done(RunProcess<MapReadsProcessInfo> runProcess) {
		guiRunningBarAbs.getProcessBar().setValue(guiRunningBarAbs.getProcessBar().getMaximum());
		guiRunningBarAbs.getBtnRun().setEnabled(true);
		guiRunningBarAbs.getBtnSave().setEnabled(true);
		guiRunningBarAbs.getBtnOpen().setEnabled(true);
	}

	@Override
	public void threadSuspended(RunProcess<MapReadsProcessInfo> runProcess) {
		guiRunningBarAbs.getBtnRun().setEnabled(true);
		
	}

	@Override
	public void threadResumed(RunProcess<MapReadsProcessInfo> runProcess) {
		guiRunningBarAbs.getBtnRun().setEnabled(false);
	}

	@Override
	public void threadStop(RunProcess<MapReadsProcessInfo> runProcess) {
		guiRunningBarAbs.getBtnRun().setEnabled(true);
		guiRunningBarAbs.getBtnSave().setEnabled(true);
		guiRunningBarAbs.getBtnOpen().setEnabled(true);
	}

	public void execute() {
		guiRunningBarAbs.getBtnRun().setEnabled(false);
		guiRunningBarAbs.getBtnSave().setEnabled(false);
		guiRunningBarAbs.getBtnOpen().setEnabled(false);
		guiRunningBarAbs.getProcessBar().setMinimum(0);
		guiRunningBarAbs.getProcessBar().setMaximum((int) (FileOperate.getFileSizeLong(bedFileName)/1000000));
		guiRunningBarAbs.getProcessBar().setValue(0);
		
		mapReads.setRunGetInfo(this);
		Thread thread = new Thread(mapReads);
		thread.start();
	}

}
