package com.novelbio.database.mongorepo.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.cosmic.CodingMuts;

public interface RepoCodingMuts extends PagingAndSortingRepository<CodingMuts, String>{
	@Query(value="{ 'geneId' : ?0 }")
	List<CodingMuts> findCodingMutsByGeneId(int geneId);
	@Query(value="{ 'cosmicId' : ?0 }")
	CodingMuts findCodingMutsByCosmicId(int cosmicId);
	List<CodingMuts> findAll();
}
