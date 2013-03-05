package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.base.dataStructure.Alignment;


/** 判定本exonCluster是否为mutually exclusive */
public class PredictME extends SpliceTypePredict {	
	ArrayList<ArrayList<ExonInfo>> lsExonBefore;
	ArrayList<ArrayList<ExonInfo>> lsExonThisBefore;//可以和前面组成mutually exclusive的exon
	ArrayList<ArrayList<ExonInfo>> lsExonThisAfter;//可以和后面组成mutually exclusive的exon
	ArrayList<ArrayList<ExonInfo>> lsExonAfter;
			
	public PredictME(ExonCluster exonCluster) {
		super(exonCluster);
	}
	@Override
	public SplicingAlternativeType getType() {
		return SplicingAlternativeType.mutually_exclusive;
	}
	public ArrayList<ArrayList<ExonInfo>> getLsExonBefore() {
		return lsExonBefore;
	}
	public ArrayList<ArrayList<ExonInfo>> getLsExonAfter() {
		return lsExonAfter;
	}
	/**
	 * 可以和后面组成mutually exclusive的exon
	 * @return
	 */
	public ArrayList<ArrayList<ExonInfo>> getLsExonThisAfter() {
		return lsExonThisAfter;
	}
	/**
	 * 可以和前面组成mutually exclusive的exon
	 * @return
	 */
	public ArrayList<ArrayList<ExonInfo>> getLsExonThisBefore() {
		return lsExonThisBefore;
	}
	public ArrayList<Align[]> getSiteInfoBefore() {
		return getSiteInfoMutually(lsExonBefore);
	}
	public ArrayList<Align[]> getSiteInfoAfter() {
		return getSiteInfoMutually(lsExonAfter);
	}
	/**
	 * 返回与前一个exon组成mutually exclusive所对应的site，前面一个和后面一个共两个<br>
	 * 如：<br>
	 * 3--4-----------5---6--------------------------------------9--10-<br>
	 * 3--4----------------------------------7--8----------------11-12<br>
	 * 返回 4-7和8-11<br>
	 * @return
	 */
	public ArrayList<Align[]> getSiteInfoThisBefore() {
		return getSiteInfoMutually(lsExonThisBefore);
	}
	/**
	 * 返回与后一个exon组成mutually exclusive所对应的site，前面一个和后面一个共两个<br>
	 * 如：<br>
	 * 3--4-----------5---6--------------------------------------9--10-<br>
	 * 3--4----------------------------------7--8----------------11-12<br>
	 * 返回 4-5和6-9<br>
	 * @return
	 */
	public ArrayList<Align[]> getSiteInfoThisAfter() {
		return getSiteInfoMutually(lsExonThisAfter);
	}
	/**
	 * 返回与后一个exon组成mutually exclusive所对应的site，前面一个和后面一个共两个<br>
	 * 如：<br>
	 * 给定5-6<br>
	 * 3--4-----------5---6--------------------------------------9--10-<br>
	 * 3--4----------------------------------7--8----------------11-12<br>
	 * 返回 4-5和6-9<br>
	 * @return
	 */
	public ArrayList<Align[]> getSiteInfoMutually(ArrayList<ArrayList<ExonInfo>> lsExonThis) {
		ArrayList<Align[]> lsSiteInfo = new ArrayList<Align[]>();
		isType();
		if (lsExonThis.size() == 0) {
			return lsSiteInfo;
		}
		for (ArrayList<ExonInfo> lsExonInfos : lsExonThis) {
			lsSiteInfo.add(getExonBeforeAndAfter(lsExonInfos));
		}
		return lsSiteInfo;
	}
	
	/**
	 * 获得一组listExon，其前后的intron信息，用于提取junction reads
	 * @return
	 */
	//TODO 检查是否正确
	private Align[] getExonBeforeAndAfter(ArrayList<ExonInfo> lsExonInfos) {
		Align[] aligns = new Align[2];
		Align alignBefore = null;
		Align alignAfter = null;
		GffGeneIsoInfo gffGeneIsoInfo = lsExonInfos.get(0).getParent();
		int beforeEnd = lsExonInfos.get(0).getStartCis();
		int exonIndexBefore = lsExonInfos.get(0).getItemNum() - 1;
		int beforeStart = gffGeneIsoInfo.get(exonIndexBefore).getEndCis();
		alignBefore = new Align(gffGeneIsoInfo.getRefID(), beforeStart, beforeEnd);
		
		int afterStart = lsExonInfos.get(lsExonInfos.size() - 1).getEndCis();
		int exonIndexAfter = lsExonInfos.get(lsExonInfos.size() - 1).getItemNum() + 1;
		int afterEnd = gffGeneIsoInfo.get(exonIndexAfter).getStartCis();
		alignAfter = new Align(gffGeneIsoInfo.getRefID(), afterStart, afterEnd);
		
		aligns[0] = alignBefore;
		aligns[1] = alignAfter;
		return aligns;
	}
	
	protected boolean isType() {
		boolean istype = false;
		if (isBeforeNotSame()) {
			findBefore();
		}
		if (isAfterNotSame()) {
			findAfter();
		}
		if (
				(lsExonThisBefore != null && lsExonThisBefore.size() > 0)
				||
				(lsExonThisAfter != null && lsExonThisAfter.size() > 0)	
			) {
			istype = true;
		}
		return istype;
	}
	
	
	private boolean isBeforeNotSame() {
		ExonCluster exonClusterBefore = exonCluster.getExonClusterBefore();
		return exonClusterBefore != null && !exonClusterBefore.isSameExon();
	}
	
