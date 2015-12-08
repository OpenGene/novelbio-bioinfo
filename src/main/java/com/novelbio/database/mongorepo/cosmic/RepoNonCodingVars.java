package com.novelbio.database.mongorepo.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.cosmic.NonCodingVars;

public interface RepoNonCodingVars extends PagingAndSortingRepository<NonCodingVars, String>{

	@Query(value="{ 'cosmicId' : ?0 }")
	NonCodingVars findNonCodingVarsByCosmicId(String cosmicId);
	List<NonCodingVars> findAll();
}
