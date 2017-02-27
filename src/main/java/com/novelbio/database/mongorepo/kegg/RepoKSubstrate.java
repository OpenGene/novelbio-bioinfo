package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.kegg.KGsubstrate;

@Repository
public class RepoKSubstrate {
	@Autowired
	MongoTemplate mongoTemplate;
	
	public List<KGsubstrate> queryLsByReactId(String reactionID) {
		Query query = new Query( Criteria.where("reactionID").is(reactionID));
		return mongoTemplate.find(query, KGsubstrate.class);
	}

	public KGsubstrate findOne(String kgId) {
		return mongoTemplate.findById(kgId, KGsubstrate.class);
	}

}
