package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;

public class GeneID2LsPath extends GeneID2LsItem {
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGenUniID();
		ArrayList<KGpathway> lsPath = geneID.getKegPath(blast);
		for (KGpathway kGpathway : lsPath) {
			setItemID.add(kGpathway.getPathName());
		}
	}
}
