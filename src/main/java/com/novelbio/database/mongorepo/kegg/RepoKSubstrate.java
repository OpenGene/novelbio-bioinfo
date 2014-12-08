package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.KGsubstrate;

public interface RepoKSubstrate extends PagingAndSortingRepository<KGsubstrate, String> {
	/**
	 * 用reactionID,pathName,id,name中任意组合去查找substrate表
	 * @param kGsubstrate
	 * @return
	 */
	@Query(value="{ 'reactionID' : ?0}")
	public List<KGsubstrate> queryLsByReactId(String reactionID);

}
