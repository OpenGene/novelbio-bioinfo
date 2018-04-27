package com.novelbio.database.model.kegg;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="kgreaction")
@CompoundIndexes({
    @CompoundIndex(unique = false, name = "name_path_idx", def = "{'name': 1, 'pathName': -1}")
 })
public class KGreaction {
	@Id
	String id;
	/**
	 * the ID of this reaction,和Entry的ID是同一个
	 */
	private int reactionId;
	 
	/**
	 * the KEGGID of this reaction.  example:<br>
	 * ex) reaction="rn:R02749"
	 */
	private String name;
	
	/**
	 * 这个反应的ID是在特定的pathway下的
	 */
	@Indexed
	private String pathName;
	
	
	/**
	 * the type of this reaction<br>
	 * reversible reaction<br>
	 * irreversible reaction
	 */
	private String type;
 
	/**
	 * The alt element specifies the alternative name of its parent element.
	 */
	private String alt;
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	/**
	 * the ID of this reaction,和Entry的ID是同一个
	 * 这个反应的ID是在特定的pathway下的
	 */
	public int getReactionId() {
		return this.reactionId;
	}
	/**
	 * the ID of this reaction,和Entry的ID是同一个
	 * 这个反应的ID是在特定的pathway下的
	 */
	public void setReactionId(int id) {
		 this.reactionId=id;
	}
	
	/**
	 * the KEGGID of this reaction.  example:<br>
	 * ex) reaction="rn:R02749"
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * already trim()
	 * the KEGGID of this reaction.  example:<br>
	 * ex) reaction="rn:R02749"
	 */
	public void setName(String name) {
		 this.name=name.trim();
	}
	
	/**
	 * the type of this reaction<br>
	 * reversible reaction<br>
	 * irreversible reaction
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * already trim()
	 * the type of this reaction<br>
	 * reversible reaction<br>
	 * irreversible reaction
	 */
	public void setType(String type) {
		this.type=type.trim();
	}
 
	/**
	 * The alt element specifies the alternative name of its parent element.
	 */
	public String getAlt() {
		return this.alt;
	}
	/**
	 * already trim()
	 * The alt element specifies the alternative name of its parent element.
	 */
	public void setAlt(String alt) {
		this.alt=alt.trim();
	}
	/**
	 * 这个反应是在特定的pathway下的
	 */
	public String getPathName() {
		return this.pathName;
	}
	/**
	 * already trim()
	 * 这个反应是在特定的pathway下的
	 */
	public void setPathName(String pathName) {
		this.pathName=pathName.trim();
	}
}
