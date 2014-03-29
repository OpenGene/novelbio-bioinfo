package com.novelbio.database.mongorepo.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.BlastFileInfo;

public interface RepoBlastFileInfo extends PagingAndSortingRepository<BlastFileInfo, String> {
	List<BlastFileInfo> findAll();
	
	@Query(value="{ 'userId' : ?0}")
	List<BlastFileInfo> findByUserId(String userId);
	
	@Query(value="{ 'fileName' : ?0}")
	List<BlastFileInfo> findByFileName(String fileName);
}
