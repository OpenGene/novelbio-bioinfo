package com.novelbio.database.mongotestdao;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.mongotestmodel.MongoPerson;


public interface MongoPersonRep extends PagingAndSortingRepository<MongoPerson, String>{
	
}
