package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.analysis.seq.genome.gffOperate.EnumMrnaLocate;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.resequencing.SnpRefAltInfo.SnpIndelType;
import com.novelbio.base.ExceptionNbcParamError;


/**
 * 对于单个位点的snp与indel的情况，可以保存多个不同的样本。
 * 在setSampleName()方法中可设定样本名，并获得该样本的信息。
 * @author zong0jie
 */
public abstract class SnpRefAltIso {
	/** 与剪接位点距离的绝对值，小于该距离才会考虑剪接位点的影响 */
	static int splitRegion = 2;
	
	SnpRefAltInfo snpRefAltInfo;
	GffGeneIsoInfo iso;
	
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	SeqFasta refSeqIntactAA = new SeqFasta();
	
	EnumMrnaLocate enumMrnaLocate;
	
	SplitType splitType = SplitType.NONE;
	int splitDistance = 0;
	
	/** 需要将alt替换ref的碱基，这里记录替换ref的起点 */
	int snpOnReplaceLocStart;
	/** 需要将alt替换ref的碱基，这里记录替换ref的终点 */
	int snpOnReplaceLocEnd;
	
	public SnpRefAltIso(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		this.snpRefAltInfo = snpRefAltInfo;
		this.iso = iso;
	}
	/** 移码突变，移了几位，一般只有1，2两个。因为三联密码子  */
	public abstract int getOrfShift();
	
	/**
	 * 跟方向相关
	 * 给定序列和起始位点，用snp位点去替换序列，同时将本次替换是否造成移码写入orfshift
	 * @param thisSeq 给定序列--该序列必须是正向，然后
	 * @param cis5to3 给定序列的正反向
	 * @param startLoc  实际位点 在序列的哪一个点开始替换，替换包括该位点 0表示插到最前面。1表示从第一个开始替换
	 * 如果ref为""，则将序列插入在startBias那个碱基的后面
	 * @param endLoc 实际位点 在序列的哪一个点结束替换，替换包括该位点
	 * @return
	 */
	private SeqFasta replaceSnpIndel(String replace, int startLoc, int endLoc) {
		SeqFasta seqFasta = refSeqIntactAA.clone();
		if (seqFasta.toString().equals("")) {
			return new SeqFasta();
		}
		seqFasta.modifySeq(startLoc, endLoc, replace, false, false);
		//修改移码
		return seqFasta;
	}
	
	/**
	 * “g.” for a genomic reference sequence<br>
	 * “c.” for a coding DNA reference sequence<br>
	 * “m.” for a mitochondrial DNA reference sequence<br>
	 * “n.” for a non-coding DNA reference sequence<br>
	 * “r.” for an RNA reference sequence (transcript)<br>
	 * “p.” for a protein reference sequence<br>
	 * <br>
	 * http://varnomen.hgvs.org/recommendations/general/<br>
	 * @return
	 */
	private String getHgvscPrefix() {
		return iso.ismRNAFromCds() ? "c." : "n.";
	}
	
	protected abstract EnumVariantClass getVariantClassification();
	
	/** 获得起点的 HGVSc */
	@VisibleForTesting
	public String getStartPosCis() {
		if (iso.getCodLoc(snpRefAltInfo.getStartPosition()) == GffGeneIsoInfo.COD_LOC_OUT
				&& iso.getCodLoc(snpRefAltInfo.getEndPosition()) == GffGeneIsoInfo.COD_LOC_OUT
				) {
			return snpRefAltInfo.getStartPosition() + "";
		} else {
			//暂时不清楚n怎么弄
			//http://varnomen.hgvs.org/bg-material/numbering/
			return getCodeStartInMRNA();
		}
	}
	/** 获得终点的 HGVSc */
	@VisibleForTesting
	public String getEndPosCis() {
		if (iso.getCodLoc(snpRefAltInfo.getStartPosition()) == GffGeneIsoInfo.COD_LOC_OUT
				&& iso.getCodLoc(snpRefAltInfo.getEndPosition()) == GffGeneIsoInfo.COD_LOC_OUT
				) {
			return snpRefAltInfo.getEndPosition() + "";
		} else {
			//暂时不清楚n怎么弄
			//http://varnomen.hgvs.org/bg-material/numbering/
			return getCodeEndInMRNA();
		}
	}
	
	private String getCodeStartInMRNA() {
		int coord = iso.isCis5to3() ? snpRefAltInfo.getStartPosition() : snpRefAltInfo.getEndPosition();
		return iso.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_OUT ? getCodOutMRNA(coord) : getCodInMRNA(coord);
	}
	
	private String getCodeEndInMRNA() {
		int coord = iso.isCis5to3() ? snpRefAltInfo.getEndPosition() : snpRefAltInfo.getStartPosition();
		return iso.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_OUT ? getCodOutMRNA(coord) : getCodInMRNA(coord);
	}
	
