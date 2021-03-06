package com.novelbio.database.service.servcosmic;

import java.util.List;

import com.novelbio.database.dao.cosmic.RepoCosmicAllSNVs;
import com.novelbio.database.model.cosmic.CosmicAllSNVs;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtCosmicAllSNVs {

	RepoCosmicAllSNVs repoCosmicAllSNVs = (RepoCosmicAllSNVs) SpringFactoryBioinfo.getBean("repoCosmicAllSNVs");
	
	public CosmicAllSNVs findCosmicAllSNVsByCosmicId(String cosmicId) {
		return repoCosmicAllSNVs.findCosmicAllSNVsByCosmicId(cosmicId);
	}
	public List<CosmicAllSNVs> findCosmicAllSNVsByCosmicId(String chr, long pos, String alt) {
		return repoCosmicAllSNVs.findCosmicAllSNVsByPosAndVar(chr, pos, alt);
	}
	public List<CosmicAllSNVs> findAll() {
		return repoCosmicAllSNVs.findAll();
	}
	public void save(CosmicAllSNVs nonCodingVars) {
		repoCosmicAllSNVs.save(nonCodingVars);
	}
	public void save(List<CosmicAllSNVs> nonCodingVars) {
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
