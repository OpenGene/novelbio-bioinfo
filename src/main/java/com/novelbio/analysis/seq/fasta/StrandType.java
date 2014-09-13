package com.novelbio.analysis.seq.fasta;

public enum StrandType {
	 /** 转录本特有的方向，不同的exon有不同的方向 */
	 isoForward, 
	 /** 一直正向 */
	 cis, 
	 /** 一直反向 */
	 trans;
}