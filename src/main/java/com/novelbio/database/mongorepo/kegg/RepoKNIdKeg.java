package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;

@Repository
public class RepoKNIdKeg {
	@Autowired
	MongoTemplate mongoTemplateKegg;
	
	public List<KGNIdKeg> findByKegId(String kegID) {
		Query query = new Query( Criteria.where("kegID").is(kegID));
		return mongoTemplateKegg.find(query, KGNIdKeg.class);
	}
	
	public List<KGNIdKeg> findAll() {
		return mongoTemplateKegg.findAll(KGNIdKeg.class);
	}

	public KGNIdKeg findOne(String usualName) {
		return mongoTemplateKegg.findById(usualName, KGNIdKeg.class);
	}

	public void save(KGNIdKeg kgnIdKeg) {
		mongoTemplateKegg.save(kgnIdKeg);
	}

	
}
