package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import com.novelbio.database.entity.kegg.KGIDgen2Keg;

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
