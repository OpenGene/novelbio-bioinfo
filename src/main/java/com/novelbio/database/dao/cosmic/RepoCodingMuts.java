package com.novelbio.database.dao.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.cosmic.CodingMuts;

public interface RepoCodingMuts extends PagingAndSortingRepository<CodingMuts, String>{
	@Query(value="{ 'geneId' : ?0 }")
	List<CodingMuts> findCodingMutsByGeneId(int geneId);
	@Query(value="{ 'cosmicId' : ?0 }")
	CodingMuts findCodingMutsByCosmicId(int cosmicId);
	@Query(value="{ 'chr' : ?0, 'pos' : ?1,'alt' : ?2 }")
	List<CodingMuts> findCodingMutsByPosAndVar(String chr, long pos, String alt);
	
	List<CodingMuts> findAll();
}
