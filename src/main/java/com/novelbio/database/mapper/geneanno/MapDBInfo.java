package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.mapper.MapperSql;

public interface MapDBInfo  extends MapperSql {

	public DBInfo queryDBInfo(DBInfo dbInfo);

	public ArrayList<DBInfo> queryLsDBInfo(DBInfo dbInfo);
	
	public void insertDBInfo(DBInfo dbInfo);
	
	/**
	 * 用geneID查找，升级全部项目，
	 * @param geneInfo
	 */
	public void updateDBInfo(DBInfo dbInfo);

}
