package com.novelbio.software.snpanno;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.software.snpanno.SnpInfo.EnumHgvsVarType;

import smile.math.Math;

public abstract class VariantTypeDetector {
	GffIso iso;
	EnumHgvsVarType varType;
	/** 不考虑方向，start < end */
	int start;
	int end;
	
	Set<EnumVariantClass> setVariantClass = new LinkedHashSet<>();
	
	public void setInfo(GffIso iso, SnpInfo snpInfo) {
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
	public static Set<EnumVariantClass> getSetVarType(GffIso iso, SnpInfo snpInfo) {
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
			if (iso.isCis5to3()) {
				if (start > exonInfo.getEndAbs()) {
					continue;
				}
				if (end < exonInfo.getStartAbs()) {
					break;
				}
			} else {
				if (end < exonInfo.getStartAbs()) {
					continue;
				}
				if (start > exonInfo.getEndAbs()) {
					break;
				}
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
		GffIso isoSub = iso.getSubGffGeneIso(start, end);
		int startNum = iso.getNumCodInEle(start);
		int endNum = iso.getNumCodInEle(end);
		if (startNum < 0 || endNum < 0) {
			if ((varType == EnumHgvsVarType.Insertions || varType == EnumHgvsVarType.Duplications)
					&&
					startNum*endNum < 0 && Math.abs(start-end) == 1) {
				//TODO 这种就是在exon边界处插入，这种不引起intron变化
			} else {
				if (iso.ismRNAFromCds()) {
					setVariantClass.add(EnumVariantClass.intron_variant);
				} else {
					setVariantClass.add(EnumVariantClass.non_coding_transcript_intron_variant);
				}
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
//					if (startNum <= 0 || endNum <= 0) {
//						setVariantClass.add(EnumVariantClass.exon_loss_variant);
//					}
				}
				if (exonInfo.getEndAbs() > iso.getUAGsite()) {
					setVariantClass.add(EnumVariantClass.Three_prime_UTR_variant);
//					if (startNum <= 0 || endNum <= 0) {
//						setVariantClass.add(EnumVariantClass.exon_loss_variant);
//					}
				}
			} else {
				if (exonInfo.getEndAbs() > iso.getATGsite()) {
					setVariantClass.add(EnumVariantClass.Five_prime_UTR_variant);
//					if (startNum <= 0 || endNum <= 0) {
//						setVariantClass.add(EnumVariantClass.exon_loss_variant);
//					}
				}
				if (exonInfo.getStartAbs() < iso.getUAGsite()) {
					setVariantClass.add(EnumVariantClass.Three_prime_UTR_variant);
//					if (startNum <= 0 || endNum <= 0) {
//						setVariantClass.add(EnumVariantClass.exon_loss_variant);
//					}
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
		for (int i = 0; i < iso.size(); i++) {
			ExonInfo exonInfo = iso.get(i);
			
			if (iso.isCis5to3()) {
				if (start > exonInfo.getEndAbs() + 10) {
					continue;
				}
				if (end < exonInfo.getStartAbs() - 10) {
					break;
				}
			} else {
				if (end < exonInfo.getStartAbs() - 10) {
					continue;
				}
				if (start > exonInfo.getEndAbs() + 10) {
					break;
				}
			}
			
			int[] spliceAcceptor = null, spliceDonor = null;
			int[] spliceRegion1 = null, spliceRegion2 = null;
						
			if (iso.isCis5to3()) {
				if (i > 0) {
					spliceAcceptor = new int[]{exonInfo.getStartCis()-2, exonInfo.getStartCis()-1};
					spliceRegion1 = new int[] {exonInfo.getStartCis(), exonInfo.getStartCis()+2};
					spliceRegion2 = new int[] {exonInfo.getStartCis()-8, exonInfo.getStartCis()-3};
					isSpliceRegion = isSpliceRegion || isRegionOverlap(spliceRegion1, startEnd, false , true) || isRegionOverlap(spliceRegion2, startEnd, true, false);
				}
				if (i < iso.getLen()-1) {
					spliceDonor = new int[]{exonInfo.getEndCis()+1, exonInfo.getEndCis()+2};
					spliceRegion1 = new int[] {exonInfo.getEndCis()-2, exonInfo.getEndCis()};
					spliceRegion2 = new int[] {exonInfo.getEndCis()+3, exonInfo.getEndCis()+8};
					isSpliceRegion = isSpliceRegion || isRegionOverlap(spliceRegion1, startEnd, true, false) || isRegionOverlap(spliceRegion2, startEnd, false , true);
				}
			} else {
				if (i > 0) {
					spliceDonor = new int[]{exonInfo.getStartAbs()-2, exonInfo.getStartAbs()-1};
					spliceRegion1 = new int[] {exonInfo.getStartAbs(), exonInfo.getStartAbs()+2};
					spliceRegion2 = new int[] {exonInfo.getStartAbs()-8, exonInfo.getStartAbs()-3};
					isSpliceRegion = isSpliceRegion || isRegionOverlap(spliceRegion1, startEnd, false, true) || isRegionOverlap(spliceRegion2, startEnd, true, false);
				}
				if (i < iso.getLen()-1) {
					spliceAcceptor = new int[]{exonInfo.getEndAbs()+1, exonInfo.getEndAbs()+2};
					spliceRegion1 = new int[] {exonInfo.getEndAbs()-2, exonInfo.getEndAbs()};
					spliceRegion2 = new int[] {exonInfo.getEndAbs()+3, exonInfo.getEndAbs()+8};
					isSpliceRegion = isSpliceRegion || isRegionOverlap(spliceRegion1, startEnd, true, false) || isRegionOverlap(spliceRegion2, startEnd, false , true);
				}
			}
			isSpliceAcceptor = isSpliceAcceptor || isRegionOverlap(spliceAcceptor, startEnd, false, false);
			isSpliceDonor = isSpliceDonor || isRegionOverlap(spliceDonor, startEnd, false, false);
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
	
	private boolean isRegionOverlap(int[] region, int[] startEnd, boolean isInsertionStart, boolean isInsertionEnd) {
		if (region == null) {
			return false;
		}
		boolean isOverlap = false;
		if (varType != EnumHgvsVarType.Insertions && varType != EnumHgvsVarType.Duplications) {
			if (startEnd[0] <= region[1] && startEnd[1] >= region[0]) {
				isOverlap = true;
			}
		} else {
			if (isSmaller(startEnd[0], region[1], isInsertionStart) && isBigger(startEnd[1], region[0], isInsertionEnd)) {
				isOverlap = true;
			}
		}
		return isOverlap;
	}
	
	private boolean isSmaller(int a, int b, boolean isEqual) {
		return isEqual ? a<=b : a<b;
	}
	private boolean isBigger(int a, int b, boolean isEqual) {
		return isEqual ? a>=b : a>b;
	}
}
