package com.novelbio.database.model.modkegg;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modcopeid.CopedID;

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
	public ArrayList<KGpathway> getLsKegPath() {
		return keggInfoAbs.getLsKegPath();
	}
	@Override
	public ArrayList<KGpathway> getLsKegPath(ArrayList<? extends KeggInfoInter> ls_keggInfo) {
		return keggInfoAbs.getLsKegPath(ls_keggInfo);
	}
	@Override
	public ArrayList<KGentry> getLsKgGentries(ArrayList<? extends KeggInfoInter> ls_keggInfo) {
		return keggInfoAbs.getLsKgGentries(ls_keggInfo);
	}
	
	public static KGpathway getKGpathway(String pathID)
	{
		return KeggInfoAbs.getHashKGpath().get(pathID);
	}
	
	
	
}
