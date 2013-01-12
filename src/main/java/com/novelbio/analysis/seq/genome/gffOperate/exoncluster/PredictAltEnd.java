package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;

public class PredictAltEnd {


	ExonCluster exonCluster;
	ArrayList<ArrayList<ExonInfo>> lsExonThis;
	Boolean isAltEnd = null;
	ArrayList<Align> lsSite;
	
	public PredictAltEnd(ExonCluster exonCluster) {
		this.exonCluster = exonCluster;
	}

	public boolean isAltEnd() {
		if (isAltEnd != null) {
			return isAltEnd;
		}
		
		if (isAfterNotSame()) {
			find();
		}
		
		if (lsSite == null || lsSite.size() == 0) {
			isAltEnd = false;
		} else {
			isAltEnd = true;
		}
		return isAltEnd;
	}
	
	private boolean isAfterNotSame() {
		ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
		return exonClusterAfter != null && !exonClusterAfter.isSameExon();
	}
	
	/**
	 * 看本位点是否能和前一个exon组成mutually exclusivelsIsoExon
	 * 并且填充lsExonThisBefore和lsExonBefore
	 */
	private void find() {
		lsSite = new ArrayList<Align>();
		for (ArrayList<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() > 0) {
				GffGeneIsoInfo gffGeneIsoInfo = lsExonInfo.get(0).getParent();
				if (lsExonInfo.get(lsExonInfo.size() - 1).getItemNum() == gffGeneIsoInfo.size() - 1) {
					int start = lsExonInfo.get(0).getStartCis();
					int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
					Align align = new Align(exonCluster.getChrID(), start, end);
					lsSite.add(align);
				}
			}
		}
	}

}
