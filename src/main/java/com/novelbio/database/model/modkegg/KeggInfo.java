package com.novelbio.database.model.modkegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;

public class KeggInfo implements KeggInfoInter{

	KeggInfoAbs keggInfoAbs;
	
	public KeggInfo(int idType, String genUniAccID, int taxID) {
		if (idType == GeneID.IDTYPE_UNIID) {
			keggInfoAbs = new KeggInfoUniID(genUniAccID, taxID);
		}
		else if (idType == GeneID.IDTYPE_GENEID) {
			keggInfoAbs = new KeggInfoGenID(genUniAccID, taxID);
		}
		else if (idType == GeneID.IDTYPE_ACCID) {
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

	public static ArrayList<KGpathway> getLsKegPath(ArrayList<? extends KeggInfoInter> ls_keggInfo) {
		return KeggInfoAbs.getLsKegPath(ls_keggInfo);
	}

	public static ArrayList<KGentry> getLsKgGentries(ArrayList<? extends KeggInfoInter> ls_keggInfo) {
		return KeggInfoAbs.getLsKgGentries(ls_keggInfo);
	}
	
	public static KGpathway getKGpathway(String pathID) {
		return KeggInfoAbs.getHashKGpath().get(pathID);
	}
	
	
	
}
