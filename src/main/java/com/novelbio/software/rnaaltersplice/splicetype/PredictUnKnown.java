package com.novelbio.software.rnaaltersplice.splicetype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.SepSign;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;

public class PredictUnKnown extends SpliceTypePredict {

	public PredictUnKnown(ExonCluster exonCluster) {
		super(exonCluster);
	}

	@Override
	protected ArrayListMultimap<String, Double> getLsJuncCounts(String condition) {
		List<ExonInfo2Value> lsExonInfos = getLsExon2Value(exonCluster.getAllExons());
		List<ExonInfo> lsExon = new ArrayList<>();
		int i = 0;
		for (ExonInfo2Value exonInfo2Value : lsExonInfos) {
			if (i++ > 2) break;
			lsExon.add(exonInfo2Value.exonInfo);
		}
		ArrayListMultimap<String, Double> mapGroup2LsValue = getlsJunInfoEdge(condition, lsExon);
		//如果跨过 exon的reads很多，则把跨过 exon的 reads添加进去
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() > 0 && 
				(lsExonInfos.size() < 2 || lsExonInfos.get(1) == null || getSkipNumAll() >= lsExonInfos.get(1).value)) {
			Map<String, Double> mapGroup2Value = getSkipReadsNum(condition);
			for (String group : mapGroup2Value.keySet()) {
				List<Double> lsTmpValue = mapGroup2LsValue.get(group);
				lsTmpValue.add(0, mapGroup2Value.get(group));
				if (lsTmpValue.size() > 2) {
					lsTmpValue.remove(2);
				}
			}
		}
		return mapGroup2LsValue;
	}

	protected Map<String, Double> getSkipReadsNum(String condition) {
		GffGene gffDetailGene = exonCluster.getParentGene();
		Map<String, Double> mapResult = new HashMap<>();
		HashSet<String> setLocation = new HashSet<String>();
		setLocation.addAll(getSkipExonLoc_From_IsoHaveExon());
		setLocation.addAll(getSkipExonLoc_From_IsoWithoutExon());
		
		for (String string : setLocation) {
			String[] ss = string.split(SepSign.SEP_ID);
			Map<String, Double> mapTmp = tophatJunction.getJunctionSite(condition, exonCluster.isCis5to3(), gffDetailGene.getRefID(),
					Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
			mapResult = addMapDouble(mapResult, mapTmp);	
		}
		return mapResult;
	}
	
	protected int getSkipNumAll() {
		GffGene gffDetailGene = exonCluster.getParentGene();
		HashSet<String> setLocation = new HashSet<String>();
		setLocation.addAll(getSkipExonLoc_From_IsoHaveExon());
		setLocation.addAll(getSkipExonLoc_From_IsoWithoutExon());
		int numAll = 0;
		for (String string : setLocation) {
			String[] ss = string.split(SepSign.SEP_ID);
			numAll += tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), gffDetailGene.getRefID(),
					Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
		}
		return numAll;
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
			GffIso gffGeneIsoInfo = lsExonInfos.get(0).getParent();
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
	private HashSet<String> getSkipExonLoc_From_IsoWithoutExon() {
		HashSet<String> setLocation = new HashSet<String>();
		
		Map<GffIso, Integer> hashTmp = exonCluster.getMapIso2ExonIndexSkipTheCluster();
		for (Entry<GffIso, Integer> entry : hashTmp.entrySet()) {
			GffIso gffGeneIsoInfo = entry.getKey();
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
