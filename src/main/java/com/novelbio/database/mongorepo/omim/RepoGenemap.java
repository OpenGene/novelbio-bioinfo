package com.novelbio.database.mongorepo.omim;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.omim.OmimGeneMap;

public interface RepoGenemap extends PagingAndSortingRepository<OmimGeneMap, String>{
	/**
	 * 
	 * @param 
	 * @param 
	 * @return
	 */
	@Query(value="{ 'phenMimId' : ?0 }")
	List<OmimGeneMap> findInfByPheMimId(int phenMimId);
	
	@Query(value="{ 'genMimId' : ?0 }")
	List<OmimGeneMap> findInfByGenMimId(int genMimId);
	
	List<OmimGeneMap> findAll();
}
