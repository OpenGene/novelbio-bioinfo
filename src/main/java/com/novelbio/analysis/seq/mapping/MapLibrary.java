package com.novelbio.analysis.seq.mapping;

import java.util.HashMap;
import java.util.LinkedHashMap;

public enum MapLibrary {
	SingleEnd, PairEnd, MatePair, MatePairLong;
	public static HashMap<String, MapLibrary> getMapLibrary() {
		LinkedHashMap<String, MapLibrary> mapReadsQualtiy = new LinkedHashMap<String, MapLibrary>();
		mapReadsQualtiy.put("SingleEnd", SingleEnd);
		mapReadsQualtiy.put("PairEnd", PairEnd);
		mapReadsQualtiy.put("MatePair", MatePair);
		mapReadsQualtiy.put("MatePairLong", MatePairLong);
		return mapReadsQualtiy;
	}
}
