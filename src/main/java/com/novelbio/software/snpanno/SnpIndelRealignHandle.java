package com.novelbio.software.snpanno;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.StringOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqHashInt;
import com.novelbio.software.snpanno.SnpInfo.EnumHgvsVarType;

/**
 * 检测var的duplication
 * 譬如现在有 T [ATC] ATC ATC ATC ATC GCAT 在第一位的T后面插入了ATC
 * 那么根据HGVS规范，这个应该写成T ATC ATC ATC ATC [ATC] GCAT
 * 也就是重复区域放到最后的位置
 * 那么我就要去提取染色体的序列并且把这个给对起来。
 * 然后每次提取100bp的长度来做这个工作
 * 
 * 本类还有一个工作，譬如
 * ref ACTGCATCATGGCG
 * chr1 3	T	TGCGT
 * ACT-[GCGT]-GCATCATGGCG
 * 等价于
 * chr1 5	C	CGTGC
 * ACTGC-[GTGC]-ATCATGGCG
 * 把这种可以滚动的都滚到最右侧
 * @author zongjie
 */
public class SnpIndelRealignHandle {
	int seqLen = 100;

	String seqRef;
	String seqAlt;
	/** 插入或缺失在reference上的位置 */
	Align alignRef;

	EnumHgvsVarType varType;
	
	/** 传入的坐标 */
	int startLoc;
	/** 经过校正后，最靠右侧的起点 */
	int startAfter;
	/** 经过校正后，最靠左侧的起点 */
	int startBefore;
	/** 最左侧的在duplicate之前的base */
	char beforeBase;

	boolean isDup;
	
	/** 偏移到最尾部的align */
	RealignUnit realignRight = new RealignUnit();
	
	/** 偏移到最头部的align */
	RealignUnit realignLeft = new RealignUnit();
	
	/** 对于某个转录本的实际realign */
	RealignUnit realign = new RealignUnit();
	
	public SnpIndelRealignHandle(Align alignRef, String seqRef, String seqAlt, String seqShort) {
		this.alignRef = alignRef;
		this.seqRef = seqRef;
		this.seqAlt = seqAlt;
		if (StringOperate.isRealNull(seqShort)) {
			seqShort = "";
		}
		realign.setSeqChangeShort(seqShort);
	}
	
	/**
	 * 不同的转录本，其偏移是不一样的，所以这个记录在hgvsp中，
	 * 每次使用不同的hgvsp的时候，加载不同的 {@link RealignUnit}
	 * @param realign
	 */
	public void setRealign(RealignUnit realign) {
		this.realign = realign;
	}
	/** 获得当前的偏移情况 */
	public RealignUnit getRealignUnit() {
		return realign;
	}
	/** 如果本align可以前后移动， 经过校正后，最靠右侧的起点 */
	protected int getStartBefore() {
		return startBefore;
	}
	/** 如果本align可以前后移动，经过校正后，最靠左侧的起点 */
	protected int getStartAfter() {
		return startAfter;
	}
	
	protected char getBeforeBase() {
		return beforeBase;
	}
	
	protected String getSeqChange() {
		return realign.getSeqChange();
	}
	protected String getSeqHead() {
		return realign.getSeqChangeShort();
	}
	protected String getSeqRef() {
		return varType == EnumHgvsVarType.Deletions ? realign.getSeqChange() : "";
	}
	protected String getSeqAlt() {
		return varType == EnumHgvsVarType.Deletions ? "" : realign.getSeqChange();
	}
	
	public String getSeqChangeRight() {
		return realignRight.getSeqChange();//seqChangeRight;
	}
	protected String getSeqHeadRight() {
		return realignRight.getSeqChangeShort();//seqChangeShortRight;
	}
	protected String getSeqRefRight() {
		return varType == EnumHgvsVarType.Deletions ? realignRight.getSeqChange() : "";
	}
	protected String getSeqAltRight() {
		return varType == EnumHgvsVarType.Deletions ? "" : realignRight.getSeqChange();
	}
	