	private String getCodOutMRNA(int coord) {
		int[] result = getCodOutOfGene2AtgUag(coord);
		String prefix = result[0] <0 ? "" : "*";
		String prefixOut = result[0] <0 ? "u" : "d";
		String append = result[1] > 0 ? "+" : "-";
		return prefix + result[0] + append + prefixOut + Math.abs(result[1]);
	}
	
	private String getCodInMRNA(int coord) {
		int[] result = iso.getCod2UAG(coord) <= 0 ?  getCod2AtgUagMRNA(coord, true) : getCod2AtgUagMRNA(coord, false);
		String prefix = iso.getCod2UAG(coord) <= 0 ? "" : "*";
		String append = "";
		if (result[1] > 0) {
			append = "+" + result[1];
		} else if (result[1] < 0) {
			append = result[1] + "";
		}
		return prefix + result[0] + append;
	}
	
	/** 必须为mRNA才能计算, coord 在基因外部<br>
	 * 参考网址：<br>
	 * http://www.hgvs.org/mutnomen/disc.html#frameshift<br>
	 * <br>
	 * -N-uM = nucleotide M 5' (upstream) of the nucleotide -N of the transcription initiation site -N (e.g. -237-u5A>G)<br>
	 * NOTE: restricted to nucleotides 5' of the transcription initiation site (cap site, i.e. upstream of the gene incl. the promoter)<br>
	 * N+dM = nucleotide M 3' (downstream) of the nucleotide transcription termination site *N (e.g. *237+d5A>G)<br>
	 * NOTE: restricted to locations 3' of the polyA-addition site (downstream of the gene)<br>
	 * @param coord<br>
	 * @return int[2]<br>
	 * 0: 距离ATG/UAG多少位的cds, 负数表示距离ATG，正数表示距离UAG
	 * 1: 当在intron中时，距离头部还是尾部的exon边界
	 * 两者合并即为 123-4，即exon的边界距离ATG123bp，位点在intron中，距离exon的3'边界4bp
	 */
	private int[] getCodOutOfGene2AtgUag(int coord) {
		int num = iso.getNumCodInEle(coord);
		//外显子上时
		if (num != 0) {
			throw new RuntimeException();
		}
		if (!iso.ismRNAFromCds()) {
			//非cds暂时不知道怎么做
			return null;
		}
		int cod2Site = 0, cod2Bound = 0;
		if (iso.isCis5to3() && coord < iso.getStart()
				|| !iso.isCis5to3() && coord > iso.getStart()
				) {
			cod2Site = iso.getCod2ATGmRNA(iso.getStart());
			cod2Bound = -Math.abs(coord - iso.getStart());
		}	else if (iso.isCis5to3() && coord > iso.getEnd()
				|| !iso.isCis5to3() && coord < iso.getEnd()
				) {
			cod2Site = iso.getCod2UAGmRNA(iso.getEnd());
			cod2Bound = Math.abs(coord - iso.getEnd());
		}
		
		return new int[]{cod2Site, cod2Bound};
	}
	
	/** 必须为mRNA才能计算
	 * @param coord
	 * @return int[2]
	 * 0: 距离ATG/UAG多少位的cds
	 * 1: 当在intron中时，距离头部还是尾部的exon边界
	 * 两者合并即为 123-4，即exon的边界距离ATG123bp，位点在intron中，距离exon的3'边界4bp
	 */
	private int[] getCod2AtgUagMRNA(int coord, boolean isAtg) {
		int num = iso.getNumCodInEle(coord);
		//外显子上时
		if (num > 0) {
			return new int[]{getCodeToAtgUag(coord, isAtg), 0};
		}
		//内含子上时，加1是因为这个返回的距离是距离内含子边界，而我现在要的是距离外显子，因此需要加上1
		int cod2BoundStart = iso.getCod2ExInStart(coord)+1;
		int cod2BoundEnd = iso.getCod2ExInEnd(coord)+1;
		int cod2Atg = 0, cod2Bound = 0;
		if (iso.isCis5to3() && cod2BoundStart <= cod2BoundEnd) {
			cod2Atg = getCodeToAtgUag(coord - cod2BoundStart, isAtg);
			cod2Bound = cod2BoundStart;
		} else if (!iso.isCis5to3() && cod2BoundStart <= cod2BoundEnd) {
			cod2Atg = getCodeToAtgUag(coord + cod2BoundStart, isAtg);
			cod2Bound = cod2BoundStart;
		} else if (iso.isCis5to3() && cod2BoundStart > cod2BoundEnd) {
			cod2Atg = getCodeToAtgUag(coord + cod2BoundEnd, isAtg);
			cod2Bound = -cod2BoundEnd;
		} else if (!iso.isCis5to3() && cod2BoundStart > cod2BoundEnd) {
			cod2Atg = getCodeToAtgUag(coord - cod2BoundEnd, isAtg);
			cod2Bound = -cod2BoundEnd;
		}
		return new int[]{cod2Atg, cod2Bound};
	}
	
