package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.KGreaction;

public interface RepoKReaction extends PagingAndSortingRepository<KGreaction, String> {

	
	/**
	 * 用name,pathNam,ID中任意组合去查找reaction表
	 * @param kGreaction
	 * @return
	 */
	@Query(value="{ 'name' : ?0}")
	public List<KGreaction> findByName(String name);
	
	@Query(value="{'name' : ?0, 'pathName' : ?1, 'reactionId' : ?0}")
	public KGreaction findByNameAndPathNameAndId(String name, String pathName, int reactionId);


}
