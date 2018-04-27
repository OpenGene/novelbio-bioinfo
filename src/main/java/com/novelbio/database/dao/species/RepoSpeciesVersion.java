package com.novelbio.database.dao.species;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.species.SpeciesVersion;

public interface RepoSpeciesVersion  extends PagingAndSortingRepository<SpeciesVersion, String>{
	
	@Query(value="{ 'taxId' : ?0 }")
	List<SpeciesVersion> findByTaxId(int taxId);
	
	@Query(value="{ 'taxId' : ?0,  'version' : ?1}")
	SpeciesVersion findByTaxIdAndVersion(int taxId, String version);
	
	@Query(value="{ 'fileTypes' : ?0}")
	List<SpeciesVersion> findByFileType(String fileType);
	
}
