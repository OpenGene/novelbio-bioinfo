package com.novelbio.database.mongorepo.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.cosmic.CosmicAbb;

public interface RepoCosmicAbb extends PagingAndSortingRepository<CosmicAbb, String>{

	@Query(value="{ 'abbreviation' : ?0 }")
	CosmicAbb findTermByAbbreviation(String abbreviation);
	List<CosmicAbb> findAll();
}
