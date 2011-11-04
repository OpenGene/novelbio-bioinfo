package com.novelbio.database.domain.geneanno;

import com.novelbio.analysis.tools.Mas3.getProbID;

/**
 * UCSC��SNP��Ϣ
 * @author zong0jie
 *
 */
public class SnpIndelRs {
	int taxID = 0;
	String chrID;
	/**
	 * snp����indel�����
	 */
	int locStart;
	/**
	 * snp����indel���յ�
	 */
	int locEnd;
	/**
	 * dbSNP Reference SNP (rs) identifier
	 */
	String snpRsID;
	/**
	 * ��ʱ����
	 */
	int score = 0;
	/**
	 * Which DNA strand contains the observed alleles
	 */
	String strand = "";
	/**
	 * ֻ�е�refUCSC��refNCBI��һ����ʱ��Ż�������
	 */
	String refNCBI;;
//	String refUCSC = "";
	/**
	 * ʵ�ʹ۲쵽��snp����indel
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
	 * ������ʲô����
	 * indel����snp��������
	 * ͬUCSC��class
	 */
	String type;
	/**
	 * Validation status of the SNP
	 * set('unknown', 'by-cluster', 'by-frequency', 'by-submitter', 'by-2hit-2allele', 'by-hapmap', 'by-1000genomes')
	 */
	String valid;
	/**
	 *  Average heterozygosity from all observations. Note: may be computed on small number of samples.
	 * ƽ���Ӻ���
	 */
	double avHet = -1;
	/**
	 * Standard Error for the average heterozygosity
	 * ƽ���Ӻ��ʱ�׼��
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
	 * �ύ��List of submitter handles
	 * ���Ÿ����������һ������
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
	 * ����1.000000,1.000000,�ȣ��ö��Ÿ�����double
	 */
	String alleleNs;
	/**
	* Allele frequencies
	* ����0.5000000,0.5000000,�ȣ��ö��Ÿ�����double
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
	* ����0.5000000,0.5000000,�ȣ��ö��Ÿ�����double
	*/
	public void setAlleleFreqs(String alleleFreqs) {
		this.alleleFreqs = alleleFreqs;
	}
	/**
	* Allele frequencies
	* ����0.5000000,0.5000000,�ȣ��ö��Ÿ�����double
	*/
	public String getAlleleFreqs() {
		return alleleFreqs;
	}
	/**
	 * Count of chromosomes (2N) on which each allele was observed.
	 * Note: this is extrapolated by dbSNP from submitted frequencies and total sample 2N, and is not always an integer.
	 * ����1.000000,1.000000,�ȣ��ö��Ÿ�����double
	 */
	public void setAlleleNs(String alleleNs) {
		this.alleleNs = alleleNs;
	}
	/**
	 * Count of chromosomes (2N) on which each allele was observed.
	 * Note: this is extrapolated by dbSNP from submitted frequencies and total sample 2N, and is not always an integer.
	 * ����1.000000,1.000000,�ȣ��ö��Ÿ�����double
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
	 * ƽ���Ӻ���
	 * С��0��ʾû��
	 */
	public void setAvHet(double avHet) {
		this.avHet = avHet;
	}
	/**
	 *  Average heterozygosity from all observations. Note: may be computed on small number of samples.
	 * ƽ���Ӻ���
	 * С��0��ʾû��
	 */
	public double getAvHet() {
		return avHet;
	}
	/**
	 * Standard Error for the average heterozygosity
	 * ƽ���Ӻ��ʱ�׼��
	 * С��0��ʾû��
	 */
	public void setAvHetSE(double avHetSE) {
		this.avHetSE = avHetSE;
	}
	/**
	 * Standard Error for the average heterozygosity
	 * ƽ���Ӻ��ʱ�׼��
	 * С��0��ʾû��
	 */
	public double getAvHetSE() {
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
	 * snp������Ⱦɫ����
	 * @param chrID
	 */
	public void setChrID(String chrID) {
		this.chrID = chrID;
	}
	/**
	 * snp������Ⱦɫ����
	 * @param chrID
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
	 * snp����indel���յ�
	 */
	public void setLocEnd(int locEnd) {
		this.locEnd = locEnd;
	}
	/**
	 * snp����indel���յ�
	 */
	public int getLocEnd() {
		return locEnd;
	}
	
	/**
	 * snp����indel�����
	 */
	public void setLocStart(int locStart) {
		this.locStart = locStart;
	}
	/**
	 * snp����indel�����
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
	 * ʵ�ʹ۲쵽��snp����indel
	 * The sequences of the observed alleles from rs-fasta files
	 */
	public void setObserved(String observed) {
		this.observed = observed;
	}
	/**
	 * ʵ�ʹ۲쵽��snp����indel
	 * The sequences of the observed alleles from rs-fasta files
	 */
	public String getObserved() {
		return observed;
	}
	/**
	 * ֻ�е�refUCSC��refNCBI��һ����ʱ��Ż�������
	 */
	public void setRefNCBI(String refNCBI) {
		this.refNCBI = refNCBI;
	}
	/**
	 * ֻ�е�refUCSC��refNCBI��һ����ʱ��Ż�������
	 */
	public String getRefNCBI() {
		return refNCBI;
	}
	/**
	 * ��ʱ����
	 */
	public void setScore(int score) {
		this.score = score;
	}
	/**
	 * ��ʱ����
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
		return snpRsID;
	}

	/**
	 * Which DNA strand contains the observed alleles
	 * + , - ��""
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
	 * �ύ��List of submitter handles
	 * ���Ÿ����������һ������
	 */
	public void setSubmitters(String submitters) {
		this.submitters = submitters;
	}
	/**
	 * �ύ��List of submitter handles
	 * ���Ÿ����������һ������
	 */
	public String getSubmitters() {
		return submitters;
	}
	
	/**
	 * Class of variant (single, in-del, named, mixed, etc.)
	 * enum('unknown', 'single', 'in-del', 'het', 'microsatellite', 'named', 'mixed', 'mnp', 'insertion', 'deletion')
	 * ������ʲô����
	 * indel����snp��������
	 * ͬUCSC��class
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * Class of variant (single, in-del, named, mixed, etc.)
	 * enum('unknown', 'single', 'in-del', 'het', 'microsatellite', 'named', 'mixed', 'mnp', 'insertion', 'deletion')
	 * ������ʲô����
	 * indel����snp��������
	 * ͬUCSC��class
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
