package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster.SplicingAlternativeType;
import com.novelbio.analysis.seq.mapping.Align;

public class PredictAltEnd extends SpliceTypePredict {
	ArrayList<ArrayList<ExonInfo>> lsExonThis;
	Boolean isAltEnd = null;
	ArrayList<Align> lsSite;
	
	public PredictAltEnd(ExonCluster exonCluster) {
		super(exonCluster);
	}
	@Override
	public String getType() {
		return SplicingAlternativeType.altend.toString();
	}
	public boolean isType() {
		if (isAltEnd != null) {
			return isAltEnd;
		}
		
		if (isAfterNotSame()) {
			find();
		}
		
		if (lsSite == null || lsSite.size() == 0) {
			isAltEnd = false;
		} else {
			isAltEnd = true;
		}
		return isAltEnd;
	}
	
	private boolean isAfterNotSame() {
		ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
		return exonClusterAfter != null && !exonClusterAfter.isSameExon();
	}
	
	/**
	 * 看本位点是否能和前一个exon组成mutually exclusivelsIsoExon
	 * 并且填充lsExonThisBefore和lsExonBefore
	 */
	private void find() {
		lsSite = new ArrayList<Align>();
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() <= 0) {
			return;
		}
		for (ArrayList<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() > 0) {
				GffGeneIsoInfo gffGeneIsoInfo = lsExonInfo.get(0).getParent();
				if (lsExonInfo.get(lsExonInfo.size() - 1).getItemNum() == gffGeneIsoInfo.size() - 1) {
					int start = lsExonInfo.get(0).getStartCis();
					int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
					Align align = new Align(exonCluster.getChrID(), start, end);
					lsSite.add(align);
				}
			}
		}
	}
	@Override
	public ArrayList<Double> getJuncCounts(String condition) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		isType();
		HashSet<Integer> setEndSide = new HashSet<Integer>();
		for (Align align : lsSite) {
			setEndSide.add(align.getStartCis());
		}
		String chrID = lsSite.get(0).getRefID();
		for (Integer integer : setEndSide) {
			lsCounts.add((double) tophatJunction.getJunctionSite(condition, chrID, integer));
		}
		lsCounts.add((double) getJunReadsNum(condition));
		return lsCounts;
	}

}
