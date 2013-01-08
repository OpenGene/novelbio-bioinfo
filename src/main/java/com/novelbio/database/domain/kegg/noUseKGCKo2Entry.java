package com.novelbio.database.domain.kegg;

import java.util.ArrayList;

public class noUseKGCKo2Entry {

	/**
	 * geneID
	 */
	private String Ko;
	/**
	 * kegg ontology
	 */
	public void setKo(String Ko)
	{
		this.Ko=Ko;
	}
	/**
	 * kegg ontology
	 */
	public String getKo()
	{
		return Ko;
	}
	
	/**
	 * keggID
	 */
	private String keggID;
	/**
	 * keggID
	 */
	public void setKeggID(String keggID)
	{
		this.keggID=keggID;
	}
	/**
	 * keggID
	 */
	public String getKeggID()
	{
		return keggID;
	}
	
	/**
	 * taxID
	 */
	private int taxID;
	/**
	 * NCBIID表中的geneID
	 */
	public void setTaxID(int taxID)
	{
		this.taxID=taxID;
	}
	/**
	 * NCBIID表中的geneID
	 */
	public int getTaxID()
	{
		return taxID;
	}
	
	private ArrayList<KGentry> lsKGentries;
	public ArrayList<KGentry> getLsKGentries() {
		return this.lsKGentries;
	}
	public void setLsKGentries(ArrayList<KGentry> lsKGentries) {
		this.lsKGentries=lsKGentries;
	}
}
