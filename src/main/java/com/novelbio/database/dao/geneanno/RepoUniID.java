package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.geneanno.AgeneUniID;
import com.novelbio.database.model.geneanno.NCBIID;
import com.novelbio.database.model.geneanno.UniProtID;

public interface RepoUniID extends PagingAndSortingRepository<UniProtID, String> {
	
	@Query(value="{ 'uniID' : ?0 }")
	List<UniProtID> findByUniID(String uniID);
	
	@Query(value="{ 'uniID' : ?0, 'taxID' : ?1}")
	List<UniProtID> findByUniIDAndTaxId(String uniID, int taxID);
	
	@Query(value="{ 'accID' : ?0}")
	List<UniProtID> findByAccID(String accID);
	
	@Query(value="{ 'accID' : ?0 ,  'taxID' : ?1}")
	List<UniProtID> findByAccIDAndTaxID(String accID, int taxID);	
	
	@Query(value="{ 'uniID' : ?0 ,  'accID' : ?1}")
	List<UniProtID> findByUniIDAndAccID(String uniID, String accID);
	
	@Query(value="{ 'uniID' : ?0 ,  'accID' : ?1, 'taxID' : ?2}")
	UniProtID findByUniIDAndAccIDAndTaxID(String uniID, String accID, int taxID);

}
