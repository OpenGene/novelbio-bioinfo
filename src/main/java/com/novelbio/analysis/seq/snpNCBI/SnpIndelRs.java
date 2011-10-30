package com.novelbio.analysis.seq.snpNCBI;
/**
 * UCSC的SNP信息
 * @author zong0jie
 *
 */
public class SnpIndelRs {
	String chrID;
	/**
	 * snp或者indel的起点
	 */
	String locStart;
	/**
	 * snp或者indel的终点
	 */
	String locEnd;
	/**
	 * dbSNP Reference SNP (rs) identifier
	 */
	String SnpRsID;
	/**
	 * 暂时不用
	 */
	int score = 0;
	/**
	 * Which DNA strand contains the observed alleles
	 */
	boolean strand;
	/**
	 * 只有当refUCSC和refNCBI不一样的时候才会有内容
	 */
	String refNCBI = "";
//	String refUCSC = "";
	/**
	 * 实际观察到的snp或者indel
	 * The sequences of the observed alleles from rs-fasta files
	 */
	String observed = "";
	/**
	 * Sample type from exemplar submitted SNPs (ss)
	 * enum('unknown', 'genomic', 'cDNA')
	 */
	String molType = "";
	/**
	 * Class of variant (single, in-del, named, mixed, etc.)
	 * enum('unknown', 'single', 'in-del', 'het', 'microsatellite', 'named', 'mixed', 'mnp', 'insertion', 'deletion')
	 * 具体是什么类型
	 * indel还是snp还是其他
	 * 同UCSC的class
	 */
	String type = "";
	/**
	 * Validation status of the SNP
	 * set('unknown', 'by-cluster', 'by-frequency', 'by-submitter', 'by-2hit-2allele', 'by-hapmap', 'by-1000genomes')
	 */
	String valid = "";
	/**
	 *  Average heterozygosity from all observations. Note: may be computed on small number of samples.
	 * 平均杂合率
	 */
	double avHet = -1;
	/**
	 * Standard Error for the average heterozygosity
	 * 平均杂合率标准误
	 */
	double avHetSE = -1;
	/**
	 * Functional category of the SNP (coding-synon, coding-nonsynon, intron, etc.)
	 * set('unknown', 'coding-synon', 'intron', 'coding-synonymy-unknown',
	 *  'near-gene-3', 'near-gene-5', 'nonsense', 'missense', 'frameshift', 
	 *  'cds-indel', 'untranslated-3', 'untranslated-5', 'splice-3', 'splice-5')
	 */
	String function = "";
	/**
	 * Type of mapping inferred from size on reference; may not agree with class
	 * enum('range', 'exact', 'between', 'rangeInsertion', 'rangeSubstitution', 'rangeDeletion')
	 */
	String locType = "";
	/**
	 * The quality of the alignment: 1 = unique mapping, 2 = non-unique, 3 = many matches
	 */
	int weight = 0;
	/**
	 * Unusual conditions noted by UCSC that may indicate a problem with the data
	 * 
	 * set('RefAlleleMismatch', 'RefAlleleRevComp', 'DuplicateObserved', 'MixedObserved', 'FlankMismatchGenomeLonger', 
	 * 'FlankMismatchGenomeEqual', 'FlankMismatchGenomeShorter', 'NamedDeletionZeroSpan', 'NamedInsertionNonzeroSpan', 
	 * 'SingleClassLongerSpan', 'SingleClassZeroSpan', 'SingleClassTriAllelic', 'SingleClassQuadAllelic',
	 *  'ObservedWrongFormat', 'ObservedTooLong', 'ObservedContainsIupac', 'ObservedMismatch',
	 *   'MultipleAlignments', 'NonIntegerChromCount', 'AlleleFreqSumNot1')
	 */
	String exceptions = "";
	/**
	 * Number of distinct submitter handles for submitted SNPs for this ref SNP
	 */
	int submitterCount = 0;
	/**
	 * 提交者List of submitter handles
	 * 逗号隔开，最后有一个逗号
	 */
	String submitters = "";
	/**
	 * Number of observed alleles with frequency data
	 */
	int alleleFreqCount = 0;
	/**
	 * Observed alleles for which frequency data are available
	 */
	String alleles = "";
	/**
	 * Count of chromosomes (2N) on which each allele was observed.
	 * Note: this is extrapolated by dbSNP from submitted frequencies and total sample 2N, and is not always an integer.
	 * 类似1.000000,1.000000,等，用逗号隔开的double
	 */
	String alleleNs = "";;
	/**
	* Allele frequencies
	* 类似0.5000000,0.5000000,等，用逗号隔开的double
	*/
	String alleleFreqs = "";
	/**
	 * SNP attributes extracted from dbSNP's SNP_bitfield table
	 * set('clinically-assoc', 'maf-5-some-pop', 'maf-5-all-pops', 'has-omim-omia', 'microattr-tpa', 'submitted-by-lsdb', 'genotype-conflict', 'rs-cluster-nonoverlapping-alleles', 'observed-mismatch')
	 */
	String bitfields = "";
}
