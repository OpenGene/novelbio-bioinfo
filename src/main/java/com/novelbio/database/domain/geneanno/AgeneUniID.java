package com.novelbio.database.domain.geneanno;

import org.apache.log4j.Logger;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageDBInfo;
import com.novelbio.database.service.servgeneanno.ManageNCBIUniID;

/**
 * 重写了equal和hash
 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
 */
public abstract class AgeneUniID {
	private static final Logger logger = Logger.getLogger(AgeneUniID.class);
	
	@Id
	private String id;
    private int taxID;
    /** 通通小写 */
	private String accID;
	/** 原始值，不小写 */
	private String accIDraw;
	
	@DBRef
	DBInfo databaseInfo;
	
	@Transient
	ManageDBInfo manageDBInfo = new ManageDBInfo();

	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
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
	/** 小写的 */
	public abstract String getGenUniID();
	public abstract void setGenUniID(String genUniID);
	/**
	 * 如果是“”，则返回null
	 * @return
	 */
	public String getAccID() {
		if (accIDraw == null) {
			return null;
		}
		accIDraw = accIDraw.trim();
		if (accIDraw.equals("")) {
			return null;
		}
		return accIDraw;
	}
	public void setAccID(String accessID) {
		if (accessID == null) {
			return;
		}
		this.accID = accessID.toLowerCase();
		this.accIDraw = accessID;
	}  
	
	public DBInfo getDataBaseInfo() {
		return databaseInfo;
	}
	
	public void setDataBaseInfoID(String dbInfoID) {
		if (dbInfoID == null || dbInfoID.equals("")) {
			return;
		}
		this.databaseInfo = manageDBInfo.findOne(dbInfoID);
	}
	/**
	 * 输入数据库的具体名字，譬如affy_U133等
	 * 然后会到数据库中查找具体的芯片型号等
	 * @param dbName
	 */
	public void setDataBaseInfo(String dbName) {
		if (dbName == null || dbName.equals("")) {
			return;
		}
		this.databaseInfo = manageDBInfo.findByDBname(dbName);
	}
	
	public void setDataBaseInfo(DBInfo databaseInfo) {
		if (databaseInfo != null) {
			this.databaseInfo = databaseInfo;
		}
	}
	
	/**
	 * <b>没有accID，放弃升级</b>
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级
	 * @param nCBIID
	 * @param override
	 */
	public boolean update(boolean overrideDBinfo) {
		if (!overrideDBinfo && getId() != null && !getId().equals("")) {
			return true;
		}
		ManageNCBIUniID manageNCBIUniID = new ManageNCBIUniID();
		return manageNCBIUniID.updateNCBIUniID(this, overrideDBinfo);
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
			if (accID == null && otherObj.accID == null) {
				return true;
			} else if (accID != null) {
				return accID.equals(otherObj.accID);
			} else if (otherObj.accID != null) {
				return otherObj.accID.equals(accID);
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
