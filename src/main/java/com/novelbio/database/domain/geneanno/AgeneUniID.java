package com.novelbio.database.domain.geneanno;

import org.apache.ibatis.metadata.Database;
import org.apache.log4j.Logger;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ServDBInfo;
import com.novelbio.database.service.servgeneanno.ServNCBIUniID;

/**
 * 重写了equal和hash
 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
 */
public abstract class AgeneUniID {
	private static final Logger logger = Logger.getLogger(AgeneUniID.class);
	
    private int taxID;
	private String accessID;
	private String dbInfo;
	
	ServDBInfo servDBInfo = new ServDBInfo();
	ServNCBIUniID servNCBIUniID = new ServNCBIUniID();
	DBInfo databaseInfo;
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/** geneUniID是否不为0，null 和 "" */
	public boolean isValidGenUniID() {
		if (getGenUniID() == null || getGenUniID().equals("") || getGenUniID().equals("0")) {
			return false;
		}
		return true;
	}
	
	/** 返回GeneID.NCBIID等 */
	public abstract Integer getGeneIDtype();
	public abstract String getGenUniID();
	public abstract void setGenUniID(String genUniID);
	/**
	 * 如果是“”，则返回null
	 * @return
	 */
	public String getAccID() {
		if (accessID == null) {
			return null;
		}
		accessID = accessID.trim();
		if (accessID.equals("")) {
			return null;
		}
		return accessID;
	}
	public void setAccID(String accessID) {
		this.accessID = accessID;
	}  
	
	public DBInfo getDataBaseInfo() {
		fillDataBase();
		return databaseInfo;
	}
	
	public void setDataBaseInfo(String dbInfo) {
		this.dbInfo = dbInfo;
		fillDataBase();
	}
	
	private void fillDataBase() {
		if (this.dbInfo == null || this.dbInfo.equals("")) {
			this.databaseInfo = null;
			return;
		}
		
		if (this.databaseInfo == null) {
			DBInfo databaseInfo = new DBInfo();
			databaseInfo.setDbName(dbInfo);
			this.databaseInfo = servDBInfo.queryDBInfo(databaseInfo);
		}
		if (this.databaseInfo == null) {
			this.databaseInfo = new DBInfo();
			this.databaseInfo.setDbName(dbInfo);
		}
	}
	
	public void setDataBaseInfo(DBInfo databaseInfo) {
		this.databaseInfo = databaseInfo;
		if (databaseInfo == null) {
			this.dbInfo = null;
		} else {
			this.dbInfo = databaseInfo.getDbName();
		}		
	}
	
	/**
	 * <b>没有accID，放弃升级</b>
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级
	 * @param nCBIID
	 * @param override
	 */
	public void update(boolean overrideDBinfo) {
		servNCBIUniID.updateNCBIUniID(this, overrideDBinfo);
		servDBInfo.updateDBInfo(databaseInfo);
	}
	
	/**
	 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
	 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		AgeneUniID otherObj = (AgeneUniID)obj;
		if(!isValidGenUniID() && !isValidGenUniID()) {
			if (accessID == null && otherObj.accessID == null) {
				return true;
			} else if (accessID != null) {
				return accessID.equals(otherObj.accessID);
			} else if (otherObj.accessID != null) {
				return otherObj.accessID.equals(accessID);
			}
		} else if (isValidGenUniID()) {
			return getGenUniID().equals(otherObj.getGenUniID());
		} else if (otherObj.isValidGenUniID()) {
			return otherObj.getGenUniID().equals(getGenUniID());
		}
		return false;
	}
	/**
	 * 重写hashcode，也是仅针对geneID
	 */
	public int hashCode(){
		return getGenUniID().hashCode(); 
	}
	
	public static AgeneUniID creatAgeneUniID(int idType) {
		if (idType == GeneID.IDTYPE_GENEID) {
			return new NCBIID();
		} else if (idType == GeneID.IDTYPE_UNIID) {
			return new UniProtID();
		} else {
			logger.error("出现未知idType: " + idType);
			return null;
		}
	}
}
