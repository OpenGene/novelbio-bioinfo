package com.novelbio.database.domain.geneanno;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

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
	private Set<String> accID;
	/** 原始值，不小写 */
	private String accIDraw;
	
	String dbInfoID;
	
	@Transient
	ManageDBInfo manageDBInfo = ManageDBInfo.getInstance();
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public Set<String> getSetAccID() {
		return accID;
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
	
	public void setAccIdRaw(String accIdRaw) {
		this.accIDraw = accIdRaw;
	}
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
	/** 不需要去掉最后的点，内部会去一次点 */
	public void setAccID(String accessID) {
		setAccID(accessID, true);
	}
	/** 不需要去掉最后的点，内部会去一次点 */
	public void setAccID(String accessID, boolean removeDot) {
		addAccID(false, accessID, removeDot);
	}
	/** 不需要去掉最后的点，内部会去一次点 */
	public void addAccID(String accessID) {
		addAccID(true, accessID, true);
	}
	/** 不需要去掉最后的点，内部会去一次点 */
	public void addAccID(String accessID, boolean removeDot) {
		addAccID(true, accessID, removeDot);
	}
	/** 不需要去掉最后的点，内部会去一次点 */
	private void addAccID(boolean isAdd,String accessID, boolean removeDot) {
		if (accessID == null || accessID.equals("")) {
			return;
		}
		if (!isAdd) {
			accID = new HashSet<>();
		}
	
		String accessIDlowcase = accessID.toLowerCase();
		this.accID.add(accessIDlowcase);
		if (removeDot) {
			String accIDremoveDot = GeneID.removeDot(accessIDlowcase);
			accID.add(accIDremoveDot);
		}
		if (removeDot && accID.size() > 2) {
			this.accIDraw = GeneID.removeDot(accessID);
		} else {
			this.accIDraw = accessID;
		}
	}
	public DBInfo getDataBaseInfo() {
		ManageDBInfo manageDBInfo = ManageDBInfo.getInstance();
		return manageDBInfo.findOne(dbInfoID);
	}
	
	/** 是否添加了，false表示不需要添加 */
	public boolean setDataBaseInfoID(String dbInfoID) {
		if (dbInfoID == null || dbInfoID.equals("")) {
			return false;
		}
		DBInfo databaseInfo = manageDBInfo.findOne(dbInfoID);
		return setDataBaseInfo(databaseInfo);
	}
	/**
	 * 输入数据库的具体名字，譬如affy_U133等
	 * 然后会到数据库中查找具体的芯片型号等
	 * @param dbName
	 */
	public boolean setDataBaseInfo(String dbName) {
		if (dbName == null || dbName.equals("")) {
			return false;
		}
		DBInfo databaseInfo = manageDBInfo.findByDBname(dbName);
		return setDataBaseInfo(databaseInfo);
	}
	
	public boolean setDataBaseInfo(DBInfo databaseInfo) {
		if (databaseInfo != null && (dbInfoID == null || !dbInfoID.equals(databaseInfo.getDbInfoID())) ) {
			this.dbInfoID = databaseInfo.getDbInfoID();
			return true;
		}
		return false;
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
		ManageNCBIUniID manageNCBIUniID = ManageNCBIUniID.getInstance();
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
	
	public void delete() {
		ManageNCBIUniID.getInstance().delete(this);
	}
	
}
