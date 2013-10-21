package com.novelbio.database.service.servgeneanno;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.mongorepo.geneanno.RepoDBinfo;
import com.novelbio.database.service.SpringFactory;

public class ManageDBInfo {
	static double[] lock = new double[0];
	static Map<String, DBInfo> mapDBid2DBinfo;
	static Map<String, DBInfo> mapDBName2DBinfo;
	
	@Autowired
	RepoDBinfo repoDBinfo;
		
	private ManageDBInfo() {
		repoDBinfo = (RepoDBinfo)SpringFactory.getFactory().getBean("repoDBinfo");
		fillMap();
	}
	
	private void fillMap() {
		if (mapDBid2DBinfo != null) {
			return;
		}
		mapDBid2DBinfo = new ConcurrentHashMap<String, DBInfo>();
		mapDBName2DBinfo = new ConcurrentHashMap<String, DBInfo>();
		for (DBInfo dbInfo : repoDBinfo.findAll()) {
			mapDBid2DBinfo.put(dbInfo.getDbInfoID(), dbInfo);
			mapDBName2DBinfo.put(dbInfo.getDbNameLowcase(), dbInfo);
		}
	}
	/**
	 * 用dbName去查询
	 * @return
	 */
	public DBInfo findByDBname(String dbName) {
		return mapDBName2DBinfo.get(dbName.toLowerCase());
	}
	
	public DBInfo findOne(String dbInfoID) {
		return mapDBid2DBinfo.get(dbInfoID);
	}
	
	/** insert和update一体化
	 * @param DBInfo必须要填充DBName和DBorg两项
	 * @param overlapDescription 是否覆盖描述
	 * 仅用dbName去查寻
	 */
	public void updateDBInfo(DBInfo dbInfo, boolean overlapDescription) {
		if (dbInfo.getDbName() == null || dbInfo.getDbName().equals("") || dbInfo.getDbOrg() == null || dbInfo.getDbOrg().equals("")) {
			return;
		}
		synchronized (lock) {
			DBInfo dbInfoS = findByDBname(dbInfo.getDbName());
			boolean update = false;
			if (dbInfoS == null) {
				update = true;
				dbInfoS = dbInfo;
			} else {
				if (overlapDescription && !dbInfoS.getDbInfoID().equals(dbInfo.getDbInfoID())) {
					dbInfoS.setDescription(dbInfo.getDescription());
					update = true;
				}
			}
			if (update) {
				dbInfoS = repoDBinfo.save(dbInfoS);
				mapDBid2DBinfo.put(dbInfoS.getDbInfoID(), dbInfoS);
				mapDBName2DBinfo.put(dbInfoS.getDbNameLowcase(), dbInfoS);
			}
		}
	}
	
	static class ManageHolder {
		static ManageDBInfo manageDBInfo = new ManageDBInfo();
	}
	
	public static ManageDBInfo getInstance() {
		return ManageHolder.manageDBInfo;
	}
}
