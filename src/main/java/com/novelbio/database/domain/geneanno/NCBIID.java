package com.novelbio.database.domain.geneanno;

import com.novelbio.database.model.modgeneid.GeneID;

/**
 * ��д��equal��ֻҪ����NCBIID��ʼ�����������ǵ�geneID����Ϊ0���ͱȽ�geneID��һ���ľ���Ϊ����һ��
 * @author zong0jie
 *
 */
public class NCBIID extends AgeneUniID{
	private long geneID;

	public long getGeneId() {
		return geneID;
	}
	public void setGeneId(long geneID) {
		this.geneID = geneID;
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
	@Override
	public String getGenUniID() {
		return geneID + "";
	}
	@Override
	public void setGenUniID(String genUniID) {
		setGeneId(Long.parseLong(genUniID));
	}
	@Override
	public String getGeneIDtype() {
		// TODO Auto-generated method stub
		return GeneID.IDTYPE_GENEID;
	}
	
}
