package com.novelbio.database.entity.kegg;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.entity.AbsPathway;
import com.novelbio.database.mapper.kegg.MapKPathway;

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
	/**
	 * �洢Go2Term����Ϣ
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, KGpathway> hashKGPath = new HashMap<String, KGpathway>();
	
	/**
	 * ������GO��Ϣ��ȡ��������hash���У��������
	 * �洢Go2Term����Ϣ
	 * key:GoID
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 * ����Ѿ������һ�Σ��Զ�����
	 */
	public static HashMap<String, KGpathway> getHashKGpath() {
		if (hashKGPath != null && hashKGPath.size() > 0) {
			return hashKGPath;
		}
		KGpathway kGpathway = new KGpathway();
		ArrayList<KGpathway> lsKGpathways = MapKPathway.queryLsKGpathways(kGpathway);
		for (KGpathway kGpathway2 : lsKGpathways) 
		{
			hashKGPath.put(kGpathway2.getPathName(), kGpathway2);
		}
		return hashKGPath;
	}
}
