package com.novelbio.database.domain.geneanno;

public class DBInfo {
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
}
