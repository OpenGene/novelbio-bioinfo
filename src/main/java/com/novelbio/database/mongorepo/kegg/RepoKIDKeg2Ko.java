package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.domain.kegg.KGIDkeg2Ko;

@Repository
public class RepoKIDKeg2Ko {
	@Autowired
	MongoTemplate mongoTemplate;
	
	public List<KGIDkeg2Ko> findyLsByKegId(String keggID) {
		Query query = new Query( Criteria.where("keggID").is(keggID));
		return mongoTemplate.find(query, KGIDkeg2Ko.class);
	}
	
	public List<KGIDkeg2Ko> findyLsByKegIdAndTaxId(String kegID, int taxID) {
		Query query = new Query( Criteria.where("keggID").is(kegID).and("taxID").is(taxID));
		return mongoTemplate.find(query, KGIDkeg2Ko.class);
	}
	
	public List<KGIDkeg2Ko> findyLsByKoAndTaxId(String koId, int taxID) {
		Query query = new Query( Criteria.where("ko").is(koId).and("taxID").is(taxID));
		return mongoTemplate.find(query, KGIDkeg2Ko.class);
	}
	
	public KGIDkeg2Ko findByKegIdAndKo(String keggID, String Ko) {
		Query query = new Query( Criteria.where("keggID").is(keggID).and("ko").is(Ko));
		return mongoTemplate.findOne(query, KGIDkeg2Ko.class);
	}

	public void save(KGIDkeg2Ko kgiDkeg2Ko) {
		mongoTemplate.save(kgiDkeg2Ko);
	}

	public void deleteAll() {
		mongoTemplate.dropCollection(KGIDkeg2Ko.class);
	}
}
