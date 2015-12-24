package com.novelbio.database.service.servcosmic;

import java.util.List;

import com.novelbio.database.domain.cosmic.CosmicAbb;
import com.novelbio.database.mongorepo.cosmic.RepoCosmicAbb;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtCosmicAbb {

	RepoCosmicAbb repoCosmicAbb = (RepoCosmicAbb) SpringFactoryBioinfo.getBean("repoCosmicAbb");

	public CosmicAbb findTermByAbbreviation(String abbreviation) {
		return repoCosmicAbb.findTermByAbbreviation(abbreviation);
	}
	public List<CosmicAbb> findAll() {
		return repoCosmicAbb.findAll(); 
	}
	public void save(List<CosmicAbb> lsCosmicAbb) {
		repoCosmicAbb.save(lsCosmicAbb);
	}
	
	//懒汉模式的单例延迟
	static class MgmtCosmicAbbHolder {
		static MgmtCosmicAbb mgmtCosmicAbb = new MgmtCosmicAbb();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtCosmicAbb getInstance() {
		return MgmtCosmicAbbHolder.mgmtCosmicAbb;
	}
	
}
