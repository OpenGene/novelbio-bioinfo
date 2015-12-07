package com.novelbio.database.mongorepo.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.cosmic.CompleteExport;

public interface RepoCompleteExport extends PagingAndSortingRepository<CompleteExport, String>{
	@Query(value="{ 'geneId' : ?0 }")
	List<CompleteExport> findCompleteExportByGeneId(int geneId);
	@Query(value="{ 'cosmicId' : ?0 }")
	CompleteExport findCompleteExportByCosmicId(int cosmicId);
	List<CompleteExport> findAll();
	
}
