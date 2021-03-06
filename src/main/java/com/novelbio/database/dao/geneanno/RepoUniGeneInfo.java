package com.novelbio.database.dao.geneanno;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.geneanno.UniGeneInfo;

public interface RepoUniGeneInfo extends PagingAndSortingRepository<UniGeneInfo, String> {

	@Query(value="{ 'uniID' : ?0 }")
	UniGeneInfo findByUniID(String uniID);
	
	@Query(value="{ 'uniID' : ?0, 'taxID' : ?1 }")
	UniGeneInfo findByUniIDAndTaxID(String uniID, int taxID);

}
