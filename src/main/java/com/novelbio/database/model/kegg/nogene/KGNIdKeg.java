package com.novelbio.database.model.kegg.nogene;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection="kgnIdkeg")
public class KGNIdKeg {
	/**
	 * 常用名，也就是待转换的ID
	 */
	@Id
	private String usualName;
	
	@Indexed
	private String kegID;
	

	/**
	 * 设定Compound等的KeggID
	 * @param kegID
	 */
	public void setKegID(String kegID) {
		this.kegID = kegID;
	}
	/**
	 * 获得Compound等的KeggID
	 * @return
	 */
	public String getKegID() {
		return this.kegID;
	}
	private String attribute;
	/**
	 * 设定KeggID的属性，也就是到底是Drug还是Compound还是其他的
	 * @param atrribute
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	/**
	 * 获得KeggID的属性，也就是到底是Drug还是Compound还是其他的
	 * @return
	 */
	public String getAttribute() {
		return this.attribute;
	}

	/**
	 * 常用名，也就是待转换的ID
	 * @param usualName
	 */
	public void setUsualName(String usualName) {
		this.usualName = usualName;
	}
	/**
	 * 常用名，也就是待转换的ID
	 * @return
	 */
	public String getUsualName() {
		return this.usualName;
	}
}
