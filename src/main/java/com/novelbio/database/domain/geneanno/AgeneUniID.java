package com.novelbio.database.domain.geneanno;
/**
 * 重写了equal和hash
 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
 */
public abstract class AgeneUniID {
    private int taxID;
	private String accessID;
	private String dbInfo;
	
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
	
	public String getDBInfo() {
		return dbInfo;
	}
	public void setDBInfo(String dbInfo) {
		this.dbInfo = dbInfo;
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
	
}
