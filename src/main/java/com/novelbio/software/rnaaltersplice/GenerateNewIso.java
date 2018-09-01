package com.novelbio.software.rnaaltersplice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.SepSign;
import com.novelbio.bioinfo.base.binarysearch.BsearchSiteDu;
import com.novelbio.bioinfo.base.binarysearch.ListAbs;
import com.novelbio.bioinfo.base.binarysearch.ListCodAbs;
import com.novelbio.bioinfo.base.binarysearch.ListCodAbsDu;
import com.novelbio.bioinfo.base.binarysearch.ListDetailAbs.ListDetailAbsCompareStrand;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffCodGene;
import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.mappedreads.MapReads;
import com.novelbio.bioinfo.rnaseq.JunctionInfo;
import com.novelbio.bioinfo.rnaseq.TophatJunction;
import com.novelbio.bioinfo.rnaseq.JunctionInfo.JunctionUnit;
import com.novelbio.bioinfo.sam.StrandSpecific;

/** 根据junction reads，产生新的iso
 * 暂时没有考虑链特异性测序
*/
public class GenerateNewIso {
	private static final Logger logger = Logger.getLogger(GenerateNewIso.class);
	/** 至少有15条reads支持的junction才会用于重建转录本 */
	int newIsoReadsNum = 15;
	
	//判断一个区段是否为exon时用到的参数
	int blankNum = 30;///如果有超过30bp的区域没有reads堆叠，则被认为是intron
	double blankProp = 0.2; //如果有20%的区域没有reads堆叠，则被认为是intron
	int blankMinCoverage = 8;//至少要有8条reads覆盖，才认为该区域有reads堆叠
	/** 重建exon的时候，如果junction大于该长度，则不重建 */
	int maxRIlen = 4000;
	
	/** 是否重建RI位点 */
	boolean isReconstructRI = false;
	int longExon = 200;//超过100bp就认为是比较长的exon，就需要做判定了
	int maxExonLen = 1000;
	int catchNum = 50000;

	
	TophatJunction tophatJunctionNew;

	GffHashGene gffHashGene;
	boolean considerStrand = false;
	GffGene gffDetailGene;
	MapReads mapReads;
	Map<String, Boolean> mapLoc2IsCovered = new HashMap<>();
	
	public GenerateNewIso(TophatJunction tophatJunctionNew, MapReads mapReads, StrandSpecific considerStrand, boolean isReconstructRI) {
		this.tophatJunctionNew = tophatJunctionNew;
		this.considerStrand = (considerStrand != StrandSpecific.NONE);
		this.mapReads = mapReads;
		this.isReconstructRI = isReconstructRI;
	}

	/** 至少有多少条reads支持的junction才会用于重建转录本，默认15 */
	public void setNewIsoReadsNum(int newIsoReadsNum) {
		this.newIsoReadsNum = newIsoReadsNum;
	}
	
