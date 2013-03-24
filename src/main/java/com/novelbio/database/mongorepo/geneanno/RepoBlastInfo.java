package com.novelbio.database.mongorepo.geneanno;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.novelbio.database.domain.geneanno.BlastInfo;

public interface RepoBlastInfo extends PagingAndSortingRepository<BlastInfo, String> {

}
