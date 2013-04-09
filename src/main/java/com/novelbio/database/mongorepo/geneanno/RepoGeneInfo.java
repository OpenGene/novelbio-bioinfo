package com.novelbio.database.mongorepo.geneanno;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.GeneInfo;

public interface RepoGeneInfo extends PagingAndSortingRepository<GeneInfo, String> {
	
	@Query(value="{ 'geneID' : ?0 }")
	GeneInfo findByGeneID(long geneID);
	
	@Query(value="{ 'geneID' : ?0, 'taxID' : ?1 }")
	GeneInfo findByGeneIDAndTaxID(long geneID, int taxID);	
}
