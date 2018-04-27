package com.novelbio.database.domain.modkegg;

import java.util.ArrayList;

import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.kegg.KGIDgen2Keg;
import com.novelbio.database.model.kegg.KGpathway;

public class KeggInfoUniID extends KeggInfoAbs{
	
	public KeggInfoUniID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
		// TODO Auto-generated constructor stub
	}
	boolean bookgiDgen2Keg = false;
	/**
	 * 这个数据库中就是一一对应的关系
	 */
	private KGIDgen2Keg kgiDgen2Keg;
	/**
	 * 暂时UniID没有对应到KEGG上，所以遇到UniID就略过
	 */
	@Override
	protected void setKeggID() {
		
	}

}
