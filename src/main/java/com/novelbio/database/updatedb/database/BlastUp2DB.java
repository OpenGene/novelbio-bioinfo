package com.novelbio.database.updatedb.database;

import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;

public class BlastUp2DB extends ImportPerLine{

	int subTaxID = 0;
	boolean update = false;
	
	int queryIDType = GeneID.IDTYPE_ACCID;
	int blastIDType = GeneID.IDTYPE_ACCID;

	public  BlastUp2DB() {
		this.readFromLine = 1;
		setReadFromLine(1);
	}
	
	/** true 导入数据库，false导入缓存
	 * 默认false导入缓存
	 */
	public void setUpdate(boolean update) {
		this.update = update;
	}
	
	/**
	 * blast到的物种ID
	 * @param subTaxID
	 */
	public void setSubTaxID(int subTaxID) {
		this.subTaxID = subTaxID;
	}
	
	/**
	 * 第一列，是accID还是geneID还是UniID
	 * @param IDtype 默认是CopedID.IDTYPE_ACCID
	 * @return
	 */
	public void setQueryIDType(int IDtypeQ) {
		this.queryIDType = IDtypeQ;
	}
	/**
	 * blast到的ID是accID还是geneID还是UniID
	 * @param blastID 默认是CopedID.IDTYPE_ACCID
	 */
	public void setBlastIDType(int IDtypeS) {
		this.blastIDType = IDtypeS;
	}
	
	@Override
	boolean impPerLine(String lineContent) {
		BlastInfo blastInfo = new BlastInfo(taxID, subTaxID, lineContent);
		try {
			if (update) {
				String[] ss = lineContent.split("\t");
				GeneID geneID = new GeneID(queryIDType, ss[0], taxID);
				geneID.addUpdateBlastInfo(blastInfo);
				geneID.update(false);
			} else {
				ManageBlastInfo.addBlastInfoToCache(blastInfo);
			}
		} catch (Exception e) {
			return true;
		}
		return true;
	}
	
}
