package com.novelbio.database.model.modcopeid;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modgo.GOInfoGenID;
import com.novelbio.database.model.modgo.GOInfoUniID;

public class CopedIDacc extends CopedIDAbs{
	 

	
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * 
	 * @param idType
	 *            ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID
	 *            ����ID
	 */
	public CopedIDacc(String accID, String idType, String genUniID, int taxID) {
		this.accID = accID;
		this.genUniID = genUniID;
		this.idType = idType;
		this.taxID = taxID;
	}
	
	@Override
	protected void setGenInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		return null;
	}

	@Override
	protected void setGoInfo() {
		goInfoAbs = new GOInfoUniID(accID, taxID);
	}

	
	
}
