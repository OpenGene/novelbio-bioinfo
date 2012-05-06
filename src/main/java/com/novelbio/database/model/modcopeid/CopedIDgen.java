package com.novelbio.database.model.modcopeid;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgo.GOInfoGenID;
import com.novelbio.database.service.servgeneanno.ServGeneInfo;
import com.novelbio.database.service.servgeneanno.ServNCBIID;

public class CopedIDgen extends CopedIDAbs{
	private static Logger logger = Logger.getLogger(CopedIDgen.class);
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
		this.taxID = taxID;
		if (taxID == 0 || (accID != null && !accID.equals("")) && (databaseType == null || databaseType.equals(""))) {
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(accID);
			ncbiid.setGenUniID(genUniID);
			ArrayList<NCBIID> lsTmp = servNCBIID.queryLsNCBIID(ncbiid);
			if (lsTmp.size() > 0) {
				if (taxID == 0) {
					this.taxID = lsTmp.get(0).getTaxID();
				}
				//accID���ڲ���databasetype�����ڣ�������accID���databasetype
				if ( (accID != null && !accID.equals("")) && (databaseType == null || databaseType.equals(""))) {
					this.databaseType = lsTmp.get(0).getDBInfo();
				}
			}
			else {
				logger.error("����û�и�genuniID��"+genUniID);
			}
			return;
		}
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
