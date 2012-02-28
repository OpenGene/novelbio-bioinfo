package com.novelbio.analysis.seq.genomeNew.gffOperate.exoncomp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;

public class ExonComp {
	
	GffDetailGene gffDetailGene = null;
	public void setGffDetailGene(GffDetailGene gffDetailGene) {
		this.gffDetailGene = gffDetailGene;
	}
	/**
	 * 保存经过分组的exon
	 */
	ArrayList<ExonCluster> lsExonClusters = new ArrayList<ExonCluster>();
	
	/**
	 * 将一个gffDetailGene中的转录本合并起来
	 * @param gffDetailGene
	 */
	private ArrayList<ExonInfo> compIso() {
		gffDetailGene.removeDupliIso();
		ArrayList<GffGeneIsoInfo> lsIsos = gffDetailGene.getLsCodSplit();
		ArrayList<ExonInfo> lsExonAll = new ArrayList<ExonInfo>();
		//将全部的exon放在一个list里面并且排序
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
			if (gffGeneIsoInfo.isCis5to3() != gffDetailGene.isCis5to3()) {
				continue;
			}
			lsExonAll.addAll(gffGeneIsoInfo);
		}
		if (gffDetailGene.isCis5to3()) {
			Collections.sort(lsExonAll);
		}
		else {
			Collections.sort(lsExonAll);
		}
		return lsExonAll;
	}
	ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
	
	/**
	 * 将经过排序的exonlist合并，获得几个连续的exon，用于分段
	 */
	private void combExon(boolean cis5to3, ArrayList<int[]> lsAllExon)
	{
		lsExonBounder.clear();
		int[] exonOld = lsAllExon.get(0).clone(); boolean allFinal = false;//最后一个exon是否需要添加入list中
		lsExonBounder.add(exonOld);
		for (int i = 1; i < lsAllExon.size(); i++) {
			int[] exon = lsAllExon.get(i);
			if (cis5to3 )
			{
				if (exon[0] <= exonOld[1]) {
					if (exon[1] > exonOld[1]) {
						exonOld[1] = exon[1];
					}
				}
				else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			}
			else {
				if (exon[0] >= exonOld[1]) {
					if (exon[1] < exonOld[1]) {
						exonOld[1] = exon[1];
					}
				}
				else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			}
		}
	}
	/**
	 * 按照分组好的边界exon，将每个转录本进行划分
	 */
	private void setExonCluster()
	{
		ArrayList<GffGeneIsoInfo> lsIsos = gffDetailGene.getLsCodSplit();
		for (int[] exonBound : lsExonBounder) {
			ExonCluster exonCluster = new ExonCluster(gffDetailGene.getParentName(), exonBound[0], exonBound[1]);
			for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
				if (gffGeneIsoInfo.isCis5to3() != gffDetailGene.isCis5to3()) {
					continue;
				}
				
				ArrayList<ExonInfo> lsExonClusterTmp = new ArrayList<ExonInfo>();
				ArrayList<ExonInfo> lsExon = gffGeneIsoInfo;
				int beforeExonNum = 0;//如果本isoform正好没有落在bounder组中的exon，那么就要记录该isoform的前后两个exon的位置，用于查找跨过和没有跨过的exon
				boolean junc = false;//如果本isoform正好没有落在bounder组中的exon，那么就需要记录跳过的exon的位置，就将这个flag设置为true
				for (int i = 0; i < lsExon.size(); i++) {
					ExonInfo exon = lsExon.get(i);
					if (gffDetailGene.isCis5to3()) {
						if (exon.getEndCis() < exonBound[0]) {
							junc = true;
							beforeExonNum = i;
							continue;
						}
						else if (exon.getStartCis() >= exonBound[0] && exon.getEndCis() <= exonBound[1]) {
							lsExonClusterTmp.add(exon);
							junc = false;
						}
						else if (exon.getStartCis() > exonBound[1]) {
							break;
						}
					}
					else {
						if (exon.getEndCis() > exonBound[0]) {
							junc = true;
							beforeExonNum = i;
							continue;
						}
						else if (exon.getStartCis() <= exonBound[0] && exon.getEndCis() >= exonBound[1]) {
							lsExonClusterTmp.add(exon);
							junc = false;
						}
						else if (exon.getStartCis() < exonBound[1]) {
							break;
						}
					}
				}
				if (lsExonClusterTmp.size() > 0) {
					exonCluster.addExonCluster(gffGeneIsoInfo.getName(), lsExonClusterTmp);
				}
				if (junc && beforeExonNum < lsExon.size()-1) {
					exonCluster.setIsoJun(gffGeneIsoInfo.getName(), beforeExonNum);
				}
			}
			lsExonClusters.add(exonCluster);
		}
	}
	/**
	 * 返回有差异的exon系列
	 * @return
	 */
	public ArrayList<ExonCluster> getDifExonCluster() {
		ArrayList<int[]> lsAllExon = compIso();
		combExon(gffDetailGene.isCis5to3(), lsAllExon);
		setExonCluster();
		ArrayList<ExonCluster> lsDifExon = new ArrayList<ExonCluster>();
		for (ExonCluster exonClusters : lsExonClusters) {
			if (exonClusters.isSameExon()) {
				continue;
			}
			lsDifExon.add(exonClusters);
		}
		return lsDifExon;
	}
}

