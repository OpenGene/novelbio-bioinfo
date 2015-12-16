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
	@Query(value="{ 'chr' : ?0, 'pos' : ?1, 'ref' : ?2, 'alt' : ?3 }")
	CompleteExport findCompleteExportByPosAndVar(String chr, long pos,String ref, String alt);
	List<CompleteExport> findAll();
}
