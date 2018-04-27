package com.novelbio.database.dao.species;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.species.TaxInfo;

public interface RepoTaxInfo extends PagingAndSortingRepository<TaxInfo, Integer>{

	@Query(value="{ 'taxID' : ?0 }")
	TaxInfo findByTaxID(int taxID);
	
	@Query(value="{ 'abbr' : ?0 }")
	List<TaxInfo> findByAbbr(String abbr);
	
	@Query(value="{ '$or': [{'$or': [{'$where' : 'function(){return (\"\" + this._id).indexOf(?0) > -1;}'}]}, {'$or': [{'abbr': {'$regex': '?0', '$options': 'i'}}]}, {'$or': [{'chnName': {'$regex': '?0', '$options': 'i'}}]}, {'$or': [{'comName': {'$regex': '?0', '$options': 'i'}}]}, {'$or': [{'latin': {'$regex': '?0', '$options': 'i'}}]}] }")
	Page<TaxInfo> findByFilter(Pageable pageable, String keyText);

	List<TaxInfo> findAll();

	Page<TaxInfo> findAll(Pageable pageable);
	
}
