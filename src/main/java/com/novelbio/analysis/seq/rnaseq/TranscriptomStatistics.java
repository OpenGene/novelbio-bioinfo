package com.novelbio.analysis.seq.rnaseq;
/**
 * 重建转录本的统计
 * @author zong0jie
 *
 */
public class TranscriptomStatistics {
	/** 新基因的数量 */
	int geneNewNum = 0;
	/** 新转录本的数量 */
	int isoNewNum = 0;
	/** 修饰的exon的数量 */
	int exonModifiedNum = 0;
	
	/** 没有被修饰的基因数量 */
	int GeneNoModifiedNum = 0;
	/** 非编码基因的数量 */
	int NunCodingGeneNum = 0;
	/** 有全长CDS的基因数量 */
	int completeCDSGeneNum = 0;
	
	
}
