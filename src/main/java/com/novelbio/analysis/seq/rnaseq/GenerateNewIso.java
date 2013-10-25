package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.rnaseq.JunctionInfo.JunctionUnit;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;

/** 根据junction reads，产生新的iso
 * 暂时没有考虑链特异性测序
 *  */
public class GenerateNewIso {
	private static final Logger logger = Logger.getLogger(GenerateNewIso.class);
	int newIsoReadsNum = 5;//至少有8条reads支持的junction才会用于重建转录本
	int blankNum = 30;//至少超过50bp的没有reads堆叠的区域，才被认为是intron
	int longExon = 200;//超过100bp就认为是比较长的exon，就需要做判定了
	int maxExonLen = 1000;
	int catchNum = 50000;
	TophatJunction tophatJunctionNew;

	GffHashGene gffHashGene;
	boolean considerStrand = false;
	GffDetailGene gffDetailGene;
	MapReads mapReads;
	Map<String, Boolean> mapLoc2IsCovered = new HashMap<>();
	
	public GenerateNewIso(TophatJunction tophatJunctionNew, MapReads mapReads, StrandSpecific considerStrand) {
		this.tophatJunctionNew = tophatJunctionNew;
		this.considerStrand = (considerStrand != StrandSpecific.NONE);
		this.mapReads = mapReads;
	}
	
