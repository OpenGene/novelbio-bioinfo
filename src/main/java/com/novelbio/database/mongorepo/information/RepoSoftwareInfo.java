package com.novelbio.database.mongorepo.information;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.information.SoftWareInfo;

public interface RepoSoftwareInfo extends PagingAndSortingRepository<SoftWareInfo, String> {
	
	@Query(value="{ 'softName' : ?0 }")
	SoftWareInfo findBySoftName(String softName);
	
}
