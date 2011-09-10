package com.novelbio.analysis.annotation.copeID;
import java.util.ArrayList;
import com.novelbio.database.DAO.FriceDAO.DaoFSGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.entity.friceDB.AgeneUniID;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.NCBIID;

public class CopedIDgen extends CopedIDAbs{
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * 
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
	 */
	public CopedIDgen(String accID, String idType, String genUniID, int taxID) {
		this.accID = accID;
		this.genUniID = genUniID;
		this.idType = idType;
		this.taxID = taxID;
	}
	
	@Override
	protected void setGenInfo() {
		GeneInfo geneInfoq = new GeneInfo();
		long geneID = Long.parseLong(getGenUniID());
		geneInfoq.setGeneID(geneID);
		geneInfo = DaoFSGeneInfo.queryGeneInfo(geneInfoq);
	}
	
	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(Long.parseLong(genUniID));ncbiid.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			ncbiid.setDBInfo(dbInfo);
		}
		ArrayList<NCBIID> lsNcbiids= DaoFSNCBIID.queryLsNCBIID(ncbiid);
		if (lsNcbiids == null || lsNcbiids.size() < 1) {
			return null;
		}
		else {
			return lsNcbiids.get(0);
		}
	}


}
