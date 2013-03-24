package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.mongorepo.geneanno.RepoDBinfo;
import com.novelbio.database.service.SpringFactory;

public class ServDBInfoMongo {

	@Inject
	RepoDBinfo repoDBinfo;
	
	public ServDBInfoMongo() {
		repoDBinfo = (RepoDBinfo)SpringFactory.getFactory().getBean("repoDBinfo");
	}
	
	/**
	 * 用dbName去查询
	 * @return
	 */
	public DBInfo queryDBinfo(DBInfo dbInfo) {
		return repoDBinfo.findByDBname(dbInfo.getDbName());
	}
	
	public DBInfo queryDBinfoByID(DBInfo dbInfo) {
		return repoDBinfo.findOne(dbInfo.getDbInfoID());
	}
	
	/** insert和update一体化
	 * @param DBInfo必须要填充DBName和DBorg两项
	 * 仅用dbName去查寻
	 */
	public void updateDBInfo(DBInfo dbInfo, boolean overlapDescription) {
		if (dbInfo.getDbName() == null || dbInfo.getDbName().equals("") || dbInfo.getDbOrg() == null || dbInfo.getDbOrg().equals("")) {
			return;
		}
		DBInfo dbInfoS = repoDBinfo.findByDBname(dbInfo.getDbName());
		if (dbInfoS == null) {
			repoDBinfo.save(dbInfo);
		} else {
			if (overlapDescription) {
				dbInfoS.setDescription(dbInfo.getDescription());
				repoDBinfo.save(dbInfoS);
			}
		}
	}
	
	
}
