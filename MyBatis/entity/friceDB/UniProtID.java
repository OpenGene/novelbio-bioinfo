package entity.friceDB;

public class UniProtID {
	   private int taxID;
		private String uniID;
		private String accessID;
		private String dbInfo;
		
		public long getTaxID() {
			return taxID;
		}
		public void setTaxID(int taxID) {
			this.taxID = taxID;
		}

		public String getUniID() {
			return uniID;
		}
		public void setUniID(String uniID) {
			this.uniID =uniID;
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
