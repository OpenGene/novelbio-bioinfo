package com.novelbio.database.domain.kegg;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.database.domain.AbsPathway;
import com.novelbio.database.service.servkegg.ServKPathway;


@Document(collection="kgpathway")
public class KGpathway extends AbsPathway{
	/**
	 * the KEGGID of this pathway map.  example:<br>
	 * <b>path:ko*****     path:[org prefix]***** </b> the KEGGID of this pathway map ex) name="path:ko00010"   name="path:hsa00010"
	 */
	@Id
	protected String pathName;

	
	/**
	 * the map number of this pathway map. example:<br>
	 * <b>five-digit integer</b>   	ex) number="00030"
	 */
	private String mapNum;

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
	
	public static KGpathway queryKPathway(String kgPathID) {	
		ServKPathway servKPathway = ServKPathway.getInstance();
		return servKPathway.findByPathName(kgPathID);
	}
	
}
