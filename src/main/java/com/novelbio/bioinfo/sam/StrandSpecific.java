package com.novelbio.bioinfo.sam;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 
 * @author zomg0jie
 *
 */
//TODO 到底FR和RF是哪种建库方式，还有待考证
public enum StrandSpecific {
	UNKNOWN("unknown"),
	/** 表示有方向的测序，无方向 */
	NONE("none"),
	/** 表示有方向的测序，第一条链的方向 */
	FIRST_READ_TRANSCRIPTION_STRAND("1st_read_is_strand(proton)"),
	/** 表示有方向的测序，第二条链的方向 */
	SECOND_READ_TRANSCRIPTION_STRAND("2nd_read_is_strand");
	
	String shortInfo;
	
	private StrandSpecific(String shortInfo) {
		this.shortInfo = shortInfo;
	}
	
	public static HashMap<String, StrandSpecific> getMapStrandLibrary() {
		LinkedHashMap<String, StrandSpecific> mapReadsQualtiy = new LinkedHashMap<String, StrandSpecific>();
		mapReadsQualtiy.put("Predict By Software", UNKNOWN);
		mapReadsQualtiy.put("Not Consider Strand", NONE);
		mapReadsQualtiy.put("1st Read is Strand(Ion Proton)", FIRST_READ_TRANSCRIPTION_STRAND);
		mapReadsQualtiy.put("2nd Read is Strand", SECOND_READ_TRANSCRIPTION_STRAND);
		return mapReadsQualtiy;
	}
	
	/** 简短一些的名字，让人看得懂，如 1st_read_is_strand(proton) 和 2nd_read_is_strand*/
	public String toStringShort() {
		return shortInfo;
	}
}
