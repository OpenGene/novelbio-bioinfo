package com.novelbio.database.domain.geneanno;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "unigene2go")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "go_tax_idx", def = "{'goID': 1, 'taxID': -1}")
 })
public class UniGene2Go extends AGene2Go{
	@Indexed
	private String uniID;
	
	public String getUniProtID() {
		return uniID;
	}
	public void setUniProtID(String uniProtID) {
		this.uniID = uniProtID.toLowerCase();
	}
	
	@Override
	public String getGeneUniId() {
		return uniID;
	}
	@Override
	public void setGeneUniID(String geneUniID) {
		setUniProtID(geneUniID);
	}
	
}