	private int getCodeToAtgUag(int coord, boolean isAtg) {
		int distance =  isAtg ? iso.getCod2ATGmRNA(coord) : iso.getCod2UAGmRNA(coord);
		if (distance >= 0 && isAtg) distance = distance + 1;
		return distance;
	}
	
}

/**
 * 貌似与SiteSnpIndelInfoSnp一模一样
 * @author zong0jie
 *
 */
class SnpRefAltIsoInsert extends SnpRefAltIso {
	private static Logger logger = Logger.getLogger(SnpRefAltIsoInsert.class);

	public SnpRefAltIsoInsert(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		super(snpRefAltInfo, iso);
		if (snpRefAltInfo.getSeqRef().length() > 1 && snpRefAltInfo.getSeqAlt().length() > 1) {
			throw new ExceptionNbcParamError("Input is not a snp");
		}
	}
	
	protected void setMapInfoRefSeqAAabs(SeqHash seqHash) {
		int snpSite = snpRefAltInfo.getStartPosition();
		String refId = snpRefAltInfo.getRefId();
		enumMrnaLocate = iso.getCodLocate(snpSite);
		if (enumMrnaLocate != EnumMrnaLocate.intergenic) {
			setEffectSplitType(iso, snpRefAltInfo.getAlignRef().getStartAbs());
		}
		//mRNA层面
		//就算在外显子中，但是如果是非编码rna，或者在UTR区域中，也返回
		if (enumMrnaLocate != EnumMrnaLocate.cds) {
			return;
		}
		
		int LocStart = iso.getLocAAbefore(snpSite);//该位点所在AA的第一个loc
		int LocEnd = iso.getLocAAend(snpSite);
		if (LocEnd <0) {//如果不在转录本中
			if (iso.isCis5to3()) {
				LocEnd = LocStart + 2;
			} else {
				LocEnd = LocStart - 2;
			}
		}
		ArrayList<ExonInfo> lsTmp = iso.getRangeIsoOnExon(LocStart, LocEnd);
		if (lsTmp == null) {
			refSeqIntactAA = seqHash.getSeq(iso.isCis5to3(), refId, LocStart, LocEnd);
		} else if (lsTmp.size() == 0) {
			return;
		} else {
			refSeqIntactAA = seqHash.getSeq(StrandType.isoForward, refId, lsTmp, false);
		}
		snpOnReplaceLocStart = -iso.getLocAAbeforeBias(snpSite) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart;
	}
	
	private void setEffectSplitType(GffGeneIsoInfo gffGeneIsoInfo, int codLoc) {
		int cod2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(codLoc);
		int cod2UAGmRNA = gffGeneIsoInfo.getCod2UAGmRNA(codLoc);
		if (cod2ATGmRNA >= 0 && cod2ATGmRNA <= splitRegion) {
			splitDistance = Math.abs(cod2ATGmRNA);
			splitType = SplitType.ATG;
		} else if (cod2UAGmRNA <= 0 && cod2UAGmRNA >= -splitRegion) {
			splitDistance = Math.abs(cod2ATGmRNA);
			splitType = SplitType.UAG;
		} else {
			int locNum = gffGeneIsoInfo.getNumCodInEle(codLoc);
			int cod2Start = gffGeneIsoInfo.getCod2ExInStart(codLoc);
			int cod2End = gffGeneIsoInfo.getCod2ExInEnd(codLoc);
			if (locNum > 1 && cod2Start <= splitRegion) {
				splitDistance = cod2Start;
				splitType = SplitType.EXON_START;
			} else if (locNum < 0 && cod2End <= splitRegion) {
				splitDistance = cod2End;
				splitType = SplitType.EXON_START;
			} else if (locNum > 0 && locNum < gffGeneIsoInfo.size() && cod2End <= splitRegion) {
				splitDistance = cod2End;
				splitType = SplitType.EXON_END;
			} else if (locNum < 0 && cod2Start <= splitRegion) {
				splitDistance = cod2Start;
				splitType = SplitType.EXON_END;
			}
		}
	}
	
	public int getOrfShift() {
		return (3 - (snpRefAltInfo.getSeqRef().length() - snpRefAltInfo.getSeqAlt().length())%3) % 3;//待检查
	}
	
	public SnpIndelType getSnpIndelType() {
		return SnpIndelType.INSERT;
	}
	
