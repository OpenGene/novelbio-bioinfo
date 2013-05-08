package com.novelbio.database.domain.geneanno;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "geneinfo")
@CompoundIndexes({
    @CompoundIndex(unique = true, name = "gene_tax_idx", def = "{'geneID': 1, 'taxID': -1}"),
 })
public class GeneInfo extends AGeneInfo {
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
