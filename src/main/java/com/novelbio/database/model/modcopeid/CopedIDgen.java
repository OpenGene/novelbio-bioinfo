package com.novelbio.database.model.modcopeid;
import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.mapper.geneanno.MapGeneInfo;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.model.modgo.GOInfoGenID;
import com.novelbio.database.service.servgeneanno.ServGeneAnno;

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
		super.geneInfo = MapGeneInfo.queryGeneInfo(geneInfoq);
	}
	
	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(Long.parseLong(genUniID));ncbiid.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			ncbiid.setDBInfo(dbInfo);
		}
		ArrayList<NCBIID> lsNcbiids= servGeneAnno.queryLsNCBIID(ncbiid);
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