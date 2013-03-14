package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrStatistics;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GUI.GuiAnnoInfo;
import com.novelbio.nbcgui.GUI.GuiSamStatistics;

public class CtrlSamRPKMLocate implements RunGetInfo<GuiAnnoInfo>, Runnable {
	GuiSamStatistics guiSamStatistics;
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	List<String[]> lsReadFile;
	boolean isCountRPKM = true;
	boolean isLocStatistics = true;
	
	Set<String> setPrefix;
	Map<String, GffChrStatistics> mapPrefix2LocStatistics;
	RPKMcomput rpkMcomput;
	
	int[] tss;
	int[] tes;
	
	String resultPrefix;

	
	/**
	 * 必须设定{@link #setFormatSeq(FormatSeq)} 方法
	 * 和{@link #setResultPrefix(String)} 方法
	 * @param guiPeakStatistics
	 */
	public CtrlSamRPKMLocate(GuiSamStatistics guiPeakStatistics) {
		this.guiSamStatistics = guiPeakStatistics;
	}
	public void setSpecies(Species species) {
		this.gffChrAbs.setSpecies(species);
	}
	public void setGffHash(GffHashGene gffHashGene) {
		this.gffChrAbs.setGffHash(gffHashGene);
	}
	public void setGffHash(GffHashGeneAbs gffHashGene) {
		this.gffChrAbs.setGffHash(gffHashGene);
	}
	
	public void setQueryFile(List<String[]> lsReadFile) {
		this.lsReadFile = lsReadFile;
	}
	public void setIsCountRPKM(boolean isCountRPKM) {
		this.isCountRPKM = isCountRPKM;
	}
	public void setTssRange(int[] tss) {
		this.tss = tss;
	}
	public void setTesRange(int[] tes) {
		this.tes = tes;
	}
	/** 设定输出文件路径前缀 */
	public void setResultPrefix(String resultPrefix) {
		this.resultPrefix = resultPrefix;
	}
	public void run() {
		int fileSize = getFileSize();		
		guiSamStatistics.getProcessBar().setMaximum(fileSize);
		
		ArrayListMultimap<String, AlignSeqReading> mapPrefix2AlignSeqReadings = getMapPrefix2LsAlignSeqReadings();
		if (!isCountRPKM && !isLocStatistics) {
			return;
		}
		double readByte = 0;
		for (String prefix : mapPrefix2AlignSeqReadings.keySet()) {
			List<AlignSeqReading> lsAlignSeqReadings = mapPrefix2AlignSeqReadings.get(prefix);
			List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<AlignmentRecorder>();
			
			if (isCountRPKM) {
				rpkMcomput.setCurrentCondition(prefix);
				lsAlignmentRecorders.add(rpkMcomput);
			}
			
			if (isLocStatistics) {
				GffChrStatistics gffChrStatistics = new GffChrStatistics();
				gffChrStatistics.setGffChrAbs(gffChrAbs);
				gffChrStatistics.setTesRegion(tes);
				gffChrStatistics.setTssRegion(tss);
				lsAlignmentRecorders.add(gffChrStatistics);
				mapPrefix2LocStatistics.put(prefix, gffChrStatistics);
			}
			
			for (AlignSeqReading alignSeqReading : lsAlignSeqReadings) {
				alignSeqReading.setReadInfo(0L, readByte);
				alignSeqReading.addColAlignmentRecorder(lsAlignmentRecorders);
				alignSeqReading.setRunGetInfo(this);
				alignSeqReading.reading();
				readByte = alignSeqReading.getReadByte();
			}
		}
		
		writeToFile();
		done(null);
		
	}
	
	private int getFileSize() {
		int fileSizeLong = 0;
		for (String[] fileName : lsReadFile) {
			long thisFileSize = (long) FileOperate.getFileSize(fileName[0]);
			if (fileName[0].endsWith("bam") || fileName[0].endsWith("gz")) {
				thisFileSize = thisFileSize * 8;
			}
			fileSizeLong += thisFileSize;
		}
		return fileSizeLong;
	}
	
