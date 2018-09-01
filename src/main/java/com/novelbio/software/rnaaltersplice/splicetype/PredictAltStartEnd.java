package com.novelbio.software.rnaaltersplice.splicetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.software.rnaaltersplice.splicetype.SpliceTypePredict.SplicingAlternativeType;

public abstract class PredictAltStartEnd extends SpliceTypePredict {
	private static final Logger logger = LoggerFactory.getLogger(PredictAltStartEnd.class);
	/** exon与前面一个exon尾巴的坐标 */
	ArrayList<Align> lsSite;

	/** 判定为altStartEnd的listexon */
	List<List<ExonInfo>> lslsExonInfos;
	
	public PredictAltStartEnd(ExonCluster exonCluster) {
		super(exonCluster);
	}
 
	protected boolean isType() {
		boolean istype = false;
		if (isBeforeOrAfterNotSame()) {
			find();
		}
		
		if (lsSite == null || lsSite.size() == 0) {
			istype = false;
		} else {
			istype = true;
		}
		return istype;
	}
	
	protected ArrayListMultimap<String, Double> getLsJuncCounts(String condition) {
		ArrayListMultimap<String, Double> mapGroup2LsValue = ArrayListMultimap.create();
		isType();
		Set<Integer> setEdge = getSetEdge();
		String chrID = lsSite.get(0).getRefID();
		TreeMap<Double, Integer> mapValue2Edge = new TreeMap<>(new Comparator<Double>() {
			public int compare(Double o1, Double o2) {
				return -o1.compareTo(o2);
			}
		});
		
		for (Integer loc : setEdge) {
			mapValue2Edge.put(tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), chrID, loc), loc);
		}
		//注意这里仅取了其中一个iso的边，那么如果这个区域有多个iso类似(2,3,4,5号iso)
		//1. 10-20--------------------------------------
		//2.              30-40--------------------------
		//3.              30--50-------------------------
		//4.              30--52-------------------------
		//5.              30---60------------------------
		
		//================================
		//取2,3,4,5号iso全部的
