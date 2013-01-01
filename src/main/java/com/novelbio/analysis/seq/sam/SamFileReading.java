package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;

import com.novelbio.base.multithread.RunProcess;

/**
 * 输入一系列的AlignmentRecorder，然后读取指定的sambam文件
 * 这样一次读取完毕就可以做很多事情
 * @author zong0jie
 *
 */
public class SamFileReading extends RunProcess<Double>{
	ArrayList<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<AlignmentRecorder>();
	SamFile samFile;
	
	public SamFileReading(SamFile samFile) {
		this.samFile = samFile;
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
	
	public SamFile getSamFile() {
		return samFile;
	}
	@Override
	protected void running() {
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
	
}
