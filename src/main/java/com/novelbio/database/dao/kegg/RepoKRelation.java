package com.novelbio.database.dao.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.model.kegg.KGrelation;

@Repository
public class RepoKRelation {
	@Autowired
	MongoTemplate mongoTemplate;
	
	/**
	 * 用pathname,entry1,entry2,type,sybtypeName中任意组合去查找relation表
	 * @param kGrelation
	 * @return
	 */
	public List<KGrelation> queryLsKGrelations(String pathName) {
		Query query = new Query( Criteria.where("pathName").is(pathName));
		return mongoTemplate.find(query, KGrelation.class);
	}
	
	public List<KGrelation> findByPathNameAndEntry1Id(String pathName, int entry1Id) {
		Query query = new Query( Criteria.where("pathName").is(pathName).and("entry1").is(entry1Id));
		return mongoTemplate.find(query, KGrelation.class);
	}

	public List<KGrelation> findByPathNameAndEntry2Id(String pathName, int entry2Id) {
		Query query = new Query( Criteria.where("pathName").is(pathName).and("entry2").is(entry2Id));
		return mongoTemplate.find(query, KGrelation.class);
	}
	
	public List<KGrelation> findByPathNameAndEntry1IdAndEntry2Id(String pathName, int entry1Id, int entry2Id) {
		Query query = new Query( Criteria.where("pathName").is(pathName).and("entry1").is(entry2Id).and("entry2").is(entry2Id));
		return mongoTemplate.find(query, KGrelation.class);
	}

	public KGrelation findByPathNameAndEntry1IdAndEntry2IdAndType(String pathName, int entry1Id, int entry2Id, String type) {
		Query query = new Query( Criteria.where("pathName").is(pathName).and("entry1").is(entry2Id).and("entry2").is(entry2Id).and("type").is(type));
		return mongoTemplate.findOne(query, KGrelation.class);
	}

	public void save(KGrelation kGrelation) {
		mongoTemplate.save(kGrelation);
	}

	public void deleteAll() {
		mongoTemplate.dropCollection(KGrelation.class);
	}
	
}
