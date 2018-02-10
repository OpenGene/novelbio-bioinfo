package com.novelbio.analysis.seq.snphgvs;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.CodeInfo;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;

public abstract class SnpIsoHgvsp {
	Set<EnumVariantClass> setVarType = new LinkedHashSet<>();
	
	SnpInfo snpRefAltInfo;
	GffGeneIsoInfo iso;
	
	boolean isNeedAA3 = true;
	
	/** 需要将alt替换ref的碱基，这里记录替换ref的起点 */
	int snpOnReplaceLocStart;
	/** 需要将alt替换ref的碱基，这里记录替换ref的终点 */
	int snpOnReplaceLocEnd;
	
	/** 如果引起了氨基酸变化，则该start所在读码框三联密码子的起点坐标 */
	int startCds;
	/** 如果引起了氨基酸变化，则该end所在读码框三联密码子的终点坐标 */
	int endCds;
	
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	SeqFasta refSeqNrForAA;
	/** 如果snp落在了exon上，则该类来保存ref所影响到的氨基酸的序列 */
	SeqFasta altSeqNrForAA;
	
	public SnpIsoHgvsp(SnpInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		this.snpRefAltInfo = snpRefAltInfo;
		this.iso = iso;
		int moveNumber = moveBeforeNum();
		if (moveNumber > 0) {
			snpRefAltInfo.setMoveBeforeNum(moveNumber);
		}
	}
	/** 默认返回三字母，可以设定为返回单字母 */
	public void setNeedAA3(boolean isNeedAA3) {
		this.isNeedAA3 = isNeedAA3;
	}
	public int getStartCis() {
		return iso.isCis5to3() ? snpRefAltInfo.getStartReal() : snpRefAltInfo.getEndReal();
	}
	public int getEndCis() {
		return iso.isCis5to3() ? snpRefAltInfo.getEndReal() : snpRefAltInfo.getStartReal();
	}
	public int getStartAbs() {
		return snpRefAltInfo.getStartReal();
	}
	public int getEndAbs() {
		return snpRefAltInfo.getEndReal();
	}
	
	/** 是否需要氨基酸变化注释，有些在内含子中的就不需要氨基酸变化注释 */
	public boolean isNeedHgvsp() {
		if (!iso.ismRNAFromCds()) {
			return false;
		}
		return isNeedHgvspDetail();
	}
	/** 是否需要氨基酸变化注释，有些在内含子中的就不需要氨基酸变化注释 */
	protected abstract boolean isNeedHgvspDetail();
	
	private void initial(SeqHashInt seqHash) {
		setStartEndCis();
		setSiteReplace();
		fillRefAltNrForAA(seqHash);
	}
	
	public String getHgvsp() {
		return getSnpChange();
	}
	public abstract String getSnpChange();
	
	/**
	 * 检测是否需要回移一次
	 * 如果是duplicate的类型，将数据往回移动一次，主要用于剪接位点gt-ag这块<br>
	 * 譬如插入 T--ACATG[ACATG]T----------AG<br>
	 * 其中插入在G和T之间，则切换为<br>
	 * T--[ACATG]-ACATGT----------AG<br>
	 * @param isDupMoveLast 默认不移动
	 */	
	protected abstract int moveBeforeNum();

	/** 把refNr和altNr都准备好 */
	protected void fillRefAltNrForAA(SeqHashInt seqHash) {
		GffGeneIsoInfo isoSub = iso.getSubGffGeneIso(startCds, endCds);
		if (isoSub.isEmpty()) {
			throw new ExceptionNBCSnpHgvs("snp error not in cds " + snpRefAltInfo.toString());
		}
		refSeqNrForAA = seqHash.getSeq(isoSub, false);		
		altSeqNrForAA = replaceSnpIndel(getSeqAltNrForAA(), snpOnReplaceLocStart, snpOnReplaceLocEnd);
	}
	
	/**
	 * 根据需求将AA1转换成AA3
	 * @param AA1
	 * @return
	 */
	protected String convertAA(String AA1) {
		if (isNeedAA3) {
			return CodeInfo.convertToAA3(AA1);
		}
		return AA1;
	}
	/**
	 * 根据需求将AA1转换成AA3
	 * @param AA1
	 * @return
	 */
	protected String convertAA(char AA1) {
		String AA1str = AA1 + "";
		if (isNeedAA3) {
			return CodeInfo.convertToAA3(AA1str);
		}
		return AA1str;
	}
	
