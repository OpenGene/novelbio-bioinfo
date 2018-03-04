package com.novelbio.analysis.seq.snphgvs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;


/**
 * 对于单个位点的snp与indel的情况，可以保存多个不同的样本。
 * 在setSampleName()方法中可设定样本名，并获得该样本的信息。
 * @author zong0jie
 */
public class SnpInfo {
	private static final Logger logger = Logger.getLogger(SnpInfo.class);
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
	/** reference的序列 */
	String seqRefRaw;
	/** 改变之后的序列 */
	String seqAltRaw;
	
	//===整理过的序列 ============
	Align alignChange;
	String seqRef;
	String seqAlt;
	String seqHead;
	
	//==经过修改的坐标==
	/** realign的模块
	 * 修改后的ref的坐标区间
	 * 如<br>
	 * ref:TTCGATTC<br>
	 * chr1	2	TC	TCGATG<br>
	 * TTC[GATG]GATTC<br>
	 *修改为<br>
	 * chr1 4	A	ATGGA<br>
	 * TTCGA[TGGA]TTC<br>
	 */
	SnpIndelRealignHandle snpRealignHandler;
	EnumHgvsVarType varType;
	/** 是否为duplicate的类型 */
	boolean isDup;

	
	/**
	 * 影响到了哪几个转录本
	 * 本版本不考虑横跨多个基因的超长deletion
	 */
	List<GffGeneIsoInfo> lsIsos = new ArrayList<>();
	Map<GffGeneIsoInfo, SnpIsoHgvsc> mapIso2Hgvsc = new HashMap<>();
	Map<GffGeneIsoInfo, SnpIsoHgvsp> mapIso2Hgvsp = new HashMap<>();
	
	public SnpInfo(String refId, int position, String seqRef, String seqAlt) {
		int positionEnd = position + seqRef.length() - 1;
		alignRefRaw = new Align(refId, position, positionEnd);
		this.seqRefRaw = seqRef;
		this.seqAltRaw = seqAlt;
	}
	/** 仅用于测试 */
	@VisibleForTesting
	protected void setAlignRef(Align align) {
		alignChange = align;
	}
	/** 是否为duplicate类型 */
	public boolean isDup() {
		return isDup;
	}
	
	/** 移动到最后，仅用于测试 */
	@VisibleForTesting
	protected void moveToAfter() {
		if (snpRealignHandler != null) {
			snpRealignHandler.moveAlignToAfter();
		}
	}
	/** 移动到最后，仅用于测试 */
	@VisibleForTesting
	protected void moveToBefore() {
		if (snpRealignHandler != null) {
			snpRealignHandler.moveAlignToBefore();
		}
	}
	/** 可以最多移动几位，恒返回正数 */
	public int moveNumMax() {
		return snpRealignHandler == null ? 0 : snpRealignHandler.getMoveBefore();
	}
	
	/** 如果是duplicate的类型，将数据往回移动若干位，<br>
	 * 主要用于剪接位点gt-ag，起始密码子ATG，三联密码子中间等<br>
	 * 譬如插入 T--ACATG[ACATG]T----------AG<br>
	 * 其中插入在G和T之间，则切换为<br>
	 * T--[ACATG]-ACATGT----------AG<br>
	 * @param moveBefore 移动几位
	 * @param isCis
	 * true表示iso正向，此时align默认在最后，则向前移动
	 * false表示iso反向，此时align默认在最前，向后移动
	 * 
	 */
	public void moveAlign(int moveNum, boolean isCis) {
		//理论上不能发生的错误
		if (snpRealignHandler == null || moveNum < 0) {
			throw new ExceptionNBCSnpHgvs("error");
		}
		if (isCis) {
			snpRealignHandler.moveAlignBefore(moveNum);
		} else {
			snpRealignHandler.moveAlignAfter(moveNum);
		}
	}
	
	/** 如果是duplicate的类型，将数据往回移动若干位，<br>
	 * 主要用于剪接位点gt-ag，起始密码子ATG，三联密码子中间等<br>
	 * 譬如插入 T--ACATG[ACATG]T----------AG<br>
	 * 其中插入在G和T之间，则切换为<br>
	 * T--[ACATG]-ACATGT----------AG<br>
	 * @param moveBefore 向前移动几位
	 */
	public void moveAlignBefore(int moveBefore) {
		//理论上不能发生的错误
		if (snpRealignHandler == null || moveBefore < 0) {
			throw new ExceptionNBCSnpHgvs("error");
		}
		snpRealignHandler.moveAlignBefore(moveBefore);
	}
	/** 如果是duplicate的类型且iso为反相，此时默认align移动到最靠前，则将align往后移动若干位，<br>
	 * 主要用于剪接位点gt-ag，起始密码子ATG，三联密码子中间等<br>
	 * 譬如插入 T--ACATG[ACATG]T----------AG<br>
	 * 其中插入在G和T之间，则切换为<br>
	 * T--[ACATG]-ACATGT----------AG<br>
	 * @param moveBefore 向前移动几位
	 */
	public void moveAlignAfter(int moveBefore) {
		//理论上不能发生的错误
		if (snpRealignHandler == null || moveBefore < 0) {
			throw new ExceptionNBCSnpHgvs("error");
		}
		snpRealignHandler.moveAlignAfter(moveBefore);
	}
	/**
	 * 与方法 {@link #setGene(GffDetailGene)} 二选一<br>
	 * 给定GffHashGene
	 * 填充SnpInfo所需的list-iso
	 * @param gffHashGene
	 */
	public void setGffHashGene(GffHashGene gffHashGene) {
		GffCodGeneDU gffCodGeneDu = gffHashGene.searchLocation(alignRefRaw.getRefID(), alignRefRaw.getStartAbs(), alignRefRaw.getEndAbs());
		if (gffCodGeneDu == null) {
			return;
		}
		Set<GffDetailGene> setGenes = gffCodGeneDu.getCoveredOverlapGffGene();
		for (GffDetailGene gffDetailGene : setGenes) {
			lsIsos.addAll(gffDetailGene.getLsCodSplit());
		}
	}
	
