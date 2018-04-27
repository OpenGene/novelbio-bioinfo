package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.dao.kegg.RepoKIDgen2Keg;
import com.novelbio.database.model.kegg.KGIDgen2Keg;
import com.novelbio.database.service.SpringFactoryBioinfo;

/**
 * geneID到KeggID的转换表
 * @author zong0jie
 */
public class ServKIDgen2Keg {
	
	RepoKIDgen2Keg mapKIDgen2Keg;
	
	public ServKIDgen2Keg() {
		mapKIDgen2Keg = (RepoKIDgen2Keg)SpringFactoryBioinfo.getFactory().getBean("repoKIDgen2Keg");
	}

	public KGIDgen2Keg findByGeneId(Long geneId) {
		return mapKIDgen2Keg.findByGeneId(geneId);
	}
	
	public KGIDgen2Keg findByKegId(String kegId) {
		return mapKIDgen2Keg.findByKegId(kegId);
	}
	
	public KGIDgen2Keg findByGeneIdAndTaxIdAndKegId(long geneID, int taxID, String kegID) {
		return mapKIDgen2Keg.findByGeneIdAndTaxIdAndKegId(geneID, taxID, kegID);
	}

	public void save(KGIDgen2Keg kgiDgen2Keg) {
		mapKIDgen2Keg.save(kgiDgen2Keg);
	}
	
	public void deleteAll() {
		mapKIDgen2Keg.deleteAll();
	}
	
	static class ManageHolder {
		static ServKIDgen2Keg instance = new ServKIDgen2Keg();
	}
	
	public static ServKIDgen2Keg getInstance() {
		return ManageHolder.instance;
	}




	
}
