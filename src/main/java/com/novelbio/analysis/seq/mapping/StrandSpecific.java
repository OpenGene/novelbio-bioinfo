package com.novelbio.analysis.seq.mapping;

import java.util.HashMap;
import java.util.LinkedHashMap;

public enum StrandSpecific {
	/** �����Ǳ�ʾ�з���Ĳ����޷��� */
	NONE,
	/** �����Ǳ�ʾ�з���Ĳ��򣬵�һ�����ķ��� */
	FIRST_READ_TRANSCRIPTION_STRAND,
	/** �����Ǳ�ʾ�з���Ĳ��򣬵ڶ������ķ��� */
	SECOND_READ_TRANSCRIPTION_STRAND;
	
	public static HashMap<String, StrandSpecific> getMapStrandLibrary() {
		LinkedHashMap<String, StrandSpecific> mapReadsQualtiy = new LinkedHashMap<String, StrandSpecific>();
		mapReadsQualtiy.put("None", NONE);
		mapReadsQualtiy.put("FIRST_READ_TRANSCRIPTION_STRAND", FIRST_READ_TRANSCRIPTION_STRAND);
		mapReadsQualtiy.put("SECOND_READ_TRANSCRIPTION_STRAND", SECOND_READ_TRANSCRIPTION_STRAND);
		return mapReadsQualtiy;
	}
}
