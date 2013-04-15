package com.novelbio.database.mongorepo.geneanno;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.UniGeneInfo;

public interface RepoGo2Term extends PagingAndSortingRepository<Go2Term, String> {

	@Query(value="{ 'setQueryGoID' : ?0 }")
	Go2Term findByQueryGoID(String queryGoID);
	
	@Query(value="{ 'goID' : ?0 }")
	Go2Term findByGoID(String goID);
	
}
