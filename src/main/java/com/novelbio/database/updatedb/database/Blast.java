package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.model.modcopeid.CopedID;

public class Blast extends ImportPerLine{
	public  Blast() {
		this.readFromLine = 1;
	}
	int taxID = 0;
	public void setTaxID(int taxID) {
		this.taxID = taxID;
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
	
	boolean accIDisGeneID = false;
	boolean blastIDisGeneID = false;
	/**
	 * 第一列，是accID还是geneID
	 * true：geneID
	 * false：accID
	 * @return
	 */
	public void setAccIDIsGeneID(boolean accIDisGeneID) {
		this.accIDisGeneID = accIDisGeneID;
	}
	/**
	 * blast到的ID是accID还是geneID
	 * @param blastIDisGeneID
	 */
	public void setBlastIDisGeneID(boolean blastIDisGeneID)
	{
		this.blastIDisGeneID = blastIDisGeneID;
	}
	@Override
	boolean impPerLine(String lineContent) {
		
		String[] ss = lineContent.split("\t");
		CopedID copedID = null;
		if (accIDisGeneID) {
			try {
				int geneID = Integer.parseInt(ss[0]);
				copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[0], taxID);
			} catch (Exception e) {
				copedID = new CopedID(CopedID.IDTYPE_UNIID, ss[0], taxID);
			}
		}
		else {
			copedID = new CopedID(ss[0], taxID);
		}
		copedID.setUpdateDBinfo(queryDBinfo, false);
		if (blastIDisGeneID) {
			try {
				int geneID = Integer.parseInt(ss[1]);
				copedID.setUpdateBlastInfo(ss[1],CopedID.IDTYPE_GENEID,  blastDBinfo, subTaxID, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
			} catch (Exception e) {
				copedID.setUpdateBlastInfo(ss[1],CopedID.IDTYPE_UNIID,  blastDBinfo, subTaxID, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
			}
		}
		else {
			copedID.setUpdateBlastInfo(ss[1], blastDBinfo, subTaxID, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
		}
		return copedID.update(false);
	}
	
}
