package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.List;


import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;

/**
 * 获取getJuncCounts时需要设定
 * setMapCond2Samfile方法
 * @author zong0jie
 *
 */
public class PredictRetainIntron extends SpliceTypePredict {
	Align alignRetain;
	GffGeneIsoInfo gffIsoRetain;
	ArrayListMultimap<String, SamFile> mapCond2Samfile;
	public PredictRetainIntron(ExonCluster exonCluster) {
		super(exonCluster);
	}
	
	public void setMapCond2Samfile(ArrayListMultimap<String, SamFile> mapCond2Samfile) {
		this.mapCond2Samfile = mapCond2Samfile;
	}
	
	@Override
	public ArrayList<Double> getJuncCounts(String condition) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		getJunctionSite();
		
		lsCounts.add((double) tophatJunction.getJunctionSite(condition, exonCluster.getChrID(), alignRetain.getStartCis(), alignRetain.getEndCis()));
		List<SamFile> lsSamFile = mapCond2Samfile.get(condition);
		int throughStart = 0, throughEnd = 0;
		for (SamFile samFile : lsSamFile) {
			throughStart += getThroughSiteReadsNum(samFile, exonCluster.getChrID(), alignRetain.getStartCis());
			throughEnd += getThroughSiteReadsNum(samFile, exonCluster.getChrID(), alignRetain.getEndCis());
		}
		lsCounts.add( ((double)(throughStart + throughEnd)/2));
		
		return lsCounts;
	}
	
	private void getJunctionSite() {
		if (alignRetain != null) {
			return;
		}
		
		int maxReadsNum = -1;
		for (ArrayList<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() > 1) {
				for (int i = 0; i < lsExonInfo.size() - 1; i++) {
					int startLoc =  lsExonInfo.get(i).getEndCis();
					int endLoc = lsExonInfo.get(i+1).getStartCis();
					int readsNum = tophatJunction.getJunctionSite(exonCluster.getChrID(), startLoc, endLoc);
					if (readsNum > maxReadsNum) {
						maxReadsNum = readsNum;
						alignRetain = new Align(exonCluster.getChrID(), startLoc, endLoc);
					}
				}
			}
		}
	}
	
	/** 获得跨过该位点的readsNum */
	private int getThroughSiteReadsNum(SamFile samFile, String chrID, int site) {
		int throughSiteNum = 0;
		for (SamRecord samRecord : samFile.readLinesOverlap(chrID, site, site)) {
			List<Align> lsAligns = samRecord.getAlignmentBlocks();
			for (Align align : lsAligns) {
				if (align.getStartAbs() < site - 3 && align.getEndAbs() > site + 3) {
					throughSiteNum++;
					break;
				}
			}
		}
		return throughSiteNum;
	}
	
	public Align getDifSite() {
		getJunctionSite();
		return alignRetain;
	}
	
	/**
	 * retainIntron有两个条件：1：存在一个长的exon，2：存在两个短的exon
	 */
	@Override
	protected boolean isType() {
		//判定是否为retain intron
		boolean twoExon = false;
		boolean oneExon = false;
		for (ArrayList<ExonInfo> lsExon : exonCluster.getLsIsoExon()) {
			if (lsExon.size() > 1) {
				twoExon = true;
			} else if (lsExon.size() == 1) {
				oneExon = true;
			}
		}
		boolean isRetain = twoExon && oneExon;
		if (!isRetain) {
			return false;
		}
		return true;
	}
	
	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.retain_intron;
	}

}
