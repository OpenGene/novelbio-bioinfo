package com.novelbio.database.entity.friceDB;

/**
 * ��д��equal��ֻҪ����NCBIID��ʼ�����������ǵ�geneID����Ϊ0���ͱȽ�geneID��һ���ľ���Ϊ����һ��
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
	 * ֻҪ����ncbiid��geneID��ͬ������Ϊ������NCBIID��ͬ
	 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
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
	 * ��дhashcode��Ҳ�ǽ����geneID
	 */
	public int hashCode(){ 
		return Long.valueOf(geneID).hashCode(); 
	}
	
}
