package com.novelbio.analysis.seq.snphgvs;

import com.novelbio.base.StringOperate;
/**
 * 这个是snpeff使用的变异类型，来源于
 * http://sequenceontology.org/browser/current_svn/term/SO:0001792
 * 
 * @author novelbio
 *
 */
public enum EnumVariantClass {
	//chromosome_number_variation,
	exon_loss_variant,
	frameshift_variant,
	//===== 这几个最好在氨基酸突变的时候看========
	stop_gained,
	stop_lost,
	start_lost,
	//==========================================
	splice_acceptor_variant,
	splice_donor_variant,
	//splice_branch_variant,
	splice_region_variant,
	
	//rare_amino_acid_variant,
	missense_variant,
	disruptive_inframe_insertion,
	conservative_inframe_insertion,
	disruptive_inframe_deletion,
	conservative_inframe_deletion,

	stop_retained_variant,
	initiator_codon_variant,
	//non_canonical_start_codon,
	synonymous_variant,
	coding_sequence_variant,
	Five_prime_UTR_variant("5_prime_UTR_variant"),
	Three_prime_UTR_variant("3_prime_UTR_variant"),
	//Five_prime_UTR_premature_start_codon_gain_variant("5_prime_UTR_premature_start_codon_gain_variant"),
	upstream_gene_variant,
	downstream_gene_variant,
	//TF_binding_site_variant,
	//regulatory_region_variant,
	miRNA,
	//custom, 
	//sequence_feature,
	////=====//conserved_intron_variant,
	intron_variant,
	//intragenic_variant,
	//conserved_intergenic_variant,
	intergenic_region,
	non_coding_transcript_variant,
	non_coding_transcript_exon_variant,
	non_coding_transcript_intron_variant,
	
	//gene_variant,
	//chromosome,
	/** 自己加的，意思在基因外部没啥变化 */
	None;
	
	String name;
	private EnumVariantClass(String name) {
		this.name = name;
	}
	private EnumVariantClass() {}
	public String toString() {
		if (!StringOperate.isRealNull(name)) {
			return name;
		}
		return super.toString();
	}
}
