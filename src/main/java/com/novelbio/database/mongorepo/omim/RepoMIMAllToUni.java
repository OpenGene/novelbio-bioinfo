package com.novelbio.database.mongorepo.omim;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.omim.MIMAllToUni;

public interface RepoMIMAllToUni extends PagingAndSortingRepository<MIMAllToUni, Integer>{
		
	List<MIMAllToUni> findAll();

	
}
