package com.novelbio.analysis.seq.genome.gffOperate;

public enum EnumMrnaCdsLocate {
	/**  标记codInExon不在UTR或CDS中，譬如该基因是none coding rna，那么就没有UTR和CDS */
	NONE,
	/**  标记codInExon处在5UTR中  */
	UTR5,
	/**  标记codInExon处在3UTR中 */
	UTR3,
	/** 标记codInExon不在UTR中 */
	OUT,
	/** 标记codInExon在CDS中 */
	CDS
}