	/**
	 * 本步会初始化mapPrefix2LocStatistics和rpkMcomput
	 * @return
	 */
	private ArrayListMultimap<String, AlignSeqReading> getMapPrefix2LsAlignSeqReadings() {
		if (isLocStatistics) {
			mapPrefix2LocStatistics = new HashMap<String, GffChrStatistics>();
		}
		if (isCountRPKM) {
			rpkMcomput = new RPKMcomput();
			rpkMcomput.setGffChrAbs(gffChrAbs);
		}
		
		setPrefix = new LinkedHashSet<String>();
		
		ArrayListMultimap<String, AlignSeqReading> mapPrefix2AlignSeqReadings = ArrayListMultimap.create();
		for (String[] fileName2Prefix : lsReadFile) {
			setPrefix.add(fileName2Prefix[1]);
			FormatSeq formatSeq = getFileFormat(fileName2Prefix[0]);

			AlignSeq alignSeq = null;
			if (formatSeq == FormatSeq.SAM || formatSeq == FormatSeq.BAM) {
				alignSeq = new SamFile(fileName2Prefix[0]);
			} else if (formatSeq == FormatSeq.BED) {
				alignSeq = new BedSeq(fileName2Prefix[0]);
			} else {
				continue;
			}
			AlignSeqReading alignSeqReading = new AlignSeqReading(alignSeq);
			
			mapPrefix2AlignSeqReadings.put(fileName2Prefix[1], alignSeqReading);
		}
		return mapPrefix2AlignSeqReadings;
	}
	
	private FormatSeq getFileFormat(String fileName) {
		 return FormatSeq.getFileType(fileName);
	}
	
	private void writeToFile() {
		if (isCountRPKM) {
			String outRPM = FileOperate.changeFileSuffix(resultPrefix, "_rpm", "txt");
			String outRPKM = FileOperate.changeFileSuffix(resultPrefix, "_rpkm", "txt");
			
			List<String[]> lsRpm = rpkMcomput.getLsRPMs();
			List<String[]> lsRpkm = rpkMcomput.getLsRPKMs();
			TxtReadandWrite txtWriteRpm = new TxtReadandWrite(outRPM, true);
			txtWriteRpm.ExcelWrite(lsRpm);
			TxtReadandWrite txtWriteRpkm = new TxtReadandWrite(outRPKM, true);
			txtWriteRpkm.ExcelWrite(lsRpkm);
		}
		if (isLocStatistics) {
			for (String prefix : setPrefix) {
				GffChrStatistics gffChrStatistics = mapPrefix2LocStatistics.get(prefix);
				String outStatistics = FileOperate.changeFileSuffix(resultPrefix, "_" + prefix + "_GeneStructure", "txt");
				TxtReadandWrite txtWrite = new TxtReadandWrite(outStatistics, true);
				txtWrite.ExcelWrite(gffChrStatistics.getStatisticsResult());
			}
		}
		
	}
	@Override
	public void setRunningInfo(GuiAnnoInfo info) {
		guiSamStatistics.getProcessBar().setValue((int)( info.getNumDouble()/1024));
	}
	@Override
	public void done(RunProcess<GuiAnnoInfo> runProcess) {
		guiSamStatistics.getProcessBar().setValue(guiSamStatistics.getProcessBar().getMaximum());
		guiSamStatistics.getBtnSave().setEnabled(true);
		guiSamStatistics.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadSuspended(RunProcess<GuiAnnoInfo> runProcess) {
		guiSamStatistics.getBtnRun().setEnabled(true);
	}
	@Override
	public void threadResumed(RunProcess<GuiAnnoInfo> runProcess) {
		guiSamStatistics.getBtnRun().setEnabled(false);
	}
	@Override
	public void threadStop(RunProcess<GuiAnnoInfo> runProcess) {
		guiSamStatistics.getBtnRun().setEnabled(true);
	}
}