class ExonCluster
{
	Boolean sameExon = null;
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	public ExonCluster(String chrID, int start, int end) {
		this.chrID = chrID;
		this.startLoc = Math.min(start, end);
		this.endLoc = Math.max(start, end);
	}
	public String getLocInfo()
	{
		return chrID + ":" + startLoc + "-" + endLoc;
	}
	/**
	 * list--所有isoform
	 * list--每个isoform中该组的所有exon
	 */
	ArrayList<ArrayList<int[]>> lsExonCluster = new ArrayList<ArrayList<int[]>>();
	ArrayList<String> lsIsoName = new ArrayList<String>();
	
	/**
	 * 存储那些跳过exon的转录本，记录跳过的是哪一个exon，只记录前一个exon的位置 
	 */
	HashMap<String, Integer> hashIsoExonNum = new HashMap<String, Integer>();
	
	public void addExonCluster(String isoName, ArrayList<int[]> lsExon) {
		lsExonCluster.add(lsExon);
		lsIsoName.add(isoName);
	}

	
	public boolean isSameExon() {
		if (sameExon != null) {
			return sameExon;
		}
		//如果本组中没有exon并且也没有跨越的junction，说明本组没有可变的exon
		if (lsExonCluster.size() >= 1 && hashIsoExonNum.size() >= 1) {
			sameExon = false;
			return false;
		}
		sameExon = true;
		if (lsExonCluster.get(0).size() != 1) {
			sameExon = false;
			return false;
		}
		int[] exonOld = lsExonCluster.get(0).get(0);
		for (int i = 1; i < lsExonCluster.size(); i++) {
			if (lsExonCluster.get(i).size() != 1) {
				sameExon = false;
				break;
			}
			int[] exon = lsExonCluster.get(i).get(0);
			if (exonOld[0] != exon[0] || exonOld[1] != exon[1]) {
				sameExon = false;
				break;
			}
		}
		return sameExon;
	}
	
	public ArrayList<int[]> getCombExon()
	{
		if (lsCombExon != null) {
			return lsCombExon;
		}
		combExon();
		return lsCombExon;
	}
	
	ArrayList<int[]> lsCombExon;
	private void combExon()
	{
		lsCombExon = new ArrayList<int[]>();
		lsCombExon.clear();
		String sep = "@@";
		//用来去重复的hash表
		HashMap<String, int[]> hashExon = new HashMap<String, int[]>();
		for (ArrayList<int[]> lsExon : lsExonCluster) {
			for (int[] is : lsExon) {
				String key = is[0] + sep + is[1];
				hashExon.put(key, is);
			}
		}
		for (int[] exon : hashExon.values()) {
			lsCombExon.add(exon);
		}
	}
	/**
	 * 
	 * @param Isoname
	 * @param exonNumStart
	 */
	public void setIsoJun(String Isoname, int exonNumStart) {
		hashIsoExonNum.put(Isoname, exonNumStart);
	}
	
	public HashMap<String, Integer> getHashIsoExonNum()
	{
		return hashIsoExonNum;
	}

	
}