	public String getAffectCdsInfo() {
		String result = "";
		int start = snpRefAltInfo.getStartPosition();
		if (iso == null) {
			return result;
		}
		EnumMrnaLocate enumMrnaLocate = iso.getCodLocate(start);
		if (enumMrnaLocate == EnumMrnaLocate.cds) {
			int cdsloc = iso.getCod2ATGmRNA(start);
			if (iso.isCis5to3()) {
				result = "c." + cdsloc + "_" + (cdsloc + 1);
			} else {
				result = "c." + cdsloc + "_" + (cdsloc - 1);
			}
			
		} else if (enumMrnaLocate == EnumMrnaLocate.intron) {
			int distance2Before = iso.getCod2ExInStart(start) + 1;//到前一个exon的距离
			int distance2After = iso.getCod2ExInEnd(start) + 1;//到后一个exon的距离
			if (distance2Before < distance2After) {
				String first = iso.getCod2ATGmRNA(start - distance2Before) + "+" + distance2Before;
				String second = iso.getCod2ATGmRNA(start - distance2Before) + "+" + (distance2Before + 1);
				result = "c." + first + "_" + second;
			} else {
				String first = iso.getCod2ATGmRNA(start - distance2After) + "-" + distance2After;
				String second =  iso.getCod2ATGmRNA(start - distance2After) + "-" + (distance2After + 1);
				result = "c." + first + "_" + second;
			}
		}
		return result;
	}
	
	public String getAffectAAInfo() {
		//TODO
		String result = "";
		int start = snpRefAltInfo.getStartPosition();
		if (iso == null) {
			return result;
		}
		EnumMrnaLocate enumMrnaLocate = iso.getCodLocate(start);
		int exonNum = iso.getNumCodInEle(start);
		if (enumMrnaLocate == EnumMrnaLocate.cds 
				&& (
						getOrfShift() != 0 
						|| 
							(splitType !=SplitType.EXON_START && splitType != splitType.EXON_END)
						)
			) {
			result = "p." + getAAchange1();
		} else if (enumMrnaLocate == EnumMrnaLocate.intron) {
			int exonNumBefore = Math.abs(iso.getNumCodInEle(start));//前一个exon的序号
			int exonNumAfter = exonNumBefore + 1;
			int distance2Before = iso.getCod2ExInStart(start) + 1;//到前一个exon的距离
			int distance2After = iso.getCod2ExInEnd(start) + 1;//到后一个exon的距离
			if (distance2Before < distance2After) {
				String first = exonNumBefore + "+" + distance2Before;
				String second = exonNumBefore + "+" + (distance2Before + 1);
				result = "e." + first + "_" + second;
			} else {
				String first = exonNumAfter + "-" + distance2After;
				String second = exonNumAfter + "-" + (distance2After + 1);
				result = "e." + first + "_" + second;
			}
		}
		return result;
	}

	@Override
	protected EnumVariantClass getVariantClassification() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

class SiteSnpIndelInfoSnp extends SiteSnpIndelInfoInsert {
	public SiteSnpIndelInfoSnp(RefSiteSnpIndel mapInfoSnpIndel, String refBase, String thisBase) {
		super(mapInfoSnpIndel, refBase, thisBase);
	}
	public int getOrfShift() {
		return 0;
	}
	@Override
	public SnpIndelType getSnpIndelType() {
		return SnpIndelType.MISMATCH;
	}
	
	@Override
	public String getAffectCdsInfo() {
		String result = "";
		GffGeneIsoInfo iso = refSiteSnpIndelParent.getGffIso();
		int start = refSiteSnpIndelParent.getRefSnpIndelStart();
		if (iso == null) {
			return result;
		}
		EnumMrnaLocate enumMrnaLocate = iso.getCodLocate(start);
		if (enumMrnaLocate == EnumMrnaLocate.cds) {
			int cdsloc = iso.getCod2ATGmRNA(start);
			result = "c." + cdsloc;
		} else if (enumMrnaLocate == EnumMrnaLocate.intron) {
			int distance2Before = iso.getCod2ExInStart(start) + 1;//到前一个exon的距离
			int distance2After = iso.getCod2ExInEnd(start) + 1;//到后一个exon的距离
			if (distance2Before < distance2After) {
				String first = iso.getCod2ATGmRNA(start - distance2Before) + "+" + distance2Before;
				result = "c." + first;
			} else {
				String first = iso.getCod2ATGmRNA(start - distance2After) + "-" + distance2After;
				result = "c." + first;
			}
		}
		return result;
	}
	
