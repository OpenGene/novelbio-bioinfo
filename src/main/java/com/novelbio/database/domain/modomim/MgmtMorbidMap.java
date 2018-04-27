package com.novelbio.database.domain.modomim;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.novelbio.database.dao.omim.RepoMorbidMap;
import com.novelbio.database.model.omim.MorbidMap;
import com.novelbio.database.service.SpringFactoryBioinfo;

@Repository
public class MgmtMorbidMap {
	private MgmtMorbidMap() {}
	RepoMorbidMap repoMorbidMap = (RepoMorbidMap)SpringFactoryBioinfo.getBean("repoMorbidMap");
	public List<MorbidMap> findInfByGeneId(int geneId){
		return repoMorbidMap.findInfByGeneId(geneId);
	}
	public List<MorbidMap> findInfByDisease(String disease){
		return repoMorbidMap.findInfByDisease(disease);
	}
	public List<MorbidMap> findAll(){
		return repoMorbidMap.findAll();
	}
	public void save(MorbidMap morbidMap){
		repoMorbidMap.save(morbidMap);
	}
	//懒汉模式的单例延迟--超牛逼
	static class MgmtMorbidMapHolder {
		static MgmtMorbidMap mgmtMorbidMap = new MgmtMorbidMap();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtMorbidMap getInstance() {
		return MgmtMorbidMapHolder.mgmtMorbidMap;
	}
}
