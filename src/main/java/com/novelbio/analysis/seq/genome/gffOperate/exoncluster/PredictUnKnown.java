package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.Alignment;

public class PredictUnKnown extends SpliceTypePredict {

	public PredictUnKnown(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	public List<List<Double>> getJuncCounts(String condition) {
		List<ExonInfo> lsExon = exonCluster.getAllExons();
		List<List<Double>> lsCounts = new ArrayList<>();
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() > 0) {
			lsCounts.add(getJunReadsNum(condition));
		}
		
		//合并相同的边界
		Set<Integer> setEdge = new HashSet<Integer>();
		for (ExonInfo exonInfo : lsExon) {
			setEdge.add(exonInfo.getStartAbs());
			setEdge.add(exonInfo.getEndAbs());
		}
		for (Integer edge : setEdge) {
			List<Double> ls1 = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), exonCluster.getRefID(), edge);
			lsCounts.add((double) thisCounts);
		}
		return lsCounts;
	}

	protected List<Double> getJunReadsNum(String condition) {
		GffDetailGene gffDetailGene = exonCluster.getParentGene();
		List<Double> lsResult = new ArrayList<>();
		HashSet<String> setLocation = new HashSet<String>();
		setLocation.addAll(getSkipExonLoc_From_IsoHaveExon());
		setLocation.addAll(getSkipExonLoc_From_IsoWithoutExon(gffDetailGene));
		
		for (String string : setLocation) {
			String[] ss = string.split(SepSign.SEP_ID);
			List<Double> lsTmp = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), gffDetailGene.getRefID(),
					Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
			addLsDouble(lsResult, lsTmp);	
		}
		
		return lsResult;
	}
	
	/**
	 * 查找含有该exon的转录本，
	 * 获得跨过该外显子的坐标
	 */
	private HashSet<String> getSkipExonLoc_From_IsoHaveExon() {
		HashSet<String> setLocation = new HashSet<String>();
		for (List<ExonInfo> lsExonInfos : exonCluster.getLsIsoExon()) {
			if (lsExonInfos.size() == 0) {
				continue;
			}
			GffGeneIsoInfo gffGeneIsoInfo = lsExonInfos.get(0).getParent();
			int exonNumBefore = lsExonInfos.get(0).getItemNum() - 1;
			int exonNumAfter = lsExonInfos.get(lsExonInfos.size() - 1).getItemNum() + 1;
			if (exonNumBefore < 0 || exonNumAfter >= gffGeneIsoInfo.size()) {
				continue;
			}
			int start = gffGeneIsoInfo.get(exonNumBefore).getEndCis();
			int end = gffGeneIsoInfo.get(exonNumAfter).getStartCis();
			setLocation.add(start + SepSign.SEP_ID + end);
		}
		return setLocation;
	}
	
	/**查找不含该exon的转录本， 
	 * 获得跨过该外显子的坐标 */
	private HashSet<String> getSkipExonLoc_From_IsoWithoutExon(GffDetailGene gffDetailGene) {
		HashSet<String> setLocation = new HashSet<String>();
		
		Map<GffGeneIsoInfo, Integer> hashTmp = exonCluster.getMapIso2ExonIndexSkipTheCluster();
		for (Entry<GffGeneIsoInfo, Integer> entry : hashTmp.entrySet()) {
			GffGeneIsoInfo gffGeneIsoInfo = entry.getKey();
			int exonNum = entry.getValue();
			if (exonNum >= gffGeneIsoInfo.size()-1) {
				continue;
			}
			String location = null;
			try {
				location = gffGeneIsoInfo.get(exonNum).getEndCis() + SepSign.SEP_ID + gffGeneIsoInfo.get(exonNum+1).getStartCis();
			} catch (Exception e) {
				location = gffGeneIsoInfo.get(exonNum).getEndCis() + SepSign.SEP_ID + gffGeneIsoInfo.get(exonNum+1).getStartCis();
			}
			setLocation.add(location);
		}
		return setLocation;
	}
	
	@Override
	protected boolean isType() {
		return !exonCluster.isSameExonInExistIso();
	}

	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.unknown;
	}

	@Override
	public Align getDifSite() {
		return new Align(exonCluster.getRefID(), exonCluster.getStartCis(), exonCluster.getEndCis());
	}

	@Override
	public List<? extends Alignment> getBGSite() {
		return exonCluster.getParentGene().getLongestSplitMrna();
	}
	
}
