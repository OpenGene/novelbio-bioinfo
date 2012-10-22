package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modgo.GOInfoGenID;
import com.novelbio.database.model.modgo.GOInfoUniID;

public class GeneIDAccID extends GeneIDabs{
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * 
	 * @param idType
	 *            ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID
	 *            ����ID
	 */
	public GeneIDAccID(String accID, String genUniID, int taxID) {
		if (accID != null) {
			this.accID = accID;
		}
		if (genUniID != null) {
			this.genUniID = genUniID;
		}
		this.idType = GeneID.IDTYPE_ACCID;
		this.taxID = taxID;
	}
	public int getTaxID() {
		if (taxID <= 0) {
			ArrayList<AgeneUniID> lsGeneUniID = getNCBIUniTax(accID, 0);
			if (lsGeneUniID.size() > 0) {
				taxID = lsGeneUniID.get(0).getTaxID();
			}
		}
		return taxID;
	}
	@Override
	protected void setGenInfo() {}
	
	protected void setSymbolDescrip() {
		if (geneInfo != null || symbol != null) return;
		
		symbol = "";
	}
	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		return null;
	}
	@Override
	protected void setGoInfo() {
		goInfoAbs = new GOInfoUniID(accID, taxID);
	}
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		lsBlastInfos = new ArrayList<BlastInfo>();
		for (int i : StaxID) {
			BlastInfo blastInfo = servBlastInfo.queryBlastInfo(accID,taxID, i,evalue);
			addLsBlastInfo(blastInfo);
		}
		isBlastedFlag = false;
	}
}
