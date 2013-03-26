package com.novelbio.database.mongorepo.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.BlastInfo;

public interface RepoBlastInfo extends PagingAndSortingRepository<BlastInfo, String> {
	
	@Query(value="{ 'queryID' : ?0 }")
	List<BlastInfo> findByDBorg(String queryID);
	
	@Query(value="{ 'subjectID' : ?0}")
	List<BlastInfo> findByDBname(String subjectID);

	@Query(value="{ 'queryID' : ?0, 'queryTax' : ?1, 'subjectTax' : ?2}")
	List<BlastInfo> findByDBname(String queryID, int queryTax, int subjectTax);
	
}
