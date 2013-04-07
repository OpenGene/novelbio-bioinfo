package com.novelbio.database.service.servgeneanno;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.mongorepo.geneanno.RepoDBinfo;
import com.novelbio.database.service.SpringFactory;

public class ManageDBInfo {
	static Map<String, DBInfo> mapDBid2DBinfo = new HashMap<String, DBInfo>();
	static Map<String, DBInfo> mapDBName2DBinfo = new HashMap<String, DBInfo>();
	
	@Inject
	RepoDBinfo repoDBinfo;
		
	public ManageDBInfo() {
		repoDBinfo = (RepoDBinfo)SpringFactory.getFactory().getBean("repoDBinfo");
		for (DBInfo dbInfo : repoDBinfo.findAll()) {
			mapDBid2DBinfo.put(dbInfo.getDbInfoID(), dbInfo);
			mapDBName2DBinfo.put(dbInfo.getDbInfoID(), dbInfo);
		}
	}
	
	/**
	 * 用dbName去查询
	 * @return
	 */
	public DBInfo findByDBname(String dbName) {
		return mapDBName2DBinfo.get(dbName);
	}
	
	public DBInfo findOne(String dbInfoID) {
		return mapDBid2DBinfo.get(dbInfoID);
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
			dbInfo = repoDBinfo.save(dbInfo);
			mapDBid2DBinfo.put(dbInfo.getDbInfoID(), dbInfo);
			mapDBName2DBinfo.put(dbInfo.getDbName(), dbInfo);
		} else {
			if (overlapDescription) {
				dbInfoS.setDescription(dbInfo.getDescription());
				repoDBinfo.save(dbInfoS);
				mapDBid2DBinfo.put(dbInfoS.getDbInfoID(), dbInfo);
				mapDBName2DBinfo.put(dbInfoS.getDbName(), dbInfo);
			}
		}
	}
	
	
}
