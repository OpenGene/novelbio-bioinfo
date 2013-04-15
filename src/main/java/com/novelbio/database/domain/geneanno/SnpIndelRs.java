package com.novelbio.database.domain.geneanno;


/**
 * UCSC的SNP信息
 * @author zong0jie
 *
 */
public class SnpIndelRs {
	int taxID = 0;
	String chrID;
	/**
	 * snp或者indel的起点
	 */
	int locStart;
	/**
	 * snp或者indel的终点
	 */
	int locEnd;
	/**
	 * dbSNP Reference SNP (rs) identifier
	 */
	String snpRsID;
	/**
	 * 暂时不用
	 */
	int score = 0;
	/**
	 * Which DNA strand contains the observed alleles
	 */
	String strand = "";
	/**
	 * 只有当refUCSC和refNCBI不一样的时候才会有内容
	 */
	String refNCBI;;
//	String refUCSC = "";
	/**
	 * 实际观察到的snp或者indel
	 * The sequences of the observed alleles from rs-fasta files
	 */
	String observed;
	/**
	 * Sample type from exemplar submitted SNPs (ss)
	 * enum('unknown', 'genomic', 'cDNA')
	 */
	String molType;
	/**
	 * Class of variant (single, in-del, named, mixed, etc.)
	 * enum('unknown', 'single', 'in-del', 'het', 'microsatellite', 'named', 'mixed', 'mnp', 'insertion', 'deletion')
	 * 具体是什么类型
	 * indel还是snp还是其他
	 * 同UCSC的class
	 */
	String type;
	/**
	 * Validation status of the SNP
	 * set('unknown', 'by-cluster', 'by-frequency', 'by-submitter', 'by-2hit-2allele', 'by-hapmap', 'by-1000genomes')
	 */
	String valid;
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
	String function;
	/**
	 * Type of mapping inferred from size on reference; may not agree with class
	 * enum('range', 'exact', 'between', 'rangeInsertion', 'rangeSubstitution', 'rangeDeletion')
	 */
	String locType;
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
	String alleles;
	/**
	 * Count of chromosomes (2N) on which each allele was observed.
	 * Note: this is extrapolated by dbSNP from submitted frequencies and total sample 2N, and is not always an integer.
	 * 类似1.000000,1.000000,等，用逗号隔开的double
	 */
	String alleleNs;
	/**
	* Allele frequencies
	* 类似0.5000000,0.5000000,等，用逗号隔开的double
	*/
	String alleleFreqs;
	/**
	 * SNP attributes extracted from dbSNP's SNP_bitfield table
	 * set('clinically-assoc', 'maf-5-some-pop', 'maf-5-all-pops', 'has-omim-omia', 'microattr-tpa', 'submitted-by-lsdb', 'genotype-conflict', 'rs-cluster-nonoverlapping-alleles', 'observed-mismatch')
	 */
	String bitfields;
	
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	/**
	 * Number of observed alleles with frequency data
	 */	
	public void setAlleleFreqCount(int alleleFreqCount) {
		this.alleleFreqCount = alleleFreqCount;
	}
	/**
	 * Number of observed alleles with frequency data
	 */
	public int getAlleleFreqCount() {
		return alleleFreqCount;
	}
	
