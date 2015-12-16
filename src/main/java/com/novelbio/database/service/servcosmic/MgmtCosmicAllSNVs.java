package com.novelbio.database.service.servcosmic;

import java.util.List;

import com.novelbio.database.domain.cosmic.CosmicAllSNVs;
import com.novelbio.database.mongorepo.cosmic.RepoCosmicAllSNVs;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtCosmicAllSNVs {

	RepoCosmicAllSNVs repoCosmicAllSNVs = (RepoCosmicAllSNVs) SpringFactoryBioinfo.getBean("repoCosmicAllSNVs");
	
	public CosmicAllSNVs findCosmicAllSNVsByCosmicId(String cosmicId) {
		return repoCosmicAllSNVs.findCosmicAllSNVsByCosmicId(cosmicId);
	}
	public CosmicAllSNVs findCosmicAllSNVsByCosmicId(String chr, long pos, String ref, String alt) {
		return repoCosmicAllSNVs.findCosmicAllSNVsByPosAndVar(chr, pos, ref, alt);
	}
	public List<CosmicAllSNVs> findAll() {
		return repoCosmicAllSNVs.findAll();
	}
	public void save(CosmicAllSNVs nonCodingVars) {
		repoCosmicAllSNVs.save(nonCodingVars);
	}

	//懒汉模式的单例延迟
	static class MgmtCosmicAllSNVsHolder {
		static MgmtCosmicAllSNVs mgmtCosmicAllSNVs = new MgmtCosmicAllSNVs();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtCosmicAllSNVs getInstance() {
		return MgmtCosmicAllSNVsHolder.mgmtCosmicAllSNVs;
	}
}