//		for (Integer edge : setEdge) {
//			addMapGroup2LsValue(mapGroup2LsValue, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), chrID, edge));
//		}
		//================================
		//只取2,3,4,5号iso其中value最大的一个
		int edge = mapValue2Edge.values().iterator().next();
		addMapGroup2LsValue(mapGroup2LsValue, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), chrID, edge));
		//================================
		
		addMapGroup2LsValue(mapGroup2LsValue, getSkipReadsNum(condition));
		return mapGroup2LsValue;
	}
	
	protected abstract Set<Integer> getSetEdge();
	
	/**
	 * 获得比较的位点
	 * 如果是cassette则返回全基因长度
	 * 如果是retain intron和alt5 alt3，返回该exon的长度
	 */
	public List<? extends Alignment> getBGSiteSplice() {
		//altstart和altend只有一个类型
		List<Align> lsDifSites = getDifSite();
		Align align = Align.getAlignFromList(lsDifSites);
		return getBGSite(align, getType(), exonCluster.getParentGene());
	}
	
	/**
	 * 给定一个exon区段，把在这个exon之前或者之后的最多两个exon全提取出来，并按照顺序排列
	 * @param align
	 * @param type
	 * @param gffDetailGene
	 * @return
	 */
	@VisibleForTesting
	public static List<? extends Alignment> getBGSite(Align align, SplicingAlternativeType type, GffGene gffDetailGene) {
		int startAbs = align.getStartAbs();
		int endAbs = align.getEndAbs();
		
		/** 获取本exon之前/之后的至多两个exon */
		List<ExonInfo> lsExonInfos = new ArrayList<>();
		for (GffIso iso : gffDetailGene.getLsCodSplit()) {
			if (
					(iso.isCis5to3() && type == SplicingAlternativeType.altstart)
					|| (!iso.isCis5to3() && type == SplicingAlternativeType.altend)
					) {
				for (ExonInfo exonInfo : iso) {
					if (exonInfo.getEndAbs() < startAbs) {
						lsExonInfos.add(exonInfo);
					}
				}
			} else if (
					(!iso.isCis5to3() && type == SplicingAlternativeType.altstart)
					||(iso.isCis5to3() && type == SplicingAlternativeType.altend)
					)
					{
				for (ExonInfo exonInfo : iso) {
					if (exonInfo.getStartAbs() > endAbs) {
						lsExonInfos.add(exonInfo);
					}
				}
			}
		}
		boolean isCis5To3 = gffDetailGene.isCis5to3();
		if (lsExonInfos.isEmpty()) {
			logger.error("cannot get BG site of Gene " + gffDetailGene.getNameSingle() + " please check !!!");
			return gffDetailGene.getLongestSplitMrna().getLsElement();
		}
		Collections.sort(lsExonInfos);

		List<Align> lsResult = new ArrayList<>();
		Align alignLast = null;
		alignLast = new Align(lsExonInfos.get(0));
		lsResult.add(alignLast);
		
		if (isCis5To3) {
			for (ExonInfo exonInfo : lsExonInfos) {
				if (exonInfo.getStartAbs() <= alignLast.getEndAbs() && exonInfo.getEndAbs() > alignLast.getEndAbs()) {
					alignLast.setEndAbs(exonInfo.getEndAbs());
				} else if (exonInfo.getStartAbs() > alignLast.getEndAbs()) {
					alignLast = new Align(exonInfo.getRefID(), exonInfo.getStartAbs(), exonInfo.getEndAbs());
					alignLast.setCis5to3(isCis5To3);
					lsResult.add(alignLast);
				}
			}
		} else {
			for (ExonInfo exonInfo : lsExonInfos) {
				if (exonInfo.getEndAbs() >= alignLast.getStartAbs() && exonInfo.getStartAbs() < alignLast.getStartAbs()) {
					alignLast.setStartAbs(exonInfo.getStartAbs());
				} else if (exonInfo.getEndAbs() < alignLast.getStartAbs()) {
					alignLast = new Align(exonInfo.getRefID(), exonInfo.getStartAbs(), exonInfo.getEndAbs());
					alignLast.setCis5to3(isCis5To3);
					lsResult.add(alignLast);
				}
			}
		}
		if (lsResult.isEmpty()) {
			if (type == SplicingAlternativeType.altstart) {
				logger.error("gene {} cannot find exons before alt start exon {}, use all iso as background", gffDetailGene.getNameSingle(), align.toString());
			} else {
				logger.error("gene {} cannot find exons after alt end exon {}, use all iso as background", gffDetailGene.getNameSingle(), align.toString());
			}
			return gffDetailGene.getLongestSplitMrna().getLsElement();
		}
		
		if (lsResult.size() <= 2) {
			return lsResult;
		}
		
		/**
		 * 一般来说 altstart/altend是这种形式
		 * 10-20-----30-40--42-45
		 * 10-20-----30-40----------48-50
		 * 10-20-----30-40-----------------60-70
		 * 10-20-----30-40---------------------------70-80-----80-90
		 * 存在情况：
		 * 其中42-45 与70-80有差异
		 * 而与48-50 60-70 都没差异
		 * 
		 * 这时候 DifSite为42-45，而BG应该是 48-50,60-70,70-80这三项
		 * 
		 * 但是现在很难确认这种情况。所以我的做法是首先BG一定包含 48-50,60-70 这两项
		 * 
		 * 然后如果70-80这个exon，距离 60-70这个exon在500bp以内，就把70-80加入BG，如果70-80距离60-70太远，则70-80就不加入BG。
		 */
		List<Align> lsResultFinal = type == SplicingAlternativeType.altstart? lsResult.subList(lsResult.size()-2, lsResult.size()) : lsResult.subList(0, 2);
		if (type == SplicingAlternativeType.altstart) {
			Align alignLast3 = lsResult.get(lsResult.size()-3);
			if (getDistance(alignLast3, lsResultFinal.get(0)) < 500) {
				lsResultFinal.add(0, alignLast3);
			}
		}
		if (type == SplicingAlternativeType.altend) {
			Align alignLast3 = lsResult.get(2);
			if (getDistance(alignLast3, lsResultFinal.get(1)) < 500) {
				lsResultFinal.add(alignLast3);
			}
		}
		return lsResultFinal;
	}
	
	@VisibleForTesting
	public static int getDistance(Align align1, Align align2) {
		if (align1.getStartAbs() > align2.getStartAbs()) {
			return align1.getStartAbs() - align2.getEndAbs();
		} else {
			return align2.getStartAbs() - align1.getEndAbs();
		}
	}
	
	public Align getResultSite() {
		List<Alignment> lsResult = new ArrayList<>();

		List<Align> lsDifSite = getDifSite();
		List<? extends Alignment> lsBG = getBGSiteSplice();
		if (getType() == SplicingAlternativeType.altstart) {
			lsResult.add(lsBG.get(0));
		} else if (getType() == SplicingAlternativeType.altend) {
			lsResult.add(lsBG.get(lsBG.size()-1));
		} else {
			throw new RuntimeException("does not support splice type " + getType().toString());
		}
		lsResult.addAll(lsDifSite);
		return Align.getAlignFromList(lsResult);
	}
	
	/**
	 * altStart返回Before
	 * altEnd 返回after
	 * @return
	 */
	protected abstract boolean isBeforeOrAfterNotSame();
	
	/**
	 * 看本位点是否能和前一个exon组成mutually exclusivelsIsoExon
	 * 并且填充lsExonThisBefore和lsExonBefore
	 */
	protected abstract void find();
	
}
