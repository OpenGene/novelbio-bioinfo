package com.novelbio.database.domain.kegg;

import java.util.ArrayList;



public class KGCKo2Gen {

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

	/**
	 * ko和kegID是多对多的关系
	 */
	public ArrayList<KGIDgen2Keg> lsKgIDgen2Keg;
	/**
	 * kegID和genID是一对一的关系
	 */
	public void setLsKGIDgen2Keg(ArrayList<KGIDgen2Keg> lsKgIDgen2Keg)
	{
		this.lsKgIDgen2Keg=lsKgIDgen2Keg;
	}
	/**
	 * kegID和genID是一对一的关系
	 */
	public ArrayList<KGIDgen2Keg> getLsKGIDgen2Keg()
	{
		return lsKgIDgen2Keg;
	}
}
