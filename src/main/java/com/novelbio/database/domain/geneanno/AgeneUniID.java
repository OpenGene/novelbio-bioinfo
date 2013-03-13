package com.novelbio.database.domain.geneanno;

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
	/** 返回GeneID.NCBIID等 */
	public abstract String getGeneIDtype();
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
		if (dbInfo == null || dbInfo.equals("")) {
			return databaseInfo;
		}
		if (databaseInfo == null) {
			DBInfo dbInfo = new DBInfo();
			dbInfo.setDbName(this.dbInfo);
			databaseInfo = servDBInfo.queryDBInfo(dbInfo);
		}
		return databaseInfo;
	}
	
	public void setDBInfo(String dbInfo) {
		this.dbInfo = dbInfo;
	}
	
	public void setDataBaseInfo(DBInfo dbInfo) {
		this.dbInfo = dbInfo.getDbName();
		this.databaseInfo = dbInfo;
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
		if(getGenUniID() == null || getGenUniID().trim().equals("") || otherObj.getGenUniID() == null || otherObj.getGenUniID().trim().equals("")) {
			return false;
		}
		if (getGenUniID().equals("0") || otherObj.getGenUniID().equals("0") ) {
			return false;
		}
		return getGenUniID().equals(otherObj.getGenUniID());
	}
	/**
	 * 重写hashcode，也是仅针对geneID
	 */
	public int hashCode(){ 
		return getGenUniID().hashCode(); 
	}
	
	public static AgeneUniID creatAgeneUniID(String idType) {
		if (idType.equals(GeneID.IDTYPE_GENEID)) {
			return new NCBIID();
		} else if (idType.equals(GeneID.IDTYPE_UNIID)) {
			return new UniProtID();
		} else {
			logger.error("出现未知idType: " + idType);
			return null;
		}
	}
}
