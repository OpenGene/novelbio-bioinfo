package com.novelbio.database.entity.friceDB;
/**
 * ��д��equal��ֻҪ����UniProtID��ʼ�����������ǵ�UniID����Ϊnull��""���ͱȽ�UniID��һ���ľ���Ϊ����һ��
 * @author zong0jie
 *
 */
public class UniProtID {
	   private int taxID;
		private String uniID;
		private String accessID;
		private String dbInfo;
		
		public int getTaxID() {
			return taxID;
		}
		public void setTaxID(int l) {
			this.taxID = l;
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
		
		/**
		 * ֻҪ����uniprotID��UniID��ͬ������Ϊ������uniprotID��ͬ
		 * �������UniIDΪ""��null��Ҳ����uniprotID����û�г�ʼ������ôֱ�ӷ���false
		 * 	@Override
		 */
		public boolean equals(Object obj) {
			if (this == obj) return true;
			
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			UniProtID otherObj = (UniProtID)obj;
			if (uniID == null || uniID.trim().equals("") || otherObj.getUniID() == null || otherObj.getUniID().trim().equals("")) {
				return false;
			}
			return uniID.equals(otherObj.getUniID());
		}
		/**
		 * ��дhashcode��Ҳ�ǽ����uniID
		 */
		public int hashCode(){ 
			return uniID.hashCode(); 
		}
		
}
