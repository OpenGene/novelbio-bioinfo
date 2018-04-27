package com.novelbio.database.model.geneanno;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public enum GOtype {
	BP("Biological Process", "BP", "P"), CC("Cellular Component", "CC", "C"), MF("Molecular Function", "MF", "F"), ALL("All", "ALL", "A");
	String detail = "";
	String oneWord = "";
	String twoWord = "";
	static Map<String, GOtype> mapStr2Gotype;
	static Map<String, GOtype> mapStrShort2Gotype;
	static Map<String, GOtype> mapStrAll2Gotype;
	
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
		mapStr2Gotype = new LinkedHashMap<String, GOtype>();
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
		mapStrShort2Gotype = new LinkedHashMap<String, GOtype>();
		mapStrShort2Gotype.put("P", BP);
		mapStrShort2Gotype.put("C", CC);
		mapStrShort2Gotype.put("F", MF);
		mapStrShort2Gotype.put("A", ALL);
		return mapStrShort2Gotype;
	}
	
	/**
	 * key: 全长说明文字
	 * @param needAll 是否需要All这个选项。All表示所有的GO类型
	 * @return
	 */
	public static Map<String, GOtype> getMapStrAllGotype(boolean needAll) {
		if (mapStrAll2Gotype != null) {
			if (needAll && !mapStrAll2Gotype.containsKey("ALL")) {
				mapStrAll2Gotype.put("ALL", ALL);
			} else if (!needAll && mapStrAll2Gotype.containsKey("ALL")) {
				mapStrAll2Gotype.remove("ALL");
			}
			return mapStrAll2Gotype;
		}
		mapStrAll2Gotype = new LinkedHashMap<String, GOtype>();
		mapStrAll2Gotype.put("Biological Process", BP);
		mapStrAll2Gotype.put("Cellular Component", CC);
		mapStrAll2Gotype.put("Molecular Function", MF);
		if (needAll) {
			mapStrAll2Gotype.put("ALL", ALL);
		}
		
		return mapStrAll2Gotype;
	}
	
	
	public static enum GORelation {
		NONE, IS, PART_OF, REGULATE, REGULATE_POS, REGULATE_NEG;
		static Map<String, GORelation> mapStr2GoRelation;
	
		public static Map<String, GORelation> getMapStr2GoRelation() {
			if (mapStr2GoRelation != null) {
				return mapStr2GoRelation;
			}
			mapStr2GoRelation = new HashMap<String, GOtype.GORelation>();
			mapStr2GoRelation.put("NONE", NONE);
			mapStr2GoRelation.put("IS", IS);
			mapStr2GoRelation.put("PART_OF", PART_OF);
			mapStr2GoRelation.put("REGULATE", REGULATE);
			mapStr2GoRelation.put("REGULATE_POS", REGULATE_POS);
			mapStr2GoRelation.put("REGULATE_NEG", REGULATE_NEG);
			return mapStr2GoRelation;
		}
	}
	
}
