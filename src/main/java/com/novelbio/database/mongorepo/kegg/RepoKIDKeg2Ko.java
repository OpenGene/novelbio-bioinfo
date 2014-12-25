package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.KGIDkeg2Ko;

public interface RepoKIDKeg2Ko extends PagingAndSortingRepository<KGIDkeg2Ko, String> {
	
	@Query(value="{ 'keggID' : ?0}")
	public List<KGIDkeg2Ko> findyLsByKegId(String keggID);
	
	@Query(value="{ 'keggID' : ?0, 'taxID' : ?1}")
	public List<KGIDkeg2Ko> findyLsByKegIdAndTaxId(String kegID, int taxID);
	
	@Query(value="{ 'ko' : ?0, 'taxID' : ?1}")
	public List<KGIDkeg2Ko> findyLsByKoAndTaxId(String koId, int taxID);
	
	@Query(value="{ 'keggID' : ?0, 'ko' : ?1}")
	public KGIDkeg2Ko findByKegIdAndKo(String keggID, String Ko);

}
