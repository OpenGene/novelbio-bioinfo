package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.Alignment;

public class PredictAlt3 extends PredictAlt5Or3 {
	
	public PredictAlt3(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.alt3;
	}
	
	public List<? extends Alignment> getBGSite() {
		List<Alignment> lsAlignments = new ArrayList<Alignment>();
		Align alignBG = null;
		int endBGcis = exonCluster.getEndCis();
		Align align = getDifSite();
		int endSpliceCis = align.getEndCis();
		alignBG = new Align(exonCluster.getRefID(), endSpliceCis, endBGcis);
		alignBG.setCis5to3(exonCluster.isCis5to3());
		lsAlignments.add(alignBG);
		return lsAlignments;
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
		for (List<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
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
