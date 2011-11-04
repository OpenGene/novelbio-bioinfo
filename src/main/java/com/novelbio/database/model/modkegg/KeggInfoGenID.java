package com.novelbio.database.model.modkegg;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.domain.kegg.KGCgen2Entry;
import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.mapper.kegg.MapKIDKeg2Ko;
import com.novelbio.database.mapper.kegg.MapKIDgen2Keg;

public class KeggInfoGenID extends KeggInfoAbs{
	
	public KeggInfoGenID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
		// TODO Auto-generated constructor stub
	}

	boolean bookgiDgen2Keg = false;
	/**
	 * 这个数据库中就是一一对应的关系
	 */
	private KGIDgen2Keg kgiDgen2Keg;
	
	long geneID = Long.parseLong(getGenUniID());
	
	@Override
	protected void setKeggID() {
		if (!bookgiDgen2Keg) {
			bookgiDgen2Keg = true;
			KGIDgen2Keg kgiDgen2KegTmp = new KGIDgen2Keg();
			kgiDgen2KegTmp.setGeneID(geneID);
			kgiDgen2Keg = MapKIDgen2Keg.queryKGIDgen2Keg(kgiDgen2KegTmp);
		}
		if (kgiDgen2Keg != null) {
			keggID = kgiDgen2Keg.getKeggID();
		}
	}

	
	
}
