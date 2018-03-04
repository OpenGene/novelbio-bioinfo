package com.novelbio.analysis.seq.snphgvs;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;

class SnpRefAltIsoSnp extends SnpIsoHgvsp {
	boolean isATG = false;
	boolean isUAG = false;

	public SnpRefAltIsoSnp(SnpInfo snpRefAltInfo, GffGeneIsoInfo iso) {
		super(snpRefAltInfo, iso);
	}
	
	protected boolean isGetAllLenAA() {
		return false;
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
			setVarType.add(EnumVariantClass.missense_variant);
			return "p." + getInDelChangeFrameShift(true, false);
		}
		
		if (ref.equals(alt)) {
			setVarType.add(EnumVariantClass.synonymous_variant);
			if (isUAG) {
				setVarType.remove(EnumVariantClass.stop_lost);
				setVarType.add(EnumVariantClass.stop_retained_variant);
			}
			return "p." + ref + getAffectAANum(snpRefAltInfo.getStartReal()) + "="; 
		}
		if (isATG) {
			setVarType.add(EnumVariantClass.start_lost);
			setVarType.add(EnumVariantClass.missense_variant);
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
	
	protected boolean isGetAllLenAA() {
		return snpRefAltInfo.getSeqAlt().length() %3 == 0;
	}

	@Override
	protected int moveBeforeNum() {
		if (iso.isCis5to3()) {
			snpRefAltInfo.moveToAfter();
		} else {
			snpRefAltInfo.moveToBefore();
		}
		int moveMax = snpRefAltInfo.moveNumMax();
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
				snpRefAltInfo.moveAlign(1, iso.isCis5to3());
				site = snpRefAltInfo.getStartReal();
				coordExonNum = iso.getNumCodInEle(site);
			}
		}
		if (!iso.isCodInAAregion(site)) {
			return moveNum;
		}
		//在氨基酸上
		int beforeSite = iso.getLocAAbefore(site);
		int afterSite = iso.getLocAAend(site);
		boolean isInFrame = snpRefAltInfo.getSeqAlt().length() %3 == 0;
		//如果可以移动出iso，则移出去
		int siteToAtg = iso.isCis5to3() ? site-iso.getATGsite() + 1 : iso.getATGsite() - site;
		if ( siteToAtg > 0 && siteToAtg <= moveMax - moveNum) {
			return moveNum + siteToAtg;
		}

