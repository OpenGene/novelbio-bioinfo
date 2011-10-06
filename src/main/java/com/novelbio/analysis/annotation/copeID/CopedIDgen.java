package com.novelbio.analysis.annotation.copeID;
import java.util.ArrayList;

import com.novelbio.analysis.annotation.GO.goEntity.GOInfoGenID;
import com.novelbio.database.DAO.FriceDAO.DaoFSGeneInfo;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.entity.friceDB.AgeneUniID;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.NCBIID;

public class CopedIDgen extends CopedIDAbs{
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * 
	 * @param idType
	 *            ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID
	 *            ����ID
	 */
	public CopedIDgen(String accID, String idType, String genUniID, int taxID) {
		super.accID = accID;
		super.genUniID = genUniID;
		super.idType = idType;
		super.taxID = taxID;
	}
	
	@Override
	protected void setGenInfo() {
		GeneInfo geneInfoq = new GeneInfo();
		long geneID = Long.parseLong(getGenUniID());
		geneInfoq.setGeneID(geneID);
		super.geneInfo = DaoFSGeneInfo.queryGeneInfo(geneInfoq);
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
	

	@Override
	protected void setGoInfo() {
		goInfoAbs = new GOInfoGenID(genUniID, taxID);
	}

}
