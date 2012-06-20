package com.novelbio.database.domain.geneanno;
/**
 * ��д��equal��hash
 * ֻҪ����ncbiid��geneID��ͬ������Ϊ������NCBIID��ͬ
 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
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

	public abstract String getGenUniID();
	public abstract void setGenUniID(String genUniID);
	/**
	 * ����ǡ������򷵻�null
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
	 * ֻҪ����ncbiid��geneID��ͬ������Ϊ������NCBIID��ͬ
	 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
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
	 * ��дhashcode��Ҳ�ǽ����geneID
	 */
	public int hashCode(){ 
		return getGenUniID().hashCode(); 
	}
	
}
