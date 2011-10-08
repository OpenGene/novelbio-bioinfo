package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.novelbio.analysis.annotation.copeID.CopedID;

public class GffCodGeneDU extends GffCodAbsDu<GffDetailGene, GffCodGene>{

	public GffCodGeneDU(ArrayList<GffDetailGene> lsgffDetail,
			GffCodGene gffCod1, GffCodGene gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}
	
	/**
	 * 将这两个端点中，涉及到Tss的基因全部提取出来
	 * @return
	 */
	public Set<GffDetailGene> getTSSGene() {
		/**
		 * 待返回的基因
		 */
		Set<GffDetailGene> setGffDetailGenes = new HashSet<GffDetailGene>();
		//在前面基因的最长转录本范围内
		if (gffCod1 != null)
		{
			//上一个基因有关系
			if (isUpTss(gffCod1.getGffDetailUp())) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			//本基因关系
			if (isUpTss(gffCod1.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		
		for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
			setGffDetailGenes.add(gffDetailGene);
		}
		
		if (gffCod2 != null)
		{
			//上一个基因有关系
			if (isDownTss(gffCod2.getGffDetailThis()))
			{
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			//本基因关系
			if (isDownTss(gffCod2.getGffDetailDown())) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	
	private boolean isUpTss(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoTss()
		||
		(!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend())
		)
		{
			return true;
		}
		return false;
	}
	
	private boolean isDownTss(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoTss()
		||
		(gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend())
		)
		{
			return true;
		}
		return false;
	}
	/**
	 * 将这两个端点中，涉及到Tes的基因全部提取出来
	 * @return
	 */
	public Set<GffDetailGene> getTESGene() {
		/**
		 * 待返回的基因
		 */
		Set<GffDetailGene> setGffDetailGenes = new HashSet<GffDetailGene>();
		//在前面基因的最长转录本范围内
		if (gffCod1 != null)
		{
			//上一个基因有关系
			if (isUpTes(gffCod1.getGffDetailUp())) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			//本基因关系
			if (isUpTes(gffCod1.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		
		for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
			setGffDetailGenes.add(gffDetailGene);
		}
		
		if (gffCod2 != null)
		{
			//上一个基因有关系
			if (isDownTes(gffCod2.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			//本基因关系
			if (isDownTes(gffCod2.getGffDetailDown())) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	
	private boolean isUpTes(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend()
		||
		(!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoGenEnd() )
		)
		{
			return true;
		}
		return false;
	}
	
	private boolean isDownTes(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend()
		||
		(gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoGenEnd())
		)
		{
			return true;
		}
		return false;
	}
	/**
	 * 返回所有覆盖到的基因的copedID
	 * @return
	 */
	public ArrayList<CopedID> getAllCoveredGenes() {
		//用来去冗余的
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		ArrayList<CopedID> lsCopedIDs = new ArrayList<CopedID>();
		if (getGffCodLeft() != null && getGffCodLeft().isInsideLoc()) {
			if (getGffCodLeft().isInsideUp())
			{
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft().getGffDetailUp().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
						CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), getGffCodLeft().getGffDetailUp().getTaxID(), false);
						if (hashCopedID.contains(copedID)) {
							continue;
						}
						hashCopedID.add(copedID);
						lsCopedIDs.add(copedID);
					}
				}
			}
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft().getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
					CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), getGffCodLeft().getGffDetailThis().getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}
		
		if (getLsGffDetailMid() != null) {
			for (GffDetailGene gffDetailGene : getLsGffDetailMid()) {
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					// 看是否真正的落在该基因内部
					CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), gffDetailGene.getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}
		
		
		if (getGffCodRight() != null && getGffCodRight().isInsideLoc()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight().getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
					CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), getGffCodRight().getGffDetailThis().getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
			if (getGffCodRight().isInsideDown())
			{
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight().getGffDetailDown().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
						CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), getGffCodRight().getGffDetailDown().getTaxID(), false);
						if (hashCopedID.contains(copedID)) {
							continue;
						}
						hashCopedID.add(copedID);
						lsCopedIDs.add(copedID);
					}
				}
			}
		}
		
	 
		return lsCopedIDs;
	}
}