	public String getSeqChangeLeft() {
		return realignLeft.getSeqChange();//seqChangeLeft;
	}
	protected String getSeqHeadLeft() {
		return realignLeft.getSeqChangeShort();//seqChangeShortLeft;
	}
	protected String getSeqRefLeft() {
		return varType == EnumHgvsVarType.Deletions ? realignLeft.getSeqChange() : "";
	}
	protected String getSeqAltLeft() {
		return varType == EnumHgvsVarType.Deletions ? "" : realignLeft.getSeqChange();
	}
	
	public EnumHgvsVarType getVarType() {
		return varType;
	}
	public boolean isDup() {
		return isDup;
	}
	@VisibleForTesting
	protected void setSeqLen(int getSeqLen) {
		seqLen = getSeqLen;
	}
	
	protected void handleSeqAlign(SeqHashInt seqHash) {
		startLoc = alignRef.getStartAbs();
		startBefore = startAfter = alignRef.getStartAbs();
		String seqIndel = seqRef.length() == 0 ? seqAlt : seqRef;
		int startLocModify = seqRef.length() > 0 ? alignRef.getStartAbs() : alignRef.getStartAbs() + 1;
		compareSeq(startLocModify, seqHash, seqIndel);
		/** 如果一个片段移动到最左侧和最右侧的距离是该片段的2倍长度，那么该片段即为Dup
		 * 如 ATC[AGATC]AG
		 * 为 ATCAG-ATCAG
		 */
		if (startAfter - startBefore >= seqIndel.length()) {
			isDup = true;
		}
		
		if (isDup) {
			varType = seqRef.length() > 0 ? EnumHgvsVarType.Deletions : EnumHgvsVarType.Duplications;
		} else {
			varType = seqRef.length() > 0 ? EnumHgvsVarType.Deletions : EnumHgvsVarType.Insertions;
		}
	}
	
