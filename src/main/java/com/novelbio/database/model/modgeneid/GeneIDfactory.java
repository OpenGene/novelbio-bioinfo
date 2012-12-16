package com.novelbio.database.model.modgeneid;

public class GeneIDfactory implements GeneIDfactoryInt {
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param idType  必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID 物种ID
	 */
	public GeneIDInt createGeneID(String idType, String genUniID, int taxID) {
		GeneIDabs geneID = null;
		genUniID = genUniID.trim();
		if (genUniID.equals("")) {
			genUniID = null;
		}
		if (idType.equals(GeneID.IDTYPE_UNIID)) {
			geneID = new GeneIDUni(null, genUniID, taxID);
		}
		else if (idType.equals(GeneID.IDTYPE_GENEID)) {
			geneID = new GeneIDNcbi(null, genUniID, taxID);
		}
		else if (idType.equals(GeneID.IDTYPE_ACCID)) {
			geneID = new GeneIDAccID(null, genUniID, taxID);
		}
		return geneID;
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
	public GeneIDInt createGeneID(String accID,String idType, String genUniID, int taxID) {
		GeneIDabs geneID = null;
		if (accID != null) {
			accID = accID.replace("\"", "").trim();
			if (accID.equals("")) {
				accID = null;
			}
		}
		if (idType.equals(GeneID.IDTYPE_UNIID)) {
			geneID = new GeneIDUni(accID, genUniID, taxID);
		}
		else if (idType.equals(GeneID.IDTYPE_GENEID)) {
			geneID = new GeneIDNcbi(accID, genUniID, taxID);
		}
		else if (idType.equals(GeneID.IDTYPE_ACCID)) {
			geneID = new GeneIDAccID(accID, genUniID, taxID);
		}
		return geneID;
	}
}
