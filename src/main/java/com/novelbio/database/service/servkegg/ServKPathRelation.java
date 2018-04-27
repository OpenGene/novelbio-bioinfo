package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.dao.kegg.RepoKPathRelation;
import com.novelbio.database.model.kegg.KGpathRelation;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class ServKPathRelation {
	RepoKPathRelation mapKPathRelation;
	
	public ServKPathRelation() {
		mapKPathRelation = (RepoKPathRelation)SpringFactoryBioinfo.getFactory().getBean("repoKPathRelation");
	}

	public List<KGpathRelation> findByPathName(String pathName) {
		return mapKPathRelation.findByPathName(pathName);
	}
	
	public KGpathRelation findByPathNameSrcTrg(String pathName, String src, String trg) {
		return mapKPathRelation.findByPathNameSrcTrg(pathName, src, trg);
	}

	public void save(KGpathRelation kGpathRelation) {
		mapKPathRelation.save(kGpathRelation);
	}
	
	public void deleteAll() {
		mapKPathRelation.deleteAll();
	}
	
	static class ManageHolder {
		static ServKPathRelation instance = new ServKPathRelation();
	}
	
	public static ServKPathRelation getInstance() {
		return ManageHolder.instance;
	}

}
