package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.geneanno.DBInfo;

public interface RepoDBinfo  extends PagingAndSortingRepository<DBInfo, String> {
	
	@Query(value="{ 'dbOrg' : ?0 }")
	List<DBInfo> findByDBorg(String dborg);
	
	@Query(value="{ 'dbName' : ?0}")
	DBInfo findByDBname(String dbName);

}
