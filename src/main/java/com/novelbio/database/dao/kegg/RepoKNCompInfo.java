package com.novelbio.database.dao.kegg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.novelbio.database.model.kegg.nogene.KGNCompInfo;
/**
 * kegg的化合物表
 * @author zong0jie
 *
 */
@Repository
public class RepoKNCompInfo {
	@Autowired
	MongoTemplate mongoTemplate;

	public KGNCompInfo findOne(String kegId) {
		return mongoTemplate.findById(kegId, KGNCompInfo.class);
	}

	public void save(KGNCompInfo kgnCompInfo) {
		mongoTemplate.save(kgnCompInfo);
	}
}
