package com.novelbio.analysis.seq.snphgvs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;

class SnpRefAltIsoIndel extends SnpIsoHgvsp {
	private static final Logger logger = LoggerFactory.getLogger(SnpRefAltIsoIndel.class);
	
	boolean isFrameShift = false;
	/** indel的起点是否在一个氨基酸的三联密码子头部 */
	boolean isAffectUAG = false;
	boolean isNeedAAanno = false;
	/** 有这个标记的直接返回Met1?或者Met1fs */
	boolean isAffectATG = false;
		
	/** startCds是不是提取了实际endCds的前一个AA头部A */
	boolean isStartLastAA = true;
	
	/** alt和ref相比增加了几个碱基，仅用于非移码突变 */
	int changeAltLen = 0;
	
	/** 是否切除了整个intron  */
	boolean isAllIntron = false;
	
	protected boolean isGetAllLenAA() {
		int siteStart = getStartCis();
		int siteEnd = getEndCis();
		
		int startExNum = iso.getNumCodInEle(siteStart);
		int endExNum = iso.getNumCodInEle(siteEnd);
		if (isAllIntron) {
			return true;
		}
		if (startExNum != endExNum || startExNum <= 0) {
			return false;
		}
		if (siteStart < iso.getATGsite() || isFrameShift || isAffectUAG) {
			return false;
		}
		return true;
	}
	
	public SnpRefAltIsoIndel(SnpInfo snpInfo, GffGeneIsoInfo iso) {
		super(snpInfo, iso);
		int startCis = getStartCis();
		int endCis = getEndCis();
		
		int[] region = getValidRange(new int[]{startCis, endCis}, new int[]{iso.getATGsite(), iso.getUAGsite()});
		if(region == null) return;
		GffGeneIsoInfo isoSub = iso.getSubGffGeneIso(region[0], region[1]);
		isNeedAAanno = !isoSub.isEmpty();
		
		// 如果缺失正好是整个的intron，并且这个intron在cds区内部
		int numStart = iso.getNumCodInEle(startCis);
		if (!isNeedAAanno && numStart == iso.getNumCodInEle(endCis) && numStart < 0 //同一个内含子中
				&& startCis < Math.max(iso.getATGsite(), iso.getUAGsite()) //在cds区内部
				&& startCis > Math.min(iso.getATGsite(), iso.getUAGsite()) 
				&& iso.getCod2ExInStart(startCis) == 0 //整个内含子
				&& iso.getCod2ExInEnd(endCis) == 0
				) {
			isAllIntron = true;
			isNeedAAanno = true;
		}
		
		int changeLen = isoSub.getLenExon();
		changeAltLen = (changeLen-snpInfo.getSeqAlt().length());
		isFrameShift = changeAltLen % 3 != 0;
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
			isAffectATG = true;
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
			if (isAllIntron) {
				startCds = iso.getLsElement().get(Math.abs(endNum)-1).getEndCis();
			}
		} else if (startNum == 0) {
			startCds = iso.getStart();
		}
		if (iso.isCodInAAregion(startCds)) {
			int startCdsTmp = iso.getLocAALastStart(startCds);
			if (startCdsTmp <= 0) {
				isStartLastAA = false;
				startCdsTmp = iso.getLocAAbefore(startCds);
			}
			startCds = startCdsTmp;
		}
		
