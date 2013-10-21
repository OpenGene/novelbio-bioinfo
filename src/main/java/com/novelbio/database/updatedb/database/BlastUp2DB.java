package com.novelbio.database.updatedb.database;

import org.apache.log4j.Logger;
import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.submig;

import com.novelbio.database.domain.geneanno.BlastFileInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;

public class BlastUp2DB extends ImportPerLine {
	private static final Logger logger = Logger.getLogger(BlastUp2DB.class);
	
	int queryIDType = GeneID.IDTYPE_ACCID;
	int blastIDType = GeneID.IDTYPE_ACCID;	
	BlastFileInfo blastFileInfo = new BlastFileInfo();
	ManageBlastInfo manageBlastInfo = ManageBlastInfo.getInstance();
	
	public  BlastUp2DB() {
		this.readFromLine = 1;
		setReadFromLine(1);
	}
	
	/** true 导入数据库，false导入缓存
	 * 默认false导入缓存
	 */
	public void setUpdate(boolean update, String blastFileName) {
		blastFileInfo.setFileName(blastFileName);
		blastFileInfo.setTmp(!update);
	}
	/**
	 * 导入单个文件时，设定taxID
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
		blastFileInfo.setQueryTaxID(taxID);
	}
	/**
	 * blast到的物种ID
	 * @param subTaxID
	 */
	public void setSubTaxID(int subTaxID) {
		blastFileInfo.setSubjectTaxID(subTaxID);
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
	public void updateFile(String gene2AccFile) {
		manageBlastInfo.saveBlastFile(blastFileInfo);
		super.updateFile(gene2AccFile);
	}
	@Override
	boolean impPerLine(String lineContent) {
		BlastInfo blastInfo = new BlastInfo(true, taxID, queryIDType == GeneID.IDTYPE_ACCID, 
				blastFileInfo.getSubjectTaxID(), blastIDType == GeneID.IDTYPE_ACCID, lineContent);
		
		blastInfo.setBlastFileInfo(blastFileInfo);
		try {
			manageBlastInfo.save(blastInfo);
		} catch (Exception e) {
			logger.error("import db error", e);
			return false;
		}
		
		return true;
	}
	
}
