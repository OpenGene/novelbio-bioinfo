package com.novelbio.database.domain.geneanno;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "unigeneinfo")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "uni_tax_idx", def = "{'uniID': 1, 'taxID': -1}"),
 })
public class UniGeneInfo extends AGeneInfo{

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
