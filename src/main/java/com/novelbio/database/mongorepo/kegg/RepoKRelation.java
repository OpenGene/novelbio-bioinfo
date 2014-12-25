package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.KGrelation;


public interface RepoKRelation extends PagingAndSortingRepository<KGrelation, String> {
	
	/**
	 * 用pathname,entry1,entry2,type,sybtypeName中任意组合去查找relation表
	 * @param kGrelation
	 * @return
	 */
	@Query(value="{ 'pathName' : ?0}")
	public List<KGrelation> queryLsKGrelations(String pathName);
	
	@Query(value="{ 'pathName' : ?0, 'entry1' : ?1}")
	public List<KGrelation> findByPathNameAndEntry1Id(String pathName, int entry1Id);

	@Query(value="{ 'pathName' : ?0, 'entry2' : ?1}")
	public List<KGrelation> findByPathNameAndEntry2Id(String pathName, int entry2Id);
	
	@Query(value="{ 'pathName' : ?0,  'entry1' : ?1, 'entry2' : ?2}")
	public List<KGrelation> findByPathNameAndEntry1IdAndEntry2Id(String pathName, int entry1Id, int entry2Id);

	@Query(value="{ 'pathName' : ?0,  'entry1' : ?1, 'entry2' : ?2, 'type' : ?3}")
	public KGrelation findByPathNameAndEntry1IdAndEntry2IdAndType(
			String pathName, int entry1Id, int entry2Id, String type);
	
}
