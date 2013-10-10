package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.LinkedHashMap;
import java.util.Map;

public enum EnumMapNormalizeType {
	/** 将每个double[]*1million/AllReadsNum 也就是将每个点除以测序深度 */
	allreads, 
	/** 将每个double[]求和/double.length 也就是将每个点除以该gene的平均测序深度 */
	per_gene,
	no_normalization;

	static Map<String, EnumMapNormalizeType> mapNormalizedType;
	public static Map<String, EnumMapNormalizeType> getMapNormalizedType() {
		if (mapNormalizedType == null) {
			mapNormalizedType = new LinkedHashMap<>();
			mapNormalizedType.put("Normalization_All_Reads", allreads);
			mapNormalizedType.put("Normalization_No", no_normalization);
			mapNormalizedType.put("Normalization_Per_Gene", per_gene);
		}
		return mapNormalizedType;
	}
}
