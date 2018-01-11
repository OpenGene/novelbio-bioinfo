package com.novelbio.analysis.seq.snphgvs;

import java.util.HashSet;
import java.util.Set;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;

import smile.math.Math;

public abstract class VariantTypeDetermine {
	GffGeneIsoInfo iso;
	/** 不考虑方向，start < end */
	int start;
	int end;
	
	/** start在哪个exon/intron上 */
	int startNum;
	/** startNum如果为0，start是不是在iso的5-端外 */
	boolean startBeforeIsoStrand;
	int endNum;
	/** endNum如果为0，end是不是在iso的5-端外 */
	boolean endBeforeIsoStrand;
	
	
	public abstract boolean isVarClass();
	
	protected boolean isCoordInRegion(int coord, int[] region) {
		return coord >= Math.min(region[0], region[1]) &&  coord <= Math.max(region[0], region[1]);
	}
	
	/**
	 * 给定区段
	 * @param align
	 * @param iso
	 * @return
	 */
	public static Set<EnumVariantClass> getSetVarType(EnumHgvsVarType varType, Align align, GffGeneIsoInfo iso) {
		return new HashSet<>();
	}
}

/** {@link EnumVariantClass#exon_loss_variant} */
class ExonLossVar extends VariantTypeDetermine {
	@Override
	public boolean isVarClass() {
		if (startNum == 0 && endNum == 0) {
			return start < iso.getStartAbs() && end > iso.getEndAbs();
		}
		// 一头在iso外面，一头在iso里面的情况
		if (startNum == 0) {
			if (startBeforeIsoStrand && (endNum > 1 || endNum < 0)) {
				return true;
			} else if (!startBeforeIsoStrand && endNum < iso.getLen()) {
				return true;
			}
			return false;
		} else if (endNum == 0) {
			if (!endBeforeIsoStrand && startNum < iso.getLen()) {
				return true;
			} else if (endBeforeIsoStrand && (startNum > 1 || startNum < 0)) {
				return true;
			}
			return false;
		}
		//两头都在iso里面的情况
		if (startNum == endNum) {
			return false;
		}
		
		if (startNum<0 && endNum < 0) {
			return true;
		} else if (startNum * endNum < 0 ) {
			int exonNum = Math.abs(Math.max(startNum, endNum));
			int intronNum = Math.abs(Math.min(startNum, endNum));
			if (intronNum > exonNum || exonNum - intronNum > 1) {
				return true;
			}
			return false;
		} else if (startNum > 0 && endNum > 0) {
			if (Math.abs(startNum-endNum) > 1) {
				return true;
			}
			return false;
		}
		return false;
	}
}

class FrameShiftVar extends VariantTypeDetermine {
	@Override
	public boolean isVarClass() {
		return false;
	}
	
}

/**
 * 这个是snpeff使用的变异类型，来源于
 * http://sequenceontology.org/browser/current_svn/term/SO:0001792
 * @author novelbio
 *
 */
enum EnumVariantClass {
	chromosome_number_variation,
	exon_loss_variant,
	frameshift_variant,
	stop_gained,
	stop_lost,
	start_lost,
	splice_acceptor_variant,
	splice_donor_variant,
	rare_amino_acid_variant,
	missense_variant,
	disruptive_inframe_insertion,
	conservative_inframe_insertion,
	disruptive_inframe_deletion,
	conservative_inframe_deletion,
	Five_prime_UTR_truncation_And_exon_loss_variant,
	Three_prime_UTR_truncation_And_exon_loss,
	splice_branch_variant,
	splice_region_variant,
	stop_retained_variant,
	initiator_codon_variant,
	synonymous_variant,
	initiator_codon_variant_And_non_canonical_start_codon,
	coding_sequence_variant,
	Five_prime_UTR_variant,
	Three_prime_UTR_variant,
	Five_prime_UTR_premature_start_codon_gain_variant,
	upstream_gene_variant,
	downstream_gene_variant,
	TF_binding_site_variant,
	regulatory_region_variant,
	miRNA,
	custom,
	sequence_feature,
	conserved_intron_variant,
	intron_variant,
	intragenic_variant,
	conserved_intergenic_variant,
	intergenic_region,
	non_coding_exon_variant,
	nc_transcript_variant,
	gene_variant,
	chromosome,
	/** 自己加的，意思在基因外部没啥变化 */
	None,
	
}