	@Override
	public String getAffectAAInfo() {
		//TODO
		String result = "";
		GffGeneIsoInfo iso = refSiteSnpIndelParent.getGffIso();
		int start = refSiteSnpIndelParent.getRefSnpIndelStart();
		if (iso == null) {
			return result;
		}
		EnumMrnaLocate enumMrnaLocate = iso.getCodLocate(start);
		int exonNum = iso.getNumCodInEle(start);
		if (enumMrnaLocate == EnumMrnaLocate.cds 
				&& (
						getOrfShift() != 0 
						|| 
							(splitType !=SplitType.EXON_START && splitType != splitType.EXON_END)
						)
			) {
			int cdsloc = iso.getCod2ATGmRNA(start);
			result = "p." + getAAchange1();
		} else if (enumMrnaLocate == EnumMrnaLocate.intron) {
			int exonNumBefore = Math.abs(iso.getNumCodInEle(start));//前一个exon的序号
			int exonNumAfter = exonNumBefore + 1;
			int distance2Before = iso.getCod2ExInStart(start) + 1;//到前一个exon的距离
			int distance2After = iso.getCod2ExInEnd(start) + 1;//到后一个exon的距离
			if (distance2Before < distance2After) {
				String first = exonNumBefore + "+" + distance2Before;
				result = "e." + first;
			} else {
				String first = exonNumAfter + "-" + distance2After;
				result = "e." + first;
			}
		}
		return result;
	}
	
}
/**
 * 没有snp的位点，就是只返回ref了
 * @author zongjie
 *
 */
class SiteSnpIndelInfoNoSnp extends SiteSnpIndelInfoInsert {
	public SiteSnpIndelInfoNoSnp(RefSiteSnpIndel refSiteSnpIndel, String refBase, String thisBase) {
		super(refSiteSnpIndel, refBase, thisBase);
	}
	public int setOrfShift() {
		return 0;
	}
	@Override
	public SnpIndelType getSnpIndelType() {
		return SnpIndelType.CORRECT;
	}
}
/**
 * 必须很短的deletion，譬如在20bp以内的deletion
 * @author zong0jie
 *
 */
class SiteSnpIndelInfoDeletion extends SnpRefAltIso {
	Logger logger = Logger.getLogger(SiteSnpIndelInfoInsert.class);
	int orfShift = 0;
	public SiteSnpIndelInfoDeletion(RefSiteSnpIndel refSiteSnpIndel, String refBase, String thisBase) {
		super(refSiteSnpIndel, refBase, thisBase);
		if (refBase.length() <= 1 || thisBase.length() > 1) {
			logger.error("本位点可能不是缺失，请核对：" + refSiteSnpIndel.getRefID() + "\t" + refSiteSnpIndel.getRefSnpIndelStart());
		}
	}

