package com.novelbio.database.entity.friceDB;

public class GeneInfo extends AGeneInfo{
	private long geneID;
	
	public long getGeneID() {
		return geneID;
	}
	public void setGeneID(long geneID) {
		this.geneID = geneID;
	}
	
	@Override
	public String getGeneUniID() {
		return geneID + "";
	}
	@Override
	public void setGeneUniID(String geneUniID) {
		setGeneID(Long.parseLong(geneUniID));
	}
	
	
}