	/** 可以向前移动几位，恒返回正数，从startAfter开始算 */
	protected int getMoveBefore() {
		return startAfter - startBefore;
	}
	/** 在方法{@link #moveAlignBefore(int)}
	 * 和 {@link #moveAlignToAfter()}
	 * 执行后会跟着调整
	 * @return
	 */
	public Align getRealign() {
		return realign.getRealign();
	}
	/** 移动到尾部的align，主要给hgvsc使用 */
	public Align getAlignRight() {
		return realignRight.getRealign();
	}
	public Align getAlignLeft() {
		return realignLeft.getRealign();
	}
	/** 我们假定默认<b>align已经移动到最右端<b>，这样我们只需要将align
	 * 向左端移动即可
	 * @param moveNum 恒为正数
	 * @return
	 */
	protected Align moveAlignBefore(int moveNum) {		
		int moveEnd = startAfter-startLoc;
		int move = moveEnd-moveNum;
		if (move < startBefore-startLoc || move > startAfter-startLoc) {
			throw new ExceptionNBCSnpHgvs("cannot move to such before "+moveNum + ". Max move number is " + (startAfter-startBefore));
		}
		changeSeq(move);
		Align align = generateNewAlign(move);
		realign.setRealign(align);
		return align;
	}
	/** 我们假定默认<b>align已经移动到最左端<b>，这样我们只需要将align
	 * 向右端移动即可
	 * @param moveNum 恒为正数
	 * @return
	 */
	protected Align moveAlignAfter(int moveNum) {		
		int moveBefore = startBefore-startLoc;
		int move = moveBefore+moveNum;
		if (move < startBefore-startLoc || move > startAfter-startLoc) {
			throw new ExceptionNBCSnpHgvs("cannot move to such before "+moveNum + ". Max move number is " + (startAfter-startBefore));
		}
		changeSeq(move);
		Align align = generateNewAlign(move);
		realign.setRealign(align);
		return align;
	}
	protected Align moveAlignToAfter() {
		int moveEnd = startAfter-startLoc;
		changeSeq(moveEnd);
		Align align = generateNewAlign(moveEnd);
		realign.setRealign(align);
		realignRight = realign.clone();
		return align;
	}
	protected Align moveAlignToBefore() {
		int moveAfter = startBefore-startLoc;
		changeSeq(moveAfter);
		Align align = generateNewAlign(moveAfter);
		realign.setRealign(align);
		realignLeft = realign.clone();
		return align;
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
	 * 
	 * @param startLocModify 如果是deletion 则为当前位，如果是 insertion，则为 插入的后一位
	 * ATC-[GCT]-GCT
	 * 则插入位原来是3，修改为4
	 */
	protected void compareSeq(int startLocModify, SeqHashInt seqHash, String seqIndel) {
		//获取步长
		int lenStep = (int)Math.ceil((double)seqLen/seqIndel.length()) * seqIndel.length()-1;
		int startBefore = startLocModify - seqIndel.length();
		int start = startBefore <= 0? 1 : startBefore;
		SeqFasta seqFasta = seqHash.getSeq(alignRef.getChrId(), start, startLocModify + lenStep);
		String seq = seqFasta.toString();
		
		int beforeSpace = startBefore < 0 ? Math.abs(startBefore)+1 : 0;
		
		char[] before = new char[seqIndel.length()];
		char[] beforeTmp = seq.substring(0, seqIndel.length()-beforeSpace).toUpperCase().toCharArray();

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
		char[] seqIndelChr = seqIndel.toUpperCase().toCharArray();
		boolean isGetNextSeq = true;
		boolean isFirst = true;
		int startLoc = startLocModify;
		while (isGetNextSeq) {
			if (isFirst) {
				seq = seqRemain;
				isFirst = false;
			} else {
				if (startLoc > seqHash.getChrLength(alignRef.getChrId())) {
					isGetNextSeq = false;
					continue;
				}
				seq = seqHash.getSeq(alignRef.getChrId(), startLoc, startLoc + lenStep).toString();
				if (seq.length() < lenStep+1) {
					seq = fillSeq(seq, lenStep+1, false);
					isGetNextSeq = false;
				}
			}
			if (seq.length() < seqIndelChr.length) {
				isGetNextSeq = false;
			} else {
				isGetNextSeq = compareSeqAfter(seq, seqIndelChr, before);
			}
			startLoc = startLoc + lenStep + 1;
		}
		
		startLoc = startLocModify;
		boolean isGetBeforeSeq = true;
		int loopNum = 0;//最多循环三次，三次都找不到头部就可以不找了
		while (isGetBeforeSeq ) {
			seq = seqHash.getSeq(alignRef.getChrId(), startLoc-lenStep-1, startLoc-1).toString();
			if (seq.length() < lenStep+1) {
				seq = fillSeq(seq, lenStep+1, true);
			}
			
			if (loopNum++ >= 3) {
				beforeBase = seq.charAt(seq.length()-1);
				break;
			}
			
			isGetBeforeSeq = compareSeqBefore(seq, seqIndelChr);
			startLoc = startLoc - lenStep - 1;
			if (startLoc < 0) {
				break;
			}
		}
	}
	
	/** 如果序列太短，则把序列补齐，一般在序列的头部和尾部会出现问题
	 * 正式项目中应该不会出现
	 * 测试中出现的问题
	 * @return
	 */
	private String fillSeq(String seq, int len, boolean fillHead) {
		char[] tmp = new char[len - seq.length()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = ' ';
		}
		if (fillHead) {
			seq = new String(tmp) + seq;
		} else {
			seq = seq + new String(tmp);
		}
		return seq;
	}
	
	/**
	 * 将indel和提取出来的序列进行比较
	 * 并返回是否需要继续比较
	 * @param seq
	 * @param seqIndel
	 * @param seqIndelBefore 根前面比，主要是看是否为duplicate
	 * 因为存在类型  GC-[ATC]-AT 可以改写为  GCAT-[CAT] 需要获得前面的C才能判定是否为duplicate
	 * @return 是否需要继续提取序列并进行比较
	 */
	private boolean compareSeqAfter(String seqstr, char[] seqIndel, char[] seqIndelBefore) {
		boolean isNext = true;
		int samNum = 0;
		char[] seq = seqstr.toUpperCase().toCharArray();
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
				startAfter = startAfter + samNum; 
				samNum = 0;
			} else {
				isNext = false;
				break;
			}
		}
		
		if (!isNext && samNum > 0 && samNum < seqIndel.length) {
			startAfter = startAfter + samNum; 
		}
		
