package com.novelbio.software.rnaaltersplice.splicetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.data.RangeType;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.AlignRecord;
import com.novelbio.bioinfo.gff.ExonCluster;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.sam.AlignmentRecorder;
import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamRecord;

/**
 * 获取getJuncCounts时需要设定
 * setMapCond2Samfile方法
 * @author zong0jie
 *
 */
public class PredictRetainIntron extends SpliceTypePredict implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(PredictRetainIntron.class);
	Align alignRetain;
	GffIso gffIsoRetain;
	
	String condition;
	String group;
	/** 覆盖retain intron区域的reads */
	List<Double> lsRetainReadsCover = new ArrayList<>();
	double junCountsTmp = 0;
	Map<String, ArrayListMultimap<String, Double>> mapCondition2Counts = new HashMap<>();
	
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
	
	@Override
	//TODO 待修该
	protected ArrayListMultimap<String, Double> getLsJuncCounts(String condition) {
		 ArrayListMultimap<String, Double> mapGroup2LsValue = mapCondition2Counts.get(condition);
		return mapGroup2LsValue;
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
		ArrayListMultimap<String, Double> mapGroup2LsValue = null;
		if (mapCondition2Counts.containsKey(condition)) {
			mapGroup2LsValue = mapCondition2Counts.get(condition);
		} else {
			mapGroup2LsValue = ArrayListMultimap.create();
			mapCondition2Counts.put(condition, mapGroup2LsValue);
		}
		
		mapGroup2LsValue.put(group, junCountsTmp);
		mapGroup2LsValue.put(group, tophatJunction.getJunctionSite(condition, group, exonCluster.isCis5to3(), exonCluster.getRefID(), alignRetain.getStartCis(), alignRetain.getEndCis()));
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
	
	public List<Align> getDifSite() {
		getJunctionSite();
		List<Align> lsAligns = new ArrayList<>();
		lsAligns.add(alignRetain);
		return lsAligns;
	}
	
	/** 把两边的区域挑出来 */
	@Override
	public List<? extends Alignment> getBGSiteSplice() {
		List<Alignment> lsAlignments = new ArrayList<Alignment>();
		int startBGAbs = exonCluster.getStartAbs();
		int endBGAbs = exonCluster.getEndAbs();
		Align align = getDifSite().get(0);
		int startSplitAbs = align.getStartAbs();
		int endSplitAbs = align.getEndAbs();
		Align alignLeft = new Align(exonCluster.getRefID(), startBGAbs, startSplitAbs);
		alignLeft.setCis5to3(exonCluster.isCis5to3());
		Align alignRight = new Align(exonCluster.getRefID(), endSplitAbs, endBGAbs);
		alignRight.setCis5to3(exonCluster.isCis5to3());
		
		if (exonCluster.isCis5to3()) {
			lsAlignments.add(alignLeft);
			lsAlignments.add(alignRight);
		} else {
			lsAlignments.add(alignRight);
			lsAlignments.add(alignLeft);
		}
		
		return lsAlignments;
	}
	
	public Align getResultSite() {
		return new Align(exonCluster);
	}
	
	/** 获得差异可变剪接的区段，用于IGV查看位点 */
	public List<Align> getLsAligns() {
		List<Align> lsAligns = new ArrayList<>();
		getBGSiteSplice().forEach((alignment) -> lsAligns.add(new Align(alignment)));
		return lsAligns;
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
