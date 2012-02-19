package com.novelbio.database.domain.kegg;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.domain.AbsPathway;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKPathway;

public class KGpathway extends AbsPathway{
	
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
		ServKPathway servKPathway = new ServKPathway();
		return servKPathway.queryKGpathway(kgPathID);
	}
	
}
