package com.novelbio.analysis.seq.sam;

import java.util.LinkedHashMap;

import net.sf.samtools.SAMFileHeader;

import com.novelbio.analysis.seq.AlignRecord;

/**
 * <b>仅能用于SamBam文件</b>
 * 把一端mapping上，另一端没mapping上的文件写入一个sam文件
 * @author zong0jie
 *
 */
public class SamOnePairMapReads2Sam implements AlignmentRecorder {
	LinkedHashMap<String, SamRecord> mapName2Record = new LinkedHashMap<String, SamRecord>();
	
	SamFile samFileWrite;
	
	public SamOnePairMapReads2Sam(String outFileName, SAMFileHeader samFileHeader) {
		this.samFileWrite = new SamFile(outFileName, samFileHeader);
		mapName2Record.clear();
	}
	
	/**
	 * @param outFileName 输出文件
	 * @param samFileRead 从输入文件中读取samHeader信息
	 */
	public SamOnePairMapReads2Sam(String outFileName, SamFile samFileRead) {
		this.samFileWrite = new SamFile(outFileName, samFileRead.getHeader());
		mapName2Record.clear();
	}
	
	public void addAlignRecord(AlignRecord alignRecord) {
		if (alignRecord instanceof SamRecord != true) {
			return;
		}
		SamRecord samRecord = (SamRecord)alignRecord;
		if (!samRecord.isHavePairEnd()) {
			return;
		}
		//将一对samRecord写入文件
		if (mapName2Record.containsKey(samRecord.getName())) {
			SamRecord samRecord1 = mapName2Record.get(samRecord.getName());
			if (samRecord1.isPaireReads(samRecord)) {
				samFileWrite.writeSamRecord(samRecord1);
				samFileWrite.writeSamRecord(samRecord);
				mapName2Record.remove(samRecord.getName());
				return;
			}
		}
		//找出一个mapping一个没有mapping的记录
		if (samRecord.isMapped() ^ samRecord.isMateMapped() ) {
			mapName2Record.put(samRecord.getName(), samRecord);
		}
		removeMap(5000, mapName2Record);
	}

	/** 将多的序列删除，以节约内存 */
	private void removeMap(int remainNum, LinkedHashMap<String, SamRecord> mapName2Record) {
		int size = mapName2Record.size();
		if (size <= remainNum) {
			return;
		}
		int num = size - remainNum;
		int count = 0;
		//将开头的几个record删除
		for (String recordName : mapName2Record.keySet()) {
			if (count > num) {
				break;
			}
			mapName2Record.remove(recordName);
			count++;
		}
	}

	@Override
	public void summary() {}
	
	/**
	 * 输出的samFile没有调用{@link SamFile#close()}
	 * @return
	 */
	public SamFile getSamFileWrite() {
		return samFileWrite;
	}

}
