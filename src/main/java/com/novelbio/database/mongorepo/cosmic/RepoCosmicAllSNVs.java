package com.novelbio.database.mongorepo.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.cosmic.CosmicAllSNVs;

public interface RepoCosmicAllSNVs extends PagingAndSortingRepository<CosmicAllSNVs, String>{
	@Query(value="{ 'cosmicId' : ?0 }")
	CosmicAllSNVs findCosmicAllSNVsByCosmicId(String cosmicId);
	@Query(value="{ 'chr' : ?0, 'pos' : ?1, 'alt' : ?2 }")
	List<CosmicAllSNVs> findCosmicAllSNVsByPosAndVar(String chr, long pos, String alt);
	List<CosmicAllSNVs> findAll();
	
}
