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
		List<ExonInfo> lsExons = iso.getRangeIsoOnExon(start, end);
		if (lsExons.isEmpty()) {
			return false;
		}
		int exon1 = startNum > 0 ? 1 : 0;
		int exon2 = endNum > 0 ? 1 : 0;
		int num = lsExons.size() - exon1 - exon2;
		return num > 1;
	}
}

/** {@link EnumVariantClass#frameshift_variant} */
class FrameShiftVar extends VariantTypeDetermine {
	@Override
	public boolean isVarClass() {
		Range<Integer> result = getValidRange();
		List<ExonInfo> lsExons = getValidExonList(result);
		int totalLength = getTotalLength(lsExons);
		return totalLength % 3 == 0;
	}

	private int getTotalLength(List<ExonInfo> lsExons) {
		int totalLength = lsExons.stream().map(it -> it.getLength()).reduce(0, (pre, cur) -> pre + cur);
		return totalLength;
	}

	private List<ExonInfo> getValidExonList(Range<Integer> result) {
		List<ExonInfo> lsExons = iso.getRangeIsoOnExon(result.lowerEndpoint(), result.upperEndpoint());
		return lsExons;
	}

	private Range<Integer> getValidRange() {
		List<Integer> lsCoordinate = Lists.newArrayList(iso.getATGsite(), iso.getUAGsite());
		Range<Integer> range1 = Range.closed(Collections.min(lsCoordinate), Collections.max(lsCoordinate));
		Range<Integer> range2 = Range.closed(this.start, this.end);
		Range<Integer> result = range1.intersection(range2);
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
	chromosome_number_variation, exon_loss_variant, frameshift_variant, stop_gained, stop_lost, start_lost, splice_acceptor_variant, splice_donor_variant, rare_amino_acid_variant, missense_variant, disruptive_inframe_insertion, conservative_inframe_insertion, disruptive_inframe_deletion, conservative_inframe_deletion, Five_prime_UTR_truncation_And_exon_loss_variant, Three_prime_UTR_truncation_And_exon_loss, splice_branch_variant, splice_region_variant, stop_retained_variant, initiator_codon_variant, synonymous_variant, initiator_codon_variant_And_non_canonical_start_codon, coding_sequence_variant, Five_prime_UTR_variant, Three_prime_UTR_variant, Five_prime_UTR_premature_start_codon_gain_variant, upstream_gene_variant, downstream_gene_variant, TF_binding_site_variant, regulatory_region_variant, miRNA, custom, sequence_feature, conserved_intron_variant, intron_variant, intragenic_variant, conserved_intergenic_variant, intergenic_region, non_coding_exon_variant, nc_transcript_variant, gene_variant, chromosome,
	/** 自己加的，意思在基因外部没啥变化 */
	None,

}
