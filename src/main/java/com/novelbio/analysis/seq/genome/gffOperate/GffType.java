package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.HashMap;
import java.util.Map;

import com.novelbio.generalConf.NovelBioConst;

public enum GffType {
	/** NCBI动物的GFF文件，需要经过修正才好读取 */
	NCBI,
	/** tigr6.1 tigr7, tair, 大豆等 */
	Plant,
	/** 水稻TIGR6.0版本专用 */
	TIGR,
	/** 常规的GTF文件，包括cufflinks产生的那种 */
	GTF,
	/** UCSC的gene list，必须排过序 */
	UCSC,
	/** 根据蛋白序列和RNA序列产生的GFF信息 */
	Fasta;
	
	/** key为小写 */
	static Map<String, GffType> mapName2Type;
	/** key为小写 */
	static Map<String, GffType> mapName2TypeSimple;
	
	public static GffType getType(String typeName) {
		return getMapGffType().get(typeName.toLowerCase());
	}
	/**
	 * key为小写
	 * 各种key都有了
	 * @return
	 */
	public static Map<String, GffType> getMapGffType() {
		if (mapName2Type != null) {
			return mapName2Type;
		}
		mapName2Type = new HashMap<String, GffType>();
		mapName2Type.put("GFF_CUFFLINK_GTF".toLowerCase(), GTF);
		mapName2Type.put("GTF".toLowerCase(), GTF);
		mapName2Type.put("GFF_UCSC".toLowerCase(), UCSC);
		mapName2Type.put("GFF_TIGR".toLowerCase(), TIGR);
		mapName2Type.put("GFF_PLANT".toLowerCase(), Plant);
		mapName2Type.put("GFF_NCBI".toLowerCase(), NCBI);
		mapName2Type.put("GFF_FASTA".toLowerCase(), Fasta);
		mapName2Type.put("CUFFLINK_GTF".toLowerCase(), GTF);
		mapName2Type.put("GTF".toLowerCase(), GTF);
		mapName2Type.put("UCSC".toLowerCase(), UCSC);
		mapName2Type.put("TIGR".toLowerCase(), TIGR);
		mapName2Type.put("PLANT".toLowerCase(), Plant);
		mapName2Type.put("NCBI".toLowerCase(), NCBI);
		mapName2Type.put("FASTA".toLowerCase(), Fasta);
		return mapName2Type;
	}
	
	/**
	 * key为小写
	 * key只有指定的几个
	 * @return
	 */
	public static Map<String, GffType> getMapGffTypeSimple() {
		if (mapName2TypeSimple != null) {
			return mapName2TypeSimple;
		}
		mapName2TypeSimple = new HashMap<String, GffType>();
		mapName2TypeSimple.put("GTF".toLowerCase(), GTF);
		mapName2TypeSimple.put("UCSC".toLowerCase(), UCSC);
		mapName2TypeSimple.put("TIGR".toLowerCase(), TIGR);
		mapName2TypeSimple.put("PLANT".toLowerCase(), Plant);
		mapName2TypeSimple.put("NCBI".toLowerCase(), NCBI);
		mapName2TypeSimple.put("FASTA".toLowerCase(), Fasta);
		return mapName2Type;
	}
}
