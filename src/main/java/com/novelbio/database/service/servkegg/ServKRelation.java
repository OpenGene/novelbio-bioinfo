package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.dao.kegg.RepoKRelation;
import com.novelbio.database.model.kegg.KGrelation;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class ServKRelation {

	RepoKRelation mapKRelation;
	
	public ServKRelation() {
		mapKRelation = (RepoKRelation)SpringFactoryBioinfo.getFactory().getBean("repoKRelation");
	}
	
	public List<KGrelation> findByPathNameAndEntry1Id(String pathName, int entry1Id) {
		return mapKRelation.findByPathNameAndEntry1Id(pathName, entry1Id);
	}
	
	public List<KGrelation> findByPathNameAndEntry2Id(String pathName, int entry2Id) {
		return mapKRelation.findByPathNameAndEntry2Id(pathName, entry2Id);
	}
	
	public List<KGrelation> findByPathNameAndEntry1IdAndEntry2Id(String pathName, int entry1Id, int entry2Id) {
		return mapKRelation.findByPathNameAndEntry1IdAndEntry2Id(pathName, entry1Id, entry2Id);
	}

	public KGrelation findByPathNameAndEntry1IdAndEntry2IdAndType(
			String pathName, int entry1Id, int entry2Id, String type) {
		return mapKRelation.findByPathNameAndEntry1IdAndEntry2IdAndType(pathName, entry1Id, entry2Id, type);
	}
	
	public void save(KGrelation kGrelation) {
		mapKRelation.save(kGrelation);		
	}
	
	public void deleteAll() {
		mapKRelation.deleteAll();
	}

	static class ManageHolder {
		static ServKRelation instance = new ServKRelation();
	}
	
	public static ServKRelation getInstance() {
		return ManageHolder.instance;
	}



	
}
