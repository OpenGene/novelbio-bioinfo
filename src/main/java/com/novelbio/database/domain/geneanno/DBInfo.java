package com.novelbio.database.domain.geneanno;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.service.servgeneanno.ManageDBInfo;
@Document(collection = "dbinfo")
public class DBInfo {
	@Id
	String dbInfoID;
	/** 数据库名称 */
	@Indexed
	String dbName;
	String dbNameRaw;
	/** 数据库来源组织，譬如affy、NCBI等 */
	@Indexed
	String dbOrg;
	String dbOrgRaw;
	/** 数据库描述 */
	String description;
	
	public void setDbName(String dbName) {
		this.dbName = dbName.toLowerCase();
		this.dbNameRaw = dbName;
	}
	public void setDbOrg(String dbOrg) {
		this.dbOrg = dbOrg.toLowerCase();
		this.dbOrgRaw = dbOrg;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDbNameLowcase() {
		return dbName;
	}
	public String getDbName() {
		return dbNameRaw;
	}
	public String getDbOrg() {
		return dbOrgRaw;
	}
	public String getDescription() {
		return description;
	}
	public String getDbInfoID() {
		return dbInfoID;
	}
	public void setDbInfoID(String dbinfo) {
		this.dbInfoID = dbinfo;
	}
	
	public static void updateDBinfo(String dbInfoFile) {
		ManageDBInfo servDBInfo = new ManageDBInfo();
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
	
	public int hashCode() {
		return (SepSign.SEP_ID + dbName + SepSign.SEP_ID + dbOrg).hashCode();
	}
	
	/**
	 * 仅比较DBname和DBorg两项
	 */
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null) return false;
		
		if (getClass() != object.getClass()) return false;
		DBInfo otherObj = (DBInfo)object;
		if (dbName.equals(otherObj.dbName) && dbOrg.equals(otherObj.dbOrg)) {
			return true;
		}
		return false;
	}
}
