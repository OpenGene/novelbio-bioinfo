package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.kegg.KGpathRelation;

@Repository
public class RepoKPathRelation {
	@Autowired
	MongoTemplate mongoTemplateKegg;
	
	public List<KGpathRelation> findByPathName(String pathName) {
		Query query = new Query( Criteria.where("pathName").is(pathName));
		return mongoTemplateKegg.find(query, KGpathRelation.class);
	}
	
	public KGpathRelation findByPathNameSrcTrg(String pathName, String src, String trg) {
		Query query = new Query( Criteria.where("pathName").is(pathName).and("scrPath").is(src).and("trgPath").is(trg));
		return mongoTemplateKegg.findOne(query, KGpathRelation.class);
	}

	public void save(KGpathRelation kGpathRelation) {
		mongoTemplateKegg.save(kGpathRelation);
	}

	public void deleteAll() {
		mongoTemplateKegg.dropCollection(KGpathRelation.class);
	}
	
}
