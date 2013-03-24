package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.DBInfo;

public interface ServDBInfoInt {
	
	public DBInfo queryDBInfo(DBInfo dbInfo);

	public ArrayList<DBInfo> queryLsDBInfo(DBInfo dbInfo);

	public void insertDBInfo(DBInfo dbInfo);

	public void updateDBInfo(DBInfo dbInfo);
	
	/** insert和update一体化
	 * @param DBInfo必须要填充DBName和DBorg两项
	 */
	public void updateDBInfo(DBInfo dbInfo, boolean overlapDescription);
	

}
