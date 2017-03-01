package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.kegg.KGIDgen2Keg;

@Repository
public class RepoKIDgen2Keg {
	@Autowired
	MongoTemplate mongoTemplate;
	
	public KGIDgen2Keg findByGeneId(long geneId) {
		Query query = new Query( Criteria.where("geneID").is(geneId));
		return mongoTemplate.findOne(query, KGIDgen2Keg.class);
	}
	
	public KGIDgen2Keg findByKegId(String keggId) {
		Query query = new Query( Criteria.where("keggID").is(keggId));
		return mongoTemplate.findOne(query, KGIDgen2Keg.class);
	}
	
	public KGIDgen2Keg findByGeneIdAndKegId(long geneId, String keggId) {
		Query query = new Query( Criteria.where("geneID").is(geneId).and("keggID").is(keggId));
		return mongoTemplate.findOne(query, KGIDgen2Keg.class);
	}
	
	public List<KGIDgen2Keg> findByGeneIdAndTaxId(long geneId, int taxId) {
		Query query = new Query( Criteria.where("geneID").is(geneId).and("taxID").is(taxId));
		return mongoTemplate.find(query, KGIDgen2Keg.class);
	}
	
	public KGIDgen2Keg findByGeneIdAndTaxIdAndKegId(long geneId, int taxId, String keggId) {
		Query query = new Query( Criteria.where("geneID").is(geneId).and("taxID").is(taxId).and("keggID").is(keggId));
		return mongoTemplate.findOne(query, KGIDgen2Keg.class);
	}

	public void save(KGIDgen2Keg kgiDgen2Keg) {
		mongoTemplate.save(kgiDgen2Keg);
	}

	public void deleteAll() {
		mongoTemplate.dropCollection(KGIDgen2Keg.class);
	}
	
	
}
