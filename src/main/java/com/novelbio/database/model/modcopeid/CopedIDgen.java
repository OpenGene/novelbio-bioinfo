package com.novelbio.database.model.modcopeid;
import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.model.modgo.GOInfoGenID;
import com.novelbio.database.service.servgeneanno.ServGeneInfo;
import com.novelbio.database.service.servgeneanno.ServNCBIID;

public class CopedIDgen extends CopedIDAbs{
	ServGeneInfo servGeneInfo = new ServGeneInfo();
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * @param accID
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
		geneInfoq.setGeneID(geneID);geneInfoq.setTaxID(taxID);
		super.geneInfo = servGeneInfo.queryGeneInfo(geneInfoq);
	}
	
	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		int geneID = Integer.parseInt(genUniID);
		ServNCBIID servGeneAnno = new ServNCBIID();
		return servGeneAnno.queryGenUniID(geneID, taxID, dbInfo);
	}
	

	@Override
	protected void setGoInfo() {
		goInfoAbs = new GOInfoGenID(genUniID, taxID);
	}

}
