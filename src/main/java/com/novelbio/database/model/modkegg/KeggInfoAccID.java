package com.novelbio.database.model.modkegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;
import com.novelbio.database.service.servkegg.ServKNIdKeg;

public class KeggInfoAccID extends KeggInfoAbs{
	static ServKNIdKeg servKNIdKeg = new ServKNIdKeg();
	public KeggInfoAccID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
		// TODO Auto-generated constructor stub
	}

	boolean bookgiDgen2Keg = false;
	/**
	 * 这个数据库中就是一一对应的关系
	 */
	private KGNIdKeg kgnIdKeg;
	
	@Override
	protected void setKeggID() {
		if (!bookgiDgen2Keg) {
			bookgiDgen2Keg = true;
			KGNIdKeg kgnIdKegTmp = new KGNIdKeg();
			kgnIdKegTmp.setUsualName(genUniAccID);
			kgnIdKeg = servKNIdKeg.queryKGNIdKeg(kgnIdKegTmp);
		}
		if (kgnIdKeg != null) {
			keggID = "cpd:"+kgnIdKeg.getKegID();
		}
	}
	
	/**
	 * 理论上accID只有希望对应component
	 * 只返回null
	 */
	public ArrayList<String> getLsKo()
	{
		return null;
	}


}
