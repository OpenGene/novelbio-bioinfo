package com.novelbio.database.entity.friceDB;

/**
 * 重写了equal，只要两个NCBIID初始化，并且他们的geneID都不为0，就比较geneID，一样的就认为两个一样
 * @author zong0jie
 *
 */
public class NCBIID {
    private int taxID;
	private long geneID;
	private String accessID;
	private String dbInfo;
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}

	public long getGeneId() {
		return geneID;
	}
	public void setGeneId(long geneID) {
		this.geneID = geneID;
	}
	
	public String getAccID() {
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
		
		NCBIID otherObj = (NCBIID)obj;
		if (geneID == 0 || otherObj.getGeneId() == 0) {
			return false;
		}
		return geneID == otherObj.getGeneId();
	}
	/**
	 * 重写hashcode，也是仅针对geneID
	 */
	public int hashCode(){ 
		return Long.valueOf(geneID).hashCode(); 
	}
	
}
