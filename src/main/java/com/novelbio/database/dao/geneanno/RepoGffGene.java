package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.bioinfo.gff.GffGene;

public interface RepoGffGene extends PagingAndSortingRepository<GffGene, String> {
	
	@Query(value="{ 'geneID' : ?0 }")
	GffGene findByGeneID(long geneID);

	@Query(value="{ 'gffFileId' : ?0}")
	List<GffGene> findByFileId(String gffFileId);

	@Query(value="{'gffFileId' : ?0, 'parentName' : ?1, 'numberstart' : {'$lte': ?3}, 'numberend' : {'$gte': ?2} }")
	List<GffGene> findByFileId_ChrId_RegionOverlap(String gffFileId, String chrID, int from, int to, Sort sort);
	
	@Query(value="{'gffFileId' : ?0, 'parentName' : ?1, 'numberstart' : {'$gte': ?2}, 'numberend' : {'$lte': ?3} }")
	List<GffGene> findByFileId_ChrId_RegionCover(String gffFileId, String chrID, int from, int to, Sort sort);
	
	@Query(value="{ 'gffFileId' : ?0, 'setNameLowcase' :  {'$regex' : ?1 }}")
	List<GffGene>findByFileId_Name_Regex(String gffFileId, String nameRegexLowcase);
	
	@Query(value="{'gffFileId' : ?0, 'setNameLowcase' : ?1}")
	List<GffGene> findByFileId_Name_Exact(String gffFileId, String nameExactLowcase);
	
}
