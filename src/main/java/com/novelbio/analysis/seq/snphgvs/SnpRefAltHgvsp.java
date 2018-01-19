package com.novelbio.analysis.seq.snphgvs;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.dataStructure.ArrayOperate;

import smile.math.Math;

public abstract class SnpRefAltHgvsp {
	SnpRefAltInfo snpRefAltInfo;
	GffGeneIsoInfo iso;
	
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
	
	public String getHgvsp(SeqHash seqHash) {
		setStartEndCis();
		setSiteReplace();
		fillRefAltNrForAA(seqHash);
		return getSnpChange();
	}
	public abstract String getSnpChange();
	
	/** 把refNr和altNr都准备好 */
	protected void fillRefAltNrForAA(SeqHash seqHash) {
		ArrayList<ExonInfo> lsTmp = iso.getRangeIsoOnExon(startCds, endCds);
		if (ArrayOperate.isEmpty(lsTmp)) {
			throw new ExceptionNBCSnpHgvs("snp error not in cds " + snpRefAltInfo.toString());
		}
		refSeqNrForAA = seqHash.getSeq(StrandType.isoForward, snpRefAltInfo.getRefId(), lsTmp, false);		
		altSeqNrForAA = replaceSnpIndel(getSeqAltNrForAA(), snpOnReplaceLocStart, snpOnReplaceLocEnd);
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
		if (iso.isCis5to3()) {
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
		throw new ExceptionNBCSnpHgvs("cannot find such indel conditon");
	}
}

class SnpRefAltIsoSnp extends SnpRefAltHgvsp {
	
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
		if (startCds <0) {
			throw new ExceptionNBCSnpHgvs("snp error not in cds " + snpRefAltInfo.toString());
		}
	}
	
	protected void setSiteReplace() {
		snpOnReplaceLocStart = -iso.getLocAAbeforeBias(getStartCis()) + 1;
		snpOnReplaceLocEnd = snpOnReplaceLocStart;
	}
	
	public String getSnpChange() {
		return "p." + refSeqNrForAA.toStringAA1() + getAffectAANum(snpRefAltInfo.getStartReal()) + altSeqNrForAA.toStringAA1();
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
		
		if (iso.getCod2ATGmRNA(startCds)%3 == 0) {
			isInsertInFrame = true;
		}
		
		//如果在两个密码子中插入碱基，类似
		// AT[G] -ACT- AGC，其中start为G
		//则我们获取 [A]TG-ACT-AG[C] 
		//最后突变类型变为 ATG_AGCinsACT
		if (isInsertInFrame && !isFrameShift()) {
			startCds = iso.getLocAAbefore(startCds);
			endCds = iso.getLocAAendBias(endCds);
			return;
		}
		
		//如果在两个密码子中插入造成移码突变，类似
		// ATG-A-ACT AGC
		//则我们获取 ATG-A-[ACT AGC ....] 一直到结束
		//最后突变类型变为 [ACT] > [-A-AC] Ter * 其中*是数字
		if (isInsertInFrame) {
			startCds = endCds;
			endCds = iso.getEnd();
			return;
		}
		
		//如果在读码框内插入造成移码突变，类似
		// A[T]-A-G ACT AGC，其中start为T
		//则我们获取 [A]T-A-G ACT AGC .... 一直到结束
		//最后突变类型变为 [ACG] > [AT-A-] Ter * 其中*是数字
		if (!isInsertInFrame()) {
			startCds = iso.getLocAAbefore(startCds);
			endCds = iso.getEnd();
		}
		endCds = iso.getLocAAend(startCds);
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
			snpOnReplaceLocEnd = snpOnReplaceLocStart;
		}
	}
	
	public String getSnpChange() {
		boolean isFrameShift = snpRefAltInfo.getSeqAlt().length()%3 != 0;
		String info = isFrameShift ? getInsertionChangeInFrame() : getInsertionChangeFrameShift();
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
		sBuilder.append(refAA[0] + getAffectAANum(startCds) + "_" + refAA[1] + getAffectAANum(endCds) + "ins");	
		for (int i = 1; i < altAA.length-1; i++) {
			sBuilder.append(altAA[i]);	
		}
		return sBuilder.toString();
	}

	/** 读码框外的插入改变 */
	private String getInsertionChangeFrameShift() {
		String refSeq = refSeqNrForAA.toStringAA1().substring(0,1);
		String aaSeq = altSeqNrForAA.toStringAA1();
		int terNum = 0;
		boolean isHaveTer = false;
		char[] aaSeqChr = aaSeq.toCharArray();
		for (char aaChar : aaSeq.toCharArray()) {
			if (aaChar == '*') {
				isHaveTer = true;
				break;
			}
			terNum++;
		}
		String ter = isHaveTer? terNum+"" : "?";
		return refSeq + getAffectAANum(startCds) + aaSeqChr[0] + "fsTer" + ter;
	}

	
	/** 插入位置是否在两个aa中间 */
	private boolean isInsertInFrame() {
		return iso.getCod2ATGmRNA(startCds)%3 == 0;
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
		int atgBefore = Math.min(atgStart, atgEnd);
		int uagBefore = Math.min(uagStart, uagEnd);

		if (getStartAbs() <= atgBefore && getEndAbs()>= atgBefore) {
			isMetDel = true;
			return;
		}
		if (getStartAbs() <= uagBefore && getEndAbs()>= uagBefore) {
			isDelInFrame = false;
		}
		startCds = getStartCis();
		endCds = getEndCis();
		
		
		int startNum = iso.getNumCodInEle(startCds);
		int endNum = iso.getNumCodInEle(endCds);
		if (startNum < 0) {
			startCds = iso.getLsElement().get(Math.abs(startNum)).getStartCis();
		}
		startCds = iso.getLocAAbefore(startCds);
		if (endNum < 0) {
			endCds = iso.getLsElement().get(Math.abs(endNum)-1).getEndCis();
		}
		endCds = iso.getLocAAendBias(endCds);
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
		boolean isFrameShift = snpRefAltInfo.getSeqAlt().length()%3 != 0;
		String info = isFrameShift ? getDelChangeInFrame() : getDelChangeFrameShift();
		return "p." + info;
	}
	
	/** 读码框内的插入改变 */
	private String getDelChangeInFrame() {
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
		sBuilder.append(refAA[0] + getAffectAANum(startCds) + "_" + refAA[1] + getAffectAANum(endCds) + "del");
		if (altAA.length > 0) {
			sBuilder.append("ins");
		}
		for (int i = 1; i < altAA.length-1; i++) {
			sBuilder.append(altAA[i]);	
		}
		return sBuilder.toString();
	}

	/** 读码框外的插入改变 */
	private String getDelChangeFrameShift() {
		String refSeq = refSeqNrForAA.toStringAA1().substring(0,1);
		String aaSeq = altSeqNrForAA.toStringAA1();
		int terNum = 0;
		boolean isHaveTer = false;
		char[] aaSeqChr = aaSeq.toCharArray();
		for (char aaChar : aaSeq.toCharArray()) {
			if (aaChar == '*') {
				isHaveTer = true;
				break;
			}
			terNum++;
		}
		String ter = isHaveTer? terNum+"" : "?";
		return refSeq + getAffectAANum(startCds) + "fsTer" + ter;
	}
	
}
