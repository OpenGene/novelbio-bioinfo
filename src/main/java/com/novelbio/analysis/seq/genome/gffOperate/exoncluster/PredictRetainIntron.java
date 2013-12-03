package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataStructure.Alignment;

/**
 * 获取getJuncCounts时需要设定
 * setMapCond2Samfile方法
 * @author zong0jie
 *
 */
public class PredictRetainIntron extends SpliceTypePredict implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(PredictRetainIntron.class);
	Align alignRetain;
	GffGeneIsoInfo gffIsoRetain;
	ArrayListMultimap<String, SamFile> mapCond2Samfile;
	
	String condition;
	String group;
	/** 覆盖retain intron区域的reads */
	List<Double> lsRetainReadsCover = new ArrayList<>();
	double junCountsTmp = 0;
	Map<String, List<List<Double>>> mapCondition2Counts = new HashMap<>();
	
	public PredictRetainIntron(ExonCluster exonCluster) {
		super(exonCluster);
	}
	
	/** 设定时期--不同的group组，接下来reads的添加就会设定为该时期
	 * 
	 * 同时会清空Junction的中间变量
	 * */
	public void setCondition_DifGroup(String condition, String group) {
		this.condition = condition;
		this.group = group;
		junCountsTmp = 0;
	}
	
	@Deprecated
	public void setMapCond2Samfile(ArrayListMultimap<String, SamFile> mapCond2Samfile) {
		this.mapCond2Samfile = mapCond2Samfile;
	}
	
	@Override
	//TODO 待修该
	protected List<List<Double>> getLsJuncCounts(String condition) {
		List<List<Double>> lsCounts = mapCondition2Counts.get(condition);
		return lsCounts;
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
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		if (alignRecord instanceof SamRecord == false) return;
		
		SamRecord samRecord = (SamRecord)alignRecord;
		List<Align> lsAligns = samRecord.getAlignmentBlocks();
		for (Align align : lsAligns) {
			if (align.getStartAbs() < alignRetain.getStartAbs() - 3 && align.getEndAbs() > alignRetain.getStartAbs() + 3) {
				junCountsTmp += (double)1/alignRecord.getMappedReadsWeight();
			}
			if (align.getStartAbs() < alignRetain.getEndAbs() - 3 && align.getEndAbs() > alignRetain.getEndAbs() + 3) {
				junCountsTmp += (double)1/alignRecord.getMappedReadsWeight();
				break;//先看了靠前位置的位点，又看了靠后位置的位点，所以现在可以跳出循环了
			}
		}
	}

	@Override
	public void summary() {
		List<List<Double>> lsCounts = null;
		if (mapCondition2Counts.containsKey(condition)) {
			lsCounts = mapCondition2Counts.get(condition);
		} else {
			lsCounts = new ArrayList<>();
			mapCondition2Counts.put(condition, lsCounts);
		}
		if (lsCounts.size() == 0) {
			lsCounts.add(new ArrayList<Double>());
			lsCounts.add(new ArrayList<Double>());
		}
		
		lsCounts.get(0).add(tophatJunction.getJunctionSite(condition, group, exonCluster.isCis5to3(), exonCluster.getRefID(), alignRetain.getStartCis(), alignRetain.getEndCis()));
		lsCounts.get(1).add(junCountsTmp);
	}

	@Override
	public Align getReadingRegion() {
		getJunctionSite();
		Align align = new Align(exonCluster.getRefID(), alignRetain.getStartAbs(), alignRetain.getEndAbs());
		return align;
	}
	
	private void getJunctionSite() {
		if (alignRetain != null) return;
		
		double maxReadsNum = -1;
		for (List<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() > 1) {
				for (int i = 0; i < lsExonInfo.size() - 1; i++) {
					int startLoc =  lsExonInfo.get(i).getEndCis();
					int endLoc = lsExonInfo.get(i+1).getStartCis();
					double readsNum = tophatJunction.getJunctionSiteAll(exonCluster.isCis5to3(), exonCluster.getRefID(), startLoc, endLoc);
					if (readsNum > maxReadsNum) {
						maxReadsNum = readsNum;
						alignRetain = new Align(exonCluster.getRefID(), startLoc, endLoc);
					}
				}
			}
		}
	}
	
	public Align getDifSite() {
		getJunctionSite();
		return alignRetain;
	}
	
	@Override
	public List<? extends Alignment> getBGSite() {
		List<Alignment> lsAlignments = new ArrayList<Alignment>();
		lsAlignments.add(exonCluster);
		return lsAlignments;
	}
	
	/**
	 * retainIntron有两个条件：1：存在一个长的exon，2：存在两个短的exon
	 */
	@Override
	protected boolean isType() {
		//判定是否为retain intron
		boolean twoExon = false;
		boolean oneExon = false;
		for (List<ExonInfo> lsExon : exonCluster.getLsIsoExon()) {
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
