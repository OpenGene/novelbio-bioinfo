package com.novelbio.database.mongorepo.geneanno;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.UniGeneInfo;

public interface RepoGo2Term extends PagingAndSortingRepository<Go2Term, String> {

	@Query(value="{ 'queryGoID' : ?0 }")
	UniGeneInfo findByQueryGoID(String queryGoID);
	
	@Query(value="{ 'GoID' : ?0 }")
	UniGeneInfo findByGoID(String GoID);
	
}
