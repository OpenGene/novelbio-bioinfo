package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.base.dataStructure.Alignment;

/**
 * 这里面可以设定一系列的监听器，然后完成一次mapping，统计多个信息的目的
 * 读完后内部自动关闭文件
 * @author zong0jie
 */
public class AlignSamReading extends AlignSeqReading {
	private static final Logger logger = Logger.getLogger(AlignSamReading.class);
	
	List<? extends Alignment> lsAlignments;

	public AlignSamReading(SamFile samFile) {
		super(samFile);
	}
	
	/**
	 * 输入需要读取的区域，不用排序<br>
	 * 如果输入的是null，则清空<br>
	 * 如果输入的是size为0的list，则不改变原有设定
	 * @param lsAlignments 输入Align的chrID无所谓大小写
	 * 输入的alignment务必不要首尾overlap
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
	
	protected void reading() {
		if (lsAlignments == null || lsAlignments.size() == 0) {
			readAllLines();
		} else {
			try {
				readSelectLines();
			} catch (Exception e) {
				// TODO: handle exception
			}
		
		}
		summaryRecorder();
		for (AlignSeq alignSeqFile : lsAlignSeqs) {
			alignSeqFile.close();
		}
	}
	
	public SamFile getFirstSamFile() {
		AlignSeq alignSeq = super.getFirstSamFile();
		if (alignSeq != null) {
			return (SamFile) alignSeq;
		}
		return null;
	}
	
	private void readSelectLines() {
		Collections.sort(lsAlignments, new Comparator<Alignment>() {
			@Override
			public int compare(Alignment o1, Alignment o2) {
				Integer loc1 = o1.getStartAbs();
				Integer loc2 = o2.getStartAbs();
				return loc1.compareTo(loc2);
			}
		});
		
		long num = 0;
		
		for (Alignment alignment : lsAlignments) {
			for (AlignSeq alignSeq : lsAlignSeqs) {
				SamFile samFile = (SamFile)alignSeq;
				//TODO
				for (AlignRecord samRecord : samFile.readLinesOverlap(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs())) {
					num++;
					if (num % 100000 == 0) {
						logger.info("read reads num: " + num);
					}
					suspendCheck();
					if (suspendFlag) {
						break;
					}
					addOneSeq(samRecord, alignSeq);
				}
			}
		
		}
		System.out.println();
	}
	
	/** 清空AlignmentRecorder和readByte和readLines，但不清除samFile */
	public void clear() {
		super.clear();
		try {
			lsAlignments = new ArrayList<>();
		} catch (Exception e) { }
	}
}
