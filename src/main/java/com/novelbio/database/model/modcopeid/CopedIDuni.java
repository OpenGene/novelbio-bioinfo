package com.novelbio.database.model.modcopeid;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgo.GOInfoUniID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.database.service.servgeneanno.ServUniGeneInfo;
import com.novelbio.database.service.servgeneanno.ServUniProtID;

public class CopedIDuni extends CopedIDAbs{
	private static Logger logger = Logger.getLogger(CopedIDuni.class);
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param accID
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
	 */
	public CopedIDuni(String accID,String idType, String genUniID, int taxID) {
		this.accID = accID;
		this.genUniID = genUniID;
		this.idType = idType;
		this.taxID = taxID;
		if (taxID == 0 || (accID != null && !accID.equals("")) && (databaseType == null || databaseType.equals("")) ) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID);
			uniProtID.setGenUniID(genUniID);
			ArrayList<UniProtID> lsTmp = servUniProtID.queryLsUniProtID(uniProtID);
			if (lsTmp.size() > 0) {
				if (taxID == 0) {
					this.taxID = lsTmp.get(0).getTaxID();
				}
				//accID存在并且databasetype不存在，才能用accID获得databasetype
				if ( (accID != null && !accID.equals("")) && (databaseType == null || databaseType.equals(""))) {
					this.databaseType = lsTmp.get(0).getDBInfo();
				}
			}
			else {
				logger.error("可能没有该genuniID："+genUniID);
			}
			return;
		}
		this.taxID = taxID;
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

}
