package com.novelbio.database.entity.kegg;


/**
 * ��������Ͳ�����࣬��type�����ֵ���Ͳ���
 * @author zong0jie
 *
 */
public class KGsubstrate {

	/**
	 * substrate���ڵ�pathway
	 */
	private String pathName;
	
	/**
	 * substrate���ڵ�reaction
	 */
	private int reactionID;
	 
	/**
	 * ��������һ����substrate��һ����product
	 */
	private String type;
	/**
	 * the ID of this substrate
	 * the identification number of this substrate
	 */
	private int id;
	
	/**
	 * KEGGID of substrate node
	 * ex) cpd:C05378   gl:G00037
	 */
	private String name;
	
	/**
	 * the ID of this substrate
	 * the identification number of this substrate
	 */
	public int getID() {
		return this.id;
	}
	/**
	 * the ID of this substrate
	 * the identification number of this substrate
	 */
	public void setID(int id) {
		this.id=id;
	}
	
	/**
	 * KEGGID of substrate node
	 * ex) cpd:C05378   gl:G00037
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * already trim()
	 * KEGGID of substrate node
	 * ex) cpd:C05378   gl:G00037
	 */
	public void setName(String name) {
		this.name=name.trim();
	}
	
	/**
	 * substrate���ڵ�pathway
	 * @return
	 */
	public String getPathName() {
		return this.pathName;
	}
	/**
	 * already trim()
	 * substrate���ڵ�pathway
	 * @return
	 */
	public void setPathName(String pathName) {
		this.pathName=pathName.trim();
	}
	
	/**
	 * substrate���ڵ�reaction
	 */
	public int getReactionID() {
		return this.reactionID;
	}
	/**
	 * substrate���ڵ�reaction
	 */
	public void setReactionID(int reactionID) {
		this.reactionID=reactionID;
	}
	
	/**
	 * ��������һ����substrate��һ����product
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * already trim()
	 * ��������һ����substrate��һ����product
	 */
	public void setType(String type) {
		this.type=type.trim();
	}
	
}
