package com.novelbio.database.entity.friceDB;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.mapper.geneanno.MapGo2Term;

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
		try {
			return Go2Term.getHashGo2Term().get(GOID).getGoID();
		} catch (Exception e) {
			return null;
		}
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
	 * û�оͷ���null
	 * @return
	 */
	public String getGOTerm() {
		try {
			return Go2Term.getHashGo2Term().get(GOID).getGoTerm();
		} catch (Exception e) {
			return null;
		}
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
	 * û�оͷ���null
	 * @return
	 */
	public String getFunction() {
		try {
			return Go2Term.getHashGo2Term().get(GOID).getGoFunction();
		} catch (Exception e) {
			return null;
		}
		
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
