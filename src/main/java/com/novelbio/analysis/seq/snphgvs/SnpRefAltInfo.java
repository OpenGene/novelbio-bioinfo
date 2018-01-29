package com.novelbio.analysis.seq.snphgvs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;


/**
 * 对于单个位点的snp与indel的情况，可以保存多个不同的样本。
 * 在setSampleName()方法中可设定样本名，并获得该样本的信息。
 * @author zong0jie
 */
public class SnpRefAltInfo {
	private static final Logger logger = Logger.getLogger(SnpRefAltInfo.class);
	static int GetSeqLen = 100;
	/**
	 * 用于检测var的duplication
	 * 譬如现在有 T [ATC] ATC ATC ATC ATC GCAT 在第一位的T后面插入了ATC
	 * 那么根据HGVS规范，这个应该写成T ATC ATC ATC ATC [ATC] GCAT
	 * 也就是重复区域放到最后的位置
	 * 那么我就要去提取染色体的序列并且把这个给对起来。
	 * 然后每次提取100bp的长度来做这个工作
	 * @param getSeqLen
	 */
	@VisibleForTesting
	protected static void setGetSeqLen(int getSeqLen) {
		GetSeqLen = getSeqLen;
	}
	
	/** ref的坐标区间 */
	Align alignRefRaw;
	/** ref的坐标区间 */
	Align alignRef;
	
	/** reference的序列 */
	String seqRef;
	/** 改变之后的序列 */
	String seqAlt;
	/**
	 * insertion和deletion的头部是一样的，这里我们要去掉
	 * chr1	2343	ACATC	ACCTA
	 * seqStart=2
	 * chr1 2345	A	T
	 * seqStart=0
	 */
	int seqStart;
	/**
	 * insertion和deletion的尾部是一样的，这里我们要去掉
	 * chr1	2343	ACATCTT	ACCTATT
	 * seqEnd=2
	 * chr1 2345	AC	TA
	 * seqEnd=0
	 */
	int seqEnd;
	
	EnumHgvsVarType varType;
	/** 是否为duplicate的类型 */
	boolean isDup;
	/** 如果是duplicate的类型，将数据往回移动一次，主要用于剪接位点gt-ag这块 */
	boolean isDupMoveLast;
	
	/**
	 * 影响到了哪几个转录本
	 * 本版本不考虑横跨多个基因的超长deletion
	 */
	List<GffGeneIsoInfo> lsIsos = new ArrayList<>();
	
	SeqHashInt seqHash;
	
