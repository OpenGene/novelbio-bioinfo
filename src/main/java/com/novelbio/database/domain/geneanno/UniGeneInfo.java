package com.novelbio.database.domain.geneanno;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "unigeneinfo")
public class UniGeneInfo extends AGeneInfo{
	@Indexed(unique = true)
	private String uniID;
	
	public String getUniProtID() {
		return uniID;
	}
	public void setUniProtID(String uniProtID) {
		this.uniID = uniProtID.toLowerCase();
	}
	
	@Override
	public String getGeneUniID() {
		return uniID;
	}
	@Override
	public void setGeneUniID(String geneUniID) {
		setUniProtID(geneUniID);
	}

}
