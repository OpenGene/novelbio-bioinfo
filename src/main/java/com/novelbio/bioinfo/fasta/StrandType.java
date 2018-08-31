package com.novelbio.bioinfo.fasta;

/**
 * 用于提取序列时提取正向还是反向的序列
 * 也用于blast中，比对到正向链还是反向链
 * @author novelbio
 *
 */
public enum StrandType {
	 /** 转录本特有的方向，不同的exon有不同的方向
	  * 也用于blast中的不考虑链方向
	  */
	 isoForward, 
	 /** 一直正向 */
	 cis, 
	 /** 一直反向 */
	 trans;
}