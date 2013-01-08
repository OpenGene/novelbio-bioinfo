package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.mapping.MappingReadsType;

/** 不要自己建立 */
public class SamFileStatistics {
	public static void main(String[] args) {
		SamFile samFile = new SamFile("/home/zong0jie/Desktop/sssFH.sam");
		SamFileStatistics samFileStatistics = new SamFileStatistics();
		samFileStatistics.setSamFile(samFile);
		ArrayList<String[]> lsResult = samFileStatistics.getMappingInfo();
		System.out.println("stop");
		for (String[] strings : lsResult) {
			System.out.println(strings[0] + "\t" + strings[1]);
		}
	}
	
	SamFile samFile;
	boolean countReadsNum;
	
	double allReadsNum = 0;
	double unmappedReadsNum = 0;
	double mappedReadsNum = 0;
	double uniqMappedReadsNum = 0;
	double repeatMappedReadsNum = 0;
	double junctionUniReads = 0;
	double junctionAllReads = 0;
	
	HashMap<String, double[]> mapChrID2ReadsNum = new HashMap<String, double[]>();
	
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
		lsResult.add(new String[]{"allReadsNum", (long)allReadsNum + ""});
		lsResult.add(new String[]{"mappedReadsNum", (long)mappedReadsNum + ""});
		lsResult.add(new String[]{"uniqMappedReadsNum", (long)uniqMappedReadsNum + ""});
		lsResult.add(new String[]{"repeatMappedReadsNum", (long)repeatMappedReadsNum + ""});
		lsResult.add(new String[]{"junctionAllReads", (long)junctionAllReads + ""});
		lsResult.add(new String[]{"junctionUniReads", (long)junctionUniReads + ""});
		lsResult.add(new String[]{"unmappedReadsNum", (long)unmappedReadsNum + ""});

		lsResult.add(new String[]{"mappringRates", (double)mappedReadsNum/allReadsNum + ""});
		lsResult.add(new String[]{"uniqMappingRates", (double)uniqMappedReadsNum/allReadsNum + ""});
		
		lsResult.add(new String[]{"Reads On Chromosome",  ""});
		try {
			lsResult.addAll(getLsChrID2Num());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lsResult;
	}
	
	private ArrayList<String[]> getLsChrID2Num() {
		ArrayList<String[]> lsChrID2Num = new ArrayList<String[]>();
		for (Entry<String, double[]> entry : mapChrID2ReadsNum.entrySet()) {
			String[] tmp = new String[]{entry.getKey(), (long)entry.getValue()[0] + ""};
			lsChrID2Num.add(tmp);
		}
		Collections.sort(lsChrID2Num, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				return o1[0].compareTo(o2[0]);
			}
		});
		return lsChrID2Num;
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
			int readsMappedWeight = samRecord.getMappedReadsWeight();
			allReadsNum = allReadsNum + (double)1/readsMappedWeight;
			if (samRecord.isMapped()) {
				mappedReadsNum = mappedReadsNum + (double)1/readsMappedWeight;
				setChrReads(readsMappedWeight, samRecord);
				if (samRecord.isUniqueMapping()) {
					uniqMappedReadsNum ++;
					if (samRecord.isJunctionReads()) {
						junctionUniReads ++;
					}
				}
				else {
					repeatMappedReadsNum = repeatMappedReadsNum + (double)1/readsMappedWeight;
				}
				if (samRecord.isJunctionReads()) {
					junctionAllReads = junctionAllReads + (double)1/readsMappedWeight;
				}
			}
			else {
				unmappedReadsNum = unmappedReadsNum + (double)1/readsMappedWeight;
			}
		}
		samFile.close();
	}
	
	
	private void setChrReads(int readsWeight, SamRecord samRecord) {
		String chrID = samRecord.getRefID();
		double[] chrNum;
		if (mapChrID2ReadsNum.containsKey(chrID)) {
			chrNum = mapChrID2ReadsNum.get(chrID);
		}
		else {
			chrNum = new double[1];
			mapChrID2ReadsNum.put(chrID, chrNum);
		}
		chrNum[0] = chrNum[0] + (double)1/readsWeight;
	}
}
