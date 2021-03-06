package com.novelbio.bioinfo.sam;

import java.util.HashMap;
import java.util.LinkedHashMap;

public enum MapLibrary {
	SingleEnd, PairEnd, MatePair,
	/** insert size特别长的mate pair文库 */
	MatePairLong, Unknown;
	public static HashMap<String, MapLibrary> getMapLibrary() {
		LinkedHashMap<String, MapLibrary> mapReadsQualtiy = new LinkedHashMap<String, MapLibrary>();
//		mapReadsQualtiy.put("SingleEnd", SingleEnd);
		mapReadsQualtiy.put("Single_or_PairEnd", PairEnd);
		mapReadsQualtiy.put("MatePair", MatePair);
		mapReadsQualtiy.put("MatePairLong", MatePairLong);
		return mapReadsQualtiy;
	}
}
