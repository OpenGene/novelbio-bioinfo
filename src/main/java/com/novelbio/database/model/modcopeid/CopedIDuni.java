package com.novelbio.database.model.modcopeid;
import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapUniGeneInfo;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.model.modgo.GOInfoUniID;

public class CopedIDuni extends CopedIDAbs{
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * 
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
	 */
	public CopedIDuni(String accID,String idType, String genUniID, int taxID) {
		this.accID = accID;
		this.genUniID = genUniID;
		this.idType = idType;
		this.taxID = taxID;
	}
	protected void setGenInfo() {
		UniGeneInfo uniGeneInfo = new UniGeneInfo();
		uniGeneInfo.setUniProtID(getGenUniID());
		geneInfo = MapUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}

	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		UniProtID uniProtID = new UniProtID();
		uniProtID.setUniID(genUniID);uniProtID.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			uniProtID.setDBInfo(dbInfo);
		}
		ArrayList<UniProtID> lsuniProtIDs= MapUniProtID.queryLsUniProtID(uniProtID);
		if (lsuniProtIDs == null || lsuniProtIDs.size() < 1) {
			return null;
		}
		else {
			return lsuniProtIDs.get(0);
		}
	}
	@Override
	protected void setGoInfo() {
		goInfoAbs = new GOInfoUniID(genUniID, taxID);
	}

}
