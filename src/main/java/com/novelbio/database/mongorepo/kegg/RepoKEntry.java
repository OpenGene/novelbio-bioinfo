package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.kegg.KGentry;

@Repository
public class RepoKEntry {
	@Autowired
	MongoTemplate mongoTemplate;
	
	public List<KGentry> findAll() {
		return mongoTemplate.findAll(KGentry.class);
	}
	
	public List<KGentry> findByName(String name) {
		Query query = new Query( Criteria.where("name").is(name));
		return mongoTemplate.find(query, KGentry.class);
	}
	
	public List<KGentry> findByNameAndTaxId(String name, int taxId) {
		Query query = new Query( Criteria.where("name").is(name).and("taxID").is(taxId));
		return mongoTemplate.find(query, KGentry.class);
	}
	
	public List<KGentry> findByPathName(String pathName) {
		Query query = new Query( Criteria.where("pathName").is(pathName));
		return mongoTemplate.find(query, KGentry.class);
	}
	
	public List<KGentry> findByPathNameAndParentId(String pathName, int parentId) {
		Query query = new Query( Criteria.where("pathName").is(pathName).and("parentID").is(parentId));
		return mongoTemplate.find(query, KGentry.class);
	}
	
	public List<KGentry> findByNamePath(String name, String pathName) {
		Query query = new Query( Criteria.where("name").is(name).and("pathName").is(pathName));
		return mongoTemplate.find(query, KGentry.class);
	}
	
	public KGentry findByNamePathAndId(String name, String pathName, int entryId) {
		Query query = new Query( Criteria.where("name").is(name).and("pathName").is(pathName).and("entryId").is(entryId));
		return mongoTemplate.findOne(query, KGentry.class);
	}
	
	public List<KGentry> findByNamePathAndIdAndTaxId(String name, String pathName, int entryId, String taxId) {
		Query query = new Query( Criteria.where("name").is(name).and("pathName").is(pathName).and("entryId").is(entryId).and("taxID").is(taxId));
		return mongoTemplate.find(query, KGentry.class);
	}
	
	public List<KGentry> findByPathNameAndEntryId(String pathName, int entryId) {
		Query query = new Query( Criteria.where("pathName").is(pathName).and("entryId").is(entryId));
		return mongoTemplate.find(query, KGentry.class);
	}
	
	public KGentry findByNamePathAndIdAndReaction(String name, String pathName, int entryId, String reaction) {
		Query query = new Query( Criteria.where("name").is(name).and("pathName").is(pathName).and("entryId").is(entryId).and("reactionName").is(reaction));
		return mongoTemplate.findOne(query, KGentry.class);
	}
	
	public void save(KGentry kGentry) {
		mongoTemplate.save(kGentry);
	}

	public void deleteAll() {
		mongoTemplate.dropCollection(KGentry.class);
		
	}
}
