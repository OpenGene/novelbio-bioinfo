package com.novelbio.database.mongorepo.geneanno;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.Person;

public interface RepoPerson extends PagingAndSortingRepository<Person, String> {
	@Query(value="{ 'name' : ?0 }")
	Person findByName(String name);
	
	
	@Query(value="{ 'setInfo' : ?0 }")
	Person findByInfo(String name);
}
