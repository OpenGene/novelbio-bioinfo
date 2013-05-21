package com.novelbio.database.model.modgeneid;

import com.novelbio.database.domain.geneanno.AgeneUniID;

public class GeneIDfactory implements GeneIDfactoryInt {
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param idType  必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID 物种ID
	 */
	public GeneIDInt createGeneID(int idType, String genUniID, int taxID) {
		GeneIDabs geneID = null;
		genUniID = genUniID.trim();
		if (genUniID.equals("")) {
			genUniID = null;
		}
		geneID = new GeneIDabs(idType, genUniID, taxID);
		return geneID;
	}
	
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param idType  必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxIDfile 物种ID
	 */
	@Override
	public GeneIDInt createGeneID(AgeneUniID ageneUniID) {
		return new GeneIDabs(ageneUniID);
	}
	
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * 
	 * @param idType
	 *            必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID
	 *            物种ID
	 */
	public GeneIDInt createGeneID(String accID, int taxID) {
		GeneIDabs geneID = null;
		if (accID != null) {
			accID = accID.replace("\"", "").trim();
			if (accID == null || accID.equals("")) {
				accID = null;
			}
		}
		geneID = new GeneIDabs(accID, taxID);
		return geneID;
	}
}
