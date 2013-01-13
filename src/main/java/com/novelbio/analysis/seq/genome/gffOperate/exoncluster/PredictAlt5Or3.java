package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;

public abstract class PredictAlt5Or3 extends SpliceTypePredict {
	Map<Integer, GffGeneIsoInfo> mapEdge2Iso;
	
	public PredictAlt5Or3(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	public ArrayList<Double> getJuncCounts(String condition) {
		Align align = getDifSite();
		ArrayList<Double> lsResult = new ArrayList<Double>();
		lsResult.add((double) tophatJunction.getJunctionSite(condition, exonCluster.getChrID(), align.getStartCis()));
		lsResult.add((double) tophatJunction.getJunctionSite(condition, exonCluster.getChrID(), align.getEndCis()));
		return lsResult;
	}
	
	/** 获得alt5， alt3的差异位点 */
	@Override
	public Align getDifSite() {
		Map<Integer, Integer> mapJuncNum2Edge = new TreeMap<Integer, Integer>();
		for (Integer integer : mapEdge2Iso.keySet()) {
			mapJuncNum2Edge.put(tophatJunction.getJunctionSite(exonCluster.getChrID(), integer), integer);
		}
		int i = 0;
		int[] startEnd = new int[2];
		for (Integer juncNum : mapJuncNum2Edge.keySet()) {
			startEnd[i] = mapJuncNum2Edge.get(juncNum);
			i++;
			if (i == 2) {
				break;
			}
		}
		return new Align(exonCluster.getChrID(), startEnd[0], startEnd[1]);
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

}
