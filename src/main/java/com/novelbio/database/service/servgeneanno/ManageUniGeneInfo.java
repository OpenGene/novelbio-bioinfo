package com.novelbio.database.service.servgeneanno;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.mongorepo.geneanno.RepoUniGeneInfo;
import com.novelbio.database.service.SpringFactory;

public class ManageUniGeneInfo {
	private static Logger logger = Logger.getLogger(ManageUniGeneInfo.class);
	@Autowired
	private RepoUniGeneInfo repoUniGeneInfo;
	
	public ManageUniGeneInfo() {
		repoUniGeneInfo = (RepoUniGeneInfo)SpringFactory.getFactory().getBean("repoUniGeneInfo");
	}
	
	public UniGeneInfo queryGeneInfo(String genUniID) {
		return repoUniGeneInfo.findByUniID(genUniID);
	}
	
	public UniGeneInfo queryGeneInfo(String genUniID, int taxID) {
		return repoUniGeneInfo.findByUniIDAndTaxID(genUniID, taxID);
	}
	
	/**
	 * 输入geneUniID以及具体的内容，看是否需要升级
	 * 能插入就插入，已经有了就判端与数据库中是否一致，不一致就升级
	 * @param genUniID
	 * @param gene2Go
	 */
	public void updateGenInfo(String genUniID, int taxID, AGeneInfo geneInfo) {
		if (taxID != 0 && geneInfo.getTaxID() != 0 && taxID != geneInfo.getTaxID()) {
			logger.error("输入taxID和自带taxID不一致,输入taxID："+taxID + " 自带taxID：" + geneInfo.getTaxID());
		}
		UniGeneInfo geneInfoOld = queryGeneInfo(genUniID, taxID);
		if (geneInfoOld != null) {
			if (geneInfoOld.addInfo(geneInfo)) {
				repoUniGeneInfo.save(geneInfoOld);
			}
		} else {
			UniGeneInfo geneInfoNew = new UniGeneInfo();
			geneInfoNew.copeyInfo(geneInfo);
			geneInfoNew.setGeneUniID(genUniID);
			geneInfoNew.setTaxID(taxID);
			try {
				repoUniGeneInfo.save(geneInfoNew);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
