package com.novelbio.database.domain.kegg;

public class KGpathRelation {

	
	/**
	 * 本相互作用的关系是在指定pathway下的
	 */
	private String pathName;
	
	
	/**
	 * 本pathway，名字就是pathName
	 * the first (from) entry that defines this relation. detail:<br>
	 * the ID of node which takes part in this relation
	 */
	private String scrPath;
	
	/**
	 * the second (to) entry that defines this relation. detail<br>
	 * the ID of node which takes part in this relation
	 */
	private String trgPath;
	
	/**
	 * 考虑是否将KGML中的maplink添加进来，还是说促进或者是抑制呢？
	 * <b>maplink</b> 	link to another map													     <br> 
	 */
	private String type;
	


	
	/**
	 * the first (from) entry that defines this relation. detail:<br>
	 * the ID of node which takes part in this relation
	 * already trim()
	 */
	public String getScrPath() 
	{
		return this.scrPath;
	}
	/**
	 * the first (from) entry that defines this relation. detail:<br>
	 * the ID of node which takes part in this relation
	 * already trim()
	 */
	public void setScrPath(String scrPath) 
	{
		this.scrPath=scrPath.trim();
	}
	
	/**
	 * the second (to) entry that defines this relation. detail<br>
	 * the ID of node which takes part in this relation
	 * already trim()
	 */
	public String getTrgPath() 
	{
		return this.trgPath;
	}
	/**
	 * the second (to) entry that defines this relation. detail<br>
	 * the ID of node which takes part in this relation
	 * already trim()
	 */
	public void setTrgPath(String trgPath) 
	{
		this.trgPath=trgPath.trim();
	}
	
	/**
	 * 考虑是否将KGML中的maplink添加进来，还是说促进或者是抑制呢？
	 * <b>maplink</b> 	link to another map			
	 */
	public String getType() 
	{
		return this.type;
	}
	/**
	 * already trim()
	 * 考虑是否将KGML中的maplink添加进来，还是说促进或者是抑制呢？
	 * <b>maplink</b> 	link to another map			
	 */
	public void setType(String type)
	{
		this.type=type.trim();
	}
	
	/**
	 * 本相互作用的关系必须在指定的pathway下才能有作用
	 */
	public String getPathName() 
	{
		return this.pathName;
	}
	/**
	 * already trim()
	 * 本相互作用的关系必须在指定的pathway下才能有作用
	 */
	public void setPathName(String pathName) 
	{
		this.pathName=pathName.trim();
	}


}
