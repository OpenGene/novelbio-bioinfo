package com.novelbio.database.domain.kegg;

public class KGpathRelation {

	
	/**
	 * ���໥���õĹ�ϵ����ָ��pathway�µ�
	 */
	private String pathName;
	
	
	/**
	 * ��pathway�����־���pathName
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
	 * �����Ƿ�KGML�е�maplink��ӽ���������˵�ٽ������������أ�
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
	 * �����Ƿ�KGML�е�maplink��ӽ���������˵�ٽ������������أ�
	 * <b>maplink</b> 	link to another map			
	 */
	public String getType() 
	{
		return this.type;
	}
	/**
	 * already trim()
	 * �����Ƿ�KGML�е�maplink��ӽ���������˵�ٽ������������أ�
	 * <b>maplink</b> 	link to another map			
	 */
	public void setType(String type)
	{
		this.type=type.trim();
	}
	
	/**
	 * ���໥���õĹ�ϵ������ָ����pathway�²���������
	 */
	public String getPathName() 
	{
		return this.pathName;
	}
	/**
	 * already trim()
	 * ���໥���õĹ�ϵ������ָ����pathway�²���������
	 */
	public void setPathName(String pathName) 
	{
		this.pathName=pathName.trim();
	}


}
