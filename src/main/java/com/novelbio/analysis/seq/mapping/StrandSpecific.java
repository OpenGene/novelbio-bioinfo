package com.novelbio.analysis.seq.mapping;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 
 * @author zomg0jie
 *
 */
//TODO 到底FR和RF是哪种建库方式，还有待考证
public enum StrandSpecific {
	UNKNOWN,
	/** 表示有方向的测序，无方向 */
	NONE,
	/** 表示有方向的测序，第一条链的方向 */
	FIRST_READ_TRANSCRIPTION_STRAND,
	/** 表示有方向的测序，第二条链的方向 */
	SECOND_READ_TRANSCRIPTION_STRAND;
	
	public static HashMap<String, StrandSpecific> getMapStrandLibrary() {
		LinkedHashMap<String, StrandSpecific> mapReadsQualtiy = new LinkedHashMap<String, StrandSpecific>();
		mapReadsQualtiy.put("Predict Automaticly", UNKNOWN);
		mapReadsQualtiy.put("Not Consider Strand", NONE);
		mapReadsQualtiy.put("1st Read is Strand(Proton)", FIRST_READ_TRANSCRIPTION_STRAND);
		mapReadsQualtiy.put("2st Read is Strand", SECOND_READ_TRANSCRIPTION_STRAND);
		return mapReadsQualtiy;
	}
}
