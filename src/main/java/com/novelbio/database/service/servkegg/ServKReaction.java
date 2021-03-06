package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.dao.kegg.RepoKReaction;
import com.novelbio.database.model.kegg.KGreaction;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class ServKReaction {
	RepoKReaction mapKReaction;
	
	public ServKReaction() {
		mapKReaction = (RepoKReaction)SpringFactoryBioinfo.getFactory().getBean("repoKReaction");
	}

	public List<KGreaction> findByName(String name) {
		return mapKReaction.findByName(name	);
	}
	
	public KGreaction findByNameAndPathNameAndId(String name, String pathName, int id) {
		return mapKReaction.findByNameAndPathNameAndId(name, pathName, id);
	}
	
	public void save(KGreaction kGreaction) {
		mapKReaction.save(kGreaction);
	}
	
	public void deleteAll() {
		mapKReaction.deleteAll();
	}
	
	static class ManageHolder {
		static ServKReaction instance = new ServKReaction();
	}
	
	public static ServKReaction getInstance() {
		return ManageHolder.instance;
	}


	
}
