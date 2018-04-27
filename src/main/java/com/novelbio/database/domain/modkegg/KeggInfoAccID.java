package com.novelbio.database.domain.modkegg;

import java.util.ArrayList;

import com.novelbio.database.model.kegg.noGene.KGNIdKeg;
import com.novelbio.database.service.servkegg.ServKNIdKeg;

public class KeggInfoAccID extends KeggInfoAbs{
	
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
		ServKNIdKeg servKNIdKeg = ServKNIdKeg.getInstance();
		
		if (!bookgiDgen2Keg) {
			bookgiDgen2Keg = true;
			kgnIdKeg = servKNIdKeg.findByUsualName(genUniAccID);
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
