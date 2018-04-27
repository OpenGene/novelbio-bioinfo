package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.geneanno.Gene2Go;

public interface RepoGene2Go extends PagingAndSortingRepository<Gene2Go, String> {
	
	@Query(value="{ 'geneID' : ?0 }")
	List<Gene2Go> findByGeneID(long geneID);
	
	@Query(value="{ 'geneID' : ?0, 'taxID' : ?1 }")
	List<Gene2Go> findByGeneIDAndTaxID(long geneID, int taxID);
	
	//TODO 看下源码怎么实现的
	@Query(value="{ 'goID' : ?0, 'taxID' : ?1 }")
	Iterable<Gene2Go> findByGoIDAndTaxID(String goID, int taxID);
	
	@Query(value="{ 'geneID' : ?0, 'taxID' : ?1, 'goID' : ?2 }")
	Gene2Go findByGeneIDAndTaxIDAndGOID(long geneID, int taxID, String goID);
	
}
