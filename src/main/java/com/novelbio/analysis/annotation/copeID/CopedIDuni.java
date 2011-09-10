package com.novelbio.analysis.annotation.copeID;
import java.util.ArrayList;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.AgeneUniID;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.entity.friceDB.UniProtID;

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
		geneInfo = DaoFSUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}

	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		UniProtID uniProtID = new UniProtID();
		uniProtID.setUniID(genUniID);uniProtID.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			uniProtID.setDBInfo(dbInfo);
		}
		ArrayList<UniProtID> lsuniProtIDs= DaoFSUniProtID.queryLsUniProtID(uniProtID);
		if (lsuniProtIDs == null || lsuniProtIDs.size() < 1) {
			return null;
		}
		else {
			return lsuniProtIDs.get(0);
		}
	}

}
