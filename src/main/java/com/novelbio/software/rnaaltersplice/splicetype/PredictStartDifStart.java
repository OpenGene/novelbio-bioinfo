package com.novelbio.software.rnaaltersplice.splicetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonInfo;

public class PredictStartDifStart extends SpliceTypePredict {
	ArrayList<List<ExonInfo>> ls_lsExonInfos;

	public PredictStartDifStart(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	protected ArrayListMultimap<String, Double> getLsJuncCounts(String condition) {
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
		Collections.sort(lsExonInfosFinal,(exon1, exon2)->{return -Integer.valueOf(exon1.getLength()).compareTo(exon2.getLength());});
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
	public List<Align> getDifSite() {
		Align align = new Align(exonCluster.getRefID(), exonCluster.getStartCis(), exonCluster.getEndCis());
		List<Align> lsAligns = new ArrayList<>();
		lsAligns.add(align);
		return lsAligns;
	}

	@Override
	public List<? extends Alignment> getBGSiteSplice() {
		return exonCluster.getParentGene().getLongestSplitMrna().getLsElement();
	}
	
	public Align getResultSite() {
		return Align.getAlignFromList(getDifSite());
	}

}