	protected abstract void setStartEndCis();
	
	/**
	 * 设置 {@link #snpOnReplaceLocStart}
	 * 和 {@link #snpOnReplaceLocEnd}
	 */
	protected abstract void setSiteReplace();

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
		SeqFasta seqFasta = refSeqNrForAA.clone();
		if (seqFasta.toString().equals("")) {
			return new SeqFasta();
		}
		seqFasta.modifySeq(startLoc, endLoc, replace);
		//修改移码
		return seqFasta;
	}
	
	/** 返回输入的位点在第几个氨基酸上，如果不在cds中则返回 -1 */
	protected int getAffectAANum(int coord) {
//		if (iso == null || iso.getCodLocUTRCDS(coord) != GffGeneIsoInfo.COD_LOCUTR_CDS) {
//			return -1;
//		}
		int num = iso.getCod2ATGmRNA(coord);
		return num/3 + 1;
	}
	
	/**
	 * 返回alt的序列，用于生成protein的替换ref的序列转AA使用
	 * @return
	 */
	protected String getSeqAltNrForAA() {
		String seq = snpRefAltInfo.getSeqAlt();
		if (!iso.isCis5to3()) {
			seq = SeqFasta.reverseComplement(seq);
		}
		return seq;
	}
	
	public static SnpIsoHgvsp generateSnpRefAltHgvsp(SnpInfo snpRefAltInfo, GffGeneIsoInfo iso, SeqHashInt seqHash) {
		SnpIsoHgvsp snpIsoHgvsp = generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		if (snpIsoHgvsp.isNeedHgvsp()) {
			snpIsoHgvsp.initial(seqHash);
		}
		return snpIsoHgvsp;
	}
	
	/** 仅用于测试，正式项目不能使用 */
	@VisibleForTesting
	protected static SnpIsoHgvsp generateSnpRefAltHgvsp(SnpInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		SnpIsoHgvsp snpIsoHgvsp = null;
		int refLen = snpRefAltInfo.getSeqRef().length();
		int altLen = snpRefAltInfo.getSeqAlt().length();
		if (refLen == 1 && altLen == 1) {
			snpIsoHgvsp = new SnpRefAltIsoSnp(snpRefAltInfo, iso);
		} else if (refLen == 0 && altLen >= 1) {
			snpIsoHgvsp = new SnpRefAltIsoIns(snpRefAltInfo, iso);
		} else if (refLen >= 1 && altLen == 0) {
			snpIsoHgvsp = new SnpRefAltIsoDel(snpRefAltInfo, iso);
		} else if (refLen > 1 && altLen > 1) {
			//TODO indel尚未实现
		}
		if (snpIsoHgvsp == null) {
			throw new ExceptionNBCSnpHgvs("cannot find such indel conditon " + snpRefAltInfo.toString());
		}
		return snpIsoHgvsp;
	}
	
	/** 读码框外的插入改变 */
	protected String getInDelChangeFrameShift(boolean isExtend, boolean isDelete) {
		char[] refSeq = refSeqNrForAA.toStringAA1().toCharArray();
		char[] aaSeqChr = altSeqNrForAA.toStringAA1().toCharArray();
		if (refSeq[0] == '*' && aaSeqChr[0] == '*') {
			return convertAA(refSeq[0]) + getAffectAANum(startCds) + "=";
		}
		
		int terNumAlt = getTerNum(aaSeqChr);
		int terNumRef = getTerNum(refSeq);

		//如果为 p.Val1106ValfsTer15
		//则需要向后延长一位为 p.Asn1107ProfsTer14
		//就是不能氨基酸不变化
		int num = 0;
		for (; num < refSeq.length; num++) {
			if (refSeq[num] != aaSeqChr[num]) {
				break;
			}
		}
		int terNumAltReal = terNumAlt - num;
		//如果是 TerfsTer3，则计算位点从Ter开始算1
		//如果是Ter225TyrextTer3，则计算位点从原来Ter的后一位开始算1
		if (isExtend) {
			terNumAltReal = terNumAlt - terNumRef;
		}
		String ter = terNumAlt>0 ? terNumAltReal+"" : "?";
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(convertAA(refSeq[num]));
		sBuilder.append(getAffectAANum(startCds)+num);
		if (!isDelete) {
			sBuilder.append(convertAA(aaSeqChr[num]));
		} else {
			//仅用于ter删除
			if (terNumRef > 1) {
				sBuilder.append("_"+convertAA(refSeq[terNumRef-1]));
				sBuilder.append(getAffectAANum(startCds)+terNumRef-1);
			}
			sBuilder.append("del");
		}
		if (isExtend) {
			sBuilder.append("ext");
		} else {
			sBuilder.append("fs");
		}
		sBuilder.append(convertAA("*"));
		sBuilder.append(ter);
		return sBuilder.toString();
	}
	
	private int getTerNum(char[] aaSeqChr) {
		int terNum = 0;
		boolean isHaveTer = false;
		for (char aaChar : aaSeqChr) {
			terNum++;
			if (aaChar == '*') {
				isHaveTer = true;
				break;
			}
		}
		return isHaveTer? terNum : 0;
	}
	
	@VisibleForTesting
	protected static int getStartSame(char[] refAA, char[] altAA) {
		int startNum = 0;
		int len = Math.min(refAA.length, altAA.length);
		for (int i = 0; i < len; i++) {
			if (refAA[i] != altAA[i]) {
				break;
			}
			startNum++;
		}
		return startNum;
	}
	
	@VisibleForTesting
	protected static int getEndSame(char[] refAA, char[] altAA) {
		int endNum = 0;
		int len = Math.min(refAA.length, altAA.length);
		for (int i = 1; i <= len; i++) {
			if (refAA[refAA.length-i] != altAA[altAA.length-i]) {
				break;
			}
			endNum++;
		}
		return endNum;
	}
}

