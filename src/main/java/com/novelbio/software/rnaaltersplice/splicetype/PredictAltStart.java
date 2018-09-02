package com.novelbio.software.rnaaltersplice.splicetype;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;

//TODO 要和alt5 alt3区分开
public class PredictAltStart extends PredictAltStartEnd {
	
	public PredictAltStart(ExonCluster exonCluster) {
		super(exonCluster);
	}
	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.altstart;
	}

	@Override
	protected Set<Integer> getSetEdge() {
		Set<Integer> setEndSide = new HashSet<Integer>();
		for (Align align : lsSite) {
			setEndSide.add(align.getEndCis());
		}
		return setEndSide;
	}
	
	protected boolean isBeforeOrAfterNotSame() {
		ExonCluster exonClusterBefore = exonCluster.getExonClusterBefore();
		return exonClusterBefore != null && !exonClusterBefore.isSameExon();
	}
	
	protected void find() {
		List<List<ExonInfo>> lslsExonInfosTmp = new ArrayList<>();
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() <= 0) {
			return;
		}
		
		//altstart的条件
		//1. 是转录本的第一个exon
		//2A. 3’端与现有的3‘端不同
		//2B. 3’端与现有的3‘端相同，但是5‘端比现有的5’端长
		//2A和2B只要符合一个就行
		//感觉2B不行，还是把2B删除
		Set<Integer> setEdge3 = new HashSet<>();//判定2A
		int edge5Max = 0;
		
		for (List<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (!lsExonInfo.isEmpty()) {
				int start = lsExonInfo.get(0).getStartCis();
				int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
				if (lsExonInfo.get(0).getItemNum() == 0) {
					lslsExonInfosTmp.add(lsExonInfo);
				} else {
					setEdge3.add(end);
					if (edge5Max == 0 || (exonCluster.isCis5to3() && edge5Max > start) || (!exonCluster.isCis5to3() && edge5Max < start)) {
						edge5Max = start;
					}
				}
			}
		}

		lsSite = new ArrayList<>();
		lslsExonInfos = new ArrayList<>();
		for (List<ExonInfo> lsExonInfo : lslsExonInfosTmp) {
			int start = lsExonInfo.get(0).getStartCis();
			int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
			Align align = new Align(exonCluster.getChrId(), start, end);

			//去除2B的情况
//			if ((exonCluster.isCis5to3() && start < edge5Max) || (!exonCluster.isCis5to3() && start > edge5Max) || !setEdge3.contains(end)) {
			if (!setEdge3.contains(end)) {
				lsSite.add(align);
				lslsExonInfos.add(lsExonInfo);
			}
		}
	}
	
	/**
	 * 注意本类型中只有一个align
	 */
	@Override
	public List<Align> getDifSite() {
		isType();
		//倒序，获得junction最多的reads
		TreeMap<Double, List<ExonInfo>> mapJuncNum2Exon = new TreeMap<>(new Comparator<Double>() {
			public int compare(Double o1, Double o2) {
				return -o1.compareTo(o2);
			}
		});
		
		for (List<ExonInfo> lsExonInfos : lslsExonInfos) {
			double juncReads = tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), exonCluster.getChrId(), lsExonInfos.get(lsExonInfos.size() - 1).getEndCis());
			mapJuncNum2Exon.put(juncReads, lsExonInfos);
		}
		//获得第一个
		Align align = null;
		for (Double juncNum : mapJuncNum2Exon.keySet()) {
			List<ExonInfo> lsExonInfos = mapJuncNum2Exon.get(juncNum);
			align = new Align(exonCluster.getChrId(), lsExonInfos.get(0).getStartCis(), lsExonInfos.get(lsExonInfos.size() - 1).getEndCis());
			align.setCis5to3(exonCluster.isCis5to3());
			break;
		}
		List<Align> lsAligns = new ArrayList<>();
		lsAligns.add(align);
		return lsAligns;
	}

}
