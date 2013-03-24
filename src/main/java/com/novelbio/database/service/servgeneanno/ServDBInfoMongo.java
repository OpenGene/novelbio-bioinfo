package com.novelbio.database.service.servgeneanno;

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
	public DBInfo findByDBname(String dbName) {
		return repoDBinfo.findByDBname(dbName);
	}
	
	public DBInfo findOne(String dbInfoID) {
		return repoDBinfo.findOne(dbInfoID);
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
