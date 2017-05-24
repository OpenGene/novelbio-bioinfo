package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;

public abstract class PredictAltStartEnd extends SpliceTypePredict {
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
		//那么就只会取2,3,4,5号iso其中一个
		int edge = mapValue2Edge.values().iterator().next();
		addMapGroup2LsValue(mapGroup2LsValue, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), chrID, edge));
		
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
		return exonCluster.getParentGene().getLongestSplitMrna().getLsElement();
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
