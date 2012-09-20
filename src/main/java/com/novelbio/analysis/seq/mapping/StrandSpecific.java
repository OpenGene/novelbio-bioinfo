package com.novelbio.analysis.seq.mapping;

import java.util.HashMap;
import java.util.LinkedHashMap;

public enum StrandSpecific {
	/** 可能是表示有方向的测序，无方向 */
	NONE,
	/** 可能是表示有方向的测序，第一条链的方向 */
	FIRST_READ_TRANSCRIPTION_STRAND,
	/** 可能是表示有方向的测序，第二条链的方向 */
	SECOND_READ_TRANSCRIPTION_STRAND;
	
	public static HashMap<String, StrandSpecific> getMapStrandLibrary() {
		LinkedHashMap<String, StrandSpecific> mapReadsQualtiy = new LinkedHashMap<String, StrandSpecific>();
		mapReadsQualtiy.put("None", NONE);
		mapReadsQualtiy.put("FIRST_READ_TRANSCRIPTION_STRAND", FIRST_READ_TRANSCRIPTION_STRAND);
		mapReadsQualtiy.put("SECOND_READ_TRANSCRIPTION_STRAND", SECOND_READ_TRANSCRIPTION_STRAND);
		return mapReadsQualtiy;
	}
}