class SnpRefAltIsoSnp extends SnpIsoHgvsp {
	boolean isATG = false;
	boolean isUAG = false;

	public SnpRefAltIsoSnp(SnpInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		super(snpRefAltInfo, iso);
	}
	@Override
	protected int moveBeforeNum() {
		return 0;
	}
	
	public boolean isNeedHgvspDetail() {
		return iso.isCodInAAregion(getStartCis());
	}
	
	protected void setStartEndCis() {
		int position = getStartCis();
		startCds = iso.getLocAAbefore(position);
		endCds = iso.getLocAAend(position);
		if (iso.getCod2ATG(startCds) == 0) {
			isATG = true;
		}
		if (iso.getCod2UAG(endCds) == 0) {
			isUAG = true;
		}
		if (isUAG) {
			endCds = iso.getEnd();
		}
		if (startCds <0) {
			throw new ExceptionNBCSnpHgvs("snp error not in cds " + snpRefAltInfo.toString());
		}
	}
	
	protected void setSiteReplace() {
		snpOnReplaceLocStart = -iso.getLocAAbeforeBias(getStartCis()) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart;
	}
	
	public String getSnpChange() {
		String ref = convertAA(refSeqNrForAA.toStringAA1().substring(0, 1));
		String alt = convertAA(altSeqNrForAA.toStringAA1().substring(0, 1));
		if (isUAG && !ref.equals(alt)) {
			setVarType.add(EnumVariantClass.stop_lost);
			return "p." + getInDelChangeFrameShift(true, false);
		}
		
		if (ref.equals(alt)) {
			setVarType.add(EnumVariantClass.synonymous_variant);
			if (isUAG) {
				setVarType.add(EnumVariantClass.stop_retained_variant);
			}
			return "p." + ref + getAffectAANum(snpRefAltInfo.getStartReal()) + "="; 
		}
		if (isATG) {
			setVarType.add(EnumVariantClass.start_lost);
			setVarType.add(EnumVariantClass.initiator_codon_variant);
			return "p." + ref + "1?";
		}
		setVarType.add(EnumVariantClass.missense_variant);
		return "p." + ref + getAffectAANum(snpRefAltInfo.getStartReal()) + alt;
	}

}

class SnpRefAltIsoIns extends SnpIsoHgvsp {
	/** 插入位置是否在两个aa中间 */
	boolean isInsertInFrame = false;
	/** 插入位置在atg，并且插入不引起移码 */
	boolean isInsertInStartAndInFrame = false;
	
	public SnpRefAltIsoIns(SnpInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		super(snpRefAltInfo, iso);
	}

