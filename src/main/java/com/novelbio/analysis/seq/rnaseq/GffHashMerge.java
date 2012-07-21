package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListGff;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListAbs;

public class GffHashMerge {
	GffHashGene gffHashGeneRef = new GffHashGene();
	ArrayList<GffHashGene> lsGffHashGenes = new ArrayList<GffHashGene>();
	HashMap<String, ArrayList<GffGeneClusterNew>> mapChrID2LsGffGeneCluster = new HashMap<String, ArrayList<GffGeneClusterNew>>();
	
	/**
	 * 添加Gff信息
	 * @param gffHashGene
	 */
	public void addGffHashGene(GffHashGene gffHashGene) {
		lsGffHashGenes.add(gffHashGene);
	}
	/**
	 * 将Gff信息装入mapChrID2LsGffGeneCluster
	 */
	public void getMapChrID2LsGffGeneCluster() {
		ArrayList<String> lsChrID = gffHashGeneRef.getLsChrID();
		for (String chrID : lsChrID) {
			ArrayList<ListGff> lsGffAll = new ArrayList<ListGff>();
			for (GffHashGene gffHashGene : lsGffHashGenes) {
				ListGff listGff = gffHashGene.getChrhash().get(chrID);
				lsGffAll.add(listGff);
			}
			ArrayList<int[]> lsGeneBound = ListAbs.getCombSep(null, lsGffAll);
			setListGeneCluster(chrID, lsGeneBound, lsGffHashGenes);
		}
	}
	/**
	 * 将某一条染色体的所有gffhashgene的信息按照划分的区域装入mapChrID2LsGffGeneCluster
	 * @param chrID
	 * @param lsGeneBount
	 * @param lsGffHashGenes
	 */
	private void setListGeneCluster(String chrID, ArrayList<int[]> lsGeneBound, ArrayList<GffHashGene> lsGffHashGenes) {
		ArrayList<GffGeneClusterNew> lsGffGeneClusters = getLsClusterFromMapLsGeneCluster(chrID);
		for (int[] geneBound : lsGeneBound) {
			GffGeneClusterNew gffGeneCluster = new GffGeneClusterNew();
			for (int i = 0; i < lsGffHashGenes.size(); i++) {
				GffHashGene gffHashGene = lsGffHashGenes.get(i);
				GffCodGene gffCodGeneStart = gffHashGene.searchLocation(chrID, geneBound[0]);
				int startID = getGffGeneNum(true, gffCodGeneStart);
				
				GffCodGene gffCodGeneEnd = gffHashGene.searchLocation(chrID, geneBound[1]);
				int endID = getGffGeneNum(false, gffCodGeneEnd);
				//起点大于终点说明在该位置区间里面，本GffHashGene中没有找到基因
				if (startID > endID || startID < 0 || endID < 0) {
					if (i == 0) {
						gffGeneCluster.setIsContainsRef(false);
					}
					continue;
				}
				addGffGene_Into_GffCluster(gffGeneCluster, gffHashGene, chrID, startID, endID);
			}
			lsGffGeneClusters.add(gffGeneCluster);
		}
	}
	
	private ArrayList<GffGeneClusterNew> getLsClusterFromMapLsGeneCluster(String chrID) {
		ArrayList<GffGeneClusterNew> lsgffGeneClusters;
		if (mapChrID2LsGffGeneCluster.containsKey(chrID)) {
			lsgffGeneClusters = mapChrID2LsGffGeneCluster.get(chrID);
		}
		else {
			lsgffGeneClusters = new ArrayList<GffGeneClusterNew>();
			mapChrID2LsGffGeneCluster.put(chrID, lsgffGeneClusters);
		}
		return lsgffGeneClusters;
	}
	
	private int getGffGeneNum(boolean start, GffCodGene gffCodGene) {
		if (gffCodGene == null) {
			return -1;
		}
		if (gffCodGene.isInsideLoc()) {
			return gffCodGene.getItemNumThis();
		}
		//头部的位点就看后面一个ID，尾部的位点就看前面一个ID
		if (start) {
			return gffCodGene.getItemNumDown();
		}
		else {
			return gffCodGene.getItemNumUp();
		}
	}
	/**
	 * 将指定范围内的gffGene装入cluster中
	 * @param gffHashGeneBed
	 * @param chrID
	 * @param startLoc 从0开始
	 * @param endLoc 从0开始
	 */
	private void addGffGene_Into_GffCluster(GffGeneClusterNew gffGeneCluster, GffHashGene gffHashGene, String chrID, int startID, int endID) {
		ListGff lsGff = gffHashGene.getChrhash().get(chrID);
		ArrayList<GffDetailGene> lsGffSubGene = new ArrayList<GffDetailGene>();
		for (int i = startID; i < endID; i++) {
			lsGffSubGene.add(lsGff.get(i));
		}
		if (lsGffSubGene.size() == 0) {
			return;
		}
		gffGeneCluster.addLsGffDetailGene(gffHashGene.getGffFilename(), lsGffSubGene);
	}
	
}
