package com.novelbio.database.dao.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.cosmic.NonCodingVars;

public interface RepoNonCodingVars extends PagingAndSortingRepository<NonCodingVars, String>{

	@Query(value="{ 'cosmicId' : ?0 }")
	NonCodingVars findNonCodingVarsByCosmicId(String cosmicId);
	@Query(value="{ 'chr' : ?0, 'pos' : ?1, 'alt' : ?2 }")
	List<NonCodingVars> findNonCodingVarsByPosAndVar(String chr, long pos, String alt);
	List<NonCodingVars> findAll();
}
