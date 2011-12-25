package com.novelbio.database.domain.geneanno;

public class UniGeneInfo extends AGeneInfo{
	private String uniProtID;
	
	public String getUniProtID() {
		return uniProtID;
	}
	public void setUniProtID(String uniProtID) {
		this.uniProtID = uniProtID;
	}
	
	@Override
	public String getGeneUniID() {
		return uniProtID;
	}
	@Override
	public void setGeneUniID(String geneUniID) {
		setUniProtID(geneUniID);
	}

}
