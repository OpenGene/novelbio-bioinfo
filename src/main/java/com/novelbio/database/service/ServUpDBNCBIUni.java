package com.novelbio.database.service;

import java.util.ArrayList;

import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniProtID;

public class ServUpDBNCBIUni {
	/**
	 * ����NCBIID��������taxID��accID��NCBIID���м���Ƿ���ڣ���������Ƿ��޸�DBInfo��Ҳ�����Ƿ�����DBinfo
	 * ��ʱ��geneID�Ǵ����ݿ��л�õģ����Բ���ı�geneID
	 * û�еĻ��Ͳ���NCBIID
	 * @param ncbiid ȫ��������ncbiid
	 * @param geneID �Ƿ�geneIDҲ��������
	 * @param updateDBinfo �����ͬ�Ƿ�����
	 */
	public static void upDateNCBIUni(NCBIID ncbiid,boolean geneID,boolean updateDBinfo) {
		NCBIID ncbiid2 = new NCBIID();
		ncbiid2.setAccID(ncbiid.getAccID());
		if (geneID) {
			ncbiid2.setGeneId(ncbiid.getGeneId());
		}
		ncbiid2.setTaxID(ncbiid.getTaxID());
		ArrayList<NCBIID> lsNcbiids = MapNCBIID.queryLsNCBIID(ncbiid2);
		if (lsNcbiids == null || lsNcbiids.size() == 0) {
			MapNCBIID.InsertNCBIID(ncbiid);
		}
		else {
			if (updateDBinfo) {
				ncbiid.setGeneId(lsNcbiids.get(0).getGeneId());
				MapNCBIID.upDateNCBIID(ncbiid);
			}
		}
	}
	
	/**
	 * ����UniProtID��������taxID��accID��UniProt���м���Ƿ���ڣ���������Ƿ��޸�DBInfo��Ҳ�����Ƿ�����DBinfo
	 * ��ʱ��UniID�Ǵ����ݿ��л�õģ����Բ���ı�UniID
	 * û�еĻ��Ͳ���UniProtID
	 * @param uniprotID ȫ��������uniprotID
	 * @param uniID �Ƿ�uniIDҲ��������
	 * @param updateDBinfo �����ͬ�Ƿ�����
	 */
	public static void upDateNCBIUni(UniProtID uniprotID,boolean uniID,boolean updateDBinfo) {
		UniProtID uniprotID2 = new UniProtID();
		uniprotID2.setAccID(uniprotID.getAccID());
		if (uniID) {
			uniprotID2.setUniID(uniprotID.getUniID());
		}
		uniprotID2.setTaxID(uniprotID.getTaxID());
		ArrayList<UniProtID> lsUniProtIDs = MapUniProtID.queryLsUniProtID(uniprotID2);
		if (lsUniProtIDs == null || lsUniProtIDs.size() == 0) {
			MapUniProtID.InsertUniProtID(uniprotID);
		}
		else {
			if (updateDBinfo) {
				uniprotID.setUniID(lsUniProtIDs.get(0).getUniID());
				MapUniProtID.upDateUniProt(uniprotID);
			}
		}
	}
	
	
}