	public SnpRefAltInfo(String refId, int position, String seqRef, String seqAlt) {
		int positionEnd = position + seqRef.length() - 1;
		alignRefRaw = new Align(refId, position, positionEnd);
		this.seqRef = seqRef;
		this.seqAlt = seqAlt;
		alignRef = alignRefRaw;
	}
	/** 设定序列 */
	public void setSeqHash(SeqHashInt seqHash) {
		this.seqHash = seqHash;
	}
	protected SeqHashInt getSeqHash() {
		return seqHash;
	}
	/** 仅用于测试 */
	@VisibleForTesting
	protected void setAlignRef(Align align) {
		this.alignRef = align;
	}
	/** 是否为duplicate类型 */
	public boolean isDup() {
		return isDup;
	}
	/** 如果是duplicate的类型，将数据往回移动一次，主要用于剪接位点gt-ag这块<br>
	 * 譬如插入 T--ACATG[ACATG]T----------AG<br>
	 * 其中插入在G和T之间，则切换为<br>
	 * T--[ACATG]-ACATGT----------AG<br>
	 * @param isDupMoveLast 默认不移动
	 */
	public void setIsDupMoveLast(boolean isDupMoveLast) {
		this.isDupMoveLast = isDupMoveLast;
	}
	/** 根据parent，设定GffChrAbs */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		GffCodGeneDU gffCodGeneDu = gffChrAbs.getGffHashGene().searchLocation(alignRef.getRefID(), alignRef.getStartAbs(), alignRef.getEndAbs());
		if (gffCodGeneDu == null) {
			return;
		}
		Set<GffDetailGene> setGenes = gffCodGeneDu.getCoveredOverlapGffGene();
		for (GffDetailGene gffDetailGene : setGenes) {
			lsIsos.addAll(gffDetailGene.getLsCodSplit());
		}
		this.seqHash = gffChrAbs.getSeqHash();
	}
	
	/**
	 * 这里主要考虑一种情况<br>
	 * ref GGGG[A]TCGGGG<br>
	 * chr1	2345	A	ATCA //这是头尾相同的，都是A<br>
	 * 插入之后为<br>
	 * ref GGGGA[TCA]TCGGGG<br>
	 * 这时候时检测不到duplicate的<br>
	 * <br>
	 * 但是如果写成<br>
	 *  chr1	2344	G	GATC <br>
	 *  ref GGGG[ATC]ATCGGGG<br>
	 * 则会标记duplicate<br>
	 * <br>
	 * 那么20180129版本snpeff不标记duplicate而vep标记duplicate，我个人认为这里确实应该标记为duplicate<br>
	 * 因此如果存在这种情况，先比尾部看有没有duplicate，再比头部<br>
	 */
	public void initial() {
		copeInputVar(true);
		setVarHgvsType();
		setDuplicate();
	}

	/**
	 * 部分输入的indel类型如下：
	 * chr1	1234	ATACTACTG	ATAGCATTG
	 * 那么这个实际上可以合并为
	 * chr1	1237	CTAC	GCAT
	 * 
	 * @param isCompareStart 是否从头比到尾部
	 * 譬如存在
	 * ref ATA
	 *  ATACGCAT
	 * 
	 */
	@VisibleForTesting
	protected void copeInputVar(boolean isCompareStart) {
		seqStart = 0; seqEnd = 0;
		char[] refChr = seqRef.toCharArray();
		char[] altChr = seqAlt.toCharArray();
		if (isCompareStart) {
			compareStart(refChr, altChr);
			if (seqStart < Math.min(seqRef.length(), seqAlt.length())) {
				compareEnd(refChr, altChr);
			}
		} else {
			compareEnd(refChr, altChr);
			if (seqEnd < Math.min(seqRef.length(), seqAlt.length())) {
				compareStart(refChr, altChr);
			}
		}
		
		if (seqStart == 0 && seqEnd == 0) {
			return;
		}
		alignRef = new Align(alignRefRaw.toString());
		alignRef.setStartEndLoc(alignRefRaw.getStartAbs() + seqStart, alignRefRaw.getEndAbs() - seqEnd);
		alignRef.setCis5to3(true);
	}
	
	private void compareStart(char[] refChr, char[] altChr) {
		for (int i = 0; i < refChr.length; i++) {
			if (i > altChr.length-1) break;
			if (refChr[i] == altChr[i]) {
				seqStart++;
			} else {
				break;
			}
		}
	}
	
	private void compareEnd(char[] refChr, char[] altChr) {
		for (int i = 0; i < refChr.length; i++) {
			if (i > altChr.length-1) break;
			if (refChr[refChr.length-i-1] == altChr[altChr.length-i-1]) {
				seqEnd++;
			} else {
				break;
			}
		}
	}
	
	/**
	 * 检测var的duplication
	 * 譬如现在有 T [ATC] ATC ATC ATC ATC GCAT 在第一位的T后面插入了ATC
	 * 那么根据HGVS规范，这个应该写成T ATC ATC ATC ATC [ATC] GCAT
	 * 也就是重复区域放到最后的位置
	 * 那么我就要去提取染色体的序列并且把这个给对起来。
	 * 然后每次提取100bp的长度来做这个工作
	 * @param getSeqLen
	 */
	protected void setVarHgvsType() {
		if (getSeqRef().length() == 0 && getSeqAlt().length() == 0) {
			//不可能出现的错误
			throw new ExceptionNBCSnpHgvs("error ref and alt cannot both be empty!");
		}
		if (getSeqRef().length() == 1 && getSeqAlt().length() == 1) {
			varType = EnumHgvsVarType.Substitutions;
		} else if (getSeqRef().length() == 0) {
			varType = EnumHgvsVarType.Insertions;
		} else if (getSeqAlt().length() == 0) {
			varType = EnumHgvsVarType.Deletions;
		} else {
			varType = EnumHgvsVarType.Indels;
		}
	}
	
	protected void setDuplicate() {
		if (varType != EnumHgvsVarType.Insertions 
				&& varType != EnumHgvsVarType.Deletions
				&& varType != EnumHgvsVarType.Duplications) {
			return;
		}
		SnpRefAltDuplicate snpRefAltDuplicate = new SnpRefAltDuplicate(alignRef, getSeqRef(), getSeqAlt());
		snpRefAltDuplicate.setSeqLen(GetSeqLen);
		snpRefAltDuplicate.initial();
		snpRefAltDuplicate.modifySeq(seqHash);
		varType = snpRefAltDuplicate.getVarType();
		this.alignRef = snpRefAltDuplicate.getAlignRef();
		isDup = snpRefAltDuplicate.isDup();
	}
	
	public EnumHgvsVarType getVarType() {
		return varType;
	}
	
	public String getRefId() {
		return alignRef.getRefID();
	}
	/**
	 * 根据{@link #setIsDupMoveLast(boolean)}
	 * 的不通返回相应的坐标
	 * @return
	 */
	public int getStartReal() {
		return isDup && isDupMoveLast ? getMoveDuplicate().getStartAbs() : alignRef.getStartAbs();
	}
	/**
	 * 根据{@link #setIsDupMoveLast(boolean)}
	 * 的不通返回相应的坐标
	 * @return
	 */
	public int getEndReal() {
		return isDup && isDupMoveLast ? getMoveDuplicate().getEndAbs() : alignRef.getEndAbs();
	}
	
	/**
	 * 如果是duplicate的类型，将数据往回移动一次，主要用于剪接位点gt-ag这块<br>
	 * 譬如插入 T--ACATG[ACATG]T----------AG<br>
	 * 其中插入在G和T之间，则切换为<br>
	 * T--[ACATG]-ACATGT----------AG<br>
	 * @param isDupMoveLast 默认不移动
	 */
	@VisibleForTesting
	protected Align getMoveDuplicate() {
		if (!isDup) {
			throw new ExceptionNBCSnpHgvs("cannot move duplicate because it is not a duplicate site");
		}
		
		Align align = new Align(alignRef.toString());
		int seqLen = varType == EnumHgvsVarType.Duplications ? seqAlt.length() : seqRef.length();
		seqLen = seqLen - 1;
		align.setStartEndLoc(alignRef.getStartAbs()-seqLen, alignRef.getEndAbs()-seqLen);
		return align;
	}
	
	/**
	 * 目前仅给hgvsc使用，注意如果为Duplication，插入在第二个ATCG之后，如下：
	 * -ATCG-[A]TCG-[ATCG]-
	 * 此时本方法返回第二个ATCG的A的位置，也就是加中括弧的那个A
	 * @return
	 */
	protected int getStartPosition() {
		if (varType == EnumHgvsVarType.Duplications) {
			return alignRef.getStartAbs() - getSeqAlt().length() + 1;
		}
		return alignRef.getStartAbs();
	}
	/**
	 * 目前仅给hgvsc使用，注意如果为Duplication，插入在第二个ATCG之后，如下：
	 * -ATCG-ATC[G]-[ATCG]-
	 * 此时本方法返回第二个ATCG的G的位置，也就是加中括弧的那个G
	 * @return
	 */
	protected int getEndPosition() {
		if (varType == EnumHgvsVarType.Duplications) {
			return alignRef.getEndAbs() - 1;
		}
		return alignRef.getEndAbs();
	}
	public String getSeqAlt() {
		return seqAlt.substring(seqStart, seqAlt.length()-seqEnd);
	}
	public String getSeqRef() {
		return seqRef.substring(seqStart, seqRef.length()-seqEnd);
	}
	public Align getAlignRef() {
		return alignRef;
	}
	
	public static enum SnpIndelType {
		INSERT, DELETION, MISMATCH, CORRECT
	}
	
	public String toString() {
		return alignRef.getRefID() + "\t" + alignRef.getStartAbs() + "\t" + seqRef + "\t" + seqAlt;
	}
}

/**
 * 具体解释看这个
 * http://www.hgvs.org/mutnomen/recs-DNA.html#inv
 */
enum EnumHgvsVarType {
	Substitutions,
	Deletions,
	Duplications,
	Insertions,
	Indels,
	//后面这几个不好界定，先缓缓
	Inversions,
	Conversions,
	Translocations,
}