	@Override
	protected int moveBeforeNum() {
		int moveMax = snpRefAltInfo.moveBeforeNum();
		if (moveMax == 0) {
			return 0;
		}
		int site = snpRefAltInfo.getStartReal();
		int coordExonNum = iso.getNumCodInEle(site);
		if (coordExonNum == 0) {
			return 0;
		}
		int moveNum = 0;
		if (coordExonNum <0) {
			int coord2BoundStart = iso.getCod2ExInStart(site)+1;
			int coord2BoundEnd = iso.getCod2ExInEnd(site)+1;
			if (iso.isCis5to3() && (coord2BoundStart == 1 || coord2BoundEnd == 2)
					|| !iso.isCis5to3() && (coord2BoundStart == 2 || coord2BoundEnd == 1)
					) {
				moveNum = 1;
				snpRefAltInfo.setMoveBeforeNum(1);
				site = snpRefAltInfo.getStartReal();
				coordExonNum = iso.getNumCodInEle(site);
			}
		}
		if (!iso.isCodInAAregion(site)) {
			return moveNum;
		}
		if (coordExonNum > 0) {
			int beforeSite = iso.getLocAAbefore(site);
			int afterSite = iso.getLocAAend(site);
			if (iso.isCis5to3() && site != afterSite && site - beforeSite + 1 <= moveMax - moveNum) {
				moveNum = moveNum + site - beforeSite + 1;
			} else if (!iso.isCis5to3() && site != beforeSite && site-afterSite+1 <= moveMax-moveNum) {
				moveNum = moveNum + site - afterSite + 1;
			}
		}
		return moveNum;
	}
	
	public boolean isNeedHgvspDetail() {
		boolean isNeedAAanno = true;
		int start = getStartCis();
		int end = getEndCis();
		if (start == iso.getUAGsite() || end == iso.getATGsite()) {
			isNeedAAanno = false;
		}
		if (! iso.isCodInAAregion(start) && !iso.isCodInAAregion(end)) {
			isNeedAAanno = false;
		}
		
		if (isNeedAAanno && isFrameShift()) {
			setVarType.add(EnumVariantClass.frameshift_variant);
		}
		if (isInsertInATG()) {
			setVarType.add(EnumVariantClass.start_lost);
		}
		return isNeedAAanno;
	}
	
	/**
	 * 插入位点在ATG前部，则需要排除类似
	 * ATG-CGT
	 * AT-[ACTGCATG-AT]-G-CGT
	 * 这种情况
	 * @return
	 */
	private boolean isInsertInATG() {
		boolean isInsertBeforeATG = false;
		int start = getStartCis();
		int end = getEndCis();
		int startCds = iso.getLocAAbefore(start);
		int endCds = iso.getLocAAend(end);
		boolean isInsertInATG = startCds == iso.getATGsite() && Math.abs(startCds-endCds) == 2;
		
		if (isInsertInATG) {
			int len = 3 - (iso.getLocDistmRNA(end, endCds)+1);
			char[] atg = {'a','t','g'};
			char[] insertion = getSeqAltNrForAA().toLowerCase().toCharArray();
			boolean isSame = true;
			for (int i = 0; i < len; i++) {
				if (atg[i] != insertion[insertion.length-len+i]) {
					isSame = false;
					break;
				}
			}
			if (isSame) {
				isInsertBeforeATG = true;
			}
		}
		return !isInsertBeforeATG&&isInsertInATG;
	}
	
