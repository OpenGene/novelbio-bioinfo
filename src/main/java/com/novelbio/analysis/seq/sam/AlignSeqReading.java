package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.novelbio.GuiAnnoInfo;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;

/**
 * 输入一系列的AlignmentRecorder，然后读取指定的sambam文件
 * 这样一次读取完毕就可以做很多事情
 * @author zong0jie
 *
 */
public class AlignSeqReading extends RunProcess<GuiAnnoInfo>{

	List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<>();
	/** 正在用的recorder */
	Set<AlignmentRecorder> setRecorderRun = new LinkedHashSet<>(); 
	Map<String, Queue<AlignmentRecorder>>  mapChrID2RecorderTodo = new HashMap<>();
	
	List<AlignSeq> lsAlignSeqs = new ArrayList<>();
	long readLines;
	double readByte;
	int lenMin = -1;
	int lenMax = -1;
	
	public AlignSeqReading() {}
	
	public AlignSeqReading(AlignSeq alignSeq) {
		lsAlignSeqs.add(alignSeq);
		readLines = 0;
		readByte = 0;
	}
	
	public void setLenMin(int lenMin) {
		this.lenMin = lenMin;
	}
	public void setLenMax(int lenMax) {
		this.lenMax = lenMax;
	}
	
	public void addSeq(AlignSeq alignSeq) {
		lsAlignSeqs.add(alignSeq);
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
	public void setLsAlignmentRecorders(Collection<? extends AlignmentRecorder> lsAlignmentRecorders) {
		this.lsAlignmentRecorders = new ArrayList<>(lsAlignmentRecorders);
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
	public void addColAlignmentRecorder(Collection<? extends AlignmentRecorder> colAlignmentRecorders) {
		lsAlignmentRecorders.addAll(colAlignmentRecorders);
	}
	
	/** 清空AlignmentRecorder */
	public void clearRecorder() {
		lsAlignmentRecorders.clear();
		lsAlignmentRecorders = new ArrayList<>();
	}
	
	/** 返回第一个SamFile */
	public AlignSeq getFirstSamFile() {
		if (lsAlignSeqs == null || lsAlignSeqs.size() == 0) {
			return null;
		}
		return lsAlignSeqs.get(0);
	}
	
	@Override
	public void running() {
		sortRecorders();
		InitialRecorders();
		reading();
	}
	
	protected void reading() {
		readAllLines();
		summaryRecorder();
		for (AlignSeq alignSeqFile : lsAlignSeqs) {
			alignSeqFile.close();
		}
	}
	protected void readAllLines() {
		long num = 0;
		for (AlignSeq alignSeqFile : lsAlignSeqs) {
			for (AlignRecord samRecord : alignSeqFile.readLines()) {
				suspendCheck();
				if (suspendFlag) {
					break;
				}
				num++;
				if (num % 100000 == 0) {
					System.out.println(num);
				}
				int seqLen = samRecord.getLength();
				if (lenMin > 0 && seqLen < lenMin) {
					continue;
				}
				if (lenMax > 0 && seqLen > lenMax) {
					continue;
				}
				addOneSeq(samRecord, alignSeqFile);
			}
		}
	}
	
	protected void addOneSeq(AlignRecord samRecord, AlignSeq alignSeqFile) {
		removeRecord(samRecord);
		addTodoRecord_2_RunList(samRecord);
		
		for (AlignmentRecorder alignmentRecorder : setRecorderRun) {
			if (alignmentRecorder == null) {
				continue;
			} else if (alignmentRecorder.getReadingRegion() != null) {
				Align align = alignmentRecorder.getReadingRegion();
				if (align.getStartAbs() > samRecord.getEndAbs() || align.getEndAbs() < samRecord.getStartAbs()) {
					continue;
				}
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
	
	protected void sortRecorders() {
		Collections.sort(lsAlignmentRecorders, new Comparator<AlignmentRecorder>() {
			@Override
			public int compare(AlignmentRecorder o1, AlignmentRecorder o2) {
				if (o1.getReadingRegion() == null && o2.getReadingRegion() == null) {
					return 0;
				} else if (o1.getReadingRegion() == null) {
					return -1;
				} else if (o2.getReadingRegion() == null) {
					return 1;
				} else {
					Integer loc1 = o1.getReadingRegion().getStartAbs();
					Integer loc2 = o2.getReadingRegion().getStartAbs();
					return loc1.compareTo(loc2);
				}
			}
		});
	}
	
	protected void InitialRecorders() {
		setRecorderRun.clear();
		mapChrID2RecorderTodo.clear();
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			if (alignmentRecorder.getReadingRegion() == null) {
				setRecorderRun.add(alignmentRecorder);
			} else {
				Queue<AlignmentRecorder> queueRecorder = null;
				String chrID = alignmentRecorder.getReadingRegion().getRefID().toLowerCase();
				if (mapChrID2RecorderTodo.containsKey(chrID)) {
					queueRecorder = mapChrID2RecorderTodo.get(chrID);
				} else {
					queueRecorder = new LinkedList<>();
					mapChrID2RecorderTodo.put(chrID, queueRecorder);
				}
				queueRecorder.add(alignmentRecorder);
			}
		}
	}
	
	/** 删除执行队列中过时的Recorder */
	private void removeRecord(AlignRecord alignRecord) {
		List<AlignmentRecorder> lsRecorderTobeRemove = new ArrayList<>();
		for (AlignmentRecorder alignmentRecorder : setRecorderRun) {
			if (alignmentRecorder.getReadingRegion() == null) continue;
			if (!alignmentRecorder.getReadingRegion().getRefID().toLowerCase().equals(alignRecord.getRefID().toLowerCase()) ||
					alignmentRecorder.getReadingRegion().getEndAbs() < alignRecord.getStartAbs()) {
				lsRecorderTobeRemove.add(alignmentRecorder);
			}
		}
		if (lsRecorderTobeRemove.size() > 0) {
			for (AlignmentRecorder alignmentRecorder : lsRecorderTobeRemove) {
				setRecorderRun.remove(alignmentRecorder);
			}
		}
	}
	
	/** 把todo队列中的record放到执行队列中 */
	private void addTodoRecord_2_RunList(AlignRecord alignRecord) {
		while (true) {
			Queue<AlignmentRecorder> queueRecord = mapChrID2RecorderTodo.get(alignRecord.getRefID().toLowerCase());
			if (queueRecord == null || queueRecord.isEmpty()) {
				break;
			}
			AlignmentRecorder alignmentRecorder = queueRecord.peek();
			if (alignmentRecorder.getReadingRegion().getStartAbs() < alignRecord.getEndAbs()) {
				setRecorderRun.add(alignmentRecorder);
				queueRecord.poll();
			} else {
				break;
			}
		}
	}
	
	protected void summaryRecorder() {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			alignmentRecorder.summary();
		}
	}
	/** 清空除samfile以外的其他信息 */
	public void clearOther() {
		lsAlignmentRecorders.clear();
		setRecorderRun.clear();
		mapChrID2RecorderTodo.clear();
		readLines = 0;
		readByte = 0;
		lenMin = -1;
		lenMax = -1;
	}
	public void clear() {
		lsAlignmentRecorders.clear();
		setRecorderRun.clear();
		mapChrID2RecorderTodo.clear();
		lsAlignSeqs.clear();
		readLines = 0;
		readByte = 0;
		lenMin = -1;
		lenMax = -1;
	}
}
