package com.novelbio.database.mongorepo.omim;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.omim.MIMInfo;

public interface RepoMIMInfo extends PagingAndSortingRepository<MIMInfo, String>{
	/**
	 * 
	 * @param 
	 * @param 
	 * @return
	 */
	@Query(value="{ 'MimId' : ?0 }")
	MIMInfo findInfByMimId(int MimId);
	
	List<MIMInfo> findAll();
	
	
}
