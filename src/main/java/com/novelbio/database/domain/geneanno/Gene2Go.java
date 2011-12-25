package com.novelbio.database.domain.geneanno;

public class Gene2Go extends AGene2Go{
	private long geneID;
	
	public long getGeneId() {
		return geneID;
	}
	protected void setGeneId(long geneID) {
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
	public void setGeneUniID(long geneUniID) {
		setGeneId(geneUniID);
	}
}
