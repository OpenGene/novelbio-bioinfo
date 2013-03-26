package com.novelbio.database.mongorepo.geneanno;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.UniGene2Go;

public interface RepoUniGene2Go extends PagingAndSortingRepository<UniGene2Go, String> {
	
	@Query(value="{ 'uniID' : ?0 }")
	UniGene2Go findByUniID(String uniID);
	
	@Query(value="{ 'goID' : ?0, 'taxID' : ?1 }")
	UniGene2Go findByGoIDAndTaxID(String goID, int taxID);
	
}
