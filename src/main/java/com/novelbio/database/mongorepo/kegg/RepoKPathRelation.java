package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.KGpathRelation;

public interface RepoKPathRelation extends PagingAndSortingRepository<KGpathRelation, String> {
	
	@Query(value="{ 'pathName' : ?0}")
	public List<KGpathRelation> findByPathName(String pathName);
	
	@Query(value="{ 'pathName' : ?0, 'scrPath' : ?1,  'trgPath' : ?1}")
	public KGpathRelation findByPathNameSrcTrg(String pathName, String src, String trg);
	
}
