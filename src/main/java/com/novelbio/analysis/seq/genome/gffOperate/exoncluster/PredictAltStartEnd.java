package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.hyperic.sigar.cmd.SysInfo;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
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
		addMapGroup2LsValue(mapGroup2LsValue, getJunReadsNum(condition));
		TreeMap<Double, Integer> mapValue2Edge = new TreeMap<>(new Comparator<Double>() {
			public int compare(Double o1, Double o2) {
				return -o1.compareTo(o2);
			}
		});
		
		for (Integer integer : setEdge) {
			mapValue2Edge.put(tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), chrID, integer), integer);
		}
		int edge = mapValue2Edge.values().iterator().next();
		try {
			addMapGroup2LsValue(mapGroup2LsValue, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), chrID, edge));
		} catch (Exception e) {
			addMapGroup2LsValue(mapGroup2LsValue, tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), chrID, edge));
		}
	
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
