package com.novelbio.database.service.servkegg;

import com.novelbio.database.domain.kegg.KGsubstrate;
import com.novelbio.database.mongorepo.kegg.RepoKSubstrate;
import com.novelbio.database.service.SpringFactory;

public class ServKSubstrate {
	
	RepoKSubstrate mapKSubstrate;
	
	public ServKSubstrate() {
		mapKSubstrate = (RepoKSubstrate)SpringFactory.getFactory().getBean("repoKSubstrate");
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
