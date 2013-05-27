package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;

/** <>仅用于分析sambam文件<>
 * 根据需求判定是否需要执行{@link #initial()}
 *  */
public class SamFileStatistics implements AlignmentRecorder {
	SamFile samFile;
	boolean countReadsNum;
	
	double allReadsNum = 0;
	double unmappedReadsNum = 0;
	double mappedReadsNum = 0;
	double uniqMappedReadsNum = 0;
	double repeatMappedReadsNum = 0;
	double junctionUniReads = 0;
	double junctionAllReads = 0;
	
	Map<String, double[]> mapChrID2ReadsNum = new TreeMap<String, double[]>(new Comparator<String>() {
		PatternOperate patternOperate = new PatternOperate("\\d+", false);
		@Override
		public int compare(String arg0, String arg1) {
			String str1 = patternOperate.getPatFirst(arg0);
			String str2 = patternOperate.getPatFirst(arg1);
			if (str1 == null || str1.equals("") || str2 == null || str2.equals("")) {
				 return arg0.compareTo(arg1);
			}
			Integer num1 = Integer.parseInt(str1);
			Integer num2 = Integer.parseInt(str2);
			return num1.compareTo(num2);
		}
	});
	
	public SamFileStatistics() { }
	
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
	
	/** 把结果写入文本，首先要运行 statistics */
	public void writeToFile(String outFileName) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFileName, true);
		txtWrite.ExcelWrite(getMappingInfo());
		txtWrite.close();
	}
	
	/**
	 * 获取每条染色体所对应的reads数量
	 * @return
	 */
	public Map<String, Long> getMapChrID2Len() {
		Map<String, Long> mapChrID2Len = new LinkedHashMap<String, Long>();
		for (String chrID : mapChrID2ReadsNum.keySet()) {
			mapChrID2Len.put(chrID, (long)mapChrID2ReadsNum.get(chrID)[0]);
		}
		return mapChrID2Len;
	}
	/**
	 * 首先要运行 statistics
	 * @return
	 */
	public ArrayList<String[]> getMappingInfo() {
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
	
	public void statistics() {
		if (countReadsNum) {
			return;
		}
		countReadsNum = true;
		 initial();
		readSamFile();
	}
	
	/** 初始化 */
	public void initial() {
		allReadsNum = 0;
		unmappedReadsNum = 0;
		mappedReadsNum = 0;
		uniqMappedReadsNum = 0;
		repeatMappedReadsNum = 0;
		junctionAllReads = 0;
		junctionUniReads = 0;
	}
	
	private void readSamFile() {
		for (SamRecord samRecord : samFile.readLines()) {
			addAlignRecord(samRecord);
		}
		summary();
		samFile.close();
	}

	@Override
	public void addAlignRecord(AlignRecord samRecord) {
		int readsMappedWeight = samRecord.getMappedReadsWeight();
		allReadsNum = allReadsNum + (double)1/readsMappedWeight;
		if (samRecord.isMapped()) {
			mappedReadsNum = mappedReadsNum + (double)1/readsMappedWeight;
			setChrReads(readsMappedWeight, samRecord);
			if (samRecord.isUniqueMapping()) {
				uniqMappedReadsNum ++;
				if (samRecord.isJunctionCovered()) {
					junctionUniReads ++;
				}
			}
			else {
				repeatMappedReadsNum = repeatMappedReadsNum + (double)1/readsMappedWeight;
			}
			if (samRecord.isJunctionCovered()) {
				junctionAllReads = junctionAllReads + (double)1/readsMappedWeight;
			}
		}
		else {
			unmappedReadsNum = unmappedReadsNum + (double)1/readsMappedWeight;
		}
	}

	
	private void setChrReads(int readsWeight, AlignRecord samRecord) {
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

	@Override
	public void summary() {	}
}