	protected void setStartEndCis() {
		startCds = getStartCis();
		endCds = getEndCis();
		isInsertInFrame = isInsertInFrame();
		
		int startNum = iso.getNumCodInEle(startCds);
		if (startNum < 0) {
			startCds = iso.getLsElement().get(Math.abs(startNum)-1).getEndCis();
		}
		int endNum = iso.getNumCodInEle(endCds);
		if (endNum < 0) {
			endCds = iso.getLsElement().get(Math.abs(endNum)).getStartCis();
		}
		
		//如果没有发生移码，类似
		// AT[G] -ACT- AGC，其中start为G
		//则我们获取 [A]TG-ACT-AG[C] 
		//最后突变类型变为 ATG_AGCinsACT
		if (!isFrameShift()) {
			startCds = iso.getLocAAbefore(startCds);
			endCds = iso.getLocAAend(endCds);
			if (isInsertInFrame) {
				return;
			}
			//在一个aa内部
			if (Math.abs(endCds-startCds) == 2 && endCds != iso.getUAGsite()) {
				endCds = iso.getLocAANextEnd(endCds);
				if (startCds == iso.getATGsite()) {
					isInsertInStartAndInFrame = true;
				}
			}
			startCds = iso.getLocAALastStart(startCds);
			return;
		}
		
		//如果在两个密码子中插入造成移码突变，类似
		// ATG-A-ACT AGC
		//则我们获取 ATG-A-[ACT AGC ....] 一直到结束
		//最后突变类型变为 [ACT] > [-A-AC] Ter * 其中*是数字
		if (isInsertInFrame) {
			startCds = endCds;
		} else {
			startCds = iso.getLocAAbefore(startCds);
		}
		endCds = iso.getEnd();
		return;
	}
	
	protected void setSiteReplace() {
		int startCds = getStartCis();		
		int startNum = iso.getNumCodInEle(getStartCis());
		if (startNum < 0) {
			startCds = iso.getLsElement().get(Math.abs(startNum)).getEndCis();
		}
	
		if (isInsertInFrame && isFrameShift()) {
			// 如果在两个密码子中插入造成移码突变，类似
			// ATG-A-ACT AGC
			// 则我们获取 ATG-A-[ACT AGC ....] 一直到结束
			// 最后突变类型变为 [ACT] > [-A-AC] Ter * 其中*是数字
			// 因此就只需要简单的将插入序列添加到ref序列的头部即可
			snpOnReplaceLocStart = 0;
			snpOnReplaceLocEnd = 0;
		} else if(!isFrameShift() && !isInsertInFrame) {
			snpOnReplaceLocStart = -iso.getLocAAbeforeBias(startCds) + 1;
//			if (!isInsertInStartAndInFrame) {
				snpOnReplaceLocStart = snpOnReplaceLocStart + 3;
//			}
			snpOnReplaceLocEnd = snpOnReplaceLocStart-1;
		} else {
			snpOnReplaceLocStart = -iso.getLocAAbeforeBias(startCds) + 1;
			snpOnReplaceLocEnd = snpOnReplaceLocStart-1;
		}
	}
	
	public String getSnpChange() {
		String info = isFrameShift() ? getInDelChangeFrameShift(false, false) : getInsertionChangeInFrame();
		return "p." + info;
	}
	
	/** 读码框内的插入改变 */
	private String getInsertionChangeInFrame() {
		char[] refAA = refSeqNrForAA.toStringAA1().toCharArray();
		char[] altAA = altSeqNrForAA.toStringAA1().toCharArray();
		
		int stopNumRef = getStopNum(refAA);
		int stopNumAlt = getStopNum(altAA);
		
		if (stopNumRef > 0 && stopNumAlt == 0) {
			setVarType.add(EnumVariantClass.stop_lost);
		} else if (stopNumRef == 0 && stopNumAlt > 0) {
			setVarType.add(EnumVariantClass.stop_gained);
		} else if (stopNumRef == 2 && stopNumAlt == 2) {
			setVarType.add(EnumVariantClass.stop_retained_variant);
		} else if (stopNumRef == 2 && stopNumAlt > 0 && stopNumAlt < altAA.length - 1) {
			setVarType.add(EnumVariantClass.stop_gained);
		}
		
		return isInsertInFrame ? getInsertionBetweenAA(refAA, altAA) : getInsertionInAA(refAA, altAA);
	}
	
	private int getStopNum(char[] chrAA) {
		int stopNum = 0;
		boolean isHaveStop = false;
		for (char refChr : chrAA) {
			stopNum++;
			if (refChr == '*') {
				isHaveStop = true;
				break;
			}
		}
		if (isHaveStop == false) {
			stopNum = 0;
		}
		return stopNum;
	}
	
