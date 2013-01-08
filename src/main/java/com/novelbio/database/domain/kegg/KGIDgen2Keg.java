package com.novelbio.database.domain.kegg;

public class KGIDgen2Keg {
	/**
	 * geneID
	 */
	private long geneID;
	/**
	 * NCBIID表中的geneID
	 */
	public void setGeneID(long geneID)
	{
		this.geneID=geneID;
	}
	/**
	 * NCBIID表中的geneID
	 */
	public long getGeneID()
	{
		return geneID;
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
