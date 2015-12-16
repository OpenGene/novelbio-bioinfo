package com.novelbio.database.service.servcosmic;

import java.util.List;

import com.novelbio.database.domain.cosmic.CosmicCNV;
import com.novelbio.database.mongorepo.cosmic.RepoNCV;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtNCV {

	RepoNCV repoNCV = (RepoNCV) SpringFactoryBioinfo.getBean("repoNCV");
	
	public CosmicCNV findNCVByCosmicId(String cosmicId) {
		return repoNCV.findNCVByCosmicId(cosmicId);
	}
	public List<CosmicCNV> findAll() {
		return repoNCV.findAll();
	}
	public void save(CosmicCNV cosmicCNV) {
		repoNCV.save(cosmicCNV);
	}
	public void save(List<CosmicCNV> lsCosmicCNV) {
		repoNCV.save(lsCosmicCNV);
	}
	//懒汉模式的单例延迟
	static class MgmtNCVHolder {
		static MgmtNCV mgmtNCV = new MgmtNCV();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtNCV getInstance() {
		return MgmtNCVHolder.mgmtNCV;
	}
	
	
}
