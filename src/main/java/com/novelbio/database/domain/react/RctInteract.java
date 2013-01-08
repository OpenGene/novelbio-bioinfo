package com.novelbio.database.domain.react;

import java.io.Serializable;

public class RctInteract implements Serializable {
    
	private static final long serialVersionUID = 242025378167320872L;
	private int taxID;
	private String geneID1;
	private String dbInfo1;
	private String geneID2;
	private String dbInfo2;
	private String interaction;
	private String ictContext;
	private String pubmed;
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}

	public String getGeneId1() {
		return geneID1;
	}
	public void setGeneId1(String geneID1) {
		this.geneID1 = geneID1;
	}
	
	public String getGeneId2() {
		return geneID2;
	}
	public void setGeneId2(String geneID2) {
		this.geneID2 = geneID2;
	}
	
	public String getDbInfo1() {
		return dbInfo1;
	}
	public void setDbInfo1(String dbInfo1) {
		this.dbInfo1 = dbInfo1;
	}
	
	public String getDbInfo2() {
		return dbInfo2;
	}
	public void setDbInfo2(String dbInfo2) {
		this.dbInfo2 = dbInfo2;
	}  
	
	public String getInteraction() {
		return interaction;
	}
	public void setInteraction(String interaction) {
		this.interaction = interaction;
	}
	
	public String getIctContext() {
		return ictContext;
	}
	public void setIctContext(String ictContext) {
		this.ictContext = ictContext;
	}
	
	public String getPubmed() {
		return pubmed;
	}
	public void setPubmed(String pubmed) {
		this.pubmed = pubmed;
	}

}