	@Override
	protected void setMapInfoRefSeqAAabs(SeqHash seqHash) {
		int refStart = refSiteSnpIndelParent.getRefSnpIndelStart();
		
		int refEnd = refStart + referenceSeq.length() - 1;
		int refStartCis = refStart; int refEndCis = refEnd;

		GffGeneIsoInfo gffGeneIsoInfo = refSiteSnpIndelParent.getGffIso();
		if (!gffGeneIsoInfo.isCis5to3()) {
			refStartCis = refEnd; refEndCis = refStart;
		}
		EnumMrnaLocate codLocInfo = setLocationInfo(gffGeneIsoInfo, refStartCis, refEndCis);
		
		if (codLocInfo != EnumMrnaLocate.exon) {
			return;
		}
		
		setEffectSplitType(gffGeneIsoInfo, refStartCis, refEndCis);
		
		int[] bound = getLocOutOfExonToNearistExonBounder(gffGeneIsoInfo, refStartCis, refEndCis);
		refStartCis = bound[0]; refEndCis = bound[1];
		
		if (gffGeneIsoInfo.isCodInAAregion(refStartCis) || gffGeneIsoInfo.isCodInAAregion(refEndCis)) {
			codLocInfo = EnumMrnaLocate.cds;
			if (!gffGeneIsoInfo.isCodInAAregion(refStartCis)) {
				refStartCis = gffGeneIsoInfo.getATGsite();
			}
			else if (!gffGeneIsoInfo.isCodInAAregion(refEndCis)) {
				refEndCis = gffGeneIsoInfo.getUAGsite();
			}
		}
		if (codLocInfo == EnumMrnaLocate.cds) {
			int LocStart = gffGeneIsoInfo.getLocAAbefore(refStartCis);
			int LocEnd =gffGeneIsoInfo.getLocAAend(refEndCis);
			refSeqIntactAA.setStartEndLoc(LocStart, LocEnd);
			ArrayList<ExonInfo> lsTmp = gffGeneIsoInfo.getRangeIsoOnExon(LocStart, LocEnd);
			if (lsTmp == null) {
				logger.error("检查一下：" + refSiteSnpIndelParent.getRefID() + "\t" + refSiteSnpIndelParent.getRefSnpIndelStart());
				return;
			}
			setOrfShiftAndReplaceSite(gffGeneIsoInfo, refStartCis, refEndCis);
			SeqFasta NR = seqHash.getSeq(StrandType.isoForward, refSeqIntactAA.getRefID(), lsTmp, false);
			refSeqIntactAA.setSeq(NR,false);//因为上面已经反向过了
		}
		
		else if (codLocInfo != EnumMrnaLocate.cds && gffGeneIsoInfo.getNumCodInEle(refStartCis) != gffGeneIsoInfo.getNumCodInEle(refEndCis)) {
			logger.error("缺失外显子："  + refSiteSnpIndelParent.getRefID() + "\t" + refSiteSnpIndelParent.getRefSnpIndelStart());
			codLocInfo = enumMrnaLocate.cds;
			return;
		}
		else {
			return;
		}
	}
	/**
	 * 如果deletion覆盖了exon--这个在前面必须首先判定
	 * 然后如果deletion的位点落到了exon外，则将该位点定位到所包含的最近的exon的边界上，如下图<br>
	 * 0--1----------------2---3---------cod1-------------4---5----------------6---7---------------cod2---------------8----9, 修正为<br>
	 * 0--1----------------2---3---------------------cod1(4)---5---------------6---(7)cod2----------------------------8----9<br>
	 * @param gffGeneIsoInfo
	 * @param refStartCis
	 * @param refEndCis
	 * @return
	 */
	private static int[] getLocOutOfExonToNearistExonBounder(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		int[] bounder = new int[]{refStartCis, refEndCis};
		//将起点和终点转换到距离最近的exon上去
		if (gffGeneIsoInfo.getCodLoc(refStartCis) != GffGeneIsoInfo.COD_LOC_EXON) {
			int startExonNum = Math.abs(gffGeneIsoInfo.getNumCodInEle(refStartCis));
			//TODO check
			if (startExonNum == -1) {
				startExonNum = 0;
			}
			bounder[0] = gffGeneIsoInfo.get(startExonNum).getStartCis();
		}
		else if (gffGeneIsoInfo.getCodLoc(refEndCis) != GffGeneIsoInfo.COD_LOC_EXON) {
			int endExonNum = Math.abs(gffGeneIsoInfo.getNumCodInEle(refEndCis)) - 1;
			if (endExonNum == -1) {
				endExonNum = gffGeneIsoInfo.size() - 1;
			}
			bounder[1] = gffGeneIsoInfo.get(endExonNum).getEndCis();
		}
		return bounder;
	}
	/**
	 * 必须在{@link#setLocationInfo}结束后调用
	 * @param gffGeneIsoInfo
	 * @param refStartCis 必须在exon中
	 * @param refEndCis 必须在exon中
	 */
	private void setEffectSplitType(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		splitType = SplitType.NONE;
		int codStart2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(refStartCis);
		int codEnd2ATGmRNA = gffGeneIsoInfo.getCod2ATGmRNA(refEndCis);
		int codStart2UAGmRNA = gffGeneIsoInfo.getCod2UAGmRNA(refStartCis);
		int codEnd2UAGmRNA = gffGeneIsoInfo.getCod2UAGmRNA(refEndCis);
		
		if (codStart2ATGmRNA <=0 && codEnd2ATGmRNA >= 0) {
			splitDistance = -100;
			splitType = SplitType.ATG;
		} else if (codStart2ATGmRNA >= 0 && codStart2ATGmRNA <= splitRegion) {
			splitDistance = codStart2ATGmRNA;
			splitType = SplitType.ATG;
		} else if (codStart2UAGmRNA <= 0 && codEnd2UAGmRNA >= 0) {
			splitDistance = -100;
			splitType = SplitType.UAG;
		} else if (codEnd2UAGmRNA <= 0 && codEnd2UAGmRNA >= -splitRegion) {
			splitDistance = Math.abs(codEnd2ATGmRNA);
			splitType = SplitType.UAG;
		} else if (gffGeneIsoInfo.getNumCodInEle(refStartCis) != gffGeneIsoInfo.getNumCodInEle(refEndCis)) {
			splitDistance = -100;
			splitType = SplitType.Cover_Exon_Intron;
		} else {
			int locNum = gffGeneIsoInfo.getNumCodInEle(refStartCis);
			if (locNum != 1 && locNum > 0 && gffGeneIsoInfo.getCod2ExInStart(refStartCis) <= splitRegion) {
				splitDistance = gffGeneIsoInfo.getCod2ExInStart(refStartCis);
				splitType = SplitType.EXON_START;
			} else if (locNum < 0 && gffGeneIsoInfo.getCod2ExInStart(refStartCis) <= splitRegion) {
				splitDistance = gffGeneIsoInfo.getCod2ExInStart(refStartCis);
				splitType = SplitType.EXON_END;
			}
			
			int codToEnd = gffGeneIsoInfo.getCod2ExInEnd(refEndCis);
			if (locNum > 0 && locNum != gffGeneIsoInfo.size() && codToEnd <= splitRegion) {
				if (splitType == SplitType.EXON_START) {
					splitDistance = Math.min(splitDistance, codToEnd);
					splitType = SplitType.Exon_From_Start_To_End;
				} else {
					splitDistance = codToEnd;
					splitType = SplitType.EXON_END;
				}
			}
		}
	}
	/**
	 * 设定该deletion处在哪个位置 ，或者说是否覆盖了exon
	 * @param gffGeneIsoInfo
	 * @param startCis
	 * @param endCis
	 */
	private static EnumMrnaLocate setLocationInfo(GffGeneIsoInfo gffGeneIsoInfo, int startCis, int endCis) {
		EnumMrnaLocate locate = EnumMrnaLocate.intergenic;
		if (gffGeneIsoInfo.getCodLoc(startCis) == GffGeneIsoInfo.COD_LOC_EXON || gffGeneIsoInfo.getCodLoc(endCis) == GffGeneIsoInfo.COD_LOC_EXON
				|| gffGeneIsoInfo.getNumCodInEle(startCis) != gffGeneIsoInfo.getNumCodInEle(endCis)
				) {
			locate = EnumMrnaLocate.exon;
		}
		//TODO 这里没有考虑一头在基因前一头在基因尾的情况
		else if (gffGeneIsoInfo.getCodLoc(startCis) == GffGeneIsoInfo.COD_LOC_OUT && gffGeneIsoInfo.getCodLoc(endCis) == GffGeneIsoInfo.COD_LOC_OUT) {
			locate = EnumMrnaLocate.intergenic;
		}
		else if (gffGeneIsoInfo.getCodLoc(startCis) == GffGeneIsoInfo.COD_LOC_INTRON && gffGeneIsoInfo.getCodLoc(endCis) == GffGeneIsoInfo.COD_LOC_INTRON
				&& gffGeneIsoInfo.getNumCodInEle(startCis) == gffGeneIsoInfo.getNumCodInEle(endCis)
				) {
			locate = EnumMrnaLocate.intron;
		}
		return locate;
	}
	/** 需要测试
	 * 这段代码的前提假设是ref很长，然后thisSeq一定为1
	 * 这个形式是deletion的一般形式。
	 * 那么这就排除了某段序列被完全不同的序列替换的情况
	 *  譬如  ref为 ATCG TC GT    this为 ATCG AACTG GT 这种情况会被拆分成两个错配和一个插入
	 *  那么这段代码就处理不了
	 *  */
	protected void setOrfShiftAndReplaceSite(GffGeneIsoInfo gffGeneIsoInfo, int refStartCis, int refEndCis) {
		int deletionLen = gffGeneIsoInfo.getLocDistmRNA(refStartCis, refEndCis) + 1 - thisSeq.length();
		orfShift = deletionLen%3;
		//TODO 这里有问题，如果一个deletion横跨了一整个intron，那么就会有错误
		snpOnReplaceLocStart = -gffGeneIsoInfo.getLocAAbeforeBias(refStartCis) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart + deletionLen;
	}

