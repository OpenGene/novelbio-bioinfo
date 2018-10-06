package com.novelbio.software.coordtransform;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;

public abstract class CoordPairSearchAbs {
	
	public abstract List<CoordPair> findCoordPairsOverlap(Align alignRef);
	
	public VarInfo findVarInfo(CoordPair coordPair, int start, int end) {
		validateSiteInCoord(coordPair, start);
		validateSiteInCoord(coordPair, end);
		
		VarInfo varInfo = new VarInfo();
		varInfo.setCis5to3(coordPair.getAlignAlt().isCis5to3());
		varInfo.setChrId(coordPair.getChrAlt());
		
		if (isCoordLsIndelEmpty(coordPair)) {
			int[] startAlt2Bias = getAltSite(coordPair, null, start, true);
			int[] endAlt2Bias = getAltSite(coordPair, null, end, false);
			varInfo.setStartCis(startAlt2Bias[0]);
			varInfo.setEndCis(endAlt2Bias[0]);
			varInfo.setStartBias(startAlt2Bias[1]);
			varInfo.setEndBias(endAlt2Bias[1]);
			return varInfo;
		}
		
		//单个位点
		if (start == end) {
			IndelForRef indelForRef = getIndelForRef(coordPair, start);
			int[] startAlt2Bias = getAltSite(coordPair, indelForRef, start, true);
			if (startAlt2Bias[1] > 0) {
				return null;
			}
			varInfo.setStartCis(startAlt2Bias[0]);
			varInfo.setEndCis(startAlt2Bias[0]);
			return varInfo;
		}
		
		IndelRefPair indelRefPair = getIndelForRef(coordPair, start, end);
		int[] startAlt2Bias = getAltSite(coordPair, indelRefPair.getLeft(), start, true);
		int[] endAlt2Bias = getAltSite(coordPair, indelRefPair.getRight(), end, false);
		//区段位于ref相对于alt多的区段
		if (coordPair.getAlignAlt().isCis() && endAlt2Bias[0] < startAlt2Bias[0]
				|| !coordPair.getAlignAlt().isCis() && startAlt2Bias[0] < endAlt2Bias[0]
				) {
			return null;
		}
		varInfo.setStartCis(startAlt2Bias[0]);
		varInfo.setEndCis(endAlt2Bias[0]);
		varInfo.setStartBias(startAlt2Bias[1]);
		varInfo.setEndBias(endAlt2Bias[1]);
		
		List<IndelForRef> lsIndels = indelRefPair.getLsCovered();
		if (!lsIndels.isEmpty()) {
			varInfo.setLsIndelForRefs(lsIndels);
		}
		return varInfo;
	}
	
	private void validateSiteInCoord(CoordPair coordPair, int site) {
		if (!Alignment.isSiteInAlign(coordPair, site)) {
			throw new ExceptionNBCCoordTransformer("refsite " + site + " is not in " + this.toString());
		}
	}

	protected abstract boolean isCoordLsIndelEmpty(CoordPair coordPair);

	protected abstract IndelForRef getIndelForRef(CoordPair coordPair, int start);
	
	protected abstract IndelRefPair getIndelForRef(CoordPair coordPair, int start, int end);
		
	/**
	 * 获取refsite起点所对应的位置
	 * @param refSite
	 * @param isStart 是起点还是终点
	 * @return 0：偏移之后的起点，1：偏移的bp
	 * 注意如果是 isStart 则向右偏移， isEnd向左偏移 
	 */
	private int[] getAltSite(CoordPair coordPair, IndelForRef indelForRef, int refSite, boolean isStart) {
		int altSite = 0, bias = 0;
		if (indelForRef == null) {
			int length = refSite - coordPair.alignRef.getStartAbs();
			altSite = coordPair.alignAlt.isCis() ? coordPair.alignAlt.getStartAbs()+length : coordPair.alignAlt.getEndAbs() - length;
			return new int[] {altSite, bias};
		}

		if (!Alignment.isSiteInAlign(indelForRef, refSite)) {
			if (indelForRef.getStartAbs() > refSite) {
				int length = refSite - coordPair.alignRef.getStartAbs();
				altSite = coordPair.alignAlt.isCis() ? coordPair.alignAlt.getStartAbs()+length : coordPair.alignAlt.getEndAbs() - length;
			} else {
				int length = refSite - indelForRef.getEndAbs();
				altSite = indelForRef.getEndExtendAlt(length);
			}
		} else {
			if (refSite == indelForRef.getStartAbs()) {
				altSite = indelForRef.getStartCisAlt();
				bias = 0;
			} else if (refSite == indelForRef.getEndAbs()) {
				altSite = indelForRef.getEndCisAlt();
				bias = 0;
			} else {
				//在indel里面
				if (isStart) {
					altSite = indelForRef.getEndCisAlt();
					bias = indelForRef.getEndAbs()-refSite;
				} else {
					altSite = indelForRef.getStartCisAlt();
					bias = refSite-indelForRef.getStartAbs();
				}
			}
		}
		return new int[] {altSite, bias};
	}
	
	public static class IndelRefPair {
		IndelForRef left;
		IndelForRef right;
		
		List<IndelForRef> lsCovered = new ArrayList<>();
		
		public void setLeft(IndelForRef left) {
			this.left = left;
		}
		public void setRight(IndelForRef right) {
			this.right = right;
		}
		public void setLsCovered(List<IndelForRef> lsCovered) {
			this.lsCovered = lsCovered;
		}
		public IndelForRef getLeft() {
			return left;
		}
		public IndelForRef getRight() {
			return right;
		}
		public List<IndelForRef> getLsCovered() {
			return lsCovered;
		}
	}
}
