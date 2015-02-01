package com.novelbio.database.model.modomim;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.novelbio.database.domain.omim.MIMAllToUni;
import com.novelbio.database.domain.omim.OmimGeneMap;
import com.novelbio.database.model.modomim.MgmtOMIM.MgmtOmimHolder;
import com.novelbio.database.mongorepo.omim.RepoMIMAllToUni;
import com.novelbio.database.service.SpringFactoryBioinfo;


public class MgmtMIMAllToUni {

	private MgmtMIMAllToUni() {}
	RepoMIMAllToUni repoMIMAllToUni = (RepoMIMAllToUni)SpringFactoryBioinfo.getBean("repoMIMAllToUni");
	public List<MIMAllToUni> findInfByMimId(int MimId){
		return repoMIMAllToUni.findInfByMimId(MimId);
	}
	public List<MIMAllToUni> findAll(){
		return repoMIMAllToUni.findAll();
	}
	public void save(MIMAllToUni oIMAllToUni){
		repoMIMAllToUni.save(oIMAllToUni);
	}
	
	//懒汉模式的单例延迟--超牛逼
	static class MgmtMIMAllToUniHolder {
		static MgmtMIMAllToUni mgmtMIMAllToUni = new MgmtMIMAllToUni();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtMIMAllToUni getInstance() {
		return MgmtMIMAllToUniHolder.mgmtMIMAllToUni;
	}
}
