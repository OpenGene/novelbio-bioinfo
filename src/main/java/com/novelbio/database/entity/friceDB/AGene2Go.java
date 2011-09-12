package com.novelbio.database.entity.friceDB;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.DAO.FriceDAO.DaoFSGo2Term;

/**
 * ��д��equal��hash
 * Ҫ����ncbiid��geneID��GOID����ͬ������Ϊ������NCBIID��ͬ
 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
 * 	@Override
 */
public abstract class AGene2Go {
	private String GOID;
	private String evidence;
	private String qualifier;
	private String GOTerm;
	private String reference;
	private String function;
	private String dataBase;
	
	public abstract String getGeneUniId();
	public abstract void setGeneUniID(String geneUniID);
	
	public String getGOID() {
		return GOID;
	}
	public void setGOID(String GOID) {
		this.GOID = GOID;
	}  
	
	public String getEvidence() {
		return evidence;
	}
	public void setEvidence(String evidence) {
		if (evidence == null || evidence.trim().equals("")) {
			return;
		}
		this.evidence = evidence;
	}
	
	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	/**
	 * ����Go2Term������У��
	 * @return
	 */
	public String getGOTerm() {
		return getHashGo2Term().get(GOID).getGoTerm();
	}
	
	public void setGOTerm(String GOTerm) {
		this.GOTerm = GOTerm;
	}
	
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	/**
	 * ����Go2Term������У��
	 * @return
	 */
	public String getFunction() {
		return getHashGo2Term().get(GOID).getGoFunction();
	}
	
	public void setFunction(String function) {
		this.function = function;
	}
	
	public String getDataBase() {
		return dataBase;
	}
	public void setDataBase(String dataBase) {
		this.dataBase = dataBase;
	}
	
	/**
	 * �洢Go2Term����Ϣ
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, Go2Term> hashGo2Term = new HashMap<String, Go2Term>();
	
	/**
	 * ������GO��Ϣ��ȡ��������hash���У��������
	 * �洢Go2Term����Ϣ
	 * key:GoID
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 * ����Ѿ������һ�Σ��Զ�����
	 */
	public static HashMap<String, Go2Term> getHashGo2Term() {
		if (hashGo2Term != null && hashGo2Term.size() > 0) {
			return hashGo2Term;
		}
		Go2Term go2Term = new Go2Term();
		ArrayList<Go2Term> lsGo2Terms = DaoFSGo2Term.queryLsGo2Term(go2Term);
		for (Go2Term go2Term2 : lsGo2Terms) 
		{
			hashGo2Term.put(go2Term2.getGoID(), go2Term2);
		}
		return hashGo2Term;
	}
	
	/**
	 * ֻҪ����gene2GO��geneID��ͬ������Ϊ������NCBIID��ͬ
	 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		AGene2Go otherObj = (AGene2Go)obj;
		
		if(getGeneUniId() == null || getGeneUniId().trim().equals("") || otherObj.getGeneUniId() == null || otherObj.getGeneUniId().trim().equals(""))
		{
			return false;
		}
		
		if (getGeneUniId().equals("0") || otherObj.getGeneUniId().equals("0") ) {
			return false;
		}
		
		return getGeneUniId().equals(otherObj.getGeneUniId())&&getGOID().equals(otherObj.getGOID());
	}
	/**
	 * ��дhashcode��Ҳ�ǽ����geneID
	 */
	public int hashCode(){
		String id = getGeneUniId()+getGOID();
		return id.hashCode(); 
	}
	
	
	
	
	
	
}
