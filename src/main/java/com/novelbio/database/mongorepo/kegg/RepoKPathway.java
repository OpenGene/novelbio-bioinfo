package com.novelbio.database.mongorepo.kegg;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.KGpathway;

public interface RepoKPathway extends PagingAndSortingRepository<KGpathway, String> {
	List<KGpathway> findAll();
}
