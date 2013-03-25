package com.novelbio.database.domain.geneanno;

import org.springframework.data.mongodb.core.index.Indexed;

public class GeneInfo extends AGeneInfo{
	@Indexed(unique = true)
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
