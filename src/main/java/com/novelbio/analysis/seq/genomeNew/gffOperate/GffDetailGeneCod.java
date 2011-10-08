package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.analysis.annotation.copeID.CopedID;

public class GffDetailGeneCod extends GffDetailAbsCod<GffDetailGene>{
	public void setCoord(int coord) {
		super.setCoord(coord);
		ArrayList<GffGeneIsoInfo> lsGffInfo = getLsCodSplit();
		if (lsGffInfo == null || lsGffInfo.size() < 1) {
			return;
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffInfo) {
			gffGeneIsoInfo.setCoord(coord);
		}
	}
	/**
	 * 是否在该基因内，具体情况
	 * @return
	 * 返回anno[4]
	 * 0：accID
	 * 1：symbol
	 * 2：description
	 * 3：location
	 * 没有就返回“”
	 */
	public String[] getInfo() {
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++) {
			anno[i] = "";
		}
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		if (isCodInGenExtend()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetail.getLsCodSplit()) {
				if (gffGeneIsoInfo.isCodInIsoExtend()) {
					hashCopedID.add(gffGeneIsoInfo.getCopedID());
				}
			}
			for (CopedID copedID : hashCopedID) {
				if (anno.equals("")) {
					anno[0] = copedID.getAccID();
					anno[1] = copedID.getSymbo();
					anno[2] = copedID.getDescription();
				}
				else {
					anno[0] = anno[0]+"//"+copedID.getAccID();
					anno[1] = anno[1]+"//"+copedID.getSymbo();
					anno[2] = anno[2]+"//"+copedID.getDescription();
				}
			}
			if (gffDetail.getLongestSplit().isCodInIsoExtend()) {
				anno[4] = gffDetail.getLongestSplit().getCodLocStr();
			}
			else {
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetail.getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend()) {
						anno[4] = gffGeneIsoInfo.getCodLocStr();
						break;
					}
				}
			}
		}
		return anno;
	}
}
