package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.samtools.SAMFileHeader.SortOrder;

/** 将sam文件转化为bam文件
 * 其中添加multiHit的功能仅适用于bowtie
 *  */
public class SamToBamSort {
	SamFile samFileBam;//需要转化成的bam文件
	String outFileName;
	SamFile samFileSam;//输入的sam文件
	List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<>();
	
	/** 相同名字的序列 */
	List<SamRecord> lsSamRecordsWithSameName = new ArrayList<>();
	boolean addMultiHitFlag = false;
	boolean isPairend = false;
	
	/** 默认不排序 */
	boolean isNeedSort = false;
	
	/** 需要转化成的bam文件名，自动从sam文件判定是否为双端，会关闭Sam流
	 * @param outFileName
	 * @param samFileSam
	 */
	public SamToBamSort(String outFileName, SamFile samFileSam) {
		this.outFileName = outFileName;
		this.samFileSam = samFileSam;
		this.isPairend = samFileSam.isPairend();
	}
	/** 需要转化成的bam文件名 */
	public SamToBamSort(String outFileName, SamFile samFileSam, boolean isPairend) {
		this.outFileName = outFileName;
		this.samFileSam = samFileSam;
		this.isPairend = isPairend;
	}
	/** 是否需要排序，默认false */
	public void setNeedSort(boolean isNeedSort) {
		this.isNeedSort = isNeedSort;
	}
	/**
	 * 设定是否添加比对到多处的标签，暂时仅适用于bowtie2
	 * bwa不需要设定该参数
	 * @param addMultiHitFlag
	 */
	public void setAddMultiHitFlag(boolean addMultiHitFlag) {
		this.addMultiHitFlag = addMultiHitFlag;
	}
	
	public void setLsAlignmentRecorders(List<AlignmentRecorder> lsAlignmentRecorders) {
		if (lsAlignmentRecorders == null) return;
			
		this.lsAlignmentRecorders = lsAlignmentRecorders;
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			if (alignmentRecorder instanceof SamFileStatistics) {
				((SamFileStatistics)alignmentRecorder).setStandardData(samFileSam.getMapChrID2Length());
			}
		}
	}
	
	/**
	 * 转换结束后，关闭输出的bam文件，但是不关闭输入的sam文件
	 */
	public void convert() {
		setBamWriteFile();
		if (addMultiHitFlag) {
			convertAndAddMultiFlag();
		} else {
			convertNotAddMultiFlag();
		}
	}
	
	private void setBamWriteFile() {
		if (isNeedSort && samFileSam.getHeader().getSortOrder()== SortOrder.unsorted) {
			samFileBam = new SamFile(outFileName, samFileSam.getHeader(true), false);
		} else {
			samFileBam = new SamFile(outFileName, samFileSam.getHeader());
		}
	}
	
	private void convertNotAddMultiFlag() {
		for (SamRecord samRecord : samFileSam.readLines()) {
			for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
				try {
					alignmentRecorder.addAlignRecord(samRecord);
				} catch (Exception e) { }
			}
			samFileBam.writeSamRecord(samRecord);
		}
		finishConvert();
	}
	
	/** 进行转换 */
	private void convertAndAddMultiFlag() {
		Set<String> setTmp = new HashSet<>();
		for (SamRecord samRecord : samFileSam.readLines()) {
			if ((!isPairend && setTmp.size() == 0) || (isPairend && setTmp.size() < 2)
					) {
				setTmp.add(samRecord.getNameAndSeq());
			} else {
				String samNameAndSeq = samRecord.getNameAndSeq();
				if (!setTmp.contains(samNameAndSeq)) {
					addLsSamRecord(lsSamRecordsWithSameName);
					setTmp.clear();
					setTmp.add(samNameAndSeq);
					lsSamRecordsWithSameName.clear();
				}
			}
			lsSamRecordsWithSameName.add(samRecord);
		}
		addLsSamRecord(lsSamRecordsWithSameName);
		finishConvert();
	}
	
	/**
	 * @param lsSamRecords
	 * @param mapHitNum 小于0表示不需要调整flag
	 */
	private void addLsSamRecord(List<SamRecord> lsSamRecords) {
		int mapHitNum = lsSamRecords.size();
		if (isPairend) mapHitNum = mapHitNum/2;
		for (SamRecord samRecord : lsSamRecords) {
			samRecord.setMultiHitNum(mapHitNum);
			for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
				try {
					alignmentRecorder.addAlignRecord(samRecord);
				} catch (Exception e) { }
			}
			samFileBam.writeSamRecord(samRecord);
		}
	}
	
	private void finishConvert() {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			alignmentRecorder.summary();
		}
		samFileBam.close();
		samFileSam.setParamSamFile(samFileBam);
		samFileBam.bamFile = true;
	}
	
	/** 返回转换好的bam文件 */
	public SamFile getSamFileBam() {
		return samFileBam;
	}
	
}
