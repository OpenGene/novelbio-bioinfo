package com.novelbio.database.model.modkegg;

import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;

public class KeggInfoGenID extends KeggInfoAbs{
	static ServKIDgen2Keg servKIDgen2Keg = new ServKIDgen2Keg();
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
			kgiDgen2Keg = servKIDgen2Keg.queryKGIDgen2Keg(kgiDgen2KegTmp);
		}
		if (kgiDgen2Keg != null) {
			keggID = kgiDgen2Keg.getKeggID();
		}
	}

	
	
}
