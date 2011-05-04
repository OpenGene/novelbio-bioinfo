package entity.friceDB;

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
	
	
	
}