	@Override
	public SnpIndelType getSnpIndelType() {
		return SnpIndelType.DELETION;
	}
	
	public int getOrfShift() {
		return orfShift;
	}

	@Override
	public String getAffectCdsInfo() {
		String result = "";
		GffGeneIsoInfo iso = refSiteSnpIndelParent.getGffIso();
		int start = refSiteSnpIndelParent.getRefSnpIndelStart();
		int end = start + getThisSeq().length() - 1;
		if (iso == null) {
			return result;
		}
		EnumMrnaLocate locatStart = iso.getCodLocate(start);
		EnumMrnaLocate locatEnd = iso.getCodLocate(end);
		if (locatStart == EnumMrnaLocate.cds) {
			int cdslocStart = iso.getCod2ATGmRNA(start);
			if (splitType != SplitType.Cover_Exon_Intron && locatEnd == EnumMrnaLocate.cds) {
				int cdslocEnd = iso.getCod2ATGmRNA(end);
				result = "c." + cdslocStart + "_" + cdslocEnd;
			} else if (splitType == SplitType.Cover_Exon_Intron) {
				//TODO
				int cdslocEnd = iso.getCod2ATGmRNA(end);
				result = "c." + cdslocStart + "_" + cdslocEnd;
			} else if (locatEnd == EnumMrnaLocate.intron || locatEnd == EnumMrnaLocate.intergenic) {
				int distance2Splice = iso.getCod2ExInEnd(start) + 2;
				result = "c." + cdslocStart + "+" + distance2Splice + "_" + cdslocStart + "1";
			} else {
				throw new RuntimeException("no such condition, please check");
			}
		} else if (locatStart == EnumMrnaLocate.intron) {
			int distance2Before = iso.getCod2ExInStart(start) + 1;//到前一个exon的距离
			int distance2After = iso.getCod2ExInEnd(start) + 1;//到后一个exon的距离
			if (distance2Before < distance2After) {
				String first = iso.getCod2ATGmRNA(start - distance2Before) + "+" + distance2Before;
				int distance2Before1 = distance2Before;
				String second = iso.getCod2ATGmRNA(start - distance2Before) + "+" + (distance2Before + 1);
				result = "c." + first + "_" + second;
			} else {
				String first = iso.getCod2ATGmRNA(start - distance2After) + "-" + distance2After;
				String second =  iso.getCod2ATGmRNA(start - distance2After) + "-" + (distance2After + 1);
				result = "c." + first + "_" + second;
			}
			
			if (locatEnd == EnumMrnaLocate.cds) {
				int cdslocEnd = iso.getCod2ATGmRNA(end);
				int distance2Splice = iso.getCod2ExInStart(end) + 2;
				result = "c." + cdslocEnd + "+" + distance2Splice + "_" + cdslocEnd + "1";
			} else if (locatEnd == EnumMrnaLocate.intron && splitType != SplitType.Cover_Exon_Intron) {
				if (distance2Before < distance2After) {
					String first = iso.getCod2ATGmRNA(start - distance2Before) + "+" + distance2Before;
					String second = iso.getCod2ATGmRNA(start - distance2Before) + "+" + (distance2Before + 1);
					result = "c." + first + "_" + second;
				} else {
					String first = iso.getCod2ATGmRNA(start - distance2After) + "-" + distance2After;
					String second =  iso.getCod2ATGmRNA(start - distance2After) + "-" + (distance2After + 1);
					result = "c." + first + "_" + second;
				}
			}			
		}
		return result;
	}
	
