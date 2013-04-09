package com.novelbio.database.service.servgeneanno;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.mongorepo.geneanno.RepoGeneInfo;
import com.novelbio.database.mongorepo.geneanno.RepoUniGeneInfo;
import com.novelbio.database.service.SpringFactory;

public class ManageGeneInfo {
	private static Logger logger = Logger.getLogger(ManageGeneInfo.class);
	@Autowired
	private RepoGeneInfo repoGeneInfo;
	private RepoUniGeneInfo repoUniGeneInfo;
	
	public ManageGeneInfo() {
		repoGeneInfo = (RepoGeneInfo)SpringFactory.getFactory().getBean("repoGeneInfo");
		repoUniGeneInfo = (RepoUniGeneInfo)SpringFactory.getFactory().getBean("repoUniGeneInfo");
	}
	
	/**
	 * @param genUniID
	 * @param taxID 小于0表示不起作用
	 * @return
	 */
	public AGeneInfo queryGeneInfo(int idtype, String genUniID, int taxID) {
		if (idtype == GeneID.IDTYPE_GENEID) {
			if (taxID > 0) {
				return repoGeneInfo.findByGeneIDAndTaxID(Integer.parseInt(genUniID), taxID);
			} else {
				return repoGeneInfo.findByGeneID(Integer.parseInt(genUniID));
			}
		} else {
			if (taxID > 0) {
				return repoUniGeneInfo.findByUniIDAndTaxID(genUniID, taxID);
			} else {
				return repoUniGeneInfo.findByUniID(genUniID);
			}
		}
		
	}
	
	/**
	 * 输入geneUniID以及具体的内容，看是否需要升级
	 * 能插入就插入，已经有了就判端与数据库中是否一致，不一致就升级
	 * @param genUniID
	 * @param gene2Go
	 */
	public void updateGenInfo(int idtype, String genUniID, int taxID, AGeneInfo geneInfo) {
		if (taxID != 0 && geneInfo.getTaxID() != 0 && taxID != geneInfo.getTaxID()) {
			logger.error("输入taxID和自带taxID不一致,输入taxID："+taxID + " 自带taxID：" + geneInfo.getTaxID());
		}
		AGeneInfo geneInfoOld = queryGeneInfo(idtype, genUniID, taxID);
		if (geneInfoOld != null) {
			if (geneInfoOld.addInfo(geneInfo)) {
				if (idtype == GeneID.IDTYPE_GENEID) {
					repoGeneInfo.save((GeneInfo)geneInfoOld);
				} else {
					repoUniGeneInfo.save((UniGeneInfo)geneInfoOld);
				}
			}
		} else {
			if (idtype == GeneID.IDTYPE_GENEID) {
				GeneInfo geneInfoNew = new GeneInfo();
				geneInfoNew.copeyInfo(geneInfo);
				geneInfoNew.setGeneUniID(genUniID);
				geneInfoNew.setTaxID(taxID);
				try {
					repoGeneInfo.save(geneInfoNew);
				} catch (Exception e) {
					e.printStackTrace();
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
}
