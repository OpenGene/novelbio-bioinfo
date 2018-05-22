package com.novelbio.analysis.seq.genome.gffoperate.exoncluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.JunctionInfo.JunctionUnit;
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
	protected ArrayListMultimap<String, Double> getLsJuncCounts(String condition) {
		Align align = getDifSite().get(0);
		ArrayListMultimap<String, Double> mapGroup2LsValue = ArrayListMultimap.create();
		//第一个剪接点
		List<JunctionUnit> lsJunctionStart = tophatJunction.getLsJunctionUnit(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getStartAbs());
		//第二个剪接点
		List<JunctionUnit> lsJunctionEnd = tophatJunction.getLsJunctionUnit(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getEndAbs());
		
		Map<String, Double> mapGroup2ValueStartSite = new HashMap<>();
		Map<String, Double> mapGroup2ValueEndSite = new HashMap<>();
		for (JunctionUnit junctionUnit : lsJunctionStart) {
			if (isInsideExonCluster(junctionUnit)) {
				continue;
			}
			Map<String, Double> mapGroup2ValueTmp1 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(),
					exonCluster.getRefID(), junctionUnit.getStartAbs(), junctionUnit.getEndAbs());
			addMapGroup2Value(mapGroup2ValueStartSite, mapGroup2ValueTmp1);
		}
		for (JunctionUnit junctionUnit : lsJunctionEnd) {
			if (isInsideExonCluster(junctionUnit)) {
				continue;
			}
			Map<String, Double> mapGroup2ValueTmp2 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(),
					exonCluster.getRefID(), junctionUnit.getStartAbs(), junctionUnit.getEndAbs());
			addMapGroup2Value(mapGroup2ValueEndSite, mapGroup2ValueTmp2);
		}
		
