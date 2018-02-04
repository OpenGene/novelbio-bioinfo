package com.novelbio.analysis.seq.snphgvs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.MathUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.StringOperate;

import smile.math.Math;

public abstract class VariantTypeDetermine {
	GffGeneIsoInfo iso;
	List<ExonInfo> lsExons;
	
	SnpInfo snpRefAltInfo;
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
	
	EnumHgvsVarType varType;
	
	public abstract boolean isVarClass();

	protected boolean isCoordInRegion(int coord, int[] region) {
		return coord >= Math.min(region[0], region[1]) && coord <= Math.max(region[0], region[1]);
	}

	/**
	 * 给定区段
	 * 
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
		boolean isHaveStart = false, isHaveEnd = false;
		for (ExonInfo exonInfo : iso) {
			if (start > exonInfo.getEndAbs()) {
				continue;
			}
			if (end < exonInfo.getStartAbs()) {
				break;
			}
			if (start <= exonInfo.getStartAbs()) {
				isHaveStart = true;
			}
			if (end >= exonInfo.getEndAbs()) {
				isHaveEnd = true;
			}
			if (isHaveStart && isHaveEnd) {
				break;
			}
		}
		return isHaveStart&&isHaveEnd;
	}
	
}

/** {@link EnumVariantClass#frameshift_variant} */
class SpliceVariant extends VariantTypeDetermine {
	@Override
	public boolean isVarClass() {
		boolean isSpliceAcceptor = false, isSplice
		for (ExonInfo exonInfo : iso) {
			if (start <= exonInfo.getEndCis()+2 && endBeforeIsoStrand >= exonInfo.getEndCis()) {
				continue;
			}
			if (end < exonInfo.getStartAbs()) {
				break;
			}
			if (start <= exonInfo.getStartAbs()) {
				isHaveStart = true;
			}
			if (end >= exonInfo.getEndAbs()) {
				isHaveEnd = true;
			}
			if (isHaveStart && isHaveEnd) {
				break;
			}
		}
		return isHaveStart&&isHaveEnd;
	}
}

/** {@link EnumVariantClass#frameshift_variant} */
class FrameShiftVar extends VariantTypeDetermine {
	@Override
	public boolean isVarClass() {
		int[] range = getValidRange();
		if(range == null) return false;
		
		if (varType == EnumHgvsVarType.Substitutions) {
			return false;
		}
		
		if (varType == EnumHgvsVarType.Insertions 
				|| varType == EnumHgvsVarType.Duplications
				) {
			return iso.isCodInAAregion(start) && snpRefAltInfo.getSeqAlt().length() %3 != 0;
		}
		
		List<ExonInfo> lsExons = iso.getRangeIsoOnExon(range[0], range[1]);
		int totalLength = lsExons.stream()
				.map(it -> it.getLength())
				.reduce(0, (result, element) -> result + element);
		return totalLength % 3 == 0;
	}
	
	private int[] getValidRange() {
		int[] coords = new int[] {start, end};
		int[] atguag = new int[] {Math.min(iso.getATGsite(), iso.getUAGsite()), Math.max(iso.getATGsite(), iso.getUAGsite())};
		int[] result = new int[] {Math.max(coords[0], atguag[0]), Math.min(coords[1], atguag[1])};
		if(result[1] < result[0]) {
			return null;
		}
		return result;
	}
}

/**
 * 这个是snpeff使用的变异类型，来源于
 * http://sequenceontology.org/browser/current_svn/term/SO:0001792
 * 
 * @author novelbio
 *
 */
enum EnumVariantClass {
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
	intragenic_variant,
	//conserved_intergenic_variant,
	intergenic_region,
	non_coding_exon_variant,
	nc_transcript_variant,
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
