package com.novelbio.analysis.seq.genome.gffoperate.exoncluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.genome.gffoperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
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
	
	public List<? extends Alignment> getBGSiteSplice() {
		List<Alignment> lsAlignments = new ArrayList<Alignment>();
		Align alignBG = null;
		int endBGcis = exonCluster.getEndCis();
		Align align = getDifSite().get(0);
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
		mapEdge2Iso = new HashMap<>();
		Align align = null;
		if (exonCluster.exonClusterBefore == null) {
			return;
		}
		//将iso中单个exon的提取出来然后做预测，多个exon的iso跳过不参与分析
		List<ExonInfo> lsExon = new ArrayList<>();
		for (List<ExonInfo> lsExonInfos : exonCluster.getLsIsoExon()) {
			if (lsExonInfos.size() == 0 || lsExonInfos.size() > 1) {
				continue;
			}
			lsExon.add(lsExonInfos.get(0));
		}
		Collections.sort(lsExon, new Comparator<ExonInfo>() {
			public int compare(ExonInfo o1, ExonInfo o2) {
				Integer o1Len = o1.getLength();
				Integer o2Len = o2.getLength();
				return -o1Len.compareTo(o2Len);
			}
		});
		for (ExonInfo exonInfo : lsExon) {
			GffGeneIsoInfo gffGeneIsoInfo = exonInfo.getParent();
			if (exonCluster.exonClusterBefore.isIsoCover(gffGeneIsoInfo)) {
				if (align == null) {
					mapEdge2Iso.put(exonInfo.getStartCis(), gffGeneIsoInfo);
					align = new Align(exonInfo.getRefID(), exonInfo.getStartAbs(), exonInfo.getEndAbs());
				} else if (isOverlap(align, exonInfo)) {
					mapEdge2Iso.put(exonInfo.getStartCis(), gffGeneIsoInfo);
					align = new Align(exonInfo.getRefID(), Math.min(align.getStartAbs(), exonInfo.getStartAbs()),
							Math.max(align.getEndAbs(), exonInfo.getEndAbs()));
				}
			}
		}
		
		if (mapEdge2Iso.size() <= 1) {
			mapEdge2Iso.clear();
		}
	}

}