		//正方向，最多移动一位
		if (isInFrame && iso.isCis5to3() && site != afterSite && site - beforeSite + 1 <= moveMax - moveNum) {
			moveNum = moveNum + site - beforeSite + 1;
		} else if (isInFrame && !iso.isCis5to3() && site != beforeSite && beforeSite-site <= moveMax-moveNum) {
//			moveNum = moveNum + site - afterSite + 1; //这是老版本，仅向右移动一位
			//新版本做法是，如果为 ATC-ACG-ACG-ACG-[ACG]-ACC
			//则移动为 ATC-[ACG]-ACG-ACG-ACG-ACC
			//意思移动到iso的最后部
//			int remain = Math.min(moveMax-moveNum, iso.getCod2ExInEnd(site)+1);
//			int num = site - afterSite + 1;
//			num = (remain - num)/3 * 3 + num;
//			moveNum = moveNum + num;
			
			moveNum = moveNum + beforeSite-site;
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
			//在一个aa内部，不直接用endCds-startCds是因为一个氨基酸可能横跨intron，这时候直接减就会出问题
			if (iso.getLocDistmRNA(startCds, endCds) == 2 && endCds != iso.getUAGsite()) {
				/**
				 * 如果插入在一个aa内部，end会向后提取一个氨基酸
				 * 可能会出现场景 GTC-CT[CAG]G-AGC
				 * 实际插入为 L-R-S
				 * 而正常取是不会把AGC(S)取到的，所以要向后取一个氨基酸
				 */
				endCds = iso.getLocAANextEnd(endCds);
				if (startCds == iso.getATGsite()) {
					isInsertInStartAndInFrame = true;
				}
			}
			/**
			 * 默认insertion都会向前提取一个氨基酸
			 * 可能会出现场景 GTC-C[AGT]TG-AGC
			 * 实际插入为 V-Q-L
			 * 而正常取是不会把GTC(V)取到的，所以要向前取一个氨基酸
			 */
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
		int startNum = iso.getNumCodInEle(startCds);
		if (startNum < 0) {
			startCds = iso.getLsElement().get(Math.abs(startNum)-1).getEndCis();
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
			/**
			 * 默认insertion都会向前提取一个氨基酸
			 * 可能会出现场景 GTC-C[AGT]TG-AGC
			 * 实际插入为 V-Q-L
			 * 而正常取是不会把GTC(V)取到的，所以要向前取一个氨基酸，这时候这里就要加上3，意思往后偏移3
			 */
			snpOnReplaceLocStart = snpOnReplaceLocStart + 3;
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
			setVarType.remove(EnumVariantClass.stop_lost);
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
	//老版本，没有考虑duplicate
	@Deprecated
	private String getInsertionBetweenAA2(char[] refAA, char[] altAA) {
		if (altAA[0] != refAA[0] || altAA[altAA.length-1] != refAA[1]) {
			throw new ExceptionNBCSnpHgvs("error");
		}
		setVarType.add(EnumVariantClass.conservative_inframe_insertion);
		
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(convertAA(refAA[0]) + getAffectAANum(startCds) 
		+ "_" + convertAA(refAA[1]) + getAffectAANum(endCds) + "ins");	
		
		StringBuilder sBuilderInsertAA = new StringBuilder();
		for (int i = 1; i < altAA.length-1; i++) {
			sBuilder.append(convertAA(altAA[i]));
			sBuilderInsertAA.append(altAA[i]);
		}
		return sBuilder.toString();
	}

	//老版本，没有考虑duplicate
	@Deprecated
	private String getInsertionInAA2(char[] refAA, char[] altAA) {
		StringBuilder sBuilder = new StringBuilder();
		int startNum = getAffectAANum(startCds);
		
		if(refAA[1] == '*' && altAA[1] == '*') {
			sBuilder.append(convertAA(refAA[1]) + (startNum+1) + "=");
			setVarType.remove(EnumVariantClass.stop_lost);
			setVarType.add(EnumVariantClass.stop_retained_variant);
		} else if (refAA[1] == altAA[1]) {					
			sBuilder.append(convertAA(refAA[1]) + (startNum +1)
			+ "_" + convertAA(refAA[2]) + (startNum+2) + "ins");
			for (int i = 2; i < altAA.length-1; i++) {
				sBuilder.append(convertAA(altAA[i]));
			}
			setVarType.add(EnumVariantClass.disruptive_inframe_insertion);
		} else if (refAA[1] == altAA[altAA.length-2]) {
			sBuilder.append(convertAA(refAA[0]) + startNum 
			+ "_" + convertAA(refAA[1]) + (startNum+1) + "ins");	
			for (int i = 1; i < altAA.length-2; i++) {
				sBuilder.append(convertAA(altAA[i]));	
			}
			//TODO 这里需要考虑是不是同义突变
			//譬如 ATC-CGC-CAG
			//变为 ATC-CG[TAG]-CCAG
			setVarType.add(EnumVariantClass.disruptive_inframe_insertion);
		} else {
			sBuilder.append(convertAA(refAA[1]) + (startNum +1) + "delins");	
			for (int i = 1; i < altAA.length-1; i++) {
				sBuilder.append(convertAA(altAA[i]));	
			}
			setVarType.add(EnumVariantClass.disruptive_inframe_insertion);
		}
		return sBuilder.toString();
	}
	
	/** insertion在两个aa中间 */
	private String getInsertionBetweenAA(char[] refAA, char[] altAA) {
		if (altAA[0] != refAA[0] || altAA[altAA.length-1] != refAA[1]) {
			throw new ExceptionNBCSnpHgvs("error");
		}
		setVarType.add(EnumVariantClass.conservative_inframe_insertion);		
		
		int startAA = getAffectAANum(startCds);
		int endAA = getAffectAANum(endCds);
		StringBuilder sBuilderAlt = new StringBuilder();
		for (int i = 1; i < altAA.length-1; i++) {
			sBuilderAlt.append(altAA[i]);
		}
		String indelAA = sBuilderAlt.toString();		
		return getInsertionDuplicate(indelAA, startAA, endAA);
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
		
		//用来判定是否为duplicate
		StringBuilder sBuilderInsertAA = new StringBuilder();
		
		if(refAA[1] == '*' && altAA[1] == '*') {
			sBuilder.append(convertAA(refAA[1]) + (startNum+1) + "=");
			setVarType.remove(EnumVariantClass.stop_lost);
			setVarType.add(EnumVariantClass.stop_retained_variant);
		} else if (refAA[1] == altAA[1]) {
			setVarType.add(EnumVariantClass.disruptive_inframe_insertion);
			for (int i = 2; i < altAA.length-1; i++) {
				sBuilderInsertAA.append(altAA[i]);
			}
			return getInsertionDuplicate(sBuilderInsertAA.toString(), startNum +1, startNum+2);
		} else if (refAA[1] == altAA[altAA.length-2]) {
			setVarType.add(EnumVariantClass.disruptive_inframe_insertion);
			for (int i = 1; i < altAA.length-2; i++) {
				sBuilderInsertAA.append(altAA[i]);
			}
			return getInsertionDuplicate(sBuilderInsertAA.toString(), startNum, startNum+1);
			//TODO 这里需要考虑是不是同义突变
			//譬如 ATC-CGC-CAG
			//变为 ATC-CG[TAG]-CCAG
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
