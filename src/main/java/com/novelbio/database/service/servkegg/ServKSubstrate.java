package com.novelbio.database.service.servkegg;

import com.novelbio.database.dao.kegg.RepoKSubstrate;
import com.novelbio.database.model.kegg.KGsubstrate;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class ServKSubstrate {
	
	RepoKSubstrate mapKSubstrate;
	
	public ServKSubstrate() {
		mapKSubstrate = (RepoKSubstrate)SpringFactoryBioinfo.getFactory().getBean("repoKSubstrate");
	}

	public KGsubstrate findByKegId(String kgId) {
		return mapKSubstrate.findOne(kgId);
	}
	
	static class ManageHolder {
		static ServKSubstrate instance = new ServKSubstrate();
	}
	
	public static ServKSubstrate getInstance() {
		return ManageHolder.instance;
	}
	
}
