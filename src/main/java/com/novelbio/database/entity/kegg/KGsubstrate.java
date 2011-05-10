package com.novelbio.database.entity.kegg;


/**
 * 包括底物和产物的类，用type来区分底物和产物
 * @author zong0jie
 *
 */
public class KGsubstrate {

	/**
	 * substrate所在的pathway
	 */
	private String pathName;
	
	/**
	 * substrate所在的reaction
	 */
	private int reactionID;
	 
	/**
	 * 就两个，一个是substrate，一个是product
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
	 * substrate所在的pathway
	 * @return
	 */
	public String getPathName() {
		return this.pathName;
	}
	/**
	 * already trim()
	 * substrate所在的pathway
	 * @return
	 */
	public void setPathName(String pathName) {
		this.pathName=pathName.trim();
	}
	
	/**
	 * substrate所在的reaction
	 */
	public int getReactionID() {
		return this.reactionID;
	}
	/**
	 * substrate所在的reaction
	 */
	public void setReactionID(int reactionID) {
		this.reactionID=reactionID;
	}
	
	/**
	 * 就两个，一个是substrate，一个是product
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * already trim()
	 * 就两个，一个是substrate，一个是product
	 */
	public void setType(String type) {
		this.type=type.trim();
	}
	
}
