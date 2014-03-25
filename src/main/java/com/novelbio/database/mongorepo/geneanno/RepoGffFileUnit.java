package com.novelbio.database.mongorepo.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.analysis.seq.genome.gffOperate.GffFileUnit;

public interface RepoGffFileUnit extends PagingAndSortingRepository<GffFileUnit, String> {	
	@Query(value="{ 'gffFileId' : ?0, 'chrId': ?1, 'trunkNum': ?2 }")
	GffFileUnit findByFileIdAndChrIdAndTruncNum(String gffFileId, String chrId, int trunkNum);
	
	@Query(value="{ 'taxVsDb' : ?0, 'chrId': ?1, 'trunkNum': ?2 }")
	GffFileUnit findByTaxVsDbAndChrIdAndTruncNum(String taxVsDb, String chrId, int trunkNum);

	@Query(value="{ 'gffFileId' : ?0}")
	List<GffFileUnit> findByFileId(String gffFileId);
	
	@Query(value="{ 'taxVsDb' : ?0}")
	List<GffFileUnit> findByTaxVsDb(String taxVsDb);

	@Query(value="{ 'gffFileId' : ?0, 'chrId': ?1}")
	List<GffFileUnit> findByFileIdAndChrId(String gffFileId, String chrId);
	
	@Query(value="{ 'taxVsDb' : ?0, 'chrId': ?1}")
	List<GffFileUnit> findByTaxVsDbAndChrId(String taxVsDb, String chrId);
	
}