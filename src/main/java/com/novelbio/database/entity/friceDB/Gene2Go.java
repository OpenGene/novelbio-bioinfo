package com.novelbio.database.entity.friceDB;

public class Gene2Go extends AGene2Go{
	private long geneID;
	
	public long getGeneId() {
		return geneID;
	}
	public void setGeneId(long geneID) {
		this.geneID = geneID;
	}
	
	@Override
	public String getGeneUniId() {
		return geneID + "";
	}
	@Override
	public void setGeneUniID(String geneUniID) {
		setGeneId(Long.parseLong(geneUniID));
	}
}
