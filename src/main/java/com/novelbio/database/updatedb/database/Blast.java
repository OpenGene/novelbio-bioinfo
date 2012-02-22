package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.model.modcopeid.CopedID;

public class Blast extends ImportPerLine{
	public  Blast() {
		this.readFromLine = 1;
	}
	int subTaxID = 0;
	String queryDBinfo = "";
	public void setQueryDBinfo(String queryDBinfo) {
		this.queryDBinfo = queryDBinfo;
	}
	
	/**
	 * blast到的物种ID
	 * @param subTaxID
	 */
	public void setSubTaxID(int subTaxID) {
		this.subTaxID = subTaxID;
	}
	String blastDBinfo= NovelBioConst.DBINFO_NCBIID;
	public void setBlastDBinfo(String blastDBinfo)
	{
		this.blastDBinfo = blastDBinfo;
	}
	
	String queryIDType = CopedID.IDTYPE_ACCID;
	String blastIDType = CopedID.IDTYPE_ACCID;
	/**
	 * 第一列，是accID还是geneID还是UniID
	 * @return
	 */
	public void setQueryID(String IDtype) {
		this.queryIDType = IDtype;
	}
	/**
	 * blast到的ID是accID还是geneID还是UniID
	 * @param blastID
	 */
	public void setBlastID(String blastID) {
		this.blastIDType = blastID;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		CopedID copedID = null;
		if (!queryIDType.equals(CopedID.IDTYPE_ACCID)) {
				copedID = new CopedID(queryIDType, ss[0], taxID);
		}
		else {
			copedID = new CopedID(ss[0], taxID);
		}
		
		copedID.setUpdateDBinfo(queryDBinfo, false);
		if (!blastIDType.equals(CopedID.IDTYPE_ACCID)) {
			copedID.setUpdateBlastInfo(ss[1],blastIDType,  blastDBinfo, subTaxID, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
		}
		else {
			copedID.setUpdateBlastInfo(ss[1], blastDBinfo, subTaxID, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
		}
		return copedID.update(false);
	}
	
}
