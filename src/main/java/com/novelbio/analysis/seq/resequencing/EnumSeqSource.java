package com.novelbio.analysis.seq.resequencing;

/**
 * 用于MAF文件格式中的Sequence Source 属性
 * @author novelbio
 *
 */
public enum EnumSeqSource {
	/**全集因组重测序*/
	WGS,
	/**全集因组扩增*/
	WGA,
	WXS,
	RNASeq,
	Other
}
