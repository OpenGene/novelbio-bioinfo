package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMReadGroupRecord;

import java.util.Map.Entry;

public class SamRGroup {
	SAMReadGroupRecord samReadGroupRecord;
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 务必要有东西
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform 默认是Illumina
	 * @return 返回的值可以直接用于
	 */
	public SamRGroup(String sampleID, String LibraryName, String SampleName, String Platform) {
		if (LibraryName == null || LibraryName.equals("")) {
			LibraryName = sampleID;
		}
		if (SampleName == null || SampleName.equals("")) {
			SampleName = sampleID;
		}
		if (Platform == null || Platform.equals("")) {
			Platform = "Illumina";
		}
		
		samReadGroupRecord = new SAMReadGroupRecord(sampleID);
		samReadGroupRecord.setAttribute(SAMReadGroupRecord.LIBRARY_TAG, LibraryName);
		samReadGroupRecord.setAttribute(SAMReadGroupRecord.PLATFORM_TAG, Platform);
		samReadGroupRecord.setAttribute(SAMReadGroupRecord.READ_GROUP_SAMPLE_TAG, SampleName);
	}

	public SamRGroup(SAMReadGroupRecord samReadGroupRecord) {
		this.samReadGroupRecord = samReadGroupRecord;
	}
	
	/** 没有则返回null */
	public String getID() {
		if (samReadGroupRecord == null) {
			return null;
		}
		return samReadGroupRecord.getId();
	}
	
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public String toString() {
		String sampleGroup = "@RG\\tID:" + samReadGroupRecord.getId();
		for (Entry<String, String> entry : samReadGroupRecord.getAttributes()) {
			if (entry.getKey() != null && !entry.getKey().equals("")) {
				sampleGroup = sampleGroup + "\\t" + entry.getKey() + ":" + entry.getValue();
			}
		}
		return sampleGroup;
	}
	
	public SAMReadGroupRecord getSamReadGroupRecord() {
		return samReadGroupRecord;
	}
}
