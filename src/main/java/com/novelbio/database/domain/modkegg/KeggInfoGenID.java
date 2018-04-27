package com.novelbio.database.domain.modkegg;

import com.novelbio.database.model.kegg.KGIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;

public class KeggInfoGenID extends KeggInfoAbs{
	public KeggInfoGenID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
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
			ServKIDgen2Keg servKIDgen2Keg = ServKIDgen2Keg.getInstance();
			bookgiDgen2Keg = true;
			kgiDgen2Keg = servKIDgen2Keg.findByGeneId(geneID);
		}
		if (kgiDgen2Keg != null) {
			keggID = kgiDgen2Keg.getKeggID();
		}
	}

	
	
}
