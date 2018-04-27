package com.novelbio.database.dao.species;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.species.SpeciesGff;
import com.novelbio.database.model.species.SpeciesVersion;

public interface RepoSpeciesGff  extends PagingAndSortingRepository<SpeciesGff, String>{
	
	@Query(value="{ 'taxId' : ?0,  'version' : ?1}")
	List<SpeciesGff> findByTaxIdAndVersion(int taxId, String version);
	
}
