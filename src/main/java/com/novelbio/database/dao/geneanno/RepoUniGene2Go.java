package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.geneanno.Gene2Go;
import com.novelbio.database.model.geneanno.UniGene2Go;

public interface RepoUniGene2Go extends PagingAndSortingRepository<UniGene2Go, String> {
	
	@Query(value="{ 'uniID' : ?0 }")
	List<UniGene2Go> findByUniID(String uniID);
	
	@Query(value="{ 'uniID' : ?0, 'taxID' : ?1 }")
	List<UniGene2Go> findByUniIDAndTaxID(String uniID, int taxID);
	
	@Query(value="{ 'goID' : ?0, 'taxID' : ?1 }")
	UniGene2Go findByGoIDAndTaxID(String goID, int taxID);
	
	@Query(value="{ 'goID' : ?0, 'taxID' : ?1, 'goID' : ?2 }")
	UniGene2Go findByUniIDAndTaxIDAndGOID(String geneID, int taxID, String goID);
	
}
