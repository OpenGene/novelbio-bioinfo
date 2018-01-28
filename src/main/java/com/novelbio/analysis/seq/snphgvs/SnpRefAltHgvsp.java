package com.novelbio.analysis.seq.snphgvs;

import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.CodeInfo;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.dataStructure.ArrayOperate;

public abstract class SnpRefAltHgvsp {
	SnpRefAltInfo snpRefAltInfo;
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
	
	public SnpRefAltHgvsp(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		this.snpRefAltInfo = snpRefAltInfo;
		this.iso = iso;
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
	protected abstract boolean isNeedHgvsp();
	
	public String getHgvsp() {
		if (snpRefAltInfo.isDup() && isNeedMoveDuplicateBefore()) {
			snpRefAltInfo.setIsDupMoveLast(true);
		}
		setStartEndCis();
		setSiteReplace();
		fillRefAltNrForAA();
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
	@VisibleForTesting
	protected boolean isNeedMoveDuplicateBefore() {
		int startNum = iso.getNumCodInEle(getStartCis());
		int endNum = iso.getNumCodInEle(getEndCis());
		if (startNum == 0 || endNum == 0) {
			return false;
		}
		//cover splice site
		if (startNum < 0 && -startNum+1==endNum
				|| startNum > 0 && -startNum == endNum
			) {
			return true;
		}
		//不考虑不在一个exon/intron中的情况
		if (startNum != endNum || startNum > 0 && endNum > 0) {
			return false;
		}
		//仅考虑内含子中的duplicate
		int num = snpRefAltInfo.getVarType() == EnumHgvsVarType.Deletions ? 1 : 0;
		if (iso.getCod2ExInStart(getStartCis()) <= num
				|| iso.getCod2ExInEnd(getEndCis()) <= num
				) {
			return true;
		}
		return false;
	}
	
	/** 把refNr和altNr都准备好 */
	protected void fillRefAltNrForAA() {
		ArrayList<ExonInfo> lsTmp = iso.getRangeIsoOnExon(startCds, endCds);
		if (ArrayOperate.isEmpty(lsTmp)) {
			throw new ExceptionNBCSnpHgvs("snp error not in cds " + snpRefAltInfo.toString());
		}
		refSeqNrForAA = snpRefAltInfo.getSeqHash().getSeq(StrandType.isoForward, snpRefAltInfo.getRefId(), lsTmp, false);		
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
		if (iso == null || iso.getCodLocUTRCDS(coord) != GffGeneIsoInfo.COD_LOCUTR_CDS) {
			return -1;
		}
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
	
	public static SnpRefAltHgvsp generateSnpRefAltHgvsp(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		int refLen = snpRefAltInfo.getSeqRef().length();
		int altLen = snpRefAltInfo.getSeqAlt().length();
		if (refLen == 1 && altLen == 1) {
			return new SnpRefAltIsoSnp(snpRefAltInfo, iso);
		} else if (refLen == 0 && altLen >= 1) {
			return new SnpRefAltIsoIns(snpRefAltInfo, iso);
		} else if (refLen >= 1 && altLen == 0) {
			return new SnpRefAltIsoDel(snpRefAltInfo, iso);
		} else if (refLen > 1 && altLen > 1) {
			//TODO indel尚未实现
		}
		throw new ExceptionNBCSnpHgvs("cannot find such indel conditon " + snpRefAltInfo.toString());
	}
	
	/** 读码框外的插入改变 */
	protected String getInDelChangeFrameShift() {
		char[] refSeq = refSeqNrForAA.toStringAA1().toCharArray();
		String aaSeq = altSeqNrForAA.toStringAA1();
		int terNum = 0;
		boolean isHaveTer = false;
		char[] aaSeqChr = aaSeq.toCharArray();
		for (char aaChar : aaSeq.toCharArray()) {
			terNum++;
			if (aaChar == '*') {
				isHaveTer = true;
				break;
			}
		}
		if (refSeq[0] == '*' && aaSeqChr[0] == '*') {
			return convertAA(refSeq[0]) + getAffectAANum(startCds) + "=";
		}
		//如果为 p.Val1106ValfsTer15
		//则需要向后延长一位为 p.Asn1107ProfsTer14
		//就是不能氨基酸不变化
		int num = 0;
		for (; num < refSeq.length; num++) {
			if (refSeq[num] != aaSeqChr[num]) {
				break;
			}
		}
		terNum = terNum - num;
		String ter = isHaveTer? terNum+"" : "?";
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(convertAA(refSeq[num]));
		sBuilder.append(getAffectAANum(startCds)+num);
		sBuilder.append(convertAA(aaSeqChr[num]));
		sBuilder.append("fs");
		sBuilder.append(convertAA("*"));
		sBuilder.append(ter);
		return sBuilder.toString();
	}
}

class SnpRefAltIsoSnp extends SnpRefAltHgvsp {
	boolean isATG = false;
	public SnpRefAltIsoSnp(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		super(snpRefAltInfo, iso);
	}
	
	protected boolean isNeedHgvsp() {
		return iso.isCodInAAregion(getStartCis());
	}
	
	protected void setStartEndCis() {
		int position = getStartCis();
		startCds = iso.getLocAAbefore(position);
		endCds = iso.getLocAAend(position);
		if (iso.getCod2ATG(startCds) == 0) {
			isATG = true;
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
		String ref = convertAA(refSeqNrForAA.toStringAA1());
		String alt = convertAA(altSeqNrForAA.toStringAA1());
		if (ref.equals(alt)) {
			return "p." + ref + getAffectAANum(snpRefAltInfo.getStartReal()) + "="; 
		}
		if (isATG) {
			return "p." + ref + "1?";
		}
		return "p." + ref + getAffectAANum(snpRefAltInfo.getStartReal()) + alt;
	}

}

class SnpRefAltIsoIns extends SnpRefAltHgvsp {
	boolean isInsertInFrame = false;
	
	public SnpRefAltIsoIns(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		super(snpRefAltInfo, iso);
	}
	
	public boolean isNeedHgvsp() {
		boolean isNeedAAanno = true;
		int start = getStartCis();
		int end = getEndCis();
		if (start == iso.getUAGsite() || end == iso.getATGsite()) {
			isNeedAAanno = false;
		}
		if (! iso.isCodInAAregion(start) && !iso.isCodInAAregion(end)) {
			isNeedAAanno = false;
		}
		return isNeedAAanno;
	}
	
	protected void setStartEndCis() {
		startCds = getStartCis();
		endCds = getEndCis();
		
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
			endCds = iso.getLocAAendBias(endCds);
			return;
		}
		
		isInsertInFrame = isInsertInFrame();
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
		} else {
			snpOnReplaceLocStart = -iso.getLocAAbeforeBias(startCds) + 1;
			snpOnReplaceLocEnd = snpOnReplaceLocStart-1;
		}
	}
	
	public String getSnpChange() {
		String info = isFrameShift() ? getInDelChangeFrameShift() : getInsertionChangeInFrame();
		return "p." + info;
	}
	
	/** 读码框内的插入改变 */
	private String getInsertionChangeInFrame() {
		char[] refAA = refSeqNrForAA.toStringAA1().toCharArray();
		//应该都是2
		if (refAA.length != 2) {
			throw new ExceptionNBCSnpHgvs("error");
		}
		char[] altAA = altSeqNrForAA.toStringAA1().toCharArray();
		if (altAA[0] != refAA[0] || altAA[altAA.length-1] != refAA[1]) {
			throw new ExceptionNBCSnpHgvs("error");
		}
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(convertAA(refAA[0]) + getAffectAANum(startCds) 
		+ "_" + convertAA(refAA[1]) + getAffectAANum(endCds) + "ins");	
		for (int i = 1; i < altAA.length-1; i++) {
			sBuilder.append(convertAA(altAA[i]));	
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


class SnpRefAltIsoDel extends SnpRefAltHgvsp {
	boolean isDelInFrame = false;
	boolean isNeedAAanno = false;
	
	/** 有这个标记的直接返回Met1?或者Met1fs */
	boolean isMetDel = false;
	
	public SnpRefAltIsoDel(SnpRefAltInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		super(snpRefAltInfo, iso);
		
		int[] region = getValidRange(new int[]{getStartCis(), getEndCis()}, new int[]{iso.getATGsite(), iso.getUAGsite()});
		if(region == null) return;
		List<ExonInfo> lsExons = iso.getRangeIsoOnExon(region[0], region[1]);
		isNeedAAanno = !lsExons.isEmpty();
		
		int totalLength = lsExons.stream()
				.map(it -> it.getLength())
				.reduce(0, (result, element) -> result + element);
		isDelInFrame = totalLength % 3 == 0;
	}
	
	public boolean isNeedHgvsp() {
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
			isMetDel = true;
		}
		//影响到了UAG，那么就当成移码处理，会一直延长到iso的结尾并获取新的UAG位点
		if (getStartAbs() <= Math.max(uagStart, uagEnd) && getEndAbs()>= Math.min(uagStart, uagEnd)) {
			isDelInFrame = false;
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
		if (!isDelInFrame) {
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
		
		String info =  isFramShift() ? getInDelChangeFrameShift() : getDelChangeInFrame();
		return "p." + info;
	}
	
	private boolean isFramShift() {
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
		ArrayList<ExonInfo> lsTmp = iso.getRangeIsoOnExon(startCds, endCds);
		int totalLength = lsTmp.stream()
				.map(it -> it.getLength())
				.reduce(0, (result, element) -> result + element);
		return totalLength % 3 != 0;
	}
	
	/** 读码框内的插入改变 */
	private String getDelChangeInFrame() {
		char[] refAA = refSeqNrForAA.toStringAA1().toCharArray();
		char[] altAA = altSeqNrForAA.toStringAA1().toCharArray();
		
		int start = getAffectAANum(startCds);
		int end = getAffectAANum(endCds);

		int refStart = 0;
		int len = Math.min(refAA.length, altAA.length);
		for (int i = 0; i < len; i++) {
			if (refAA[i] == altAA[i]) {
				refStart++;
			}
		}
		
		int refEnd = 0;
		for (int i = 0; i < len; i++) {
			if (refAA[refAA.length-i-1] == altAA[altAA.length-i-1]) {
				refEnd++;
			}
		}
		
		StringBuilder sBuilder = new StringBuilder();
		if (start+refStart == end-refEnd) {
			sBuilder.append(convertAA(refAA[refStart]) + (start+refStart) + "del");
		} else {
			sBuilder.append(convertAA(refAA[refStart]) + (start+refStart) + "_" + convertAA(refAA[refAA.length-refEnd-1]) + (end-refEnd) + "del");
		}
		if (altAA.length > 0) {
			sBuilder.append("ins");
		}
		for (int i = refStart; i < altAA.length-refEnd; i++) {
			sBuilder.append(convertAA(altAA[i]));	
		}
		return sBuilder.toString();
	}

}
