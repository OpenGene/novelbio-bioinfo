package com.novelbio.database.mongorepo.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.TaxInfo;

public interface RepoTaxInfo extends PagingAndSortingRepository<TaxInfo, Integer>{

	@Query(value="{ 'taxID' : ?0 }")
	TaxInfo findByTaxID(int taxID);
	
	@Query(value="{ 'abbr' : ?0 }")
	List<TaxInfo> findByAbbr(String abbr);

}
