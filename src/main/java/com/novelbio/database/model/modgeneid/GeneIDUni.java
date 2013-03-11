package com.novelbio.database.model.modgeneid;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgo.GOInfoUniID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.database.service.servgeneanno.ServUniProtID;

public class GeneIDUni extends GeneIDabs{
	private static Logger logger = Logger.getLogger(GeneIDUni.class);
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param accID
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
	 */
	public GeneIDUni(String accID, String genUniID, int taxID) {
		this.accID = accID;
		this.genUniID = genUniID;
		this.idType = GeneID.IDTYPE_UNIID;
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
				logger.error("可能没有该genuniID："+genUniID);
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
	protected AgeneUniID getGenUniID(String genUniID, DBAccIDSource dbInfo) {
		UniProtID uniProtID = new UniProtID();
		uniProtID.setUniID(genUniID);
		uniProtID.setTaxID(taxID); uniProtID.setDBInfo(dbInfo.toString());
		servUniProtID = new ServUniProtID();
		ArrayList<UniProtID> lsSubject = servUniProtID.queryLsUniProtID(uniProtID);
		for (UniProtID uniProtID2 : lsSubject) {
			UniProtID uniProtIDQueryAccID = new UniProtID();
			uniProtIDQueryAccID.setAccID(uniProtID2.getAccID());
			uniProtIDQueryAccID.setTaxID(taxID);
			ArrayList<UniProtID> lsuniprotIDs = servUniProtID.queryLsUniProtID(uniProtIDQueryAccID);
			if (lsuniprotIDs.size() == 1) {
				return uniProtID2;
			}
		}
		if (dbInfo != null && !dbInfo.equals("")) {
			return getGenUniID(genUniID, null);	
		}
		return null;
	}

	@Override
	protected void setGoInfo() {
		goInfoAbs = new GOInfoUniID(genUniID, taxID);
	}
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		lsBlastInfos = new ArrayList<BlastInfo>();
		for (int i : StaxID) {
			if (i <= 0) {
				continue;
			}
			BlastInfo blastInfo = servBlastInfo.queryBlastInfo(genUniID,taxID, i,evalue);
			addLsBlastInfo(blastInfo);
		}
		isBlastedFlag = false;
	}
}