	/**
	* Allele frequencies
	* 类似0.5000000,0.5000000,等，用逗号隔开的double
	*/
	public void setAlleleFreqs(String alleleFreqs) {
		this.alleleFreqs = alleleFreqs;
	}
	/**
	* Allele frequencies
	* 类似0.5000000,0.5000000,等，用逗号隔开的double
	*/
	public String getAlleleFreqs() {
		return alleleFreqs;
	}
	/**
	 * Count of chromosomes (2N) on which each allele was observed.
	 * Note: this is extrapolated by dbSNP from submitted frequencies and total sample 2N, and is not always an integer.
	 * 类似1.000000,1.000000,等，用逗号隔开的double
	 */
	public void setAlleleNs(String alleleNs) {
		this.alleleNs = alleleNs;
	}
	/**
	 * Count of chromosomes (2N) on which each allele was observed.
	 * Note: this is extrapolated by dbSNP from submitted frequencies and total sample 2N, and is not always an integer.
	 * 类似1.000000,1.000000,等，用逗号隔开的double
	 */
	public String getAlleleNs() {
		return alleleNs;
	}
	/**
	 * Observed alleles for which frequency data are available
	 */
	public void setAlleles(String alleles) {
		this.alleles = alleles;
	}
	/**
	 * Observed alleles for which frequency data are available
	 */
	public String getAlleles() {
		return alleles;
	}
	/**
	 *  Average heterozygosity from all observations. Note: may be computed on small number of samples.
	 * 平均杂合率
	 * 小于0表示没有
	 */
	public void setAvHet(double avHet) {
		this.avHet = avHet;
	}
	/**
	 *  Average heterozygosity from all observations. Note: may be computed on small number of samples.
	 * 平均杂合率
	 * -1表示没有
	 */
	public double getAvHet() {
		if (avHet < 0) {
			return -1;
		}
		return avHet;
	}
	/**
	 * Standard Error for the average heterozygosity
	 * 平均杂合率标准误
	 * 小于0表示没有
	 */
	public void setAvHetSE(double avHetSE) {
		this.avHetSE = avHetSE;
	}
	/**
	 * Standard Error for the average heterozygosity
	 * 平均杂合率标准误
	 * -1表示没有
	 */
	public double getAvHetSE() {
		if (avHetSE < 0) {
			return -1;
		}
		return avHetSE;
	}
	/**
	 * SNP attributes extracted from dbSNP's SNP_bitfield table
	 * set('clinically-assoc', 'maf-5-some-pop', 'maf-5-all-pops', 'has-omim-omia', 'microattr-tpa', 'submitted-by-lsdb', 'genotype-conflict', 'rs-cluster-nonoverlapping-alleles', 'observed-mismatch')
	 */
	public void setBitfields(String bitfields) {
		this.bitfields = bitfields;
	}
	/**
	 * SNP attributes extracted from dbSNP's SNP_bitfield table
	 * set('clinically-assoc', 'maf-5-some-pop', 'maf-5-all-pops', 'has-omim-omia', 'microattr-tpa', 'submitted-by-lsdb', 'genotype-conflict', 'rs-cluster-nonoverlapping-alleles', 'observed-mismatch')
	 */
	public String getBitfields() {
		return bitfields;
	}
	/**
	 * snp在哪条染色体上
	 * @param chrID
	 */
	public void setChrID(String chrID) {
		this.chrID = chrID;
	}
	/**
	 * snp在哪条染色体上
	 * @param refID
	 */
	public String getChrID() {
		return chrID;
	}
	/**
	 * Unusual conditions noted by UCSC that may indicate a problem with the data
	 * 
	 * set('RefAlleleMismatch', 'RefAlleleRevComp', 'DuplicateObserved', 'MixedObserved', 'FlankMismatchGenomeLonger', 
	 * 'FlankMismatchGenomeEqual', 'FlankMismatchGenomeShorter', 'NamedDeletionZeroSpan', 'NamedInsertionNonzeroSpan', 
	 * 'SingleClassLongerSpan', 'SingleClassZeroSpan', 'SingleClassTriAllelic', 'SingleClassQuadAllelic',
	 *  'ObservedWrongFormat', 'ObservedTooLong', 'ObservedContainsIupac', 'ObservedMismatch',
	 *   'MultipleAlignments', 'NonIntegerChromCount', 'AlleleFreqSumNot1')
	 */
	public void setExceptions(String exceptions) {
		this.exceptions = exceptions;
	}
	/**
	 * Unusual conditions noted by UCSC that may indicate a problem with the data
	 * 
	 * set('RefAlleleMismatch', 'RefAlleleRevComp', 'DuplicateObserved', 'MixedObserved', 'FlankMismatchGenomeLonger', 
	 * 'FlankMismatchGenomeEqual', 'FlankMismatchGenomeShorter', 'NamedDeletionZeroSpan', 'NamedInsertionNonzeroSpan', 
	 * 'SingleClassLongerSpan', 'SingleClassZeroSpan', 'SingleClassTriAllelic', 'SingleClassQuadAllelic',
	 *  'ObservedWrongFormat', 'ObservedTooLong', 'ObservedContainsIupac', 'ObservedMismatch',
	 *   'MultipleAlignments', 'NonIntegerChromCount', 'AlleleFreqSumNot1')
	 */
	public String getExceptions() {
		return exceptions;
	}
	/**
	 * Functional category of the SNP (coding-synon, coding-nonsynon, intron, etc.)
	 * set('unknown', 'coding-synon', 'intron', 'coding-synonymy-unknown',
	 *  'near-gene-3', 'near-gene-5', 'nonsense', 'missense', 'frameshift', 
	 *  'cds-indel', 'untranslated-3', 'untranslated-5', 'splice-3', 'splice-5')
	 */
	public void setFunction(String function) {
		this.function = function;
	}
	/**
	 * Functional category of the SNP (coding-synon, coding-nonsynon, intron, etc.)
	 * set('unknown', 'coding-synon', 'intron', 'coding-synonymy-unknown',
	 *  'near-gene-3', 'near-gene-5', 'nonsense', 'missense', 'frameshift', 
	 *  'cds-indel', 'untranslated-3', 'untranslated-5', 'splice-3', 'splice-5')
	 */
	public String getFunction() {
		return function;
	}
	/**
	 * snp或者indel的终点
	 */
	public void setLocEnd(int locEnd) {
		this.locEnd = locEnd;
	}
	/**
	 * snp或者indel的终点
	 */
	public int getLocEnd() {
		return locEnd;
	}
	
