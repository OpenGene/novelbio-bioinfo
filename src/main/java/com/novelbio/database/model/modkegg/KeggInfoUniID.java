package com.novelbio.database.model.modkegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modcopeid.GeneID;

public class KeggInfoUniID extends KeggInfoAbs{
	
	public KeggInfoUniID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
		// TODO Auto-generated constructor stub
	}
	boolean bookgiDgen2Keg = false;
	/**
	 * ������ݿ��о���һһ��Ӧ�Ĺ�ϵ
	 */
	private KGIDgen2Keg kgiDgen2Keg;
	/**
	 * ��ʱUniIDû�ж�Ӧ��KEGG�ϣ���������UniID���Թ�
	 */
	@Override
	protected void setKeggID() {
		
	}

}
