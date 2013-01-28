package com.novelbio.analysis.seq.sam;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.nbcgui.GUI.GuiAnnoInfo;

public class AlignSamReading extends AlignSeqReading {
	List<? extends Alignment> lsAlignments;

	public AlignSamReading(SamFile samFile) {
		super(samFile);
	}
	
	/**
	 * 输入需要读取的区域，不用排序
	 * @param lsAlignments
	 */
	public void setLsAlignments(List<? extends Alignment> lsAlignments) {
		if (lsAlignments == null || lsAlignments.size() == 0) {
			return;
		}
		this.lsAlignments = lsAlignments;

		Collections.sort(lsAlignments, new Comparator<Alignment>() {
			public int compare(Alignment o1, Alignment o2) {
				int compare = 0;
				compare = o1.getRefID().compareTo(o2.getRefID());
				if (compare == 0) {
					Integer o1int = o1.getStartAbs();
					Integer o2int = o2.getStartAbs();
					compare = o1int.compareTo(o2int);
				}
				return compare;
			}
		});
	}
	
	public void reading() {
		if (lsAlignments == null || lsAlignments.size() == 0) {
			readAllLines();
		} else {
			readSelectLines();
		}
		summaryRecorder();
	}
	
	public SamFile getSamFile() {
		return (SamFile)alignSeqFile;
	}
	
	private void readSelectLines() {
		SamFile samFile = (SamFile)super.alignSeqFile;
		GuiAnnoInfo guiAnnoInfo;
		long readLines = 0;
		for (Alignment alignment : lsAlignments) {
			for (AlignRecord samRecord : samFile.readLinesOverlap(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs())) {
				for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
					alignmentRecorder.addAlignRecord(samRecord);
				}
				suspendCheck();
				if (suspendFlag) {
					break;
				}
				readLines++;
				guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setNum(readLines);
				setRunInfo(guiAnnoInfo);
				samRecord = null;
			}
		}
	}
	/** 清空AlignmentRecorder和readByte和readLines，但不清除samFile */
	public void clear() {
		super.clear();
		try {
			lsAlignments.clear();
		} catch (Exception e) { }
	}
}
