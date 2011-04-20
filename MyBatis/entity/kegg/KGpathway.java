package entity.kegg;

import java.util.ArrayList;
import java.util.HashMap;

public class KGpathway {
	/**
	 * the KEGGID of this pathway map.  example:<br>
	 * <b>path:ko*****     path:[org prefix]***** </b> the KEGGID of this pathway map ex) name="path:ko00010"   name="path:hsa00010"
	 */
	private String pathName;
	
	
	/**
	 * ko/ec/[org prefix].  example:<br>
	 * <b>ko</b>   the reference pathway map represented by KO identifiers<br>
	 * <b>ec</b>   the reference pathway map represented by ENZYME identifiers<br>
	 * <b>[org prefix]</b>   the organism-specific pathway map for "org"<br>
	 */
	private String org;
	
	/**
	 * the map number of this pathway map. example:<br>
	 * <b>five-digit integer</b>   	ex) number="00030"
	 */
	private String mapNum;
	
	/**
	 * the title of this pathway map. example:<br>
	 * <b>string</b> ex) title="Pentose phosphate pathway"
	 */
	private String title;
	
	/**
	 * the resource location of the image file of this pathway map.  example:<br>
	 * <b>URL</b>  ex) image="http://www.genome.jp/kegg/pathway/ko/ko00010.png"
	 */
	private String imageUrl;
	
	/**
	 * the resource location of the information about this pathway map.  example:<br>
	 * <b>URL</b>  ex) link="http://www.genome.jp/kegg-bin/show_pathway?ko00010"
	 */
	private String linkUrl;
	
	/**
	 * the KEGGID of this pathway map.  example:<br>
	 * <b>path:ko*****     path:[org prefix]***** </b> the KEGGID of this pathway map ex) name="path:ko00010"   name="path:hsa00010"
	 */
	public String getPathName()
	{
		return this.pathName;
	}
	/**
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
	 * the map number of this pathway map. example:<br>
	 * <b>five-digit integer</b>   	ex) number="00030"
	 */
	public String getMapNum() 
	{
		return this.mapNum;
	}
	/**
	 * already trim()
	 * the map number of this pathway map. example:<br>
	 * <b>five-digit integer</b>   	ex) number="00030"
	 */
	public void setMapNum(String mapNum) 
	{
		this.mapNum=mapNum.trim();
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
	
	/**
	 * @return
	 */
	public String getImageUrl() 
	{
		return this.imageUrl;
	}
	/**
	 * already trim()
	 * @param imageUrl
	 */
	public void setImageUrl(String imageUrl) 
	{
		this.imageUrl=imageUrl.trim();
	}
	/**
	 * @return
	 */
	public String getLinkUrl() 
	{
		return this.linkUrl;
	}
	public void setLinkUrl(String linkUrl) 
	{
		this.linkUrl=linkUrl.trim();
	}
	
	private int taxID;
	public int getTaxID() {
		return this.taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID=taxID;
	}
	
	
	
	
	
	
	
	
	
	
}
