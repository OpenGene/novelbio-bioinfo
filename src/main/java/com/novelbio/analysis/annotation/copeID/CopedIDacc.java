package com.novelbio.analysis.annotation.copeID;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.GO.goEntity.GOInfoAbs;
import com.novelbio.analysis.annotation.GO.goEntity.GOInfoGenID;
import com.novelbio.database.entity.friceDB.AgeneUniID;
import com.novelbio.database.entity.friceDB.Gene2Go;

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
		
	}

	
	
}
