package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.entity.kegg.KGpathway;

public class KeggInfo implements KeggInfoInter{

	KeggInfoAbs keggInfoAbs;
	public KeggInfo(String idType, String genUniAccID, int taxID)
	{
		if (idType.equals(CopedID.IDTYPE_UNIID)) {
			keggInfoAbs = new KeggInfoUniID(genUniAccID, taxID);
		}
		else if (idType.equals(CopedID.IDTYPE_GENEID)) {
			keggInfoAbs = new KeggInfoGenID(genUniAccID, taxID);
		}
		else if (idType.equals(CopedID.IDTYPE_ACCID)) {
			keggInfoAbs = new KeggInfoAccID(genUniAccID, taxID);
		}
	}
	@Override
	public String getGenUniID() {
		return keggInfoAbs.getGenUniID();
	}

	@Override
	public String getKegID() {
		return keggInfoAbs.getKegID();
	}

	@Override
	public int getTaxID() {
		return keggInfoAbs.getTaxID();
	}

	@Override
	public ArrayList<KGentry> getKgGentries() {
		return keggInfoAbs.getKgGentries();
	}

	@Override
	public ArrayList<String> getLsKo() {
		return keggInfoAbs.getLsKo();
	}

	@Override
	public ArrayList<KGentry> getBlastQInfo(List<String> lsKO) {
		return keggInfoAbs.getBlastQInfo(lsKO);
	}

	@Override
	public ArrayList<KGpathway> getLsKegPath() {
		return keggInfoAbs.getLsKegPath();
	}

}
