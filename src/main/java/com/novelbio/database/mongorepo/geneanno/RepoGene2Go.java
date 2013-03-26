package com.novelbio.database.mongorepo.geneanno;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.Gene2Go;

public interface RepoGene2Go extends PagingAndSortingRepository<Gene2Go, String> {
	
	@Query(value="{ 'geneID' : ?0 }")
	Gene2Go findByGeneID(long geneID);
	
	@Query(value="{ 'goID' : ?0, 'taxID' : ?1 }")
	Gene2Go findByGoIDAndTaxID(String goID, int taxID);
	
}
