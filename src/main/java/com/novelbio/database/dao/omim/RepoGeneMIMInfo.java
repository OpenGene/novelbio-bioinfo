package com.novelbio.database.dao.omim;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.omim.GeneMIM;

public interface RepoGeneMIMInfo  extends PagingAndSortingRepository<GeneMIM, String>{
	/**
	 * 
	 * @param 
	 * @param 
	 * @return
	 */
	@Query(value="{ 'GeneMimId' : ?0 }")
	GeneMIM findInfByGeneMimId(int geneMimId);
	@Query(value="{ 'geneId' : ?0 }")
	GeneMIM findOmimInfByGeneId(int geneId);

	List<GeneMIM> findAll();
	
}
