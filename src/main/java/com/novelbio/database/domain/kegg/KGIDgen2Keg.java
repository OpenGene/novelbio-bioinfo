package com.novelbio.database.domain.kegg;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="kggen2Keg")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "gene_tax_idx", def = "{'geneID': 1, 'taxID': -1}")
 })
public class KGIDgen2Keg {

	@Id
	private long geneID;

	@Indexed
	private String keggID;

	@Indexed
	private int taxID;
	/**
	 * NCBIID表中的geneID
	 */
	public void setGeneID(long geneID) {
		this.geneID=geneID;
	}
	/**
	 * NCBIID表中的geneID
	 */
	public long getGeneID() {
		return geneID;
	}
	

	/**
	 * keggID
	 */
	public void setKeggID(String keggID) {
		this.keggID=keggID;
	}
	/**
	 * keggID
	 */
	public String getKeggID() {
		return keggID;
	}
	

	/**
	 * NCBIID表中的geneID
	 */
	public void setTaxID(int taxID) {
		this.taxID=taxID;
	}
	/**
	 * NCBIID表中的geneID
	 */
	public int getTaxID() {
		return taxID;
	}
	
}
