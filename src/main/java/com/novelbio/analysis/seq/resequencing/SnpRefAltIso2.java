package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;

import org.apache.log4j.Logger;

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
public abstract class SnpRefAltIso2 {
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
	
	public SnpRefAltIso2(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
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
	
	public String getAAchange1() {
		if (this instanceof SiteSnpIndelInfoSnp && getAffectAANum() > 0) {
			return getRefAAnr().toStringAA1() + getAffectAANum() + getThisAAnr().toStringAA1();
		} else {
			return "";
		}
	}
	public String getAAchange3() {
		if (this instanceof SiteSnpIndelInfoSnp && getAffectAANum() > 0) {
			return getRefAAnr().toStringAA3() + getAffectAANum() + getThisAAnr().toStringAA3();
		} else {
			return "";
		}
	}
	
	/**
“g.” for a genomic reference sequence
“c.” for a coding DNA reference sequence
“m.” for a mitochondrial DNA reference sequence
“n.” for a non-coding DNA reference sequence
“r.” for an RNA reference sequence (transcript)
“p.” for a protein reference sequence
http://varnomen.hgvs.org/recommendations/general/
	 * @return
	 */
	private String getHgvscPrefix() {
		return iso.ismRNAFromCds() ? "c." : "n.";
	}
	
	protected abstract EnumVariantClass getVariantClassification();
	
	/**
	 * 返回变化的AA的化学性质改变形式，不在cds中则返回""；
	 * @return
	 */
	public String getAAattrConvert() {
		if (isCDS() && seqRef.length() == 1 && seqAlt.length() == 1) {
			String refAA = getRefAAnr().toStringAA1();
			String thisAA = getThisAAnr().toStringAA1();
			try {
				return SeqFasta.cmpAAquality(refAA, thisAA);
			} catch (Exception e) {
				logger.error("变化的AA的化学性质出错");
				return "";
			}
		}
		return "";
	}
	/**
	 * 如果一个位点有两个以上的snp，就可能会出错
	 * 获得本snp位置最多变异的AA序列
	 * 注意要通过{@link #setCis5to3(Boolean)}来设定 插入序列在基因组上是正向还是反向
	 * 还要通过{@link #setReplaceLoc(int)}来设定插入在refnr上的位置
	 * @return 没有的话就返回一个空的seqfasta
	 */
	public SeqFasta getThisAAnr() {
		String seq = thisSeq;
		if ( refSeqIntactAA.isCis5to3() != null && !refSeqIntactAA.isCis5to3()) {
			seq = SeqFasta.reverseComplement(seq);
		}
		if (refSiteSnpIndelParent.getGffIso() == null)
			return new SeqFasta();
	
		return replaceSnpIndel(seq, snpOnReplaceLocStart, snpOnReplaceLocEnd);
	}
	
	public SeqFasta getRefAAnr() {
		return refSeqIntactAA;
	}
	public String getAAchamicalConvert() {
		if (this instanceof SiteSnpIndelInfoSnp) {
			String refaa =  refSeqIntactAA.toStringAA(false);
			String thisaa = getThisAAnr().toStringAA(false);
			return SeqFasta.cmpAAquality(refaa, thisaa);
		}
		return "";
	}
	
	/** 返回该位点的起点在第几个氨基酸上，如果不在cds中则返回 -1 */
	public int getAffectAANum() {
		if (iso == null || iso.getCodLocUTRCDS(snpRefAltInfo.getStartPosition()) != GffGeneIsoInfo.COD_LOCUTR_CDS) {
			return -1;
		}
		int num = iso.getCod2ATGmRNA(snpRefAltInfo.getStartPosition());
		return num/3 + 1;
	}
	
	/** 返回该位点的起点在第几个氨基酸上，如果不在cds中则返回 -1 */
	public int getAffectCdsNum() {
		if (iso == null || iso.getCodLocUTRCDS(snpRefAltInfo.getStartPosition()) != GffGeneIsoInfo.COD_LOCUTR_CDS) {
			return -1;
		}
		int num = iso.getCod2ATGmRNA(snpRefAltInfo.getStartPosition());
		return num + 1;
	}
}

/**
 * 貌似与SiteSnpIndelInfoSnp一模一样
 * @author zong0jie
 *
 */
class SiteSnpIndelInfoInsert extends SnpRefAltIso2 {
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfoInsert.class);

	public SiteSnpIndelInfoInsert(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
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
class SiteSnpIndelInfoDeletion extends SnpRefAltIso2 {
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