	@Override
	public String getAffectAAInfo() {
		String result = "";
		GffGeneIsoInfo iso = refSiteSnpIndelParent.getGffIso();
		int start = refSiteSnpIndelParent.getRefSnpIndelStart();
		int end = start + getThisSeq().length() - 1;
		if (iso == null) {
			return result;
		}
		EnumMrnaLocate locateStart = iso.getCodLocate(start);
		EnumMrnaLocate locateEnd = iso.getCodLocate(end);
		int exonNumStart = iso.getNumCodInEle(start);
		int exonNumEnd = iso.getNumCodInEle(end);
		
		if (locateStart == EnumMrnaLocate.cds && exonNumStart == exonNumEnd) {
			if (getOrfShift() == 0) {
				result = "p." + getRefAAnr().toStringAA1() + this.refSiteSnpIndelParent.getAffectAANum() + "in_frame_del";
			}
		}
		
		
		if (locateStart == EnumMrnaLocate.cds 
				&& (
						getOrfShift() != 0 
						|| 
							(splitType !=SplitType.EXON_START && splitType != SplitType.EXON_END)
						)
			) {
			int cdsloc = iso.getCod2ATGmRNA(start);
			result = "p." + getAAchange1();
		} else if (enumMrnaLocate == EnumMrnaLocate.intron) {
			int exonNumBefore = Math.abs(iso.getNumCodInEle(start));//前一个exon的序号
			int exonNumAfter = exonNumBefore + 1;
			int distance2Before = iso.getCod2ExInStart(start) + 1;//到前一个exon的距离
			int distance2After = iso.getCod2ExInEnd(start) + 1;//到后一个exon的距离
			if (distance2Before < distance2After) {
				String first = exonNumBefore + "+" + distance2Before;
				String second = exonNumBefore + "+" + (distance2Before + 1);
				result = "e." + first + "_" + second;
			} else {
				String first = exonNumAfter + "-" + distance2After;
				String second = exonNumAfter + "-" + (distance2After + 1);
				result = "e." + first + "_" + second;
			}
		}
		return result;
	}
}

enum EnumVariantClass {
	Frame_Shift_Del,
	Frame_Shift_Ins,
	In_Frame_Del,
	In_Frame_Ins,
	Missense_Mutation,
	Nonsense_Mutation,
	Silent,
	Splice_Site,
	Translation_Start_Site,
	Nonstop_Mutation,
	RNA,
	Targeted_Region
}

/**
 * If Variant_Type == "INS", then (End_position - Start_position + 1 == length (Reference_Allele) or End_position - Start_position == 1) and length(Reference_Allele) <= length(Tumor_Seq_Allele1 and Tumor_Seq_Allele2)
 * If Variant_Type == "DEL", then End_position - Start_position + 1 == length (Reference_Allele), then length(Reference_Allele) >= length(Tumor_Seq_Allele1 and Tumor_Seq_Allele2)
 * If Variant_Type == "SNP", then length(Reference_Allele and Tumor_Seq_Allele1 and Tumor_Seq_Allele2) ==  1 and (Reference_Allele and Tumor_Seq_Allele1 and Tumor_Seq_Allele2) != "-"
 * If Variant_Type == "DNP", then length(Reference_Allele and Tumor_Seq_Allele1 and Tumor_Seq_Allele2) ==  2 and (Reference_Allele and Tumor_Seq_Allele1 and Tumor_Seq_Allele2) !contain "-"
 * If Variant_Type == "TNP", then length(Reference_Allele and Tumor_Seq_Allele1 and Tumor_Seq_Allele2) ==  3 and (Reference_Allele and Tumor_Seq_Allele1 and Tumor_Seq_Allele2) !contain "-"
 * If Variant_Type == "ONP", then length(Reference_Allele) == length(Tumor_Seq_Allele1) == length(Tumor_Seq_Allele2) > 3 and (Reference_Allele and Tumor_Seq_Allele1 and Tumor_Seq_Allele2) !contain "-"
 * @author zong0jie
 * @data 2018年1月5日
 */
enum EnumVariantType {
	INS,
	DEL,
	SNP,
	/** 两个突变成两个*/
	DNP,
	/** 三个突变成三个 */
	TNP,
	/** n个突变成n个 */
	ONP
}
