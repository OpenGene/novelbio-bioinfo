package com.novelbio.analysis.seq.resequencing;

import java.util.HashMap;
import java.util.Map;

public enum SnpLevel {
	/** 纯合 */
	Homo, 
	/** 杂合很少，只要有就跳选出来，可以认为是最宽松的阈值 */
	HetoLess, 
	/** 中等杂合 */
	HetoMid, 
	/** 杂合很高，比它更高的等级就是纯合snp */
	HetoMore,
	/** 纯合snp */
	SnpHomo;
	
	static Map<String, SnpLevel> mapStr2SnpLevel;
	
	public static Map<String, SnpLevel> getMapStr2SnpLevel() {
		if (mapStr2SnpLevel != null) {
			return mapStr2SnpLevel;
		}
		mapStr2SnpLevel = new HashMap<String, SnpLevel>();
		mapStr2SnpLevel.put("Homo", Homo);
		mapStr2SnpLevel.put("HetoLess", HetoLess);
		mapStr2SnpLevel.put("HetoMid", HetoMid);
		mapStr2SnpLevel.put("HetoMore", HetoMore);
		mapStr2SnpLevel.put("SnpHomo", SnpHomo);
		return mapStr2SnpLevel;
	}
	
	public static SnpLevel getSnpLevel(String snpLevel) {
		return getMapStr2SnpLevel().get(snpLevel);
	}
}
