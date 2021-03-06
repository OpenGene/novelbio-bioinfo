package com.novelbio.database.dao.kegg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.novelbio.database.model.kegg.KGreaction;

@Repository
public class RepoKReaction {
	@Autowired
	MongoTemplate mongoTemplate;
	
	/**
	 * 用name,pathNam,ID中任意组合去查找reaction表
	 * @param kGreaction
	 * @return
	 */
	public List<KGreaction> findByName(String name) {
		Query query = new Query( Criteria.where("name").is(name));
		return mongoTemplate.find(query, KGreaction.class);
	}
	
	public KGreaction findByNameAndPathNameAndId(String name, String pathName, int reactionId) {
		Query query = new Query( Criteria.where("name").is(name).and("pathName").is(pathName).and("reactionId").is(reactionId));
		return mongoTemplate.findOne( query, KGreaction.class);
	}

	public void save(KGreaction kGreaction) {
		mongoTemplate.save(kGreaction);
	}

	public void deleteAll() {
		mongoTemplate.dropCollection(KGreaction.class);		
	}

}
