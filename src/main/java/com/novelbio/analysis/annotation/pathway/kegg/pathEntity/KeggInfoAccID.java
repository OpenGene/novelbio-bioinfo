package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.database.DAO.KEGGDAO.DaoKIDgen2Keg;
import com.novelbio.database.DAO.KEGGDAO.DaoKNIdKeg;
import com.novelbio.database.entity.kegg.KGIDgen2Keg;
import com.novelbio.database.entity.kegg.KGpathway;
import com.novelbio.database.entity.kegg.noGene.KGNIdKeg;

public class KeggInfoAccID extends KeggInfoAbs{
	
	public KeggInfoAccID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
		// TODO Auto-generated constructor stub
	}

	boolean bookgiDgen2Keg = false;
	/**
	 * ������ݿ��о���һһ��Ӧ�Ĺ�ϵ
	 */
	private KGNIdKeg kgnIdKeg;
	
	@Override
	protected void setKeggID() {
		if (!bookgiDgen2Keg) {
			bookgiDgen2Keg = true;
			KGNIdKeg kgnIdKegTmp = new KGNIdKeg();
			kgnIdKegTmp.setUsualName(genUniAccID);
			kgnIdKeg = DaoKNIdKeg.queryKGNIdKeg(kgnIdKegTmp);
		}
		if (kgnIdKeg != null) {
			keggID = "cpd:"+kgnIdKeg.getKegID();
		}
	}
	
	/**
	 * ������accIDֻ��ϣ����Ӧcomponent
	 * ֻ����null
	 */
	public ArrayList<String> getLsKo()
	{
		return null;
	}


}
