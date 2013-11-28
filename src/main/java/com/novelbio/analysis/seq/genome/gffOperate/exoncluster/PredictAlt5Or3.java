package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.MathComput;

public abstract class PredictAlt5Or3 extends SpliceTypePredict {
	Map<Integer, GffGeneIsoInfo> mapEdge2Iso;
	
	/** 小于9个碱基，就不计算其pvalue */
	public static final int lengthMin = 9;
	/**
	 * 是否通过过滤
	 * alt5和alt3，如果差距太小，就不进行考虑
	 */
	boolean isFiltered = true;
	
	public PredictAlt5Or3(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	public ArrayList<Double> getJuncCounts(String condition) {
		Align align = getDifSite();
		ArrayList<Double> lsResult = new ArrayList<Double>();
		lsResult.add((double) tophatJunction.getJunctionSiteAll(condition, exonCluster.getRefID(), align.getStartAbs()));
		lsResult.add((double) tophatJunction.getJunctionSiteAll(condition, exonCluster.getRefID(), align.getEndAbs()));
		return lsResult;
	}
	
	/** 获得alt5， alt3的差异位点 */
	@Override
	public Align getDifSite() {
		Map<Integer, List<Integer>> mapJuncNum2Edge = new TreeMap<Integer, List<Integer>>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return -o1.compareTo(o2);
			}
		});
		//把每个exon边界 所对应的junc reads Num放入treemap
		//junc reads Num为key，treemap直接排序
		//为防止junc reads num重复，用list装value
		for (Integer edge : mapEdge2Iso.keySet()) {
			int juncNum = tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), exonCluster.getRefID(), edge);
			List<Integer> lsSite = null;
			if (mapJuncNum2Edge.containsKey(juncNum)) {
				lsSite = mapJuncNum2Edge.get(juncNum);
			} else {
				lsSite = new ArrayList<Integer>();
				mapJuncNum2Edge.put(juncNum, lsSite);
			}
			lsSite.add(edge);
		}
		
		//获得reads数最多的两个边界
		int i = 0;
		int[] startEnd = new int[2];
		for (Integer juncNum : mapJuncNum2Edge.keySet()) {
			List<Integer> lsSite = mapJuncNum2Edge.get(juncNum);
			for (Integer integer : lsSite) {
				if (i >= 2) {
					break;
				}
				startEnd[i] = integer;
				i++;
			}
		}
		
		if (Math.abs(startEnd[0] - startEnd[1]) > 500000) {
			System.out.println("stop");
		}
		if (Math.abs(startEnd[0] - startEnd[1]) < lengthMin) {
			isFiltered = false;
		} else {
			isFiltered = true;
		}
		return new Align(exonCluster.getRefID(), MathComput.min(startEnd), MathComput.max(startEnd));
	}
	
	public List<? extends Alignment> getBGSite() {
		List<Alignment> lsAlignments = new ArrayList<Alignment>();
		lsAlignments.add(exonCluster);
		return lsAlignments;
	}
	
	public boolean isFiltered() {
		return isFiltered;
	}
	
	@Override
	protected boolean isType() {
		find();
		if (mapEdge2Iso.size() > 1) {
			return true;
		}
		return false;
	}
	
	protected abstract void find();

}
