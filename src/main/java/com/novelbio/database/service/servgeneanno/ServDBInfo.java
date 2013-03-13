package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.mapper.geneanno.MapDBInfo;
import com.novelbio.database.service.SpringFactory;

@Service
public class ServDBInfo {
	@Inject
	MapDBInfo mapDBInfo;
	
	public ServDBInfo() {
		mapDBInfo = (MapDBInfo)SpringFactory.getFactory().getBean("mapDBInfo");
	}

	public DBInfo queryDBInfo(DBInfo dbInfo) {
		return mapDBInfo.queryDBInfo(dbInfo);
	}

	public ArrayList<DBInfo> queryLsDBInfo(DBInfo dbInfo) {
		return mapDBInfo.queryLsDBInfo(dbInfo);
	}

	public void insertDBInfo(DBInfo dbInfo) {
		mapDBInfo.insertDBInfo(dbInfo);
	}

	public void updateDBInfo(DBInfo dbInfo) {
		updateDBInfo(dbInfo);
	}
	
	/** insert和update一体化
	 * @param DBInfo必须要填充DBName和DBorg两项
	 */
	public void updateDBInfo(DBInfo dbInfo, boolean overlapDescription) {
		if (dbInfo.getDbName() == null || dbInfo.getDbName().equals("") || dbInfo.getDbOrg() == null || dbInfo.getDbOrg().equals("")) {
			return;
		}
		List<DBInfo> lsDbInfos = queryLsDBInfo(dbInfo);
		if (lsDbInfos == null || lsDbInfos.size() == 0) {
			insertDBInfo(dbInfo);
		} else {
			updateDBInfo(dbInfo);
		}
		updateDBInfo(dbInfo);
	}
	
}
