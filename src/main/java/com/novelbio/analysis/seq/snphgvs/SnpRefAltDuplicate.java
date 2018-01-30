package com.novelbio.analysis.seq.snphgvs;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.SeqFasta;
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
	int seqLen = 100;

	String seqRef;
	String seqAlt;
	/** 插入或缺失在reference上的位置 */
	Align alignRef;
	
	EnumHgvsVarType varType;
	
	int startLoc;
	/** 经过校正后的起点 */
	int startReal;
	boolean isDup;
	
	/**
	 * ref ATGCCG
	 * chr1 2	T	TGCAG
	 * chr1	4	C	CAGGC
	 * 
	 * ATGCAGGCCG
	 * 这两个是等价的，这时候 {@link #convertSeqNum}为2
	 */
	int  convertSeqNum;
	/**
	 * 这里只可能是insertion或deletion，因此只有一条序列存在，另一条为空
	 * 根据 {@link #convertSeqNum} 来修改序列
	 * 最后获得 AGGC
	 */
	String seqChange;
	/**
	 * 同上
	 * 最后获得 C
	 */
	String seqChangeShort;
	
	public SnpRefAltDuplicate(Align alignRef, String seqRef, String seqAlt, String seqShort) {
		this.alignRef = alignRef;
		this.seqRef = seqRef;
		this.seqAlt = seqAlt;
		this.seqChangeShort = seqShort;
	}
	
	/** {@link #compareSeq(String, char[])}比较结束后可以获得修正后的align */
	public Align getAlignRef() {
		return alignRef;
	}
	public String getSeqChange() {
		return seqChange;
	}
	public String getSeqChangeShort() {
		return seqChangeShort;
	}
	public EnumHgvsVarType getVarType() {
		return varType;
	}
	public boolean isDup() {
		return isDup;
	}
	@VisibleForTesting
	public void setSeqLen(int getSeqLen) {
		seqLen = getSeqLen;
	}
	
	protected void initial() {
		startLoc = seqRef.length() > 0 ? alignRef.getStartAbs() : alignRef.getStartAbs() + 1;
		startReal = alignRef.getStartAbs();
	}
	
	protected void modifySeq(SeqHashInt seqHash) {
		String seqIndel = seqRef.length() == 0 ? seqAlt : seqRef;
		compareSeq(seqHash, seqIndel);
		generateNewAlign(seqIndel);
		changeSeq();
	}
	
	/**
	 * indel有这种类型比较难处理（符号-主要用来断字，没什么实际意义）<br>
	 * ref: TTT-ATCAC-GCGCAGATC-TTTT<br>
	 * <br>
	 * 插入 chr1	8	C	CGCGCAGATC-ATCAC<br>
	 * 产生 TTT-ATCAC-[GCGCAGATC-ATCAC]-GCGCAGATC-TTTT<br>
	 * 实际上为 TTT-ATCACGCGCAGATC-ATCACGCGCAGATC-TTTT<br>
	 * 将其变换为  <b>chr1	17	C	CATCACGCGCAGATC</b><br>
	 * 也就是我们最后所需要的结果<br>
	 * <br>
	 * 我们虽然插入的是GCGCAGATCATCAC 但是产生的结果却是ATCACGCGCAGATC的duplicate<br>
	 * 因此我们需要用一定的算法把这种duplicate给鉴定出来<br>
	 * <br>
	 * 我们注意到插入的GCGCAGATCATCAC 是由两部分组成， GCGCAGATC---ATCAC<br>
	 * 而ref为TTT-ATCAC***GCGCAGATC-TTTT，其中***表示插入位置<br>
	 * <br>
	 * 那么实际上 GCGCAGATC与ref前面的ATCAC组合，ATCAC与ref后面的GCGCAGATC组合<br>
	 * 即为 TTT---[ATCAC---GCGCAGATC]---[ATCAC----GCGCAGATC]-TTTT，形成duplicate<br>
	 * 因此我们就需要循环比对这个过程<br>
	 * <br>
	 * 首先我们要获取 TTT-ATCAC --|-- GCGCAGATC-TTTT 两部分<br>
	 * 然后用 GCGCAGATC-ATCAC 与后边的GCGCAGATC-TTTT 进行比对，一直到比不上为止。<br>
	 * 那么会剩下ATCAC这5个碱基，再拿这5个碱基和头部TTT-ATCAC的5个碱基进行比较，看能不能比上。<br>
	 * 如果比上了，那就说明是duplicate<br>
	 * <br>
	 * 为了方便比对，我们对于头部的 TTT-ATCAC，会用空格将其补充为 ______TTT-ATCAC。注意前面有6个空位<br>
	 * 这6个空位也就是代码中的变量 beforeSpace<br>
	 */
	protected void compareSeq(SeqHashInt seqHash, String seqIndel) {
		//获取步长
		int lenStep = (int)Math.ceil((double)seqLen/seqIndel.length()) * seqIndel.length()-1;
		int startBefore = startLoc - seqIndel.length();
		int start = startBefore <= 0? 1 : startBefore;
		SeqFasta seqFasta = seqHash.getSeq(alignRef.getRefID(), start, startLoc + lenStep);
		String seq = seqFasta.toString();
		
		int beforeSpace = startBefore < 0 ? Math.abs(startBefore)+1 : 0;
		
		char[] before = new char[seqIndel.length()];
		char[] beforeTmp = seq.substring(0, seqIndel.length()-beforeSpace).toLowerCase().toCharArray();

		for (int i = beforeSpace; i < before.length; i++) {
			before[i] = beforeTmp[i-beforeSpace];
		}
		/**
		 * 如果为deletion，此时 seqRef.length() > 0
		 * ATCAC-[ACTT]-TCAG
		 * 直接比ref为 [ACTT]-TCAG 一定会发现deletion和ref的[ACTT]一致
		 * 因此需要把头部的[ACTT]去掉
		 * 
		 * 如果为insertion
		 * ATCAC-[ACTT]-TCAG
		 * 直接比ref为 TCAG 就不会找到一致
		 */
		int startNum = seqIndel.length();
		if (seqRef.length() > 0) {
			startNum = seqIndel.length() + seqIndel.length();
			startBefore = startBefore + seqIndel.length();
		}
		String seqRemain = seq.substring(startNum-beforeSpace, seq.length());
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
			isGetNextSeq = compareSeq(seq, seqIndelChr, before);
			startLoc = startLoc + lenStep + 1;
		}
	}

	/**
	 * 插入ATCGACGT
	 * 如果为 TT-ATCGAC-[GTATCGAC]-GT，也就是插入在中间或者尾部
	 * 这种类型，需要将前面的 TT-ATCGAC 拿出来比一次这样才能获得最好的结果
	 * 
	 * 但是可能头部就是 [ATCGAC]-GTATCGAC-GT，也就是前面没有TT了
	 * 我们在这里用空位补齐，反正后面也用不到，最后返回 [--ATCGAC]
	 * 那么直接提取会报错，所以这里要处理下
	 */
	private char[] getSeqBefore(SeqHashInt seqHash, String seqIndel, int lenStep) {
		int startReal = startLoc - seqIndel.length();
		int start = startReal <= 0? 1 : startReal;
		SeqFasta seqFasta = seqHash.getSeq(alignRef.getRefID(), start, startLoc + lenStep);
		String seq = seqFasta.toString();
		
		int beforeSpace = startReal < 0 ? Math.abs(startReal) : 0;
		
		char[] before = new char[beforeSpace + seq.length()];
		char[] beforeTmp = seq.substring(0, seqIndel.length()).toLowerCase().toCharArray();

		for (int i = beforeSpace; i < before.length; i++) {
			before[i] = beforeTmp[i-beforeSpace];
		}
		return before;
	}
	
	private void generateNewAlign(String seqIndel) {
		//在if之前，T [ATC] ATC ATC ATC ATC GCAT
		//startReal在17位也就是GCAT的G位，减去3位变成14位
//		if (seqRef.length() > 0) {
//			startReal = startReal - seqIndel.length();
//		}
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
	private boolean compareSeq(String seqstr, char[] seqIndel, char[] seqIndelBefore) {
		boolean isNext = true;
		int samNum = 0;
		char[] seq = seqstr.toLowerCase().toCharArray();
		for (int i = 0; i < seq.length; i=i + seqIndel.length) {
			boolean isSame = true;
			for (int j = 0; j < seqIndel.length; j++) {
				if (seq[i+j] != seqIndel[j]) {
					isSame = false;
					break;
				}
				samNum++;
			}
			if (isSame) {
				isDup = true;
				startReal = startReal + samNum; 
				samNum = 0;
			} else {
				isNext = false;
				break;
			}
		}
		
		if (!isNext && samNum > 0 && samNum < seqIndel.length) {
			convertSeqNum = samNum;
			startReal = startReal + samNum; 
		}
		
		if (!isNext && (samNum > 0 || !isDup)) {
			boolean isDupBefore = true;
			for (int i = samNum; i < seqIndel.length; i++) {
				if (seqIndel[i] != seqIndelBefore[i]) {
					isDupBefore = false;
					break;
				}
			}
			if (isDupBefore) {
				isDup = true;
			}
		}
		return isNext;
	}
	
	private void changeSeq() {
		String seq = seqRef.length() > 0 ? seqRef : seqAlt;
		if (convertSeqNum <= 0) {
			seqChange = seq;
			return;
		}
		seqChange = seq.substring(convertSeqNum) + seq.substring(0, convertSeqNum);
		seqChangeShort = seq.substring(convertSeqNum-1, convertSeqNum);
	}
	
}