	/** 
	 * 与方法 {@link #setGffHashGene(GffHashGene)} 二选一<br>
	 * 给定GffHashGene
	 * 填充SnpInfo所需的list-iso
	 * @param gffHashGene
	 */
	public void setGene(GffDetailGene gene) {
		 lsIsos.addAll(gene.getLsCodSplit());
	}
	
	/**
	 * 所影响的iso
	 */
	public List<GffGeneIsoInfo> getLsIsos() {
		return lsIsos;
	}
	public Map<GffGeneIsoInfo, SnpIsoHgvsc> getMapIso2Hgvsc() {
		return mapIso2Hgvsc;
	}
	public Map<GffGeneIsoInfo, SnpIsoHgvsp> getMapIso2Hgvsp() {
		return mapIso2Hgvsp;
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
	public void initial(SeqHashInt seqHash) {
		copeInputVar();
		realign(seqHash);
		
		mapIso2Hgvsc.clear();
		mapIso2Hgvsp.clear();
		for (GffGeneIsoInfo iso : lsIsos) {
			SnpIsoHgvsc snpIsoHgvsc = new SnpIsoHgvsc(this, iso);
			mapIso2Hgvsc.put(iso, snpIsoHgvsc);
		}
		for (GffGeneIsoInfo iso : lsIsos) {
			SnpIsoHgvsp snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(this, iso, seqHash);
			mapIso2Hgvsp.put(iso, snpIsoHgvsp);
		}
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
	protected void copeInputVar() {
		char[] refChr = seqRefRaw.toCharArray();
		char[] altChr = seqAltRaw.toCharArray();
		int seqLenMax = Math.max(refChr.length, altChr.length);

		int[] startEndSameIndex = SnpInfo.getStartEndSameIndex(refChr, altChr);
		int startSameIndex = startEndSameIndex[0], endSameIndex = startEndSameIndex[1];
		
	
		if (startSameIndex == 0 && endSameIndex == 0) {
			alignChange = new Align(alignRefRaw.toString());
			seqRef = seqRefRaw;
			seqAlt = seqAltRaw;
			setVarHgvsType(seqRef, seqAlt);
			return;
		}
		seqRef = seqRefRaw.substring(startSameIndex, seqRefRaw.length() - endSameIndex);
		seqAlt = seqAltRaw.substring(startSameIndex, seqAltRaw.length() - endSameIndex);
		setVarHgvsType(seqRef, seqAlt);
		alignChange = new Align(alignRefRaw.toString());
		
		int startSiteSubSeq = startSameIndex > 0 ? startSameIndex-1 : seqLenMax - endSameIndex;
		
		if (varType == EnumHgvsVarType.Insertions) {
			alignChange.setStartEndLoc(alignRefRaw.getStartAbs() + startSameIndex-1, alignRefRaw.getStartAbs() + startSameIndex);
			seqHead = seqAltRaw.substring(startSiteSubSeq, startSiteSubSeq+1);
		} else {
			alignChange.setStartEndLoc(alignRefRaw.getStartAbs() + startSameIndex, alignRefRaw.getEndAbs() - endSameIndex);
			if (varType == EnumHgvsVarType.Deletions) {
				seqHead = seqRefRaw.substring(startSiteSubSeq, startSiteSubSeq+1);
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
	protected void setVarHgvsType(String seqRef, String seqAlt) {
		if (seqRef.length() == 0 && seqAlt.length() == 0) {
			//不可能出现的错误
			throw new ExceptionNBCSnpHgvs("error ref and alt cannot both be empty!");
		}
		if (seqRef.length() == 1 && seqAlt.length() == 1) {
			varType = EnumHgvsVarType.Substitutions;
		} else if (seqRef.length() == 0) {
			varType = EnumHgvsVarType.Insertions;
		} else if (seqAlt.length() == 0) {
			varType = EnumHgvsVarType.Deletions;
		} else {
			varType = EnumHgvsVarType.Indels;
		}
	}
	
	protected void realign(SeqHashInt seqHash) {
		if (varType != EnumHgvsVarType.Insertions 
				&& varType != EnumHgvsVarType.Deletions
				&& varType != EnumHgvsVarType.Duplications) {
			return;
		}
		snpRealignHandler = new SnpIndelRealignHandle(alignChange, seqRef, seqAlt, seqHead);
		snpRealignHandler.setSeqLen(GetSeqLen);
		snpRealignHandler.handleSeqAlign(seqHash);
		varType = snpRealignHandler.getVarType();
		snpRealignHandler.moveAlignToAfter();
		isDup = snpRealignHandler.isDup();
	}
	
	public EnumHgvsVarType getVarType() {
		return varType;
	}
	
	public String getRefId() {
		return alignChange.getRefID();
	}
	/**
	 * 根据{@link #setIsDupMoveLast(boolean)}
	 * 的不同返回相应的坐标
	 * @return
	 */
	public int getStartReal() {
		return snpRealignHandler == null ? alignChange.getStartAbs() : snpRealignHandler.getRealign().getStartAbs();
	}
	/**
	 * 根据{@link #setIsDupMoveLast(boolean)}
	 * 的不同返回相应的坐标
	 * @return
	 */
	public int getEndReal() {
		return snpRealignHandler == null ? alignChange.getEndAbs() : snpRealignHandler.getRealign().getEndAbs();
	}
	
	/**
	 * 目前仅给hgvsc使用，注意如果为Duplication，插入在第二个ATCG之后，如下：
	 * -ATCG-[A]TCG-[ATCG]-
	 * 此时本方法返回第二个ATCG的A的位置，也就是加中括弧的那个A
	 * 
	 * <br>
	 * <b>注意：</b>当调用{@link #moveAlignBefore(int)}之后本返回值会变，只有不设置或当{@link #moveAlignBefore(int)}为0才能获得正确的值
	 * <br>
	 * @return
	 */
	protected int getStartPosition() {
		Align align = snpRealignHandler == null ? alignChange : snpRealignHandler.getAlignEnd();
		if (varType == EnumHgvsVarType.Duplications) {
			return align.getStartAbs() - getSeqAlt().length() + 1;
		}
		return align.getStartAbs();
	}
	/**
	 * 目前仅给hgvsc使用，注意如果为Duplication，插入在第二个ATCG之后，如下：
	 * -ATCG-ATC[G]-[ATCG]-
	 * 此时本方法返回第二个ATCG的G的位置，也就是加中括弧的那个G
	 * 
	 * <br>
	 * <b>注意：</b>当调用{@link #moveAlignBefore(int)}之后本返回值会变，只有不设置或当{@link #moveAlignBefore(int)}为0才能获得正确的值
	 * <br>
	 * @return
	 */
	protected int getEndPosition() {
		Align align = snpRealignHandler == null ? alignChange : snpRealignHandler.getAlignEnd();
		if (varType == EnumHgvsVarType.Duplications) {
			return align.getEndAbs() - 1;
		}
		return align.getEndAbs();
	}
	
	public String getSeqRef() {
		return snpRealignHandler == null ? seqRef : snpRealignHandler.getSeqRef();
	}
	public String getSeqAlt() {
		return snpRealignHandler == null ? seqAlt : snpRealignHandler.getSeqAlt();
	}
	public String getSeqHead() {
		return snpRealignHandler == null ? seqHead : snpRealignHandler.getSeqHead();
	}
	public Align getAlignRef() {
		return snpRealignHandler == null ? alignChange : snpRealignHandler.getRealign();
	}

	
	/** 修正过的位点信息 */
	public String toStringModify() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(alignChange.getRefID() + "\t");
		int startLen = varType == EnumHgvsVarType.Deletions ? 1 : 0;
		sBuilder.append(getAlignRef().getStartAbs() - startLen);
		sBuilder.append("\t");
		sBuilder.append(getSeqHead() + getSeqRef() + "\t");
		sBuilder.append(getSeqHead() + getSeqAlt());
		return sBuilder.toString();
	}
	public static enum SnpIndelType {
		INSERT, DELETION, MISMATCH, CORRECT
	}
	
	public String toString() {
		return alignRefRaw.getRefID() + "\t" + alignRefRaw.getStartAbs() + "\t" + seqRefRaw + "\t" + seqAltRaw;
	}
	
	/**
	 * 给定ref和alt，返回他们有几个相同的start和几个相同的end<br>
	 * 譬如<br>
	 *  ref P-M-AE<br>
	 *  alt P-CAAY-AE<br>
	 *  则返回<br>
	 *  0: 1      1: 2 <br>
	 * @param ref
	 * @param alt
	 */
	protected static int[] getStartEndSameIndex(char[] ref, char[] alt) {
		int startSameIndex = 0, endSameIndex = 0;
		int seqLenMax = Math.max(ref.length, alt.length);
		int seqLenMin = Math.min(ref.length, alt.length);

		startSameIndex = SnpIsoHgvsp.getStartSame(ref, alt);
		if (startSameIndex < seqLenMin) {
			endSameIndex = SnpIsoHgvsp.getEndSame(ref, alt);
			if (endSameIndex+startSameIndex >= seqLenMax) {
				endSameIndex = seqLenMin - startSameIndex;
			}
		}
		return new int[]{startSameIndex, endSameIndex};
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
