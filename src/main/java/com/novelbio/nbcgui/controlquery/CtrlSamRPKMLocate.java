package com.novelbio.nbcgui.controlquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrStatistics;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.nbcgui.GuiAnnoInfo;
import com.novelbio.nbcgui.GUI.GuiSamStatistics;

public class CtrlSamRPKMLocate implements RunGetInfo<GuiAnnoInfo>, Runnable {
	private static final Logger logger = Logger.getLogger(CtrlSamRPKMLocate.class);
	
	GuiSamStatistics guiSamStatistics;
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	List<String[]> lsReadFile;
	boolean isCountExpression = true;
	boolean isCalculateFPKM = true;
	/** 目前只对proton的strand优化 */
	boolean isConsiderProtonStrand = true;
	
	boolean isLocStatistics = true;
	
	Set<String> setPrefix;
	Map<String, GffChrStatistics> mapPrefix2LocStatistics;
	Map<String, SamFileStatistics> mapPrefix2Statistics;
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
	public void setIsCountRPKM(boolean isCountExpression, boolean isConsiderProtonStrand, boolean isCountFPKM) {
		this.isCountExpression = isCountExpression;
		this.isConsiderProtonStrand = isConsiderProtonStrand;
		this.isCalculateFPKM = isCountFPKM;
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
		rpkMcomput = new RPKMcomput();
		int fileSize = getFileSize();		
		guiSamStatistics.getProcessBar().setMaximum(fileSize);
		
		ArrayListMultimap<String, AlignSeqReading> mapPrefix2AlignSeqReadings = getMapPrefix2LsAlignSeqReadings();
		if (!isCountExpression && !isLocStatistics) {
			return;
		}
		double readByte = 0;
		for (String prefix : mapPrefix2AlignSeqReadings.keySet()) {
			List<AlignSeqReading> lsAlignSeqReadings = mapPrefix2AlignSeqReadings.get(prefix);
			List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<AlignmentRecorder>();
			
			if (isCountExpression && gffChrAbs.getTaxID() != 0) {
				rpkMcomput.setCurrentCondition(prefix);
				rpkMcomput.setConsiderStrand(isConsiderProtonStrand);
				rpkMcomput.setCalculateFPKM(isCalculateFPKM);
				lsAlignmentRecorders.add(rpkMcomput);
			}

			if (isLocStatistics) {
				if (gffChrAbs.getTaxID() != 0) {
					GffChrStatistics gffChrStatistics = new GffChrStatistics();
					gffChrStatistics.setGffChrAbs(gffChrAbs);
					gffChrStatistics.setTesRegion(tes);
					gffChrStatistics.setTssRegion(tss);
					lsAlignmentRecorders.add(gffChrStatistics);
					mapPrefix2LocStatistics.put(prefix, gffChrStatistics);
				}
				
				SamFileStatistics samFileStatistics = new SamFileStatistics();
				lsAlignmentRecorders.add(samFileStatistics);
				mapPrefix2Statistics.put(prefix, samFileStatistics);
			}
			
			for (AlignSeqReading alignSeqReading : lsAlignSeqReadings) {
				alignSeqReading.setReadInfo(0L, readByte);
				alignSeqReading.addColAlignmentRecorder(lsAlignmentRecorders);
				if (alignSeqReading.getSamFile() instanceof SamFile) {
					rpkMcomput.setIsPairend(((SamFile)alignSeqReading.getSamFile()).isPairend());
				}
				alignSeqReading.setRunGetInfo(this);
				alignSeqReading.run();
				logger.info("finish reading " + alignSeqReading.getSamFile().getFileName());
				readByte = alignSeqReading.getReadByte();
			}
			
			logger.info("finish reading " + prefix);
			try {
				writeToFileCurrent(prefix);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		try {
			writeToFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		done(null);
		guiSamStatistics.getProcessBar().setValue(guiSamStatistics.getProcessBar().getMaximum());
		guiSamStatistics.getBtnSave().setEnabled(true);
		guiSamStatistics.getBtnRun().setEnabled(true);
	}
	
	private int getFileSize() {
		long fileSizeLong = 0;
		for (String[] fileName : lsReadFile) {
			fileSizeLong += (long) FileOperate.getFileSizeLong(fileName[0]);
		}
		return (int)(fileSizeLong/1024);
	}
	
	/**
	 * 本步会初始化mapPrefix2LocStatistics和rpkMcomput
	 * @return
	 */
	private ArrayListMultimap<String, AlignSeqReading> getMapPrefix2LsAlignSeqReadings() {
		mapPrefix2LocStatistics = new HashMap<String, GffChrStatistics>();
		mapPrefix2Statistics = new HashMap<String, SamFileStatistics>();
		
		if (gffChrAbs.getTaxID() != 0) {
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
		if (isCountExpression && gffChrAbs.getTaxID() != 0) {
			String outTPM = FileOperate.changeFileSuffix(resultPrefix, "_tpm", "txt");
			String outRPKM = FileOperate.changeFileSuffix(resultPrefix, "_rpkm", "txt");
			String outCounts = FileOperate.changeFileSuffix(resultPrefix, "_Counts", "txt");
			
			List<String[]> lsTpm = rpkMcomput.getLsTPMs();
			List<String[]> lsRpkm = rpkMcomput.getLsRPKMs();
			List<String[]> lsCounts = rpkMcomput.getLsCounts();
			TxtReadandWrite txtWriteRpm = new TxtReadandWrite(outTPM, true);
			txtWriteRpm.ExcelWrite(lsTpm);
			TxtReadandWrite txtWriteRpkm = new TxtReadandWrite(outRPKM, true);
			txtWriteRpkm.ExcelWrite(lsRpkm);
			TxtReadandWrite txtWriteCounts = new TxtReadandWrite(outCounts, true);
			txtWriteCounts.ExcelWrite(lsCounts);
			txtWriteCounts.close();
			txtWriteRpkm.close();
			txtWriteRpm.close();
		}
		if (isLocStatistics) {
			for (String prefix : setPrefix) {
				if (gffChrAbs.getTaxID() != 0) {
					GffChrStatistics gffChrStatistics = mapPrefix2LocStatistics.get(prefix);
					String outStatistics = FileOperate.changeFileSuffix(resultPrefix, "_" + prefix + "_GeneStructure", "txt");
					TxtReadandWrite txtWrite = new TxtReadandWrite(outStatistics, true);
					txtWrite.ExcelWrite(gffChrStatistics.getStatisticsResult());
					txtWrite.close();
				}
				
				
				SamFileStatistics samFileStatistics = mapPrefix2Statistics.get(prefix);
				String outSamStatistics = FileOperate.changeFileSuffix(resultPrefix, "_" + prefix + "_MappingStatistics", "txt");
				TxtReadandWrite txtWriteStatistics = new TxtReadandWrite(outSamStatistics, true);
				txtWriteStatistics.ExcelWrite(samFileStatistics.getMappingInfo());
				txtWriteStatistics.close();
			}
		}
	}
	
	private void writeToFileCurrent(String prefix) {
		if (isCountExpression && gffChrAbs.getTaxID() != 0) {
			String outTPM = FileOperate.changeFileSuffix(resultPrefix, prefix + "_tpm", "txt");
			String outRPKM = FileOperate.changeFileSuffix(resultPrefix, prefix + "_rpkm", "txt");
			String outCounts = FileOperate.changeFileSuffix(resultPrefix, prefix + "_Counts", "txt");
			
			List<String[]> lsTpm = rpkMcomput.getLsTPMsCurrent();
			List<String[]> lsRpkm = rpkMcomput.getLsRPKMsCurrent();
			List<String[]> lsCounts = rpkMcomput.getLsCountsCurrent();
			TxtReadandWrite txtWriteRpm = new TxtReadandWrite(outTPM, true);
			txtWriteRpm.ExcelWrite(lsTpm);
			TxtReadandWrite txtWriteRpkm = new TxtReadandWrite(outRPKM, true);
			txtWriteRpkm.ExcelWrite(lsRpkm);
			TxtReadandWrite txtWriteCounts = new TxtReadandWrite(outCounts, true);
			txtWriteCounts.ExcelWrite(lsCounts);
			txtWriteCounts.close();
			txtWriteRpkm.close();
			txtWriteRpm.close();
		}
		if (isLocStatistics) {
			if (gffChrAbs.getTaxID() != 0) {
				GffChrStatistics gffChrStatistics = mapPrefix2LocStatistics.get(prefix);
				String outStatistics = FileOperate.changeFileSuffix(resultPrefix, "_" + prefix + "_GeneStructure", "txt");
				TxtReadandWrite txtWrite = new TxtReadandWrite(outStatistics, true);
				txtWrite.ExcelWrite(gffChrStatistics.getStatisticsResult());
				txtWrite.close();
			}
			SamFileStatistics samFileStatistics = mapPrefix2Statistics.get(prefix);
			String outSamStatistics = FileOperate.changeFileSuffix(resultPrefix, "_" + prefix + "_MappingStatistics", "txt");
			TxtReadandWrite txtWriteStatistics = new TxtReadandWrite(outSamStatistics, true);
			txtWriteStatistics.ExcelWrite(samFileStatistics.getMappingInfo());
			txtWriteStatistics.close();
		}
	}
	
	@Override
	public void setRunningInfo(GuiAnnoInfo info) {
		guiSamStatistics.getProcessBar().setValue((int)( info.getNumDouble()/1024));
		guiSamStatistics.getLabel().setText(info.getInfo());
	}
	
	@Override
	public void done(RunProcess<GuiAnnoInfo> runProcess) {
		//只是单个文本读取完毕，不需要做什么事情
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
