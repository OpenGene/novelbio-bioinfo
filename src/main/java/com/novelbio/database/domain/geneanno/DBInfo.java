package com.novelbio.database.domain.geneanno;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.service.servgeneanno.ServDBInfo;

public class DBInfo {
	public static void main(String[] args) {
		updateDBinfo("/media/winE/NBCplatform/DBinfo.txt");
	}
	
	
	int dbInfoID = -1;
	/** 数据库名称 */
	String dbName;
	/** 数据库来源组织，譬如affy、NCBI等 */
	String dbOrg;
	/** 数据库描述 */
	String description;
	
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public void setDbOrg(String dbOrg) {
		this.dbOrg = dbOrg;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDbName() {
		return dbName;
	}
	public String getDbOrg() {
		return dbOrg;
	}
	public String getDescription() {
		return description;
	}
	public int getDbInfoID() {
		return dbInfoID;
	}
	public void setDbInfoID(int dbinfo) {
		this.dbInfoID = dbinfo;
	}
	
	public static void updateDBinfo(String dbInfoFile) {
		ServDBInfo servDBInfo = new ServDBInfo();
		TxtReadandWrite txtRead = new TxtReadandWrite(dbInfoFile, false);
		for (String content : txtRead.readlines()) {
			content = content.trim();
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			DBInfo dbInfo = new DBInfo();
			dbInfo.setDbName(ss[0]);
			dbInfo.setDbOrg(ss[1]);
			dbInfo.setDescription(ss[2]);
			servDBInfo.updateDBInfo(dbInfo, true);
		}
		txtRead.close();
	}
}
