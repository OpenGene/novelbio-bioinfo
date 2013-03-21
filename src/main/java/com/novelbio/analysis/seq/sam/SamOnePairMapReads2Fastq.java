package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.sf.samtools.SAMFileHeader;

import com.novelbio.analysis.seq.AlignRecord;

public class SamOnePairMapReads2Fastq implements AlignmentRecorder {
	LinkedHashMap<String, SamRecord> mapName2Record = new LinkedHashMap<String, SamRecord>();
	
	public void addAlignRecord(SamRecord samRecord) {
		if (!samRecord.isHavePairEnd()) {
			continue;
		}
		//将一对samRecord写入文件
		if (mapName2Record.containsKey(samRecord.getName())) {
			SamRecord samRecord1 = mapName2Record.get(samRecord.getName());
			if (samRecord1.isPaireReads(samRecord)) {
				samFile.writeSamRecord(samRecord1);
				samFile.writeSamRecord(samRecord);
				mapName2Record.remove(samRecord.getName());
				continue;
			}
		}
		//找出一个mapping一个没有mapping的记录
		if (samRecord.isMapped() ^ samRecord.isMateMapped() ) {
			mapName2Record.put(samRecord.getName(), samRecord);
		}
		removeMap(5000, mapName2Record);
	
	}

	@Override
	public void summary() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 将那种一头mapping上，一头没有mapping上的序列，两头都提取出来写入一个sam文件
	 */
	public SamFile getSingleUnMappedReads(String outSamFile) {
		for (SamRecord samRecord : samReader.readLines()) {
			if (!samRecord.isHavePairEnd()) {
				continue;
			}
			//将一对samRecord写入文件
			if (mapName2Record.containsKey(samRecord.getName())) {
				SamRecord samRecord1 = mapName2Record.get(samRecord.getName());
				if (samRecord1.isPaireReads(samRecord)) {
					samFile.writeSamRecord(samRecord1);
					samFile.writeSamRecord(samRecord);
					mapName2Record.remove(samRecord.getName());
					continue;
				}
			}
			//找出一个mapping一个没有mapping的记录
			if (samRecord.isMapped() ^ samRecord.isMateMapped() ) {
				mapName2Record.put(samRecord.getName(), samRecord);
			}
			removeMap(5000, mapName2Record);
		}
		samFile.close();
		return samFile;
	}
	/** 将多的序列删除，以节约内存 */
	private void removeMap(int remainNum, LinkedHashMap<String, SamRecord> mapName2Record) {
		if (mapName2Record.size() <= remainNum) {
			return;
		}
		int num = mapName2Record.size() - remainNum;
		int count = 0;
		ArrayList<String> lsName = new ArrayList<String>();
		for (String recordName : mapName2Record.keySet()) {
			if (count > num) {
				break;
			}
			lsName.add(recordName);
			count++;
		}
		for (String recordName : lsName) {
			mapName2Record.remove(recordName);
		}
	}
	
}
