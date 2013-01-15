package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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
	
	public ArrayList<Double> getJuncCounts(String condition) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		isType();
		HashSet<Integer> setEndSide = new HashSet<Integer>();
		for (Align align : lsSite) {
			setEndSide.add(align.getEndCis());
		}
		String chrID = lsSite.get(0).getRefID();
		for (Integer integer : setEndSide) {
			lsCounts.add((double) tophatJunction.getJunctionSite(condition, chrID, integer));
		}
		lsCounts.add((double) getJunReadsNum(condition));
		return lsCounts;
	}

	
	protected boolean isBeforeOrAfterNotSame() {
		ExonCluster exonClusterBefore = exonCluster.getExonClusterBefore();
		return exonClusterBefore != null && !exonClusterBefore.isSameExon();
	}
	
	protected void find() {
		lsSite = new ArrayList<Align>();
		lslsExonInfos = new ArrayList<ArrayList<ExonInfo>>();
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() <= 0) {
			return;
		}
		for (ArrayList<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() > 0 && lsExonInfo.get(0).getItemNum() == 0) {
				int start = lsExonInfo.get(0).getStartCis();
				int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
				Align align = new Align(exonCluster.getChrID(), start, end);
				lsSite.add(align);
				lslsExonInfos.add(lsExonInfo);
			}
		}
	}
	
	@Override
	public Align getDifSite() {
		isType();
		//倒序，获得junction最多的reads
		TreeMap<Integer, ArrayList<ExonInfo>> mapJuncNum2Exon = new TreeMap<Integer, ArrayList<ExonInfo>>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return -o1.compareTo(o2);
			}
		});
		
		for (ArrayList<ExonInfo> lsExonInfos : lslsExonInfos) {
			int juncReads = tophatJunction.getJunctionSite(exonCluster.getChrID(), lsExonInfos.get(lsExonInfos.size() - 1).getEndCis());
			mapJuncNum2Exon.put(juncReads, lsExonInfos);
		}
		//获得第一个
		Align align = null;
		for (Integer juncNum : mapJuncNum2Exon.keySet()) {
			ArrayList<ExonInfo> lsExonInfos = mapJuncNum2Exon.get(juncNum);
			align = new Align(exonCluster.getChrID(), lsExonInfos.get(0).getStartCis(), lsExonInfos.get(lsExonInfos.size() - 1).getEndCis());
			break;
		}		
		return align;
	}
}
