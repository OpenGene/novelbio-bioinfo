package com.novelbio.database.mongorepo.kegg;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.kegg.noGene.KGNCompInfo;
/**
 * kegg的化合物表
 * @author zong0jie
 *
 */
public interface RepoKNCompInfo extends PagingAndSortingRepository<KGNCompInfo, String> {
	
}