	/**
	 * snp或者indel的起点
	 */
	public void setLocStart(int locStart) {
		this.locStart = locStart;
	}
	/**
	 * snp或者indel的起点
	 */
	public int getLocStart() {
		return locStart;
	}
	
	/**
	 * Type of mapping inferred from size on reference; may not agree with class
	 * enum('range', 'exact', 'between', 'rangeInsertion', 'rangeSubstitution', 'rangeDeletion')
	 */
	public void setLocType(String locType) {
		this.locType = locType;
	}
	/**
	 * Type of mapping inferred from size on reference; may not agree with class
	 * enum('range', 'exact', 'between', 'rangeInsertion', 'rangeSubstitution', 'rangeDeletion')
	 */
	public String getLocType() {
		return locType;
	}
	/**
	 * Sample type from exemplar submitted SNPs (ss)
	 * enum('unknown', 'genomic', 'cDNA')
	 */
	public void setMolType(String molType) {
		this.molType = molType;
	}
	/**
	 * Sample type from exemplar submitted SNPs (ss)
	 * enum('unknown', 'genomic', 'cDNA')
	 */
	public String getMolType() {
		return molType;
	}
	
	/**
	 * 实际观察到的snp或者indel
	 * The sequences of the observed alleles from rs-fasta files
	 */
	public void setObserved(String observed) {
		this.observed = observed;
	}
	/**
	 * 实际观察到的snp或者indel
	 * The sequences of the observed alleles from rs-fasta files
	 */
	public String getObserved() {
		return observed;
	}
	/**
	 * 只有当refUCSC和refNCBI不一样的时候才会有内容
	 */
	public void setRefNCBI(String refNCBI) {
		this.refNCBI = refNCBI;
	}
	/**
	 * 只有当refUCSC和refNCBI不一样的时候才会有内容
	 */
	public String getRefNCBI() {
		return refNCBI;
	}
	/**
	 * 暂时不用
	 */
	public void setScore(int score) {
		this.score = score;
	}
	/**
	 * 暂时不用
	 */
	public int getScore() {
		return score;
	}
	
	/**
	 * dbSNP Reference SNP (rs) identifier
	 */
	public void setSnpRsID(String snpRsID) {
		this.snpRsID = snpRsID;
	}
	/**
	 * dbSNP Reference SNP (rs) identifier
	 */
	public String getSnpRsID() {
		if (snpRsID == null) {
			return "";
		}
		return snpRsID;
	}

	/**
	 * Which DNA strand contains the observed alleles
	 * + , - 和""
	 */
	public void setStrand(String strand) {
		this.strand = strand;
	}
	/**
	 * Which DNA strand contains the observed alleles
	 */
	public String getStrand() {
		return strand;
	}
	
	/**
	 * Number of distinct submitter handles for submitted SNPs for this ref SNP
	 */
	public void setSubmitterCount(int submitterCount) {
		this.submitterCount = submitterCount;
	}
	/**
	 * Number of distinct submitter handles for submitted SNPs for this ref SNP
	 */
	public int getSubmitterCount() {
		return submitterCount;
	}

	/**
	 * 提交者List of submitter handles
	 * 逗号隔开，最后有一个逗号
	 */
	public void setSubmitters(String submitters) {
		this.submitters = submitters;
	}
	/**
	 * 提交者List of submitter handles
	 * 逗号隔开，最后有一个逗号
	 */
	public String getSubmitters() {
		return submitters;
	}
	
	/**
	 * Class of variant (single, in-del, named, mixed, etc.)
	 * enum('unknown', 'single', 'in-del', 'het', 'microsatellite', 'named', 'mixed', 'mnp', 'insertion', 'deletion')
	 * 具体是什么类型
	 * indel还是snp还是其他
	 * 同UCSC的class
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * Class of variant (single, in-del, named, mixed, etc.)
	 * enum('unknown', 'single', 'in-del', 'het', 'microsatellite', 'named', 'mixed', 'mnp', 'insertion', 'deletion')
	 * 具体是什么类型
	 * indel还是snp还是其他
	 * 同UCSC的class
	 */
	public String getType() {
		return type;
	}

	/**
	 * Validation status of the SNP
	 * set('unknown', 'by-cluster', 'by-frequency', 'by-submitter', 'by-2hit-2allele', 'by-hapmap', 'by-1000genomes')
	 */
	public void setValid(String valid) {
		this.valid = valid;
	}
	/**
	 * Validation status of the SNP
	 * set('unknown', 'by-cluster', 'by-frequency', 'by-submitter', 'by-2hit-2allele', 'by-hapmap', 'by-1000genomes')
	 */
	public String getValid() {
		return valid;
	}

	/**
	 * The quality of the alignment: 1 = unique mapping, 2 = non-unique, 3 = many matches
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	/**
	 * The quality of the alignment: 1 = unique mapping, 2 = non-unique, 3 = many matches
	 */
	public int getWeight() {
		return weight;
	}
	
}
