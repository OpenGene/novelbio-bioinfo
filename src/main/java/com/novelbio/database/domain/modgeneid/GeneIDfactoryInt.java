package com.novelbio.database.domain.modgeneid;

import com.novelbio.database.model.geneanno.AgeneUniID;

public interface GeneIDfactoryInt {

	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param idType  必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID 物种ID
	 */
	public GeneIDabs createGeneID(int idType, String genUniID, int taxID);
	
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param accID
	 * @param taxID
	 * @return
	 */
	public GeneIDabs createGeneID(String accID, int taxID);
	/**
	 * 设定初始值，不验证 如果在数据库中没有找到相应的geneUniID，则返回null 只能产生一个CopedID，此时accID = ""
	 * @param idType  必须是IDTYPE中的一种
	 * @param genUniID
	 * @param taxID 物种ID
	 */
	GeneIDabs createGeneID(AgeneUniID ageneUniID);

}
