package com.novelbio.database.domain;

public class AbsPathway {
	/**
	 * the KEGGID of this pathway map.  example:<br>
	 * <b>path:ko*****     path:[org prefix]***** </b> the KEGGID of this pathway map ex) name="path:ko00010"   name="path:hsa00010"
	 */
	protected String pathName;
	
	/**
	 * ko/ec/[org prefix].  example:<br>
	 * <b>ko</b>   the reference pathway map represented by KO identifiers<br>
	 * <b>ec</b>   the reference pathway map represented by ENZYME identifiers<br>
	 * <b>[org prefix]</b>   the organism-specific pathway map for "org"<br>
	 */
	protected String org;
	
	/**
	 * the title of this pathway map. example:<br>
	 * <b>string</b> ex) title="Pentose phosphate pathway"
	 */
	protected String title;
	
	/**
	 * the KEGGID of this pathway map.  example:<br>
	 * <b>path:ko*****     path:[org prefix]***** </b> the KEGGID of this pathway map ex) name="path:ko00010"   name="path:hsa00010"
	 */
	public String getPathName()
	{
		return this.pathName;
	}
	
	/**
	 * KEGG
	 * already trim()
	 * the KEGGID of this pathway map.  example:<br>
	 * <b>path:ko*****     path:[org prefix]***** </b> the KEGGID of this pathway map ex) name="path:ko00010"   name="path:hsa00010"
	 */
	public void setPathName(String pathName)
	{
		this.pathName=pathName.trim();
	}
	
	/**
	 * ko/ec/[org prefix].  example:<br>
	 * <b>ko</b>   the reference pathway map represented by KO identifiers<br>
	 * <b>ec</b>   the reference pathway map represented by ENZYME identifiers<br>
	 * <b>[org prefix]</b>   the organism-specific pathway map for "org"<br>
	 */
	public String getSpeciesID() 
	{
		return this.org;
	}
	
	/**
	 * already trim()
	 * ko/ec/[org prefix].  example:<br>
	 * <b>ko</b>   the reference pathway map represented by KO identifiers<br>
	 * <b>ec</b>   the reference pathway map represented by ENZYME identifiers<br>
	 * <b>[org prefix]</b>   the organism-specific pathway map for "org"<br>
	 */
	public void setSpecies(String org) 
	{
		this.org=org.trim();
	}
	
	/**
	 * already trim()
	 * the title of this pathway map. example:<br>
	 * <b>string</b> ex) title="Pentose phosphate pathway"
	 */
	public String getTitle() 
	{
		return this.title;
	}
	/**
	 * already trim()
	 * the title of this pathway map. example:<br>
	 * <b>string</b> ex) title="Pentose phosphate pathway"
	 */
	public void setTitle(String title) 
	{
		this.title=title.trim();
	}
	
	private int taxID;
	public int getTaxID() {
		return this.taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID=taxID;
	}
	
	
}
