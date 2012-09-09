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
		mapReadsQualtiy.put("SingleEnd", NONE);
		mapReadsQualtiy.put("PairEnd", FIRST_READ_TRANSCRIPTION_STRAND);
		mapReadsQualtiy.put("MatePair", SECOND_READ_TRANSCRIPTION_STRAND);
		return mapReadsQualtiy;
	}
}
