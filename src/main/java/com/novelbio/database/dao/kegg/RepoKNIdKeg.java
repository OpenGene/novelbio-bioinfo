package com.novelbio.database.dao.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.model.kegg.nogene.KGNIdKeg;

@Repository
public class RepoKNIdKeg {
	@Autowired
	MongoTemplate mongoTemplate;
	
	public List<KGNIdKeg> findByKegId(String kegID) {
		Query query = new Query( Criteria.where("kegID").is(kegID));
		return mongoTemplate.find(query, KGNIdKeg.class);
	}
	
	public List<KGNIdKeg> findAll() {
		return mongoTemplate.findAll(KGNIdKeg.class);
	}

	public KGNIdKeg findOne(String usualName) {
		return mongoTemplate.findById(usualName, KGNIdKeg.class);
	}

	public void save(KGNIdKeg kgnIdKeg) {
		mongoTemplate.save(kgnIdKeg);
	}

	
}
