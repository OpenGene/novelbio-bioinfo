package com.novelbio.database.entity.kegg;

public class KGIDkeg2Ko {
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
}
