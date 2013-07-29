package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.rnaseq.JunctionInfo.JunctionUnit;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;

/** 根据junction reads，产生新的iso
 * 暂时没有考虑链特异性测序
 *  */
public class GenerateNewIso {
	private static final Logger logger = Logger.getLogger(GenerateNewIso.class);
	int newIsoReadsNum = 20;
	TophatJunction tophatJunctionNew;
	GffDetailGene gffDetailGene;
	
	public void setTophatJunctionNew(TophatJunction tophatJunctionNew) {
		this.tophatJunctionNew = tophatJunctionNew;
	}
	
	public void setGffDetailGene(GffDetailGene gffDetailGene) {
		this.gffDetailGene = gffDetailGene;
	}
	
	public void reconstructGffDetailGene() {
		String chrID = gffDetailGene.getRefID();
		int star = gffDetailGene.getStartAbs(), end = gffDetailGene.getEndAbs();
		ListCodAbsDu<JunctionInfo, ListCodAbs<JunctionInfo>>  lsJunDu = tophatJunctionNew.searchLocation(chrID, star, end);
		for (JunctionInfo junctionInfo : lsJunDu.getAllGffDetail()) {
			for (JunctionUnit junctionUnit : junctionInfo.lsJunctionUnits) {
				if (junctionUnit.getReadsNumAll() >= newIsoReadsNum && !isJunInGene(junctionUnit)) {
					reconstructIso(junctionUnit);
				}
			}
		}
	}
	
	//TODO 
	private boolean isJunInGene(JunctionUnit junctionUnit) {
		boolean findJun = false;
		int start, end = 0;
		
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.isCis5to3()) {
				start= junctionUnit.getStartAbs(); end = junctionUnit.getEndAbs();
			} else {
				start= junctionUnit.getEndAbs(); end = junctionUnit.getStartAbs();
			}
			//没找到
			if (gffGeneIsoInfo.getCod2ExInEnd(start) !=0 || gffGeneIsoInfo.getCod2ExInStart(end) != 0) {
				continue;
			}
			
