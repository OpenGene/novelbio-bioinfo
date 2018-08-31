package com.novelbio.bioinfo.gff;

public enum EnumMrnaLocate {
	/** 外显子中，只有当在ncRNA上时才标记为次 */
	exon,
	/** 在内含子中 */
	intron,
	/** 基因间区  */
	intergenic,
	utr5,
	utr3,
	cds
}
