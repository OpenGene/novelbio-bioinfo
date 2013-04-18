package com.novelbio.database.domain.geneanno;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gene2go")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "go_tax_idx", def = "{'goID': 1, 'taxID': -1}")
 })
public class Gene2Go extends AGene2Go {
	@Indexed
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

}