		if (endNum < 0) {
			endCds = iso.getLsElement().get(Math.abs(endNum)-1).getEndCis();
		} else if (endNum == 0) {
			endCds = iso.getEnd();
		}
		if (iso.isCodInAAregion(endCds) && !isAffectUAG) {
			endCds = iso.getLocAANextEnd(endCds);
		}
		
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
			if (isAllIntron) {
				startCds = iso.getLsElement().get(Math.abs(endNum)-1).getEndCis();
			} else {
				startCds = iso.getLsElement().get(Math.abs(startNum)).getStartCis();
			}
		}
		if (endNum < 0) {
			endCds = iso.getLsElement().get(Math.abs(endNum)-1).getEndCis();
		}
		snpOnReplaceLocStart = -iso.getLocAAbeforeBias(startCds) + 1;
		if (isStartLastAA) {
			snpOnReplaceLocStart = snpOnReplaceLocStart + 3;
		}
		snpOnReplaceLocEnd = snpOnReplaceLocStart + iso.getLocDistmRNA(startCds, endCds);
		if (isAllIntron) {
			snpOnReplaceLocEnd = snpOnReplaceLocStart-1;
		}
	}
	
	public String getSnpChange() {
		if (isAffectATG) {
			return "p." + convertAA("M") +"1?";
		}
		
		String info =  isFrameShift? getInDelChangeFrameShift(false, false) : getDelChangeInFrame();
		return "p." + info;
	}
	
	//========================================================
	//TODO 以下代码尚未测试
	// 考虑核酸变化氨基酸没有变化的场景
	//========================================================
	/** 读码框内的插入改变 */
	private String getDelChangeInFrame() {
		char[] refAA = refSeqNrForAA.toStringAA1().toCharArray();
		char[] altAA = altSeqNrForAA.toStringAA1().toCharArray();
		if (!isStartLastAA) {
			refAA = addFirst(refAA);
			altAA = addFirst(altAA);
		}
		String refAAstr = new String(refAA);
		String altAAstr = new String(altAA);
		
		int start = getAffectAANum(startCds);
		int end = getAffectAANum(endCds);
		if (!isStartLastAA) {
			start = start-1;
		}
		if (refAA[1] == '*' && altAA[1] == '*') {
			//TODO 未测试
			setVarType.remove(EnumVariantClass.stop_lost);
			setVarType.add(EnumVariantClass.stop_retained_variant);
			return convertAA(refAA[1]) + (start+1) + "=";
		} else if (!isFrameShift && isAffectUAG) {
			if (refAAstr.contains("*") && altAAstr.contains("*")) {
				setVarType.remove(EnumVariantClass.stop_lost);
				int refAAterIndex = refAAstr.indexOf("*");
				int altAAterIndex = altAAstr.indexOf("*");
				if (refAAterIndex == refAA.length-2 && altAAterIndex == altAA.length-2) {
					setVarType.add(EnumVariantClass.stop_retained_variant);
				} else if (refAAterIndex < altAAterIndex) {
					//TODO 吃不准
					setVarType.add(EnumVariantClass.stop_lost);
				} else if (refAAterIndex == altAAterIndex) {
					setVarType.add(EnumVariantClass.stop_retained_variant);
				} else {//refAAterIndex > altAAterIndex
					setVarType.add(EnumVariantClass.stop_gained);
				}
			} else if (refAAstr.contains("*") && !altAAstr.contains("*")) {
				setVarType.add(EnumVariantClass.stop_lost);
			}
			//说明终止密码子被删掉了
			return getInDelChangeFrameShift(true, true);
		}
		
		if (refAAstr.equals(altAAstr)) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(convertAA(refAA[1]) + (start+1));
			if (refAA.length-2 > 1) {
				stringBuilder.append("_");
				stringBuilder.append(convertAA(refAA[refAA.length-2]) + (end-1));
			}
			stringBuilder.append("=");
			return stringBuilder.toString();
		}
		
		setIsStopGain(refAA, altAA);
		int[] startEndSameIndex = SnpInfo.getStartEndSameIndex(refAA, altAA);
		int startSameIndex = startEndSameIndex[0], endSameIndex = startEndSameIndex[1];
		//插入
		if (startSameIndex+ endSameIndex >= refAA.length) {
			StringBuilder sBuilder = new StringBuilder();
			for (int i = startSameIndex; i < altAA.length - endSameIndex; i++) {
				sBuilder.append(altAA[i]);
			}
			logger.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
			return getInsertionDuplicate(sBuilder.toString(), start + startSameIndex-1, start + startSameIndex);
		}
		//缺失
		if (startSameIndex+ endSameIndex >= altAA.length) {
			StringBuilder sBuilder = new StringBuilder();
			for (int i = startSameIndex; i < refAA.length - endSameIndex; i++) {
				sBuilder.append(refAA[i]);
			}
			logger.info("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
			return getDeletionDuplicate(sBuilder.toString(), start + startSameIndex, end-endSameIndex);
		}
		
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(convertAA(refAA[startSameIndex]) + (start + startSameIndex));
		sBuilder.append("_"); 
		sBuilder.append(convertAA(refAA[refAA.length-endSameIndex]) + (end - endSameIndex + 1));
		sBuilder.append("del");
		if (startSameIndex + endSameIndex < altAA.length) {
			sBuilder.append("ins");
		}
		for (int i = startSameIndex; i < altAA.length-endSameIndex-1; i++) {
			sBuilder.append(convertAA(altAA[i]));
		}
		logger.info("ccccccccccccccccccccccccccccccccccccccccccc");
		return sBuilder.toString();
	}
	
	private char[] addFirst(char[] aa) {
		char[] aaNew = new char[aa.length+1];
		aaNew[0] = 'X';
		for (int i = 1; i < aaNew.length; i++) {
			aaNew[i] = aa[i-1];
		}
		return aaNew;
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
