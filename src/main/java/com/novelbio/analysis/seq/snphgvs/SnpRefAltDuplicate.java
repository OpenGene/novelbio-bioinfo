package com.novelbio.analysis.seq.snphgvs;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.analysis.seq.mapping.Align;

/**
 * 检测var的duplication
 * 譬如现在有 T [ATC] ATC ATC ATC ATC GCAT 在第一位的T后面插入了ATC
 * 那么根据HGVS规范，这个应该写成T ATC ATC ATC ATC [ATC] GCAT
 * 也就是重复区域放到最后的位置
 * 那么我就要去提取染色体的序列并且把这个给对起来。
 * 然后每次提取100bp的长度来做这个工作
 * @author zongjie
 */
public class SnpRefAltDuplicate {
	int GetSeqLen = 100;

	String seqRef;
	String seqAlt;
	/** 插入或缺失在reference上的位置 */
	Align alignRef;
	
	EnumHgvsVarType varType;
	
	int startLoc;
	/** 经过校正后的起点 */
	int startReal;
	boolean isDup;

	public SnpRefAltDuplicate(Align alignRef, String seqRef, String seqAlt) {
		this.alignRef = alignRef;
		this.seqRef = seqRef;
		this.seqAlt = seqAlt;
	}
	
	/** {@link #compareSeq(String, char[])}比较结束后可以获得修正后的align */
	public Align getAlignRef() {
		return alignRef;
	}
	public EnumHgvsVarType getVarType() {
		return varType;
	}
	public boolean isDup() {
		return isDup;
	}
	@VisibleForTesting
	public void setGetSeqLen(int getSeqLen) {
		GetSeqLen = getSeqLen;
	}
	
	protected void initial() {
		startLoc = seqRef.length() > 0 ? alignRef.getStartAbs() : alignRef.getStartAbs() + 1;
		startReal = alignRef.getStartAbs();
	}
	
	protected void modifySeq(SeqHashInt seqHash) {
		String seqIndel = seqRef.length() == 0 ? seqAlt : seqRef;
		compareSeq(seqHash, seqIndel);
		generateNewAlign(seqIndel);
	}
	
	protected void compareSeq(SeqHashInt seqHash, String seqIndel) {
		int lenStep = (int)Math.ceil((double)GetSeqLen/seqIndel.length()) * seqIndel.length()-1;

		SeqFasta seqFasta = seqHash.getSeq(alignRef.getRefID(), startLoc-seqIndel.length(), startLoc + lenStep);
		String seq = seqFasta.toString();
		String seqLast = seq.substring(0, seqIndel.length());
		int startRealThis = startReal;
		if (seqLast.equalsIgnoreCase(seqIndel)) {
			isDup = true;
			startRealThis = startReal - seqIndel.length();
		}
		String seqRemain = seq.substring(seqIndel.length(), seq.length());
		char[] seqIndelChr = seqIndel.toLowerCase().toCharArray();
		boolean isGetNextSeq = true;
		boolean isFirst = true;
		while (isGetNextSeq) {
			if (isFirst) {
				seq = seqRemain;
				isFirst = false;
			} else {
				seq = seqHash.getSeq(alignRef.getRefID(), startLoc, startLoc + lenStep).toString();
			}
			isGetNextSeq = compareSeq(seq, seqIndelChr);
			startLoc = startLoc + lenStep + 1;
		}
		if (startReal == 0) {
			startReal = startRealThis;
		}
	}
	
	private void generateNewAlign(String seqIndel) {
		//在if之前，T [ATC] ATC ATC ATC ATC GCAT
		//startReal在17位也就是GCAT的G位，减去3位变成14位
		if (seqRef.length() > 0) {
			startReal = startReal - seqIndel.length();
		}
		if (isDup) {
			varType = seqRef.length() > 0 ? EnumHgvsVarType.Deletions : EnumHgvsVarType.Duplications;
		} else {
			varType = seqRef.length() > 0 ? EnumHgvsVarType.Deletions : EnumHgvsVarType.Insertions;
		}
		int length = alignRef.getLength();
		Align alignNew = new Align(alignRef.toString());
		alignNew.setStartAbs(startReal);
		alignNew.setEndAbs(startReal + length - 1);
		alignRef = alignNew;
	}
	
	/**
	 * 将indel和提取出来的序列进行比较
	 * 并返回是否需要继续比较
	 * @param seq
	 * @param seqIndel
	 * @return 是否需要继续提取序列并进行比较
	 */
	private boolean compareSeq(String seqstr, char[] seqIndel) {
		char[] seq = seqstr.toLowerCase().toCharArray();
		for (int i = 0; i < seq.length; i=i + seqIndel.length) {
			boolean isSame = true;
			for (int j = 0; j < seqIndel.length; j++) {
				if (seq[i+j] != seqIndel[j]) {
					isSame = false;
					break;
				}
			}
			if (isSame) {
				isDup = true;
				startReal = startReal + seqIndel.length; 
			} else {
				return false;
			}
		}
		return true;
	}
	
}
