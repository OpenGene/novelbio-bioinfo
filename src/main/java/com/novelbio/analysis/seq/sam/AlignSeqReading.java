package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.nbcgui.GuiAnnoInfo;

/**
 * 输入一系列的AlignmentRecorder，然后读取指定的sambam文件
 * 这样一次读取完毕就可以做很多事情
 * @author zong0jie
 *
 */
public class AlignSeqReading extends RunProcess<GuiAnnoInfo>{

	List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<AlignmentRecorder>();
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
	
	/** 
	 * 如果读取一系列的文件，安顺序读取需要在进度条显示读取的内容，就把上一个文件的信息设定进去
	 * @param readLines
	 * @param readByte
	 */
	public void setReadInfo(Long readLines, double readByte) {
		this.readLines = readLines;
		this.readByte = readByte;
	}

	/**
	 * 设定记录器，也就是记录该sam文件Record的信息
	 * @param lsAlignmentRecorders
	 */
	public void setLsAlignmentRecorders(ArrayList<AlignmentRecorder> lsAlignmentRecorders) {
		this.lsAlignmentRecorders = lsAlignmentRecorders;
	}
	/**
	 * 添加记录器，也就是记录该sam文件Record的信息
	 * @param alignmentRecorder
	 */
	public void addAlignmentRecorder(AlignmentRecorder alignmentRecorder) {
		lsAlignmentRecorders.add(alignmentRecorder);
	}
	/**
	 * 添加记录器，也就是记录该sam文件Record的信息
	 * @param colAlignmentRecorders
	 */
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
	
	protected void reading() {
		readAllLines();
		summaryRecorder();
		alignSeqFile.close();
	}
	protected void readAllLines() {
		for (AlignRecord samRecord : alignSeqFile.readLines()) {
			suspendCheck();
			if (suspendFlag) {
				break;
			}
			addOneSeq(samRecord);
		}
	}
	
	protected void addOneSeq(AlignRecord samRecord) {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			if (alignmentRecorder == null) {
				continue;
			}
			alignmentRecorder.addAlignRecord(samRecord);
		}
		readLines++;
		if (readLines%50000 == 0) {
			GuiAnnoInfo guiAnnoInfo = new GuiAnnoInfo();
			guiAnnoInfo.setNum(readLines);
			guiAnnoInfo.setDouble(readByte + alignSeqFile.getReadByte());
			guiAnnoInfo.setInfo("File: " + FileOperate.getFileName(alignSeqFile.getFileName()) + "  reading: " + readLines + " lines");
			setRunInfo(guiAnnoInfo);
		}
		samRecord = null;
	}
	
	protected void summaryRecorder() {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			alignmentRecorder.summary();
		}
	}
}
