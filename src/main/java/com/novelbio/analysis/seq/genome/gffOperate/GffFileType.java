package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.HashMap;
import java.util.Map;

import com.novelbio.generalConf.NovelBioConst;

public enum GffFileType {
	/** UCSC的gene list，必须排过序 */
	UCSC,
	/** 常规的GTF文件，包括cufflinks产生的那种 */
	GTF,
	/** NCBI的细菌GFF文件 */
	Bacterium,
	/** 水稻TIGR6.0版本专用 */
	TIGR,
	/** tigr6.1 tigr7, tair, 大豆等 */
	Plant,
	/** NCBI动物的GFF文件，需要经过修正才好读取 */
	NCBI,
	/** 根据蛋白序列和RNA序列产生的GFF信息 */
	Fasta;
	
	/** key为小写 */
	static Map<String, GffFileType> mapName2Type;
	
	public static GffFileType getType(String typeName) {
		return getMapGffFileType().get(typeName.toLowerCase());
	}
	/**
	 * key为小写
	 * @return
	 */
	public static Map<String, GffFileType> getMapGffFileType() {
		if (mapName2Type != null) {
			return mapName2Type;
		}
		mapName2Type = new HashMap<String, GffFileType>();
		mapName2Type.put("GFF_CUFFLINK_GTF".toLowerCase(), GTF);
		mapName2Type.put("GTF".toLowerCase(), GTF);
		mapName2Type.put("GFF_UCSC".toLowerCase(), UCSC);
		mapName2Type.put("GFF_TIGR".toLowerCase(), TIGR);
		mapName2Type.put("GFF_PLANT".toLowerCase(), Plant);
		mapName2Type.put("GFF_NCBI".toLowerCase(), NCBI);
		mapName2Type.put("GFF_FASTA".toLowerCase(), Fasta);
		return mapName2Type;
	}
}
