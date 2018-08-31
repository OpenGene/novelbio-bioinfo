package com.novelbio.database.dao.geneanno;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.bioinfo.gff.GffIso;

public interface RepoGffIso extends PagingAndSortingRepository<GffIso, String> {
	
	@Query(value="{ 'gffFileId' : ?0 }")
	List<GffIso> findByFileId(String gffFileId);
	
}