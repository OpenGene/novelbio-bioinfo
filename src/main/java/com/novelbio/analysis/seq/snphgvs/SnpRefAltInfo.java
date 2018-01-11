package com.novelbio.analysis.seq.snphgvs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.SeqFasta;
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
	Align alignRef;
	/** reference的序列 */
	String seqRef;
	
	/** 改变之后的序列 */
	String seqAlt;

	EnumHgvsVarType varType;
	
	/**
	 * 影响到了哪几个转录本
	 * 本版本不考虑横跨多个基因的超长deletion
	 */
	List<GffGeneIsoInfo> lsIsos = new ArrayList<>();
	
	SeqHashInt seqHash;
	
	public SnpRefAltInfo(String refId, int position, String seqRef, String seqAlt) {
		int positionEnd = position + seqRef.length() - 1;
		alignRef = new Align(refId, position, positionEnd);
		this.seqRef = seqRef;
		this.seqAlt = seqAlt;
	}
	/** 设定序列 */
	public void setSeqHash(SeqHashInt seqHash) {
		this.seqHash = seqHash;
	}
	
	/** 仅用于测试 */
	@VisibleForTesting
	protected void setAlignRef(Align align) {
		this.alignRef = align;
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
	
	public void initial() {
		copeInputVar();
		setVarHgvsType();
	}
	
	/**
	 * 部分输入的indel类型如下：
	 * ATACTACTG
	 * ATAGCATTG
	 * 那么这个实际上可以合并为
	 * CTAC
	 * GCAT
	 */
	@VisibleForTesting
	protected void copeInputVar() {
		int startNum = 0, endNum = 0;
		char[] refChr = seqRef.toCharArray();
		char[] altChr = seqAlt.toCharArray();
		for (int i = 0; i < refChr.length; i++) {
			if (i > altChr.length-1) break;
			if (refChr[i] == altChr[i]) {
				startNum++;
			} else {
				break;
			}
		}
		if (startNum < Math.min(seqRef.length(), seqAlt.length())) {
			for (int i = 0; i < refChr.length; i++) {
				if (i > altChr.length-1) break;
				if (refChr[refChr.length-i-1] == altChr[altChr.length-i-1]) {
					endNum++;
				} else {
					break;
				}
			}
		}
		
		if (startNum == 0 && endNum == 0) {
			return;
		}
		seqRef = seqRef.substring(startNum, seqRef.length()-endNum);
		seqAlt = seqAlt.substring(startNum, seqAlt.length()-endNum);

		alignRef.setStartEndLoc(alignRef.getStartAbs() + startNum, alignRef.getEndAbs() - endNum);
		alignRef.setCis5to3(true);
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
		if (seqRef.length() == seqAlt.length() && seqRef.length() == 1) {
			varType = EnumHgvsVarType.Substitutions;
		} else if (seqRef.length() > 1 && seqAlt.length() > 1) {
			varType = EnumHgvsVarType.Indels;
		}
		//如果是缺失，如A-TC-G-->"A-G"，此时startAbs是T位，则直接使用startAbs
		//如果是插入，如AT --> A-CA-T ，此时startAbs是A位，则需要把startAbs+1
		int startlocation = seqRef.length() > 0 ? alignRef.getStartAbs() : alignRef.getStartAbs() + 1;
		char[] seqIndel = seqRef.length() == 0 ? seqAlt.toLowerCase().toCharArray() : seqRef.toLowerCase().toCharArray();
		int lenStep = (int)Math.ceil((double)GetSeqLen/seqIndel.length) * seqIndel.length-1;
		int startReal = alignRef.getStartAbs();
		boolean isGetNextSeq = true;
		while (isGetNextSeq) {
			SeqFasta seqFasta = seqHash.getSeq(alignRef.getRefID(), startlocation, startlocation + lenStep);
			char[] seq = seqFasta.toString().toLowerCase().toCharArray();
		
			for (int i = 0; i < seq.length; i=i + seqIndel.length) {
				boolean isSame = true;
				for (int j = 0; j < seqIndel.length; j++) {
					if (seq[i+j] != seqIndel[j]) {
						isSame = false;
						break;
					}
				}
				if (isSame) {
					startReal = startReal + seqIndel.length; 
				} else {
					isGetNextSeq = false;
					break;
				}
			}
			startlocation = startlocation + lenStep + 1;
		}
		//在if之前，T [ATC] ATC ATC ATC ATC GCAT
		//startReal在17位也就是GCAT的G位，减去3位变成14位
		if (seqRef.length() > 0) {
			startReal = startReal - seqIndel.length;
		}
		if (startReal > alignRef.getStartAbs()) {
			varType = seqRef.length() > 0 ? EnumHgvsVarType.Deletions : EnumHgvsVarType.Duplications;
		} else {
			varType = seqRef.length() > 0 ? EnumHgvsVarType.Deletions : EnumHgvsVarType.Insertions;
		}
		int length = alignRef.getLength();
		alignRef.setStartAbs(startReal);
		alignRef.setEndAbs(startReal + length - 1);		
	}
	
	public EnumHgvsVarType getVarType() {
		return varType;
	}
	
	public String getRefId() {
		return alignRef.getRefID();
	}
	public int getStartPosition() {
		return alignRef.getStartAbs();
	}
	public int getEndPosition() {
		return alignRef.getEndAbs();
	}
	public String getSeqAlt() {
		return seqAlt;
	}
	public String getSeqRef() {
		return seqRef;
	}
	public Align getAlignRef() {
		return alignRef;
	}
	
	public static enum SnpIndelType {
		INSERT, DELETION, MISMATCH, CORRECT
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