	/** insertion在两个aa中间 */
	private String getInsertionBetweenAA(char[] refAA, char[] altAA) {
		if (altAA[0] != refAA[0] || altAA[altAA.length-1] != refAA[1]) {
			throw new ExceptionNBCSnpHgvs("error");
		}
		setVarType.add(EnumVariantClass.conservative_inframe_insertion);
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(convertAA(refAA[0]) + getAffectAANum(startCds) 
		+ "_" + convertAA(refAA[1]) + getAffectAANum(endCds) + "ins");	
		for (int i = 1; i < altAA.length-1; i++) {
			sBuilder.append(convertAA(altAA[i]));	
		}
		return sBuilder.toString();
	}
	/** insertion在某个aa内部
	 * 但是可能出现 ref: CysArg，alt: CysMetArg
	 * 的情况，这种其实还是InsertionBetweenAA
	 * 
	 * 前面{@link SnpIndelRealignHandle}保证了不会存存在
	 *  ATG-CGT
	 *  A-[TGC]-TG-CGT
	 *  一定会被修正为
	 * ATG-[CTG]-CGT
	 * 
	 * @param refAA
	 * @param altAA
	 * @return
	 */
	private String getInsertionInAA(char[] refAA, char[] altAA) {
		StringBuilder sBuilder = new StringBuilder();
		int startNum = getAffectAANum(startCds);
		System.out.println();
		if(refAA[1] == '*' && altAA[1] == '*') {
			sBuilder.append(convertAA(refAA[1]) + (startNum+1) + "=");
		} else if (refAA[1] == altAA[1]) {
			sBuilder.append(convertAA(refAA[1]) + (startNum +1)
			+ "_" + convertAA(refAA[2]) + (startNum+2) + "ins");
			for (int i = 2; i < altAA.length-1; i++) {
				sBuilder.append(convertAA(altAA[i]));	
			}
			setVarType.add(EnumVariantClass.conservative_inframe_insertion);
		} else if (refAA[1] == altAA[altAA.length-2]) {
			sBuilder.append(convertAA(refAA[0]) + startNum 
			+ "_" + convertAA(refAA[1]) + (startNum+1) + "ins");	
			for (int i = 1; i < altAA.length-2; i++) {
				sBuilder.append(convertAA(altAA[i]));	
			}
			//TODO 这里需要考虑是不是同义突变
			//譬如 ATC-CGC-CAG
			//变为 ATC-CG[TAG]-CCAG
			setVarType.add(EnumVariantClass.conservative_inframe_insertion);
		} else {
			sBuilder.append(convertAA(refAA[1]) + (startNum +1) + "delins");	
			for (int i = 1; i < altAA.length-1; i++) {
				sBuilder.append(convertAA(altAA[i]));	
			}
			setVarType.add(EnumVariantClass.disruptive_inframe_insertion);
		}
		return sBuilder.toString();
	}
	
	/** 插入位置是否在两个aa中间 */
	private boolean isInsertInFrame() {
		return iso.getCod2ATGmRNA(endCds)%3 == 0;
	}
	
	/** 插入是否引起了移码 */
	private boolean isFrameShift() {
		return snpRefAltInfo.getSeqAlt().length()%3 != 0;
	}
	
}

class SnpRefAltIsoDel extends SnpIsoHgvsp {
	boolean isFrameShift = false;
	boolean isAffectUAG = false;
	boolean isNeedAAanno = false;
	
	/** 有这个标记的直接返回Met1?或者Met1fs */
	boolean isMetDel = false;
	
	public SnpRefAltIsoDel(SnpInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		super(snpRefAltInfo, iso);
		
		int[] region = getValidRange(new int[]{getStartCis(), getEndCis()}, new int[]{iso.getATGsite(), iso.getUAGsite()});
		if(region == null) return;
		GffGeneIsoInfo isoSub = iso.getSubGffGeneIso(region[0], region[1]);
		isNeedAAanno = !isoSub.isEmpty();
		isFrameShift = isoSub.getLenExon() % 3 != 0;
		if (isNeedAAanno && isFrameShift) {
			setVarType.add(EnumVariantClass.frameshift_variant);
		}
	}

