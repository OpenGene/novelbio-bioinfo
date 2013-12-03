package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.mapping.Align;

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
		lsSite = new ArrayList<Align>();
		lslsExonInfos = new ArrayList<>();
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() <= 0) {
			return;
		}
		for (List<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() > 0 && lsExonInfo.get(0).getItemNum() == 0) {
				int start = lsExonInfo.get(0).getStartCis();
				int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
				Align align = new Align(exonCluster.getRefID(), start, end);
				lsSite.add(align);
				lslsExonInfos.add(lsExonInfo);
			}
		}
	}
	
	@Override
	public Align getDifSite() {
		isType();
		//倒序，获得junction最多的reads
		TreeMap<Double, List<ExonInfo>> mapJuncNum2Exon = new TreeMap<>(new Comparator<Double>() {
			public int compare(Double o1, Double o2) {
				return -o1.compareTo(o2);
			}
		});
		
		for (List<ExonInfo> lsExonInfos : lslsExonInfos) {
			double juncReads = tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), exonCluster.getRefID(), lsExonInfos.get(lsExonInfos.size() - 1).getEndCis());
			mapJuncNum2Exon.put(juncReads, lsExonInfos);
		}
		//获得第一个
		Align align = null;
		for (Double juncNum : mapJuncNum2Exon.keySet()) {
			List<ExonInfo> lsExonInfos = mapJuncNum2Exon.get(juncNum);
			align = new Align(exonCluster.getRefID(), lsExonInfos.get(0).getStartCis(), lsExonInfos.get(lsExonInfos.size() - 1).getEndCis());
			break;
		}		
		return align;
	}

}
