package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;

public class PredictAlt3 extends PredictAlt5Or3 {
	
	public PredictAlt3(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.alt3;
	}
	
	/**
	 * 仅判断本位点的可变剪接情况
	 * 也就是仅判断alt5，alt3
	 */
	protected void find() {
		mapEdge2Iso = new HashMap<Integer, GffGeneIsoInfo>();
		if (exonCluster.exonClusterBefore == null) {
			return;
		}
		for (ArrayList<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() == 0) {
				continue;
			}
			GffGeneIsoInfo gffGeneIsoInfo = lsExonInfo.get(0).getParent();
			
			if (exonCluster.exonClusterBefore.isIsoCover(gffGeneIsoInfo)) {
				mapEdge2Iso.put(lsExonInfo.get(0).getStartCis(), gffGeneIsoInfo);
			}
		}
		
		if (mapEdge2Iso.size() <= 1) {
			mapEdge2Iso.clear();
		}
	}
	

}
