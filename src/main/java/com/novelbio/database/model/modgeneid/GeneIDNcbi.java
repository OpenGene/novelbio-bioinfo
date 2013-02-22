package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.model.modgo.GOInfoGenID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;

public class GeneIDNcbi extends GeneIDabs {
	private static Logger logger = Logger.getLogger(GeneIDNcbi.class);

	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * 
	 * @param accID
	 *            输入的accID不可能为""
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
	 */
	public GeneIDNcbi(String accID, String genUniID, int taxID) {
		super.accID = accID;
		super.genUniID = genUniID;
		super.idType = GeneID.IDTYPE_GENEID;
		this.taxID = taxID;
		if (taxID == 0 || accID != null) {
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(accID);
			ncbiid.setGenUniID(genUniID);
			ArrayList<NCBIID> lsTmp = servNCBIID.queryLsNCBIID(ncbiid);
			if (lsTmp.size() > 0) {
				if (taxID == 0) {
					this.taxID = lsTmp.get(0).getTaxID();
				}
				if (accID != null) {
					this.geneIDDBinfo = lsTmp.get(0).getDBInfo();
				}
			} else {
				logger.error("可能没有该genuniID：" + genUniID);
			}
			return;
		}
	}

	@Override
	protected void setGenInfo() {
		GeneInfo geneInfoq = new GeneInfo();
		long geneID = Long.parseLong(getGenUniID());
		geneInfoq.setGeneID(geneID);
		geneInfoq.setTaxID(taxID);
		super.geneInfo = servGeneInfo.queryGeneInfo(geneInfoq);
	}

	@Override
	protected AgeneUniID getGenUniID(String genUniID, String dbInfo) {
		int geneID = Integer.parseInt(genUniID);
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID);
		ncbiid.setTaxID(taxID); ncbiid.setDBInfo(dbInfo);
		ServNCBIID servGeneAnno = new ServNCBIID();
		ArrayList<NCBIID> lsSubject = servGeneAnno.queryLsNCBIID(ncbiid);
		for (NCBIID ncbiid2 : lsSubject) {
			NCBIID ncbiidQueryAccID = new NCBIID();
			ncbiidQueryAccID.setAccID(ncbiid2.getAccID());
			ncbiidQueryAccID.setTaxID(taxID);
			ArrayList<NCBIID> lsncbiid = servGeneAnno.queryLsNCBIID(ncbiidQueryAccID);
			if (lsncbiid.size() == 1) {
				return ncbiid2;
			}
		}
		if (dbInfo != null && !dbInfo.trim().equals("")) {
			return getGenUniID(genUniID, null);
		}
		return null;
	}

	@Override
	protected void setGoInfo() {
		goInfoAbs = new GOInfoGenID(genUniID, taxID);
	}

	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		lsBlastInfos = new ArrayList<BlastInfo>();
		for (int i : StaxID) {
			if (i <= 0) {
				continue;
			}
			BlastInfo blastInfo = servBlastInfo.queryBlastInfo(genUniID, taxID, i, evalue);
			addLsBlastInfo(blastInfo);
		}
		isBlastedFlag = false;
	}
}
