package com.novelbio.analysis.seq.snphgvs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.StringOperate;

import smile.math.Math;

public abstract class VariantTypeDetector {
	GffGeneIsoInfo iso;
	EnumHgvsVarType varType;
	/** 不考虑方向，start < end */
	int start;
	int end;
	
	Set<EnumVariantClass> setVariantClass = new LinkedHashSet<>();
	
	public void setInfo(GffGeneIsoInfo iso, SnpInfo snpInfo) {
		this.iso = iso;
		this.varType = snpInfo.getVarType();
		this.start = snpInfo.getStartReal();
		this.end = snpInfo.getEndReal();
	}
	
	protected boolean isCoordInRegion(int coord, int[] region) {
		return coord >= Math.min(region[0], region[1]) && coord <= Math.max(region[0], region[1]);
	}
	
	public abstract void fillVarClass();
	
	public Set<EnumVariantClass> getSetVariantClass() {
		return setVariantClass;
	}
	
	/**
	 * 给定区段
	 * @param align
	 * @param iso
	 * @return
	 */
	public static Set<EnumVariantClass> getSetVarType(GffGeneIsoInfo iso, SnpInfo snpInfo) {
		List<VariantTypeDetector> lsVariantTypeDetectors = new ArrayList<>();
		lsVariantTypeDetectors.add(new ExonLossVaration());
		lsVariantTypeDetectors.add(new UtrVariant());
		lsVariantTypeDetectors.add(new SpliceVariant());
		for (VariantTypeDetector variantTypeDetector : lsVariantTypeDetectors) {
			variantTypeDetector.setInfo(iso, snpInfo);
		}
		
		Set<EnumVariantClass> setVarResult = new HashSet<>();
		for (VariantTypeDetector variantTypeDetector : lsVariantTypeDetectors) {
			variantTypeDetector.fillVarClass();
			setVarResult.addAll(variantTypeDetector.getSetVariantClass());
		}
		return setVarResult;
	}
	
	/** 合并Var注释 */
	public static String mergeVars(Set<EnumVariantClass> setVarResult) {
		StringBuilder sBuilder = new StringBuilder();
		int i = 0;
		for (EnumVariantClass enumVariantClass : setVarResult) {
			if (i++ > 0) {
				sBuilder.append("&");
			}
			sBuilder.append(enumVariantClass.toString());
		}
		return sBuilder.toString();
	}
	
}
/** {@link EnumVariantClass#exon_loss_variant} */
class ExonLossVaration extends VariantTypeDetector {
	@Override
	public void fillVarClass() {
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
		if( isHaveStart&&isHaveEnd) {
			setVariantClass.add(EnumVariantClass.exon_loss_variant);
		}
	}
	
}

/** {@link EnumVariantClass#exon_loss_variant} */
class UtrVariant extends VariantTypeDetector {
	@Override
	public void fillVarClass() {
		GffGeneIsoInfo isoSub = iso.getSubGffGeneIso(start, end);
		int startNum = iso.getNumCodInEle(start);
		int endNum = iso.getNumCodInEle(end);
		if (startNum == endNum && startNum < 0) {
			if (iso.ismRNAFromCds()) {
				setVariantClass.add(EnumVariantClass.intron_variant);
			} else {
				setVariantClass.add(EnumVariantClass.non_coding_transcript_intron_variant);
			}
		}
		if (!iso.ismRNAFromCds() && startNum == endNum && startNum > 0) {
			setVariantClass.add(EnumVariantClass.non_coding_transcript_exon_variant);
		}
		if (iso.isCis5to3() && start < iso.getStartAbs()) {
			setVariantClass.add(EnumVariantClass.upstream_gene_variant);
		} else if (!iso.isCis5to3() && start < iso.getStartAbs()) {
			setVariantClass.add(EnumVariantClass.downstream_gene_variant);
		}
		if (iso.isCis5to3() && end > iso.getEndAbs()) {
			setVariantClass.add(EnumVariantClass.downstream_gene_variant);
		} else if (!iso.isCis5to3() && end > iso.getEndAbs()) {
			setVariantClass.add(EnumVariantClass.upstream_gene_variant);
		}
		if (!iso.ismRNAFromCds()) {
			return;
		}
		for (ExonInfo exonInfo : isoSub) {
			if (iso.isCis5to3()) {
				if (exonInfo.getStartAbs() < iso.getATGsite()) {
					setVariantClass.add(EnumVariantClass.Five_prime_UTR_variant);
					if (startNum <= 0 || endNum <= 0) {
						setVariantClass.add(EnumVariantClass.exon_loss_variant);
					}
				}
				if (exonInfo.getEndAbs() > iso.getUAGsite()) {
					setVariantClass.add(EnumVariantClass.Three_prime_UTR_variant);
					if (startNum <= 0 || endNum <= 0) {
						setVariantClass.add(EnumVariantClass.exon_loss_variant);
					}
				}
			} else {
				if (exonInfo.getEndAbs() > iso.getATGsite()) {
					setVariantClass.add(EnumVariantClass.Five_prime_UTR_variant);
					if (startNum <= 0 || endNum <= 0) {
						setVariantClass.add(EnumVariantClass.exon_loss_variant);
					}
				}
				if (exonInfo.getStartAbs() < iso.getUAGsite()) {
					setVariantClass.add(EnumVariantClass.Three_prime_UTR_variant);
					if (startNum <= 0 || endNum <= 0) {
						setVariantClass.add(EnumVariantClass.exon_loss_variant);
					}
				}
			}
		}
	}
	
}