	@Override
	protected int moveBeforeNum() {
		int moveMax = snpRefAltInfo.moveBeforeNum();
		if (moveMax == 0) {
			return 0;
		}
		
		int siteStart = getStartCis();
		int siteEnd = getEndCis();
		GffGeneIsoInfo isoSub = iso.getSubGffGeneIso(siteStart, siteEnd);
		if (isoSub.size() > 1) {
			return 0;
		}
		if (isoSub.size() == 0) {
			int start2Start = iso.getCod2ExInStart(siteStart)+1;
			int end2End = iso.getCod2ExInEnd(siteEnd)+1;
			if (start2Start == 1 && end2End == 1 || start2Start > 2 && end2End > 2) {
				return 0;
			}
			//---10==20-----start-------end--30===40--------------------
			if (iso.isCis5to3()) {
				int end2Start = iso.getCod2ExInStart(siteEnd) + 1;
				if (start2Start <= 2 && end2Start <= moveMax) {
					return end2Start;
				} else if (end2End <= 2 && start2Start > 3-end2End && 3-end2End <= moveMax) {
					return 3-end2End;
				}
				return 0;
			} else {
				//---10==20----end-------start--30===40--------------------
				int start2End = iso.getCod2ExInEnd(siteStart) + 1;
				if (end2End <= 2 && start2End <= moveMax) {
					return start2End;
				} else if (start2Start <= 2 && end2End > 3-start2Start && 3-start2Start <= moveMax) {
					return 3-start2Start;
				}
				return 0;
			}
		}
		
		int startExNum = iso.getNumCodInEle(siteStart);
		int endExNum = iso.getNumCodInEle(siteEnd);
		if (startExNum < 0 && endExNum < 0 || startExNum == 0 || endExNum == 0) {
			return 0;
		}
		if (startExNum < 0) {
			if (iso.isCis5to3()) {
				//--------start-------10====end====20----------------------
				int start2Start = iso.getCod2ExInStart(siteStart)+1;
				int end2Start = iso.getCod2ExInStart(siteEnd)+1;
				if (end2Start <= moveMax && start2Start >= end2Start) {
					return end2Start;
				}
			} else {
				//--10====end====20------------start----------
				//TODO 这种情况可能还要再分析下外显子中是否移码
				int start2End = iso.getCod2ExInStart(siteStart)+1;
				if (start2End <= moveMax) {
					return start2End;
				}
			}
		} else if (endExNum < 0) {
			if (iso.isCis5to3()) {
				//----10====start====20-------end---------------
				int end2Start = iso.getCod2ExInStart(siteEnd)+1;
				if (end2Start <= moveMax) {
					return end2Start;
				}
			} else {
				//--------end-------10====start====20----------------------
				//TODO 这种情况可能还要再分析下外显子中是否移码
				int end2End = iso.getCod2ExInEnd(siteEnd)+1;
				int start2End = iso.getCod2ExInEnd(siteStart)+1;
				if (start2End <= moveMax && end2End >= start2End) {
					return start2End;
				}
			}
		} else {
			if (Math.abs(siteStart - siteEnd) + 1 %3 != 0) {
				return 0;
			}
			if (iso.isCis5to3()) {
				//----------10==start====end==20----------------------
				int siteBefore = iso.getLocAAbefore(siteStart);
				if (siteBefore - siteStart < 3 && siteBefore - siteStart < moveMax) {
					return siteBefore - siteStart;
				}
			} else {
				//----------10==end====start==20----------------------
				int siteAfter = iso.getLocAAend(siteEnd);
				if (siteAfter - siteEnd < 3 && siteAfter - siteEnd < moveMax) {
					return siteAfter - siteEnd ;
				}
			}
		}

		return 0;
	}
	
	public boolean isNeedHgvspDetail() {
		return isNeedAAanno;
	}
	
	private int[] getValidRange(int[] startEnd, int[] atgUag) {
		int[] startend = new int[] {Math.min(startEnd[0], startEnd[1]), Math.max(startEnd[0], startEnd[1])};
		int[] atguag = new int[] {Math.min(atgUag[0], atgUag[1]), Math.max(atgUag[0], atgUag[1])};
		int[] result = new int[] {Math.max(startend[0], atguag[0]), Math.min(startend[1], atguag[1])};
		if(result[1] < result[0]) {
			return null;
		}
		return result;
	}
	
