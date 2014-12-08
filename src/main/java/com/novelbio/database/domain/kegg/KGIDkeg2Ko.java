package com.novelbio.database.domain.kegg;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="kgidkeg2ko")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "keggID_Ko_idx", def = "{'keggID': 1, 'Ko': -1}")
 })
public class KGIDkeg2Ko {
	/** Mongodb的id */
	@Id
	String kgIdinside;
	/** keggID */
	private String keggID;
	/** KoId */
	@Indexed
	private String ko;
	/** taxID */
	private int taxID;
	/**
	 * kegg ontology
	 */
	public void setKo(String Ko) {
		this.ko = Ko;
	}
	/**
	 * kegg ontology
	 */
	public String getKo() {
		return ko;
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
