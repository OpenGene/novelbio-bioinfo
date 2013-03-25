package com.novelbio.database.domain.geneanno;

import org.springframework.data.mongodb.core.index.Indexed;

public class UniGeneInfo extends AGeneInfo{
	@Indexed(unique = true)
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
