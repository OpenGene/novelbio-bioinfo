package com.novelbio.database.domain.modomim;

import java.util.List;

import com.novelbio.database.dao.omim.RepoGeneMIMInfo;
import com.novelbio.database.dao.omim.RepoMIMInfo;
import com.novelbio.database.domain.modomim.MgmtGeneMIMInfo.MgmtOmimHolder;
import com.novelbio.database.model.omim.MIMInfo;
import com.novelbio.database.service.SpringFactoryBioinfo;

public class MgmtOMIMUnit {

	private MgmtOMIMUnit () {}
	RepoMIMInfo repoMIMInfo = (RepoMIMInfo)SpringFactoryBioinfo.getBean("repoMIMInfo");
	public MIMInfo findByMimId(int MimId){
		return repoMIMInfo.findInfByMimId(MimId);
	}
	public List<MIMInfo> findAll(){
		return repoMIMInfo.findAll();
	}
	public void save(MIMInfo mIMInfo){
		repoMIMInfo.save(mIMInfo);
	}
	//懒汉模式的单例延迟--超牛逼
	static class MgmtOmimUnitHolder {
		static MgmtOMIMUnit mgmtOMIMUnit = new MgmtOMIMUnit();
	}
	/** 
	 * 获得
	 * @return
	 */
	public static MgmtOMIMUnit getInstance() {
		return MgmtOmimUnitHolder.mgmtOMIMUnit;
	}
}