//		Map<String, Double> mapGroup2Value1 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getStartAbs());
//		Map<String, Double> mapGroup2Value2 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getEndAbs());
		if (mapGroup2ValueEndSite.size() == 0) {
			mapGroup2ValueEndSite = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), align.getEndAbs());
		}
		if (mapGroup2ValueStartSite.size() == 0) {
			for (String groupName : mapGroup2ValueEndSite.keySet()) {
				mapGroup2ValueStartSite.put(groupName, 0.0);
			}
		}
		if (isCis()) {
			addMapGroup2Value(mapGroup2LsValue, mapGroup2ValueStartSite);
			addMapGroup2Value(mapGroup2LsValue, mapGroup2ValueEndSite);
		} else {
			addMapGroup2Value(mapGroup2LsValue, mapGroup2ValueEndSite);
			addMapGroup2Value(mapGroup2LsValue, mapGroup2ValueStartSite);
		}

		return mapGroup2LsValue;
	}
	
	/**
	 * 位点是否为顺式，即从5-->3这种
	 * 我们认为 5-->3 方向的 alt3 为顺式
	 * @return
	 */
	private boolean isCis() {
		return (exonCluster.isCis5to3() && getType() == SplicingAlternativeType.alt3)
				||(!exonCluster.isCis5to3() && getType() == SplicingAlternativeType.alt5)
				;
	}
	
	private boolean isInsideExonCluster(JunctionUnit junctionUnit) {
		boolean startIn = junctionUnit.getStartAbs() > exonCluster.getStartAbs() && junctionUnit.getStartAbs() < exonCluster.getEndAbs();
		boolean endIn = junctionUnit.getEndAbs() > exonCluster.getStartAbs() && junctionUnit.getEndAbs() < exonCluster.getEndAbs();
		return startIn && endIn;
	}
	
	private void addMapGroup2Value(ArrayListMultimap<String, Double> mapGroup2LsValue, Map<String, Double> mapGroup2ValueTmp) {
		for (String group : mapGroup2ValueTmp.keySet()) {
			mapGroup2LsValue.put(group, mapGroup2ValueTmp.get(group));
		}
	}
	
	private void addMapGroup2Value(Map<String, Double> mapGroup2Value, Map<String, Double> mapGroup2ValueTmp) {
		for (String group : mapGroup2ValueTmp.keySet()) {
			double value = mapGroup2ValueTmp.get(group);
			if (mapGroup2Value.containsKey(group)) {
				value = mapGroup2Value.get(group) + value;
				
			}
			mapGroup2Value.put(group, value);
		}
	}
	/** 获得alt5， alt3的差异位点 */
	@Override
	public List<Align> getDifSite() {
		Map<Integer, Double> mapEdge2JuncNum = new HashMap<>();
		
		//把每个exon边界 所对应的junc reads Num放入treemap
		//junc reads Num为key，treemap直接排序
		//为防止junc reads num重复，用list装value
		for (Integer edge : mapEdge2Iso.keySet()) {
			double juncNum = tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), exonCluster.getRefID(), edge);
			mapEdge2JuncNum.put(edge, juncNum);
		}
		
		//获得reads数最多的两个边界，同时还要比最短  lengthMin 要长
		int[] startEnd = getStartEnd(mapEdge2JuncNum);
		isFiltered = Math.abs(startEnd[0] - startEnd[1]) >= lengthMin;

		Align align = new Align(exonCluster.getRefID(), MathComput.min(startEnd), MathComput.max(startEnd));
		align.setCis5to3(exonCluster.isCis5to3());
		
		List<Align> lsAligns = new ArrayList<>();
		lsAligns.add(align);
		return lsAligns;
	}
	
	public Align getResultSite() {
		List<Align> lsDifSite = getDifSite();
		List<? extends Alignment> lsBG = getBGSite();
		List<Alignment> lsResult = new ArrayList<>();
		lsResult.addAll(lsBG);
		lsResult.addAll(lsDifSite);
		return Align.getAlignFromList(lsResult);
	}
	
	/**
	 * 给定边界和该边界所对应的reads，获得相对来说区域比较长，并且reads比较多的区段
	 * @param mapEdge2JuncNum
	 * @return
	 */
	private int[] getStartEnd(Map<Integer, Double> mapEdge2JuncNum) {
		TreeMap<Double, List<int[]>> treeReadsAll2LsStartEnd = new TreeMap<>(
				new Comparator<Double>() {
					public int compare(Double o1, Double o2) {
						return -o1.compareTo(o2);
					}
				}
		);
		List<int[]> lsEdge2Value = new ArrayList<>();
		for (Integer edge : mapEdge2JuncNum.keySet()) {
			lsEdge2Value.add(new int[]{edge, mapEdge2JuncNum.get(edge).intValue()});
		}
		for (int i = 0; i < lsEdge2Value.size()-1; i++) {
			for (int j = i+1; j < lsEdge2Value.size(); j++) {
				//看几何平均
				double max = Math.sqrt(lsEdge2Value.get(i)[1] * lsEdge2Value.get(j)[1]);
				List<int[]> lsStartEnd = treeReadsAll2LsStartEnd.get(max);
				if (lsStartEnd == null) {
					lsStartEnd = new ArrayList<>();
					treeReadsAll2LsStartEnd.put(max, lsStartEnd);
				}
				lsStartEnd.add(new int[]{lsEdge2Value.get(i)[0], lsEdge2Value.get(j)[0]});
			}
		}
		int[] startEnd = null;
		for (Double readsNum : treeReadsAll2LsStartEnd.keySet()) {
			List<int[]> lsStartEnd = treeReadsAll2LsStartEnd.get(readsNum);
			for (int[] is : lsStartEnd) {
				if (Math.abs(is[0] - is[1]) >= lengthMin) {
					startEnd = is;
					break;
				}
			}
		}
		if (startEnd == null) {
			startEnd = treeReadsAll2LsStartEnd.firstEntry().getValue().get(0);
		}
		return startEnd;
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

	public static boolean isOverlap(Alignment alg1, Alignment alg2) {
		return alg1.getStartAbs() < alg2.getEndAbs() && alg1.getEndAbs() > alg2.getStartAbs(); 
	}
}
