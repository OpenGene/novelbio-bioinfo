package com.novelbio.database.entity.friceDB;

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
	
	public String getGOTerm() {
		return GOTerm;
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
	
	public String getFunction() {
		return function;
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
}
