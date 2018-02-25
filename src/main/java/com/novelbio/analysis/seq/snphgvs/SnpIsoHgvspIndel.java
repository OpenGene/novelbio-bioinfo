package com.novelbio.analysis.seq.snphgvs;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;

class SnpRefAltIsoIndel extends SnpIsoHgvsp {
	boolean isFrameShift = false;
	/** indel的起点是否在一个氨基酸的三联密码子头部 */
	boolean isStartAtAAstart = false;
	boolean isAffectUAG = false;
	boolean isNeedAAanno = false;
	
	/** 有这个标记的直接返回Met1?或者Met1fs */
	boolean isMetDel = false;
		
	protected boolean isGetAllLenAA() {
		int siteStart = getStartCis();
		int siteEnd = getEndCis();
		
		int startExNum = iso.getNumCodInEle(siteStart);
		int endExNum = iso.getNumCodInEle(siteEnd);
		
		if (startExNum != endExNum || startExNum <= 0) {
			return false;
		}
		if (siteStart < iso.getATGsite() || isFrameShift || isAffectUAG) {
			return false;
		}
		if ((Math.abs(siteEnd - siteStart) + 1) %3 != 0) {
			return false;
		}
		return true;
	}
	
	public SnpRefAltIsoIndel(SnpInfo snpInfo, GffGeneIsoInfo iso) {
		super(snpInfo, iso);
		
		int[] region = getValidRange(new int[]{getStartCis(), getEndCis()}, new int[]{iso.getATGsite(), iso.getUAGsite()});
		if(region == null) return;
		GffGeneIsoInfo isoSub = iso.getSubGffGeneIso(region[0], region[1]);
		isNeedAAanno = !isoSub.isEmpty();
		
		int changeLen = isoSub.getLenExon();
		isFrameShift = (changeLen-snpInfo.getSeqAlt().length()) % 3 != 0;
		if (isNeedAAanno && isFrameShift) {
			setVarType.add(EnumVariantClass.frameshift_variant);
		}
	}

	@Override
	protected int moveBeforeNum() {
		return 0;
	}
	
	public boolean isNeedHgvspDetail() {
		return isNeedAAanno;
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
			if (startCdsTmp == startCds) {
				isStartAtAAstart = true;
			}
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
		if (startNum == 0) {
			startCds = iso.getStart();
		}
		if (endNum == 0) {
			endCds = iso.getEnd();
		}
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
	
	//========================================================
	//TODO 以下代码尚未审核
	// 考虑核酸变化氨基酸没有变化的场景
	//========================================================
	/** 读码框内的插入改变 */
	private String getDelChangeInFrame() {
		char[] refAA = refSeqNrForAA.toStringAA1().toCharArray();
		char[] altAA = altSeqNrForAA.toStringAA1().toCharArray();
		int start = getAffectAANum(startCds);
		int end = getAffectAANum(endCds);
		
		if (refAA[0] == '*' && altAA[0] == '*') {
			//TODO 未测试
			setVarType.add(EnumVariantClass.stop_retained_variant);
			return convertAA(refAA[0]) + start + "=";
		} else if (!isFrameShift && isAffectUAG) {
			//说明终止密码子被删掉了
			setVarType.add(EnumVariantClass.stop_lost);
			return getInDelChangeFrameShift(true, true);
		}
		setIsStopGain(refAA, altAA);
		int seqLenMin = Math.min(refAA.length, altAA.length);
		int seqLenMax = Math.min(refAA.length, altAA.length);
		int refStart = getStartSame(refAA, altAA);
		int refEnd = 0;
		if (refStart < seqLenMin) {
			refEnd = SnpIsoHgvsp.getEndSame(refAA, altAA);
			if (refEnd+refStart >= seqLenMax) {
				refEnd = seqLenMin - refStart;
			}
		}
		
		if (isStartAtAAstart) {
			setVarType.add(EnumVariantClass.conservative_inframe_deletion);
		} else {
			setVarType.add(EnumVariantClass.disruptive_inframe_deletion);
		}
		
		StringBuilder sBuilderDelDup = new StringBuilder();
		int startDup = start+refStart, endDup = end-refEnd;
		for (int i = refStart; i < refAA.length-refEnd; i++) {
			sBuilderDelDup.append(refAA[i]);
		}
		if (altAA.length-refStart-refEnd <= 0 && isGetAllLenAA()) {
			return getDeletionDuplicate(sBuilderDelDup.toString(), startDup, endDup);
		}
		
		StringBuilder sBuilder = new StringBuilder();
		if (start+refStart == end-refEnd) {
			sBuilder.append(convertAA(refAA[refStart]) + (start+refStart) + "del");
		} else {
			sBuilder.append(convertAA(refAA[refStart]) + (start+refStart) + "_" + convertAA(refAA[refAA.length-refEnd-1]) + (end-refEnd) + "del");
		}
		if (altAA.length-refStart-refEnd > 0) {
			sBuilder.append("ins");
		}
		for (int i = refStart; i < altAA.length-refEnd; i++) {
			sBuilder.append(convertAA(altAA[i]));	
		}
		return sBuilder.toString();
	}
	
	private String getDeletionDuplicate(String indelAA, int startAA, int endAA) {
		String aaSeq = aa.toStringAA1();
		char[] aaChr = aaSeq.toCharArray();
		SnpIndelRealignHandle snpIndelRealignHandle = new SnpIndelRealignHandle(new Align("", startAA, endAA), indelAA, "", "");
		snpIndelRealignHandle.handleSeqAlign(new SeqHashAAforHgvs(aaSeq));
		snpIndelRealignHandle.moveAlignToAfter();
		Align reAlign = snpIndelRealignHandle.getRealign();
		StringBuilder sBuilderResult = new StringBuilder();
		sBuilderResult.append(convertAA(aaChr[reAlign.getStartAbs()-1]));
		sBuilderResult.append(reAlign.getStartAbs());
		if (reAlign.getStartAbs() != reAlign.getEndAbs()) {
			sBuilderResult.append("_");
			sBuilderResult.append(convertAA(aaChr[reAlign.getEndAbs()-1]));
			sBuilderResult.append(reAlign.getEndAbs());
		}
		sBuilderResult.append("del");
		return sBuilderResult.toString();
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
