package com.novelbio.analysis.seq.resequencing;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public enum SnpLevel {
	/** Ref纯合，表示没有snp */
	RefHomo, 
	/** 杂合很少，只要有就跳选出来，可以认为是最宽松的阈值 */
	HeteroLess, 
	/** 中等杂合 */
	HeteroMid, 
	/** 杂合很高，比它更高的等级就是纯合snp */
	HeteroMore,
	/** 纯合snp */
	SnpHomo;
	
	static Map<String, SnpLevel> mapStr2SnpLevel;
	
	public static Map<String, SnpLevel> getMapStr2SnpLevel() {
		if (mapStr2SnpLevel != null) {
			return mapStr2SnpLevel;
		}
		mapStr2SnpLevel = new LinkedHashMap<String, SnpLevel>();
		mapStr2SnpLevel.put("RefHomo", RefHomo);
		mapStr2SnpLevel.put("HeteroLess", HeteroLess);
		mapStr2SnpLevel.put("HeteroMid", HeteroMid);
		mapStr2SnpLevel.put("HeteroMore", HeteroMore);
		mapStr2SnpLevel.put("SnpHomo", SnpHomo);
		return mapStr2SnpLevel;
	}
	
	public static SnpLevel getSnpLevel(String snpLevel) {
		return getMapStr2SnpLevel().get(snpLevel);
	}
}
