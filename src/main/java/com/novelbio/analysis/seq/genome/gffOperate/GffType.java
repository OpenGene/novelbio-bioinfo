package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.HashMap;
import java.util.Map;

public enum GffType {
	/** NCBI动物的GFF文件，需要经过修正才好读取 */
	GFF3,
	/** 常规的GTF文件，包括cufflinks产生的那种 */
	GTF,
	/** UCSC的gene list，必须排过序 */
	UCSC,
	/** 暂时只支持输出，不支持输入 */
	BED,
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
		mapName2Type.put("GFF_GTF".toLowerCase(), GTF);
		mapName2Type.put("FASTA".toLowerCase(), Fasta);
		mapName2Type.put("GFF_UCSC".toLowerCase(), UCSC);
		mapName2Type.put("GFF_TIGR".toLowerCase(), GFF3);
		mapName2Type.put("GFF_PLANT".toLowerCase(), GFF3);
		mapName2Type.put("GFF_NCBI".toLowerCase(), GFF3);
		mapName2Type.put("GFF_GFF3".toLowerCase(), GFF3);
		mapName2Type.put("GFF_FASTA".toLowerCase(), Fasta);
		mapName2Type.put("CUFFLINK_GTF".toLowerCase(), GTF);
		mapName2Type.put("GTF".toLowerCase(), GTF);
		mapName2Type.put("UCSC".toLowerCase(), UCSC);
		mapName2Type.put("TIGR".toLowerCase(), GFF3);
		mapName2Type.put("PLANT".toLowerCase(), GFF3);
		mapName2Type.put("NCBI".toLowerCase(), GFF3);
		mapName2Type.put("GFF3".toLowerCase(), GFF3);
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
		mapName2TypeSimple.put("GFF3".toLowerCase(), GFF3);
		return mapName2TypeSimple;
	}
}
