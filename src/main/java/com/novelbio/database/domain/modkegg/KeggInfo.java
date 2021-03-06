package com.novelbio.database.domain.modkegg;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.kegg.KGentry;
import com.novelbio.database.model.kegg.KGpathway;

public class KeggInfo implements KeggInfoInter{
	static KeggInfoAbs keggInfoAbs;
	
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
	public List<KGentry> getKgGentries() {
		return keggInfoAbs.getKgGentries();
	}

	@Override
	public List<String> getLsKo() {
		return keggInfoAbs.getLsKo();
	}

	@Override
	public ArrayList<KGpathway> getLsKegPath() {
		return keggInfoAbs.getLsKegPath();
	}

	public static ArrayList<KGpathway> getLsKegPath(ArrayList<? extends KeggInfoInter> ls_keggInfo, int taxID) {
		return KeggInfoAbs.getLsKegPath(ls_keggInfo, taxID);
	}

	public static ArrayList<KGentry> getLsKgGentries(ArrayList<? extends KeggInfoInter> ls_keggInfo) {
		return KeggInfoAbs.getLsKgGentries(ls_keggInfo);
	}
	
	public static KGpathway getKGpathway(String pathID) {
		return KeggInfoAbs.getHashKGpath().get(pathID);
	}
	
	
	
}
