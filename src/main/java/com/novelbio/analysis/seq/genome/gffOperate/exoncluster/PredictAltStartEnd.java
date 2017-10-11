package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

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
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;

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
		//那么就会取全部2,3,4,5号iso的边
		for (Integer edge : mapValue2Edge.values()) {
			addMapGroup2LsValue(mapGroup2LsValue, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), chrID, edge));
		}
		
		addMapGroup2LsValue(mapGroup2LsValue, getSkipReadsNum(condition));
		return mapGroup2LsValue;
	}
	
	protected abstract Set<Integer> getSetEdge();
	
	/**
	 * 获得比较的位点
	 * 如果是cassette则返回全基因长度
	 * 如果是retain intron和alt5 alt3，返回该exon的长度
	 */
	public List<? extends Alignment> getBGSite() {
		//altstart和altend只有一个类型
		Align align = getDifSite().get(0);
		return getBGSite(align, getType(), exonCluster.getParentGene());
	}
	
	@VisibleForTesting
	protected static List<? extends Alignment> getBGSite(Align align, SplicingAlternativeType type, GffDetailGene gffDetailGene) {
		int startAbs = align.getStartAbs();
		int endAbs = align.getEndAbs();
		
		List<ExonInfo> lsExonInfos = new ArrayList<>();
		for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
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
		boolean isCis5to3 = lsExonInfos.get(0).isCis5to3();
		Collections.sort(lsExonInfos);
		List<Alignment> lsResult = new ArrayList<>();
		Align alignLast = new Align(lsExonInfos.get(0).getRefID(), lsExonInfos.get(0).getStartAbs(), lsExonInfos.get(0).getEndAbs());
		alignLast.setCis5to3(isCis5to3);
		lsResult.add(alignLast);

		if (isCis5to3) {
			for (ExonInfo exonInfo : lsExonInfos) {
				if (exonInfo.getStartAbs() <= alignLast.getEndAbs() && exonInfo.getEndAbs() > alignLast.getEndAbs()) {
					alignLast.setEndAbs(exonInfo.getEndAbs());
				} else if (exonInfo.getStartAbs() > alignLast.getEndAbs()) {
					alignLast = new Align(exonInfo.getRefID(), exonInfo.getStartAbs(), exonInfo.getEndAbs());
					alignLast.setCis5to3(isCis5to3);
					lsResult.add(alignLast);
				}
			}
		} else {
			for (ExonInfo exonInfo : lsExonInfos) {
				if (exonInfo.getEndAbs() >= alignLast.getStartAbs() && exonInfo.getStartAbs() < alignLast.getStartAbs()) {
					alignLast.setStartAbs(exonInfo.getStartAbs());
				} else if (exonInfo.getStartAbs() > alignLast.getEndAbs()) {
					alignLast = new Align(exonInfo.getRefID(), exonInfo.getStartAbs(), exonInfo.getEndAbs());
					alignLast.setCis5to3(isCis5to3);
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
		return lsResult;
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