		return isNext;
	}
	
	/**
	 * 将indel和提取出来的序列进行比较
	 * 并返回是否需要继续比较
	 * @param seq
	 * @param seqIndel
	 * @param seqIndelBefore 根前面比，主要是看是否为duplicate
	 * 因为存在类型  GC-[ATC]-AT 可以改写为  GCAT-[CAT] 需要获得前面的C才能判定是否为duplicate
	 * @return 是否需要继续提取序列并进行比较
	 */
	private boolean compareSeqBefore(String seqstr, char[] seqIndel) {
		boolean isBefore = true;
		int samNum = 0;
		char[] seq = seqstr.toUpperCase().toCharArray();
		for (int i = seq.length-seqIndel.length; i >= 0; i=i - seqIndel.length) {
			boolean isSame = true;
			for (int j = seqIndel.length - 1; j >= 0; j--) {
				if (seq[i+j] != seqIndel[j]) {
					isSame = false;
					beforeBase = seq[i+j];
					break;
				}
				samNum++;
			}
			if (isSame) {
				startBefore = startBefore - samNum; 
				samNum = 0;
			} else {
				isBefore = false;
				break;
			}
		}
		
		if (!isBefore && samNum > 0 && samNum < seqIndel.length) {
			startBefore = startBefore - samNum; 
		}
		return isBefore;
	}
	
	/**
	 * 根据需要偏移的位置，重新进行align
	 * 注意0点位startLoc，负数表示向前移动，正数表示向后移动
	 * @param moveNumber 偏移数量，负数为向做偏移，正数为向右偏移
	 * @return
	 */
	private Align generateNewAlign(int moveNumber) {
		int length = alignRef.getLength();
		Align alignNew = new Align(alignRef.toString());
		int site = startLoc + moveNumber;
		alignNew.setStartAbs(site);
		alignNew.setEndAbs(site + length - 1);
		return alignNew;
	}
	
	/**
	 * 根据需要偏移的位置，重新进行align
	 * 注意0点位startLoc，负数表示向前移动，正数表示向后移动
	 * @param moveNumber
	 */
	private void changeSeq(int moveNumber) {
		//理论上不可能发生的错误
		if (moveNumber < startBefore-startLoc || moveNumber > startAfter-startLoc) {
			throw new ExceptionNBCSnpHgvs("move number error!");
		}
		
		String seq = seqRef.length() > 0 ? seqRef : seqAlt;
		int shiftNum = moveNumber%seq.length();
		if (shiftNum <= 0) {
			shiftNum += seq.length();
		}
		
		String seqChange = seq.substring(shiftNum) + seq.substring(0, shiftNum);
		realign.setSeqChange(seqChange);
		String seqChangeShort = null;
		if (moveNumber == startBefore - startLoc) {
			seqChangeShort = beforeBase+"";
		} else {
			seqChangeShort = seq.substring(shiftNum-1, shiftNum);
		}
		realign.setSeqChangeShort(seqChangeShort);
	}
	
}


class RealignUnit {
	/** 移动之后的realign，考虑了剪接位点等因素，移动到了最合适的位置
	 * 譬如如果是正向转录本则右移
	 * 反向转录本则左移
	 */
	Align realign;
	/**
	 * 这里只可能是insertion或deletion，因此只有一条序列存在，另一条为空<br>
	 * 根据 {@link #convertSeqNum} 来修改序列<br>
	 * 最后获得 AGGC
	 */
	String seqChange;
	/**
	 * 同上
	 * 最后获得 C
	 */
	String seqChangeShort;
	
	public void setRealign(Align realign) {
		this.realign = realign;
	}
	public void setSeqChange(String seqChange) {
		this.seqChange = seqChange;
	}
	public void setSeqChangeShort(String seqChangeShort) {
		this.seqChangeShort = seqChangeShort;
	}
	public Align getRealign() {
		return realign;
	}
	public String getSeqChange() {
		return seqChange;
	}
	public String getSeqChangeShort() {
		return seqChangeShort;
	}
	
	public RealignUnit clone() {
		RealignUnit realignUnit = new RealignUnit();
		realignUnit.realign = this.realign.clone();
		realignUnit.seqChange = this.seqChange;
		realignUnit.seqChangeShort = this.seqChangeShort;
		return realignUnit;
	}
}