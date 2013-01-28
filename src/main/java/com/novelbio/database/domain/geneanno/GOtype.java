package com.novelbio.database.domain.geneanno;

import java.util.Map;

public enum GOtype {
	BP("Biological Process", "BP", "P"), CC("Cellular Component", "CC", "C"), MF("Molecular Function", "MF", "F"), ALL("All", "ALL", "A");
	String detail = "";
	String oneWord = "";
	String twoWord = "";
	static Map<String, GOtype> mapStr2Gotype;
	static Map<String, GOtype> mapStrShort2Gotype;
	
	GOtype(String detail, String twoWord, String oneWord) {
		this.detail = detail;
		this.twoWord = twoWord;
		this.oneWord = oneWord;
	}
	
	@Override
	public String toString() {
		return detail;
	}
	
	public String getOneWord() {
		return oneWord;
	}
	public String getTwoWord() {
		return twoWord;
	}
	public static Map<String, GOtype> getMapStr2Gotype() {
		if (mapStr2Gotype != null) {
			return mapStr2Gotype;
		}
		mapStr2Gotype.put("BP", BP);
		mapStr2Gotype.put("CC", CC);
		mapStr2Gotype.put("MF", MF);
		mapStr2Gotype.put("ALL", ALL);
		return mapStr2Gotype;
	}
	public static Map<String, GOtype> getMapStrShort2Gotype() {
		if (mapStrShort2Gotype != null) {
			return mapStrShort2Gotype;
		}
		mapStrShort2Gotype.put("P", BP);
		mapStrShort2Gotype.put("C", CC);
		mapStrShort2Gotype.put("F", MF);
		mapStrShort2Gotype.put("A", ALL);
		return mapStrShort2Gotype;
	}
	public static enum GORelation {
		IS, REGULATE, PART_OF, REGULATE_POS, REGULATE_NEG
	}
	
}
