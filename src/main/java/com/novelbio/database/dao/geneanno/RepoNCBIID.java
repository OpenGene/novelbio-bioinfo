package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.geneanno.NCBIID;

public interface RepoNCBIID extends PagingAndSortingRepository<NCBIID, String> {
	
	@Query(value="{ 'geneID' : ?0 }")
	List<NCBIID> findByGeneID(long geneID);
	
	@Query(value="{ 'geneID' : ?0, 'taxID' : ?1}")
	List<NCBIID> findByGeneIDAndTaxId(long geneID, int taxID);
	
	@Query(value="{ 'accID' : ?0}")
	List<NCBIID> findByAccID(String accID);
	
	@Query(value="{ 'accID' : ?0 ,  'taxID' : ?1}")
	List<NCBIID> findByAccIDAndTaxID(String accID, int taxID);
	
	@Query(value="{'taxID' : ?0}")
	List<NCBIID> findByTaxID(int taxID);
	
	@Query(value="{ 'geneID' : ?0 ,  'accID' : ?1}")
	List<NCBIID> findByGeneIDAndAccID(long geneID, String accID);
	
	@Query(value="{ 'geneID' : ?0 ,  'accID' : ?1, 'taxID' : ?2}")
	NCBIID findByGeneIDAndAccIDAndTaxID(long geneID, String accID, int taxID);

}
