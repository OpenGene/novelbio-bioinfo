package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
/** 不要自己建立 */
public class SamFileStatistics {
	SamFile samFile;
	boolean countReadsNum;
	
	double allReadsNum = 0;
	double unmappedReadsNum = 0;
	double mappedReadsNum = 0;
	double uniqMappedReadsNum = 0;
	double repeatMappedReadsNum = 0;
	double junctionUniReads = 0;
	double junctionAllReads = 0;
	
	protected SamFileStatistics() { }
	
	protected void setSamFile(SamFile samFile) {
		this.samFile = samFile;
		countReadsNum = false;
	}
	/**
	 * 返回readsNum
	 * @param mappingType MAPPING_ALLREADS等
	 * @return -1表示错误
	 */
	public long getReadsNum(MappingReadsType mappingType) {
		statistics();
		if (mappingType == MappingReadsType.allReads) {
			return (long)allReadsNum;
		}
		if (mappingType == MappingReadsType.allMappedReads) {
			return (long)mappedReadsNum;
		}
		if (mappingType == MappingReadsType.unMapped) {
			return (long)unmappedReadsNum;
		}
		
		if (mappingType == MappingReadsType.uniqueMapping) {
			return (long)uniqMappedReadsNum;
		}

		if (mappingType == MappingReadsType.repeatMapping) {
			return (long)repeatMappedReadsNum;
		}
		
		if (mappingType == MappingReadsType.junctionUniqueMapping) {
			return (long)junctionUniReads;
		}
		if (mappingType == MappingReadsType.junctionAllMappedReads) {
			return (long)junctionAllReads;
		}
		return -1;
	}
	public ArrayList<String[]> getMappingInfo() {
		statistics();
		
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{"allReadsNum", (long)allReadsNum+""});
		lsResult.add(new String[]{"mappedReadsNum", (long)mappedReadsNum+""});
		lsResult.add(new String[]{"uniqMappedReadsNum", (long)uniqMappedReadsNum+""});
		lsResult.add(new String[]{"repeatMappedReadsNum", (long)repeatMappedReadsNum+""});
		lsResult.add(new String[]{"junctionAllReads", (long)junctionAllReads+""});
		lsResult.add(new String[]{"junctionUniReads", (long)junctionUniReads+""});
		lsResult.add(new String[]{"unmappedReadsNum", (long)unmappedReadsNum+""});

		return lsResult;
	}
	
	protected void statistics() {
		if (countReadsNum) {
			return;
		}
		countReadsNum = true;
		getReadsNum();
	}

	private void getReadsNum() {
		allReadsNum = 0;
		unmappedReadsNum = 0;
		mappedReadsNum = 0;
		uniqMappedReadsNum = 0;
		repeatMappedReadsNum = 0;
		junctionAllReads = 0;
		junctionUniReads = 0;
		
		for (SamRecord samRecord : samFile.readLines()) {
			int readsMappedNum = samRecord.getNumMappedReadsInFile();
			allReadsNum = allReadsNum + (double)1/readsMappedNum;
			if (samRecord.isMapped()) {
				mappedReadsNum = mappedReadsNum + (double)1/readsMappedNum;
				if (samRecord.isUniqueMapping()) {
					uniqMappedReadsNum ++;
					if (samRecord.isJunctionReads()) {
						junctionUniReads ++;
					}
				}
				else {
					repeatMappedReadsNum = repeatMappedReadsNum + (double)1/readsMappedNum;
				}
				if (samRecord.isJunctionReads()) {
					junctionAllReads = junctionAllReads + (double)1/readsMappedNum;
				}
			}
			else {
				unmappedReadsNum = unmappedReadsNum + (double)1/readsMappedNum;
			}
		}
		samFile.close();
	}
}