	public void setGffHash(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	
	public void setGffDetailGene(GffGene gffDetailGene) {
		this.gffDetailGene = gffDetailGene;
	}
	
	public void reconstructGffDetailGene() {
		if (!isNeedReconstruct()) return;
		
		List<JunctionUnit> lsJunUnit = getLsJunUnit(gffDetailGene);
		Set<String> setJunInfoLast = getJunctionInfo(lsJunUnit);
		//循环直至找不到新的junction reads
		int loopNum = 0;
		for (;;) {
			loopNum++;
			for (JunctionUnit junctionUnit : lsJunUnit) {
				logger.debug(loopNum + " " +junctionUnit.toString());
				if (junctionUnit.getReadsNumAll() >= newIsoReadsNum && !isJunInGene(junctionUnit)) {
					if (loopNum > 1 && isJunctionInAnotherGene(gffDetailGene, junctionUnit) && gffDetailGene.getLsCodSplit().size() > 100) {
						continue;
					}
//					logger.debug(junctionUnit.toString());
					reconstructIso(junctionUnit);
				}
			}
			lsJunUnit = getLsJunUnit(gffDetailGene);
			//去重复
			Set<String> setJunInfo = getJunctionInfo(lsJunUnit);
			//如果找到的junction数量是最长转录本exon的10倍，一般来说这里的junction事件会非常复杂，
			//所以怀疑该处的剪接事件很混乱。为了保证准确性，不进行转录本重建的工作
			if (setJunInfo.size() > gffDetailGene.getLsCodSplit().size() * 10) {
				break;
			}
			
			if (setJunInfo.equals(setJunInfoLast)) {
				break;
			}
			setJunInfoLast = setJunInfo;
		}
		//TODO 这里连续的扩大junction的数量，所以会引入很多噪声
		//再反着来
//		for (int i = lsJunUnit.size() - 1; i >= 0; i--) {
//			JunctionUnit junctionUnit = lsJunUnit.get(i);
//			if (junctionUnit.getReadsNumAll() >= newIsoReadsNum && !isJunInGene(junctionUnit)) {
//				reconstructIso(junctionUnit);
//			}
//		}
		
		if (isReconstructRI) {
			reconstructRI();
		}
		//最后可以构建出比较长的iso
	}
	
	/** 是否需要重建转录本，有些单基因的就不要重建了要不然很乱 */
	private boolean isNeedReconstruct() {
		int onExonIsoNum = 0;
		for (GffIso iso : gffDetailGene.getLsCodSplit()) {
			if (iso.getLsElement().size() == 1) {
				onExonIsoNum++;
			}
		}
		if ((double)onExonIsoNum/gffDetailGene.getLsCodSplit().size() > 0.7) {
			return false;
		}
		return true;
	}
	
	
	/** 获得与该基因相关的全体JunctionUnit */
	private List<JunctionUnit> getLsJunUnit(GffGene gffDetailGene) {
		int extendBp = 100;//意思左右两端延长然后获取junction
		String chrID = gffDetailGene.getRefID();
		int start = gffDetailGene.getStartAbs(), end = gffDetailGene.getEndAbs();
		//TODO 需要获得尽可能多的junction
		BsearchSiteDu<JunctionInfo>  lsJunDu = tophatJunctionNew.searchLocation(chrID, start-extendBp, end+extendBp);
		if (lsJunDu == null) {
//			logger.error("could not find junctions in this region:" + chrID + " " + start + " " + end);
			return new ArrayList<>();
		}
		List<JunctionUnit> lsJunUnit = new ArrayList<>();
		for (JunctionInfo junctionInfo : lsJunDu.getAllElement()) {
			for (JunctionUnit junctionUnit : junctionInfo.getLsJunctionUnits()) {				
				if (junctionUnit.getStartAbs() <= gffDetailGene.getStartAbs() && junctionUnit.getEndAbs() >= gffDetailGene.getEndAbs()
						|| 
						 junctionUnit.getLength() > 0.8 * gffDetailGene.getLength()) {
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
			setJunInfo.add(getKey(junctionUnit));
		}
		return setJunInfo;
	}
	
	private String getKey(JunctionUnit junctionUnit) {
		return junctionUnit.getRefID() + SepSign.SEP_ID + junctionUnit.getStartAbs() + SepSign.SEP_ID + junctionUnit.getEndAbs();
	}
	
	/**
	 * 该junction是否cover了超过两个基因，这种情况下，该junction就要舍弃不能用了
	 * @param junctionUnit
	 * @return
	 */
	//TODO 没有考虑链特异性
	private boolean isJunctionCoverTwoGene(GffGene gffDetailGene, JunctionUnit junctionUnit) {
		int isoExtend = 600;
		int startJun = junctionUnit.getStartAbs(), endJun = junctionUnit.getEndAbs();
		int juncMid = (startJun + endJun)/2, juncGene = (gffDetailGene.getStartAbs() + gffDetailGene.getEndAbs())/2;
		int start = startJun, end = endJun;

		if (juncMid > juncGene) {
			if (startJun > gffDetailGene.getEndAbs()) {
				start = gffDetailGene.getEndAbs();
			}
		} else {
			if (endJun < gffDetailGene.getStartAbs()) {
				end = gffDetailGene.getStartAbs();
			}
		}
		
		
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(junctionUnit.getRefID(), start, end);
		if (gffCodGeneDU.getLsGffDetailMid().size() > 0) return true;
		//TODO
		GffCodGene gffCodGeneStart = gffCodGeneDU.getGffCod1();
		GffCodGene gffCodGeneEnd = gffCodGeneDU.getGffCod2();
		if (!gffCodGeneStart.isInsideLoc() || !gffCodGeneEnd.isInsideLoc()) {
			return false;
		}
		
		Set<GffGene> setGeneUp = gffCodGeneStart.getSetGeneCodIn();
		Set<GffGene> setGeneDown = gffCodGeneEnd.getSetGeneCodIn();
		boolean isInUp = false, isInDown = false;
		for (GffGene gffDetailGeneUpcod : setGeneUp) {
			if (gffDetailGeneUpcod.equals(gffDetailGene)) {
				isInUp = true;
				break;
			}
		}
		for (GffGene gffDetailGeneDowncod : setGeneDown) {
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
	
	private boolean isJunctionInAnotherGene(GffGene gffDetailGene, JunctionUnit junctionUnit) {
		if (considerStrand && gffDetailGene.isCis5to3() != null && junctionUnit.isCis5to3() != gffDetailGene.isCis5to3()) {
			return false;
		}
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(junctionUnit.getRefID(), junctionUnit.getStartAbs(), junctionUnit.getEndAbs());
		if (gffCodGeneDU.getLsGffDetailMid().size() > 0) return true;
		//TODO
		GffCodGene gffCodGeneStart = gffCodGeneDU.getGffCod1();
		GffCodGene gffCodGeneEnd = gffCodGeneDU.getGffCod2();
		
		Set<GffGene> setGeneUp = gffCodGeneStart.getSetGeneCodIn();
		Set<GffGene> setGeneDown = gffCodGeneEnd.getSetGeneCodIn();
		boolean isUpInOtherGene = false, isDownInOtherGene = false;
		for (GffGene gffDetailGeneUpcod : setGeneUp) {
			if (!gffDetailGeneUpcod.equals(gffDetailGene)) {
				isUpInOtherGene = true;
				break;
			}
		}
		for (GffGene gffDetailGeneDowncod : setGeneDown) {
			if (!gffDetailGeneDowncod.equals(gffDetailGene)) {
				isDownInOtherGene = true;
				break;
			}
		}
		if (isUpInOtherGene || isDownInOtherGene) {
			return true;	
		}
		return false;
	}
	
	//TODO 
	private boolean isJunInGene(JunctionUnit junctionUnit) {
		boolean findJun = false;
		int start, end = 0;
		
		if (considerStrand && gffDetailGene.isCis5to3() != null && gffDetailGene.isCis5to3() != junctionUnit.isCis5to3()) {
			return false;
		}
		
		for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
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
				logger.error("出错" + gffDetailGene.getNameSingle() + " " + junctionUnit.getStartAbs() + " " + junctionUnit.getEndAbs());
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
		 for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			GffIso gffGeneIsoInfoNew = getReconstructIso(junctionUnit, gffGeneIsoInfo);
			if (gffGeneIsoInfoNew != null ) {
				gffDetailGene.addIso(gffGeneIsoInfoNew);
				break;
			}
		}
	}
	
	/** 建立新的转录本 */
	//TODO 似乎只对cis有效果
	private GffIso getReconstructIso(JunctionUnit junctionUnit, GffIso gffGeneIsoInfo) {
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
		boolean isEdgeInExonBefore = isEdgeInExon(true, junctionUnit, gffGeneIsoInfo);
		boolean isEdgeInExonAfter = isEdgeInExon(false, junctionUnit, gffGeneIsoInfo);
		if (isEdgeInExonBefore || isEdgeInExonAfter) {
			lsJun.add(junctionUnit);
			setJunInfo.add(junctionUnit.key(false));
		}
		if (isEdgeInExonBefore) {
			if (gffGeneIsoInfo.isCis5to3()) {
				exonStart = gffGeneIsoInfo.getNumCodInEle(junctionUnit.getStartAbs());
			} else {
				exonStart = gffGeneIsoInfo.getNumCodInEle(junctionUnit.getEndAbs());
			}
		} else {
			exonStart = getExonSiteAndAddLsJun(true, junctionUnit, lsJun, setJunInfo, gffGeneIsoInfo);
		}
	
		if (isEdgeInExonAfter) {
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
		GffIso gffGeneIsoInfoNew = gffGeneIsoInfo.clone();
		gffGeneIsoInfoNew.clearElements();
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
		
		return gffGeneIsoInfoNew;
	}
	
	/**
	 * 根据GffGeneIsoInfo，在Junction的前后找，直到找到处于exon中的Jun，返回该Jun的位置，并将jun依次装入list中
	 * @param beforExon 找前面的还是后面的exon
	 * @param junThis
	 * @param lsJun
	 * @param gffGeneIsoInfo
	 * @return 如果输入的iso不满足则返回-100
	 */
	private int getExonSiteAndAddLsJun(boolean beforExon, JunctionUnit junThis, List<JunctionUnit> lsJun, 
			Set<String> setJunInfo, GffIso gffGeneIsoInfo) {
		int exonNum = -100;
		boolean search = true;
		while (search) {
			JunctionUnit juncPrevAfter = getJunPrevAfter(beforExon, gffGeneIsoInfo.isCis5to3(), junThis);
			if (juncPrevAfter == null) {
				exonNum = getExonNum(beforExon, false, gffGeneIsoInfo, junThis, lsJun, setJunInfo);
				break;
			}
			
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
			if (isEdgeInExon(beforExon,junThis, juncPrevAfter, gffGeneIsoInfo)) {
				exonNum = getExonNum(beforExon, true, gffGeneIsoInfo, junThis, lsJun, setJunInfo);
				search = false;
			}
			junThis = juncPrevAfter;
		}
		return exonNum;
	}
	
	/** 返回0表示超出了范围 */
	private int getExonNum(boolean beforExon, boolean isHavePrevJun, GffIso gffGeneIsoInfo, JunctionUnit junThis, 
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
				if (isContinuousExon(gffGeneIsoInfo.getRefIDlowcase(), gffGeneIsoInfo.get(exonNum-1).getEndCis(), junEdge)) {
					return exonNum;
				}
			} else if (!beforExon && Math.abs(gffGeneIsoInfo.get(exonNum-1).getStartCis() - junEdge) >= longExon) {
				if (isContinuousExon(gffGeneIsoInfo.getRefIDlowcase(), gffGeneIsoInfo.get(exonNum-1).getStartCis(), junEdge)) {
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
		double[] regionFinal = null;
		if (mapReads != null) {
			regionFinal = mapReads.getRangeInfo(chrID, start, end, 0);
		}

		if (regionFinal == null) {
			mapLoc2IsCovered.put(keySite, false);
			return false;
		}
		int blankNumFinal = 0, blankNum = 0;
		
		for (double d : regionFinal) {
			if (d < blankMinCoverage) {
				blankNum++;
			} else if (d >= blankMinCoverage) {
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
		if (blankNumFinal >= this.blankNum || blankNumFinal > Math.abs(end - start) * blankProp) {
			mapLoc2IsCovered.put(keySite, false);
			return false;
		}
		mapLoc2IsCovered.put(keySite, true);
		return true;
	}

	/**
	 * 返回null表示没有找到前面的junction 
	 * @param prev
	 * @param cis5to3
	 * @param junctionUnit
	 * @return
	 */
	private JunctionUnit getJunPrevAfter(boolean prev, boolean cis5to3, JunctionUnit junctionUnit) {
		try {
			if ((prev && cis5to3) || (!prev && !cis5to3)) {
				JunctionUnit junPrev = getJunPrev(junctionUnit, cis5to3);
				if (junPrev != null) {
					if (Math.abs(junPrev.getEndAbs() - junctionUnit.getStartAbs()) > longExon && !isContinuousExon(junPrev.getRefID(), junPrev.getEndAbs(), junctionUnit.getStartAbs())) {
						return null;
					}
				}
				return junPrev;
			} else {
				JunctionUnit junAfter = getJunAfter(junctionUnit, cis5to3);
				if (junAfter != null) {
					if (Math.abs(junctionUnit.getEndAbs() - junAfter.getStartAbs()) > longExon && !isContinuousExon(junAfter.getRefID(), junctionUnit.getEndAbs(), junAfter.getStartAbs())) {
						return null;
					}
				}
				return junAfter;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
	private boolean isEdgeInExon(boolean beforExon, JunctionUnit junctionUnit, JunctionUnit junBeforeAfter, GffIso gffGeneIsoInfo) {
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
			return (gffGeneIsoInfo.getCodLoc(edgeLoc) == GffIso.COD_LOC_EXON 
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
			return (gffGeneIsoInfo.getCodLoc(edgeLoc) == GffIso.COD_LOC_EXON
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
	private boolean isEdgeInExon(boolean beforExon, JunctionUnit junctionUnit, GffIso gffGeneIsoInfo) {
		int edgeLoc = 0;
		int exonLessNum = 10;//exon最短10bp。一些exon可能只有4-5bp的差別，譬如 100-200 和100-202。这种就不考虑了
		if (beforExon) {
			if (gffGeneIsoInfo.isCis5to3()) {
				edgeLoc = junctionUnit.getStartAbs();
			} else {
				edgeLoc = junctionUnit.getEndAbs();
			}
			return gffGeneIsoInfo.getCodLoc(edgeLoc) == GffIso.COD_LOC_EXON
					&& gffGeneIsoInfo.getCod2ExInStart(edgeLoc) > exonLessNum;
		} else {
			if (gffGeneIsoInfo.isCis5to3()) {
				edgeLoc = junctionUnit.getEndAbs();
			} else {
				edgeLoc = junctionUnit.getStartAbs();
			}
			return gffGeneIsoInfo.getCodLoc(edgeLoc) == GffIso.COD_LOC_EXON
					&& gffGeneIsoInfo.getCod2ExInEnd(edgeLoc) > exonLessNum;
		}
	}
	
	/** 选择前一个Junction Site */
	private JunctionUnit getJunPrev(JunctionUnit junctionUnit, final boolean isCis5To3) {
		List<JunctionUnit> lsJunctionUnits = junctionUnit.getLsJunBeforeAbs(tophatJunctionNew);
		if (lsJunctionUnits.size() > 1) {
			Collections.sort(lsJunctionUnits, new Comparator<JunctionUnit>() {
				public int compare(JunctionUnit o1, JunctionUnit o2) {
					Integer o1Start = o1.getStartAbs(), o1End = o1.getEndAbs();
					Integer o2Start = o2.getStartAbs(), o2End = o2.getEndAbs();					
					if (isCis5To3) {
						return -o1End.compareTo(o2End);
					} else {
						return o1Start.compareTo(o2Start);
					}
				}
			});
		}
		if (!lsJunctionUnits.isEmpty()) {
			return lsJunctionUnits.get(0);
		}
		
		//TODO 如果前面没有jun，是否要到tophatJunctionNew中去查找Jun
		int start = getGeneStart(200);
		if (start > junctionUnit.getStartAbs()) {
			start = junctionUnit.getStartAbs() - maxExonLen;
		}
		
		if (lsJunctionUnits.size() == 0 && tophatJunctionNew != null) {
			BsearchSiteDu<JunctionInfo> lsCodDuAbs = tophatJunctionNew.searchLocation(
					junctionUnit.getRefID(), start, junctionUnit.getStartAbs());			
			List<JunctionInfo> lsJunctionInfos = lsCodDuAbs.getAllElement();
			JunctionUnit junctionUnitPrev = null;
			int lastEnd = 0;
			for (JunctionInfo junctionInfo : lsJunctionInfos) {
				for (JunctionUnit junction : junctionInfo.getLsJunctionUnits()) {
					if (considerStrand && junction.isCis5to3() != junctionUnit.isCis5to3()) continue;
					if (junction.getReadsNumAll() < newIsoReadsNum) continue;
					
					if (junction.getEndAbs() < junctionUnit.getStartAbs() && junction.getEndAbs() > lastEnd ) {
						lastEnd = junction.getEndAbs();
						junctionUnitPrev = junction;
					}
				}
			}
			if (junctionUnitPrev != null && (isJunInGene(junctionUnitPrev) || !isJunctionInAnotherGene(gffDetailGene, junctionUnitPrev))) {
				lsJunctionUnits.add(junctionUnitPrev);
			}
			return junctionUnitPrev;
		}
		return null;
	}
	
	/** 获得该基因的起点，不考虑方向
	 * 如果该基因前面没有基因，则向前延展extend bp长度
	 */
	private int getGeneStart(int extend) {
		int start = gffDetailGene.getStartAbs();
		if (gffHashGene == null) {
			return start;
		}
		GffCodGene gffCodGene = gffHashGene.searchLocation(gffDetailGene.getRefID(), start);
		if (gffCodGene.isInsideUp() || (gffCodGene.isInsideLoc() && gffCodGene.isInsideDown())) {
			return start;
		}
		GffGene gffDetailGeneLast = gffCodGene.getGffDetailUp();
		if (gffDetailGeneLast == null) {
			start = start - 200;
		} else if (start > (gffDetailGeneLast.getEndAbs() + extend + 100)) {
			start = start - extend;
		}
		return start;
	}
	
	/** 选择后一个Junction Site，返回只有一个元素的list */
	private JunctionUnit getJunAfter(JunctionUnit junctionUnit, boolean isCis5To3) {
		List<JunctionUnit> lsJunctionUnits= junctionUnit.getLsJunAfterAbs(tophatJunctionNew);
		
		if (lsJunctionUnits.size() > 1) {
			Collections.sort(lsJunctionUnits, new Comparator<JunctionUnit>() {
				public int compare(JunctionUnit o1, JunctionUnit o2) {
					Integer o1Start = o1.getStartAbs(), o1End = o1.getEndAbs();
					Integer o2Start = o2.getStartAbs(), o2End = o2.getEndAbs();					
					if (isCis5To3) {
						return o1Start.compareTo(o2Start);
					} else {
						return -o1End.compareTo(o2End);
					}
				}
			});
		}
		if (!lsJunctionUnits.isEmpty()) {
			return lsJunctionUnits.get(0);
		}
		
		//TODO 如果后面没有jun，是否要到tophatJunctionNew中去查找Jun
		int end = getGeneEnd(200);
		if (end < junctionUnit.getEndAbs()) {
			end = junctionUnit.getEndAbs() + maxExonLen;
		}
		if (lsJunctionUnits.size() == 0 && tophatJunctionNew != null) {
			//////////////////////////
			BsearchSiteDu<JunctionInfo> lsCodDuAbs = tophatJunctionNew.searchLocation(
					junctionUnit.getRefID(), junctionUnit.getEndAbs(), end);
			List<JunctionInfo> lsJunctionInfos = lsCodDuAbs.getAllElement();
			JunctionUnit junctionUnitNext = null;
			int nextStart = Integer.MAX_VALUE;
			for (JunctionInfo junctionInfo : lsJunctionInfos) {
				for (JunctionUnit junction : junctionInfo.getLsJunctionUnits()) {
					if (considerStrand && junction.isCis5to3() != junctionUnit.isCis5to3()) continue;
					if (junction.getReadsNumAll() < newIsoReadsNum) continue;
					
					if (junction.getStartAbs() > junctionUnit.getEndAbs() && junction.getStartAbs() < nextStart ) {
						nextStart = junction.getStartAbs();
						junctionUnitNext = junction;
					}
				}
			}
			if (junctionUnitNext != null) {
				return junctionUnitNext;
			}
		}
		return null;
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
		GffGene gffDetailGeneNext = gffCodGene.getGffDetailDown();
		if (gffDetailGeneNext == null) {
			end = end + extend;
		} else if (end < (gffDetailGeneNext.getStartAbs() - extend - 100)) {
			end = end + extend;
		}
		return end;
	}
	
	/** 看每个剪接位点是否可以成为一个连在一起的exon，然后重建RI位点 */
	private void reconstructRI() {
		List<JunctionUnit> lsJuncUnit = null;
		try {
			lsJuncUnit = getLsJuncUnitNoOverlap();
		} catch (Exception e) {
			logger.error("reconstructRI error " + gffDetailGene.getNameSingle() + " " + gffDetailGene.getRefID()
					+ " " + gffDetailGene.getStartAbs() + " " + gffDetailGene.getEndAbs());
			lsJuncUnit = getLsJuncUnitNoOverlap();
			
		}
		
		List<JunctionUnit> lsNeedReconstruct = new ArrayList<>();
		for (JunctionUnit junctionUnit : lsJuncUnit) {
			if (junctionUnit.getLength() > maxRIlen) {
				continue;
			}
			
			if (isContinuousExon(junctionUnit.getRefID(), junctionUnit.getStartAbs(), junctionUnit.getEndAbs())) {
				lsNeedReconstruct.add(junctionUnit);
			}
		}
		Map<JunctionUnit, GffIso> mapJunc2IsoNeedReconstruct = getMapJunc2IsoNeedReconstruct(lsNeedReconstruct);
		
		List<GffIso> lsIsoNew = new ArrayList<>();
		for (JunctionUnit junctionUnit : mapJunc2IsoNeedReconstruct.keySet()) {
			GffIso iso = mapJunc2IsoNeedReconstruct.get(junctionUnit);
			lsIsoNew.add(reconstructIso(junctionUnit, iso));
		}
		
		for (GffIso isoNew : lsIsoNew) {
			gffDetailGene.addIsoSimple(isoNew);
		}
	}
	
	/** 去除overlap的junction，仅返回没有overlap的基因, for test */
	protected List<JunctionUnit> getLsJuncUnitNoOverlap() {
		Map<String, JunctionUnit> mapKey2Junc = new HashMap<>();
		for (GffIso iso : gffDetailGene.getLsCodSplit()) {
			for (int i = 0; i < iso.size() - 1; i++) {
				ExonInfo exonInfo1 = iso.get(i);
				ExonInfo exonInfo2 = iso.get(i+1);
				
				JunctionUnit junctionUnitRaw = new JunctionUnit(iso.getRefID(), exonInfo1.getEndCis(), exonInfo2.getStartCis());
				junctionUnitRaw.setConsiderStrand(considerStrand);
				JunctionUnit junctionUnit = new JunctionUnit(iso.getRefID(), junctionUnitRaw.getStartAbs() + 1, junctionUnitRaw.getEndAbs() - 1);
				junctionUnit.setConsiderStrand(considerStrand);
				junctionUnit.setCis5to3(iso.isCis5to3());
				mapKey2Junc.put(getKey(junctionUnit), junctionUnit);
			}
		}
		
		//把juncUnit按照位置归类，归类成类似下图这种，然后每个类别里面获取短的
		// ----------------|10==20-----|-----------|50==60-------|----------------------
		//-----------------|---15===30|-----------|---55====80|-----------------------
		List<JunctionUnit> lsJuncUnit = new ArrayList<>(mapKey2Junc.values());
		Collections.sort(lsJuncUnit, new ListDetailAbsCompareStrand());
		List<int[]> lsSub = ListAbs.getLsElementSep(gffDetailGene.isCis5to3(), lsJuncUnit);
		ArrayListMultimap<String, JunctionUnit> mapLoc2LsJunUnit = ArrayListMultimap.create();
		
		Iterator<int[]> itInt = lsSub.iterator();
		int[] sub = itInt.next();
		int i = 0;
		for (JunctionUnit junctionUnit : lsJuncUnit) {
			i++;
			if (junctionUnit.isCis5to3() && junctionUnit.getStartCis()  > sub[1] || (!junctionUnit.isCis5to3() && junctionUnit.getStartCis() < sub[0])) {
				sub = itInt.next();
			}
			if (junctionUnit.getStartAbs() >= sub[0] && junctionUnit.getEndAbs() <= sub[1]) {
				mapLoc2LsJunUnit.put(sub[0] + "_" + sub[1], junctionUnit);
			}
		}
		
		//获得没有overlap的junciton
		List<JunctionUnit> lsJuncUnitSmall = new ArrayList<>();
		for (String loc : mapLoc2LsJunUnit.keySet()) {
			List<JunctionUnit> lsResultTmp = new ArrayList<>();
			List<JunctionUnit> lsUnits = mapLoc2LsJunUnit.get(loc);
			Collections.sort(lsUnits, new Comparator<JunctionUnit>() {
				public int compare(JunctionUnit o1, JunctionUnit o2) {
					Integer o1Len = o1.getLength(), o2Len = o2.getLength();
					return o1Len.compareTo(o2Len);
				}
			});
			for (JunctionUnit unit : lsUnits) {
				boolean isOverlap = false;
				for (JunctionUnit unitExist : lsResultTmp) {
					if (unitExist.getStartAbs() < unit.getEndAbs() && unitExist.getEndAbs() > unit.getStartAbs()) {
						isOverlap = true;
						break;
					}
				}
				
				if (!isOverlap) {
					lsResultTmp.add(unit);
				}
			}
			
			lsJuncUnitSmall.addAll(lsResultTmp);
		}
		return lsJuncUnitSmall;
	}
	
	/**
	 *  根据junction和iso的关系，返回junction 与 需要重建的iso的对照表
	 * @param lsNeedReconstruct 给定可能需要重建的iso列表
	 * @return
	 */
	protected Map<JunctionUnit, GffIso> getMapJunc2IsoNeedReconstruct(	List<JunctionUnit> lsNeedReconstruct) {
		Map<JunctionUnit, GffIso> mapJunc2IsoNeedReconstruct = new HashMap<>();
		for (JunctionUnit junctionUnit : lsNeedReconstruct) {
			GffIso isoNeedReconstruct = null;
			for (GffIso iso : gffDetailGene.getLsCodSplit()) {
				int startNum = iso.getNumCodInEle(junctionUnit.getStartCis());
				int endNum = iso.getNumCodInEle(junctionUnit.getEndCis());
				if (startNum == 0 || endNum == 0) continue;
				
				if (startNum > 0 && startNum == endNum) {
					isoNeedReconstruct = null;
					break;
				} else if (startNum < 0 && startNum == endNum) {
					int startDistance = iso.getCod2ExInStart(junctionUnit.getStartCis());
					int endDistance = iso.getCod2ExInEnd(junctionUnit.getEndCis());
					if (startDistance == 0 && endDistance == 0) {
						if (isoNeedReconstruct == null || isoNeedReconstruct.size() < iso.size()) {
							isoNeedReconstruct = iso;
						}
					}
				}
			}
			
			if (isoNeedReconstruct == null) continue;
			mapJunc2IsoNeedReconstruct.put(junctionUnit, isoNeedReconstruct);
		}
		return mapJunc2IsoNeedReconstruct;
	}
	
	/** 这个junction就来源于该iso，只要把该iso的那两个exon连上就行了 */
	protected static GffIso reconstructIso(JunctionUnit juncUnit, GffIso iso) {
		GffIso isoNew = iso.clone();
		isoNew.clearElements();
		
		int intronNum = Math.abs(iso.getNumCodInEle(juncUnit.getStartAbs())) - 1;
		
		for (int i = 0; i < iso.size(); i++) {
			ExonInfo exonInfo = iso.get(i).clone();
			if (i == intronNum) {
				ExonInfo exonInfoNext = iso.get(i+1);
				exonInfo.setEndCis(exonInfoNext.getEndCis());
				i++;
			}
			isoNew.add(exonInfo);
		}
		return isoNew;
	}
	
	public void clear() {
		gffDetailGene = null;
		gffHashGene = null;
		mapReads = null;
	}
	
	/** 有的基因同时包含两个方向的iso，这里我们选择多的方向的要过滤掉方向不同的iso */
	public static GffGene getGeneWithSameStrand(GffGene gene) {
		List<GffGene> lsGenes = getlsGffDetailGenes(gene);
		GffGene geneResult = lsGenes.get(0);
		if (lsGenes.size() > 1) {
			GffGene gene2 = lsGenes.get(1);
			if (gene2.getLsCodSplit().size() >= geneResult.getLsCodSplit().size()) {
				geneResult = gene2;
			}
		}
		return geneResult;
	}
	
	/**
	 * <b>输入的GffDetailGene必须所有iso都是同一个parentGeneName，也就是说这些iso都来自于同一个基因</b><br><br>
	 * 就算是输入的iso都是来自同一个基因，它们的方向依然可能相反，这是我在ensembl的文件中发现的问题<br><br>
	 * 如果本GffDetailGene中包含正反两个方向的iso，则把两个方向的iso都返回<br>
	 * 仅用于GFF3的结果，如NCBI的等<br>
	 */
	private static List<GffGene> getlsGffDetailGenes(GffGene gene) {
		Map<Boolean, GffGene> mapStrand2Gene = new HashMap<>();
		for (GffIso gffGeneIsoInfo : gene.getLsCodSplit()) {
			String parentName = gffGeneIsoInfo.getParentGeneName();
			GffGene gffDetailGene = mapStrand2Gene.get(gffGeneIsoInfo.isCis5to3());
			if (gffDetailGene == null) {
				gffDetailGene = getGffDetailGeneClone(gene);
				gffDetailGene.addItemName(parentName);
				mapStrand2Gene.put(gffGeneIsoInfo.isCis5to3(), gffDetailGene);
			}
			gffDetailGene.addIsoSimple(gffGeneIsoInfo);
		}
		return new ArrayList<>(mapStrand2Gene.values());
	}
	
	/** 返回一个和现在GffDetailGene一样的GffDetailGene */
	private static GffGene getGffDetailGeneClone(GffGene gene) {
		GffGene gffDetailGene = gene.clone();
		gffDetailGene.clearIso();
		return gffDetailGene;
	}
}
