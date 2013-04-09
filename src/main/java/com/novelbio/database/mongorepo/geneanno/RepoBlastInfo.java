package com.novelbio.database.mongorepo.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.BlastInfo;

public interface RepoBlastInfo extends PagingAndSortingRepository<BlastInfo, String> {
	
	@Query(value="{ 'subjectID' : ?0, 'subjectTax' : ?1}")
	List<BlastInfo> findBySubID(String subjectID, int subjectTax);

	@Query(value="{ 'queryID' : ?0, 'queryTax' : ?1, 'subjectTax' : ?2}")
	List<BlastInfo> findByQueryIDAndSubTaxID(String queryID, int queryTax, int subjectTax);
	
	@Query(value="{ 'queryID' : ?0, 'queryTax' : ?1}")
	List<BlastInfo> findByQueryID(String queryID, int queryTax);
	
	@Query(value="{ 'queryID' : ?0, 'queryTax' : ?1, 'subjectID' : ?2, 'subjectTax' : ?3}")
	BlastInfo findByQueryIDAndSubID(String queryID, int queryTax, String subjectID, int subjectTax);
}
