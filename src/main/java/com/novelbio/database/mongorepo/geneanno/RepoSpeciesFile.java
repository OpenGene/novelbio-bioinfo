package com.novelbio.database.mongorepo.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.SpeciesFile;

public interface RepoSpeciesFile extends PagingAndSortingRepository<SpeciesFile, String>{

	@Query(value="{ 'taxID' : ?0 }")
	List<SpeciesFile> findByTaxID(int taxID);
	
	@Query(value="{ 'taxID' : ?0, 'version' : ?1 }")
	SpeciesFile findByTaxIDAndVersion(int taxID, String version);

}
