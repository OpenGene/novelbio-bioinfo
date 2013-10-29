package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;

/**
 * 这个不在那八种定义内，就是提前终止<br>
 * 譬如<br>
 * 1--2-------3--4----------5--6<br>
 * 1--2-------3-----4‘<br>
 * @author zong0jie
 *
 */
public class PredictEndDifStop extends SpliceTypePredict {
	ArrayList<List<ExonInfo>> ls_lsExonInfos;
	
	public PredictEndDifStop(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	public ArrayList<Double> getJuncCounts(String condition) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		for (List<ExonInfo> lsExonInfos : ls_lsExonInfos) {
			for (ExonInfo exonInfo : lsExonInfos) {
				int num = tophatJunction.getJunctionSite(condition, exonCluster.getRefID(), exonInfo.getStartCis());
				num += tophatJunction.getJunctionSite(condition, exonCluster.getRefID(), exonInfo.getEndCis());
				lsCounts.add((double) num);
			}
		}
		return lsCounts;
	}

	@Override
	protected boolean isType() {
		ls_lsExonInfos = new ArrayList<List<ExonInfo>>();
		ArrayListMultimap<Integer, ExonInfo> mapEdge2LsExons = ArrayListMultimap.create();
		
		for (List<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() == 0 || lsExonInfo.size() > 1) {
				continue;
			}
			mapEdge2LsExons.put(lsExonInfo.get(0).getStartCis(), lsExonInfo.get(0));
		}
		
		for (Integer startEdge : mapEdge2LsExons.keySet()) {
			List<ExonInfo> lsExonInfos = mapEdge2LsExons.get(startEdge);
			if (lsExonInfos.size() <= 1) {
				continue;
			}
			for (ExonInfo exonInfo : lsExonInfos) {
				if (exonInfo.getItemNum() == exonInfo.getParent().size() - 1) {
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
		return SplicingAlternativeType.endDif;
	}

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
