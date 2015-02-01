package com.novelbio.database.service.servkegg;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.kegg.noGene.KGNCompInfo;
import com.novelbio.database.mongorepo.kegg.RepoKNCompInfo;
import com.novelbio.database.service.SpringFactoryBioinfo;

/**
 * keggID化合物的具体信息
 * @author zong0jie
 *
 */
@Service
public class ServKNCompInfo {
	RepoKNCompInfo repoKNCompInfo;
	public ServKNCompInfo() {
		repoKNCompInfo = (RepoKNCompInfo)SpringFactoryBioinfo.getFactory().getBean("repoKNCompInfo");
	}

	public KGNCompInfo findByKegId(String kegId) {
		return repoKNCompInfo.findOne(kegId);
	}
	
	public void save(KGNCompInfo kgnCompInfo) {
		repoKNCompInfo.save(kgnCompInfo);
	}
	
	static class ManageHolder {
		static ServKNCompInfo instance = new ServKNCompInfo();
	}
	
	public static ServKNCompInfo getInstance() {
		return ManageHolder.instance;
	}


}
