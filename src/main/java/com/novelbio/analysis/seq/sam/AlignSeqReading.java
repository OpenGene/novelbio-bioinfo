package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.nbcgui.GUI.GuiAnnoInfo;

/**
 * 输入一系列的AlignmentRecorder，然后读取指定的sambam文件
 * 这样一次读取完毕就可以做很多事情
 * @author zong0jie
 *
 */
public class AlignSeqReading extends RunProcess<GuiAnnoInfo>{

	ArrayList<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<AlignmentRecorder>();
	AlignSeq alignSeqFile;
	long readLines;
	double readByte;
	
	public AlignSeqReading(AlignSeq samFile) {
		this.alignSeqFile = samFile;
		readLines = 0;
		readByte = 0;
	}
	public double getReadByte() {
		return readByte;
	}
	public long getReadLines() {
		return readLines;
	}
	/** 如果读取一系列的文件，安顺序读取需要在进度条显示读取的内容，就把上一个文件的信息设定进去
	 * 
	 * @param readByte
	 */
	public void setReadInfo(Long readLines, double readByte) {
		this.readLines = readLines;
		this.readByte = readByte;
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
	/** 清空AlignmentRecorder和readByte和readLines，但不清除samFile */
	public void clear() {
		lsAlignmentRecorders.clear();
		readByte = 0;
		readLines = 0;
	}
	
	public AlignSeq getSamFile() {
		return alignSeqFile;
	}
	
	@Override
	protected void running() {
		reading();
	}
	
	public void reading() {
		readAllLines();
		summaryRecorder();
	}
	protected void readAllLines() {
		GuiAnnoInfo guiAnnoInfo;
		for (AlignRecord samRecord : alignSeqFile.readLines()) {
			for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
				alignmentRecorder.addAlignRecord(samRecord);
			}
			suspendCheck();
			if (suspendFlag) {
				break;
			}
			readByte += samRecord.toString().getBytes().length;
			readLines++;
			if (readLines%50000 == 0) {
				guiAnnoInfo = new GuiAnnoInfo();
				guiAnnoInfo.setNum(readLines);
				guiAnnoInfo.setDouble(readByte);
				guiAnnoInfo.setInfo("reading " + readLines + " lines");
				setRunInfo(guiAnnoInfo);
			}
			samRecord = null;
		}
	}
	
	protected void summaryRecorder() {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			alignmentRecorder.summary();
		}
	}
}
