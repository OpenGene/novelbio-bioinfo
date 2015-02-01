package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.mongorepo.kegg.RepoKPathway;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class ServKPathway {
	
	RepoKPathway mapKPathway;
	
	public ServKPathway() {
		mapKPathway = (RepoKPathway)SpringFactoryBioinfo.getFactory().getBean("repoKPathway");
	}

	public KGpathway findByPathName(String mapNum) {
		return mapKPathway.findOne(mapNum);
	}
	
	public List<KGpathway> findAll() {
		return mapKPathway.findAll();
	}
	
	public void save(KGpathway kGpathway) {
		mapKPathway.save(kGpathway);
	}
	
	/** 给定一个含有pathName的pathway，将其删除 */
	public void delete(KGpathway kGpathway) {
		mapKPathway.delete(kGpathway.getPathName());
	}
	
	public void deleteAll() {
		mapKPathway.deleteAll();
	}
	
	static class ManageHolder {
		static ServKPathway instance = new ServKPathway();
	}
	
	public static ServKPathway getInstance() {
		return ManageHolder.instance;
	}





}