	protected void setStartEndCis() {
		int atgStart = iso.getATGsite();
		int atgEnd = iso.getLocAAend(iso.getATGsite());
		int uagEnd = iso.getUAGsite();
		int uagStart = iso.getLocAAend(iso.getUAGsite());

		if (getStartAbs() <= Math.max(atgStart, atgEnd) && getEndAbs()>= Math.min(atgStart, atgEnd)) {
			setVarType.add(EnumVariantClass.start_lost);
			isMetDel = true;
		}
		//影响到了UAG，那么就当成移码处理，会一直延长到iso的结尾并获取新的UAG位点
		if (getStartAbs() <= Math.max(uagStart, uagEnd) && getEndAbs()>= Math.min(uagStart, uagEnd)) {
			isAffectUAG = true;
			setVarType.add(EnumVariantClass.stop_lost);
		}
		startCds = getStartCis();
		endCds = getEndCis();
		
		
		int startNum = iso.getNumCodInEle(startCds);
		int endNum = iso.getNumCodInEle(endCds);
		if (startNum < 0) {
			startCds = iso.getLsElement().get(Math.abs(startNum)).getStartCis();
		} else if (startNum == 0) {
			startCds = iso.getStart();
		}
		int startCdsTmp = iso.getLocAAbefore(startCds);
		if (startCdsTmp > 0) {
			startCds = startCdsTmp;
		}
		
		if (endNum < 0) {
			endCds = iso.getLsElement().get(Math.abs(endNum)-1).getEndCis();
		} else if (endNum == 0) {
			endCds = iso.getEnd();
		}
		
		endCds = iso.getLocAAend(endCds);
		if (isFrameShift || isAffectUAG) {
			endCds = iso.getEnd();
		} 
		return;
	}
	
	protected void setSiteReplace() {
		int startCds = getStartCis();
		int endCds = getEndCis();
		
		int startNum = iso.getNumCodInEle(startCds);
		int endNum = iso.getNumCodInEle(endCds);
		if (startNum < 0) {
			startCds = iso.getLsElement().get(Math.abs(startNum)).getStartCis();
		}
		if (endNum < 0) {
			endCds = iso.getLsElement().get(Math.abs(endNum)-1).getEndCis();
		}
		snpOnReplaceLocStart = -iso.getLocAAbeforeBias(startCds) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart + iso.getLocDistmRNA(startCds, endCds);
	}
	
	public String getSnpChange() {
		if (isMetDel) {
			return "p." + convertAA("M") +"1?";
		}
		
		String info =  isFrameShift? getInDelChangeFrameShift(false, false) : getDelChangeInFrame();
		return "p." + info;
	}
	
	/** 读码框内的插入改变 */
	private String getDelChangeInFrame() {
		char[] refAA = refSeqNrForAA.toStringAA1().toCharArray();
		char[] altAA = altSeqNrForAA.toStringAA1().toCharArray();
		int start = getAffectAANum(startCds);
		int end = getAffectAANum(endCds);
		
		if (refAA[0] == '*' && altAA[0] == '*') {
			//TODO 未测试
			return convertAA(refAA[0]) + start + "=";
		} else if (!isFrameShift && isAffectUAG) {
			//说明终止密码子被删掉了
			setVarType.add(EnumVariantClass.stop_lost);
			return getInDelChangeFrameShift(true, true);
		}
		setIsStopGain(refAA, altAA);
		
		int refStart = getStartSame(refAA, altAA);
		int refEnd = getEndSame(refAA, altAA);
		
		StringBuilder sBuilder = new StringBuilder();
		if (start+refStart == end-refEnd) {
			sBuilder.append(convertAA(refAA[refStart]) + (start+refStart) + "del");
		} else {
			sBuilder.append(convertAA(refAA[refStart]) + (start+refStart) + "_" + convertAA(refAA[refAA.length-refEnd-1]) + (end-refEnd) + "del");
		}
		if (altAA.length-refStart-refEnd > 0) {
			sBuilder.append("ins");
			setVarType.add(EnumVariantClass.disruptive_inframe_deletion);
		} else {
			setVarType.add(EnumVariantClass.conservative_inframe_deletion);
		}
		for (int i = refStart; i < altAA.length-refEnd; i++) {
			sBuilder.append(convertAA(altAA[i]));	
		}
		return sBuilder.toString();
	}
	
	private void setIsStopGain(char[] refAA, char[] altAA) {
		int refStart = getStartSame(refAA, altAA);
		int refEnd = getEndSame(refAA, altAA);
		for (int i = refStart; i < altAA.length-refEnd; i++) {
			if (altAA[i] == '*') {
				setVarType.add(EnumVariantClass.stop_gained);
			}
		}
	}

}
