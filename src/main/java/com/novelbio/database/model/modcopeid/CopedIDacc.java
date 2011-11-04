package com.novelbio.database.model.modcopeid;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modgo.GOInfoGenID;
import com.novelbio.database.model.modgo.GOInfoUniID;

public class CopedIDacc extends CopedIDAbs{
	 

	
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * 
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
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
