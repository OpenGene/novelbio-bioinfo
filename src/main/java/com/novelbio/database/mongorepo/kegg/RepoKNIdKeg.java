package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;

public interface RepoKNIdKeg extends PagingAndSortingRepository<KGNIdKeg, String> {

	@Query(value="{ 'kegID' : ?0}")
	public List<KGNIdKeg> findByKegId(String kegID);
	
	public List<KGNIdKeg> findAll();

	
}