			int exonNumStart = gffGeneIsoInfo.getNumCodInEle(start);
			int exonNumEnd = gffGeneIsoInfo.getNumCodInEle(end);
			if (exonNumEnd < exonNumStart) {
				logger.error("出错" + gffDetailGene.getName() + " " + junctionUnit.getStartAbs() + " " + junctionUnit.getEndAbs());
			}
			if (exonNumEnd - exonNumStart == 1) {
				findJun = true;
				break;
			}
		}
		
		return findJun;
	}
	
	/** 根据给定的junctionUnit，重建转录本
	 * 本方法仅用于单元测试
	 *  */
	public void reconstructIso(JunctionUnit junctionUnit) {
		 for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			GffGeneIsoInfo gffGeneIsoInfoNew = getReconstructIso(junctionUnit, gffGeneIsoInfo);
			if (gffGeneIsoInfoNew != null ) {
				gffDetailGene.addIso(gffGeneIsoInfoNew);
				break;
			}
		}
	}
	
	/** 建立新的转录本 */
	//TODO 似乎只对cis有效果
	private GffGeneIsoInfo getReconstructIso(JunctionUnit junctionUnit, GffGeneIsoInfo gffGeneIsoInfo) {
		if (junctionUnit.getStartAbs() < gffGeneIsoInfo.getStartAbs() || junctionUnit.getEndAbs() > gffGeneIsoInfo.getEndAbs()) {
			return null;
		}
		int exonStart = 0, exonEnd = 0;
		List<JunctionUnit> lsJun = new ArrayList<>();
		Set<String> setJunInfo = new HashSet<>();
		if (isEdgeInExon(true, junctionUnit, gffGeneIsoInfo) || isEdgeInExon(false, junctionUnit, gffGeneIsoInfo)) {
			lsJun.add(junctionUnit);
			setJunInfo.add(junctionUnit.key());
		}
		if (isEdgeInExon(true, junctionUnit, gffGeneIsoInfo)) {
			if (gffGeneIsoInfo.isCis5to3()) {
				exonStart = gffGeneIsoInfo.getNumCodInEle(junctionUnit.getStartAbs());
			} else {
				exonStart = gffGeneIsoInfo.getNumCodInEle(junctionUnit.getEndAbs());
			}
		} else {
			exonStart = getExonSiteAndAddLsJun(true, junctionUnit, lsJun, setJunInfo, gffGeneIsoInfo);
		}
	
		if (isEdgeInExon(false, junctionUnit, gffGeneIsoInfo)) {
			if (gffGeneIsoInfo.isCis5to3()) {
				exonEnd = gffGeneIsoInfo.getNumCodInEle(junctionUnit.getEndAbs());
			} else {
				exonEnd = gffGeneIsoInfo.getNumCodInEle(junctionUnit.getStartAbs());
			}
		} else {
			exonEnd = getExonSiteAndAddLsJun(false, junctionUnit, lsJun, setJunInfo, gffGeneIsoInfo);
		}
		if (exonStart < 0 && exonEnd < 0) {
			return null;
		}
		/////////////   根据指定的startexon和endexon，还有listJun的信息，新建转录本 /////////////////////////////////////////
		GffGeneIsoInfo gffGeneIsoInfoNew = gffGeneIsoInfo.clone();
		gffGeneIsoInfoNew.clear();
		for (int i = 0; i < exonStart; i++) {
			gffGeneIsoInfoNew.add(gffGeneIsoInfo.get(i).clone());
		}
		JunctionUnit junStart = lsJun.get(0);
		int endEdge = junStart.getStartAbs();
		if (!gffGeneIsoInfo.isCis5to3()) {
			endEdge = junStart.getEndAbs();
		}
		gffGeneIsoInfoNew.get(gffGeneIsoInfoNew.size() - 1).setEndCis(endEdge);
		for (int i = 1; i < lsJun.size(); i++) {
			ExonInfo exonInfo = new ExonInfo(gffGeneIsoInfoNew, gffGeneIsoInfoNew.isCis5to3(), 0, 0);
			if (gffGeneIsoInfoNew.isCis5to3()) {
				exonInfo.setStartAbs(lsJun.get(i-1).getEndAbs());
				exonInfo.setEndAbs(lsJun.get(i).getStartAbs());
			} else {
				exonInfo.setStartAbs(lsJun.get(i-1).getStartAbs());
				exonInfo.setEndAbs(lsJun.get(i).getEndAbs());
			}
			gffGeneIsoInfoNew.add(exonInfo);
		}
		
		JunctionUnit junEnd = lsJun.get(lsJun.size() - 1);
		int startEdge = junEnd.getEndAbs();
		if (!gffGeneIsoInfo.isCis5to3()) {
			startEdge = junEnd.getStartAbs();
		}
		ExonInfo exonInfo = gffGeneIsoInfo.get(exonEnd - 1).clone();
		exonInfo.setStartCis(startEdge);
		gffGeneIsoInfoNew.add(exonInfo);
		for (int i = exonEnd; i < gffGeneIsoInfo.size(); i++) {
			gffGeneIsoInfoNew.add(gffGeneIsoInfo.get(i).clone());
		}
		return gffGeneIsoInfoNew;
	}
	
	/**
	 * 根据GffGeneIsoInfo，在Junction的前后找，直到找到处于exon中的Jun，返回该Jun的位置，并将jun依次装入list中
	 * @param beforExon
	 * @param junThis
	 * @param lsJun
	 * @param gffGeneIsoInfo
	 * @return 如果输入的iso不满足则返回-100
	 */
	private int getExonSiteAndAddLsJun(boolean beforExon, JunctionUnit junThis, List<JunctionUnit> lsJun, 
			Set<String> setJunInfo, GffGeneIsoInfo gffGeneIsoInfo) {
		int exonNum = -100;
		boolean search = true;
		while (search) {
			List<JunctionUnit> lsJunPrevAfter = getJunPrevAfter(beforExon&&gffGeneIsoInfo.isCis5to3(), junThis);
			if (lsJunPrevAfter.size() == 0) {
				exonNum = getExonNum(beforExon, gffGeneIsoInfo, junThis, lsJun, setJunInfo);
				break;
			}
			
			for (JunctionUnit junPrevTmp : lsJunPrevAfter) {
				if (beforExon) {
					if (!setJunInfo.contains(junThis.key())) {
						lsJun.add(0, junThis);
						setJunInfo.add(junThis.key());
					}
				} else {
					if (!setJunInfo.contains(junThis.key())) {
						lsJun.add(junThis);
						setJunInfo.add(junThis.key());
					}
				}
				if (isEdgeInExon(beforExon,junThis, junPrevTmp, gffGeneIsoInfo)) {
					exonNum = getExonNum(beforExon, gffGeneIsoInfo, junThis, lsJun, setJunInfo);
					search = false;
					break;
				}
			}
			junThis = lsJunPrevAfter.get(0);
		}
		return exonNum;
	}
	
	private int getExonNum(boolean beforExon, GffGeneIsoInfo gffGeneIsoInfo, JunctionUnit junThis, 
			List<JunctionUnit> lsJun, Set<String> setJunInfo) {
		int exonNum = 0;
		if (beforExon) {
			if (gffGeneIsoInfo.isCis5to3()) {
				exonNum = Math.abs(gffGeneIsoInfo.getNumCodInEle(junThis.getStartAbs()));
			} else {
				exonNum = Math.abs(gffGeneIsoInfo.getNumCodInEle(junThis.getEndAbs()));
			}
			if (!setJunInfo.contains(junThis.key())) {
				lsJun.add(0, junThis);
				setJunInfo.add(junThis.key());
			}
		} else {
			if (gffGeneIsoInfo.isCis5to3()) {
				exonNum = gffGeneIsoInfo.getNumCodInEle(junThis.getEndAbs());
			} else {
				exonNum = gffGeneIsoInfo.getNumCodInEle(junThis.getStartAbs());
			}
			if (exonNum < 0) {
				exonNum = Math.abs(exonNum) + 1;
			}
			if (!setJunInfo.contains(junThis.key())) {
				lsJun.add(junThis);
				setJunInfo.add(junThis.key());
			}
		}
		return exonNum;
	}
	
	private List<JunctionUnit> getJunPrevAfter(boolean prev, JunctionUnit junctionUnit) {
		try {
			if (prev) {
				return getJunPrev(junctionUnit);
			} else {
				return getJunAfter(junctionUnit);
			}
		} catch (Exception e) {
			return new ArrayList<>();
		}

	}
	
	/**
	 * 指定的junction是否在exon中
	 * @param beforExon 是靠前的exon靠后的exon<br>
	 * 0---1------------------2----3 <br>
	 * ----|---------------------| <br>
	 * @param junctionUnit
	 * @param gffGeneIsoInfo
	 * @return
	 */
	private boolean isEdgeInExon(boolean beforExon, JunctionUnit junctionUnit, JunctionUnit junBeforeAfter, GffGeneIsoInfo gffGeneIsoInfo) {
		int edgeLoc = 0, edgeLocBeforeAfter = 0;
		int exonLessNum = 10;//exon最短10bp
		if (beforExon) {
			if (gffGeneIsoInfo.isCis5to3()) {
				edgeLoc = junctionUnit.getStartAbs();
				edgeLocBeforeAfter = junBeforeAfter.getEndAbs();
			} else {
				edgeLoc = junctionUnit.getEndAbs();
				edgeLocBeforeAfter = junBeforeAfter.getStartAbs();
			}
			int edgNum = gffGeneIsoInfo.getNumCodInEle(edgeLoc);
			int edgBeforeAfterNum = gffGeneIsoInfo.getNumCodInEle(edgeLocBeforeAfter);
			return (gffGeneIsoInfo.getCodLoc(edgeLoc) == GffGeneIsoInfo.COD_LOC_EXON 
					&& gffGeneIsoInfo.getCod2ExInStart(edgeLoc) > exonLessNum)
					|| (edgNum != 0 && edgBeforeAfterNum !=0 && edgNum != edgBeforeAfterNum);
		} else {
			if (gffGeneIsoInfo.isCis5to3()) {
				edgeLoc = junctionUnit.getEndAbs();
				edgeLocBeforeAfter = junBeforeAfter.getStartAbs();
			} else {
				edgeLoc = junctionUnit.getStartAbs();
				edgeLocBeforeAfter = junBeforeAfter.getEndAbs();
			}
			int edgNum = gffGeneIsoInfo.getNumCodInEle(edgeLoc);
			int edgBeforeAfterNum = gffGeneIsoInfo.getNumCodInEle(edgeLocBeforeAfter);
			return (gffGeneIsoInfo.getCodLoc(edgeLoc) == GffGeneIsoInfo.COD_LOC_EXON
					&& gffGeneIsoInfo.getCod2ExInEnd(edgeLoc) > exonLessNum)
					|| (edgNum != 0 && edgBeforeAfterNum !=0 && edgNum != edgBeforeAfterNum);
		}
	}
	
	/**
	 * 指定的junction是否在exon中
	 * @param beforExon 是靠前的exon靠后的exon<br>
	 * 0---1------------------2----3 <br>
	 * ----|---------------------| <br>
	 * @param junctionUnit
	 * @param gffGeneIsoInfo
	 * @return
	 */
	private boolean isEdgeInExon(boolean beforExon, JunctionUnit junctionUnit, GffGeneIsoInfo gffGeneIsoInfo) {
		int edgeLoc = 0;
		int exonLessNum = 10;//exon最短10bp
		if (beforExon) {
			if (gffGeneIsoInfo.isCis5to3()) {
				edgeLoc = junctionUnit.getStartAbs();
			} else {
				edgeLoc = junctionUnit.getEndAbs();
			}
			return gffGeneIsoInfo.getCodLoc(edgeLoc) == GffGeneIsoInfo.COD_LOC_EXON
					&& gffGeneIsoInfo.getCod2ExInStart(edgeLoc) > exonLessNum;
		} else {
			if (gffGeneIsoInfo.isCis5to3()) {
				edgeLoc = junctionUnit.getEndAbs();
			} else {
				edgeLoc = junctionUnit.getStartAbs();
			}
			return gffGeneIsoInfo.getCodLoc(edgeLoc) == GffGeneIsoInfo.COD_LOC_EXON
					&& gffGeneIsoInfo.getCod2ExInEnd(edgeLoc) > exonLessNum;
		}
	}
	
	/** 选择前一个Junction Site */
	private List<JunctionUnit> getJunPrev(JunctionUnit junctionUnit) {
		List<JunctionUnit> lsJunctionUnits= junctionUnit.getLsJunBeforeAbs();
		//TODO 如果前面没有jun，是否要到tophatJunctionNew中去查找Jun
//		if (lsJunctionUnits.size() == 0) {
//			int start = gffDetailGene.getStartAbs();
//			ListCodAbsDu<JunctionInfo,ListCodAbs<JunctionInfo>> lsCodDuAbs = tophatJunctionNew.searchLocation(
//					junctionUnit.getRefID(), start, junctionUnit.getStartAbs());			
//			List<JunctionInfo> lsJunctionInfos = lsCodDuAbs.getAllGffDetail();
//			JunctionUnit junctionUnitPrev = null;
//			int lastEnd = 0;
//			for (JunctionInfo junctionInfo : lsJunctionInfos) {
//				for (JunctionUnit junction : junctionInfo.lsJunctionUnits) {
//					if (junction.getEndAbs() < junctionUnit.getStartAbs() && junction.getEndAbs() > lastEnd ) {
//						lastEnd = junction.getEndAbs();
//						junctionUnitPrev = junction;
//					}
//				}
//			}
//			if (junctionUnitPrev != null) {
//				lsJunctionUnits.add(junctionUnitPrev);
//			}
//		}
		return lsJunctionUnits;
	}
	
	/** 选择后一个Junction Site */
	private List<JunctionUnit> getJunAfter(JunctionUnit junctionUnit) {
		List<JunctionUnit> lsJunctionUnits= junctionUnit.getLsJunAfterAbs();
		//TODO 如果后面没有jun，是否要到tophatJunctionNew中去查找Jun
//		if (lsJunctionUnits.size() == 0) {
//			int end = gffDetailGene.getEndAbs();
//			ListCodAbsDu<JunctionInfo,ListCodAbs<JunctionInfo>> lsCodDuAbs = tophatJunctionNew.searchLocation(
//					junctionUnit.getRefID(), junctionUnit.getEndAbs(), end);
//			List<JunctionInfo> lsJunctionInfos = lsCodDuAbs.getAllGffDetail();
//			JunctionUnit junctionUnitNext = null;
//			int nextStart = 2000000000;
//			for (JunctionInfo junctionInfo : lsJunctionInfos) {
//				for (JunctionUnit junction : junctionInfo.lsJunctionUnits) {
//					if (junction.getStartAbs() > junctionUnit.getEndAbs() && junction.getStartAbs() > nextStart ) {
//						nextStart = junction.getStartAbs();
//						junctionUnitNext = junction;
//					}
//				}
//			}
//			if (junctionUnitNext != null) {
//				lsJunctionUnits.add(junctionUnitNext);
//			}
//		}
		return lsJunctionUnits;
	}
	
}
