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
	MongoTemplate mongoTemplateKegg;
	
	public KGIDgen2Keg findByGeneId(long geneId) {
		Query query = new Query( Criteria.where("geneID").is(geneId));
		return mongoTemplateKegg.findOne(query, KGIDgen2Keg.class);
	}
	
	public KGIDgen2Keg findByKegId(String keggId) {
		Query query = new Query( Criteria.where("keggID").is(keggId));
		return mongoTemplateKegg.findOne(query, KGIDgen2Keg.class);
	}
	
	public KGIDgen2Keg findByGeneIdAndKegId(long geneId, String keggId) {
		Query query = new Query( Criteria.where("geneID").is(geneId).and("keggID").is(keggId));
		return mongoTemplateKegg.findOne(query, KGIDgen2Keg.class);
	}
	
	public List<KGIDgen2Keg> findByGeneIdAndTaxId(long geneId, int taxId) {
		Query query = new Query( Criteria.where("geneID").is(geneId).and("taxID").is(taxId));
		return mongoTemplateKegg.find(query, KGIDgen2Keg.class);
	}
	
	public KGIDgen2Keg findByGeneIdAndTaxIdAndKegId(long geneId, int taxId, String keggId) {
		Query query = new Query( Criteria.where("geneID").is(geneId).and("taxID").is(taxId).and("keggID").is(keggId));
		return mongoTemplateKegg.findOne(query, KGIDgen2Keg.class);
	}

	public void save(KGIDgen2Keg kgiDgen2Keg) {
		mongoTemplateKegg.save(kgiDgen2Keg);
	}

	public void deleteAll() {
		mongoTemplateKegg.dropCollection(KGIDgen2Keg.class);
	}
	
	
}
