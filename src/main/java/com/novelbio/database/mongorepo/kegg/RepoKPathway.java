package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.kegg.KGpathway;

@Repository
public class RepoKPathway {
	@Autowired
	MongoTemplate mongoTemplateKegg;
	
	public List<KGpathway> findAll() {
		return mongoTemplateKegg.findAll(KGpathway.class, "kgpathway");
	}

	public KGpathway findOne(String mapNum) {
		return mongoTemplateKegg.findById(mapNum, KGpathway.class);
	}

	public void save(KGpathway kGpathway) {
		mongoTemplateKegg.save(kGpathway);
	}

	public void delete(String pathName) {
		KGpathway kGpathway = new KGpathway();
		kGpathway.setPathName(pathName);
		mongoTemplateKegg.remove(kGpathway);
		
	}

	public void deleteAll() {
		mongoTemplateKegg.dropCollection(KGpathway.class);
	}
}
