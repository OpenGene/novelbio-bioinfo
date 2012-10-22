package com.novelbio.database.domain.geneanno;

import com.novelbio.database.model.modgeneid.GeneID;

/**
 * ��д��equal��ֻҪ����UniProtID��ʼ�����������ǵ�UniID����Ϊnull��""���ͱȽ�UniID��һ���ľ���Ϊ����һ��
 * @author zong0jie
 *
 */
public class UniProtID extends AgeneUniID{
		private String uniID;

		public String getUniID() {
			return uniID;
		}
		public void setUniID(String uniID) {
			this.uniID =uniID;
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
		@Override
		public String getGenUniID() {
			return uniID;
		}
		@Override
		public void setGenUniID(String genUniID) {
			setUniID(genUniID);
		}
		@Override
		public String getGeneIDtype() {
			return GeneID.IDTYPE_UNIID;
		}
}
