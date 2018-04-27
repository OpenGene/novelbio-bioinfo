package com.novelbio.database.dao.cosmic;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.model.cosmic.CancerGene;
import com.novelbio.database.model.omim.GeneMIM;

public interface RepoCancerGene extends PagingAndSortingRepository<CancerGene, String>{
	
	@Query(value="{ 'geneId' : ?0 }")
	CancerGene findCancerGeneByGeneId(int geneId);
	List<CancerGene> findAll();
}
