package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.base.multithread.RunProcess;

/**
 * 输入一系列的AlignmentRecorder，然后读取指定的sambam文件
 * 这样一次读取完毕就可以做很多事情
 * @author zong0jie
 *
 */
public class SamFileReading extends RunProcess<Double>{
	List<? extends Alignment> lsAlignments;
	ArrayList<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<AlignmentRecorder>();
	SamFile samFile;
	
	public SamFileReading(SamFile samFile) {
		this.samFile = samFile;
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
	public void setLsAlignmentRecorders(ArrayList<AlignmentRecorder> lsAlignmentRecorders) {
		this.lsAlignmentRecorders = lsAlignmentRecorders;
	}
	public void addAlignmentRecorder(AlignmentRecorder alignmentRecorder) {
		lsAlignmentRecorders.add(alignmentRecorder);
	}
	public void addColAlignmentRecorder(Collection<AlignmentRecorder> colAlignmentRecorders) {
		lsAlignmentRecorders.addAll(colAlignmentRecorders);
	}
	/** 清空，但不清除samFile */
	public void clear() {
		lsAlignmentRecorders.clear();
		try {
			lsAlignments.clear();
		} catch (Exception e) { }
	}
	
	public SamFile getSamFile() {
		return samFile;
	}
	@Override
	protected void running() {
		if (lsAlignments == null || lsAlignments.size() == 0) {
			readAllLines();
		} else {
			readSelectLines();
		}
		summaryRecorder();
	}
	
	private void readAllLines() {
		double readByte = 0;
		for (SamRecord samRecord : samFile.readLines()) {
			for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
				alignmentRecorder.addAlignRecord(samRecord);
			}
			suspendCheck();
			if (suspendFlag) {
				break;
			}
			readByte = readByte + samRecord.toString().getBytes().length;
			setRunInfo(readByte);
			samRecord = null;
		}
	}
	private void readSelectLines() {
		double readByte = 0;
		for (Alignment alignment : lsAlignments) {
			for (SamRecord samRecord : samFile.readLinesOverlap(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs())) {
				for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
					alignmentRecorder.addAlignRecord(samRecord);
				}
				suspendCheck();
				if (suspendFlag) {
					break;
				}
				readByte = readByte + samRecord.toString().getBytes().length;
				setRunInfo(readByte);
				samRecord = null;
			}
		}
	}
	
	private void summaryRecorder() {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			alignmentRecorder.summary();
		}
	}
}
