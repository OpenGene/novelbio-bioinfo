package com.novelbio.bioinfo.annotation.cog;

import java.util.LinkedHashMap;
import java.util.Map;

/** cog的种类，cog还是kog */
public enum EnumCogType {
	COG, KOG;
	
	public static Map<String, EnumCogType> getMapCogType() {
		Map<String, EnumCogType> mapCog2Enum = new LinkedHashMap<>();
		mapCog2Enum.put("KOG_Eukaryotes", EnumCogType.KOG);
		mapCog2Enum.put("COG_Prokaryotes", EnumCogType.COG);
		return mapCog2Enum;
	}
}
