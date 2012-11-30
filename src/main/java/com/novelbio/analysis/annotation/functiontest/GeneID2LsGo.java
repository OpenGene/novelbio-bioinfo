package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.model.modgeneid.GeneID;

public class GeneID2LsGo extends GeneID2LsItem {
	String GOtype;
	public void setGOtype(String gOtype) {
		GOtype = gOtype;
	}
	@Override
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGenUniID();
		ArrayList<AGene2Go> lsGo = null;
		if (blast) {
			lsGo = geneID.getGene2GOBlast(GOtype);
		} else {
			lsGo = geneID.getGene2GO(GOtype);
		}
		for (AGene2Go aGene2Go : lsGo) {
			setItemID.add(aGene2Go.getGOID());
		}
	}

}
