package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster.SplicingAlternativeType;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.database.domain.geneanno.SepSign;

//TODO 要和alt5 alt3区分开
public class PredictAltStart extends SpliceTypePredict {
	ArrayList<ArrayList<ExonInfo>> lsExonThis;
	Boolean isAltStart = null;
	ArrayList<Align> lsSite;
	
	public PredictAltStart(ExonCluster exonCluster) {
		super(exonCluster);
	}
	@Override
	public String getType() {
		return SplicingAlternativeType.altstart.toString();
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
	
	public boolean isType() {
		if (isAltStart != null) {
			return isAltStart;
		}
		
		if (isBeforeNotSame()) {
			find();
		}
		
		if (lsSite == null || lsSite.size() == 0) {
			isAltStart = false;
		} else {
			isAltStart = true;
		}
		return isAltStart;
	}
	
	private boolean isBeforeNotSame() {
		ExonCluster exonClusterBefore = exonCluster.getExonClusterBefore();
		return exonClusterBefore != null && !exonClusterBefore.isSameExon();
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
			if (lsExonInfo.size() > 0 && lsExonInfo.get(0).getItemNum() == 0) {
				int start = lsExonInfo.get(0).getStartCis();
				int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
				Align align = new Align(exonCluster.getChrID(), start, end);
				lsSite.add(align);
			}
		}
	}
}
