package com.novelbio.database.mongorepo.kegg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.KGentry;

public interface RepoKEntry extends PagingAndSortingRepository<KGentry, String> {
	List<KGentry> findAll();
	
	@Query(value="{ 'name' : ?0}")
	List<KGentry> findByName(String name);
	
	@Query(value="{ 'name' : ?0, 'taxID' : ?1}")
	List<KGentry> findByNameAndTaxId(String name, int taxId);
	
	@Query(value="{ 'pathName' : ?0}")
	List<KGentry> findByPathName(String pathName);
	
	@Query(value="{ 'pathName' : ?0, 'parentID' : ?1}")
	List<KGentry> findByPathNameAndParentId(String pathName, int parentId);
	
	@Query(value="{ 'name' : ?0, 'pathName' : ?1}")
	List<KGentry> findByNamePath(String name, String pathName);
	
	@Query(value="{ 'name' : ?0, 'pathName' : ?1, 'entryId' : ?2}")
	KGentry findByNamePathAndId(String name, String pathName, int entryId);
	
	@Query(value="{ 'name' : ?0, 'pathName' : ?1, 'entryId' : ?2, 'taxID' : ?3}")
	List<KGentry> findByNamePathAndIdAndTaxId(String name, String pathName, int entryID, String taxId);
	
	@Query(value="{ 'pathName' : ?0, 'entryId' : ?1}")
	List<KGentry> findByPathNameAndEntryId(String pathName, int entryID);
	
	@Query(value="{ 'name' : ?0, 'pathName' : ?1, 'entryId' : ?2, 'reactionName' : ?3}")
	KGentry findByNamePathAndIdAndReaction(String name, String pathName, int entryId, String reaction);

}
