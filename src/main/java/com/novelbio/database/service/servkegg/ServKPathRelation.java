package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.domain.kegg.KGpathRelation;
import com.novelbio.database.mongorepo.kegg.RepoKPathRelation;
import com.novelbio.database.service.SpringFactory;

public class ServKPathRelation {
	RepoKPathRelation mapKPathRelation;
	
	public ServKPathRelation() {
		mapKPathRelation = (RepoKPathRelation)SpringFactory.getFactory().getBean("repoKPathRelation");
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
	
	static class ManageHolder {
		static ServKPathRelation instance = new ServKPathRelation();
	}
	
	public static ServKPathRelation getInstance() {
		return ManageHolder.instance;
	}

}
