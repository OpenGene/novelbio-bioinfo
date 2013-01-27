package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.SAMFileReader;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.genome.GffChrStatistics;
import com.novelbio.analysis.seq.genome.GffChrStatistics.GffChrStatiscticsProcessInfo;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiPeakStatistics;
import com.novelbio.nbcgui.GUI.GuiSamStatistics;

public class CtrlSamRPKMLocate implements RunGetInfo<GffChrStatistics.GffChrStatiscticsProcessInfo> {
	GuiSamStatistics guiSamStatistics;
	GffChrStatistics gffChrStatistics = new GffChrStatistics();
	RPKMcomput rpkMcomput = new RPKMcomput();
	FormatSeq formatSeq;

	
	Species species;
	
	List<String[]> lsReadFile;
	
	public CtrlSamRPKMLocate(GuiSamStatistics guiPeakStatistics) {
		this.guiSamStatistics = guiPeakStatistics;
		gffChrStatistics.setRunGetInfo(this);
	}
	public void setSpecies(Species species) {
		this.species = species;
	}
	public void setQueryFile(List<String[]> lsReadFile) {
		this.lsReadFile = lsReadFile;
	}
	public void setFormatSeq(FormatSeq formatSeq) {
		this.formatSeq = formatSeq;
	}

	public void setTssRange(int[] tss) {
		gffChrStatistics.setTssRegion(tss);
	}
	public void setTesRange(int[] tes) {
		gffChrStatistics.setTesRegion(tes);
	}

	public void execute() {
		long fileSizeLong = getFileSize();
		int fileSize = (int)(fileSizeLong/1000000);
		
		guiSamStatistics.getProcessBar().setMaximum(fileSize);
		
		List<AlignSeqReading> lsAlignSeqReadings = getLsAlignSeqReadings();
		for (AlignSeqReading alignSeqReading : lsAlignSeqReadings) {
			alignSeqReading.addAlignmentRecorder(gffChrStatistics);
			alignSeqReading.addAlignmentRecorder(rpkMcomput);
		}
		
		
		
		
		gffChrStatistics.clean();
		gffChrStatistics.setSpecies(species);
		gffChrStatistics.setFileName(readFile);
		
		Thread thread = new Thread(gffChrStatistics);
		thread.start();
	}
	
	private List<AlignSeqReading> getLsAlignSeqReadings() {
		ArrayList<AlignSeqReading> lsAlignSeqReadings = new ArrayList<AlignSeqReading>();
		for (String[] fileName : lsReadFile) {
			AlignSeq alignSeq;
			if (formatSeq == FormatSeq.SAM || formatSeq == formatSeq.BAM) {
				alignSeq = new SamFile(fileName[0]);
			} else if (formatSeq == FormatSeq.BED) {
				alignSeq = new BedSeq(fileName[0]);
			}
			AlignSeqReading alignSeqReading = new AlignSeqReading(alignSeq);
			lsAlignSeqReadings.add(alignSeqReading);
		}
		return lsAlignSeqReadings;
	}
	
	private long getFileSize() {
		long fileSizeLong = 0;
		for (String[] fileName : lsReadFile) {
			long thisFileSize = FileOperate.getFileSizeLong(fileName[0]);
			if (fileName[0].endsWith("bam") || fileName[0].endsWith("gz")) {
				thisFileSize = thisFileSize * 8;
			}
			fileSizeLong += thisFileSize;
		}
		return fileSizeLong;
	}
	
	public ArrayList<String[]> getResult() {
		return gffChrStatistics.getStatisticsResult();
	}
	@Override
	public void setRunningInfo(GffChrStatiscticsProcessInfo info) {
		guiSamStatistics.getProcessBar().setValue(info.getReadsize());
	}
	@Override
	public void done(RunProcess<GffChrStatiscticsProcessInfo> runProcess) {
		guiSamStatistics.getProcessBar().setValue(guiSamStatistics.getProcessBar().getMaximum());
		guiSamStatistics.getBtnSave().setEnabled(true);
		guiSamStatistics.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadSuspended(RunProcess<GffChrStatiscticsProcessInfo> runProcess) {
		guiSamStatistics.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadResumed(RunProcess<GffChrStatiscticsProcessInfo> runProcess) {
		guiSamStatistics.getBtnRun().setEnabled(false);
	}
	@Override
	public void threadStop(RunProcess<GffChrStatiscticsProcessInfo> runProcess) {
		guiSamStatistics.getBtnRun().setEnabled(true);
	}
}
