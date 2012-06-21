package com.novelbio.database.model.modcopeid;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgo.GOInfoUniID;

public class GeneIDUni extends GeneIDabs{
	private static Logger logger = Logger.getLogger(GeneIDUni.class);
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * @param accID
	 * @param idType
	 *            ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID
	 *            ����ID
	 */
	public GeneIDUni(String accID,String idType, String genUniID, int taxID) {
		this.accID = accID;
		this.genUniID = genUniID;
		this.idType = idType;
		this.taxID = taxID;
		if (taxID == 0 || accID != null) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID);
			uniProtID.setGenUniID(genUniID);
			ArrayList<UniProtID> lsTmp = servUniProtID.queryLsUniProtID(uniProtID);
			if (lsTmp.size() > 0) {
				if (taxID == 0) {
					this.taxID = lsTmp.get(0).getTaxID();
				}
				if ( accID != null ) {
					this.geneIDDBinfo = lsTmp.get(0).getDBInfo();
				}
			}
			else {
				logger.error("����û�и�genuniID��"+genUniID);
			}
			return;
		}
	}
	protected void setGenInfo() {
		UniGeneInfo uniGeneInfo = new UniGeneInfo();
		uniGeneInfo.setUniProtID(getGenUniID()); uniGeneInfo.setTaxID(taxID);
		geneInfo = servUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}

	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		return servUniProtID.queryGenUniID(genUniID, taxID, dbInfo);
	}
	
	@Override
	protected void setGoInfo() {
		goInfoAbs = new GOInfoUniID(genUniID, taxID);
	}
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		lsBlastInfos = new ArrayList<BlastInfo>();
		for (int i : StaxID) {
			BlastInfo blastInfo = servBlastInfo.queryBlastInfo(genUniID,taxID, i,evalue);
			addLsBlastInfo(blastInfo);
		}
		isBlastedFlag = false;
	}
}
