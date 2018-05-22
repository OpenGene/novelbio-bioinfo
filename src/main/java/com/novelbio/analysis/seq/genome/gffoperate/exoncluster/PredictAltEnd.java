package com.novelbio.analysis.seq.genome.gffoperate.exoncluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.novelbio.analysis.seq.genome.gffoperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;

public class PredictAltEnd extends PredictAltStartEnd {

	
	public PredictAltEnd(ExonCluster exonCluster) {
		super(exonCluster);
	}
	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.altend;
	}
	
	protected boolean isBeforeOrAfterNotSame() {
		ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
		return exonClusterAfter != null && !exonClusterAfter.isSameExon();
	}

	@Override
	protected Set<Integer> getSetEdge() {
		Set<Integer> setStartSide = new HashSet<Integer>();
		for (Align align : lsSite) {
			setStartSide.add(align.getStartCis());
		}
		return setStartSide;
	}
	/**
	 * 看本位点是否能和前一个exon组成mutually exclusivelsIsoExon
	 * 并且填充lsExonThisBefore和lsExonBefore
	 */
	protected void find() {
		List<List<ExonInfo>> lslsExonInfosTmp = new ArrayList<>();
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() <= 0) {
			return;
		}
		//altend的条件
		//1. 是转录本的最后一个exon
		//2A. 5’端与现有的5‘端不同
		//2B. 5’端与现有的5‘端相同，但是3‘端比现有的3’端长
		//2A和2B只要符合一个就行
		//感觉2B不行，还是把2B删除
		Set<Integer> setEdge5 = new HashSet<>();//判定2A
		int edge3Max = 0;
		for (List<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (!lsExonInfo.isEmpty()) {
				GffGeneIsoInfo gffGeneIsoInfo = lsExonInfo.get(0).getParent();
				int start = lsExonInfo.get(0).getStartCis();
				int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
				
				if (lsExonInfo.get(lsExonInfo.size() - 1).getItemNum() == gffGeneIsoInfo.size() - 1) {
					lslsExonInfosTmp.add(lsExonInfo);
				} else {
					setEdge5.add(start);
					if (edge3Max == 0 || (exonCluster.isCis5to3() && edge3Max < end) || (!exonCluster.isCis5to3() && edge3Max > end)) {
						edge3Max = end;
					}
				}
			}
		}
		lsSite = new ArrayList<>();
		lslsExonInfos = new ArrayList<>();
		for (List<ExonInfo> lsExonInfo : lslsExonInfosTmp) {
			int start = lsExonInfo.get(0).getStartCis();
			int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
			Align align = new Align(exonCluster.getRefID(), start, end);

			//去除2B的情况
//			if ((exonCluster.isCis5to3() && end > edge3Max) || (!exonCluster.isCis5to3() && end < edge3Max) || !setEdge5.contains(start)) {
			if (!setEdge5.contains(start)) {
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
			double juncReads = tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), exonCluster.getRefID(), lsExonInfos.get(0).getStartCis());
			mapJuncNum2Exon.put(juncReads, lsExonInfos);
		}
		//获得第一个
		Align align = null;
		for (Double juncNum : mapJuncNum2Exon.keySet()) {
			List<ExonInfo> lsExonInfos = mapJuncNum2Exon.get(juncNum);
			align = new Align(exonCluster.getRefID(), lsExonInfos.get(0).getStartCis(), lsExonInfos.get(lsExonInfos.size() - 1).getEndCis());
			break;
		}		
		List<Align> lsAligns = new ArrayList<>();
		lsAligns.add(align);
		return lsAligns;
	}

}
