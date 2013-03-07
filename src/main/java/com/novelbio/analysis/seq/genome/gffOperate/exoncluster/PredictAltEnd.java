package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeMap;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;

public class PredictAltEnd extends PredictAltStartEnd {

	
	public PredictAltEnd(ExonCluster exonCluster) {
		super(exonCluster);
	}
	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.altend;
	}
	
	protected boolean isBeforeOrAfterNotSame() {
		ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
		return exonClusterAfter != null && !exonClusterAfter.isSameExon();
	}
	
	@Override
	public ArrayList<Double> getJuncCounts(String condition) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		isType();
		HashSet<Integer> setStartSide = new HashSet<Integer>();
		for (Align align : lsSite) {
			setStartSide.add(align.getStartCis());
		}
		String chrID = lsSite.get(0).getRefID();
		for (Integer integer : setStartSide) {
			lsCounts.add((double) tophatJunction.getJunctionSite(condition, chrID, integer));
		}
		lsCounts.add((double) getJunReadsNum(condition));
		return lsCounts;
	}
	
	/**
	 * 看本位点是否能和前一个exon组成mutually exclusivelsIsoExon
	 * 并且填充lsExonThisBefore和lsExonBefore
	 */
	protected void find() {
		lsSite = new ArrayList<Align>();
		lslsExonInfos = new ArrayList<ArrayList<ExonInfo>>();
		if (exonCluster.getMapIso2ExonIndexSkipTheCluster().size() <= 0) {
			return;
		}
		for (ArrayList<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() > 0) {
				GffGeneIsoInfo gffGeneIsoInfo = lsExonInfo.get(0).getParent();
				if (lsExonInfo.get(lsExonInfo.size() - 1).getItemNum() == gffGeneIsoInfo.size() - 1) {
					int start = lsExonInfo.get(0).getStartCis();
					int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
					Align align = new Align(exonCluster.getRefID(), start, end);
					lsSite.add(align);
					lslsExonInfos.add(lsExonInfo);
				}
			}
		}
	}
	
	@Override
	public Align getDifSite() {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		isType();
		//倒序，获得junction最多的reads
		TreeMap<Integer, ArrayList<ExonInfo>> mapJuncNum2Exon = new TreeMap<Integer, ArrayList<ExonInfo>>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return -o1.compareTo(o2);
			}
		});
		
		for (ArrayList<ExonInfo> lsExonInfos : lslsExonInfos) {
			int juncReads = tophatJunction.getJunctionSite(exonCluster.getRefID(), lsExonInfos.get(lsExonInfos.size() - 1).getStartCis());
			mapJuncNum2Exon.put(juncReads, lsExonInfos);
		}
		//获得第一个
		Align align = null;
		for (Integer juncNum : mapJuncNum2Exon.keySet()) {
			ArrayList<ExonInfo> lsExonInfos = mapJuncNum2Exon.get(juncNum);
			align = new Align(exonCluster.getRefID(), lsExonInfos.get(0).getStartCis(), lsExonInfos.get(lsExonInfos.size() - 1).getEndCis());
			break;
		}		
		return align;
	}

}
