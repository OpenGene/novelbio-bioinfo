package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.geneanno.SpeciesFile;

public interface RepoSpeciesFile extends PagingAndSortingRepository<SpeciesFile, String>{

	@Query(value="{ 'taxID' : ?0 }")
	List<SpeciesFile> findByTaxID(int taxID);
	
	@Query(value="{ 'taxID' : ?0, 'version' : ?1 }")
	SpeciesFile findByTaxIDAndVersion(int taxID, String version);
	
}
