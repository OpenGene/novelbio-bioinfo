package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.KGIDgen2Keg;

public interface RepoKIDgen2Keg extends PagingAndSortingRepository<KGIDgen2Keg, Long> {
	@Query(value="{ 'geneID' : ?0}")
	KGIDgen2Keg findByGeneId(long geneId);
	
	@Query(value="{ 'keggID' : ?0}")
	KGIDgen2Keg findByKegId(String keggId);
	
	@Query(value="{ 'geneID' : ?0, 'keggID' : ?1 }")
	KGIDgen2Keg findByGeneIdAndKegId(long geneId, String keggId);
	
	@Query(value="{ 'geneID' : ?0, 'taxID' : ?1}")
	List<KGIDgen2Keg> findByGeneIdAndTaxId(long geneId, int taxId);
	
	@Query(value="{ 'geneID' : ?0, 'taxID' : ?1, 'keggID' : ?2}")
	KGIDgen2Keg findByGeneIdAndTaxIdAndKegId(long geneId, int taxId, String keggId);



	
}