	public void setGffHash(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	
	public void setGffDetailGene(GffDetailGene gffDetailGene) {
		this.gffDetailGene = gffDetailGene;
	}
	
	public void reconstructGffDetailGene() {
		List<JunctionUnit> lsJunUnit = getLsJunUnit(gffDetailGene);
		Set<String> setJunInfoLast = getJunctionInfo(lsJunUnit);
		//循环直至找不到新的junction reads
		for (;;) {
			for (JunctionUnit junctionUnit : lsJunUnit) {
				if (junctionUnit.getReadsNumAll() >= newIsoReadsNum && !isJunInGene(junctionUnit)) {
//					logger.debug(junctionUnit.toString());
					reconstructIso(junctionUnit);
				}
			}
			lsJunUnit = getLsJunUnit(gffDetailGene);
			Set<String> setJunInfo = getJunctionInfo(lsJunUnit);
			if (setJunInfo.equals(setJunInfoLast)) {
				break;
			}
			setJunInfoLast = setJunInfo;
		}

		//再反着来
//		for (int i = lsJunUnit.size() - 1; i >= 0; i--) {
//			JunctionUnit junctionUnit = lsJunUnit.get(i);
//			if (junctionUnit.getReadsNumAll() >= newIsoReadsNum && !isJunInGene(junctionUnit)) {
//				reconstructIso(junctionUnit);
//			}
//		}
		//最后可以构建出比较长的iso
	}
	
	/** 获得与该基因相关的全体JunctionUnit */
	private List<JunctionUnit> getLsJunUnit(GffDetailGene gffDetailGene) {
		int extendBp = 100;//意思左右两端延长然后获取junction
		String chrID = gffDetailGene.getRefID();
		int start = gffDetailGene.getStartAbs(), end = gffDetailGene.getEndAbs();
		//TODO 需要获得尽可能多的junction
		ListCodAbsDu<JunctionInfo, ListCodAbs<JunctionInfo>>  lsJunDu = tophatJunctionNew.searchLocation(chrID, start-extendBp, end+extendBp);
		if (lsJunDu == null) {
//			logger.error("could not find junctions in this region:" + chrID + " " + start + " " + end);
			return new ArrayList<>();
		}
		List<JunctionUnit> lsJunUnit = new ArrayList<>();
		for (JunctionInfo junctionInfo : lsJunDu.getAllGffDetail()) {
			for (JunctionUnit junctionUnit : junctionInfo.lsJunctionUnits) {
				if (junctionUnit.getStartAbs() <= gffDetailGene.getStartAbs() && junctionUnit.getEndAbs() >= gffDetailGene.getEndAbs()
						|| 
						(
						  (junctionUnit.getEndAbs() - junctionUnit.getStartAbs() > gffDetailGene.getLength() &&  junctionInfo.lsJunctionUnits.size() >= 5)
						   ||  junctionUnit.getEndAbs() - junctionUnit.getStartAbs() > 4 * gffDetailGene.getLength() )
						) {
					continue;
				}
				if(isJunctionCoverTwoGene(gffDetailGene, junctionUnit)) {
					continue;
				}
				lsJunUnit.add(junctionUnit);
			}
		}
		Collections.sort(lsJunUnit, new Comparator<JunctionUnit>() {
			public int compare(JunctionUnit o1, JunctionUnit o2) {
				Integer into1 = o1.getStartAbs();
				Integer into2 = o2.getStartAbs();
				return into1.compareTo(into2);
			}
		});
		
		return lsJunUnit;
	}
	
	/** 获得junction的信息，用来去重复的 */
	private Set<String> getJunctionInfo(List<JunctionUnit> lsJunctionUnits) {
		Set<String> setJunInfo = new HashSet<>();
		for (JunctionUnit junctionUnit : lsJunctionUnits) {
			setJunInfo.add(junctionUnit.getRefID() + SepSign.SEP_ID + junctionUnit.getStartAbs() + SepSign.SEP_ID + junctionUnit.getEndAbs());
		}
		return setJunInfo;
	}
	
	/**
	 * 该junction是否cover了超过两个基因，这种情况下，该junction就要舍弃不能用了
	 * @param junctionUnit
	 * @return
	 */
	//TODO 没有考虑链特异性
	private boolean isJunctionCoverTwoGene(GffDetailGene gffDetailGene, JunctionUnit junctionUnit) {
		int isoExtend = 600;
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(junctionUnit.getRefID(), junctionUnit.getStartAbs(), junctionUnit.getEndAbs());
		if (gffCodGeneDU.getLsGffDetailMid().size() > 0) return true;
		//TODO
		GffCodGene gffCodGeneStart = gffCodGeneDU.getGffCod1();
		GffCodGene gffCodGeneEnd = gffCodGeneDU.getGffCod2();
		if (!gffCodGeneStart.isInsideLoc() || !gffCodGeneEnd.isInsideLoc()) {
			return false;
		}
		
		Set<GffDetailGene> setGeneUp = gffCodGeneStart.getSetGeneCodIn();
		Set<GffDetailGene> setGeneDown = gffCodGeneEnd.getSetGeneCodIn();
		boolean isInUp = false, isInDown = false;
		for (GffDetailGene gffDetailGeneUpcod : setGeneUp) {
			if (gffDetailGeneUpcod.equals(gffDetailGene)) {
				isInUp = true;
				break;
			}
		}
		for (GffDetailGene gffDetailGeneDowncod : setGeneDown) {
			if (gffDetailGeneDowncod.equals(gffDetailGene)) {
				isInDown = true;
				break;
			}
		}
		if (isInUp && isInDown) {
			return false;	
		}
		if (gffCodGeneStart.isInsideLoc() && !isInUp && gffDetailGene.getStartAbs() - junctionUnit.getStartAbs() > isoExtend) {
			return true;
		}
		if (gffCodGeneEnd.isInsideLoc() && !isInDown && junctionUnit.getEndAbs() - gffDetailGene.getEndAbs() > isoExtend) {
			return true;
		}
		return false;
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
			
			int exonNumStart = gffGeneIsoInfo.getNumCodInEle(start);
			int exonNumEnd = gffGeneIsoInfo.getNumCodInEle(end);
			if (exonNumStart <= 0 || exonNumEnd <= 0) {
				continue;
			}
			if (gffGeneIsoInfo.getCod2ExInEnd(start) !=0 || gffGeneIsoInfo.getCod2ExInStart(end) != 0) {
				continue;
			}
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
		int startEndExonLen = 80;//如果是AltStart或者AltEnd，直接设定该exon长度为startEndExonLen的长度;
		int extendTssAndTes = 500;
		if (junctionUnit.getEndAbs() < gffGeneIsoInfo.getStartAbs() - extendTssAndTes || junctionUnit.getStartAbs() > gffGeneIsoInfo.getEndAbs() + extendTssAndTes) {
			return null;
		}
		if (considerStrand && junctionUnit.isCis5to3() != gffGeneIsoInfo.isCis5to3()) {
			return null;
		}
		
		int exonStart = 0, exonEnd = 0;
		List<JunctionUnit> lsJun = new ArrayList<>();
		Set<String> setJunInfo = new HashSet<>();
		if (isEdgeInExon(true, junctionUnit, gffGeneIsoInfo) || isEdgeInExon(false, junctionUnit, gffGeneIsoInfo)) {
			lsJun.add(junctionUnit);
			setJunInfo.add(junctionUnit.key(false));
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
		if (exonStart < 0 || exonEnd < 0 || (exonStart == 0 && exonEnd == 0)) {
//			logger.error("could not reconstruct iso:" + gffGeneIsoInfo.getName());
			return null;
		}
		
		/////////////   根据指定的startexon和endexon，还有listJun的信息，新建转录本 /////////////////////////////////////////
		GffGeneIsoInfo gffGeneIsoInfoNew = gffGeneIsoInfo.clone();
		gffGeneIsoInfoNew.clear();
		JunctionUnit junStart = lsJun.get(0);
		//如果是altstart和altend，现在设定边界exon的长度
		int start_endEdge = junStart.getStartAbs(), start_startEdge = start_endEdge - startEndExonLen;
		if (!gffGeneIsoInfo.isCis5to3()) {
			start_endEdge = junStart.getEndAbs(); start_startEdge = start_endEdge + startEndExonLen;
		}
		
		if (exonStart > 0) {
			for (int i = 0; i < exonStart; i++) {
				gffGeneIsoInfoNew.add(gffGeneIsoInfo.get(i).clone());
			}
			gffGeneIsoInfoNew.get(gffGeneIsoInfoNew.size() - 1).setEndCis(start_endEdge);
		} else {
			gffGeneIsoInfoNew.add(new ExonInfo( gffGeneIsoInfoNew.isCis5to3(), start_startEdge, start_endEdge));
		}

		for (int i = 1; i < lsJun.size(); i++) {
			ExonInfo exonInfo = new ExonInfo(gffGeneIsoInfoNew.isCis5to3(), 0, 0);
			if (gffGeneIsoInfoNew.isCis5to3()) {
				exonInfo.setStartAbs(lsJun.get(i-1).getEndAbs());
				exonInfo.setEndAbs(lsJun.get(i).getStartAbs());
			} else {
				exonInfo.setStartCis(lsJun.get(i-1).getStartAbs());
				exonInfo.setEndCis(lsJun.get(i).getEndAbs());
			}
			gffGeneIsoInfoNew.add(exonInfo);
		}
		
		JunctionUnit junEnd = lsJun.get(lsJun.size() - 1);
		int end_startEdge = junEnd.getEndAbs(), end_endEdge = end_startEdge + startEndExonLen;
		if (!gffGeneIsoInfo.isCis5to3()) {
			end_startEdge = junEnd.getStartAbs(); end_endEdge = end_startEdge - startEndExonLen;
		}
		
		if (exonEnd > 0) {
			ExonInfo exonInfo = gffGeneIsoInfo.get(exonEnd - 1).clone();
			exonInfo.setStartCis(end_startEdge);
			gffGeneIsoInfoNew.add(exonInfo);
			for (int i = exonEnd; i < gffGeneIsoInfo.size(); i++) {
				gffGeneIsoInfoNew.add(gffGeneIsoInfo.get(i).clone());
			}
		} else {
			gffGeneIsoInfoNew.add(new ExonInfo(gffGeneIsoInfoNew.isCis5to3(), end_startEdge, end_endEdge));
		}
		
		
		for (ExonInfo exonInfoNew : gffGeneIsoInfoNew) {
			exonInfoNew.setParentListAbs(gffGeneIsoInfoNew);
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
			List<JunctionUnit> lsJunPrevAfter = getJunPrevAfter(beforExon, gffGeneIsoInfo.isCis5to3(), junThis);
			if (lsJunPrevAfter.size() == 0) {
				exonNum = getExonNum(beforExon, false, gffGeneIsoInfo, junThis, lsJun, setJunInfo);
				break;
			}

			for (JunctionUnit junPrevTmp : lsJunPrevAfter) {
				if (beforExon) {
					if (!setJunInfo.contains(junThis.key(false))) {
						lsJun.add(0, junThis);
						setJunInfo.add(junThis.key(false));
					}
				} else {
					if (!setJunInfo.contains(junThis.key(false))) {
						lsJun.add(junThis);
						setJunInfo.add(junThis.key(false));
					}
				}
				if (isEdgeInExon(beforExon,junThis, junPrevTmp, gffGeneIsoInfo)) {
					exonNum = getExonNum(beforExon, true, gffGeneIsoInfo, junThis, lsJun, setJunInfo);
					search = false;
					break;
				}
			}
			junThis = lsJunPrevAfter.get(0);
		}
		return exonNum;
	}
	
	/** 返回0表示超出了范围 */
	private int getExonNum(boolean beforExon, boolean isHavePrevJun, GffGeneIsoInfo gffGeneIsoInfo, JunctionUnit junThis, 
			List<JunctionUnit> lsJun, Set<String> setJunInfo) {
		int exonNumReal = 0, exonNum = 0;
		int junEdge = 0;
		if (beforExon) {
			if (gffGeneIsoInfo.isCis5to3()) {
				junEdge = junThis.getStartAbs();
				exonNumReal = gffGeneIsoInfo.getNumCodInEle(junEdge);
			} else {
				junEdge = junThis.getEndAbs();
				exonNumReal = gffGeneIsoInfo.getNumCodInEle(junEdge);
			}
			exonNum = Math.abs(exonNumReal);
			if (!setJunInfo.contains(junThis.key(false))) {
				lsJun.add(0, junThis);
				setJunInfo.add(junThis.key(false));
			}
		} else {
			if (gffGeneIsoInfo.isCis5to3()) {
				junEdge = junThis.getEndAbs();
				exonNumReal = gffGeneIsoInfo.getNumCodInEle(junEdge);
			} else {
				junEdge = junThis.getStartAbs();
				exonNumReal = gffGeneIsoInfo.getNumCodInEle(junEdge);
			}
			exonNum = exonNumReal;
			if (exonNumReal < 0) {
				exonNum = Math.abs(exonNumReal) + 1;
			}
			if (!setJunInfo.contains(junThis.key(false))) {
				lsJun.add(junThis);
				setJunInfo.add(junThis.key(false));
			}
		}
		
		if (!isHavePrevJun && exonNum > 0 && exonNumReal < 0) {
			if (beforExon && Math.abs(gffGeneIsoInfo.get(exonNum-1).getEndCis() - junEdge) >= longExon) {
				if (isContinuousExon(gffGeneIsoInfo.getRefID(), gffGeneIsoInfo.get(exonNum-1).getEndCis(), junEdge)) {
					return exonNum;
				}
			} else if (!beforExon && Math.abs(gffGeneIsoInfo.get(exonNum-1).getStartCis() - junEdge) >= longExon) {
				if (isContinuousExon(gffGeneIsoInfo.getRefID(), gffGeneIsoInfo.get(exonNum-1).getStartCis(), junEdge)) {
					return exonNum;
				}
			}
			return 0;
		}
		return exonNum;
	}
	
	/** 是否为连续的exon，意思中间不得有超过numBlankBp的空余
	 * @param chrID
	 * @param startLoc 会根据大小自动设定起点和终点
	 * @param endLoc 会根据大小自动设定起点和终点
	 * @return
	 */
	private boolean isContinuousExon(String chrID, int startLoc, int endLoc) {
		int start = Math.min(startLoc, endLoc), end = Math.max(startLoc, endLoc);
		String keySite = (chrID+SepSign.SEP_ID+start + SepSign.SEP_ID+end).toLowerCase();
		if (mapLoc2IsCovered.containsKey(keySite)) {
			return mapLoc2IsCovered.get(keySite);
		}
		double[] regionFinal = mapReads.getRangeInfo(chrID, start, end, 0);
		if (regionFinal == null) {
			mapLoc2IsCovered.put(keySite, false);
			return false;
		}
		int blankNumFinal = 0, blankNum = 0;
		
		for (double d : regionFinal) {
			if (d == 0) {
				blankNum++;
			} else if (d > 0) {
				if (blankNum > blankNumFinal) {
					blankNumFinal = blankNum;
				}
				blankNum = 0;
			}
		}
		if (blankNum > blankNumFinal) {
			blankNumFinal = blankNum;
		}
		blankNumFinal = blankNumFinal * mapReads.getBinNum();
		if (blankNumFinal >= this.blankNum) {
			mapLoc2IsCovered.put(keySite, false);
			return false;
		}
		mapLoc2IsCovered.put(keySite, true);
		return true;
	}
	
	
	private List<JunctionUnit> getJunPrevAfter(boolean prev, boolean cis5to3, JunctionUnit junctionUnit) {
		try {
			if ((prev && cis5to3) || (!prev && !cis5to3)) {
				List<JunctionUnit> lsJunPrev = getJunPrev(junctionUnit);
				if (lsJunPrev.size() > 0) {
					JunctionUnit junPrev = lsJunPrev.get(0);
					if (Math.abs(junPrev.getEndAbs() - junctionUnit.getStartAbs()) > longExon && !isContinuousExon(junPrev.getRefID(), junPrev.getEndAbs(), junctionUnit.getStartAbs())) {
						return new ArrayList<>();
					}
				}
				return lsJunPrev;
			} else {
				List<JunctionUnit> lsJunAfter = getJunAfter(junctionUnit);
				if (lsJunAfter.size() > 0) {
					JunctionUnit junAfter = lsJunAfter.get(0);
					if (Math.abs(junctionUnit.getEndAbs() - junAfter.getStartAbs()) > longExon && !isContinuousExon(junAfter.getRefID(), junctionUnit.getEndAbs(), junAfter.getStartAbs())) {
						return new ArrayList<>();
					}
				}
				return lsJunAfter;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		List<JunctionUnit> lsJunctionUnits = junctionUnit.getLsJunBeforeAbs();
		//TODO 如果前面没有jun，是否要到tophatJunctionNew中去查找Jun
		int start = getGeneStart(200);
		if (start > junctionUnit.getStartAbs()) {
			start = junctionUnit.getStartAbs() - maxExonLen;
		}
		if (lsJunctionUnits.size() == 0 && tophatJunctionNew != null) {
			ListCodAbsDu<JunctionInfo, ListCodAbs<JunctionInfo>> lsCodDuAbs = tophatJunctionNew.searchLocation(
					junctionUnit.getRefID(), start, junctionUnit.getStartAbs());			
			List<JunctionInfo> lsJunctionInfos = lsCodDuAbs.getAllGffDetail();
			JunctionUnit junctionUnitPrev = null;
			int lastEnd = 0;
			for (JunctionInfo junctionInfo : lsJunctionInfos) {
				for (JunctionUnit junction : junctionInfo.lsJunctionUnits) {
					if (considerStrand && junction.isCis5to3() != junctionUnit.isCis5to3()) continue;
					
					if (junction.getEndAbs() < junctionUnit.getStartAbs() && junction.getEndAbs() > lastEnd ) {
						lastEnd = junction.getEndAbs();
						junctionUnitPrev = junction;
					}
				}
			}
			if (junctionUnitPrev != null) {
				lsJunctionUnits.add(junctionUnitPrev);
			}
		}
		return lsJunctionUnits;
	}
	
	/** 获得该基因的起点，不考虑方向
	 * 如果该基因前面没有基因，则向前延展extend bp长度
	 *  */
	private int getGeneStart(int extend) {
		int start = gffDetailGene.getStartAbs();
		if (gffHashGene == null) {
			return start;
		}
		GffCodGene gffCodGene = gffHashGene.searchLocation(gffDetailGene.getRefID(), start);
		if (gffCodGene.isInsideUp() || (gffCodGene.isInsideLoc() && gffCodGene.isInsideDown())) {
			return start;
		}
		GffDetailGene gffDetailGeneLast = gffCodGene.getGffDetailUp();
		if (gffDetailGeneLast == null) {
			start = start - 200;
		} else if (start > (gffDetailGeneLast.getEndAbs() + extend + 100)) {
			start = start - extend;
		}
		return start;
	}
	
	/** 选择后一个Junction Site，返回只有一个元素的list */
	private List<JunctionUnit> getJunAfter(JunctionUnit junctionUnit) {
		List<JunctionUnit> lsJunctionUnits= junctionUnit.getLsJunAfterAbs();
		//TODO 如果后面没有jun，是否要到tophatJunctionNew中去查找Jun
		int end = getGeneEnd(200);
		if (end < junctionUnit.getEndAbs()) {
			end = junctionUnit.getEndAbs() + maxExonLen;
		}
		if (lsJunctionUnits.size() == 0 && tophatJunctionNew != null) {
			//////////////////////////
			ListCodAbsDu<JunctionInfo,ListCodAbs<JunctionInfo>> lsCodDuAbs = tophatJunctionNew.searchLocation(
					junctionUnit.getRefID(), junctionUnit.getEndAbs(), end);
			List<JunctionInfo> lsJunctionInfos = lsCodDuAbs.getAllGffDetail();
			JunctionUnit junctionUnitNext = null;
			int nextStart = Integer.MAX_VALUE;
			for (JunctionInfo junctionInfo : lsJunctionInfos) {
				for (JunctionUnit junction : junctionInfo.lsJunctionUnits) {
					if (considerStrand && junction.isCis5to3() != junctionUnit.isCis5to3()) continue;
					
					if (junction.getStartAbs() > junctionUnit.getEndAbs() && junction.getStartAbs() < nextStart ) {
						nextStart = junction.getStartAbs();
						junctionUnitNext = junction;
					}
				}
			}
			if (junctionUnitNext != null) {
				lsJunctionUnits.add(junctionUnitNext);
			}
		}
		return lsJunctionUnits;
	}
	
	/** 获得该基因的起点，不考虑方向
	 * 如果该基因前面没有基因，则向前延展extend bp长度
	 *  */
	private int getGeneEnd(int extend) {
		int end = gffDetailGene.getEndAbs();
		if (gffHashGene == null) {
			return end;
		}
		GffCodGene gffCodGene = gffHashGene.searchLocation(gffDetailGene.getRefID(), end);
		if (gffCodGene.isInsideDown() || (gffCodGene.isInsideLoc() && gffCodGene.isInsideUp())) {
			return end;
		}
		GffDetailGene gffDetailGeneNext = gffCodGene.getGffDetailDown();
		if (gffDetailGeneNext == null) {
			end = end + extend;
		} else if (end < (gffDetailGeneNext.getStartAbs() - extend - 100)) {
			end = end + extend;
		}
		return end;
	}
	
	public void clear() {
		gffDetailGene = null;
		gffHashGene = null;
		mapReads = null;
	}
}
