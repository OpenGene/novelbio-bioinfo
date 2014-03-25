package com.novelbio.database.mongorepo.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;

public interface RepoGffIso extends PagingAndSortingRepository<GffGeneIsoInfo, String> {
	
	@Query(value="{ 'gffFileId' : ?0 }")
	List<GffGeneIsoInfo> findByFileId(String gffFileId);
	
}