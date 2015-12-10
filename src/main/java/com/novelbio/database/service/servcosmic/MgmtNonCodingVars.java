package com.novelbio.database.service.servcosmic;

import java.util.List;

import com.novelbio.database.domain.cosmic.NonCodingVars;
import com.novelbio.database.mongorepo.cosmic.RepoNonCodingVars;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtNonCodingVars {
	RepoNonCodingVars repoNonCodingVars = (RepoNonCodingVars) SpringFactoryBioinfo.getBean("repoNonCodingVars");
	public NonCodingVars findNonCodingVarsByCosmicId(String cosmicId) {
		return repoNonCodingVars.findNonCodingVarsByCosmicId(cosmicId);
	}
	
	public List<NonCodingVars> findAll() {
		return repoNonCodingVars.findAll();
	}
	public void save(NonCodingVars nonCodingVars) {
		repoNonCodingVars.save(nonCodingVars);
	}

	//懒汉模式的单例延迟
	static class MgmtNonCodingVarsHolder {
		static MgmtNonCodingVars mgmtNonCodingVars = new MgmtNonCodingVars();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtNonCodingVars getInstance() {
		return MgmtNonCodingVarsHolder.mgmtNonCodingVars;
	}
}
