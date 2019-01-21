package com.novelbio.software.snpanno;

import com.novelbio.bioinfo.gff.GffIso;

class SnpIsoHgvspDel extends SnpIsoHgvsp {
	boolean isFrameShift = false;
	/** del的起点是否在一个氨基酸的三联密码子头部 */
	boolean isStartAtAAstart = false;
	boolean isAffectUAG = false;
	
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
		if (iso.isCis5to3() && siteStart < iso.getATGsite() || !iso.isCis5to3() && siteStart > iso.getATGsite()  || isFrameShift || isAffectUAG) {
			return false;
		}
		if ((Math.abs(siteEnd - siteStart) + 1) %3 != 0) {
			return false;
		}
		return true;
	}
	
	public SnpIsoHgvspDel(SnpInfo snpRefAltInfo, GffIso iso) {
		super(snpRefAltInfo, iso);
	}
	
	@Override
	protected int moveBeforeNum() {
		int moveMax = snpInfo.moveNumMax();
		if (moveMax == 0) {
			return 0;
		}
		
		if (iso.isCis5to3()) {
			snpInfo.moveToAfter();
		} else {
			snpInfo.moveToBefore();
		}
		
		int siteStart = getStartCis();
		int siteEnd = getEndCis();
		if (!iso.isRegionOnIso(siteStart, siteEnd)) {
			return 0;
		}
		GffIso isoSub = iso.getSubGffGeneIso(siteStart, siteEnd);

		if (isoSub.size() > 1) {
			return 0;
		}
		
		int moveNum = 0;
		if (isoSub.size() == 0) {
			moveNum = getMoveNumIntron(moveMax, siteStart, siteEnd);
			if (moveNum == 0 || moveNum == moveMax) {
				return moveNum;
			}
			snpInfo.moveAlign(moveNum, iso.isCis5to3());
			siteStart = getStartCis();
			siteEnd = getEndCis();
			if (!iso.isRegionOnIso(siteStart, siteEnd)) {
				return moveNum;
			}
			isoSub = iso.getSubGffGeneIso(siteStart, siteEnd);
			if (isoSub.size() == 0) {
				return moveNum;
			}
		}
		
		int startExNum = iso.getNumCodInEle(siteStart);
		int endExNum = iso.getNumCodInEle(siteEnd);
		if (startExNum < 0 && endExNum < 0 || startExNum == 0 || endExNum == 0) {
			return 0;
		}
		if (startExNum < 0) {
			int num = getMoveNumStartIntronEndExon(moveMax-moveNum, siteStart, siteEnd);
			moveNum += num;
			if (num == 0 || moveNum == moveMax) {
				return moveNum;
			}
			snpInfo.moveAlign(moveNum, iso.isCis5to3());
			siteStart = getStartCis();
			siteEnd = getEndCis();
		} else if (endExNum < 0) {
			int num = getMoveNumStartExonEndIntron(moveMax-moveNum, siteStart, siteEnd);
			moveNum += num;
			if (num == 0 || moveNum == moveMax) {
				return moveNum;
			}
			snpInfo.moveAlign(moveNum, iso.isCis5to3());
			siteStart = getStartCis();
			siteEnd = getEndCis();
		}
		if (!iso.isRegionOnIso(siteStart, siteEnd)) {
			return moveNum;
		}
		isoSub = iso.getSubGffGeneIso(siteStart, siteEnd);
		startExNum = iso.getNumCodInEle(siteStart);
		endExNum = iso.getNumCodInEle(siteEnd);
		
		if ((startExNum != endExNum && startExNum < 0 && endExNum < 0) || startExNum == 0 || endExNum == 0) {
			return moveNum;
		}
		if (startExNum == endExNum && startExNum > 0) {
			int num = getMoveNumExon(moveMax-moveNum, siteStart, siteEnd);
			moveNum += num;
			if (num == 0 || moveNum == moveMax) {
				return moveNum;
			}
			snpInfo.moveAlign(moveNum, iso.isCis5to3());
			siteStart = getStartCis();
			siteEnd = getEndCis();
			if (!iso.isRegionOnIso(siteStart, siteEnd)) {
				return moveNum;
			}
			isoSub = iso.getSubGffGeneIso(siteStart, siteEnd);
		}
		
		if (isoSub.size() > 1) {
			return moveNum;
		}
		
		if (isoSub.size() == 0) {
			int num = getMoveNumIntron(moveMax-moveNum, siteStart, siteEnd);
			return moveNum+num;
		}

		return moveNum;
	}
	
	private int getMoveNumIntron(int moveMax, int siteStart, int siteEnd) {
		int start2Start = iso.getCod2ExInStart(siteStart)+1;
		int end2End = iso.getCod2ExInEnd(siteEnd)+1;
		if (start2Start == 1 && end2End == 1 || start2Start > 2 && end2End > 2) {
			return 0;
		}
		int end2Start = iso.getCod2ExInStart(siteEnd) + 1;
		if (start2Start <= 2 && end2Start <= moveMax) {
			return end2Start;
		} else if (end2End <= 2 && start2Start > 3-end2End && 3-end2End <= moveMax) {
			return 3-end2End;
		}
		return 0;
	}
	
	/** start 在intron中，end在exon中 */
	private int getMoveNumStartIntronEndExon(int moveMax, int siteStart, int siteEnd) {
		//--------start-------10====end====20----------------------
		//--10====end====20------------start----------
		int start2Start = iso.getCod2ExInStart(siteStart)+1;
		int end2Start = iso.getCod2ExInStart(siteEnd)+1;
		if (end2Start <= moveMax && start2Start >= end2Start) {
			return end2Start;
		}
		return 0;
	}
	
	/** start 在exon中，end在intron中 */
	private int getMoveNumStartExonEndIntron(int moveMax, int siteStart, int siteEnd) {
		//----10====start====20-------end---------------
		int end2Start = iso.getCod2ExInStart(siteEnd)+1;
		if (end2Start <= moveMax) {
			return end2Start;
		}
		return 0;
	}
	
	/**
	 * 要么直接移动到内含子中，要么就仅移动到两个exon中
	 * @param moveMax
	 * @param siteStart
	 * @param siteEnd
	 * @return
	 */
	private int getMoveNumExon(int moveMax, int siteStart, int siteEnd) {
		//直接移动出exon
		//----------10==start====end==20----------------------
		int end2Start = iso.getCod2ExInStart(siteEnd) + 1;
		boolean isAfterUAG = iso.isCis5to3() ? siteStart > iso.getUAGsite() : siteEnd < iso.getUAGsite();
		if (!isAfterUAG && end2Start + 2 <= moveMax) {
			return end2Start+2;
		}
		
		//覆盖到ATG和UAG的处理方式
		if (Math.abs(siteEnd - iso.getATGsite()) + 1<= moveMax) {
			// ---ATG==start==end====||---------------
			return Math.abs(siteEnd - iso.getATGsite()) + 1;
		}
		
		if ((Math.abs(siteStart - siteEnd) + 1) %3 != 0) {
			return 0;
		}
		//仅移动到两个密码子中间
		//----------10==start====end==20----------------------
		int siteBefore = iso.getLocAAbefore(siteStart);
		int distanceToSiteBefore = Math.abs(siteBefore - siteStart);
		if (distanceToSiteBefore < 3 && distanceToSiteBefore < moveMax) {
			return distanceToSiteBefore;
		}
		return 0;
	}
	public boolean isNeedHgvspDetail() {
		boolean isNeedAAanno = false;
		int[] region = getValidRange(new int[]{getStartCis(), getEndCis()}, new int[]{iso.getATGsite(), iso.getUAGsite()});
		if(region == null) return isNeedAAanno;
		
		GffIso isoSub = iso.getSubGffGeneIso(region[0], region[1]);
		isNeedAAanno = !isoSub.isEmpty();
		isFrameShift = isoSub.getLenExon() % 3 != 0;
		if (isNeedAAanno && isFrameShift) {
			setVarType.add(EnumVariantClass.frameshift_variant);
		}
		return isNeedAAanno;
	}
	
	protected void setStartEndCis() {
		int atgStart = iso.getATGsite();
		int atgEnd = iso.getLocAAend(iso.getATGsite());
		int uagEnd = iso.getUAGsite();
		int uagStart = iso.getLocAAbefore(iso.getUAGsite());

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
		
		/**
		 * 如果是移码突变，则获取全长序列，这样可以知道多少位后终止
		 * 但是如果endCds太靠近iso.getEnd，那么可能只知道丢失了终止密码子，具体后面多少距离终止密码子出现不知道
		 * 这里就是往后面多取点，好歹知道最后终止密码子在多远出现了
		 */
		if (isFrameShift || isAffectUAG) {
			int distanceToEnd = Math.abs(endCds-iso.getEnd());
			int minLen = 150;
			if (distanceToEnd < 100) {
				endCds = iso.isCis5to3() ? iso.getEnd() + minLen - distanceToEnd : iso.getEnd() - minLen + distanceToEnd;
			} else {
				endCds = iso.getEnd();
			}
			
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
		boolean isExtend = false;
		if (isFrameShift) {
			char[] refAA = refSeqNrForAA.toStringAA1().toCharArray();
			char[] altAA = altSeqNrForAA.toStringAA1().toCharArray();
			int num = 0;
			for (; num < Math.min(refAA.length, altAA.length); num++) {
				if (refAA[num] != altAA[num]) {
					break;
				}
			}
			if (refAA[num] == '*' && isAffectUAG) {
				isExtend = true;
			}
		}
		
		String info =  isFrameShift? getInDelChangeFrameShift(isExtend, false) : getDelChangeInFrame();
		return "p." + info;
	}
	
	/** 读码框内的插入改变 */
	private String getDelChangeInFrame() {
		char[] refAA = refSeqNrForAA.toStringAA1().toCharArray();
		char[] altAA = altSeqNrForAA.toStringAA1().toCharArray();
		int start = getAffectAANum(startCds);
		int end = getAffectAANum(endCds);
		if (isRefAltEqual(refAA, altAA)) {
			setVarType.add(EnumVariantClass.synonymous_variant);
			//TODO 待验证
			return convertAA(refAA[0]) + start + "_" + convertAA(refAA[refAA.length-1]) + end + "=";
		} else if (refAA[0] == '*' && altAA[0] == '*') {
			//TODO 未测试
			setVarType.remove(EnumVariantClass.stop_lost);
			setVarType.add(EnumVariantClass.stop_retained_variant);
			return convertAA(refAA[0]) + start + "=";
		} else if (!isFrameShift && isAffectUAG) {
			//说明终止密码子被删掉了
			setVarType.add(EnumVariantClass.stop_lost);
			return getInDelChangeFrameShift(true, true);
		}
		setIsStopGain(refAA, altAA);
		
		int[] startEndSameIndex = SnpInfo.getStartEndSameIndex(refAA, altAA);
		int startSameIndex = startEndSameIndex[0], endSameIndex = startEndSameIndex[1];
		
		if (isStartAtAAstart) {
			setVarType.add(EnumVariantClass.conservative_inframe_deletion);
		} else {
			setVarType.add(EnumVariantClass.disruptive_inframe_deletion);
		}
		
		if (altAA.length-startSameIndex-endSameIndex <= 0 && isGetAllLenAA()) {
			StringBuilder indelAA = new StringBuilder();
			for (int i = startSameIndex; i < refAA.length-endSameIndex; i++) {
				indelAA.append(refAA[i]);
			}
			return getDeletionDuplicate(indelAA.toString(), start+startSameIndex, end-endSameIndex);			
		}
		
		StringBuilder sBuilder = new StringBuilder();
		if (start+startSameIndex == end-endSameIndex) {
			sBuilder.append(convertAA(refAA[startSameIndex]) + (start+startSameIndex) + "del");
		} else {
			sBuilder.append(convertAA(refAA[startSameIndex]) + (start+startSameIndex) + "_" + convertAA(refAA[refAA.length-endSameIndex-1]) + (end-endSameIndex) + "del");
		}
		if (altAA.length-startSameIndex-endSameIndex > 0) {
			sBuilder.append("ins");
		}
		for (int i = startSameIndex; i < altAA.length-endSameIndex; i++) {
			sBuilder.append(convertAA(altAA[i]));	
		}
		return sBuilder.toString();
	}
	
	private boolean isRefAltEqual(char[] ref, char[] alt) {
		if (ref.length != alt.length) {
			return false;
		}
		for (int i = 0; i < ref.length; i++) {
			if (ref[i] != alt[i]) {
				return false;
			}
		}
		return true;
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
