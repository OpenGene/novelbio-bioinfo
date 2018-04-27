package com.novelbio.database.dao.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.cosmic.CosmicCNV;

public interface RepoNCV extends PagingAndSortingRepository<CosmicCNV, String>{

	@Query(value="{ 'cosmicId' : ?0 }")
	CosmicCNV findNCVByCosmicId(String cosmicId);
	
	List<CosmicCNV> findAll();
}
