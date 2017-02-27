package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.kegg.KGpathway;

@Repository
public class RepoKPathway {
	@Autowired
	MongoTemplate mongoTemplate;
	
	public List<KGpathway> findAll() {
		return mongoTemplate.findAll(KGpathway.class, "kgpathway");
	}

	public KGpathway findOne(String mapNum) {
		return mongoTemplate.findById(mapNum, KGpathway.class);
	}

	public void save(KGpathway kGpathway) {
		mongoTemplate.save(kGpathway);
	}

	public void delete(String pathName) {
		KGpathway kGpathway = new KGpathway();
		kGpathway.setPathName(pathName);
		mongoTemplate.remove(kGpathway);
		
	}

	public void deleteAll() {
		mongoTemplate.dropCollection(KGpathway.class);
	}
}
