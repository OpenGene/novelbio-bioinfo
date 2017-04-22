package com.novelbio.database.mongorepo.geneanno;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.analysis.seq.genome.gffOperate.GffFile;

public interface RepoGffFile extends PagingAndSortingRepository<GffFile, String> {
	
	@Query(value="{ 'taxID' : ?0, 'version' : ?1, 'dbinfo' : ?2 }")
	GffFile findByTaxIdAndVersionAndDbinfo(int taxID, String version, String dbinfo);
	
	 Page<GffFile> findAll(Pageable bePageable);
	
	List<GffFile> findAll();
}