/** {@link EnumVariantClass#frameshift_variant} */
class SpliceVariant extends VariantTypeDetector {
	@Override
	public void fillVarClass() {
		int[] startEnd = new int[] {start, end};
		boolean isSpliceAcceptor = false, isSpliceDonor = false, isSpliceRegion = false;
		for (int i = 0; i < iso.getLen(); i++) {
			ExonInfo exonInfo = iso.get(i);
			if (end < exonInfo.getStartAbs() -10) break;
			if (start > exonInfo.getEndAbs() + 10) continue;
			int[] spliceAcceptor = null, spliceDonor = null;
			int[] spliceRegion1 = null, spliceRegion2 = null;
						
			if (iso.isCis5to3()) {
				if (i > 0) {
					spliceAcceptor = new int[]{exonInfo.getStartCis()-2, exonInfo.getStartCis()-1};
					spliceRegion1 = new int[] {exonInfo.getStartCis(), exonInfo.getStartCis()+2};
					spliceRegion2 = new int[] {exonInfo.getStartCis()-8, exonInfo.getStartCis()-3};
					isSpliceRegion = isSpliceRegion || isRegionOverlap(spliceRegion1, startEnd) || isRegionOverlap(spliceRegion2, startEnd);
				}
				if (i < iso.getLen()-1) {
					spliceDonor = new int[]{exonInfo.getEndCis()+1, exonInfo.getEndCis()+2};
					spliceRegion1 = new int[] {exonInfo.getEndCis()-2, exonInfo.getEndCis()};
					spliceRegion2 = new int[] {exonInfo.getEndCis()+3, exonInfo.getEndCis()+8};
					isSpliceRegion = isSpliceRegion || isRegionOverlap(spliceRegion1, startEnd) || isRegionOverlap(spliceRegion2, startEnd);
				}
			} else {
				if (i > 0) {
					spliceDonor = new int[]{exonInfo.getStartAbs()-2, exonInfo.getStartAbs()-1};
					spliceRegion1 = new int[] {exonInfo.getStartAbs(), exonInfo.getStartAbs()+2};
					spliceRegion2 = new int[] {exonInfo.getStartAbs()-8, exonInfo.getStartAbs()-3};
					isSpliceRegion = isSpliceRegion || isRegionOverlap(spliceRegion1, startEnd) || isRegionOverlap(spliceRegion2, startEnd);
				}
				if (i < iso.getLen()-1) {
					spliceAcceptor = new int[]{exonInfo.getEndAbs()+1, exonInfo.getEndAbs()+2};
					spliceRegion1 = new int[] {exonInfo.getEndAbs()-2, exonInfo.getEndAbs()};
					spliceRegion2 = new int[] {exonInfo.getEndAbs()+3, exonInfo.getEndAbs()+8};
					isSpliceRegion = isSpliceRegion || isRegionOverlap(spliceRegion1, startEnd) || isRegionOverlap(spliceRegion2, startEnd);
				}
			}
			isSpliceAcceptor = isSpliceAcceptor || isRegionOverlap(spliceAcceptor, startEnd);
			isSpliceDonor = isSpliceDonor || isRegionOverlap(spliceDonor, startEnd);
		}
		if (isSpliceAcceptor) {
			setVariantClass.add(EnumVariantClass.splice_acceptor_variant);
		}
		if (isSpliceDonor) {
			setVariantClass.add(EnumVariantClass.splice_donor_variant);
		}
		if (isSpliceRegion) {
			setVariantClass.add(EnumVariantClass.splice_region_variant);
		}
	}
	
	private boolean isRegionOverlap(int[] region, int[] startEnd) {
		if (region == null) {
			return false;
		}
		boolean isOverlap = false;
		if (varType != EnumHgvsVarType.Insertions) {
			if (startEnd[0] <= region[1] && startEnd[1] >= region[0]) {
				isOverlap = true;
			}
		} else {
			if (startEnd[0] < region[1] && startEnd[1] > region[0]) {
				isOverlap = true;
			}
		}
		return isOverlap;
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
