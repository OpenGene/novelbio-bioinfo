package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;

//TODO 要和alt5 alt3区分开
public class PredictAltStart {

	ExonCluster exonCluster;
	ArrayList<ArrayList<ExonInfo>> lsExonThis;
	Boolean isAltStart = null;
	ArrayList<Align> lsSite;
	
	public PredictAltStart(ExonCluster exonCluster) {
		this.exonCluster = exonCluster;
	}
	
	public ArrayList<Double> getjuncCounts(String condition, TophatJunction tophatJunction) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		isAltStart();
		HashSet<Integer> setEndSide = new HashSet<Integer>();
		for (Align align : lsSite) {
			setEndSide.add(align.getEndCis());
		}
		String chrID = lsSite.get(0).getRefID();
		for (Integer integer : setEndSide) {
			lsCounts.add((double) tophatJunction.getJunctionSite(condition, chrID, integer));
		}
		
		
	}
	
	public boolean isAltStart() {
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
