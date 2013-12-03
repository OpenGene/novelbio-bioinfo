package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;

public class PredictStartDifStart extends SpliceTypePredict {
	ArrayList<List<ExonInfo>> ls_lsExonInfos;

	public PredictStartDifStart(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	protected List<List<Double>> getLsJuncCounts(String condition) {
		List<ExonInfo> lsExonInfosFinal = new ArrayList<>();
		for (List<ExonInfo> lsExonInfos : ls_lsExonInfos) {
			for (ExonInfo exonInfo : lsExonInfos) {
				lsExonInfosFinal.add(exonInfo);
			}
		}
		List<ExonInfo2Value> lsExonInfo2Values = getLsExon2Value(lsExonInfosFinal);
		lsExonInfosFinal.clear();
		int i = 0;
		for (ExonInfo2Value exonInfo2Value : lsExonInfo2Values) {
			if (i++ > 1) break;
			lsExonInfosFinal.add(exonInfo2Value.exonInfo);
		}
		
		return getlsJunInfoEdge(condition, lsExonInfosFinal);
	}

	@Override
	protected boolean isType() {
		ls_lsExonInfos = new ArrayList<List<ExonInfo>>();
		ArrayListMultimap<Integer, ExonInfo> mapEdge2LsExons = ArrayListMultimap.create();
		
		for (List<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() == 0 || lsExonInfo.size() > 1) {
				continue;
			}
			mapEdge2LsExons.put(lsExonInfo.get(0).getEndCis(), lsExonInfo.get(0));
		}
		
		for (Integer endEdge : mapEdge2LsExons.keySet()) {
			List<ExonInfo> lsExonInfos = mapEdge2LsExons.get(endEdge);
			if (lsExonInfos.size() <= 1) {
				continue;
			}
			for (ExonInfo exonInfo : lsExonInfos) {
				if (exonInfo.getItemNum() == 0) {
					ls_lsExonInfos.add(lsExonInfos);
					break;
				}
			}
		}
		
		if (ls_lsExonInfos.size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.startDif;
	}
	
	//TODO
	/** 待修正 */
	@Override
	public Align getDifSite() {
		return new Align(exonCluster.getRefID(), exonCluster.getStartCis(), exonCluster.getEndCis());
	}


	@Override
	public List<? extends Alignment> getBGSite() {
		return exonCluster.getParentGene().getLongestSplitMrna();
	}

}