	private boolean isAfterNotSame() {
		ExonCluster exonClusterAfter = exonCluster.getExonClusterAfter();
		return exonClusterAfter != null && !exonClusterAfter.isSameExon();
	}
	
	/**
	 * 看本位点是否能和前一个exon组成mutually exclusivelsIsoExon
	 * 并且填充lsExonThisBefore和lsExonBefore
	 */
	private void findBefore() {
		lsExonThisBefore = getLsExonThis(exonCluster.getExonClusterBefore());
		if (lsExonThisBefore.size() == 0) {
			return;
		}
		lsExonBefore = getLsExonsBeforeAfter(exonCluster.getExonClusterBefore());
		if (lsExonBefore.size() == 0) {
			lsExonThisBefore.clear();
		}
	}
	
	/**
	 * 看本位点是否能和前一个exon组成mutually exclusive
	 * 并且填充lsExonThisAfter和lsExonAfter
	 */
	private void findAfter() {
		lsExonThisAfter = getLsExonThis(exonCluster.getExonClusterAfter());
		if (lsExonThisAfter.size() == 0) {
			return;
		}
		lsExonAfter = getLsExonsBeforeAfter(exonCluster.getExonClusterAfter());
				
		if (lsExonAfter.size() == 0) {
			lsExonThisAfter.clear();
		}
	}
	
	private ArrayList<ArrayList<ExonInfo>> getLsExonsBeforeAfter(ExonCluster exonClusterBeforeOrAfter ) {
		ArrayList<ArrayList<ExonInfo>> lsExonBeforeOrAfter = new ArrayList<ArrayList<ExonInfo>>();
		
		for (GffGeneIsoInfo gffGeneIsoInfo : exonCluster.getMapIso2ExonIndexSkipTheCluster().keySet()) {
			ArrayList<ExonInfo> lsExons = exonClusterBeforeOrAfter.getMapIso2LsExon().get(gffGeneIsoInfo);
			if (lsExons != null && lsExons.size() > 0) {
				//并且不是本iso的最后一个exon
				if (lsExons.get(lsExons.size() - 1).getItemNum() != gffGeneIsoInfo.size() - 1 && lsExons.get(0).getItemNum() != 0)  {
					lsExonBeforeOrAfter.add(lsExons);
				}
			}
		}
		return lsExonBeforeOrAfter;
	}
	
	/**
	 * 判断本位点是否有符合mutually exclusive特征的exon，有的话加入lsExonThis
	 * 主要就是在本位点存在exon，而在上一个exoncluster或下一个exoncluster不存在exon
	 * @return
	 */
	private ArrayList<ArrayList<ExonInfo>> getLsExonThis(ExonCluster exonClusterBeforeOrAfter) {
		ArrayList<ArrayList<ExonInfo>> lsExonThis = new ArrayList<ArrayList<ExonInfo>>();
		
		for (ArrayList<ExonInfo> lsExonInfo : exonCluster.getLsIsoExon()) {
			if (lsExonInfo.size() == 0) {
				continue;
			}
			GffGeneIsoInfo gffGeneIsoInfo = lsExonInfo.get(0).getParent();
			if (lsExonInfo.get(0).getItemNum() == 0 
					|| lsExonInfo.get(lsExonInfo.size() - 1).getItemNum() == gffGeneIsoInfo.size() - 1) {
				continue;
			}
			
			ArrayList<ExonInfo> lsExons = exonClusterBeforeOrAfter.getMapIso2LsExon().get(gffGeneIsoInfo);
			if (lsExons != null && lsExons.size() == 0 ) {
				lsExonThis.add(lsExonInfo);
			}
		}
		return lsExonThis;
	}

	/**
	 * 用于mutually exclusive检测
	 * @param chrID
	 * @param condition
	 * @param tophatJunction
	 * @return
	 */
	public ArrayList<Double> getJuncCounts(String condition) {
		ArrayList<Double> lsCounts = new ArrayList<Double>();
		if (lsExonThisBefore != null && lsExonThisBefore.size() > 0) {
			lsCounts.add((double) getJuncNum(true, getSiteInfoThisBefore(), condition, tophatJunction));
			lsCounts.add((double) getJuncNum(false, getSiteInfoBefore(), condition, tophatJunction));
		}
		if (lsExonThisAfter != null && lsExonThisAfter.size() > 0) {
			lsCounts.add((double) getJuncNum(false, getSiteInfoThisAfter(), condition, tophatJunction));
			lsCounts.add((double) getJuncNum(true, getSiteInfoAfter(), condition, tophatJunction));
		}
		return lsCounts;
	}
	/** 输入的是
	 * 一个exon组成mutually exclusive所对应的site，前面一个和后面一个共两个<br>
	 * 如：<br>
	 * 3--4-----------5---6--------------------------------------9--10-<br>
	 * 3--4----------------------------------7--8----------------11-12<br>
	 * 输入4-5和6-9<br>
	 * @param before true选取4-5，false选取6-9
	 * 
	 */
	private static int getJuncNum(boolean before, ArrayList<Align[]> lsAligns, String condition, TophatJunction tophatJunction) {
		int num = 0;
		for (Align[] aligns : lsAligns) {
			if (before) {
				num = tophatJunction.getJunctionSite(condition,aligns[0].getRefID(), aligns[0].getStartAbs(), aligns[0].getEndAbs());
			} else {
				num = tophatJunction.getJunctionSite(condition,aligns[1].getRefID(), aligns[1].getStartAbs(), aligns[1].getEndAbs());
			}
		}
		return num;
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