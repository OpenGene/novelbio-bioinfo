package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;

public class PredictAlt5 extends PredictAlt5Or3 {
	Map<Integer, GffGeneIsoInfo> mapEdge2Iso;
	
	public PredictAlt5(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	public String getType() {
		return SplicingAlternativeType.alt5.toString();
	}
	
	/**
	 * 仅判断本位点的可变剪接情况
	 * 也就是仅判断alt5，alt3
	 */
	protected void find() {
		mapEdge2Iso = new HashMap<Integer, GffGeneIsoInfo>();
		if (exonCluster.exonClusterAfter == null) {
			return;
		}
		for (ArrayList<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() == 0) {
				continue;
			}
			GffGeneIsoInfo gffGeneIsoInfo = lsExonInfo.get(0).getParent();
			
			if (exonCluster.exonClusterAfter.isIsoCover(gffGeneIsoInfo)) {
				mapEdge2Iso.put(lsExonInfo.get(lsExonInfo.size() - 1).getEndCis(), gffGeneIsoInfo);
			}
		}
		
		if (mapEdge2Iso.size() <= 1) {
			mapEdge2Iso.clear();
		}
	}
	
}
