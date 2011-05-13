package com.novelbio.database.DAO;

import java.util.ArrayList;

import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;

public class SvDBNCBIUni {
	/**
	 * 给定NCBIID，首先用taxID和accID在NCBIID表中检查是否存在，如果存在是否修改DBInfo，也就是是否升级DBinfo
	 * 这时候geneID是从数据库中获得的，所以不会改变geneID
	 * 没有的话就插入NCBIID
	 * @param ncbiid 全部填满的ncbiid
	 */
	public static void upDateNCBIUni(NCBIID ncbiid,boolean updateDBinfo) {
		NCBIID ncbiid2 = new NCBIID();
		ncbiid2.setAccID(ncbiid.getAccID());
		ncbiid2.setTaxID(ncbiid.getTaxID());
		ArrayList<NCBIID> lsNcbiids = DaoFSNCBIID.queryLsNCBIID(ncbiid2);
		if (lsNcbiids == null || lsNcbiids.size() == 0) {
			DaoFSNCBIID.InsertNCBIID(ncbiid);
		}
		else {
			if (updateDBinfo) {
				ncbiid.setGeneId(lsNcbiids.get(0).getGeneId());
				DaoFSNCBIID.upDateNCBIID(ncbiid);
			}
		}
	}
	
	/**
	 * 给定UniProtID，首先用taxID和accID在UniProt表中检查是否存在，如果存在是否修改DBInfo，也就是是否升级DBinfo
	 * 这时候UniID是从数据库中获得的，所以不会改变UniID
	 * 没有的话就插入UniProtID
	 * @param uniprotID 全部填满的uniprotID
	 */
	public static void upDateNCBIUni(UniProtID uniprotID,boolean updateDBinfo) {
		UniProtID uniprotID2 = new UniProtID();
		uniprotID2.setAccID(uniprotID.getAccID());
		uniprotID2.setTaxID(uniprotID.getTaxID());
		ArrayList<UniProtID> lsUniProtIDs = DaoFSUniProtID.queryLsUniProtID(uniprotID2);
		if (lsUniProtIDs == null || lsUniProtIDs.size() == 0) {
			DaoFSUniProtID.InsertUniProtID(uniprotID);
		}
		else {
			if (updateDBinfo) {
				uniprotID.setUniID(lsUniProtIDs.get(0).getUniID());
				DaoFSUniProtID.upDateUniProt(uniprotID);
			}
		}
	}
	
	
}
