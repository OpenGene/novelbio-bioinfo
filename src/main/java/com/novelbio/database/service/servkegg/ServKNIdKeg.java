package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.dao.kegg.RepoKNIdKeg;
import com.novelbio.database.model.kegg.nogene.KGNIdKeg;
import com.novelbio.database.service.SpringFactoryBioinfo;

/**
 * 将不是基因的ID--也就是一些化合物等转换为KeggID
 * @author zong0jie
 *
 */
public class ServKNIdKeg {
	RepoKNIdKeg mapKNIdKeg;
	public ServKNIdKeg() {
		mapKNIdKeg = (RepoKNIdKeg)SpringFactoryBioinfo.getFactory().getBean("repoKNIdKeg");
	}

	public List<KGNIdKeg> findByKegId(String kegID) {
		return mapKNIdKeg.findByKegId(kegID);
	}
	public KGNIdKeg findByUsualName(String usualName) {
		return mapKNIdKeg.findOne(usualName);
	}
	
	static class ManageHolder {
		static ServKNIdKeg instance = new ServKNIdKeg();
	}
	
	public static ServKNIdKeg getInstance() {
		return ManageHolder.instance;
	}

	public void save(KGNIdKeg kgnIdKeg) {
		mapKNIdKeg.save(kgnIdKeg);
	}


	
}
