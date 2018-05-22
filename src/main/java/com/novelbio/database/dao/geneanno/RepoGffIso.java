package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;

public interface RepoGffIso extends PagingAndSortingRepository<GffGeneIsoInfo, String> {
	
	@Query(value="{ 'gffFileId' : ?0 }")
	List<GffGeneIsoInfo> findByFileId(String gffFileId);
	
}